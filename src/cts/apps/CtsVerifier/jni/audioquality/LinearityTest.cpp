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

/* This test accepts a collection of N speech waveforms collected as
   part of N recognition attempts.  The waveforms are ordered by
   increasing presentation level.  The test determines the extent to
   which the peak amplitudes in the waveforms track the change in
   presentation level.  Failure to track the presentation level within
   some reasonable margin is an indication of clipping or of automatic
   gain control in the signal path.

   The speech stimuli that are used for this test should simply be
   replications of exactly the same speech signal presented at
   different levels.  It is expected that all recognition attempts on
   this signal result in correct recognition.  A warning, but not a
   hard failure, should be issued if any of the attempts fail.  The
   hard failure criterion for this test should be only based on the
   amplitude linearity tracking. */

#include <stdlib.h>
#include <stdio.h>
#include <math.h>

/* Keep a record of the top N absolute values found using a slow
   bubble-sort.  This is OK, since the use of this program is not time
   critical, and N is usually small.  Note that the argument n = N-1. */
static void bubbleUp(int* store, int n, short val) {
    if (val < store[n])
        return;
    for (int i = 0; i <= n; ++i) {
        if (val >= store[i]) {
            for (int j = n; j > i ; j--)
                store[j] = store[j-1];
            store[i] = val;
            return;
        }
    }
}

/* Make two measurements on the signal of length numSamples sampled at
   sampleRate in pcm: the RMS of the highest amplitude 30ms segment
   (returned in peakRms), and the RMS of the top 50 peak absolute
   values found (returned in peakAverage).  If the signal is too
   short to make reasonable measurements, the function returns 0, else
   it returns 1. */
static int peakLevels(short* pcm, int numSamples, float sampleRate,
                      float* peakAverage, float* peakRms) {
    float rmsFrameSize = 0.03;
    float rmsFrameStep = 0.01;
    int frameStep = int(0.5 + (sampleRate * rmsFrameStep));
    int frameSize = int(0.5 + (sampleRate * rmsFrameSize));
    int numFrames = 1 + ((numSamples - frameSize) / frameStep);

    if (numFrames < 10) {
        return 0; // failure for too short signal
    }

    // Peak RMS calculation
    double maxEnergy = 0.0;
    for (int frame = 0; frame < numFrames; ++frame) {
        double energy = 0.0;
        int limit = (frame * frameStep) + frameSize;
        for (int i = frame * frameStep; i < limit; ++i) {
            double s = pcm[i];
            energy += s * s;
        }
        if (energy > maxEnergy) {
            maxEnergy = energy;
        }
    }
    *peakRms = sqrt(maxEnergy / frameSize);

    // Find the absolute highest topN peaks in the signal and compute
    // the RMS of their values.
    int topN = 50; // The number of highest peaks over which to average.
    int topM = topN - 1;
    int* maxVal = new int[topN];
    for (int i = 0; i < topN; ++i) {
        maxVal[i] = 0;
    }
    for (int i = 0; i < numSamples; ++i) {
        if (pcm[i] >= 0) {
            bubbleUp(maxVal, topM, pcm[i]);
        } else {
            bubbleUp(maxVal, topM, -pcm[i]);
        }
    }
    float sum = 0.0;
    // The RMS is taken bacause we want the values of the highest peaks
    // to dominate.
    for (int i = 0; i < topN; ++i) {
        float fval = maxVal[i];
        sum += (fval * fval);
    }
    delete [] maxVal;
    *peakAverage = sqrt(sum/topN);
    return 1; // success
}

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
      -4 The dB step size for the increase in stimulus level is <= 0.0
      -5 The specified reverence stimulus number is out of range.
      -6 One or more of the stimuli is too short in duration. */
int linearityTest(short** pcms, int* sampleCounts, int numSignals,
                  float sampleRate, float dbStepSize, int referenceStim,
                  float* maxDeviation) {
    if (!(pcms && sampleCounts)) {
        return -1; // Input signals or sample counts are missing
    }
    if (numSignals < 2) {
        return -2; // the number of input signals must be >= 2;
    }
    if (sampleRate <= 4000.0) {
        return -3; // The sample rate must be > 4000 Hz.
    }
    if (dbStepSize <= 0.0) {
        return -4; // The dB step size must be > 0.0
    }
    if (!((referenceStim >= 0) && (referenceStim < numSignals))) {
        return -5; // (0 <= referenceStim < numSignals) must be true
    }
    float* peakAverage = new float[numSignals];
    float* peakRms = new float[numSignals];
    for (int sig = 0; sig < numSignals; ++sig) {
        if (!peakLevels(pcms[sig], sampleCounts[sig],
             sampleRate, peakAverage + sig, peakRms + sig)) {
            return -6; // failure because a signal is too short.
        }
    }
    float peakAverageRef = peakAverage[referenceStim];
    float peakRmsRef = peakRms[referenceStim];
    float maxDev = 0.0;
    for (int i = 0; i < numSignals; ++i) {
        float dbAverage = 20.0 * log10(peakAverage[i]/peakAverageRef);
        float dbRms = 20.0 * log10(peakRms[i]/peakRmsRef);
        float reference = dbStepSize * (i - referenceStim);
        float average_level = 0.5 * (dbAverage + dbRms);
        float dev = fabs(average_level - reference);
        // fprintf(stderr,"dbAverage:%f dbRms:%f reference:%f dev:%f\n",
        //         dbAverage, dbRms, reference, dev);
        if (dev > maxDev)
            maxDev = dev;
    }
    *maxDeviation = maxDev;
    return 1;
}
