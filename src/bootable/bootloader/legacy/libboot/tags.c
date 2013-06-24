/*
 * Copyright (C) 2008 The Android Open Source Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the 
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include <boot/tags.h>

#define DBG(x...) do{}while(0)
//#define DBG(x...) dprintf(x)

void tags_parse(void *_tags, struct tag_handler *handlers, unsigned count)
{
    unsigned n;
    unsigned *tags = _tags;
    
    DBG("tags_parse %p\n", tags);
     
        /* make sure there's a CORE marker first */
    if(tags[0] != 2) return;
    if(tags[1] != 0x54410001) return;

    for(;;) {
        unsigned size = tags[0];
        unsigned type = tags[1];
        
        DBG("tags_parse %x %x\n", size, type);
        
        if(size < 2) break;
        
        for(n = 0; n < count; n++) {
            struct tag_handler *h = handlers + n;
            if((h->type == type) || (h->type == 0)) {
                h->func(type, (void*) &tags[2], (size - 2) * 4, h->cookie);
                break;
            }
        }

        tags += size;
    }
}

