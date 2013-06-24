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
#include <boot/flash.h>
#include <boot/board.h>
#include <boot/usb.h>

#include <boot/bootimg.h>
#include <boot/tags.h>

#include <boot/gpio.h>

#define VERSION "0.5"

#define REQUIRE_SIGNATURE 0

#if REQUIRE_SIGNATURE
unsigned key_engineering[2 + 64 + 64] = {
    64,0x5b022317,-60769447,648742897,-13657530,585562035,591851935,
    454860199,-1809625305,1868200692,-155297008,-1688439840,-1333607631,
    -483027189,-2051438457,1030069735,819944365,2133377257,-1978924214,
    2109678622,1974978919,-1811463608,765849268,1984092281,921245328,
    -1055062768,1487475997,1209618652,871985152,-611178965,-2057018571,
    335641539,-1196119550,1550548229,-356223887,1909799623,1281016007,
    957001635,1005656532,-1027634024,-1576447610,-1917246637,589192795,
    -1137386186,-1958135372,1933245070,64958951,-1820428322,-1577697840,
    1824253519,555306239,-1588272058,-1925773018,1205934271,-836584444,
    -1140961670,-185198349,1293769947,37045923,1516796974,-297288651,
    651582073,-1337054592,-543971216,-1706823885,-1040652818,-594113104,
    260093481,-1277656496,56493468,1577037283,773995876,244894933,
    -2075797967,783894843,880611008,-1433369702,380946504,-2081431477,
    1377832804,2089455451,-410001201,1245307237,-1228170341,-2062569137,
    -1327614308,-1671042654,1242248660,-418803721,40890010,-1806767460,
    -1468529145,-1058158532,1243817302,-527795003,175453645,-210650325,
    -827053868,-571422860,886300657,2129677324,846504590,-1413102805,
    -1287448511,-1991140134,56194155,1375685594,-129884114,1393568535,
    -1098719620,-935279550,1717137954,-1782544741,272581921,-669183778,
    584824755,1434974827,-1122387971,-810584927,-2147338547,-937541680,
    -313561073,5506366,-1594059648,-1744451574,1896015834,1496367069,
    1742853908,508461291,1905056764
};
#endif

const char *get_fastboot_version(void)
{
    return VERSION;
}

unsigned linux_type = 0;
unsigned linux_tags = 0;

unsigned ramdisk_addr = 0x10400000;
unsigned ramdisk_size = 0;
unsigned kernel_addr = 0x10008000;
unsigned kernel_size = 0;

static void fixup_tags(unsigned *tags, unsigned *out, const char *cmdline)
{
    unsigned *newtags = (unsigned *) 0x10004000;
    unsigned *np = newtags;
    unsigned n;
    char *oldcmdline = "";
    
    if(cmdline == 0) cmdline = "";

        /* CORE */
    *np++ = 2;
    *np++ = 0x54410001;

    if(tags != 0) {
        while(*tags) {
            if(tags[1] == 0x54410001) {
                    /* skip core tag */
                tags += tags[0];
            } else if((tags[1] == 0x54420005) && (ramdisk_size != 0)) {
                    /* skip ramdisk if we have one of our own */
                tags += tags[0];
            } else if((tags[1] == 0x54410009) && (cmdline[0])) {
                    /* skip existing cmdline so we can replace it */
                oldcmdline = (char*) (tags + 2);
                tags += tags[0];
            } else {
                    /* copy any unknown tags */
                n = tags[0];
                while(n-- > 0) {
                    *np++ = *tags++;
                }
            }
        }
    }

        /* create a ramdisk tag if we need to */
    if(ramdisk_size) {
        *np++ = 4;
        *np++ = 0x54420005;
        *np++ = ramdisk_addr;
        *np++ = ramdisk_size;
    }

    dprintf("cmdline: '%s'\n", oldcmdline);
    dprintf("cmdline: '%s'\n", cmdline);
    
        /* create a cmdline tag if we need to */
    if(cmdline[0]) {
        int len;
        char *str = (char*) (np + 2);
        
        len = strlen(oldcmdline);
        if(len) {
            memcpy(str, oldcmdline, len);
            str += len;
            *str++ = ' ';
        }

        len = strlen(cmdline);
        memcpy(str, cmdline, len);
        str += len;
        *str++ = 0;
        
            /* length in words */
        len = ((str - ((char*) (np + 2))) + 3) / 4;

        dprintf("CMDLINE: '%s'\n", ((char*) (np + 2)));
        
        *np++ = 2 + len;
        *np++ = 0x54410009;
        
        np += len;
    }

        /* add footer tag */
    *np++ = 0;
    *np++ = 0;
    
        /* copy it all back to the original tags area */
    while(newtags < np) {
        *out++ = *newtags++;
    }
}

