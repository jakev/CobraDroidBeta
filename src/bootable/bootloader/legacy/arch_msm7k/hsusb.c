/*
 * Copyright (c) 2008, Google Inc.
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
#include <msm7k/hsusb.h>
#include <boot/usb.h>
#include <boot/usb_descriptors.h>

#if 1
#define DBG(x...) do {} while(0)
#else
#define DBG(x...) dprintf(x)
#endif

struct usb_endpoint
{
    struct usb_endpoint *next;
    unsigned bit;
    struct ept_queue_head *head;
    struct usb_request *req;
    unsigned char num;
    unsigned char in;
};

struct usb_endpoint *ept_list = 0;
struct ept_queue_head *epts = 0;

static int usb_online = 0;
static int usb_highspeed = 0;

struct usb_endpoint *usb_endpoint_alloc(unsigned num, unsigned in, unsigned max_pkt)
{
    struct usb_endpoint *ept;
    unsigned cfg;

    ept = alloc(sizeof(*ept));
    
    ept->num = num;
    ept->in = !!in;
    ept->req = 0;
    
    cfg = CONFIG_MAX_PKT(max_pkt) | CONFIG_ZLT;

    if(ept->in) {
        ept->bit = EPT_TX(ept->num);
    } else {
        ept->bit = EPT_RX(ept->num);
        if(num == 0) 
            cfg |= CONFIG_IOS;
    }

    ept->head = epts + (num * 2) + (ept->in);
    ept->head->config = cfg;

    ept->next = ept_list;
    ept_list = ept;
    
    DBG("ept%d %s @%p/%p max=%d bit=%x\n", 
            num, in ? "in":"out", ept, ept->head, max_pkt, ept->bit);

    return ept;
}

static void endpoint_enable(struct usb_endpoint *ept, unsigned yes)
{
    unsigned n = readl(USB_ENDPTCTRL(ept->num));

    if(yes) {
        if(ept->in) {
            n |= (CTRL_TXE | CTRL_TXR | CTRL_TXT_BULK);
        } else {
            n |= (CTRL_RXE | CTRL_RXR | CTRL_RXT_BULK);
        }

        if(ept->num != 0) {
                /* XXX should be more dynamic... */
            if(usb_highspeed) {
                ept->head->config = CONFIG_MAX_PKT(512) | CONFIG_ZLT;
            } else {
                ept->head->config = CONFIG_MAX_PKT(64) | CONFIG_ZLT;
            }
        }
    }
    writel(n, USB_ENDPTCTRL(ept->num));
}

struct usb_request *usb_request_alloc(unsigned bufsiz)
{
    struct usb_request *req;
    req = alloc(sizeof(*req));
    req->buf = alloc(bufsiz);
    req->item = alloc(32);
    return req;
}

int usb_queue_req(struct usb_endpoint *ept, struct usb_request *req)
{
    struct ept_queue_item *item = req->item;
    unsigned phys = (unsigned) req->buf;
    
    item->next = TERMINATE;
    item->info = INFO_BYTES(req->length) | INFO_IOC | INFO_ACTIVE;
    item->page0 = phys;
    item->page1 = (phys & 0xfffff000) + 0x1000;
    
    ept->head->next = (unsigned) item;
    ept->head->info = 0;
    ept->req = req;
    
    DBG("ept%d %s queue req=%p\n",
            ept->num, ept->in ? "in" : "out", req);

    writel(ept->bit, USB_ENDPTPRIME);
    return 0;
}

static void handle_ept_complete(struct usb_endpoint *ept)
{
    struct ept_queue_item *item;
    unsigned actual;
    int status;
    struct usb_request *req;
    
    DBG("ept%d %s complete req=%p\n",
            ept->num, ept->in ? "in" : "out", ept->req);
    
    req = ept->req;
    if(req) {
        ept->req = 0;
        
        item = req->item;

        if(item->info & 0xff) {
            actual = 0;
            status = -1;
            dprintf("EP%d/%s FAIL nfo=%x pg0=%x\n",
                    ept->num, ept->in ? "in" : "out", item->info, item->page0);
        } else {
            actual = req->length - ((item->info >> 16) & 0x7fff);
            status = 0;
        }
        if(req->complete)
            req->complete(req, actual, status);
    }
}

