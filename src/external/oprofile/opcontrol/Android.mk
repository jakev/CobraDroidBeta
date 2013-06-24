LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(ARCH_ARM_HAVE_ARMV7A), true)
    LOCAL_CFLAGS += -DWITH_ARM_V7_A
endif

LOCAL_SRC_FILES:= \
	opcontrol.cpp

LOCAL_STATIC_LIBRARIES := \
	libpopt libutil libdb libabi libop

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/.. \
	$(LOCAL_PATH)/../libop

LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_MODULE_TAGS := debug
LOCAL_MODULE:= opcontrol

include $(BUILD_EXECUTABLE)
