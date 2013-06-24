LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	init.c \
	opd_anon.c \
	opd_cookie.c \
	opd_events.c \
	opd_extended.c \
	opd_ibs.c \
	opd_ibs_trans.c \
	opd_kernel.c \
	opd_mangling.c \
	opd_perfmon.c \
	opd_pipe.c \
	opd_sfile.c \
	opd_spu.c \
	opd_stats.c \
	opd_trans.c \
	oprofiled.c

LOCAL_STATIC_LIBRARIES := \
	libpopt libutil libdb libabi libop

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/.. \
	$(LOCAL_PATH)/../libdb \
	$(LOCAL_PATH)/../libutil \
	$(LOCAL_PATH)/../libop \
	$(LOCAL_PATH)/../libabi

LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
LOCAL_MODULE_TAGS := debug
LOCAL_MODULE:= oprofiled

include $(BUILD_EXECUTABLE)
