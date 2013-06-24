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
import com.android.cts.verifier.audioquality.CalibrateVolumeActivity;
import com.android.cts.verifier.audioquality.Native;
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;

/**
 * Experiment to test the clipping behaviour of the microphone.
 *
 * The stimulus is sinusoidal, and calculated to cause clipping for
 * part of the waveform. The experiment looks for strange clipping behaviour
 * by checking if the signal has any discontinuities (which might indicate
 * wraparound, for example).
 */
public class OverflowExperiment extends LoopbackExperiment {
    private static final float FREQ = 250.0f;
    private static final float AMPL = 32768.0f * 1.1f * CalibrateVolumeActivity.OUTPUT_AMPL
            / CalibrateVolumeActivity.TARGET_AMPL;
    private static final float DURATION = 3.0f; // Duration of tone in seconds
    private static final float MIN_DURATION = DURATION * 0.9f;
    private static final float RAMP = 0.01f;

    public OverflowExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_overflow_exp);
    }

    @Override
    protected byte[] getStim(Context context) {
        short[] sinusoid = mNative.generateSinusoid(FREQ, DURATION,
                AudioQualityVerifierActivity.SAMPLE_RATE, AMPL, RAMP);
        return Utils.shortToByteArray(sinusoid);
    }

    @Override
    protected void compare(byte[] stim, byte[] record) {
        short[] pcm = Utils.byteToShortArray(record);
        float[] ret = mNative.overflowCheck(pcm, AudioQualityVerifierActivity.SAMPLE_RATE);
        int numDeltas = Math.round(ret[0]);
        float error = ret[Native.OVERFLOW_ERROR];
        float duration = ret[Native.OVERFLOW_DURATION];
        float minPeak = ret[Native.OVERFLOW_MIN];
        float maxPeak = ret[Native.OVERFLOW_MAX];

        if (error < 0.0f) {
            setScore(getString(R.string.aq_fail));
            setReport(getString(R.string.aq_overflow_report_error));
        } else if (duration < MIN_DURATION) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_overflow_report_short),
                    DURATION, duration));
        } else if (numDeltas > 0) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_overflow_report_fail),
                    numDeltas, duration, minPeak, maxPeak));
        } else {
            setScore(getString(R.string.aq_pass));
            setReport(String.format(getString(R.string.aq_overflow_report_pass),
                    numDeltas, duration, minPeak, maxPeak));
        }
   }
}
