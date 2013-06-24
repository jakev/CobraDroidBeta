/*
 * OMAP3430 support
 *
 * Author: Srini Gosangi <srini.gosangi@windriver.com>
 * Author: Michael Barabanov <michael.barabanov@windriver.com>

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 */

/* ------------------------------------------------------------------
 * Copyright (C) 2008 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */


//#define LOG_NDEBUG 0
#ifdef LOG_NDEBUG
#warning LOG_NDEBUG ##LOG_NDEBUG##
#endif

#define LOG_TAG "VideoMio34xx"
#include <utils/Log.h>

#include <cutils/properties.h>
#define UNLIKELY( exp ) (__builtin_expect( (exp) != 0, false ))
static int mDebugFps = 0;

#include "android_surface_output_omap34xx.h"
#include "pv_mime_string_utils.h"
#include "pvmf_video.h"
#include <media/PVPlayer.h>

extern "C" {
#include "v4l2_utils.h"
}

#define CACHEABLE_BUFFERS 0x1

using namespace android;

static void convertYuv420ToYuv422(int width, int height, void* src, void* dst);

OSCL_EXPORT_REF AndroidSurfaceOutputOmap34xx::AndroidSurfaceOutputOmap34xx() :
    AndroidSurfaceOutput()
{
    mUseOverlay = true;
    mOverlay = NULL;
    mIsFirstFrame = true;
    mbufferAlloc.buffer_address = NULL;
    mConvert = false;
}

OSCL_EXPORT_REF AndroidSurfaceOutputOmap34xx::~AndroidSurfaceOutputOmap34xx()
{
    mUseOverlay = false;
}

OSCL_EXPORT_REF bool AndroidSurfaceOutputOmap34xx::initCheck()
{
    LOGV("Calling Vendor(34xx) Specific initCheck");
    
    // reset flags in case display format changes in the middle of a stream
    resetVideoParameterFlags();
    bufEnc = 0;

    // copy parameters in case we need to adjust them
    int displayWidth = iVideoDisplayWidth;
    int displayHeight = iVideoDisplayHeight;
    int frameWidth = iVideoWidth;
    int frameHeight = iVideoHeight;
    int frameSize;
    int videoFormat = OVERLAY_FORMAT_CbYCrY_422_I;
    mapping_data_t *data;
    LOGV("Use Overlays");

    if (mUseOverlay) {
        if(mOverlay == NULL){
            LOGV("using Vendor Specific(34xx) codec");
            sp<OverlayRef> ref = NULL;
            // FIXME:
            // Surfaceflinger may hold onto the previous overlay reference for some
            // time after we try to destroy it. retry a few times. In the future, we
            // should make the destroy call block, or possibly specify that we can
            // wait in the createOverlay call if the previous overlay is in the
            // process of being destroyed.
            for (int retry = 0; retry < 50; ++retry) {
                ref = mSurface->createOverlay(displayWidth, displayHeight, videoFormat, 0);
                if (ref != NULL) break;
                LOGD("Overlay create failed - retrying");
                usleep(100000);
            }
            if ( ref.get() == NULL )
            {
                LOGE("Overlay Creation Failed!");
                return mInitialized;
            }
            mOverlay = new Overlay(ref);
            mOverlay->setParameter(CACHEABLE_BUFFERS, 0);
        }
        else
        {
            mOverlay->resizeInput(displayWidth, displayHeight);
        }

        mbufferAlloc.maxBuffers = 6;  // Hardcoded to work with OMX decoder component
        mbufferAlloc.bufferSize = iBufferSize;
        if (mbufferAlloc.buffer_address) {
            delete [] mbufferAlloc.buffer_address;
        }
        mbufferAlloc.buffer_address = new uint8*[mbufferAlloc.maxBuffers];
        if (mbufferAlloc.buffer_address == NULL) {
            LOGE("unable to allocate mem for overlay addresses");
            return mInitialized;
        }
        LOGV("number of buffers = %d\n", mbufferAlloc.maxBuffers);
        for (int i = 0; i < mbufferAlloc.maxBuffers; i++) {
            data = (mapping_data_t *)mOverlay->getBufferAddress((void*)i);
            mbufferAlloc.buffer_address[i] = (uint8*)data->ptr;
            strcpy((char *)mbufferAlloc.buffer_address[i], "hello");
            if (strcmp((char *)mbufferAlloc.buffer_address[i], "hello")) {
                LOGI("problem with buffer\n");
                return mInitialized;
            }else{
                LOGV("buffer = %d allocated addr=%#lx\n", i, (unsigned long) mbufferAlloc.buffer_address[i]);
            }
        }        
    }
    mInitialized = true;
    LOGV("sendEvent(MEDIA_SET_VIDEO_SIZE, %d, %d)", iVideoDisplayWidth, iVideoDisplayHeight);
    mPvPlayer->sendEvent(MEDIA_SET_VIDEO_SIZE, iVideoDisplayWidth, iVideoDisplayHeight);

    // is conversion necessary?
    if (iVideoSubFormat == PVMF_MIME_YUV420_PLANAR) {
        LOGV("Use YUV420_PLANAR -> YUV422_INTERLEAVED_UYVY converter");
        mConvert = true;
    }

    char value[PROPERTY_VALUE_MAX];
    property_get("debug.video.showfps", value, "0");
    mDebugFps = atoi(value);
    LOGV_IF(mDebugFps, "showfps enabled");

    return mInitialized;
}

