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
#include <msm7k/vic.h>
#include <msm7k/irqs.h>

extern irq_handler irq_vector_table[NR_IRQS];

void unknown_handler(unsigned n)
{
    dprintf("unsolicited irq #%d\n", n);
    for(;;);
}

void irq_init(void)
{
    unsigned n;

    for(n = 0; n < NR_IRQS; n++) {
        irq_vector_table[n] = unknown_handler;
    }
        /* select level interrupts */
    writel(0, VIC_INT_TYPE0);
    writel(0, VIC_INT_TYPE1);
    
        /* select IRQ for all INTs */
    writel(0, VIC_INT_SELECT0);
    writel(0, VIC_INT_SELECT1);

        /* clear interrupts */
    writel(0xffffffff, VIC_INT_CLEAR0);
    writel(0xffffffff, VIC_INT_CLEAR1);
    
        /* disable all INTs */
    writel(0, VIC_INT_EN0);
    writel(0, VIC_INT_EN1);

        /* don't use 1136 vic */
    writel(0, VIC_CONFIG);
    
    writel(1, VIC_INT_MASTEREN);
    
        /* enable IRQs */
    enable_irq();

    (void) readl(VIC_IRQ_VEC_RD);
}

void irq_unmask(unsigned n)
{
    unsigned reg, bit;

    reg = n > 31 ? VIC_INT_EN1 : VIC_INT_EN0;
    bit = 1 << (n & 31);
    
    writel(readl(reg) | bit, reg);
}

void irq_mask(unsigned n)
{
    unsigned reg, bit;

    reg = n > 31 ? VIC_INT_ENCLEAR1 : VIC_INT_ENCLEAR0;
    bit = 1 << (n & 31);
    
    writel(bit, reg);
}

void irq_install(unsigned n, irq_handler func, int edge)
{
    unsigned reg, bit, tmp;

    reg = n > 31 ? VIC_INT_TYPE1 : VIC_INT_TYPE0;
    bit = 1 << (n & 31);
    
    tmp = readl(reg);
    if(edge) {
        writel(tmp | bit, reg);
    } else {
        writel(tmp & (~bit), reg);
    }

    irq_vector_table[n] = func;    
}

