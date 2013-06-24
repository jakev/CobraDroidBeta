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

#include "Fft.h"

#include <stdlib.h>
#include <math.h>

Fft::Fft(void) : mCosine(0), mSine(0), mFftSize(0), mFftTableSize(0) { }

Fft::~Fft(void) {
    fftCleanup();
}

/* Construct a FFT table suitable to perform a DFT of size 2^power. */
void Fft::fftInit(int power) {
    fftCleanup();
    fftMakeTable(power);
}

void Fft::fftCleanup() {
        delete [] mSine;
        delete [] mCosine;
        mSine = NULL;
        mCosine = NULL;
        mFftTableSize = 0;
        mFftSize = 0;
}

/* z <- (10 * log10(x^2 + y^2))    for n elements */
int Fft::fftLogMag(float *x, float *y, float *z, int n) {
    float *xp, *yp, *zp, t1, t2, ssq;

    if(x && y && z && n) {
        for(xp=x+n, yp=y+n, zp=z+n; zp > z;) {
            t1 = *--xp;
            t2 = *--yp;
            ssq = (t1*t1)+(t2*t2);
            *--zp = (ssq > 0.0)? 10.0 * log10((double)ssq) : -200.0;
        }
        return 1;    //true
    } else {
        return 0;    // false/fail
    }
}

int Fft::fftMakeTable(int pow2) {
    int lmx, lm;
    float *c, *s;
    double scl, arg;

    mFftSize = 1 << pow2;
    mFftTableSize = lmx = mFftSize/2;
    mSine = new float[lmx];
    mCosine = new float[lmx];
    scl = (M_PI*2.0)/mFftSize;
    for (s=mSine, c=mCosine, lm=0; lm<lmx; lm++ ) {
        arg = scl * lm;
        *s++ = sin(arg);
        *c++ = cos(arg);
    }
    mBase = (mFftTableSize * 2)/mFftSize;
    mPower2 = pow2;
    return(mFftTableSize);
}


/* Compute the discrete Fourier transform of the 2**l complex sequence
 * in x (real) and y (imaginary).  The DFT is computed in place and the
 * Fourier coefficients are returned in x and y.
 */
void Fft::fft( float *x, float *y ) {
    float c, s, t1, t2;
    int j1, j2, li, lix, i;
    int lmx, lo, lixnp, lm, j, nv2, k=mBase, im, jm, l = mPower2;

    for (lmx=mFftSize, lo=0; lo < l; lo++, k *= 2) {
        lix = lmx;
        lmx /= 2;
        lixnp = mFftSize - lix;
        for (i=0, lm=0; lm<lmx; lm++, i += k ) {
            c = mCosine[i];
            s = mSine[i];
            for ( li = lixnp+lm, j1 = lm, j2 = lm+lmx; j1<=li;
                  j1+=lix, j2+=lix ) {
                t1 = x[j1] - x[j2];
                t2 = y[j1] - y[j2];
                x[j1] += x[j2];
                y[j1] += y[j2];
                x[j2] = (c * t1) + (s * t2);
                y[j2] = (c * t2) - (s * t1);
            }
        }
    }

    /* Now perform the bit reversal. */
    j = 1;
    nv2 = mFftSize/2;
    for ( i=1; i < mFftSize; i++ ) {
        if ( j < i ) {
            jm = j-1;
            im = i-1;
            t1 = x[jm];
            t2 = y[jm];
            x[jm] = x[im];
            y[jm] = y[im];
            x[im] = t1;
            y[im] = t2;
        }
        k = nv2;
        while ( j > k ) {
            j -= k;
            k /= 2;
        }
        j += k;
    }
}

/* Compute the discrete inverse Fourier transform of the 2**l complex
 * sequence in x (real) and y (imaginary).  The DFT is computed in
 * place and the Fourier coefficients are returned in x and y.  Note
 * that this DOES NOT scale the result by the inverse FFT size.
 */
void Fft::ifft(float *x, float *y ) {
    float c, s, t1, t2;
    int j1, j2, li, lix, i;
    int lmx, lo, lixnp, lm, j, nv2, k=mBase, im, jm, l = mPower2;

    for (lmx=mFftSize, lo=0; lo < l; lo++, k *= 2) {
        lix = lmx;
        lmx /= 2;
        lixnp = mFftSize - lix;
        for (i=0, lm=0; lm<lmx; lm++, i += k ) {
            c = mCosine[i];
            s = - mSine[i];
            for ( li = lixnp+lm, j1 = lm, j2 = lm+lmx; j1<=li;
                        j1+=lix, j2+=lix ) {
                t1 = x[j1] - x[j2];
                t2 = y[j1] - y[j2];
                x[j1] += x[j2];
                y[j1] += y[j2];
                x[j2] = (c * t1) + (s * t2);
                y[j2] = (c * t2) - (s * t1);
            }
        }
    }

    /* Now perform the bit reversal. */
    j = 1;
    nv2 = mFftSize/2;
    for ( i=1; i < mFftSize; i++ ) {
        if ( j < i ) {
            jm = j-1;
            im = i-1;
            t1 = x[jm];
            t2 = y[jm];
            x[jm] = x[im];
            y[jm] = y[im];
            x[im] = t1;
            y[im] = t2;
        }
        k = nv2;
        while ( j > k ) {
            j -= k;
            k /= 2;
        }
        j += k;
    }
}

int Fft::fftGetSize(void) { return mFftSize; }

int Fft::fftGetPower2(void) { return mPower2; }
