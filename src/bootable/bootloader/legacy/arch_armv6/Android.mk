LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := irq.S dcc.S jtag.S misc.S

LOCAL_C_INCLUDES := $(call include-path-for, bootloader)

LOCAL_CFLAGS := -O2 -g -W -Wall
LOCAL_CFLAGS += -march=armv6

LOCAL_MODULE := libboot_arch_armv6

include $(BUILD_RAW_STATIC_LIBRARY)