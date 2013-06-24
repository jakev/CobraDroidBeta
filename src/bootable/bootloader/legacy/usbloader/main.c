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
#include <boot/uart.h>
#include <boot/tags.h>
#include <boot/flash.h>
#include <boot/board.h>

#include <bootimg.h>

#define FLASH_PAGE_SIZE 2048
#define FLASH_PAGE_BITS 11

#define ADDR_TAGS    0x10000100

static void create_atags(unsigned taddr, const char *cmdline,
                         unsigned raddr, unsigned rsize)
{
    unsigned n = 0;
    unsigned pcount;
    unsigned *tags = (unsigned *) taddr;

        // ATAG_CORE 
    tags[n++] = 2;
    tags[n++] = 0x54410001;

    if(rsize) {
        // ATAG_INITRD2
        tags[n++] = 4;
        tags[n++] = 0x54420005;
        tags[n++] = raddr;
        tags[n++] = rsize;
    }

    if((pcount = flash_get_ptn_count())){
        ptentry *ptn;
        unsigned pn;
        unsigned m = n + 2;

        for(pn = 0; pn < pcount; pn++) {
            ptn = flash_get_ptn(pn);
            memcpy(tags + m, ptn, sizeof(ptentry));
            m += (sizeof(ptentry) / sizeof(unsigned));
        }
        
        tags[n + 0] = m - n;
        tags[n + 1] = 0x4d534d70;
        n = m;
    }
    if(cmdline && cmdline[0]) {
        const char *src;
        char *dst;
        unsigned len = 0;
        
        dst = (char*) (tags + n + 2);
        src = cmdline;
        while((*dst++ = *src++)) len++;
        
        len++;
        len = (len + 3) & (~3);

            // ATAG_CMDLINE
        tags[n++] = 2 + (len / 4);
        tags[n++] = 0x54410009;

        n += (len / 4);
    }
    
        // ATAG_NONE
    tags[n++] = 0;
    tags[n++] = 0;
}

static void boot_linux(unsigned kaddr)
{
    void (*entry)(unsigned,unsigned,unsigned) = (void*) kaddr;

    entry(0, board_machtype(), ADDR_TAGS);
}

unsigned char raw_header[2048];

int boot_linux_from_flash(void)
{
    boot_img_hdr *hdr = (void*) raw_header;
    unsigned n;
    ptentry *p;
    unsigned offset = 0;
    const char *cmdline;

    if((p = flash_find_ptn("boot")) == 0) {
        cprintf("NO BOOT PARTITION\n");
        return -1;
    }

    if(flash_read(p, offset, raw_header, 2048)) {
        cprintf("CANNOT READ BOOT IMAGE HEADER\n");
        return -1;
    }
    offset += 2048;
    
    if(memcmp(hdr->magic, BOOT_MAGIC, BOOT_MAGIC_SIZE)) {
        cprintf("INVALID BOOT IMAGE HEADER\n");
        return -1;
    }

    n = (hdr->kernel_size + (FLASH_PAGE_SIZE - 1)) & (~(FLASH_PAGE_SIZE - 1));
    if(flash_read(p, offset, (void*) hdr->kernel_addr, n)) {
        cprintf("CANNOT READ KERNEL IMAGE\n");
        return -1;
    }
    offset += n;

    n = (hdr->ramdisk_size + (FLASH_PAGE_SIZE - 1)) & (~(FLASH_PAGE_SIZE - 1));
    if(flash_read(p, offset, (void*) hdr->ramdisk_addr, n)) {
        cprintf("CANNOT READ RAMDISK IMAGE\n");
        return -1;
    }
    offset += n;
    
    dprintf("\nkernel  @ %x (%d bytes)\n", hdr->kernel_addr, hdr->kernel_size);
    dprintf("ramdisk @ %x (%d bytes)\n\n\n", hdr->ramdisk_addr, hdr->ramdisk_size);

    if(hdr->cmdline[0]) {
        cmdline = (char*) hdr->cmdline;
    } else {
        cmdline = board_cmdline();
        if(cmdline == 0) {
            cmdline = "mem=50M console=null";
        }
    }
    cprintf("cmdline = '%s'\n", cmdline);
    
    cprintf("\nBooting Linux\n");

    create_atags(ADDR_TAGS, cmdline,
                 hdr->ramdisk_addr, hdr->ramdisk_size);
    
    boot_linux(hdr->kernel_addr);
    return 0;
}

