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

#ifndef _BOOT_H_
#define _BOOT_H_

static inline void DWB(void) /* drain write buffer */
{
    asm volatile (
        "mcr p15, 0, %0, c7, c10, 4\n" : : "r" (0)
        );
}

static inline void writel(unsigned val, unsigned addr)
{
    DWB();
    (*(volatile unsigned *) (addr)) = (val);
    DWB();
}

static inline void writeb(unsigned val, unsigned addr)
{
    DWB();
    (*(volatile unsigned char *) (addr)) = (val);
    DWB();
}

static inline unsigned readl(unsigned addr)
{
    return (*(volatile unsigned *) (addr));
}

int dcc_putc(unsigned c);
int dcc_getc();

void enable_irq(void);

/* main.c */
enum boot_keys {
	BOOT_KEY_STOP_BOOT = 1,
	BOOT_KEY_CONTINUE_BOOT = 2,
};
extern void key_changed(unsigned int key, unsigned int is_down) __attribute__ ((weak));

/* manage a list of functions to call */
void boot_register_poll_func(void (*func)(void));
void boot_poll(void);

/* console.c */
void dcc_init();

void dprintf(const char *fmt, ...);
void dprintf_set_putc(void (*func)(unsigned));
void dprintf_set_flush(void (*func)(void));

/* gpio */
void gpio_output_enable(unsigned n, unsigned out);
void gpio_write(unsigned n, unsigned on);
int gpio_read(unsigned n);

/* misc.c */
void *alloc(unsigned sz); /* alloc 32byte aligned memory */
void *alloc_page_aligned(unsigned sz);

void *memcpy(void *dst, const void *src, unsigned len);
void *memset(void *dst, unsigned val, unsigned len);
char *strcpy(char *dst, const char *src);
int strcmp(const char *s1, const char *s2);
int memcmp(const void *a, const void *b, unsigned len);
char *strstr(const char *s1, const char *s2);
int strlen(const char *s);

/* clock */
unsigned cycles_per_second(void);
void print_cpu_speed(void);
void arm11_clock_init(void);
void mdelay(unsigned msecs);
void udelay(unsigned usecs);

/* LCD */
void console_init(void);
void console_set_colors(unsigned bg, unsigned fg);
void console_clear(void);
void console_putc(unsigned n);
void console_flush(void);

void cprintf(const char *fmt, ...);

void mddi_init(void);
void mddi_start_update(void);
int mddi_update_done(void);
void *mddi_framebuffer(void);
void mddi_remote_write(unsigned val, unsigned reg);
extern unsigned fb_width;
extern unsigned fb_height;

/* provided by board files */
void set_led(int on);

/* provided by jtag.c */
void jtag_okay(const char *msg);
void jtag_fail(const char *msg);
void jtag_dputc(unsigned ch);
void jtag_cmd_loop(void (*do_cmd)(const char *, unsigned, unsigned, unsigned));

typedef void (*irq_handler)(unsigned n);


#define DIGEST_SIZE 20
#define SIGNATURE_SIZE 256

void compute_digest(void *data, unsigned len, void *digest_out);
int is_signature_okay(void *digest, void *signature, void *pubkey);

#if 0
#define __attr_used __attribute__((used))
#define __attr_init __attribute__((__section__(".init.func.0")))
#define boot_init_hook(func) \
static int (*__boot_init_hook__)(void) __attr_used __attr_init = func
#endif

#endif
