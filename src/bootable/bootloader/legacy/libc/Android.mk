LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := \
	dprintf.c \
	xprintf.c \
	cprintf.c \
	sprintf.c \
	malloc.c \
	memcmp.c \
	memcpy.c \
	memset.c \
	strcpy.c \
	strlen.c \
	strcmp.c \
	strstr.c \
	crypto.c \
	rsa.c \
	sha.c

LOCAL_C_INCLUDES := $(call include-path-for, bootloader)
LOCAL_C_INCLUDES += include/mincrypt

LOCAL_CFLAGS := -O2 -g -W -Wall
LOCAL_CFLAGS += -march=armv6

LOCAL_MODULE := libboot_c

include $(BUILD_RAW_STATIC_LIBRARY)
