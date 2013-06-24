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

// Wrapper to the native phone test signal processing library, which
// exposes an interface suitable for calling via JNI.

#include <stdlib.h>
#include <jni.h>

#include "GenerateSinusoid.h"
#include "MeasureRms.h"
#include "GlitchTest.h"
#include "OverflowCheck.h"
#include "CompareSpectra.h"
#include "LinearityTest.h"

typedef short *shortPtr;

extern "C" {
    JNIEXPORT jshortArray JNICALL
        Java_com_android_cts_verifier_audioquality_Native_generateSinusoid(
            JNIEnv *env, jobject obj,
            jfloat freq, jfloat duration,
            jfloat sampleRate, jfloat amplitude, jfloat ramp);
    JNIEXPORT jfloatArray JNICALL
        Java_com_android_cts_verifier_audioquality_Native_measureRms(
            JNIEnv *env, jobject obj,
            jshortArray jpcm, jfloat sampleRate, jfloat onsetThresh);
    JNIEXPORT jfloatArray JNICALL
        Java_com_android_cts_verifier_audioquality_Native_glitchTest(
            JNIEnv *env, jobject obj,
            jfloat sampleRate, jfloat stimFreq, jfloat onsetThresh,
            jfloat dbSnrThresh, jshortArray jpcm);
    JNIEXPORT jfloatArray JNICALL
        Java_com_android_cts_verifier_audioquality_Native_overflowCheck(
            JNIEnv *env, jobject obj,
            jshortArray jpcm, jfloat sampleRate);
    JNIEXPORT jfloatArray JNICALL
        Java_com_android_cts_verifier_audioquality_Native_compareSpectra(
            JNIEnv *env, jobject obj,
            jshortArray jpcm, jshortArray jrefPcm, jfloat sampleRate);
    JNIEXPORT jfloat JNICALL
        Java_com_android_cts_verifier_audioquality_Native_linearityTest(
            JNIEnv *env, jobject obj,
            jobjectArray jpcms,
            jfloat sampleRate, jfloat dbStepSize, jint referenceStim);
};

/* Returns an array of sinusoidal samples.
   If the arguments are invalid, returns an empty array. */
JNIEXPORT jshortArray JNICALL
    Java_com_android_cts_verifier_audioquality_Native_generateSinusoid(
        JNIEnv *env, jobject obj,
        jfloat freq, jfloat duration,
        jfloat sampleRate, jfloat amplitude, jfloat ramp) {
    short *wave = NULL;
    int numSamples = 0;

    generateSinusoid(freq, duration, sampleRate, amplitude, ramp,
            &numSamples, &wave);

    jshortArray ja;
    if (!numSamples) {
        ja = env->NewShortArray(0);
    } else {
        ja = env->NewShortArray(numSamples);
        env->SetShortArrayRegion(ja, 0, numSamples, wave);
        delete[] wave;
    }
    return ja;
}

/* Returns an array of four floats.
   ret[0] = RMS
   ret[1] = standard deviation of the RMS
   ret[2] = non-silent region duration
   ret[3] = mean value
*/
JNIEXPORT jfloatArray JNICALL
    Java_com_android_cts_verifier_audioquality_Native_measureRms(
        JNIEnv *env, jobject obj,
        jshortArray jpcm, jfloat sampleRate, jfloat onsetThresh) {
    float ret[4];
    ret[0] = ret[1] = ret[2] = ret[3] = -1.0;
    int numSamples = env->GetArrayLength(jpcm);
    short *pcm = new short[numSamples];
    env->GetShortArrayRegion(jpcm, 0, numSamples, pcm);

    measureRms(pcm, numSamples, sampleRate, onsetThresh, ret, ret + 1,
            ret + 3, ret + 2);

    jfloatArray ja = env->NewFloatArray(4);
    env->SetFloatArrayRegion(ja, 0, 4, ret);
    return ja;
}

/* Returns an array of three floats.
   ret[0] = #bad frames
   ret[1] = error code
   ret[2] = duration
   Error code = 1 for success,
               -1 if initialization failed,
               -2 if insufficient samples
               -3 if tone signal onset not found
               -4 if tone signal end not found
*/
JNIEXPORT jfloatArray JNICALL
    Java_com_android_cts_verifier_audioquality_Native_glitchTest(
        JNIEnv *env, jobject obj,
        jfloat sampleRate, jfloat stimFreq, jfloat onsetThresh,
        jfloat dbSnrThresh, jshortArray jpcm) {
    float ret[3];
    int numSamples = env->GetArrayLength(jpcm);
    short *pcm = new short[numSamples];
    env->GetShortArrayRegion(jpcm, 0, numSamples, pcm);

    GlitchTest gt;
    gt.init(sampleRate, stimFreq, onsetThresh, dbSnrThresh);
    float duration = -1.0;
    int badFrames = -1;
    int success = gt.checkToneSnr(pcm, numSamples, &duration, &badFrames);
    ret[0] = badFrames;
    ret[1] = success;
    ret[2] = duration;
    jfloatArray ja = env->NewFloatArray(3);
    env->SetFloatArrayRegion(ja, 0, 3, ret);
    return ja;
}

