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

#ifndef COMPARE_SPECTRA_H
#define COMPARE_SPECTRA_H

/* Compare the average magnitude spectra of the signals in pcm and
   refPcm, which are of length numSamples and nRefSamples,
   respectively; both sampled at sample_rate.  The maximum deviation
   between average spectra, expressed in dB, is returned in
   maxDeviation, and the rms of all dB variations is returned in
   rmsDeviation.  Note that a lower limit is set on the frequencies that
   are compared so as to ignore irrelevant DC and rumble components.  If
   the measurement fails for some reason, return 0; else return 1, for
   success.  Causes for failure include the amplitude of one or both of
   the signals being too low, or the duration of the signals being too
   short.

   Note that the expected signal collection scenario is that the phone
   would be stimulated with a broadband signal as in a recognition
   attempt, so that there will be some "silence" regions at the start and
   end of the pcm signals.  The preferred stimulus would be pink noise,
   but any broadband signal should work. */

int compareSpectra(short* pcm, int numSamples, short* refPcm,
                   int nRefSamples, float sampleRate,
                   float* maxDeviation, float* rmsDeviation);

#endif // COMPARE_SPECTRA_H