static void debugShowFPS()
{
    static int mFrameCount = 0;
    static int mLastFrameCount = 0;
    static nsecs_t mLastFpsTime = 0;
    static float mFps = 0;
    mFrameCount++;
    if (!(mFrameCount & 0x1F)) {
        nsecs_t now = systemTime();
        nsecs_t diff = now - mLastFpsTime;
        mFps = ((mFrameCount - mLastFrameCount) * float(s2ns(1))) / diff;
        mLastFpsTime = now;
        mLastFrameCount = mFrameCount;
        LOGV("%d Frames, %f FPS", mFrameCount, mFps);
    }
    // XXX: mFPS has the value we want
}

PVMFStatus AndroidSurfaceOutputOmap34xx::writeFrameBuf(uint8* aData, uint32 aDataLen, const PvmiMediaXferHeader& data_header_info)
{
    LOGV(" calling Vendor Specific(34xx) writeFrameBuf call");
    if (mSurface == 0) return PVMFFailure;

    if (UNLIKELY(mDebugFps)) {
        debugShowFPS();
    }

    if (mUseOverlay) {
        int ret;

        // Convert from YUV420 to YUV422 for software codec
        if (mConvert) {
            convertYuv420ToYuv422(iVideoWidth, iVideoHeight, aData, mbufferAlloc.buffer_address[bufEnc]);
        } else {
            int i;
            for (i = 0; i < mbufferAlloc.maxBuffers; i++) {
                if (mbufferAlloc.buffer_address[i] == aData) {
                    break;
                }

            }
            if (i == mbufferAlloc.maxBuffers) {
                LOGE("aData does not match any v4l buffer address\n");
                return PVMFSuccess;
            }
            LOGV("queueBuffer %d\n", i);
            bufEnc = i;
        }

        /* This is to reset the buffer queue when stream_off is called as
         * all the buffers are flushed when stream_off is called.
         */
        ret = mOverlay->queueBuffer((void*)bufEnc);
        if (ret == ALL_BUFFERS_FLUSHED) {
            mIsFirstFrame = true;
            mOverlay->queueBuffer((void*)bufEnc);
        }

        overlay_buffer_t overlay_buffer;

        /* This is to prevent dequeueBuffer to be called before the first
         * queueBuffer call is done. If that happens, there will be a delay
         * as the dequeueBuffer call will be blocked.
         */
        if (!mIsFirstFrame)
        {
            ret = mOverlay->dequeueBuffer(&overlay_buffer);
            if (ret != NO_ERROR) {
                if (ret == ALL_BUFFERS_FLUSHED)
                    mIsFirstFrame = true;
                return false;
            }
        }
        else
        {
            mIsFirstFrame = false;
        }

        // advance the overlay index if using color conversion
        if (mConvert) {
            if (++bufEnc == mbufferAlloc.maxBuffers) {
                bufEnc = 0;
            }
        }
    }
    return PVMFSuccess;
}


#define USE_BUFFER_ALLOC 1

/* based on test code in pvmi/media_io/pvmiofileoutput/src/pvmi_media_io_fileoutput.cpp */
void AndroidSurfaceOutputOmap34xx::setParametersSync(PvmiMIOSession aSession,
        PvmiKvp* aParameters,
        int num_elements,
        PvmiKvp * & aRet_kvp)
{
    OSCL_UNUSED_ARG(aSession);
    aRet_kvp = NULL;

#ifndef USE_BUFFER_ALLOC
    AndroidSurfaceOutput::setParametersSync(aSession, aParameters, num_elements, aRet_kvp);
    return;
#endif

    for (int32 i = 0;i < num_elements;i++)
    {
        if (pv_mime_strcmp(aParameters[i].key, MOUT_VIDEO_FORMAT_KEY) == 0)
        {
            iVideoFormatString=aParameters[i].value.pChar_value;
            iVideoFormat=iVideoFormatString.get_str();
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Video Format Key, Value %s",iVideoFormatString.get_str());
        }
        else if (pv_mime_strcmp(aParameters[i].key, PVMF_FORMAT_SPECIFIC_INFO_KEY_YUV) == 0)
        {
            uint8* data = (uint8*)aParameters->value.key_specific_value;
            PVMFYuvFormatSpecificInfo0* yuvInfo = (PVMFYuvFormatSpecificInfo0*)data;

            iVideoWidth = (int32)yuvInfo->width;
            iVideoParameterFlags |= VIDEO_WIDTH_VALID;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Video Width, Value %d", iVideoWidth);

            iVideoHeight = (int32)yuvInfo->height;
            iVideoParameterFlags |= VIDEO_HEIGHT_VALID;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Video Height, Value %d", iVideoHeight);

            iVideoDisplayHeight = (int32)yuvInfo->display_height;
            iVideoParameterFlags |= DISPLAY_HEIGHT_VALID;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Video Display Height, Value %d", iVideoDisplayHeight);


            iVideoDisplayWidth = (int32)yuvInfo->display_width;
            iVideoParameterFlags |= DISPLAY_WIDTH_VALID;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Video Display Width, Value %d", iVideoDisplayWidth);

            iNumberOfBuffers = (int32)yuvInfo->num_buffers;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Number of Buffer, Value %d", iNumberOfBuffers);

            iBufferSize = (int32)yuvInfo->buffer_size;
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Buffer Size, Value %d", iBufferSize);

            LOGV("Ln %d video_format %s", __LINE__, yuvInfo->video_format.getMIMEStrPtr() );
            iVideoSubFormat = yuvInfo->video_format.getMIMEStrPtr();
            iVideoParameterFlags |= VIDEO_SUBFORMAT_VALID;
        }
        else
        {
            //if we get here the key is unrecognized.
            LOGV("AndroidSurfaceOutputOmap34xx::setParametersSync() Error, unrecognized key %s ", aParameters[i].key);

            //set the return value to indicate the unrecognized key
            //and return.
            aRet_kvp = &aParameters[i];
            return;
        }
    }
    /* Copy Code from base class. Ideally we'd just call base class's setParametersSync, but we can't as it will not get to initCheck if it encounters an unrecognized parameter such as the one we're handling here */
    uint32 mycache = iVideoParameterFlags ;
    if( checkVideoParameterFlags() ) {
        initCheck();
    }
    iVideoParameterFlags = mycache;
    if(!iIsMIOConfigured && checkVideoParameterFlags() )
    {
        iIsMIOConfigured = true;
        if(iObserver)
        {
            iObserver->ReportInfoEvent(PVMFMIOConfigurationComplete);
        }
    }
}

