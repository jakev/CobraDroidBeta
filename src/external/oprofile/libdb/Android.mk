LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	db_debug.c \
	db_insert.c \
	db_manage.c \
	db_stat.c \
	db_travel.c

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/.. \
	$(LOCAL_PATH)/../libutil

LOCAL_MODULE := libdb

include $(BUILD_STATIC_LIBRARY)
