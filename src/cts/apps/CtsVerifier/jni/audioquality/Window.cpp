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

#include "Window.h"

#include <stdlib.h>
#include <math.h>

void Window::windowCreate(int size) {
    if (mWindowWeights && (mWindowSize < size)) {
        delete [] mWindowWeights;
        mWindowWeights = NULL;
        mWindowSize = 0;
    }
    if (!mWindowWeights) {
        mWindowWeights = new float[size];
    }
    if (mWindowSize != size) {
        double arg = M_PI * 2.0 / size;
        mWindowSize = size;
        for (int i = 0; i < size; ++i) {
            mWindowWeights[i] = 0.5 - (0.5 * cos((i + 0.5) * arg));
        }
    }
}

void Window::windowCleanup() {
    mWindowSize = 0;
    delete [] mWindowWeights;
    mWindowWeights = NULL;
}

/* Multiply the signal in data by the window weights.  Place the
   resulting mWindowSize floating-point values in output.  If preemp
   is != 0.0, apply a 1st-order preemphasis filter, and assume that
   there are mWindowSize+1 samples available in data. */
void Window::window(short* data, float* output, float preemp) {
    if (preemp == 0.0) {
        for (int i = 0; i < mWindowSize; ++i)
            output[i] = data[i] * mWindowWeights[i];
    } else {
        for (int i = 0; i < mWindowSize; ++i)
            output[i] = (data[i+1] - (preemp * data[i])) * mWindowWeights[i];
    }
}

