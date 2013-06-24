CLI_DEBUG ?= y
CLI_STATIC_LIB ?= y
SG ?= n

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE = libWifiApi

ifeq ($(CLI_DEBUG),y)
  CLI_DEBUGFLAGS = -O0 -g -fno-builtin -DDEBUG -D TI_DBG  # "-O" is needed to expand inlines
else
  CLI_DEBUGFLAGS = -O2
endif

CLI_STA_DK_ROOT = $(LOCAL_PATH)/../..
CLI_CUDK_ROOT = $(LOCAL_PATH)/..


LOCAL_SRC_FILES := \
	../OAL/Common/TI_OAL.cpp \
	../OAL/Pform/Linux/TILibLinux.cpp \
	TI_AdapterApi.cpp \
	TI_Adapter.cpp \
	CTI_Adapter.cpp \
	../IPC/Linux/ipc_event.c \
	../IPC/Linux/cu_ipc.c \
	../IPC/Linux/ipc_user.c

ifeq ($(SG), y)
LOCAL_CFLAGS += -D BTH_COEXISTENCE
endif

INCLUDEDIR = $(CLI_CUDK_ROOT)/Inc \
	$(CLI_CUDK_ROOT)/OAL/Common \
	$(CLI_CUDK_ROOT)/UtilityAdapter \
	$(CLI_STA_DK_ROOT)/common/inc \
	$(CLI_STA_DK_ROOT)/pform/linux/inc \
	$(CLI_STA_DK_ROOT)/pform/common/inc \
	$(CLI_CUDK_ROOT)/IPC/Linux \
	$(CLI_STA_DK_ROOT)/common/src/hal/FirmwareApi \
	$(CLI_CUDK_ROOT)/CLI

LOCAL_CFLAGS += -Wall -D__LINUX__ $(CLI_DEBUGFLAGS) -mabi=aapcs-linux -DHOST_COMPILE

LOCAL_C_INCLUDES := $(INCLUDEDIR)
LOCAL_MODULE_TAGS := tests

ifeq ($(CLI_STATIC_LIB),y)
include $(BUILD_STATIC_LIBRARY)
else
include $(BUILD_SHARED_LIBRARY)
endif
