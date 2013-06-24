CLI_STATIC_LIB ?= y
CLI_DEBUG ?= y
FIRMWARE_DYNAMIC_LOAD ?= y
BUILD_SUPPL ?= n
SG  ?= n

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

TARGET = wlan_loader
LOCAL_MODULE = $(TARGET)

ifeq ($(CLI_DEBUG),y)
  CLI_DEBUGFLAGS = -O0 -g -fno-builtin -DDEBUG -D TI_DBG  # "-O" is needed to expand inlines
else
  CLI_DEBUGFLAGS = -O2
endif

CLI_STA_DK_ROOT = $(LOCAL_PATH)/../..
CLI_DK_ROOT = $(CLI_STA_DK_ROOT)
CLI_COMMON  = $(CLI_DK_ROOT)/common
CLI_COMSRC  = $(CLI_COMMON)/src
CLI_TESTSRC = $(CLI_DK_ROOT)/Test
CLI_CUDK_ROOT = $(LOCAL_PATH)/..
CLI_IPC_SRC = $(CLI_CUDK_ROOT)/IPC/Linux

DK_INCS = $(CLI_COMMON)/inc \
	$(CLI_COMSRC)/inc \
	$(CLI_COMSRC)/utils \
	$(CLI_COMSRC)/hal/inc \
	$(CLI_COMSRC)/hal/hl_data \
	$(CLI_COMSRC)/hal/hl_ctrl \
	$(CLI_COMSRC)/hal/hw_data \
	$(CLI_COMSRC)/hal/hw_ctrl \
	$(CLI_COMSRC)/hal/security \
	$(CLI_COMSRC)/core/inc \
	$(CLI_COMSRC)/core/data_ctrl/Tx \
	$(CLI_COMSRC)/core/data_ctrl/Ctrl \
	$(CLI_COMSRC)/core/data_ctrl/Ctrl/4X \
	$(CLI_COMSRC)/core/sme/Inc \
	$(CLI_COMSRC)/core/sme/siteMgr \
	$(CLI_COMSRC)/core/sme/configMgr \
	$(CLI_COMSRC)/core/sme/conn \
	$(CLI_COMSRC)/core/rsn \
	$(CLI_COMSRC)/core/rsn/mainKeysSm \
	$(CLI_COMSRC)/core/rsn/mainKeysSm/keyDerive \
	$(CLI_COMSRC)/core/rsn/inc \
	$(CLI_COMSRC)/core/mlme \
	$(CLI_COMSRC)/core/NetworkCtrl/inc \
	$(CLI_COMSRC)/core/NetworkCtrl/Measurement \
	$(CLI_COMSRC)/core/NetworkCtrl/RegulatoryDomain \
	$(CLI_COMSRC)/core/NetworkCtrl/QOS \
	$(CLI_CUDK_ROOT)/CLI \
	$(CLI_CUDK_ROOT)/UtilityAdapter \
	$(CLI_COMSRC)/hal/FirmwareApi \
	$(CLI_COMSRC)/hal/TnetwServices \
	$(CLI_COMSRC)/hal/TnetwServices/TNETW1251

ifeq ($(SG), y)
DK_INCS += $(CLI_COMSRC)/core/NetworkCtrl/BThWlanCombo
endif

DK_DEFINES = \
	-D __BYTE_ORDER_LITTLE_ENDIAN \
	-D INCLUDE_DEFRAGMENTATION \
	-D CONFIGURE_BSS_TYPE_STA \
	-D TNETW1150=1 \
	-D DOT11_A_G=1 \
	-D ELP_NO_PDA_SCREEN_VIBRATE

ifeq ($(SG), y)
DK_DEFINES += -D BTH_COEXISTENCE
endif

LOCAL_SRC_FILES := tiwlan_loader.c

ifeq ($(CLI_STATIC_LIB),y)
LOCAL_STATIC_LIBRARIES := libWifiApi
else
LOCAL_SHARED_LIBRARIES := libWifiApi
endif
LOCAL_SHARED_LIBRARIES += libcutils liblog libc libhardware_legacy

INCLUDES = $(DK_INCS) $(CLI_STA_DK_ROOT)/pform/linux/inc \
	$(CLI_CUDK_ROOT)/Inc                             \
	$(CLI_STA_DK_ROOT)/pform/common/inc

LOCAL_CFLAGS = -Wall -Wstrict-prototypes $(CLI_DEBUGFLAGS) -D__LINUX__ $(DK_DEFINES) -mabi=aapcs-linux
LOCAL_CFLAGS += -DDRV_NAME='"tiwlan"' -DHOST_COMPILE

ifeq ($(FIRMWARE_DYNAMIC_LOAD), y)
    LOCAL_CFLAGS += -DFIRMWARE_DYNAMIC_LOAD
endif

LOCAL_C_INCLUDES = $(INCLUDES)

include $(BUILD_EXECUTABLE)
