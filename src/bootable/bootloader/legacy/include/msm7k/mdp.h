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

#ifndef __ASM_ARCH_MSM7200_MDP_H
#define __ASM_ARCH_MSM7200_MDP_H

#define MSM_MDP_BASE1 0xAA200000
#define MSM_MDP_BASE2 0xAA210100

/* see 80-VA736-2 C pp 587-627 */

#define MDP_REG1(off) (MSM_MDP_BASE1 + (off))
#define MDP_REG(off) (MSM_MDP_BASE2 + (off))


#define MDP_SYNC_CONFIG_0       MDP_REG1(0x0000)
#define MDP_SYNC_CONFIG_1       MDP_REG1(0x0004)
#define MDP_SYNC_CONFIG_2       MDP_REG1(0x0008)
#define MDP_SYNC_VSYNC_EN       (1 << 17)
#define MDP_SYNC_SYNC_EN        (1 << 16)
#define MDP_SYNC_DIV_CNT(n)     ((n) & 0xffff)

#define MDP_SYNC_STATUS_0       MDP_REG1(0x000C)
#define MDP_SYNC_STATUS_1       MDP_REG1(0x0010)
#define MDP_SYNC_STATUS_2       MDP_REG1(0x0014)
#define MDP_SYNC_FRAME_COUNT(n) (((n) >> 16) & 0xfff)
#define MDP_SYNC_LINE_COUNT(n)  ((n) & 0x3ff)

#define MDP_SYNC_THRESH_0       MDP_REG1(0x0018)
#define MDP_SYNC_SEC_ABOVE(n)   (((n) & 0xFF) << 24)
#define MDP_SYNC_SEC_BELOW(n)   (((n) & 0xFF) << 16)
#define MDP_SYNC_PRIM_ABOVE(n)  (((n) & 0xFF) << 8)
#define MDP_SYNC_PRIM_BELOW(n)  ((n) & 0xFF)

#define MDP_SYNC_THRESH_1       MDP_REG1(0x001C)
#define MDP_SYNC_EXT_ABOVE(n)   (((n) & 0xFF) << 8)
#define MDP_SYNC_EXT_BELOW(n)   ((n) & 0xFF)

#define MDP_INTR_ENABLE         MDP_REG1(0x0020)
#define MDP_INTR_STATUS         MDP_REG1(0x0024)
#define MDP_INTR_CLEAR          MDP_REG1(0x0028)
#define MDP_INTR_LIST0_DONE     (1 << 0)
#define MDP_INTR_LIST1_DONE     (1 << 1)
#define MDP_INTR_DMA_DONE       (1 << 2)
#define MDP_INTR_TV_DONE        (1 << 3)
#define MDP_INTR_CONFIG_ERR     (1 << 4)
#define MDP_INTR_ROI_ERR        (1 << 5)
#define MDP_INTR_TV_UNDERRUN    (1 << 6)

#define MDP_HW_VERSION          MDP_REG1(0x0070)


#define MDP_EDGE_CONFIG         MDP_REG(0x0000)
#define MDP_TILE_CONFIG         MDP_REG(0x0004)

/* BLT controls */
#define MDP_SRC_ROI             MDP_REG(0x0008)
#define MDP_SRCP0_ADDR          MDP_REG(0x000C)
#define MDP_SRCP1_ADDR          MDP_REG(0x0010)
#define MDP_SRCP2_ADDR          MDP_REG(0x0014)
#define MDP_SRCP3_ADDR          MDP_REG(0x0018)
#define MDP_SRCP01_STRIDE       MDP_REG(0x001C)
#define MDP_SRCP23_STRIDE       MDP_REG(0x0020)
#define MDP_SRC_CONFIG          MDP_REG(0x0024)
#define MDP_UNPACK_PATTERN0     MDP_REG(0x0028)
#define MDP_UNPACK_PATTERN1     MDP_REG(0x002C)
#define MDP_UNPACK_PATTERN2     MDP_REG(0x0030)
#define MDP_UNPACK_PATTERN3     MDP_REG(0x0034)
#define MDP_PPP_CONFIG          MDP_REG(0x0038)
#define MDP_PHASEX_INIT         MDP_REG(0x003C)
#define MDP_PHASEY_INIT         MDP_REG(0x0040)
#define MDP_PHASEX_STEP         MDP_REG(0x0044)
#define MDP_PHASEY_STEP         MDP_REG(0x0048)
#define MDP_ALPHA_CONFIG        MDP_REG(0x004C)
#define MDP_DST_CONFIG          MDP_REG(0x0050)
#define MDP_PACK_PATTERN0       MDP_REG(0x0054)
#define MDP_PACK_PATTERN1       MDP_REG(0x0058)
#define MDP_PACK_PATTERN2       MDP_REG(0x005C)
#define MDP_PACK_PATTERN3       MDP_REG(0x0060)
#define MDP_DST_ROI             MDP_REG(0x0064)
#define MDP_DSTP0_ADDR          MDP_REG(0x0068)
#define MDP_DSTP1_ADDR          MDP_REG(0x006C)
#define MDP_DSTP2_ADDR          MDP_REG(0x0070)
#define MDP_DSTP3_ADDR          MDP_REG(0x0074)
#define MDP_DSTP01_STRIDE       MDP_REG(0x0078)
#define MDP_DSTP23_STRIDE       MDP_REG(0x007C)

#endif
