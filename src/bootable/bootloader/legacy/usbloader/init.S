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

.global irq_glue
.global irq_vector_table

#include <boot/arm.h>

v_reset:
	b start
v_undefined:
	b .
v_swi:
	b .
v_prefetch_abt:
	b .
v_data_abt:
	b .
v_reserved:		
	b .
v_irq:
	b .
v_fiq:	
	b .

start:
	ldr r5, =0xfffff000
	ands r4, pc, r5
	beq already_at_zero

	/* we're not loaded at 0 -- relocate us back down to where we belong */
	mov r5, #0
	ldr r6, =BOOTLOADER_END
1:	ldr r7, [r4], #4
	str r7, [r5], #4
	cmp r5, r6
	bne 1b
		
	mov pc, #0

already_at_zero:		
	/* save registers for main() */
	mov r7, r0
	mov r8, r1
	mov r9, r2
	mov r10, r3

	/* init stack */
	ldr r0, =BOOTLOADER_STACK
	msr cpsr_c, #(PSR_I | PSR_F | PSR_SVC)
	mov sp, r0

	/* zero the BSS */
	ldr r1, =BOOTLOADER_BSS
	ldr r2, =BOOTLOADER_END
	mov r0, #0
1:	str r0, [r1], #4
	cmp r1, r2
	ble 1b
		
	bl periph_2gb_open

	/* restore registers for main() */
	mov r0, r7
	mov r1, r8
	mov r2, r9
	mov r3, r10
		
	ldr r4, =_main
	blx r4
	b .
