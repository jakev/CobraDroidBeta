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
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;

/**
 * Experiment to test the linearity of the microphone gain response.
 *
 * This plays a sequence of identical stimuli at increasing volumes and then
 * analyzes the set of recordings.
 */
public class GainLinearityExperiment extends SequenceExperiment {
    private static final int LEVELS = 4;
    private static final float DB_STEP_SIZE = 10.0f;
    private static final float TOLERANCE = 2.0f; // Maximum allowed deviation from linearity in dB
    private static final int STIM_NUM = 31;

    private short[] mReference = null;

    public GainLinearityExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_linearity_exp);
    }

    @Override
    protected int getTrials() {
        return LEVELS;
    }

    @Override
    protected byte[] getStim(Context context, int trial) {
        float db = (trial - (LEVELS - 1)) * DB_STEP_SIZE;
        if (mReference == null) {
            mReference = Utils.byteToShortArray(Utils.getStim(context, STIM_NUM));
        }
        short[] samples = Utils.scale(mReference, db);
        return Utils.shortToByteArray(samples);
    }

    @Override
    protected void compare(byte[][] stim, byte[][] record) {
        short[][] pcms = new short[LEVELS][];
        for (int i = 0; i < LEVELS; i++) {
            pcms[i] = Utils.byteToShortArray(record[i]);
        }
        // We specify the middle stimulus (LEVELS / 2) as the "reference":
        float deviation = mNative.linearityTest(pcms, AudioQualityVerifierActivity.SAMPLE_RATE,
                DB_STEP_SIZE, LEVELS / 2);
        if (deviation < 0.0f) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_linearity_report_error), deviation));
        } else if (deviation > TOLERANCE) {
            setScore(getString(R.string.aq_fail));
            setReport(String.format(getString(R.string.aq_linearity_report_normal),
                    deviation, TOLERANCE));
        } else {
            setScore(getString(R.string.aq_pass));
            setReport(String.format(getString(R.string.aq_linearity_report_normal),
                    deviation, TOLERANCE));
        }
    }
}
