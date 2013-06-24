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


#ifndef BUFFER_ALLOC_OMAP34XXH_INCLUDED
#define BUFFER_ALLOC_OMAP34XXH_INCLUDED

#include "pv_interface.h"

#ifndef PVMF_FIXEDSIZE_BUFFER_ALLOC_H_INCLUDED
#include "pvmf_fixedsize_buffer_alloc.h"
#endif

/* based on test code in pvmi/media_io/pvmiofileoutput/include/pvmi_media_io_fileoutput.h */

class BufferAllocOmap34xx: public PVInterface, public PVMFFixedSizeBufferAlloc
{
    public:

        BufferAllocOmap34xx();

        virtual ~BufferAllocOmap34xx();

        OSCL_IMPORT_REF void addRef();

        OSCL_IMPORT_REF void removeRef();

        OSCL_IMPORT_REF bool queryInterface(const PVUuid& uuid, PVInterface*& aInterface) ;

        OSCL_IMPORT_REF OsclAny* allocate();

        OSCL_IMPORT_REF void deallocate(OsclAny* ptr) ;

        OSCL_IMPORT_REF uint32 getBufferSize() ;

        OSCL_IMPORT_REF uint32 getNumBuffers() ;

    public:
        int32 refCount;
        int32 bufferSize;
        int32 maxBuffers;
        int32 numAllocated;
        uint8** buffer_address;
};

#endif
