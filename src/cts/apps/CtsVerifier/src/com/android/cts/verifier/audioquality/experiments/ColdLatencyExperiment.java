/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.media.MediaRecorder.AudioSource;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Experiment} that measures how long it takes for an initialized
 * {@link AudioRecord} object to enter the recording state.
 */
public class ColdLatencyExperiment extends Experiment {

    /**
     * Rough latency amounts observed:
     *
     * N1 2.3.4: 350 ms
     * NS 2.3.4: 250 ms
     * Xoom 3.1: 100 ms
     */
    private static final int MAXIMUM_LATENCY_ALLOWED_MS = 500;

    /** Enough time to say a short phrase usually entered as a voice command. */
    private static final int BUFFER_TIME_MS = 25 * 1000;

    /** Milliseconds to pause while repeatedly checking the recording state. */
    private static final int DELAY_MS = 10;

    /** Milliseconds to record before turning off the recording. */
    private static final int RECORDING_DELAY_MS = 3000;

    /** Milliseconds to pause before checking the latency after making a sound. */
    private static final int LATENCY_CHECK_DELAY_MS = 5000;

    private static final int TEST_TIMEOUT_SECONDS = 10;

    public ColdLatencyExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_cold_latency);
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newCachedThreadPool();
        RecordingTask recordingTask = new RecordingTask(RECORDING_DELAY_MS);

        try {
            // 1. Start recording for a couple seconds.
            Future<Long> recordingFuture = executor.submit(recordingTask);
            long recordTime = recordingFuture.get(RECORDING_DELAY_MS * 2, TimeUnit.MILLISECONDS);
            if (recordTime < 0) {
                setScore(getString(R.string.aq_fail));
                return;
            }

            // 2. Wait a bit for the audio hardware to shut down.
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < LATENCY_CHECK_DELAY_MS) {
                Utils.delay(DELAY_MS);
            }

            // 3. Now measure the latency by starting up the hardware again.
            long latency = getLatency();
            if (latency < 0) {
                setScore(getString(R.string.aq_fail));
            } else {
                setScore(latency < MAXIMUM_LATENCY_ALLOWED_MS
                        ? getString(R.string.aq_pass)
                        : getString(R.string.aq_fail));
                setReport(String.format(getString(R.string.aq_cold_latency_report), latency,
                        MAXIMUM_LATENCY_ALLOWED_MS));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setScore(getString(R.string.aq_fail));
        } catch (ExecutionException e) {
            setScore(getString(R.string.aq_fail));
        } catch (TimeoutException e) {
            setScore(getString(R.string.aq_fail));
        } finally {
            recordingTask.stopRecording();
            executor.shutdown();
            mTerminator.terminate(false);
        }
    }

    @Override
    public int getTimeout() {
        return TEST_TIMEOUT_SECONDS;
    }

    /** Task that records for a given length of time. */
    private class RecordingTask implements Callable<Long> {

        private static final int READ_TIME = 25;

        private final long mRecordMs;

        private final int mSamplesToRead;

        private final byte[] mBuffer;

        private boolean mKeepRecording = true;

        public RecordingTask(long recordMs) {
            this.mRecordMs = recordMs;
            this.mSamplesToRead = (READ_TIME * AudioQualityVerifierActivity.SAMPLE_RATE) / 1000;
            this.mBuffer = new byte[mSamplesToRead * AudioQualityVerifierActivity.BYTES_PER_SAMPLE];
        }

        public Long call() throws Exception {
            int minBufferSize = BUFFER_TIME_MS / 1000
                    * AudioQualityVerifierActivity.SAMPLE_RATE
                    * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
            int bufferSize = Utils.getAudioRecordBufferSize(minBufferSize);
            if (bufferSize < 0) {
                setReport(getString(R.string.aq_audiorecord_buffer_size_error));
                return -1L;
            }

            AudioRecord record = null;
            try {
                record = new AudioRecord(AudioSource.VOICE_RECOGNITION,
                        AudioQualityVerifierActivity.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioQualityVerifierActivity.AUDIO_FORMAT,
                        bufferSize);

                if (record.getRecordingState() != AudioRecord.STATE_INITIALIZED) {
                    setReport(getString(R.string.aq_init_audiorecord_error));
                    return -2L;
                }

                record.startRecording();
                while (record.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    // Wait until we can start recording...
                    Utils.delay(DELAY_MS);
                }

                long startTime = System.currentTimeMillis();
                int maxBytes = mSamplesToRead * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
                while (true) {
                    synchronized (this) {
                        if (!mKeepRecording) {
                            break;
                        }
                    }
                    int numBytesRead = record.read(mBuffer, 0, maxBytes);
                    if (numBytesRead < 0) {
                        setReport(getString(R.string.aq_recording_error));
                        return -3L;
                    } else if (System.currentTimeMillis() - startTime >= mRecordMs) {
                        return System.currentTimeMillis() - startTime;
                    }
                }

                return -4L;
            } finally {
                if (record != null) {
                    if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        record.stop();
                    }
                    record.release();
                    record = null;
                }
            }
        }

        public void stopRecording() {
            synchronized (this) {
                mKeepRecording = false;
            }
        }
    }

    /**
     * @return latency between starting to record and entering the record state or
     *         -1 if an error occurred
     */
    private long getLatency() {
        int minBufferSize = BUFFER_TIME_MS / 1000
                * AudioQualityVerifierActivity.SAMPLE_RATE
                * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
        int bufferSize = Utils.getAudioRecordBufferSize(minBufferSize);
        if (bufferSize < 0) {
            setReport(String.format(getString(R.string.aq_audiorecord_buffer_size_error),
                    bufferSize));
            return -1;
        }

        AudioRecord record = null;
        try {
            record = new AudioRecord(AudioSource.VOICE_RECOGNITION,
                    AudioQualityVerifierActivity.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioQualityVerifierActivity.AUDIO_FORMAT, bufferSize);

            if (record.getRecordingState() != AudioRecord.STATE_INITIALIZED) {
                setReport(getString(R.string.aq_init_audiorecord_error));
                return -1;
            }

            long startTime = System.currentTimeMillis();
            record.startRecording();
            while (record.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                Utils.delay(DELAY_MS);
            }
            long endTime = System.currentTimeMillis();

            return endTime - startTime;
        } finally {
            if (record != null) {
                if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop();
                }
                record.release();
                record = null;
            }
        }
    }
}
