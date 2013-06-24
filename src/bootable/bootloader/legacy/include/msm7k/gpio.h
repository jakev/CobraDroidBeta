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

#ifndef __ASM_ARCH_MSM7200_GPIO_H
#define __ASM_ARCH_MSM7200_GPIO_H

#define MSM_GPIO1_BASE 0xA9200000
#define MSM_GPIO2_BASE 0xA9300000

/* see 80-VA736-2 Rev C pp 695-751
**
** These are actually the *shadow* gpio registers, since the
** real ones (which allow full access) are only available to the
** ARM9 side of the world.
**
** Since the _BASE need to be page-aligned when we're mapping them
** to virtual addresses, adjust for the additional offset in these
** macros.
*/

#define GPIO1_REG(off) (MSM_GPIO1_BASE + 0x800 + (off))
#define GPIO2_REG(off) (MSM_GPIO2_BASE + 0xC00 + (off))

/* output value */
#define GPIO_OUT_0         GPIO1_REG(0x00)  /* gpio  15-0  */
#define GPIO_OUT_1         GPIO2_REG(0x00)  /* gpio  42-16 */
#define GPIO_OUT_2         GPIO1_REG(0x04)  /* gpio  67-43 */
#define GPIO_OUT_3         GPIO1_REG(0x08)  /* gpio  94-68 */
#define GPIO_OUT_4         GPIO1_REG(0x0C)  /* gpio 106-95 */

/* same pin map as above, output enable */
#define GPIO_OE_0          GPIO1_REG(0x10)
#define GPIO_OE_1          GPIO2_REG(0x08)
#define GPIO_OE_2          GPIO1_REG(0x14)
#define GPIO_OE_3          GPIO1_REG(0x18)
#define GPIO_OE_4          GPIO1_REG(0x1C)

/* same pin map as above, input read */
#define GPIO_IN_0          GPIO1_REG(0x34)
#define GPIO_IN_1          GPIO2_REG(0x20)
#define GPIO_IN_2          GPIO1_REG(0x38)
#define GPIO_IN_3          GPIO1_REG(0x3C)
#define GPIO_IN_4          GPIO1_REG(0x40)

/* same pin map as above, 1=edge 0=level interrup */
#define GPIO_INT_EDGE_0    GPIO1_REG(0x60)
#define GPIO_INT_EDGE_1    GPIO2_REG(0x50)
#define GPIO_INT_EDGE_2    GPIO1_REG(0x64)
#define GPIO_INT_EDGE_3    GPIO1_REG(0x68)
#define GPIO_INT_EDGE_4    GPIO1_REG(0x6C)

/* same pin map as above, 1=positive 0=negative */
#define GPIO_INT_POS_0     GPIO1_REG(0x70)
#define GPIO_INT_POS_1     GPIO2_REG(0x58)
#define GPIO_INT_POS_2     GPIO1_REG(0x74)
#define GPIO_INT_POS_3     GPIO1_REG(0x78)
#define GPIO_INT_POS_4     GPIO1_REG(0x7C)

/* same pin map as above, interrupt enable */
#define GPIO_INT_EN_0      GPIO1_REG(0x80)
#define GPIO_INT_EN_1      GPIO2_REG(0x60)
#define GPIO_INT_EN_2      GPIO1_REG(0x84)
#define GPIO_INT_EN_3      GPIO1_REG(0x88)
#define GPIO_INT_EN_4      GPIO1_REG(0x8C)

/* same pin map as above, write 1 to clear interrupt */
#define GPIO_INT_CLEAR_0   GPIO1_REG(0x90)
#define GPIO_INT_CLEAR_1   GPIO2_REG(0x68)
#define GPIO_INT_CLEAR_2   GPIO1_REG(0x94)
#define GPIO_INT_CLEAR_3   GPIO1_REG(0x98)
#define GPIO_INT_CLEAR_4   GPIO1_REG(0x9C)

/* same pin map as above, 1=interrupt pending */
#define GPIO_INT_STATUS_0  GPIO1_REG(0xA0)
#define GPIO_INT_STATUS_1  GPIO2_REG(0x70)
#define GPIO_INT_STATUS_2  GPIO1_REG(0xA4)
#define GPIO_INT_STATUS_3  GPIO1_REG(0xA8)
#define GPIO_INT_STATUS_4  GPIO1_REG(0xAC)

#endif
