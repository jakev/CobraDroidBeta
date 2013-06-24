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

#define LOG_TAG "TIHardwareRenderer"
#include <utils/Log.h>

#include "TIHardwareRenderer.h"

#include <media/stagefright/MediaDebug.h>
#include <surfaceflinger/ISurface.h>
#include <ui/Overlay.h>

#include "v4l2_utils.h"

#define CACHEABLE_BUFFERS 0x1

namespace android {

////////////////////////////////////////////////////////////////////////////////

TIHardwareRenderer::TIHardwareRenderer(
        const sp<ISurface> &surface,
        size_t displayWidth, size_t displayHeight,
        size_t decodedWidth, size_t decodedHeight,
        OMX_COLOR_FORMATTYPE colorFormat)
    : mISurface(surface),
      mDisplayWidth(displayWidth),
      mDisplayHeight(displayHeight),
      mDecodedWidth(decodedWidth),
      mDecodedHeight(decodedHeight),
      mColorFormat(colorFormat),
      mInitCheck(NO_INIT),
      mFrameSize(mDecodedWidth * mDecodedHeight * 2),
      mIsFirstFrame(true),
      mIndex(0) {
    CHECK(mISurface.get() != NULL);
    CHECK(mDecodedWidth > 0);
    CHECK(mDecodedHeight > 0);

    if (colorFormat != OMX_COLOR_FormatCbYCrY
            && colorFormat != OMX_COLOR_FormatYUV420Planar) {
        return;
    }

    sp<OverlayRef> ref = mISurface->createOverlay(
            mDecodedWidth, mDecodedHeight, OVERLAY_FORMAT_CbYCrY_422_I, 0);

    if (ref.get() == NULL) {
        LOGE("Unable to create the overlay!");
        return;
    }

    mOverlay = new Overlay(ref);
    mOverlay->setParameter(CACHEABLE_BUFFERS, 0);

    for (size_t i = 0; i < (size_t)mOverlay->getBufferCount(); ++i) {
        mapping_data_t *data =
            (mapping_data_t *)mOverlay->getBufferAddress((void *)i);

        mOverlayAddresses.push(data->ptr);
    }

    mInitCheck = OK;
}

TIHardwareRenderer::~TIHardwareRenderer() {
    if (mOverlay.get() != NULL) {
        mOverlay->destroy();
        mOverlay.clear();

        // XXX apparently destroying an overlay is an asynchronous process...
        sleep(1);
    }
}

// return a byte offset from any pointer
static inline const void *byteOffset(const void* p, size_t offset) {
    return ((uint8_t*)p + offset);
}

static void convertYuv420ToYuv422(
        int width, int height, const void *src, void *dst) {
    // calculate total number of pixels, and offsets to U and V planes
    int pixelCount = height * width;
    int srcLineLength = width / 4;
    int destLineLength = width / 2;
    uint32_t* ySrc = (uint32_t*) src;
    const uint16_t* uSrc = (const uint16_t*) byteOffset(src, pixelCount);
    const uint16_t* vSrc = (const uint16_t*) byteOffset(uSrc, pixelCount >> 2);
    uint32_t *p = (uint32_t*) dst;

    // convert lines
    for (int i = 0; i < height; i += 2) {

        // upsample by repeating the UV values on adjacent lines
        // to save memory accesses, we handle 2 adjacent lines at a time
        // convert 4 pixels in 2 adjacent lines at a time
        for (int j = 0; j < srcLineLength; j++) {

            // fetch 4 Y values for each line
            uint32_t y0 = ySrc[0];
            uint32_t y1 = ySrc[srcLineLength];
            ySrc++;

            // fetch 2 U/V values
            uint32_t u = *uSrc++;
            uint32_t v = *vSrc++;

            // assemble first U/V pair, leave holes for Y's
            uint32_t uv = (u | (v << 16)) & 0x00ff00ff;

            // OR y values and write to memory
            p[0] = ((y0 & 0xff) << 8) | ((y0 & 0xff00) << 16) | uv;
            p[destLineLength] = ((y1 & 0xff) << 8) | ((y1 & 0xff00) << 16) | uv;
            p++;

            // assemble second U/V pair, leave holes for Y's
            uv = ((u >> 8) | (v << 8)) & 0x00ff00ff;

            // OR y values and write to memory
            p[0] = ((y0 >> 8) & 0xff00) | (y0 & 0xff000000) | uv;
            p[destLineLength] = ((y1 >> 8) & 0xff00) | (y1 & 0xff000000) | uv;
            p++;
        }

        // skip the next y line, we already converted it
        ySrc += srcLineLength;
        p += destLineLength;
    }
}

void TIHardwareRenderer::render(
        const void *data, size_t size, void *platformPrivate) {
    // CHECK_EQ(size, mFrameSize);

    if (mOverlay.get() == NULL) {
        return;
    }

#if 0
    size_t i = 0;
    for (; i < mOverlayAddresses.size(); ++i) {
        if (mOverlayAddresses[i] == data) {
            break;
        }

        if (mIsFirstFrame) {
            LOGI("overlay buffer #%d: %p", i, mOverlayAddresses[i]);
        }
    }

    if (i == mOverlayAddresses.size()) {
        LOGE("No suitable overlay buffer found.");
        return;
    }

    mOverlay->queueBuffer((void *)i);

    overlay_buffer_t overlay_buffer;
    if (!mIsFirstFrame) {
        CHECK_EQ(mOverlay->dequeueBuffer(&overlay_buffer), OK);
    } else {
        mIsFirstFrame = false;
    }
#else
    if (mColorFormat == OMX_COLOR_FormatYUV420Planar) {
        convertYuv420ToYuv422(
                mDecodedWidth, mDecodedHeight, data, mOverlayAddresses[mIndex]);
    } else {
        CHECK_EQ(mColorFormat, OMX_COLOR_FormatCbYCrY);

        memcpy(mOverlayAddresses[mIndex], data, size);
    }

    if (mOverlay->queueBuffer((void *)mIndex) == ALL_BUFFERS_FLUSHED) {
        mIsFirstFrame = true;
        if (mOverlay->queueBuffer((void *)mIndex) != 0) {
            return;
        }
    }

    if (++mIndex == mOverlayAddresses.size()) {
        mIndex = 0;
    }

    overlay_buffer_t overlay_buffer;
    if (!mIsFirstFrame) {
        status_t err = mOverlay->dequeueBuffer(&overlay_buffer);

        if (err == ALL_BUFFERS_FLUSHED) {
            mIsFirstFrame = true;
        } else {
            return;
        }
    } else {
        mIsFirstFrame = false;
    }
#endif
}

}  // namespace android

