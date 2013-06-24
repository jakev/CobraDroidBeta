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
import com.android.cts.verifier.audioquality.Native;
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Experiment} that measures how long it takes for a stimulus emitted
 * by a warmed up {@link AudioTrack} to be recorded by a warmed up
 * {@link AudioRecord} instance.
 */
public class WarmLatencyExperiment extends Experiment {

    /** Milliseconds to wait before playing the sound. */
    private static final int DELAY_TIME = 2000;

    /** Target RMS value to detect before quitting the experiment. */
    private static final float TARGET_RMS = 4000;

    /** Target latency to react to the sound. */
    private static final long TARGET_LATENCY_MS = 200;

    private static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final float FREQ = 625.0f;
    private static final int DURATION = 1;
    private static final int OUTPUT_AMPL = 5000;
    private static final float RAMP = 0.0f;
    private static final int BUFFER_TIME_MS = 100;
    private static final int READ_TIME = 25;

    public WarmLatencyExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_warm_latency);
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        PlaybackTask playbackTask = new PlaybackTask(barrier);
        RecordingTask recordingTask = new RecordingTask(barrier);

        Future<Long> playbackTimeFuture = executor.submit(playbackTask);
        Future<Long> recordTimeFuture = executor.submit(recordingTask);

        try {
            // Get the time when the sound is detected or throw an exception...
            long recordTime = recordTimeFuture.get(DELAY_TIME * 2, TimeUnit.MILLISECONDS);

            // Stop the playback now since the sound was detected. Get the time playback started.
            playbackTask.stopPlaying();
            long playbackTime = playbackTimeFuture.get();

            if (recordTime == -1 || playbackTime == -1) {
                setScore(getString(R.string.aq_fail));
            } else {
                long latency = recordTime - playbackTime;
                setScore(latency < TARGET_LATENCY_MS
                        ? getString(R.string.aq_pass)
                        : getString(R.string.aq_fail));
                setReport(String.format(getString(R.string.aq_warm_latency_report_normal),
                        latency));
            }
        } catch (InterruptedException e) {
            setExceptionReport(e);
        } catch (ExecutionException e) {
            setExceptionReport(e);
        } catch (TimeoutException e) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_warm_latency_report_error),
                    recordingTask.getLastRms(), TARGET_RMS));
        } finally {
            playbackTask.stopPlaying();
            recordingTask.stopRecording();
            mTerminator.terminate(false);
        }
    }

    private void setExceptionReport(Exception e) {
        setScore(getString(R.string.aq_fail));
        setReport(String.format(getString(R.string.aq_exception_error), e.getClass().getName()));
    }

    @Override
    public int getTimeout() {
        return 10; // seconds
    }

    /**
     * Task that plays a sinusoid after playing silence for a couple of seconds.
     * Returns the playback start time.
     */
    private class PlaybackTask implements Callable<Long> {

        private final byte[] mData;

        private final int mBufferSize;

        private final CyclicBarrier mReadyBarrier;

        private int mPosition;

        private boolean mKeepPlaying = true;

        public PlaybackTask(CyclicBarrier barrier) {
            this.mData = getAudioData();
            this.mBufferSize = getBufferSize();
            this.mReadyBarrier = barrier;
        }

        private byte[] getAudioData() {
            short[] sinusoid = mNative.generateSinusoid(FREQ, DURATION,
                    AudioQualityVerifierActivity.SAMPLE_RATE, OUTPUT_AMPL, RAMP);
            return Utils.shortToByteArray(sinusoid);
        }

        private int getBufferSize() {
            int minBufferSize = (BUFFER_TIME_MS * AudioQualityVerifierActivity.SAMPLE_RATE
                    * AudioQualityVerifierActivity.BYTES_PER_SAMPLE) / 1000;
            return Utils.getAudioTrackBufferSize(minBufferSize);
        }

        public Long call() throws Exception {
            if (mBufferSize == -1) {
                setReport(getString(R.string.aq_audiotrack_buffer_size_error));
                return -1l;
            }

            AudioTrack track = null;
            try {
                track = new AudioTrack(AudioManager.STREAM_MUSIC,
                        AudioQualityVerifierActivity.SAMPLE_RATE, CHANNEL_OUT_CONFIG,
                        AudioQualityVerifierActivity.AUDIO_FORMAT, mBufferSize,
                        AudioTrack.MODE_STREAM);

                if (track.getPlayState() != AudioTrack.STATE_INITIALIZED) {
                    setReport(getString(R.string.aq_init_audiotrack_error));
                    return -1l;
                }

                track.play();
                while (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    // Wait until we've started playing...
                }

                // Wait until the recording thread has started and is recording...
                mReadyBarrier.await(1, TimeUnit.SECONDS);

                long time = System.currentTimeMillis();
                while (System.currentTimeMillis() - time < DELAY_TIME) {
                    synchronized (this) {
                        if (!mKeepPlaying) {
                            break;
                        }
                    }
                    // Play nothing...
                }

                long playTime = System.currentTimeMillis();
                writeAudio(track);
                while (true) {
                    synchronized (this) {
                        if (!mKeepPlaying) {
                            break;
                        }
                    }
                    writeAudio(track);
                }

                return playTime;
            } finally {
                if (track != null) {
                    if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop();
                    }
                    track.release();
                    track = null;
                }
            }
        }

        private void writeAudio(AudioTrack track) {
            int length = mData.length;
            int writeBytes = Math.min(mBufferSize, length - mPosition);
            int numBytesWritten = track.write(mData, mPosition, writeBytes);
            if (numBytesWritten < 0) {
                throw new IllegalStateException("Couldn't write any data to the track!");
            } else {
                mPosition += numBytesWritten;
                if (mPosition == length) {
                    mPosition = 0;
                }
            }
        }

        public void stopPlaying() {
            synchronized (this) {
                mKeepPlaying = false;
            }
        }
    }

    /** Task that records until detecting a sound of the target RMS. Returns the detection time. */
    private class RecordingTask implements Callable<Long> {

        private final int mSamplesToRead;

        private final byte[] mBuffer;

        private final CyclicBarrier mBarrier;

        private boolean mKeepRecording = true;

        private float mLastRms = 0.0f;

        public RecordingTask(CyclicBarrier barrier) {
            this.mSamplesToRead = (READ_TIME * AudioQualityVerifierActivity.SAMPLE_RATE) / 1000;
            this.mBuffer = new byte[mSamplesToRead * AudioQualityVerifierActivity.BYTES_PER_SAMPLE];
            this.mBarrier = barrier;
        }

        public Long call() throws Exception {
            int minBufferSize = BUFFER_TIME_MS / 1000
                    * AudioQualityVerifierActivity.SAMPLE_RATE
                    * AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
            int bufferSize = Utils.getAudioRecordBufferSize(minBufferSize);
            if (bufferSize < 0) {
                setReport(getString(R.string.aq_audiorecord_buffer_size_error));
                return -1l;
            }

            long recordTime = -1;
            AudioRecord record = null;
            try {
                record = new AudioRecord(AudioSource.VOICE_RECOGNITION,
                        AudioQualityVerifierActivity.SAMPLE_RATE, CHANNEL_IN_CONFIG,
                        AudioQualityVerifierActivity.AUDIO_FORMAT, bufferSize);

                if (record.getRecordingState() != AudioRecord.STATE_INITIALIZED) {
                    setReport(getString(R.string.aq_init_audiorecord_error));
                    return -1l;
                }

                record.startRecording();
                while (record.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    // Wait until we can start recording...
                }

                // Wait until the playback thread has started and is playing...
                mBarrier.await(1, TimeUnit.SECONDS);

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
                        return -1l;
                    } else if (numBytesRead > 2) {
                        // TODO: Could be improved to use a sliding window?
                        short[] samples = Utils.byteToShortArray(mBuffer, 0, numBytesRead);
                        float[] results = mNative.measureRms(samples,
                                AudioQualityVerifierActivity.SAMPLE_RATE, -1.0f);
                        mLastRms = results[Native.MEASURE_RMS_RMS];
                        if (mLastRms >= TARGET_RMS) {
                            recordTime = System.currentTimeMillis();
                            break;
                        }
                    }
                }

                return recordTime;
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

        public float getLastRms() {
            return mLastRms;
        }

        public void stopRecording() {
            synchronized (this) {
                mKeepRecording = false;
            }
        }
    }
}