static const char *reqname(unsigned r)
{
    switch(r) {
    case GET_STATUS: return "GET_STATUS";
    case CLEAR_FEATURE: return "CLEAR_FEATURE";
    case SET_FEATURE: return "SET_FEATURE";
    case SET_ADDRESS: return "SET_ADDRESS";
    case GET_DESCRIPTOR: return "GET_DESCRIPTOR";
    case SET_DESCRIPTOR: return "SET_DESCRIPTOR";
    case GET_CONFIGURATION: return "GET_CONFIGURATION";
    case SET_CONFIGURATION: return "SET_CONFIGURATION";
    case GET_INTERFACE: return "GET_INTERFACE";
    case SET_INTERFACE: return "SET_INTERFACE";
    default: return "*UNKNOWN*";
    }
}

static struct usb_endpoint *ep0in, *ep0out;
static struct usb_request *ep0req;

static void setup_ack(void)
{
    ep0req->complete = 0;
    ep0req->length = 0;
    usb_queue_req(ep0in, ep0req);
}

static void ep0in_complete(struct usb_request *req, unsigned actual, int status)
{
    DBG("ep0in_complete %p %d %d\n", req, actual, status);
    if(status == 0) {
        req->length = 0;
        req->complete = 0;
        usb_queue_req(ep0out, req);
    }
}

static void setup_tx(void *buf, unsigned len)
{
    DBG("setup_tx %p %d\n", buf, len);
    memcpy(ep0req->buf, buf, len);
    ep0req->complete = ep0in_complete;
    ep0req->length = len;
    usb_queue_req(ep0in, ep0req);
}

static unsigned char usb_config_value = 0;

#define SETUP(type,request) (((type) << 8) | (request))

static void handle_setup(struct usb_endpoint *ept)
{
    setup_packet s;
    
    memcpy(&s, ept->head->setup_data, sizeof(s));
    writel(ept->bit, USB_ENDPTSETUPSTAT);

    DBG("handle_setup type=0x%b req=0x%b val=%d idx=%d len=%d (%s)\n",
            s.type, s.request, s.value, s.index, s.length,
            reqname(s.request));

    switch(SETUP(s.type,s.request)) {
    case SETUP(DEVICE_READ, GET_STATUS): {
        unsigned zero = 0;
        if(s.length == 2) {
            setup_tx(&zero, 2);
            return;
        }
        break;
    }
    case SETUP(DEVICE_READ, GET_DESCRIPTOR): {
        dtable *d = usb_highspeed ? descr_hs : descr_fs;
        while(d->data) {
            if(s.value == d->id) {
                unsigned len = d->length;
                if(len > s.length) len = s.length;
                setup_tx(d->data, len);
                return;
            }
            d++;
        }
        break;
    }
    case SETUP(DEVICE_READ, GET_CONFIGURATION):
            /* disabling this causes data transaction failures on OSX. Why?
             */
        if((s.value == 0) && (s.index == 0) && (s.length == 1)) {
            setup_tx(&usb_config_value, 1);
            return;
        }
        break;
    case SETUP(DEVICE_WRITE, SET_CONFIGURATION):
        if(s.value == 1) {
            struct usb_endpoint *ept;
                /* enable endpoints */
            for(ept = ept_list; ept; ept = ept->next){
                if(ept->num == 0) 
                    continue;
                endpoint_enable(ept, s.value);
            }
            usb_config_value = 1;
        } else {
            writel(0, USB_ENDPTCTRL(1));
            usb_config_value = 0;
        }
        setup_ack();
        usb_online = s.value ? 1 : 0;
        usb_status(s.value ? 1 : 0, usb_highspeed);
        return;
    case SETUP(DEVICE_WRITE, SET_ADDRESS):
            /* write address delayed (will take effect
            ** after the next IN txn)
            */
        writel((s.value << 25) | (1 << 24), USB_DEVICEADDR);
        setup_ack();
        return;
    case SETUP(INTERFACE_WRITE, SET_INTERFACE):
            /* if we ack this everything hangs */
            /* per spec, STALL is valid if there is not alt func */
        goto stall;
    case SETUP(ENDPOINT_WRITE, CLEAR_FEATURE): {
        struct usb_endpoint *ept;
        unsigned num = s.index & 15;
        unsigned in = !!(s.index & 0x80);
        
        if((s.value == 0) && (s.length == 0)) {
            DBG("clr feat %d %d\n", num, in);
            for(ept = ept_list; ept; ept = ept->next) {
                if((ept->num == num) && (ept->in == in)) {
                    endpoint_enable(ept, 1);
                    setup_ack();
                    return;
                }
            }
        }
        break;
    }
    }

    dprintf("STALL %s %b %b %d %d %d\n",
            reqname(s.request),
            s.type, s.request, s.value, s.index, s.length);

stall:
    writel((1<<16) | (1 << 0), USB_ENDPTCTRL(ept->num));    
}

