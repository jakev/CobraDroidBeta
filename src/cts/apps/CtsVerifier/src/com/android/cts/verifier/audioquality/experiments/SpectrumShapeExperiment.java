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
import com.android.cts.verifier.audioquality.Native;
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;

/**
 * Experiment to check that the frequency profile of the recorded signal
 * does not differ too much from the stimulus.
 */
public class SpectrumShapeExperiment extends LoopbackExperiment {
    private static final float MAX_RMS_DEVIATION = 7.0f;
    private static final int AMPL = 10000;
    private static final int DURATION = 3;

    public SpectrumShapeExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_spectrum_shape_exp);
    }

   @Override
    protected byte[] getStim(Context context) {
        return Utils.getPinkNoise(context, AMPL, DURATION);
    }

    @Override
    protected void compare(byte[] stim, byte[] record) {
        short[] pcm = Utils.byteToShortArray(record);
        short[] refPcm = Utils.byteToShortArray(stim);
        float[] ret = mNative.compareSpectra(pcm, refPcm, AudioQualityVerifierActivity.SAMPLE_RATE);
        float maxDeviation = ret[Native.SPECTRUM_MAX_DEVIATION];
        float error = ret[Native.SPECTRUM_ERROR];
        float rmsDeviation = ret[Native.SPECTRUM_RMS_DEVIATION];
        if (error < 0.0f) {
            setScore(getString(R.string.aq_fail));
            setReport(getString(R.string.aq_spectrum_report_error));
        } else if (rmsDeviation > MAX_RMS_DEVIATION) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_spectrum_report_normal),
                    rmsDeviation, MAX_RMS_DEVIATION));
        } else {
            setScore(getString(R.string.aq_pass));
            setReport(String.format(getString(R.string.aq_spectrum_report_normal),
                    rmsDeviation, MAX_RMS_DEVIATION));
        }
    }
}
