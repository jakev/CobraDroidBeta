/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.cts.verifier.audioquality;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Launch an experiment.
 *
 * Experiments are decoupled from the UI both so that they can run on a
 * background thread without freezing the UI, and to support experiments
 * which take over the screen from the UI (such as invoking Voice Search).
 */
public class ExperimentService extends Service implements Terminator {
    private static final String TAG = "AudioQualityVerifier";
    private static int BACKGROUND_LOAD = 0;

    private ArrayList<Experiment> mExperiments;
    private Experiment mExp;

    private TimeoutHandler mHandler = null;
    private PowerManager.WakeLock mWakeLock = null;

    private boolean mRunAll;
    private int mExpID;

    private boolean mActive;
    private LoadGenerator[] mLoadGenerator = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mExperiments = VerifierExperiments.getExperiments(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        terminate(true);
    }

    /**
     * Implements Terminator, to clean up when the experiment indicates it
     * has completed.
     */
    public void terminate(boolean aborted) {
        if (mLoadGenerator != null) {
            for (LoadGenerator generator : mLoadGenerator) {
                generator.halt();
            }
            mLoadGenerator = null;
        }
        if (!mActive) return;
        mActive = false;
        if (mHandler != null) mHandler.clear();
        if (aborted) {
            mExp.cancel();
        } else {
            mExp.stop();
        }
        Intent intent = new Intent(AudioQualityVerifierActivity.ACTION_EXP_FINISHED);
        intent.putExtra(AudioQualityVerifierActivity.EXTRA_EXP_ID, mExpID);
        intent.putExtra(AudioQualityVerifierActivity.EXTRA_RUN_ALL, mRunAll);
        intent.putExtra(AudioQualityVerifierActivity.EXTRA_ABORTED, aborted);
        sendBroadcast(intent);
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mActive = true;

        // Obtain wakelock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        if (BACKGROUND_LOAD > 0) {
            mLoadGenerator = new LoadGenerator[BACKGROUND_LOAD];
            for (int i = 0; i < BACKGROUND_LOAD; i++) {
                mLoadGenerator[i] = new LoadGenerator();
            }
        }

        mExpID = intent.getIntExtra(AudioQualityVerifierActivity.EXTRA_EXP_ID, -1);
        if (mExpID == -1) {
            Log.e(TAG, "Invalid test ID");
            System.exit(0);
        }
        mRunAll = intent.getBooleanExtra(AudioQualityVerifierActivity.EXTRA_RUN_ALL, false);
        mExp = mExperiments.get(mExpID);
        mExp.start();

        // Inform the VerifierActivity Activity that we have started:
        Intent feedback = new Intent(AudioQualityVerifierActivity.ACTION_EXP_STARTED);
        feedback.putExtra(AudioQualityVerifierActivity.EXTRA_EXP_ID, mExpID);
        sendBroadcast(feedback);

        mHandler = new TimeoutHandler();
        mHandler.delay(mExp.getTimeout());
        mExp.run(this, this);

        return START_NOT_STICKY;
    }

    class TimeoutHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            terminate(true);
            stopSelf();
        }

        public void delay(int secs) {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), secs * 1000);
        }

        public void clear() {
            removeMessages(0);
        }
    }
}