void usbloader_init(void);
void uart_putc(unsigned);
const char *get_fastboot_version(void);

extern unsigned linux_type;
extern unsigned linux_tags;

static unsigned revision = 0;

char serialno[32];

void dump_smem_info(void);

static void tag_dump(unsigned tag, void *data, unsigned sz, void *cookie)
{
    dprintf("tag type=%x data=%x size=%x\n", tag, (unsigned) data, sz);
}

static struct tag_handler tag_dump_handler = {
    .func = tag_dump,
    .type = 0,
};

void xdcc_putc(unsigned x)
{
    while (dcc_putc(x) < 0) ;
}

#define SERIALNO_STR "androidboot.serialno="
#define SERIALNO_LEN strlen(SERIALNO_STR)

static int boot_from_flash = 1;

void key_changed(unsigned int key, unsigned int down)
{
    if(!down) return;
    if(key == BOOT_KEY_STOP_BOOT) boot_from_flash = 0;
}

static int tags_okay(unsigned taddr)
{
    unsigned *tags = (unsigned*) taddr;

    if(taddr != ADDR_TAGS) return 0;
    if(tags[0] != 2) return 0;
    if(tags[1] != 0x54410001) return 0;

    return 1;
}

int _main(unsigned zero, unsigned type, unsigned tags)
{    
    const char *cmdline = 0;
    int n;
    
    arm11_clock_init();

        /* must do this before board_init() so that we
        ** use the partition table in the tags if it 
        ** already exists 
        */
    if((zero == 0) && (type != 0) && tags_okay(tags)) {
        linux_type = type;
        linux_tags = tags;

        cmdline = tags_get_cmdline((void*) linux_tags);
        
        tags_import_partitions((void*) linux_tags);
        revision = tags_get_revision((void*) linux_tags);
        if(revision == 1) {
            console_set_colors(0x03E0, 0xFFFF);
        }
        if(revision == 2) {
            console_set_colors(0x49B2, 0xFFFF);
        }

            /* we're running as a second-stage, so wait for interrupt */
        boot_from_flash = 0;
    } else {
        linux_type = board_machtype();
        linux_tags = 0;
    }

    board_init();
    keypad_init();
    
    console_init();
    dprintf_set_putc(uart_putc);    

    if(linux_tags == 0) {
            /* generate atags containing partitions 
             * from the bootloader, etc 
             */
        linux_tags = ADDR_TAGS;
        create_atags(linux_tags, 0, 0, 0);
    }
    
    if (cmdline) {
        char *sn = strstr(cmdline, SERIALNO_STR);
        if (sn) {
            char *s = serialno;
            sn += SERIALNO_LEN;
            while (*sn && (*sn != ' ') && ((s - serialno) < 31)) {
                *s++ = *sn++;
            }
            *s++ = 0;
        }
    }

    cprintf("\n\nUSB FastBoot:  V%s\n", get_fastboot_version());
    cprintf("Machine ID:    %d v%d\n", linux_type, revision);
    cprintf("Build Date:    "__DATE__", "__TIME__"\n\n");

    cprintf("Serial Number: %s\n\n", serialno[0] ? serialno : "UNKNOWN");

    flash_dump_ptn();

    flash_init();

        /* scan the keyboard a bit */
    for(n = 0; n < 50; n++) {
        boot_poll();
    }

    if (boot_from_flash) {
        cprintf("\n ** BOOTING LINUX FROM FLASH **\n");
        boot_linux_from_flash();
    }

    usbloader_init();
    
    for(;;) {
        usb_poll();
    }
    return 0;
}
