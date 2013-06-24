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

#ifndef ANDROID_SURFACE_OUTPUT_OMAP34XXH_INCLUDED
#define ANDROID_SURFACE_OUTPUT_OMAP34XXH_INCLUDED

#include "android_surface_output.h"
#include "buffer_alloc_omap34xx.h"

// support for shared contiguous physical memory
#include <ui/Overlay.h>


class AndroidSurfaceOutputOmap34xx : public AndroidSurfaceOutput
{
    public:
        AndroidSurfaceOutputOmap34xx();

        // frame buffer interface
        virtual bool initCheck();
        virtual void setParametersSync(PvmiMIOSession aSession, PvmiKvp* aParameters, int num_elements, PvmiKvp * & aRet_kvp);
        virtual PVMFStatus getParametersSync(PvmiMIOSession aSession, PvmiKeyType aIdentifier,
                PvmiKvp*& aParameters, int& num_parameter_elements, PvmiCapabilityContext aContext);
        virtual PVMFStatus writeFrameBuf(uint8* aData, uint32 aDataLen, const PvmiMediaXferHeader& data_header_info);

        virtual void closeFrameBuf();
        virtual void postLastFrame();

        OSCL_IMPORT_REF ~AndroidSurfaceOutputOmap34xx();
    private:
        bool                    mUseOverlay;
        sp<Overlay>             mOverlay;
        int                     bufEnc;

        int32                   iNumberOfBuffers;
        int32                   iBufferSize;
        bool                    mIsFirstFrame;
        bool                    mConvert;
    public:
        BufferAllocOmap34xx     mbufferAlloc;
};

#endif // ANDROID_SURFACE_OUTPUT_OMAP34XX_H_INCLUDED
