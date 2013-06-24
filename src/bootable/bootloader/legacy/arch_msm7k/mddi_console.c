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
#include <boot/font5x12.h>

static unsigned short BGCOLOR = 0x001F;
static unsigned short FGCOLOR = 0xFFFF;

static void drawglyph(unsigned short *pixels, unsigned short paint,
                      unsigned stride, unsigned *glyph)
{
    unsigned x, y, data;
    stride -= 5;

    data = glyph[0];
    for(y = 0; y < 6; y++) {
        for(x = 0; x < 5; x++) {
            if(data & 1) *pixels = paint;
            data >>= 1;
            pixels++;
        }
        pixels += stride;
    }
    data = glyph[1];
    for(y = 0; y < 6; y++) {
        for(x = 0; x < 5; x++) {
            if(data & 1) *pixels = paint;
            data >>= 1;
            pixels++;
        }
        pixels += stride;
    }
}

#if 0
static void drawtext(unsigned x, unsigned y, unsigned paint, const char *text)
{
    unsigned short *pixels = mddi_framebuffer();
    unsigned stride = fb_width;
    char c;

    while((c = *text++)) {
        if((c < ' ') || (c > 127)) continue;
        c = (c - 32) * 2;
        drawglyph(pixels + y*stride + x, paint, stride, font5x12 + c);
        x += 6;
    }
}
#endif

static int cx, cy, cmaxx, cmaxy;

void console_clear(void)
{
    unsigned short *dst = mddi_framebuffer();
    unsigned count = fb_width * fb_height;
    cx = 0;
    cy = 0;
    while(count--) *dst++ = BGCOLOR;
}

static void scroll_up(void)
{
    unsigned short *dst = mddi_framebuffer();
    unsigned short *src = dst + (fb_width * 12);
    unsigned count = fb_height * (fb_width - 12);

    while(count--) {
        *dst++ = *src++;
    }
    count = fb_width * 12;
    while(count--) {
        *dst++ = BGCOLOR;
    }       
}

void console_putc(unsigned c)
{
    unsigned short *pixels;
    
    if(c > 127) return;
    if(c < 32) {
        if(c == '\n') goto newline;
        return;
    }

    pixels = mddi_framebuffer();
    drawglyph(pixels + cy * 12 * fb_width + cx * 6, FGCOLOR,
              fb_width, font5x12 + (c - 32) * 2);

    cx++;
    if(cx < cmaxx) return;

newline:
    cy++;
    cx = 0;
    if(cy >= cmaxy) {
        cy = cmaxy - 1;
        scroll_up();
    }
}

void console_flush(void)
{
	mddi_start_update();
	while(!mddi_update_done()) ;
}

void console_set_colors(unsigned bg, unsigned fg)
{
    BGCOLOR = bg;
    FGCOLOR = fg;
}

void console_init(void)
{
    mddi_init();

    cmaxx = fb_width / 6;
    cmaxy = (fb_height-1) / 12;
    cx = 0;
    cy = 0;

    console_clear();
    console_flush();
}

