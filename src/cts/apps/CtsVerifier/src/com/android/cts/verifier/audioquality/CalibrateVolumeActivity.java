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

import com.android.cts.verifier.R;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Play a continuous sound and allow the user to monitor the sound level
 * at the mic in real time, relative to the range the phone can detect.
 * This is not an absolute sound level meter, but lets the speaker volume
 * and position be adjusted so that the clipping point is known.
 */
public class CalibrateVolumeActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "AudioQualityVerifier";

    public static final int OUTPUT_AMPL = 5000;
    public static final float TARGET_RMS = 5000.0f;
    public static final float TARGET_AMPL = (float) (TARGET_RMS * Math.sqrt(2.0));
    private static final float FREQ = 625.0f;

    private static final float TOLERANCE = 1.03f;

    private static final int DEBOUNCE_TIME = 500; // Minimum time in ms between status text changes
    public static final boolean USE_PINK = false;

    private ProgressBar mSlider;
    private Button mDoneButton;
    private TextView mStatus;
    private BackgroundAudio mBackgroundAudio;
    private Monitor mMonitor;
    private Handler mHandler;

    private Native mNative;

    enum Status { LOW, OK, HIGH, UNDEFINED }
    private static int[] mStatusText = {
        R.string.aq_status_low, R.string.aq_status_ok, R.string.aq_status_high };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aq_sound_level_meter);

        mSlider = (ProgressBar) findViewById(R.id.slider);
        mStatus = (TextView) findViewById(R.id.status);
        mStatus.setText(R.string.aq_status_unknown);

        mDoneButton = (Button) findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(this);

        mNative = Native.getInstance();
        mHandler = new UpdateHandler();
    }

    // Implements View.OnClickListener
    public void onClick(View v) {
        if (v == mDoneButton) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int DURATION = 1;
        final float RAMP = 0.0f;
        byte[] noise;
        if (USE_PINK) {
            noise = Utils.getPinkNoise(this, OUTPUT_AMPL, DURATION);
        } else {
            short[] sinusoid = mNative.generateSinusoid(FREQ, DURATION,
                    AudioQualityVerifierActivity.SAMPLE_RATE, OUTPUT_AMPL, RAMP);
            noise = Utils.shortToByteArray(sinusoid);
        }
        float[] results = mNative.measureRms(Utils.byteToShortArray(noise),
                AudioQualityVerifierActivity.SAMPLE_RATE, -1.0f);
        float rms = results[Native.MEASURE_RMS_RMS];
        Log.i(TAG, "Stimulus amplitude " + OUTPUT_AMPL + ", RMS " + rms);
        mBackgroundAudio = new BackgroundAudio(noise);
        mMonitor = new Monitor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBackgroundAudio.halt();
        mMonitor.halt();
    }

    private class UpdateHandler extends Handler {
        private Status mState = Status.UNDEFINED;
        private long mTimestamp = 0; // Time of last status change in ms

        @Override
        public void handleMessage(Message msg) {
            int rms = msg.arg1;
            int max = mSlider.getMax();
            int progress = (max / 2 * rms) / Math.round(TARGET_RMS);
            if (progress > max) progress = max;
            mSlider.setProgress(progress);

            Status state;
            if (rms * TOLERANCE < TARGET_RMS) state = Status.LOW;
            else if (rms > TARGET_RMS * TOLERANCE) state = Status.HIGH;
            else state = Status.OK;
            if (state != mState) {
                long timestamp = System.currentTimeMillis();
                if (timestamp - mTimestamp > DEBOUNCE_TIME) {
                    mStatus.setText(mStatusText[state.ordinal()]);
                    mState = state;
                    mTimestamp = timestamp;
                }
            }
        }
    }

    class Monitor extends Thread {
        private static final int BUFFER_TIME = 100; // Min time in ms to buffer for
        private static final int READ_TIME = 25;    // Max length of time in ms to read in one chunk
        private static final boolean DEBUG = false;

        private AudioRecord mRecord;
        private int mSamplesToRead;
        private byte[] mBuffer;
        private boolean mProceed;

        Monitor() {
            mProceed = true;

            mSamplesToRead = (READ_TIME * AudioQualityVerifierActivity.SAMPLE_RATE) / 1000;
            mBuffer = new byte[mSamplesToRead * AudioQualityVerifierActivity.BYTES_PER_SAMPLE];

            final int minBufferSize = (BUFFER_TIME * AudioQualityVerifierActivity.SAMPLE_RATE *
                    AudioQualityVerifierActivity.BYTES_PER_SAMPLE) / 1000;
            final int bufferSize = Utils.getAudioRecordBufferSize(minBufferSize);

            mRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    AudioQualityVerifierActivity.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioQualityVerifierActivity.AUDIO_FORMAT, bufferSize);
            if (mRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Couldn't open audio for recording");
                return;
            }
            mRecord.startRecording();
            if (mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                Log.e(TAG, "Couldn't record");
                return;
            }

            start(); // Begin background thread
        }

        public void halt() {
            mProceed = false;
        }

        @Override
        public void run() {
            int maxBytes = mSamplesToRead * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
            int bytes;
            while (true) {
                if (!mProceed) {
                    mRecord.stop();
                    mRecord.release();
                    return; // End thread
                }
                bytes = mRecord.read(mBuffer, 0, maxBytes);
                if (bytes < 0) {
                    if (bytes == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "Recording object not initalized");
                    } else if (bytes == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "Invalid recording parameters");
                    } else {
                        Log.e(TAG, "Error during recording");
                    }
                    return;
                }
                if (bytes >= 2) {
                    // Note: this won't work well if bytes is small (we should check)
                    short[] samples = Utils.byteToShortArray(mBuffer, 0, bytes);
                    float[] results = mNative.measureRms(samples,
                            AudioQualityVerifierActivity.SAMPLE_RATE, -1.0f);
                    float rms = results[Native.MEASURE_RMS_RMS];
                    float duration = results[Native.MEASURE_RMS_DURATION];
                    float mean = results[Native.MEASURE_RMS_MEAN];
                    if (DEBUG) {
                        // Confirm the RMS calculation
                        float verifyRms = 0.0f;
                        for (short x : samples) {
                            verifyRms += x * x;
                        }
                        verifyRms /= samples.length;
                        Log.i(TAG, "RMS: " + rms + ", bytes: " + bytes
                                + ", duration: " + duration + ", mean: " + mean
                                + ", manual RMS: " + Math.sqrt(verifyRms));
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = Math.round(rms);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }
}
