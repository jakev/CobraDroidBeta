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

#ifndef GLITCH_TEST_H
#define GLITCH_TEST_H

class Fft;
class Window;

class GlitchTest {
public:
    GlitchTest(void);

    virtual ~GlitchTest(void) {
        cleanup();
    }

    /* Set up the instance to operate on input test signals sampled at
       sample_rate that contain a stimulis tone of stim_freq frequency.
       The signal will be considered on during the interval it exceeds
       onset_thresh (dB re 1.0).  Any frames containing a tone that have
       a signal energy to out-of-band energy ratio less than
       db_snr_thresh are counted as bad frames.  Init must be called
       before CheckToneSnr. */
    void init(float sample_rate, float stim_freq, float onset_thresh,
              float db_snr_thresh);

    /* Analyze the n_samples of the lin16 signal in pcm.  This signal is
       assumed sampled at sample_rate, and to contain a sinusoid with
       center frequency stim_freq, embedded somewhere in time, with
       "silence" intervals before and after.  The contiguous duration of
       the tone that exceeds onset_thresh (in dB re 1.0) is returned as
       seconds in duration. The number of frames for which the ratio of
       the energy at the tone frequency to the energy in the rest of the
       spectrum is less than db_snr_thresh is returned in n_bad_frames.
       If the test succeed, the method returns 1, else it returns a
       negative number that reflects the cause of failure as follows:
         -1     The instance is not initialized.
         -2     There are not enough samples to do a reasonable check.
         -3     The tone signal onset was not found.
         -4     The tone signal end was not found. */
    int checkToneSnr(short* pcm, int n_samples, float* duration,
                     int* n_bad_frames);

private:
    // Free memory, etc.
    void cleanup(void);

    /* Do a real FFT on the n_input samples in data, and return n_output
       power spectral density points in output.  The output points include
       DC through the Nyquist frequency (i.e. 1 + fft_size/2).  output
       must be large enough to accommodate this size. If n_input==0 or
       n_input > fft_size, return 0; else return 1. */
    int realMagSqSpectrum(float* data, int n_input,
                             float* output, int* n_output);

    /* Find the largest value in data starting at start_search and ending
       at end_search-1.  The values in data are assumed to be magnitude
       squared values from a spectrum computation based on window_size
       sample points.  Return the index where the largest value was found,
       and return the dB (re 1.0) equivalent of the highest magnitude. */
    void findPeak(float* data, int start_search, int end_search,
                  int* max_loc, float* max_value);

    // Real and Imaginary analysis arrays.
    float* mRe;
    float* mIm;
    // Fourier transform and window.
    Fft* mFt;
    Window* mWind;
    // Derived parameters and other variables.
    float mSampleRate;
    int mFrameStep;
    int mWindowSize;
    int mFftSize;
    float mOnsetThresh;
    float mDbSnrThresh;
    int mLowestSpectrumBin;
    int mLowToneBin;
    int mHighToneBin;
};

#endif // GLITCH_TEST_H
