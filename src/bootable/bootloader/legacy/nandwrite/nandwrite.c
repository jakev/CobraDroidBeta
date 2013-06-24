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

#include <boot/boot.h>
#include <boot/board.h>
#include <boot/flash.h>

#define FLASH_PAGE_SIZE 2048
#define FLASH_PAGE_BITS 11

int startswith(const char *str, const char *prefix)
{
    while(*prefix){
        if(*prefix++ != *str++) return 0;
    }
    return 1;
}

void verify_flash(ptentry *p, void *addr, unsigned len, int extra)
{
	int offset = 0;
	void *buf = alloc(FLASH_PAGE_SIZE + extra);
	int verify_extra = extra;
	if(verify_extra > 4)
		verify_extra = 16;
	while(len > 0) {
		flash_read_ext(p, extra, offset, buf, FLASH_PAGE_SIZE);
		if(memcmp(addr, buf, FLASH_PAGE_SIZE + verify_extra)) {
			dprintf("verify failed at %x\n", offset);
	        jtag_fail("verify failed");
			return;
		}
		offset += FLASH_PAGE_SIZE;
		addr += FLASH_PAGE_SIZE;
		len -= FLASH_PAGE_SIZE;
		if(extra) {
			addr += extra;
			len -= extra;
		}
	}
	dprintf("verify done %d extra bytes\n", verify_extra);
	jtag_okay("verify done");
}

void handle_flash(const char *name, unsigned addr, unsigned len)
{
    int r;
    ptentry *p;

    dprintf("image @ %x (%d bytes)\n", addr, len);
    dprintf("write to '%s' partition\n", name);

    p = flash_find_ptn(name);

    if(p == 0) {
        jtag_fail("partition not found");
        return;
    } else {
        if(flash_init()) {
            jtag_fail("flash_init() failed");
            return;
        }

        dprintf("erasing flash @ %d (len=%d)\n", p->start, p->length);
        flash_erase(p);

        if(len) {
            dprintf("writing flash at @ %d\n", p->start);

            if(!strcmp(name, "system") || !strcmp(name, "userdata")) {
                r = flash_write(p, 64, (void*) addr, len);
            } else {
                len = (len + FLASH_PAGE_SIZE - 1) & (~(FLASH_PAGE_SIZE-1));
                r = flash_write(p, 0, (void*) addr, len);
            }
			//verify_flash(p, addr, len, (!strcmp(name, "system") || !strcmp(name, "userdata")) ? 64 : 0);
            if(r) {
                jtag_fail("partition write failed");
            } else {
                jtag_okay("partition written");
            }
            return;
        } else {
            jtag_okay("partition erased");
            return;
        }
    }
}

void hexdump(void *ptr, unsigned len)
{
    unsigned char *b = ptr;
    int count = 0;

    dprintf("%x: ", (unsigned) b);
    while(len-- > 0) {
        dprintf("%b ", *b++);
        if(++count == 16) {
            dprintf("\n%x: ", (unsigned) b);
            count = 0;
        }
    }
    if(count != 0) dprintf("\n");
}

static unsigned char *tmpbuf = 0;

void handle_dump(const char *name, unsigned offset)
{
    ptentry *p;
    
    if(tmpbuf == 0) {
        tmpbuf = alloc(4096);
    }

    dprintf("dump '%s' partition\n", name);
    p = flash_find_ptn(name);

    if(p == 0) {
        jtag_fail("partition not found");
        return;
    }
    
    if(flash_init()) {
        jtag_fail("flash_init() failed");
        return;
    }

#if 0
        /* XXX reimpl */
    if(flash_read_page(p->start * 64, tmpbuf, tmpbuf + 2048)){
        jtag_fail("flash_read() failed");
        return;
    }
#endif

    dprintf("page %d data:\n", p->start * 64);
    hexdump(tmpbuf, 256);
    dprintf("page %d extra:\n", p->start * 64);
    hexdump(tmpbuf, 16);
    jtag_okay("done");
}

void handle_command(const char *cmd, unsigned a0, unsigned a1, unsigned a2)
{
    if(startswith(cmd,"flash:")){
        handle_flash(cmd + 6, a0, a1);
        return;
    }

    if(startswith(cmd,"dump:")){
        handle_dump(cmd + 5, a0);
        return;
    }

    jtag_fail("unknown command");
}

int _main(void)
{
    arm11_clock_init();

    dprintf_set_putc(jtag_dputc);

    board_init();
    
    jtag_cmd_loop(handle_command);

    return 0;
}
