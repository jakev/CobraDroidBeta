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

#ifndef __ASM_ARCH_MSM7200_VIC_H
#define __ASM_ARCH_MSM7200_VIC_H

#define MSM_VIC_BASE 0xC0000000

#define VIC_REG(off) (MSM_VIC_BASE + (off))

/* See 80-VE113-1 A, pp 218-228 */

#define VIC_IRQ_STATUS0     VIC_REG(0x0000)
#define VIC_IRQ_STATUS1     VIC_REG(0x0004)
#define VIC_FIQ_STATUS0     VIC_REG(0x0008)
#define VIC_FIQ_STATUS1     VIC_REG(0x000C)
#define VIC_RAW_STATUS0     VIC_REG(0x0010)
#define VIC_RAW_STATUS1     VIC_REG(0x0014)
#define VIC_INT_CLEAR0      VIC_REG(0x0018)
#define VIC_INT_CLEAR1      VIC_REG(0x001C)
#define VIC_INT_SELECT0     VIC_REG(0x0020)  /* 1: FIQ, 0: IRQ */
#define VIC_INT_SELECT1     VIC_REG(0x0024)  /* 1: FIQ, 0: IRQ */
#define VIC_INT_EN0         VIC_REG(0x0028)
#define VIC_INT_EN1         VIC_REG(0x002C)
#define VIC_INT_ENCLEAR0    VIC_REG(0x0040)
#define VIC_INT_ENCLEAR1    VIC_REG(0x0044)
#define VIC_SOFTINT0        VIC_REG(0x0050)
#define VIC_SOFTINT1        VIC_REG(0x0054)
#define VIC_INT_MASTEREN    VIC_REG(0x0060)  /* 1: IRQ, 2: FIQ     */
#define VIC_PROTECTION      VIC_REG(0x0064)  /* 1: ENABLE          */
#define VIC_CONFIG          VIC_REG(0x0068)  /* 1: USE ARM1136 VIC */
#define VIC_INT_TYPE0       VIC_REG(0x0070)  /* 1: EDGE, 0: LEVEL  */
#define VIC_INT_TYPE1       VIC_REG(0x0074)  /* 1: EDGE, 0: LEVEL  */
#define VIC_IRQ_VEC_RD      VIC_REG(0x0F00)  /* pending int # */
#define VIC_IRQ_VEC_PEND_RD VIC_REG(0x0F20)  /* pending vector addr */

#define VIC_VECTADDR(n)     VIC_REG(0x0100+((n) * 4))
#define VIC_VECTPRIORITY(n) VIC_REG(0x0200+((n) * 4))

#endif
