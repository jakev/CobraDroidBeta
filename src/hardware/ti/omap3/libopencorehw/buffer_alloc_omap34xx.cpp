/*
 * OMAP3430 support
 *
 * Author: Michael Barabanov <michael.barabanov@windriver.com>
 * Author: Srini Gosangi <srini.gosangi@windriver.com>

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

#define LOG_NDEBUG 0
#define LOG_TAG "BufferAllocOmap34xx"
#include <utils/Log.h>

#include "buffer_alloc_omap34xx.h"
#include "oscl_mem.h" // needed for oscl_malloc / oscl_free

/* based on test code in pvmi/media_io/pvmiofileoutput/src/pvmi_media_io_fileoutput.cpp */

BufferAllocOmap34xx::BufferAllocOmap34xx(): refCount(0), bufferSize(0), maxBuffers(4), numAllocated(0)
{
}

BufferAllocOmap34xx::~BufferAllocOmap34xx()
{

}

OSCL_EXPORT_REF void BufferAllocOmap34xx::addRef()
{
    ++refCount;
}

OSCL_EXPORT_REF void BufferAllocOmap34xx::removeRef()
{
    --refCount;
    if (refCount <= 0)
    {
    	LOGV("BufferAllocOmap34xx::removeRef()");
       // this->~BufferAllocOmap34xx();
    }
}


OSCL_EXPORT_REF OsclAny* BufferAllocOmap34xx::allocate()
{
    if (numAllocated < maxBuffers)
    {
        OsclAny* ptr = buffer_address[numAllocated];
        if (ptr) ++numAllocated;
        return ptr;
    }
    return NULL;
}

OSCL_EXPORT_REF void BufferAllocOmap34xx::deallocate(OsclAny* ptr)
{
    if (ptr)
    {
        --numAllocated;
    }
}

OSCL_EXPORT_REF uint32 BufferAllocOmap34xx::getBufferSize()
{
    return bufferSize;
}

OSCL_EXPORT_REF uint32 BufferAllocOmap34xx::getNumBuffers()
{
    return maxBuffers;
}


OSCL_EXPORT_REF bool BufferAllocOmap34xx::queryInterface(const PVUuid& uuid, PVInterface*& aInterface)
{
    aInterface = NULL; // initialize aInterface to NULL in case uuid is not supported

    if (PVMFFixedSizeBufferAllocUUID == uuid)
    {
        // Send back ptr to the allocator interface object
        PVMFFixedSizeBufferAlloc* myInterface	= OSCL_STATIC_CAST(PVMFFixedSizeBufferAlloc*, this);
        refCount++; // increment interface refcount before returning ptr
        aInterface = OSCL_STATIC_CAST(PVInterface*, myInterface);
        return true;
    }
    return false;
}
