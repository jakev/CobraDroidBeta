LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ifneq ($(TARGET_SIMULATOR),true)
ifeq ($(TARGET_ARCH),x86)

LOCAL_SRC_FILES := \
	editdisklbl.c

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../libdiskconfig

LOCAL_CFLAGS := -O2 -g -W -Wall -Werror

LOCAL_MODULE := editdisklbl
LOCAL_STATIC_LIBRARIES := libdiskconfig_host libcutils liblog

include $(BUILD_HOST_EXECUTABLE)

endif   # TARGET_ARCH == x86
endif   # !TARGET_SIMULATOR