/* based on test code in pvmi/media_io/pvmiofileoutput/src/pvmi_media_io_fileoutput.cpp */
PVMFStatus AndroidSurfaceOutputOmap34xx::getParametersSync(PvmiMIOSession aSession, PvmiKeyType aIdentifier,
        PvmiKvp*& aParameters, int& num_parameter_elements,
        PvmiCapabilityContext aContext)
{
#ifdef USE_BUFFER_ALLOC
    OSCL_UNUSED_ARG(aSession);
    OSCL_UNUSED_ARG(aContext);
    aParameters=NULL;

    if (strcmp(aIdentifier, PVMF_BUFFER_ALLOCATOR_KEY) == 0)
    {
        if( iVideoSubFormat != PVMF_MIME_YUV422_INTERLEAVED_UYVY ) {
            LOGV("Ln %d iVideoSubFormat %s. do NOT allocate decoder buffer from overlay", __LINE__, iVideoSubFormat.getMIMEStrPtr() );
            OSCL_LEAVE(OsclErrNotSupported);
            return PVMFErrNotSupported;
        }

        int32 err;
        aParameters = (PvmiKvp*)oscl_malloc(sizeof(PvmiKvp));
        if (!aParameters)
        {
            return PVMFErrNoMemory;
        }
        aParameters[0].value.key_specific_value = (PVInterface*)&mbufferAlloc;
        return PVMFSuccess;
    }

#endif
    return AndroidSurfaceOutput::getParametersSync(aSession, aIdentifier, aParameters, num_parameter_elements, aContext);
}

// post the last video frame to refresh screen after pause
void AndroidSurfaceOutputOmap34xx::postLastFrame()
{
    //do nothing here, this is only for override the Android_Surface_output::PostLastFrame()
    LOGV("AndroidSurfaceOutputOmap34xx::postLastFrame()");
    //mSurface->postBuffer(mOffset);
}

void AndroidSurfaceOutputOmap34xx::closeFrameBuf()
{
    LOGV("Vendor(34xx) Speicif CloseFrameBuf");
    if (UNLIKELY(mDebugFps)) {
        debugShowFPS();
    }
    if (mOverlay != NULL){
        mOverlay->destroy();
        mOverlay = NULL;
    }
    if (mbufferAlloc.buffer_address) {
        delete [] mbufferAlloc.buffer_address;
        mbufferAlloc.buffer_address = NULL;
    }
    if (!mInitialized) return;
    mInitialized = false;
}

// return a byte offset from any pointer
static inline void* byteOffset(void* p, size_t offset) { return (void*)((uint8_t*)p + offset); }

static void convertYuv420ToYuv422(int width, int height, void* src, void* dst)
{

    // calculate total number of pixels, and offsets to U and V planes
    int pixelCount = height * width;
    int srcLineLength = width / 4;
    int destLineLength = width / 2;
    uint32_t* ySrc = (uint32_t*) src;
    uint16_t* uSrc = (uint16_t*) byteOffset(src, pixelCount);
    uint16_t* vSrc = (uint16_t*) byteOffset(uSrc, pixelCount >> 2);
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


// factory function for playerdriver linkage
extern "C" AndroidSurfaceOutputOmap34xx* createVideoMio()
{
    LOGV("Creating Vendor(34xx) Specific MIO component");
    return new AndroidSurfaceOutputOmap34xx();
}
