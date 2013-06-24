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

import java.util.Random;

/**
 * Experiment to detect glitches or dropouts in the signal.
 * A number of artificial glitches can be optionally introduced.
 */
public class GlitchExperiment extends LoopbackExperiment {
    private static final float FREQ = 625.0f;
    private static final float AMPL = 10000.0f;
    private static final float ONSET_THRESH = 80.0f;
    private static final float SNR_THRESH = 20.0f;
    private static final float RAMP = 0.01f;
    private static final float DURATION = 3.0f;

    private int mArtificialGlitches;

    public GlitchExperiment(int artificialGlitches) {
        super(true);
        mArtificialGlitches = artificialGlitches;
    }

    @Override
    protected String lookupName(Context context) {
        String s = context.getString(R.string.aq_glitch_exp);
        if (mArtificialGlitches > 0) {
            s += " (" + mArtificialGlitches + ")";
        }
        return s;
    }

    @Override
    protected byte[] getStim(Context context) {
        short[] sinusoid = mNative.generateSinusoid(FREQ, DURATION,
                AudioQualityVerifierActivity.SAMPLE_RATE, AMPL, RAMP);
        addGlitches(sinusoid);
        return Utils.shortToByteArray(sinusoid);
    }

    private void addGlitches(short[] samples) {
        Random random = new Random();
        for (int i = 0; i < mArtificialGlitches; i++) {
            samples[random.nextInt(samples.length)] = 0;
        }
    }

    @Override
    protected void compare(byte[] stim, byte[] record) {
        int targetMin = mArtificialGlitches > 0 ? 1 : 0;
        int targetMax = mArtificialGlitches;
        short[] pcm = Utils.byteToShortArray(record);
        float[] ret = mNative.glitchTest(AudioQualityVerifierActivity.SAMPLE_RATE, FREQ,
                ONSET_THRESH, SNR_THRESH, pcm);
        int glitches = Math.round(ret[Native.GLITCH_COUNT]);
        float error = ret[Native.GLITCH_ERROR];
        float duration = ret[Native.GLITCH_DURATION];
        if (error < 0.0f) {
            setScore(getString(R.string.aq_fail));
            setReport(getString(R.string.aq_glitch_report_error));
        } else {
            if (glitches > targetMax || glitches < targetMin) {
                setScore(getString(R.string.aq_fail));
            } else {
                setScore(getString(R.string.aq_pass));
            }
            if (targetMin == targetMax) {
                setReport(String.format(getString(R.string.aq_glitch_report_exact),
                        glitches, targetMax, duration));
            } else {
                setReport(String.format(getString(R.string.aq_glitch_report_range),
                        glitches, targetMin, targetMax, duration));
            }
        }
   }
}
