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

#ifndef OVERFLOW_CHECK_H
#define OVERFLOW_CHECK_H

/* This is a clipping/overflow check.  This is designed to look for a
   ~3-second duration ~250 Hz pure tone at a level sufficient to cause
   about 20% clipping.  It examines the recorded waveform for possible
   overflow/wraparound.  The input signal of numSamples total sampled at
   sampleRate is in pcm. The expected signal contains silence - tone -
   silence.  The approximate duration in seconds of the tone found is
   returned in duration.  The number of unexpectedly-large jumps within
   the tone is returned in numDeltas.  The approximate sample numbers of
   the tone endpoints are returned in onset and offset.  The maximum
   and minimum found in the signal located between onset and offset are
   returned in maxPeak and minPeak, respectively.  The function
   return is 1 if the operation was sucessful, 0 on failure. */
int overflowCheck(short* pcm, int numSamples, float sampleRate,
                  float* duration, int* numDeltas, int* onset, int* offset,
                  int* maxPeak, int* minPeak);

#endif // OVERFLOW_CHECK_H
