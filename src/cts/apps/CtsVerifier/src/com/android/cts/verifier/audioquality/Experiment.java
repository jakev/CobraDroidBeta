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

import java.util.ArrayList;
import java.util.List;
import com.android.cts.verifier.R;

import android.content.Context;
import android.util.Log;

/**
 * The base class for all audio experiments.
 */
public class Experiment implements Runnable {
    protected static final String TAG = "AudioQualityVerifier";

    private static final int DEFAULT_TIMEOUT = 5; // In seconds

    private String mName;
    private String mScore;
    private String mReport;
    private List<String> mAudioFileNames;

    enum Status { NotStarted, Running, Stopped, Completed }
    private Status mStatus;
    private boolean mEnabled;

    protected Native mNative;
    protected Context mContext;
    protected Terminator mTerminator;

    public Experiment(boolean enable) {
        mEnabled = enable;
        mNative = Native.getInstance();
        reset();
    }

    public void init(Context context) {
        mName = lookupName(context);
    }

    protected String lookupName(Context context) {
        return context.getString(R.string.aq_default_exp);
    }

    protected String getString(int resId) {
        return mContext.getString(resId);
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void reset() {
        mStatus = Status.NotStarted;
        mScore = "";
        mReport = "";
        mAudioFileNames = new ArrayList<String>();
    }

    public void start() {
        mStatus = Status.Running;
    }

    protected void setScore(String score) {
        mScore = score;
    }

    protected void setReport(String report) {
        mReport = report;
    }

    // Implements Runnable
    public void run() {}

    public void run(Context context, Terminator t) {
        mContext = context;
        mTerminator = t;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void setRecording(byte[] data) {
        setRecording(data, -1);
    }

    public void setRecording(byte[] data, int num) {
        // Save captured data to file
        String filename = Utils.getExternalDir(mContext, this) + "/"
            + Utils.cleanString(getName())
            + (num == -1 ? "" : "_" + String.valueOf(num)) + ".raw";
        Log.i(TAG, "Saving recorded data to " + filename);
        Utils.saveFile(filename, data);
        mAudioFileNames.add(filename);
    }

    public List<String> getAudioFileNames() {
        return mAudioFileNames;
    }

    // Timeout in seconds
    public int getTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public void cancel() {
        mStatus = Status.Stopped;
    }

    public void stop() {
        mStatus = Status.Completed;
    }

    public boolean isRunning() {
        return mStatus == Status.Running;
    }

    public String getName() {
        return mName;
    }

    public String getScore() {
        switch (mStatus) {
            case NotStarted:
                return "-";
            case Running:
                return "...";
            case Stopped:
                return "-";
            case Completed:
                return mScore;
        }
        return "";
    }

    public void getReport(StringBuilder sb) {
        sb.append(getName());
        sb.append(": ");
        sb.append(getScore());
        sb.append("\n");
        sb.append(mReport);
        sb.append("\n\n");
    }
}
