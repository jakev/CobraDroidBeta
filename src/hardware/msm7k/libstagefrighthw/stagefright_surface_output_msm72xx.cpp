/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <media/stagefright/HardwareAPI.h>

#include "QComHardwareRenderer.h"

using android::sp;
using android::ISurface;
using android::VideoRenderer;

VideoRenderer *createRendererWithRotation(
        const sp<ISurface> &surface,
        const char *componentName,
        OMX_COLOR_FORMATTYPE colorFormat,
        size_t displayWidth, size_t displayHeight,
        size_t decodedWidth, size_t decodedHeight,
        int32_t rotationDegrees) {
    using android::QComHardwareRenderer;

    static const int OMX_QCOM_COLOR_FormatYVU420SemiPlanar = 0x7FA30C00;

    if (colorFormat == OMX_QCOM_COLOR_FormatYVU420SemiPlanar
        && !strncmp(componentName, "OMX.qcom.video.decoder.", 23)) {
        return new QComHardwareRenderer(
                surface, displayWidth, displayHeight,
                decodedWidth, decodedHeight,
                rotationDegrees);
    }

    return NULL;
}
