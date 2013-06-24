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

/* This assumes an input signal that has at least a few hundred
   milliseconds of high-amplitude sinusoidal signal.  The signal is
   expected to be a relatively low-frequency sinusoid (200-400 Hz).
   If the signal is linearly reproduced or clipped, There will be no
   large step changes in value from one sample to the next.  On the
   other hand, if the signal contains numerical overflow
   (wrap-around), very large excursions will be produced.

   This program first searches for the high-amplitude recorded
   segment, then examines just that part of the signal for "large
   excursions", and returns the results of the search as four
   integers:

   n_wraps signal_size sine_start sine_end

   where n_wraps is the number of anomolous value jumps found,
   signal_size is the number of lin16 samples found in the file,
   sine_start and sine_end are the limits of the region searched for
   anomolous jumps. */

#include <stdlib.h>
#include <math.h>

// MAX_ALLOWED_STEP is the largest sample-to-sample change that will
// be considered "normal" for 250 Hz signals sampled at 8kHz.  This is
// calibrated by the largest sample-to-sample change that is naturally
// present in a 250 Hz sine wave with amplitude of 40000 (which is
// actually 7804).
#define MAX_ALLOWED_STEP 16000

// This is the RMS value that is expected to be exceded by a sinusoid
// with a peak amplitude of 32767 (actually 23169).
#define SIGNAL_ON_RMS 12000.0

static void findEndpoints(short* data, int n, int step, int* start, int* end) {
    int size = step;
    *start = *end = 0;
    int last_frame = n - size;
    for (int frame = 0; frame < last_frame; frame += step) {
        double sum = 0.0;
        for (int i=0; i < size; ++i) {
            float val = data[i + frame];
            sum += (val * val);
        }
        float rms = sqrt(sum / size);
        if (! *start) {
            if (rms >= SIGNAL_ON_RMS) {
                *start = frame + size;
            }
            continue;
        } else {
            if (rms < SIGNAL_ON_RMS) {
                *end = frame - size;
                return;
            }
        }
    }
    if ((*start > 0) && (! *end)) {
        *end = n - size - 1;
    }
}

static void checkExcursions(short* data, int start, int end, int* numJumps,
                            int* maxPeak, int* minPeak) {
    *numJumps = 0;
    int endm = end - 1;
    if ((endm - start) < 3) {
        *numJumps = -1;
        return;
    }
    *maxPeak = *minPeak = data[start];
    for (int i = start; i < endm; ++i) {
        int v1 = data[i];
        int v2 = data[i+1];
        if (v1 > *maxPeak)
            *maxPeak = v1;
        if (v1 < *minPeak)
            *minPeak = v1;
        int diff = v2 - v1;
        if (diff < 0)
            diff = -diff;
        if (diff > MAX_ALLOWED_STEP)
            (*numJumps) += 1;
    }
    return;
}

int overflowCheck(short* pcm, int numSamples, float sampleRate,
                  float* duration, int* numDeltas, int* onset, int* offset,
                  int* maxPeak, int* minPeak) {
    float windSize = 0.020;
    int minBuff = int(2.0 * sampleRate); // must have 2 sec of data at least.

    if(pcm && (numSamples >= minBuff)) {
        int step = int(0.5 + (windSize * sampleRate));
        *onset = 0;
        *offset = 0;

        findEndpoints(pcm, numSamples, step, onset, offset);
        *numDeltas = -1;
        checkExcursions(pcm, *onset, *offset, numDeltas, maxPeak, minPeak);
        *duration = (*offset - *onset) / sampleRate;
        return 1; // true/success
    }
    return 0; // failure
}
