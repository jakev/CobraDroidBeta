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

#ifndef GENERATE_SINUSOID_H
#define GENERATE_SINUSOID_H

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
                      int* numOutput, short** output);

#endif // GENERATE_SINUSOID_H

