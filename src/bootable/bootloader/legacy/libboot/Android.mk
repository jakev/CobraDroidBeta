LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := \
	flash.c \
	poll.c \
	tags_partition.c \
	tags_revision.c \
	tags_serialno.c \
	tags_cmdline.c \
	gpio_keypad.c \
	init.c \
	tags.c

LOCAL_C_INCLUDES := $(call include-path-for, bootloader)

LOCAL_CFLAGS := -O2 -g -W -Wall
LOCAL_CFLAGS += -march=armv6

LOCAL_MODULE := libboot

include $(BUILD_RAW_STATIC_LIBRARY)
