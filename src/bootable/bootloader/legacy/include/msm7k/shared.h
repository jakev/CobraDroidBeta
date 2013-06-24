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

#ifndef MSM7K_SHARED_H
#define MSM7K_SHARED_H

#define MSM7K_SHARED_PHYS  0x01F00000

#define MSM7K_VERSION (MSM7K_SHARED_PHYS + 0x40)

#define VERSION_QDSP6     4
#define VERSION_APPS_SBL  6
#define VERSION_MODEM_SBL 7
#define VERSION_APPS      8
#define VERSION_MODEM     9

void get_version_modem(char *s);
void get_version_modem_sbl(char *s);


#define ACPU_CLK           0  /* Applications processor clock */
#define ADM_CLK            1  /* Applications data mover clock */
#define ADSP_CLK           2  /* ADSP clock */
#define EBI1_CLK           3  /* External bus interface 1 clock */
#define EBI2_CLK           4  /* External bus interface 2 clock */
#define ECODEC_CLK         5  /* External CODEC clock */
#define EMDH_CLK           6  /* External MDDI host clock */
#define GP_CLK             7  /* General purpose clock */
#define GRP_CLK            8  /* Graphics clock */
#define I2C_CLK            9  /* I2C clock */
#define ICODEC_RX_CLK     10  /* Internal CODEX RX clock */
#define ICODEC_TX_CLK     11  /* Internal CODEX TX clock */
#define IMEM_CLK          12  /* Internal graphics memory clock */
#define MDC_CLK           13  /* MDDI client clock */
#define MDP_CLK           14  /* Mobile display processor clock */
#define PBUS_CLK          15  /* Peripheral bus clock */
#define PCM_CLK           16  /* PCM clock */
#define PMDH_CLK          17  /* Primary MDDI host clock */
#define SDAC_CLK          18  /* Stereo DAC clock */
#define SDC1_CLK          19  /* Secure Digital Card clocks */
#define SDC1_PCLK         20
#define SDC2_CLK          21
#define SDC2_PCLK         22
#define SDC3_CLK          23 
#define SDC3_PCLK         24 
#define SDC4_CLK          25
#define SDC4_PCLK         26
#define TSIF_CLK          27  /* Transport Stream Interface clocks */
#define TSIF_REF_CLK      28
#define TV_DAC_CLK        29  /* TV clocks */
#define TV_ENC_CLK        30
#define UART1_CLK         31  /* UART clocks */
#define UART2_CLK         32
#define UART3_CLK         33
#define UART1DM_CLK       34
#define UART2DM_CLK       35
#define USB_HS_CLK        36  /* High speed USB core clock */
#define USB_HS_PCLK       37  /* High speed USB pbus clock */
#define USB_OTG_CLK       38  /* Full speed USB clock */
#define VDC_CLK           39  /* Video controller clock */
#define VFE_CLK           40  /* Camera / Video Front End clock */
#define VFE_MDC_CLK       41  /* VFE MDDI client clock */

enum 
{
    VREG_MSMA_ID,
    VREG_MSMP_ID,
    VREG_MSME1_ID, /* Not supported in Panoramix */
    VREG_MSMC1_ID, /* Not supported in PM6620 */
    VREG_MSMC2_ID, /* Supported in PM7500 only */
    VREG_GP3_ID, /* Supported in PM7500 only */   
    VREG_MSME2_ID, /* Supported in PM7500 and Panoramix only */
    VREG_GP4_ID, /* Supported in PM7500 only */
    VREG_GP1_ID, /* Supported in PM7500 only */
    VREG_TCXO_ID,
    VREG_PA_ID,
    VREG_RFTX_ID,
    VREG_RFRX1_ID,
    VREG_RFRX2_ID,
    VREG_SYNT_ID,
    VREG_WLAN_ID,
    VREG_USB_ID,
    VREG_BOOST_ID,
    VREG_MMC_ID,
    VREG_RUIM_ID,
    VREG_MSMC0_ID, /* Supported in PM6610 only */
    VREG_GP2_ID, /* Supported in PM7500 only */
    VREG_GP5_ID, /* Supported in PM7500 only */
    VREG_GP6_ID, /* Supported in PM7500 only */
    VREG_RF_ID,
    VREG_RF_VCO_ID,
    VREG_MPLL_ID,
    VREG_S2_ID,
    VREG_S3_ID,
    VREG_RFUBM_ID,
    VREG_NCP_ID, 
};

int clock_enable(unsigned id);
int clock_disable(unsigned id);
int clock_set_rate(unsigned id, unsigned hz);
int clock_get_rate(unsigned id);

int vreg_enable(unsigned id);
int vreg_disable(unsigned id);
int vreg_set_level(unsigned id, unsigned mv);

void reboot(void);

#endif