static char cmdline[BOOT_ARGS_SIZE];

static void boot_linux(void)
{
    unsigned *tags = (unsigned*) 0x10000100;
    void (*entry)(unsigned,unsigned,unsigned) = (void*) kernel_addr;

    if(linux_type == 0) {
        linux_type = board_machtype();
    }
    
    fixup_tags((unsigned*) linux_tags, tags, cmdline);    

    entry(0, linux_type, tags);
    
    for(;;);
}

/* convert a boot_image at kernel_addr into a kernel + ramdisk + tags */
static int init_boot_linux(void)
{
    boot_img_hdr *hdr = (void*) kernel_addr;
    unsigned page_mask = 2047;
    unsigned kernel_actual;
    unsigned ramdisk_actual;
    unsigned second_actual;
    
    if((kernel_size < 2048) || memcmp(hdr->magic, BOOT_MAGIC, BOOT_MAGIC_SIZE)){
        dprintf("bootimg: bad header\n");
        return -1;
    }

    if(hdr->page_size != 2048) {
        dprintf("bootimg: invalid page size\n");
        return -1;
    }

    kernel_actual = (hdr->kernel_size + page_mask) & (~page_mask);
    ramdisk_actual = (hdr->ramdisk_size + page_mask) & (~page_mask);
    second_actual = (hdr->second_size + page_mask) & (~page_mask);
    
    if(kernel_size != (kernel_actual + ramdisk_actual + second_actual + 2048)) {
        dprintf("bootimg: invalid image size");
        return -1;
    }

        /* XXX process commandline here */
    if(hdr->cmdline[0]){
        hdr->cmdline[BOOT_ARGS_SIZE - 1] = 0;
        memcpy(cmdline, hdr->cmdline, BOOT_ARGS_SIZE);
    }

        /* XXX how to validate addresses? */
    ramdisk_addr = hdr->ramdisk_addr;
    ramdisk_size = hdr->ramdisk_size;

    kernel_addr = hdr->kernel_addr;
    kernel_size = hdr->kernel_size;
    
    dprintf("bootimg: kernel addr=%x size=%x\n",
            kernel_addr, kernel_size);
    dprintf("bootimg: ramdisk addr=%x size=%x\n",
            ramdisk_addr, ramdisk_size);
    
    memcpy((void*) ramdisk_addr, 
           hdr->magic + 2048 + kernel_actual,
           ramdisk_size);
    
    memcpy((void*) kernel_addr,
           hdr->magic + 2048,
           kernel_size);
    
    return 0; 
}

static struct usb_endpoint *ep1in, *ep1out;
static struct usb_request *rx_req, *tx_req;
static unsigned rx_addr;
static unsigned rx_length;

static char *cmdbuf;

static void usb_rx_cmd_complete(struct usb_request *req, unsigned actual, int status);
static void usb_rx_data_complete(struct usb_request *req, unsigned actual, int status);

static void rx_cmd(void)
{
    struct usb_request *req = rx_req;
    req->buf = cmdbuf;
    req->length = 4096;
    req->complete = usb_rx_cmd_complete;
    usb_queue_req(ep1out, req);
}

