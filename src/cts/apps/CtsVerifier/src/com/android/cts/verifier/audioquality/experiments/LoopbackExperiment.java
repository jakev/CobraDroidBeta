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

package com.android.cts.verifier.audioquality.experiments;

import com.android.cts.verifier.R;
import com.android.cts.verifier.audioquality.AudioQualityVerifierActivity;
import com.android.cts.verifier.audioquality.Experiment;
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * LoopbackExperiment represents a general class of experiments, all of which
 * comprise playing an audio stimulus of some kind, whilst simultaneously
 * recording from the microphone. The recording is then analyzed to determine
 * the test results (score and report).
 */
public class LoopbackExperiment extends Experiment {
    protected static final int TIMEOUT = 10;

    // Amount of silence in ms before and after playback
    protected static final int END_DELAY_MS = 500;

    private Recorder mRecorder = null;

    public LoopbackExperiment(boolean enable) {
        super(enable);
    }

    protected byte[] getStim(Context context) {
        int stimNum = 2;
        byte[] data = Utils.getStim(context, stimNum);
        return data;
    }

    @Override
    public void run() {
        byte[] playbackData = getStim(mContext);
        byte[] recordedData = loopback(playbackData);

        compare(playbackData, recordedData);
        setRecording(recordedData);
        mTerminator.terminate(false);
    }

    protected byte[] loopback(byte[] playbackData) {
        int samples = playbackData.length / 2;
        int duration = (samples * 1000) / AudioQualityVerifierActivity.SAMPLE_RATE; // In ms
        int padSamples = (END_DELAY_MS * AudioQualityVerifierActivity.SAMPLE_RATE) / 1000;
        int totalSamples = samples + 2 * padSamples;
        byte[] recordedData = new byte[totalSamples * 2];

        mRecorder = new Recorder(recordedData, totalSamples);
        mRecorder.start();
        Utils.delay(END_DELAY_MS);

        Utils.playRaw(playbackData);

        int timeout = duration + 2 * END_DELAY_MS;
        try {
            mRecorder.join(timeout);
        } catch (InterruptedException e) {}

        return recordedData;
    }

    protected void compare(byte[] stim, byte[] record) {
        setScore(getString(R.string.aq_complete));
        setReport(getString(R.string.aq_loopback_report));
    }

    private void halt() {
        if (mRecorder != null) {
            mRecorder.halt();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        halt();
    }

    @Override
    public void stop() {
        super.stop();
        halt();
    }

    @Override
    public int getTimeout() {
        return TIMEOUT;
    }

    /* Class which records audio in a background thread, to fill the supplied buffer. */
    class Recorder extends Thread {
        private AudioRecord mRecord;
        private int mSamples;
        private byte[] mBuffer;
        private boolean mProceed;

        Recorder(byte[] buffer, int samples) {
            mBuffer = buffer;
            mSamples = samples;
            mProceed = true;
        }

        public void halt() {
            mProceed = false;
        }

        @Override
        public void run() {
            final int minBufferSize = AudioQualityVerifierActivity.SAMPLE_RATE
                    * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
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

            captureLoop();

            mRecord.stop();
            mRecord.release();
            mRecord = null;
        }

        private void captureLoop() {
            int totalBytes = mSamples * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
            Log.i(TAG, "Recording " + totalBytes + " bytes");
            int position = 0;
            int bytes;
            while (position < totalBytes && mProceed) {
                bytes = mRecord.read(mBuffer, position, totalBytes - position);
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
                position += bytes;
            }
        }
    }
}
