/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// Generate a sinusoidal signal with optional onset and offset ramps.

#include <stdlib.h>
#include <math.h>

static inline short clipAndRound(float val) {
    if (val > 32767.0)
        return 32767;
    if (val < -32768.0)
        return -32768;
    if (val >= 0.0)
        return static_cast<short>(0.5 + val);
    return static_cast<short>(val - 0.5);
}

/* Return a sinusoid of frequency freq Hz and amplitude ampl in output
   of length numOutput.  If ramp > 0.0, taper the ends of the signal
   with a half-raised-cosine function.  It is up to the caller to
   delete[] output.  If this call fails due to unreasonable arguments,
   numOutput will be zero, and output will be NULL.  Note that the
   duration of the up/down ramps will be within the specified
   duration.  Note that if amplitude is specified outside of the
   numerical range of int16, the signal will be clipped at +- 32767. */
void generateSinusoid(float freq, float duration, float sampleRate,
                      float amplitude, float ramp,
                      int* numOutput, short** output) {
    // Sanity check
    if ((duration < (2.0 * ramp)) || ((freq * 2.0) > sampleRate) || (ramp < 0.0)) {
        *output = NULL;
        *numOutput = 0;
        return;
    }
    int numSamples = int(0.5 + (sampleRate * duration));
    double arg = M_PI * 2.0 * freq / sampleRate;
    short* wave = new short[numSamples];
    int numRamp = int(0.5 + (sampleRate * ramp));
    for (int i = 0; i < numSamples; ++i) {
        float val = amplitude * sin(arg * i);
        if (numRamp > 0) {
            if (i < numRamp) {
                float gain = (0.5 - (0.5 * cos((0.5 + i) * M_PI / numRamp)));
                val *= gain;
            } else {
                if (i > (numSamples - numRamp - 1)) {
                    float gain = (0.5 - (0.5 * cos((0.5 + (numSamples - i - 1))
                            * M_PI / numRamp)));
                    val *= gain;
                }
            }
        }
        wave[i] = clipAndRound(val);
    }
    *numOutput = numSamples;
    *output = wave;
}