static void rx_data(void)
{
    struct usb_request *req = rx_req;
    req->buf = (void*) rx_addr;
    req->length = (rx_length > 4096) ? 4096 : rx_length;
    req->complete = usb_rx_data_complete;
    usb_queue_req(ep1out, req);
}

static void tx_status(const char *status)
{
    struct usb_request *req = tx_req;
    int len = strlen(status);
//    dprintf("tx_status('%s')\n", status);
    memcpy(req->buf, status, len);
    req->length = len;
    req->complete = 0;
    usb_queue_req(ep1in, req);
}

static void usb_rx_data_complete(struct usb_request *req, unsigned actual, int status)
{
    if(status != 0) return;

    if(actual > rx_length) {
        actual = rx_length;
    }

    rx_addr += actual;
    rx_length -= actual;
    
    if(rx_length > 0) {
        rx_data();
    } else {
        tx_status("OKAY");
        rx_cmd();
    }
}

static unsigned hex2unsigned(char *x)
{
    unsigned n = 0;

    while(*x) {
        switch(*x) {
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
            n = (n << 4) | (*x - '0');
            break;
        case 'a': case 'b': case 'c':
        case 'd': case 'e': case 'f':
            n = (n << 4) | (*x - 'a' + 10);
            break;
        case 'A': case 'B': case 'C':
        case 'D': case 'E': case 'F':
            n = (n << 4) | (*x - 'A' + 10);
            break;
        default:
            return n;
        }
        x++;
    }

    return n;
}

static void num_to_hex8(unsigned n, char *out)
{
    static char tohex[16] = "0123456789abcdef";
    int i;
    for(i = 7; i >= 0; i--) {
        out[i] = tohex[n & 15];
        n >>= 4;
    }
    out[8] = 0;
}

extern char serialno[];

static char signature[SIGNATURE_SIZE];

