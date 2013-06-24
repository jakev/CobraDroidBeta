#include <linux/autoconf.h>
#include <linux/module.h>
#include <linux/hid.h>

static int __init hid_dummy_init(void)
{
#ifdef CONFIG_HID_A4TECH_MODULE
	HID_COMPAT_CALL_DRIVER(a4tech);
#endif
#ifdef CONFIG_HID_APPLE_MODULE
	HID_COMPAT_CALL_DRIVER(apple);
#endif
#ifdef CONFIG_HID_BELKIN_MODULE
	HID_COMPAT_CALL_DRIVER(belkin);
#endif
#ifdef CONFIG_HID_BRIGHT_MODULE
	HID_COMPAT_CALL_DRIVER(bright);
#endif
#ifdef CONFIG_HID_CHERRY_MODULE
	HID_COMPAT_CALL_DRIVER(cherry);
#endif
#ifdef CONFIG_HID_CHICONY_MODULE
	HID_COMPAT_CALL_DRIVER(chicony);
#endif
#ifdef CONFIG_HID_CYPRESS_MODULE
	HID_COMPAT_CALL_DRIVER(cypress);
#endif
#ifdef CONFIG_HID_DELL_MODULE
	HID_COMPAT_CALL_DRIVER(dell);
#endif
#ifdef CONFIG_HID_EZKEY_MODULE
	HID_COMPAT_CALL_DRIVER(ezkey);
#endif
#ifdef CONFIG_HID_GYRATION_MODULE
	HID_COMPAT_CALL_DRIVER(gyration);
#endif
#ifdef CONFIG_HID_LOGITECH_MODULE
	HID_COMPAT_CALL_DRIVER(logitech);
#endif
#ifdef CONFIG_HID_MICROSOFT_MODULE
	HID_COMPAT_CALL_DRIVER(microsoft);
#endif
#ifdef CONFIG_HID_MONTEREY_MODULE
	HID_COMPAT_CALL_DRIVER(monterey);
#endif
#ifdef CONFIG_HID_NTRIG_MODULE
	HID_COMPAT_CALL_DRIVER(ntrig);
#endif
#ifdef CONFIG_HID_PANTHERLORD_MODULE
	HID_COMPAT_CALL_DRIVER(pantherlord);
#endif
#ifdef CONFIG_HID_PETALYNX_MODULE
	HID_COMPAT_CALL_DRIVER(petalynx);
#endif
#ifdef CONFIG_HID_SAMSUNG_MODULE
	HID_COMPAT_CALL_DRIVER(samsung);
#endif
#ifdef CONFIG_HID_SONY_MODULE
	HID_COMPAT_CALL_DRIVER(sony);
#endif
#ifdef CONFIG_HID_SUNPLUS_MODULE
	HID_COMPAT_CALL_DRIVER(sunplus);
#endif
#ifdef CONFIG_GREENASIA_FF_MODULE
	HID_COMPAT_CALL_DRIVER(greenasia);
#endif
#ifdef CONFIG_THRUSTMASTER_FF_MODULE
	HID_COMPAT_CALL_DRIVER(thrustmaster);
#endif
#ifdef CONFIG_ZEROPLUS_FF_MODULE
	HID_COMPAT_CALL_DRIVER(zeroplus);
#endif

	return -EIO;
}
module_init(hid_dummy_init);

MODULE_LICENSE("GPL");
