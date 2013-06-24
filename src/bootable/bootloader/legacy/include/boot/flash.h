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

#ifndef _INCLUDE_BOOT_FLASH_H_
#define _INCLUDE_BOOT_FLASH_H_

typedef struct flash_ops flash_ops;
typedef struct ptentry ptentry;

/* flash partitions are defined in terms of blocks
** (flash erase units)
*/
struct ptentry
{
    char name[16];
    unsigned start;
    unsigned length;
    unsigned flags;
};

/* tools to populate and query the partition table */
void flash_add_ptn(ptentry *ptn);
ptentry *flash_find_ptn(const char *name);
ptentry *flash_get_ptn(unsigned n);
unsigned flash_get_ptn_count(void);
void flash_dump_ptn(void);

int flash_init(void);
int flash_erase(ptentry *ptn);
int flash_read_ext(ptentry *ptn, unsigned extra_per_page, unsigned offset, 
               void *data, unsigned bytes);
#define flash_read(ptn, offset, data, bytes) flash_read_ext(ptn, 0, offset, data, bytes)
int flash_write(ptentry *ptn, unsigned extra_per_page,
                const void *data, unsigned bytes);
#endif
