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
 * Experiment to look for excessive DC bias in the recordings.
 * It calculates the mean of the observed samples.
 */
public class BiasExperiment extends LoopbackExperiment {
    private static final float ONSET_THRESH = 10.0f;
    private static final float TOLERANCE = 200.0f;

    public BiasExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_bias_exp);
    }

    @Override
    protected void compare(byte[] stim, byte[] record) {
        short[] pcm = Utils.byteToShortArray(record);
        float[] results = mNative.measureRms(pcm, AudioQualityVerifierActivity.SAMPLE_RATE, ONSET_THRESH);
        float rms = results[Native.MEASURE_RMS_RMS];
        float duration = results[Native.MEASURE_RMS_DURATION];
        float mean = results[Native.MEASURE_RMS_MEAN];
        if (mean < -TOLERANCE || mean > TOLERANCE) {
            setScore(getString(R.string.aq_fail));
        } else {
            setScore(getString(R.string.aq_pass));
        }
        setReport(String.format(getString(R.string.aq_bias_report),
                mean, TOLERANCE, rms, duration));
    }
}
