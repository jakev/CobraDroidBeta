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

#ifndef LINEARITY_TEST_H
#define LINEARITY_TEST_H

/* There are numSignals int16 signals in pcms.  sampleCounts is an
   integer array of length numSignals containing their respective
   lengths in samples.  They are all sampled at sampleRate.  The pcms
   are ordered by increasing stimulus level.  The level steps between
   successive stimuli were of size dbStepSize dB.  The signal with
   index referenceStim (0 <= referenceStim < numSignals) should be in
   an amplitude range that is reasonably certain to be linear (e.g. at
   normal speaking levels).  The maximum deviation in linearity found
   (in dB) is returned in maxDeviation.  The function returns 1 if
   the measurements could be made, or a negative number that
   indicates the error, as follows:
      -1 The input signals or sample counts are missing.
      -2 The number of input signals is < 2.
      -3 The specified sample rate is <= 4000.0
      -4 The dB step size for the increase in stimulus level is <= 0.0/
      -5 The specified reverence stimulus number is out of range.
      -6 One or more of the stimuli is too short in duration. */
int linearityTest(short** pcms, int* sampleCounts, int numSignals,
                  float sampleRate, float dbStepSize,
                  int referenceStim, float* maxDeviation);


#endif /* LINEARITY_TEST_H */