static void usb_rx_cmd_complete(struct usb_request *req, unsigned actual, int status)
{
    if(status != 0) return;
    
    if(actual > 4095) actual = 4095;    
    cmdbuf[actual] = 0;

    dprintf("\n> %s\n",cmdbuf);
    
//    dprintf("usb_rx_cmd_complete() '%s'\n", cmdbuf);  
    
    if(memcmp(cmdbuf, "reboot", 6) == 0) {
        tx_status("OKAY");
        rx_cmd();
        mdelay(100);
        board_reboot();
    }
#if 0
    if(memcmp(cmdbuf, "debug:", 6) == 0) {
        void debug(char *cmd, char *resp);
        memcpy(cmdbuf, "OKAY", 5);
        tx_status(cmdbuf);
        rx_cmd();
        mdelay(5000);
        dprintf("NOW!\n");
        debug(cmdbuf + 6, cmdbuf + 4);
        return;
    }
#endif
    if(memcmp(cmdbuf, "getvar:", 7) == 0) {
        char response[64];
        strcpy(response,"OKAY");
        
        if(!strcmp(cmdbuf + 7, "version")) {
            strcpy(response + 4, VERSION);
        } else if(!strcmp(cmdbuf + 7, "product")) {
            strcpy(response + 4, PRODUCTNAME);
        } else if(!strcmp(cmdbuf + 7, "serialno")) {
            strcpy(response + 4, serialno);
        } else {
            board_getvar(cmdbuf + 7, response + 4);
        }
        tx_status(response);
        rx_cmd();
        return;
    }

    if(memcmp(cmdbuf, "download:", 9) == 0) {
        char status[16];
        rx_addr = kernel_addr;
        rx_length = hex2unsigned(cmdbuf + 9);
        if (rx_length > (64*1024*1024)) {
            tx_status("FAILdata too large");
            rx_cmd();
            return;
        }
        kernel_size = rx_length;
        dprintf("recv data addr=%x size=%x\n", rx_addr, rx_length); 
        strcpy(status,"DATA");
        num_to_hex8(rx_length, status + 4);
        tx_status(status);
        rx_data();
        return;
    }

    if(memcmp(cmdbuf, "erase:", 6) == 0){
        struct ptentry *ptn;
        ptn = flash_find_ptn(cmdbuf + 6);
        if(ptn == 0) {
            tx_status("FAILpartition does not exist");
            rx_cmd();
            return;
        }
        dprintf("erasing '%s'\n", ptn->name);
        cprintf("erasing '%s'", ptn->name);
        if(flash_erase(ptn)) {
            tx_status("FAILfailed to erase partition");
            rx_cmd();
            cprintf(" - FAIL\n");
            return;
        } else {
            dprintf("partition '%s' erased\n", ptn->name);
            cprintf(" - OKAY\n");
        }
        tx_status("OKAY");
        rx_cmd();
        return;
    }

    if(memcmp(cmdbuf, "flash:", 6) == 0){
        struct ptentry *ptn;
        int extra = 0;
        ptn = flash_find_ptn(cmdbuf + 6);
        if(kernel_size == 0) {
            tx_status("FAILno image downloaded");
            rx_cmd();
            return;
        }
        if(ptn == 0) {
            tx_status("FAILpartition does not exist");
            rx_cmd();
            return;
        }
        if(!strcmp(ptn->name,"boot") || !strcmp(ptn->name,"recovery")) {
            if(memcmp((void*) kernel_addr, BOOT_MAGIC, BOOT_MAGIC_SIZE)) {
                tx_status("FAILimage is not a boot image");
                rx_cmd();
                return;
            }
        }
#if REQUIRE_SIGNATURE
        {
            unsigned char digest[DIGEST_SIZE];
            compute_digest((void*) kernel_addr, kernel_size, digest);
            if (is_signature_okay(digest, signature, key_engineering)) {
                dprintf("verified by engineering key\n");
            } else {
                tx_status("FAILsignature did not verify");
                rx_cmd();
                return;
            }
        }
#endif
        if(!strcmp(ptn->name,"system") || !strcmp(ptn->name,"userdata")) {
            extra = 64;
        } else {
            kernel_size = (kernel_size + 2047) & (~2047);
        }
        dprintf("writing %d bytes to '%s'\n", 
                kernel_size, ptn->name);
        cprintf("writing '%s' (%d bytes)", ptn->name, kernel_size);
        if(flash_write(ptn, extra, (void*) kernel_addr, kernel_size)) {
            tx_status("FAILflash write failure");
            rx_cmd();
            cprintf(" - FAIL\n");
            return;
        } else {
            dprintf("partition '%s' updated\n", ptn->name);
            cprintf(" - OKAY\n");
        }
        tx_status("OKAY");
        rx_cmd();
        return;
    }
    if(memcmp(cmdbuf, "boot", 4) == 0) {
        if(init_boot_linux()) {
            tx_status("FAILinvalid boot image");
            rx_cmd();
            return;
        }
        dprintf("booting linux...\n");
        cprintf("\nbooting linux...\n");
        tx_status("OKAY");
        mdelay(10);
        usb_shutdown();
        boot_linux();
        return;
    }
    if(memcmp(cmdbuf, "signature", 9) == 0) {
        if (kernel_size != SIGNATURE_SIZE) {
            tx_status("FAILsignature not 256 bytes long");
            rx_cmd();
            return;
        }
        memcpy(signature, (void*)kernel_addr, SIGNATURE_SIZE);
        tx_status("OKAY");
        rx_cmd();
        return;
    }

    tx_status("FAILinvalid command");
    rx_cmd();
}

void usb_status(unsigned online, unsigned highspeed)
{
    if(online) {
        dprintf("usb: online (%s)\n", highspeed ? "highspeed" : "fullspeed");
        rx_cmd();
    }
}

void usbloader_init(void)
{
    usb_init();

    ep1out = usb_endpoint_alloc(1, 0, 512);
    ep1in = usb_endpoint_alloc(1, 1, 512);
    rx_req = usb_request_alloc(4096);
    tx_req = usb_request_alloc(4096);
    cmdbuf = rx_req->buf;

    boot_register_poll_func(usb_poll);
}
