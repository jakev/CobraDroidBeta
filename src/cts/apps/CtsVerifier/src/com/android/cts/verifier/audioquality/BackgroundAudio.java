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

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Continuously play background noise in a loop, until halt() is called.
 * Used to simulate noisy environments, and for sound level calibration.
 */
public class BackgroundAudio extends Thread {
    public static final String TAG = "AudioQualityVerifier";

    private static final int BUFFER_TIME = 100; // Time in ms to buffer for

    private boolean mProceed;
    private AudioTrack mAudioTrack;
    private byte[] mData;
    private int mPos;
    private int mBufferSize;

    public void halt() {
        mProceed = false;
    }

    public BackgroundAudio(byte[] data) {
        mProceed = true;
        mData = data;
        mPos = 0;

        // Calculate suitable buffer size:
        final int minBufferSize = (BUFFER_TIME * AudioQualityVerifierActivity.SAMPLE_RATE
                * AudioQualityVerifierActivity.BYTES_PER_SAMPLE) / 1000;
        final int minHardwareBufferSize =
                AudioTrack.getMinBufferSize(AudioQualityVerifierActivity.SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioQualityVerifierActivity.AUDIO_FORMAT);
        mBufferSize = Utils.getAudioTrackBufferSize(minBufferSize);
        Log.i(TAG, "minBufferSize = " + minBufferSize + ", minHWSize = " + minHardwareBufferSize
                + ", bufferSize = " + mBufferSize);

        // Start playback:
        Log.i(TAG, "Looping " + data.length + " bytes of audio, buffer size " + mBufferSize);
        mAudioTrack = new AudioTrack(AudioQualityVerifierActivity.PLAYBACK_STREAM,
                AudioQualityVerifierActivity.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioQualityVerifierActivity.AUDIO_FORMAT, mBufferSize, AudioTrack.MODE_STREAM);
        if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            writeAudio();
            start(); // Begin background thread to push audio data
            mAudioTrack.play();
        } else {
            Log.e(TAG, "Error initializing audio track.");
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!mProceed) {
                mAudioTrack.stop();
                return; // End thread
            }
            writeAudio();
        }
    }

    private void writeAudio() {
        int len = mData.length;
        int count;
        int maxBytes = Math.min(mBufferSize, len - mPos);

        count = mAudioTrack.write(mData, mPos, maxBytes);
        if (count < 0) {
            Log.e(TAG, "Error writing looped audio data");
            halt();
            return;
        }
        mPos += count;
        if (mPos == len) {
            mPos = 0; // Wraparound
        }
    }
}