/* Returns an array of seven floats.
   ret[0] = num deltas
   ret[1] = error code
   ret[2] = duration
   ret[3] = onset
   ret[4] = offset
   ret[5] = max peak
   ret[6] = min peak
   Error code = 1 for success, -1 for failure. */
JNIEXPORT jfloatArray JNICALL
    Java_com_android_cts_verifier_audioquality_Native_overflowCheck(
        JNIEnv *env, jobject obj,
        jshortArray jpcm, jfloat sampleRate) {
    float ret[7];
    int numSamples = env->GetArrayLength(jpcm);
    short *pcm = new short[numSamples];
    env->GetShortArrayRegion(jpcm, 0, numSamples, pcm);

    float duration = -1.0;
    int numDeltas = -1, onset = -1, offset = -1;
    int maxPeak = 0, minPeak = 0;
    int success = overflowCheck(pcm, numSamples, sampleRate,
            &duration, &numDeltas, &onset, &offset, &maxPeak, &minPeak);
    ret[0] = numDeltas;
    ret[1] = success ? 1 : -1;
    ret[2] = duration;
    ret[3] = onset;
    ret[4] = offset;
    ret[5] = maxPeak;
    ret[6] = minPeak;
    jfloatArray ja = env->NewFloatArray(7);
    env->SetFloatArrayRegion(ja, 0, 7, ret);
    return ja;
}

/* Returns an array of three floats.
   ret[0] = max deviation,
   ret[1] = error code,
   ret[2] = rms deviation.
   Error code = 1 for success, -1 for failure. */
JNIEXPORT jfloatArray JNICALL
    Java_com_android_cts_verifier_audioquality_Native_compareSpectra(
        JNIEnv *env, jobject obj,
        jshortArray jpcm, jshortArray jrefPcm, jfloat sampleRate) {
    float ret[3];
    int numSamples = env->GetArrayLength(jpcm);
    short *pcm = new short[numSamples];
    env->GetShortArrayRegion(jpcm, 0, numSamples, pcm);
    int nRefSamples = env->GetArrayLength(jrefPcm);
    short *refPcm = new short[nRefSamples];
    env->GetShortArrayRegion(jrefPcm, 0, nRefSamples, refPcm);

    float maxDeviation = -1.0, rmsDeviation = -1.0;
    int success = compareSpectra(pcm, numSamples, refPcm, nRefSamples,
            sampleRate, &maxDeviation, &rmsDeviation);
    ret[1] = success ? 1 : -1;

    ret[0] = maxDeviation;
    ret[2] = rmsDeviation;
    jfloatArray ja = env->NewFloatArray(3);
    env->SetFloatArrayRegion(ja, 0, 3, ret);
    return ja;
}

/* Return maximum deviation from linearity in dB.
   On failure returns:
      -1.0 The input signals or sample counts are missing.
      -2.0 The number of input signals is < 2.
      -3.0 The specified sample rate is <= 4000.0
      -4.0 The dB step size for the increase in stimulus level is <= 0.0
      -5.0 The specified reverence stimulus number is out of range.
      -6.0 One or more of the stimuli is too short in duration.
*/
JNIEXPORT jfloat JNICALL
    Java_com_android_cts_verifier_audioquality_Native_linearityTest(
        JNIEnv *env, jobject obj,
        jobjectArray jpcms,
        jfloat sampleRate, jfloat dbStepSize, jint referenceStim) {
    int numSignals = env->GetArrayLength(jpcms);
    int *sampleCounts = new int[numSignals];
    short **pcms = new shortPtr[numSignals];
    jshortArray ja;
    for (int i = 0; i < numSignals; i++) {
        ja = (jshortArray) env->GetObjectArrayElement(jpcms, i);
        sampleCounts[i] = env->GetArrayLength(ja);
        pcms[i] = new short[sampleCounts[i]];
        env->GetShortArrayRegion(ja, 0, sampleCounts[i], pcms[i]);
    }

    float maxDeviation = -1.0;
    int ret = linearityTest(pcms, sampleCounts, numSignals,
            sampleRate, dbStepSize, referenceStim, &maxDeviation);
    delete[] sampleCounts;
    for (int i = 0; i < numSignals; i++) {
        delete[] pcms[i];
    }
    delete[] pcms;
    if (ret < 1) return ret;

    return maxDeviation;
}
