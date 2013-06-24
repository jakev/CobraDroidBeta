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

import com.android.cts.verifier.PassFailButtons;
import com.android.cts.verifier.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

/**
 * Main UI for the Android Audio Quality Verifier.
 */
public class AudioQualityVerifierActivity extends PassFailButtons.Activity
        implements View.OnClickListener, OnItemClickListener {
    public static final String TAG = "AudioQualityVerifier";

    public static final int SAMPLE_RATE = 16000;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BYTES_PER_SAMPLE = 2;
    public static final int PLAYBACK_STREAM = AudioManager.STREAM_MUSIC;

    // Intent Extra definitions, which must match those in
    // com.google.android.voicesearch.speechservice.RecognitionController
    public static final String EXTRA_RAW_AUDIO =
            "android.speech.extras.RAW_AUDIO";

    // Communication with ExperimentService
    public static final String ACTION_EXP_STARTED =
            "com.android.cts.verifier.audioquality.EXP_STARTED";
    public static final String ACTION_EXP_FINISHED =
            "com.android.cts.verifier.audioquality.EXP_FINISHED";
    public static final String EXTRA_ABORTED =
            "com.android.cts.verifier.audioquality.ABORTED";
    public static final String EXTRA_EXP_ID =
            "com.android.cts.verifier.audioquality.EXP_ID";
    public static final String EXTRA_RUN_ALL =
            "com.android.cts.verifier.audioquality.RUN_ALL";

    // Communication with ViewResultsActivity
    public static final String EXTRA_RESULTS =
            "com.android.cts.verifier.audioquality.RESULTS";

    private Button mCalibrateButton;
    private Button mRunAllButton;
    private Button mStopButton;
    private Button mViewResultsButton;
    private Button mClearButton;

    private ListView mList;
    private TwoColumnAdapter mAdapter;

    private ProgressBar mProgress;

    private ArrayList<Experiment> mExperiments;

    private boolean mRunningExperiment;

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aq_verifier_activity);
        setPassFailButtonClickListeners();
        setInfoResources(R.string.aq_verifier, R.string.aq_verifier_info, -1);

        mCalibrateButton = (Button) findViewById(R.id.calibrateButton);
        mRunAllButton = (Button) findViewById(R.id.runAllButton);
        mStopButton = (Button) findViewById(R.id.stopButton);
        mViewResultsButton = (Button) findViewById(R.id.viewResultsButton);
        mClearButton = (Button) findViewById(R.id.clearButton);

        mCalibrateButton.setOnClickListener(this);
        mRunAllButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mViewResultsButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);

        mStopButton.setEnabled(false);

        mProgress = (ProgressBar) findViewById(R.id.progress);

        mList = (ListView) findViewById(R.id.list);
        mAdapter = new TwoColumnAdapter(this);

        mExperiments = VerifierExperiments.getExperiments(this);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                experimentReplied(intent);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EXP_STARTED);
        filter.addAction(ACTION_EXP_FINISHED);
        registerReceiver(mReceiver, filter);

        fillAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        checkNotSilent();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged(); // Update List UI
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        checkNotSilent();
    }

    private void checkNotSilent() {
        AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(PLAYBACK_STREAM, false);
        int volume = mgr.getStreamVolume(PLAYBACK_STREAM);
        int max = mgr.getStreamMaxVolume(PLAYBACK_STREAM);
        Log.i(TAG, "Volume " + volume + ", max " + max);
        if (volume <= max / 10) {
            // Volume level is silent or very quiet; increase to two-thirds
            mgr.setStreamVolume(PLAYBACK_STREAM, (max * 2) / 3, AudioManager.FLAG_SHOW_UI);
        }
    }

    // Called when an experiment has completed
    private void experimentReplied(Intent intent) {
        String action = intent.getAction();
        if (ACTION_EXP_STARTED.equals(action)) {
            mStopButton.setEnabled(true);
            mRunAllButton.setEnabled(false);
        } else if (ACTION_EXP_FINISHED.equals(action)) {
            boolean mRunAll = intent.getBooleanExtra(EXTRA_RUN_ALL, false);
            boolean aborted = intent.getBooleanExtra(EXTRA_ABORTED, true);
            int expID = intent.getIntExtra(EXTRA_EXP_ID, -1);
            if (mRunAll && !aborted) {
                while (expID < mExperiments.size() - 1) {
                    if (runExperiment(++expID, true)) {
                        // OK, experiment is running
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                    // Otherwise, loop back and try the next experiment
                }
            }
            mStopButton.setEnabled(false);
            mRunAllButton.setEnabled(true);
            mRunningExperiment = false;
            mProgress.setVisibility(ProgressBar.INVISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    // Implements AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mRunningExperiment) return;
        runExperiment(position, false);
    }

    // Begin an experiment. Returns false if the experiment is not enabled.
    private boolean runExperiment(int which, boolean all) {
        Experiment exp = mExperiments.get(which);
        if (!exp.isEnabled()) return false;
        Intent intent = new Intent(this, ExperimentService.class);
        intent.putExtra(EXTRA_EXP_ID, which);
        intent.putExtra(EXTRA_RUN_ALL, all);
        startService(intent);
        mRunningExperiment = true;
        mAdapter.notifyDataSetChanged();
        mProgress.setVisibility(ProgressBar.VISIBLE);
        return true;
    }

    // Implements View.OnClickListener:
    public void onClick(View v) {
        if (v == mCalibrateButton) {
            Intent intent = new Intent(this, CalibrateVolumeActivity.class);
            startActivity(intent);
        } else if (v == mRunAllButton) {
            if (mRunningExperiment) return;
            int expID = -1;
            while (expID < mExperiments.size() - 1) {
                if (runExperiment(++expID, true)) break;
            }
        } else if (v == mStopButton) {
            Intent intent = new Intent(this, ExperimentService.class);
            stopService(intent);
        } else if (v == mViewResultsButton) {
            Intent intent = new Intent(this, ViewResultsActivity.class);
            intent.putExtra(EXTRA_RESULTS, genReport());
            startActivity(intent);
        } else if (v == mClearButton) {
            clear();
        }
    }

    private void fillAdapter() {
        mAdapter.clear();
        for (Experiment exp : mExperiments) {
            mAdapter.add(exp.getName());
        }
    }

    class TwoColumnAdapter extends ArrayAdapter<String> {
        TwoColumnAdapter(Context context) {
            super(context, R.layout.aq_row);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.aq_row, parent, false);
            TextView nameField = (TextView) row.findViewById(R.id.testName);
            TextView scoreField = (TextView) row.findViewById(R.id.testScore);
            Experiment exp = mExperiments.get(position);
            nameField.setText(exp.getName());
            scoreField.setText(exp.getScore());
            if (exp.isRunning()) {
                Typeface tf = nameField.getTypeface();
                nameField.setTypeface(tf, 1);
            }
            if (!exp.isEnabled()) {
                nameField.setTextColor(Color.GRAY);
            }
            return row;
        }
    }

    private String genReport() {
        StringBuilder sb = new StringBuilder();
        for (Experiment exp : mExperiments) {
            exp.getReport(sb);
        }
        return sb.toString();
    }

    private void clear() {
        if (mRunningExperiment) {
            Intent intent = new Intent(this, ExperimentService.class);
            stopService(intent);
        }
        for (Experiment exp : mExperiments) {
            exp.reset();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
