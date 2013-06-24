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
 * Experiment to verify that the sound level has been correctly set.
 *
 * This experiment should be run first; if it fails the others may
 * fail also.
 */
public class SoundLevelExperiment extends LoopbackExperiment {
    private static final float ONSET_THRESH = 10.0f;
    private static final int DURATION = 2;
    private static final float TOLERANCE = 1.05f;
    private static final float FREQ = 625.0f;
    private static final float RAMP = 0.0f;

    public SoundLevelExperiment() {
        super(true);
    }

    @Override
    protected String lookupName(Context context) {
        return context.getString(R.string.aq_sound_level_exp);
    }

    @Override
    protected byte[] getStim(Context context) {
        if (CalibrateVolumeActivity.USE_PINK) {
            return Utils.getPinkNoise(context, CalibrateVolumeActivity.OUTPUT_AMPL, DURATION);
        } else {
            short[] sinusoid = mNative.generateSinusoid(FREQ, DURATION,
                    AudioQualityVerifierActivity.SAMPLE_RATE, CalibrateVolumeActivity.OUTPUT_AMPL, RAMP);
            return Utils.shortToByteArray(sinusoid);
        }
    }

    @Override
    protected void compare(byte[] stim, byte[] record) {
        short[] pcm = Utils.byteToShortArray(record);
        float[] results = mNative.measureRms(pcm, AudioQualityVerifierActivity.SAMPLE_RATE, ONSET_THRESH);
        float rms = results[Native.MEASURE_RMS_RMS];
        float duration = results[Native.MEASURE_RMS_DURATION];
        String delta;
        if (rms * TOLERANCE < CalibrateVolumeActivity.TARGET_RMS) {
            setScore(getString(R.string.aq_fail));
            delta = getString(R.string.aq_status_low);
        } else if (rms > CalibrateVolumeActivity.TARGET_RMS * TOLERANCE) {
            setScore(getString(R.string.aq_fail));
            delta = getString(R.string.aq_status_high);
        } else {
            setScore(getString(R.string.aq_pass));
            delta = getString(R.string.aq_status_ok);
        }
        setReport(delta + ".\n" + String.format(getString(R.string.aq_level_report),
                rms, CalibrateVolumeActivity.TARGET_RMS,
                100.0f * (TOLERANCE - 1.0f), duration));
    }
}
