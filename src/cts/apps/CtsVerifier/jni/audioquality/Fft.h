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

#ifndef LEGACY_TALKIN_FFT_H
#define LEGACY_TALKIN_FFT_H

class Fft {
public:
    Fft(void);

    virtual ~Fft(void);

    // Prepare for radix-2 FFT's of size (1<<pow2) 
    void fftInit(int pow2);

    // Forward fft.  Real time-domain components in x, imaginary in y 
    void fft(float *x, float *y);

    // Inverse fft.  Real frequency-domain components in x, imaginary in y 
    void ifft(float *x, float *y);

    // Compute the dB-scaled log-magnitude spectrum from the real spectal
    // amplitude values in 'x', and imaginary values in 'y'.  Return the
    // magnitude spectrum in z.  Compute 'n' components. 
    int fftLogMag(float *x, float *y, float *z, int n);

    int fftGetSize();

    int fftGetPower2();

    // Return the power of 2 required to contain at least size samples.
    static int fftPow2FromWindowSize(int size) {
        int pow2 = 1;
        while ((1 << pow2) < size)
            pow2++;
        return pow2;
    }

private:
    // Free up memory and reset the static globals. 
    void fftCleanup();

    // Create the sine/cosine basis tables and return the size of the FFT
    // corresponding to pow2. 
    int fftMakeTable(int pow2);

    float* mSine;
    float* mCosine;
    int mFftTableSize;
    int mFftSize;
    int mPower2;
    int mBase;
};

#endif // LEGACY_TALKIN_FFT_H 
