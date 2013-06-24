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

#ifndef WINDOW_INTERFACE_H
#define WINDOW_INTERFACE_H

class Window {
public:
    Window(int size) : mWindowWeights(0), mWindowSize(0) {
        windowCreate(size);
    }

    Window() : mWindowWeights(0), mWindowSize(0) {
    }

    virtual ~Window(void) {
        windowCleanup();
    }

    /* Create a Hann window of length size.  This allocates memory that
       should be freed using window_cleanup(). */
    void windowCreate(int size);

    /* Free up memory and reset size to 0. */
    void windowCleanup(void);


    /* Multiply the signal in data by the window weights.  Place the
       resulting window_size floating-point values in output.  If preemp
       is != 0.0, apply a 1st-order preemphasis filter, and assume that
       there are window_size+1 samples available in data. */
    void window(short* data, float* output, float preemp);

    int getWindowSize() { return mWindowSize; }

    float* getWindowWeights() { return mWindowWeights; }

private:
    float* mWindowWeights;
    int mWindowSize;

};

#endif /* WINDOW_INTERFACE_H */
