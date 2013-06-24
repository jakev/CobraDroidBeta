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
#include <msm7k/shared.h>

static void get_version(char *s, unsigned id)
{
    unsigned *ver = (unsigned*) MSM7K_VERSION;
    unsigned n = ver[id];
    
    snprintf(s, 32, "%d.%d", n >> 16, n & 0xffff);
}

void get_version_modem(char *s)
{
    get_version(s, VERSION_MODEM);
}

void get_version_modem_sbl(char *s)
{
    get_version(s, VERSION_MODEM_SBL);
}

#define MSM_CSR_BASE 0xC0100000

#define MSM_A2M_INT(n) (MSM_CSR_BASE + 0x400 + (n) * 4)

static inline void notify_other_proc_comm(void)
{
	writel(1, MSM_A2M_INT(6));
}

#define APP_COMMAND (MSM7K_SHARED_PHYS + 0x00)
#define APP_STATUS  (MSM7K_SHARED_PHYS + 0x04)
#define APP_DATA1   (MSM7K_SHARED_PHYS + 0x08)
#define APP_DATA2   (MSM7K_SHARED_PHYS + 0x0C)

#define MDM_COMMAND (MSM7K_SHARED_PHYS + 0x10)
#define MDM_STATUS  (MSM7K_SHARED_PHYS + 0x14)
#define MDM_DATA1   (MSM7K_SHARED_PHYS + 0x18)
#define MDM_DATA2   (MSM7K_SHARED_PHYS + 0x1C)


enum
{
	PCOM_CMD_IDLE = 0x0,
	PCOM_CMD_DONE,
	PCOM_RESET_APPS,
	PCOM_RESET_CHIP,
	PCOM_CONFIG_NAND_MPU,
	PCOM_CONFIG_USB_CLKS,
	PCOM_GET_POWER_ON_STATUS,
	PCOM_GET_WAKE_UP_STATUS,
	PCOM_GET_BATT_LEVEL,
	PCOM_CHG_IS_CHARGING,
	PCOM_POWER_DOWN,
	PCOM_USB_PIN_CONFIG,
	PCOM_USB_PIN_SEL,
	PCOM_SET_RTC_ALARM,
	PCOM_NV_READ,
	PCOM_NV_WRITE,
	PCOM_GET_UUID_HIGH,
	PCOM_GET_UUID_LOW,
	PCOM_GET_HW_ENTROPY,
	PCOM_RPC_GPIO_TLMM_CONFIG_REMOTE,
	PCOM_CLKCTL_RPC_ENABLE,
	PCOM_CLKCTL_RPC_DISABLE,
	PCOM_CLKCTL_RPC_RESET,
	PCOM_CLKCTL_RPC_SET_FLAGS,
	PCOM_CLKCTL_RPC_SET_RATE,
	PCOM_CLKCTL_RPC_MIN_RATE,
	PCOM_CLKCTL_RPC_MAX_RATE,
	PCOM_CLKCTL_RPC_RATE,
	PCOM_CLKCTL_RPC_PLL_REQUEST,
	PCOM_CLKCTL_RPC_ENABLED,
	PCOM_VREG_SWITCH,
	PCOM_VREG_SET_LEVEL,
	PCOM_GPIO_TLMM_CONFIG_GROUP,
	PCOM_GPIO_TLMM_UNCONFIG_GROUP,
	PCOM_NV_READ_HIGH_BITS,
	PCOM_NV_WRITE_HIGH_BITS,
	PCOM_NUM_CMDS,
};

enum
{
	 PCOM_INVALID_STATUS = 0x0,
	 PCOM_READY,
	 PCOM_CMD_RUNNING,
	 PCOM_CMD_SUCCESS,
	 PCOM_CMD_FAIL,
};

int msm_proc_comm(unsigned cmd, unsigned *data1, unsigned *data2)
{
	int ret = -1;

	while (readl(MDM_STATUS) != PCOM_READY) {
		/* XXX check for A9 reset */
	}

	writel(cmd, APP_COMMAND);
	if (data1)
		writel(*data1, APP_DATA1);
	if (data2)
		writel(*data2, APP_DATA2);

	notify_other_proc_comm();
	while (readl(APP_COMMAND) != PCOM_CMD_DONE) {
		/* XXX check for A9 reset */
	}

	if (readl(APP_STATUS) != PCOM_CMD_FAIL) {
		if (data1)
			*data1 = readl(APP_DATA1);
		if (data2)
			*data2 = readl(APP_DATA2);
		ret = 0;
	}

	return ret;
}

int clock_enable(unsigned id)
{
    return msm_proc_comm(PCOM_CLKCTL_RPC_ENABLE, &id, 0);
}

int clock_disable(unsigned id)
{
    return msm_proc_comm(PCOM_CLKCTL_RPC_DISABLE, &id, 0);
}

int clock_set_rate(unsigned id, unsigned rate)
{
    return msm_proc_comm(PCOM_CLKCTL_RPC_SET_RATE, &id, &rate);
}

int clock_get_rate(unsigned id)
{
    if (msm_proc_comm(PCOM_CLKCTL_RPC_RATE, &id, 0)) {
        return -1;
    } else {
        return (int) id;
    }
}

void reboot(void)
{
    msm_proc_comm(PCOM_RESET_CHIP, 0, 0);
    for (;;) ;
}

int vreg_enable(unsigned id)
{
    unsigned n = 1;
    return msm_proc_comm(PCOM_VREG_SWITCH, &id, &n);
}

int vreg_disable(unsigned id)
{
    unsigned n = 0;
    return msm_proc_comm(PCOM_VREG_SWITCH, &id, &n);
}

int vreg_set_level(unsigned id, unsigned level)
{
    return msm_proc_comm(PCOM_VREG_SET_LEVEL, &id, &level);
}


