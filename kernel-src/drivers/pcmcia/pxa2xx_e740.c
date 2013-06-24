/*
 * Toshiba e740 PCMCIA specific routines.
 *
 * (c) 2004 Ian Molton <spyro@f2s.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */

#include <linux/init.h>
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/errno.h>
#include <linux/gpio.h>
#include <linux/interrupt.h>
#include <linux/platform_device.h>

#include <mach/hardware.h>
#include <mach/pxa-regs.h>
#include <mach/eseries-gpio.h>

#include <asm/irq.h>
#include <asm/mach-types.h>

#include "soc_common.h"

static struct pcmcia_irqs cd_irqs[] = {
	{
		.sock = 0,
		.irq  = IRQ_GPIO(GPIO_E740_PCMCIA_CD0),
		.str  = "CF card detect"
	},
	{
		.sock = 1,
		.irq  = IRQ_GPIO(GPIO_E740_PCMCIA_CD1),
		.str  = "Wifi switch"
	},
};

static int e740_pcmcia_hw_init(struct soc_pcmcia_socket *skt)
{
	skt->irq = skt->nr == 0 ? IRQ_GPIO(GPIO_E740_PCMCIA_RDY0) :
				IRQ_GPIO(GPIO_E740_PCMCIA_RDY1);

	return soc_pcmcia_request_irqs(skt, &cd_irqs[skt->nr], 1);
}

/*
 * Release all resources.
 */
static void e740_pcmcia_hw_shutdown(struct soc_pcmcia_socket *skt)
{
	soc_pcmcia_free_irqs(skt, &cd_irqs[skt->nr], 1);
}

static void e740_pcmcia_socket_state(struct soc_pcmcia_socket *skt,
					struct pcmcia_state *state)
{
	if (skt->nr == 0) {
		state->detect = gpio_get_value(GPIO_E740_PCMCIA_CD0) ? 0 : 1;
		state->ready  = gpio_get_value(GPIO_E740_PCMCIA_RDY0) ? 1 : 0;
	} else {
		state->detect = gpio_get_value(GPIO_E740_PCMCIA_CD1) ? 0 : 1;
		state->ready  = gpio_get_value(GPIO_E740_PCMCIA_RDY1) ? 1 : 0;
	}

	state->vs_3v  = 1;
	state->bvd1   = 1;
	state->bvd2   = 1;
	state->wrprot = 0;
	state->vs_Xv  = 0;
}

static int e740_pcmcia_configure_socket(struct soc_pcmcia_socket *skt,
					const socket_state_t *state)
{
	if (state->flags & SS_RESET) {
		if (skt->nr == 0)
			gpio_set_value(GPIO_E740_PCMCIA_RST0, 1);
		else
			gpio_set_value(GPIO_E740_PCMCIA_RST1, 1);
	} else {
		if (skt->nr == 0)
			gpio_set_value(GPIO_E740_PCMCIA_RST0, 0);
		else
			gpio_set_value(GPIO_E740_PCMCIA_RST1, 0);
	}

	switch (state->Vcc) {
	case 0:	/* Socket off */
		if (skt->nr == 0)
			gpio_set_value(GPIO_E740_PCMCIA_PWR0, 0);
		else
			gpio_set_value(GPIO_E740_PCMCIA_PWR1, 1);
		break;
	case 50:
	case 33: /* socket on */
		if (skt->nr == 0)
			gpio_set_value(GPIO_E740_PCMCIA_PWR0, 1);
		else
			gpio_set_value(GPIO_E740_PCMCIA_PWR1, 0);
		break;
	default:
		printk(KERN_ERR "e740_cs: Unsupported Vcc: %d\n", state->Vcc);
	}

	return 0;
}

/*
 * Enable card status IRQs on (re-)initialisation.  This can
 * be called at initialisation, power management event, or
 * pcmcia event.
 */
static void e740_pcmcia_socket_init(struct soc_pcmcia_socket *skt)
{
	soc_pcmcia_enable_irqs(skt, cd_irqs, ARRAY_SIZE(cd_irqs));
}

/*
 * Disable card status IRQs on suspend.
 */
static void e740_pcmcia_socket_suspend(struct soc_pcmcia_socket *skt)
{
	soc_pcmcia_disable_irqs(skt, cd_irqs, ARRAY_SIZE(cd_irqs));
}

static struct pcmcia_low_level e740_pcmcia_ops = {
	.owner            = THIS_MODULE,
	.hw_init          = e740_pcmcia_hw_init,
	.hw_shutdown      = e740_pcmcia_hw_shutdown,
	.socket_state     = e740_pcmcia_socket_state,
	.configure_socket = e740_pcmcia_configure_socket,
	.socket_init      = e740_pcmcia_socket_init,
	.socket_suspend   = e740_pcmcia_socket_suspend,
	.nr               = 2,
};

static struct platform_device *e740_pcmcia_device;

static int __init e740_pcmcia_init(void)
{
	int ret;

	if (!machine_is_e740())
		return -ENODEV;

	e740_pcmcia_device = platform_device_alloc("pxa2xx-pcmcia", -1);
	if (!e740_pcmcia_device)
		return -ENOMEM;

	ret = platform_device_add_data(e740_pcmcia_device, &e740_pcmcia_ops,
					sizeof(e740_pcmcia_ops));

	if (!ret)
		ret = platform_device_add(e740_pcmcia_device);

	if (ret)
		platform_device_put(e740_pcmcia_device);

	return ret;
}

static void __exit e740_pcmcia_exit(void)
{
	platform_device_unregister(e740_pcmcia_device);
}

module_init(e740_pcmcia_init);
module_exit(e740_pcmcia_exit);

MODULE_LICENSE("GPL v2");
MODULE_AUTHOR("Ian Molton <spyro@f2s.com>");
MODULE_ALIAS("platform:pxa2xx-pcmcia");
MODULE_DESCRIPTION("e740 PCMCIA platform support");