unsigned ulpi_read(unsigned reg)
{
        /* initiate read operation */
    writel(ULPI_RUN | ULPI_READ | ULPI_ADDR(reg),
               USB_ULPI_VIEWPORT);

        /* wait for completion */
    while(readl(USB_ULPI_VIEWPORT) & ULPI_RUN) ;
    
    return ULPI_DATA_READ(readl(USB_ULPI_VIEWPORT));
}

void ulpi_write(unsigned val, unsigned reg)
{
        /* initiate write operation */
    writel(ULPI_RUN | ULPI_WRITE | 
               ULPI_ADDR(reg) | ULPI_DATA(val),
               USB_ULPI_VIEWPORT);

        /* wait for completion */
    while(readl(USB_ULPI_VIEWPORT) & ULPI_RUN) ;
}

void board_usb_init(void);
void board_ulpi_init(void);

void usb_init(void) 
{
    epts = alloc_page_aligned(4096);

    memset(epts, 0, 32 * sizeof(struct ept_queue_head));
    
    board_usb_init();
    
        /* select ULPI phy */
    writel(0x81000000, USB_PORTSC);

        /* RESET */
    writel(0x00080002, USB_USBCMD);
    mdelay(20);
    
    board_ulpi_init();
    
    writel((unsigned) epts, USB_ENDPOINTLISTADDR);

        /* select DEVICE mode */
    writel(0x02, USB_USBMODE);

    writel(0xffffffff, USB_ENDPTFLUSH);
    
        /* go to RUN mode (D+ pullup enable) */
    writel(0x00080001, USB_USBCMD);


    ep0out = usb_endpoint_alloc(0, 0, 64);
    ep0in = usb_endpoint_alloc(0, 1, 64);
    ep0req = usb_request_alloc(4096);
}

void usb_shutdown(void)
{
        /* disable pullup */
    writel(0x0008000, USB_USBCMD);
    mdelay(10);
}

void usb_poll(void) 
{
    struct usb_endpoint *ept;
    unsigned n = readl(USB_USBSTS);
    writel(n, USB_USBSTS);
    
    n &= (STS_SLI | STS_URI | STS_PCI | STS_UI | STS_UEI);
    
    if(n == 0) return;
    
    if(n & STS_URI) {
        writel(readl(USB_ENDPTCOMPLETE), USB_ENDPTCOMPLETE);
        writel(readl(USB_ENDPTSETUPSTAT), USB_ENDPTSETUPSTAT);
        writel(0xffffffff, USB_ENDPTFLUSH);
        writel(0, USB_ENDPTCTRL(1));
        DBG("-- reset --\n");
        usb_online = 0;
        usb_config_value = 0;

            /* error out any pending reqs */
        for(ept = ept_list; ept; ept = ept->next) {
            ept->head->info = INFO_ACTIVE;
            handle_ept_complete(ept);
        }
        usb_status(0, usb_highspeed);
    }
    if(n & STS_SLI) {
        DBG("-- suspend --\n");
    }
    if(n & STS_PCI) {
        DBG("-- portchange --\n");
        unsigned spd = (readl(USB_PORTSC) >> 26) & 3;
        if(spd == 2) {
            usb_highspeed = 1;
        } else {
            usb_highspeed = 0;
        }
    }
    if(n & STS_UEI) dprintf("<UEI %x>\n", readl(USB_ENDPTCOMPLETE));
#if 0
    DBG("STS: ");
    if(n & STS_UEI) DBG("ERROR ");
    if(n & STS_SLI) DBG("SUSPEND ");
    if(n & STS_URI) DBG("RESET ");
    if(n & STS_PCI) DBG("PORTCHANGE ");
    if(n & STS_UI) DBG("USB ");
    DBG("\n");
#endif
    if((n & STS_UI) || (n & STS_UEI)) {
        n = readl(USB_ENDPTSETUPSTAT);
        if(n & EPT_RX(0)) {
            handle_setup(ep0out);
        }

        n = readl(USB_ENDPTCOMPLETE);
        if(n != 0) {
            writel(n, USB_ENDPTCOMPLETE);
        }

        for(ept = ept_list; ept; ept = ept->next){
            if(n & ept->bit) {
                handle_ept_complete(ept);
            }
        }
    }
//    dprintf("@\n");
}


