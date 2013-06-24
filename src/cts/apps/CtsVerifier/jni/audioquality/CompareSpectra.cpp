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

// An amplitude-normalized spectrum comparison method.

#include "Fft.h"
#include "Window.h"

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

/* Find the endpoints of the signal stored in data such that the rms of
   the found segment exceeds signalOnRms.  data is of length n.  The
   RMS calculations used to find the endpoints use a window of length
   step and advance step samples.  The approximate, conservative
   endpoint sample indices are returned in start and end. */
static void findEndpoints(short* data, int n, int step, float signalOnRms,
                          int* start, int* end) {
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
            if (rms >= signalOnRms) {
                *start = frame + size;
            }
            continue;
        } else {
            if (rms < signalOnRms) {
                *end = frame - size;
                // fprintf(stderr, "n:%d onset:%d offset:%d\n", n, *start, *end);
                return;
            }
        }
    }
    // Handle the case where the signal does not drop below threshold
    // after onset.
    if ((*start > 0) && (! *end)) {
        *end = n - size - 1;
    }
}

// Sum the magnitude squared spectra.
static void accumulateMagnitude(float* im, float* re, int size, double* mag) {
    for (int i = 0; i < size; ++i) {
        mag[i] += ((re[i] * re[i]) + (im[i] * im[i]));
    }
}

/* Return a pointer to 1+(fftSize/2) spectrum magnitude values
   averaged over all of the numSamples in pcm.  It is the
   responsibility of the caller to free this magnitude array.  Return
   NULL on failure.  Use 50% overlap on the spectra. An FFT of
   fftSize points is used to compute the spectra, The overall signal
   rms over all frequencies between lowestBin and highestBin is
   returned as a scalar in rms. */
double* getAverageSpectrum(short* pcm, int numSamples, int fftSize,
                           int lowestBin, int highestBin, float* rms) {
    if (numSamples < fftSize) return NULL;
    int numFrames = 1 + ((2 * (numSamples - fftSize)) / fftSize);
    int numMag = 1 + (fftSize / 2);
    float* re = new float[fftSize];
    float* im = new float[fftSize];
    double* mag = new double[numMag];
    for (int i = 0; i < numMag; ++i) {
        mag[i] = 0.0;
    }
    Window wind(fftSize);
    Fft ft;
    int pow2 = ft.fftPow2FromWindowSize(fftSize);
    ft.fftInit(pow2);
    int input_p = 0;
    for (int i = 0; i < numFrames; ++i) {
        wind.window(pcm + input_p, re, 0.0);
        for (int j = 0; j < fftSize; ++j) {
            im[j] = 0.0;
        }
        ft.fft(re,im);
        accumulateMagnitude(im, re, numMag, mag);
        input_p += fftSize / 2;
    }
    double averageEnergy = 0.0; // per frame average energy
    for (int i = 0; i < numMag; ++i) {
        double e = mag[i]/numFrames;
        if ((i >= lowestBin) && (i <= highestBin))
            averageEnergy += e;
        mag[i] = sqrt(e);
    }
    *rms = sqrt(averageEnergy / (highestBin - lowestBin + 1));
    delete [] re;
    delete [] im;
    return mag;
}

/* Compare the average magnitude spectra of the signals in pcm and
   refPcm, which are of length numSamples and numRefSamples,
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
                   int numRefSamples, float sample_rate,
                   float* maxDeviation, float* rmsDeviation) {
    int fftSize = 512;           // must be a power of 2
    float lowestFreq = 100.0;    // don't count DC, room rumble, etc.
    float highestFreq = 3500.0;  // ignore most effects of sloppy anti alias filters.
    int lowestBin = int(0.5 + (lowestFreq * fftSize / sample_rate));
    int highestBin = int(0.5 + (highestFreq * fftSize / sample_rate));
    float signalOnRms = 1000.0; // endpointer RMS on/off threshold
    int endpointStepSize = int(0.5 + (sample_rate * 0.02)); // 20ms setp
    float rms1 = 0.0;
    float rms2 = 0.0;
    int onset = 0;
    int offset = 0;
    findEndpoints(refPcm, numRefSamples, endpointStepSize, signalOnRms,
                   &onset, &offset);
    double* spect1 = getAverageSpectrum(refPcm + onset, offset - onset,
                                        fftSize, lowestBin, highestBin, &rms1);
    findEndpoints(pcm, numSamples, endpointStepSize, signalOnRms,
                  &onset, &offset);
    double* spect2 = getAverageSpectrum(pcm + onset, offset - onset,
                                        fftSize, lowestBin, highestBin, &rms2);
    int magSize = 1 + (fftSize/2);
    if ((rms1 <= 0.0) || (rms2 <= 0.0))
        return 0; // failure because one or both signals are too short or
                  // too low in amplitude.
    float rmsNorm = rms2 / rms1; // compensate for overall gain differences
    // fprintf(stderr, "Level normalization: %f dB\n", 20.0 * log10(rmsNorm));
    *maxDeviation = 0.0;
    float sumDevSquared = 0.0;
    for (int i = lowestBin; i <= highestBin; ++i) {
        double val = 1.0;
        if ((spect1[i] > 0.0) && (spect2[i] > 0.0)) {
            val = 20.0 * log10(rmsNorm * spect1[i] / spect2[i]);
        }
        sumDevSquared += val * val;
        if (fabs(val) > fabs(*maxDeviation)) {
            *maxDeviation = val;
        }
        // fprintf(stderr, "%d %f\n", i, val);
    }
    *rmsDeviation = sqrt(sumDevSquared / (highestBin - lowestBin + 1));
    delete [] spect1;
    delete [] spect2;
    return 1; // success
}


