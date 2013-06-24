LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	op_cpufreq.c \
	op_deviceio.c \
	op_file.c \
	op_fileio.c \
	op_get_time.c \
	op_growable_buffer.c \
	op_libiberty.c \
	op_lockfile.c \
	op_popt.c \
	op_string.c \
	op_version.c

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/..

LOCAL_MODULE := libutil

include $(BUILD_STATIC_LIBRARY)
