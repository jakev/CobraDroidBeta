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

#include <boot/boot.h>
#include <boot/gpio.h>
#include <boot/gpio_keypad.h>

int gpio_keypad_init(gpio_keypad_info *keypad)
{
	unsigned i;
	for(i = 0; i < keypad->noutputs; i++) {
		gpio_set(keypad->output_gpios[i], keypad->polarity ^ keypad->drive_inactive_outputs);
		gpio_dir(keypad->output_gpios[i], keypad->drive_inactive_outputs);
	}
	for(i = 0; i < keypad->ninputs; i++) {
		gpio_dir(keypad->input_gpios[i], 0);
	}
	keypad->state = 0;
	return 0;
}

void gpio_keypad_scan_keys(gpio_keypad_info *keypad)
{
	unsigned out, in;
	unsigned long long keys;
	unsigned npolarity = !keypad->polarity;
	unsigned int shift;

	keys = 0;
	out = keypad->noutputs;
	shift = keypad->noutputs * keypad->ninputs;
	while(out > 0) {
		out--;
		if(keypad->drive_inactive_outputs)
			gpio_set(keypad->output_gpios[out], !npolarity);
		else	
			gpio_dir(keypad->output_gpios[out], 1);
		udelay(keypad->settle_time);
		in = keypad->ninputs;
		while(in > 0) {
			in--;
			shift--;
			keys = (keys << 1) | (gpio_get(keypad->input_gpios[in]) ^ npolarity);
			if(((unsigned)(keypad->state >> shift) ^ (unsigned)keys) & 1) {
				unsigned int mapped_key = 0;
				if(keypad->key_map)
					mapped_key = keypad->key_map[shift];
				//dprintf("gpio_keypad_scan_keys: %d-%d (%d-%d) %d (%d): %d\n", out, in,
				//        keypad->output_gpios[out], keypad->input_gpios[in],
				//        shift, mapped_key, keys & 1);
				if(mapped_key && key_changed)
					key_changed(mapped_key, keys & 1);
			}
		}
		if(keypad->drive_inactive_outputs)
			gpio_set(keypad->output_gpios[out], npolarity);
		else	
			gpio_dir(keypad->output_gpios[out], 0);
	}
	if(keys != keypad->state) {
		keypad->state = keys;
		//dprintf("gpio_keypad_scan_keys: %x %x\n", (unsigned long)(keys >> 32), (unsigned long)keys);
	}
}

