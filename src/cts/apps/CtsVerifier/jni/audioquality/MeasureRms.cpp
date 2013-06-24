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

#include <math.h>

/* Return the median of the n values in "values".
   Uses a stupid bubble sort, but is only called once on small array. */
float getMedian(float* values, int n) {
    if (n <= 0)
        return 0.0;
    if (n == 1)
        return values[0];
    if (n == 2)
        return 0.5 * (values[0] + values[1]);
    for (int i = 1; i < n; ++i)
        for (int j = i; j < n; ++j) {
            if (values[j] < values[i-1]) {
                float tmp = values[i-1];
                values[i-1] = values[j];
                values[j] = tmp;
            }
        }
    int ind = int(0.5 + (0.5 * n)) - 1;
    return values[ind];
}

float computeAndRemoveMean(short* pcm, int numSamples) {
    float sum = 0.0;

    for (int i = 0; i < numSamples; ++i)
        sum += pcm[i];
    short mean;
    if (sum >= 0.0)
        mean = (short)(0.5 + (sum / numSamples));
    else
        mean = (short)((sum / numSamples) - 0.5);
    for (int i = 0; i < numSamples; ++i)
        pcm[i] -= mean;
    return sum / numSamples;
}

void measureRms(short* pcm, int numSamples, float sampleRate, float onsetThresh,
                float* rms, float* stdRms, float* mean, float* duration) {
    *rms = 0.0;
    *stdRms = 0.0;
    *duration = 0.0;
    float frameDur = 0.025;    // Both the duration and interval of the
                                // analysis frames.
    float calInterval = 0.250; // initial part of signal used to
                                // establish background level (seconds).
    double sumFrameRms = 1.0;
    float sumSampleSquares = 0.0;
    double sumFrameSquares = 1.0; // init. to small number to avoid
                                    // log and divz problems.
    int frameSize = (int)(0.5 + (sampleRate * frameDur));
    int numFrames = numSamples / frameSize;
    int numCalFrames = int(0.5 + (calInterval / frameDur));
    if (numCalFrames < 1)
        numCalFrames = 1;
    int frame = 0;

    *mean = computeAndRemoveMean(pcm, numSamples);

    if (onsetThresh < 0.0) { // Handle the case where we want to
                              // simply measure the RMS of the entire
                              // input sequence.
        for (frame = 0; frame < numFrames; ++frame) {
            short* p_data = pcm + (frame * frameSize);
            int i;
            for (i = 0, sumSampleSquares = 0.0; i < frameSize; ++i) {
                float samp = p_data[i];
                sumSampleSquares += samp * samp;
            }
            sumSampleSquares /= frameSize;
            sumFrameSquares += sumSampleSquares;
            double localRms = sqrt(sumSampleSquares);
            sumFrameRms += localRms;
        }
        *rms = sumFrameRms / numFrames;
        *stdRms = sqrt((sumFrameSquares / numFrames) - (*rms * *rms));
        *duration = frameSize * numFrames / sampleRate;
        return;
    }

    /* This handles the case where we look for a target signal against a
       background, and expect the signal to start some time after the
       beginning, and to finish some time before the end of the input
       samples. */
    if (numFrames < (3 * numCalFrames)) {
        return;
    }
    float* calValues = new float[numCalFrames];
    float calMedian = 0.0;
    int onset = -1;
    int offset = -1;

    for (frame = 0; frame < numFrames; ++frame) {
        short* p_data = pcm + (frame * frameSize);
        int i;
        for (i = 0, sumSampleSquares = 1.0; i < frameSize; ++i) {
            float samp = p_data[i];
            sumSampleSquares += samp * samp;
        }
        sumSampleSquares /= frameSize;
        /* We handle three states: (1) before the onset of the signal; (2)
           within the signal; (3) following the signal.  The signal is
           assumed to be at least onsetThresh dB above the background
           noise, and that at least one frame of silence/background
           precedes the onset of the signal. */
        if (onset < 0) { // (1)
            sumFrameSquares += sumSampleSquares;
            if (frame < numCalFrames ) {
                calValues[frame] = sumSampleSquares;
                continue;
            }
            if (frame == numCalFrames) {
                calMedian = getMedian(calValues, numCalFrames);
                if (calMedian < 10.0)
                    calMedian = 10.0; // avoid divz, etc.
            }
            float ratio = 10.0 * log10(sumSampleSquares / calMedian);
            if (ratio > onsetThresh) {
                onset = frame;
                sumFrameSquares = 1.0;
                sumFrameRms = 1.0;
            }
            continue;
        }
        if ((onset > 0) && (offset < 0)) { // (2)
            int sig_frame = frame - onset;
            if (sig_frame < numCalFrames) {
                calValues[sig_frame] = sumSampleSquares;
            } else {
                if (sig_frame == numCalFrames) {
                    calMedian = getMedian(calValues, numCalFrames);
                    if (calMedian < 10.0)
                        calMedian = 10.0; // avoid divz, etc.
                }
                float ratio = 10.0 * log10(sumSampleSquares / calMedian);
                int denFrames = frame - onset - 1;
                if (ratio < (-onsetThresh)) { // found signal end
                    *rms = sumFrameRms / denFrames;
                    *stdRms = sqrt((sumFrameSquares / denFrames) - (*rms * *rms));
                    *duration = frameSize * (frame - onset) / sampleRate;
                    offset = frame;
                    continue;
                }
            }
            sumFrameSquares += sumSampleSquares;
            double localRms = sqrt(sumSampleSquares);
            sumFrameRms += localRms;
            continue;
        }
        if (offset > 0) { // (3)
            /* If we have found the real signal end, the level should stay
               low till data end.  If not, flag this anomaly by increasing the
               reported duration. */
            float localRms = 1.0 + sqrt(sumSampleSquares);
            float localSnr = 20.0 * log10(*rms / localRms);
            if (localSnr < onsetThresh)
                *duration = frameSize * (frame - onset) / sampleRate;
            continue;
        }
    }
    delete [] calValues;
}

