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

#include "GlitchTest.h"
#include "Window.h"
#include "Fft.h"

#include <math.h>

GlitchTest::GlitchTest(void) : mRe(0), mIm(0), mFt(0), mWind(0) {}

void GlitchTest::init(float sampleRate, float stimFreq, float onsetThresh,
                      float dbSnrThresh) {
    cleanup(); // in case Init gets called multiple times.
    // Analysis parameters
    float windowDur = 0.025;    // Duration of the analysis window in seconds. 
    float frameInterval = 0.01; // Interval in seconds between frame onsets 
    float lowestFreq = 200.0;   // Lowest frequency to include in
                                // background noise power integration.
    mSampleRate = sampleRate;
    mOnsetThresh = onsetThresh;
    mDbSnrThresh = dbSnrThresh;
    mFrameStep = (int)(0.5 + (mSampleRate * frameInterval));
    mWindowSize = (int)(0.5 + (mSampleRate * windowDur));
    mWind = new Window(mWindowSize);
    mFt = new Fft;
    mFt->fftInit(mFt->fftPow2FromWindowSize(mWindowSize));
    mFftSize = mFt->fftGetSize();
    mRe = new float[mFftSize];
    mIm = new float[mFftSize];
    float freqInterval = mSampleRate / mFftSize;
    // We can exclude low frequencies from consideration, since the
    // phone may have DC offset in the A/D, and there may be rumble in
    // the testing room.    
    mLowestSpectrumBin = (int)(0.5 + (lowestFreq / freqInterval));
    // These are the bin indices within which most of the energy due to
    // the (windowed) tone should be found. 
    mLowToneBin = (int)(0.5 + (stimFreq / freqInterval)) - 2;
    mHighToneBin = (int)(0.5 + (stimFreq / freqInterval)) + 2;
    if (mLowestSpectrumBin >= mLowToneBin) {
        mLowestSpectrumBin = mHighToneBin + 1;
    }
}

int GlitchTest::checkToneSnr(short* pcm, int numSamples, float* duration,
                             int* numBadFrames) {
    *numBadFrames = 0;
    *duration = 0.0;
    if (!(mRe && mIm)) {
        return -1; // not initialized.
    }
    int n_frames = 1 + ((numSamples - mWindowSize) / mFrameStep);
    if (n_frames < 4) { // pathologically short input signal 
        return -2;
    }
    *numBadFrames = 0;
    int onset = -1;
    int offset = -1;
    for (int frame = 0; frame < n_frames; ++frame) {
        int numSpectra = 0;
        mWind->window(pcm + frame*mFrameStep, mRe, 0.0);
        realMagSqSpectrum(mRe, mWindowSize, mRe, &numSpectra);
        int maxLoc = 0;
        float maxValue = 0.0;
        findPeak(mRe, mLowestSpectrumBin, numSpectra, &maxLoc, &maxValue);
        // possible states: (1) before tone onset; (2) within tone
        // region; (3) after tone offset.
        if ((onset < 0) && (offset < 0)) { // (1) 
            if ((maxLoc >= mLowToneBin) && (maxLoc <= mHighToneBin)
                    && (maxValue > mOnsetThresh)) {
                onset = frame;
            }
            continue;
        }
        if ((onset >= 0) && (offset < 0)) { // (2) 
            if (frame > (onset + 2)) { // let the framer get completely
                                       // into the tonal signal
                double sumNoise = 1.0; // init. to small non-zero vals to
                                       // avoid log or divz problems
                double sumSignal = 1.0;
                float snr = 0.0;
                if (maxValue < mOnsetThresh) {
                    offset = frame;
                    *duration = mFrameStep * (offset - onset) / mSampleRate;
                    if (*numBadFrames >= 1) {
                        (*numBadFrames) -= 1; // account for expected glitch at
                                            // signal offset
                    }
                    continue;
                }
                for (int i = mLowestSpectrumBin; i < mLowToneBin; ++i) {
                    sumNoise += mRe[i]; // note that mRe contains the magnitude
                                        // squared spectrum.
                }
                for (int i = mLowToneBin; i <= mHighToneBin; ++i)
                    sumSignal += mRe[i];
                for (int i = mHighToneBin + 1; i < numSpectra; ++i) {
                    sumNoise += mRe[i]; // Note: mRe has the mag squared spectrum.
                }
                snr = 10.0 * log10(sumSignal / sumNoise);
                if (snr < mDbSnrThresh)
                    (*numBadFrames) += 1;
            }
            continue;
        }
        if ((onset >= 0) && (offset > 0)) { // (3)
            if ((maxLoc >= mLowToneBin) && (maxLoc <= mHighToneBin) &&
                    (maxValue > mOnsetThresh)) { // tone should not pop up again!
                (*numBadFrames) += 1;
            }
            continue;
        }
    }
    if ((onset >= 0) && (offset > 0))
        return 1;  // Success.
    if (onset < 0) {
        return -3; // Signal onset not found.
    }
    return -4;     // Signal offset not found.
}

void GlitchTest::cleanup(void) {
    delete [] mRe;
    delete [] mIm;
    delete mFt;
    delete mWind;
    mRe = 0;
    mIm = 0;
    mWind = 0;
    mFt = 0;
}

int GlitchTest::realMagSqSpectrum(float* data, int numInput,
                                  float* output, int* numOutput) {
    *numOutput = 0;
    if ((numInput <= 0) || (numInput > mFftSize))
        return 0;
    int i = 0;
    for (i = 0; i < numInput; ++i) {
        mRe[i] = data[i];
        mIm[i] = 0.0;
    }
    for ( ; i < mFftSize; ++i) {
        mRe[i] = 0.0;
        mIm[i] = 0.0;
    }
    mFt->fft(mRe, mIm);
    *numOutput = 1 + (mFftSize / 2);
    for (i = 0; i < *numOutput; ++i) {
        output[i] = (mRe[i] * mRe[i]) + (mIm[i] * mIm[i]);
    }
    return 1;
}

void GlitchTest::findPeak(float* data, int startSearch, int endSearch,
                          int* maxLoc, float* maxValue) {
    float amax = data[startSearch];
    int loc = startSearch;
    for (int i = startSearch + 1; i < endSearch; ++i) {
        if (data[i] > amax) {
            amax = data[i];
            loc = i;
        }
    }
    *maxLoc = loc;
    *maxValue = 10.0 * log10(amax / mWindowSize);
}

