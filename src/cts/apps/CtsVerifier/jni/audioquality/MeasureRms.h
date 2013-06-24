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

#ifndef MEASURE_RMS_H
#define MEASURE_RMS_H

/* Measure the rms of the non-silence segment of the signal in pcm, which
   is of numSamples length, and sampled at sampleRate.  pcm is assumed to
   consist of silence - signal - silence, as might be logged during a
   speech recognition attempt.  The stimulus signal in this case should
   be approximately a 3-second burst of pink noise presented at a level
   comparable to normal speaking level.  The RMS is measured using 25ms
   duration non-overlapping windows.  These are averaged over the whole
   non-silence part of pcm, and the result is returned in rms.  The
   standard deviation of this measurement over all frames is returned in
   stdRms, and the estimated duration of the non-silence region, in
   seconds, is returned in duration.  The target signal is taken to be
   that segment that is onsetThresh dB above the background, and is
   expected to be continuous, once the onset has been found.  If
   onsetThresh < 0.0, simply make the measurememt over the entire pcm
   signal.  In both cases, the mean of the entire signal is returned in
   mean. */
void measureRms(short* pcm, int numSamples, float sampleRate,
                float onsetThresh, float* rms, float* stdRms,
                float* mean, float* duration);


#endif /* MEASURE_RMS_H */
