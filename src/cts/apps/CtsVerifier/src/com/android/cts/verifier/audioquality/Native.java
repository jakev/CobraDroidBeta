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

/**
 * Interface to native (C++) DSP code.
 */
public class Native {
    public native short[] generateSinusoid(float freq, float duration,
            float sampleRate, float amplitude, float ramp);
    public native float[] measureRms(short[] pcm, float sampleRate,
            float onsetThresh);
    public native float[] glitchTest(float sampleRate, float stimFreq,
            float onsetThresh, float dbSnrThresh, short[] pcm);
    public native float[] overflowCheck(short[] pcm, float sampleRate);
    public native float[] compareSpectra(short[] pcm, short[] refPcm,
            float sampleRate);
    public native float linearityTest(short[][] pcms,
        float sampleRate, float dbStepSize, int referenceStim);

    // The following indexes must match those in wrapper.cc
    public static final int MEASURE_RMS_RMS = 0;
    public static final int MEASURE_RMS_STD_DEV = 1;
    public static final int MEASURE_RMS_DURATION = 2;
    public static final int MEASURE_RMS_MEAN = 3;

    public static final int OVERFLOW_DELTAS = 0;
    public static final int OVERFLOW_ERROR = 1;
    public static final int OVERFLOW_DURATION = 2;
    public static final int OVERFLOW_ONSET = 3;
    public static final int OVERFLOW_OFFSET = 4;
    public static final int OVERFLOW_MAX = 5;
    public static final int OVERFLOW_MIN = 6;

    public static final int GLITCH_COUNT = 0;
    public static final int GLITCH_ERROR = 1;
    public static final int GLITCH_DURATION = 2;

    public static final int SPECTRUM_MAX_DEVIATION = 0;
    public static final int SPECTRUM_ERROR = 1;
    public static final int SPECTRUM_RMS_DEVIATION = 2;

    private static Native mInstance = null;

    static {
        System.loadLibrary("audioquality");
    }

    private Native() {}

    public static Native getInstance() {
        if (mInstance == null) {
            mInstance = new Native();
        }
        return mInstance;
    }
}
