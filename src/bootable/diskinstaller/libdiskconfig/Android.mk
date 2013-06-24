LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

commonSources := \
	diskconfig.c \
	diskutils.c \
	write_lst.c \
	config_mbr.c

ifneq ($(TARGET_SIMULATOR),true)
ifeq ($(TARGET_ARCH),x86)

###########################
# static library for host
LOCAL_SRC_FILES := $(commonSources)

LOCAL_CFLAGS := -O2 -g -W -Wall -Werror -D_LARGEFILE64_SOURCE

LOCAL_MODULE := libdiskconfig_host
LOCAL_STATIC_LIBRARIES := libcutils
include $(BUILD_HOST_STATIC_LIBRARY)

## Build a test executable for host (to dump configuration files).
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(commonSources)
LOCAL_SRC_FILES += dump_diskconfig.c
LOCAL_MODULE := dump_diskconfig
LOCAL_STATIC_LIBRARIES := libdiskconfig_host libcutils
include $(BUILD_HOST_EXECUTABLE)

###########################
# shared library for target
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(commonSources)

LOCAL_CFLAGS := -O2 -g -W -Wall -Werror

LOCAL_MODULE := libdiskconfig
LOCAL_MODULE_TAGS := system_builder
LOCAL_SYSTEM_SHARED_LIBRARIES := libcutils liblog libc

include $(BUILD_SHARED_LIBRARY)

endif  # ! TARGET_SIMULATOR
endif  # TARGET_ARCH == x86
