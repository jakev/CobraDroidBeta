LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	findme.c \
	popt.c \
	poptconfig.c \
	popthelp.c \
	poptparse.c

LOCAL_CFLAGS += -DHAVE_CONFIG_H

LOCAL_MODULE := libpopt

include $(BUILD_STATIC_LIBRARY)
