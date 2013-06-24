#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

# This makefile is only included if BOARD_WLAN_TI_STA_DK_ROOT is set,
# and if we're not building for the simulator.
ifndef BOARD_WLAN_TI_STA_DK_ROOT
  $(error BOARD_WLAN_TI_STA_DK_ROOT must be defined when including this makefile)
endif
ifeq ($(TARGET_SIMULATOR),true)
  $(error This makefile must not be included when building the simulator)
endif

ifndef WPA_SUPPLICANT_VERSION
WPA_SUPPLICANT_VERSION := VER_0_5_X
endif
ifneq ($(WPA_SUPPLICANT_VERSION),VER_0_5_X)
  $(error This wlan can be used only with 0.5.X version of the wpa_supplicant)
endif

DK_ROOT = $(BOARD_WLAN_TI_STA_DK_ROOT)
OS_ROOT = $(BOARD_WLAN_TI_STA_DK_ROOT)/pform
COMMON  = $(DK_ROOT)/common
COMSRC  = $(COMMON)/src
CUDK_ROOT = $(DK_ROOT)/CUDK
LIB	= ../../lib

include external/wpa_supplicant/.config

# To force sizeof(enum) = 4
ifneq ($(TARGET_SIMULATOR),true)
L_CFLAGS += -mabi=aapcs-linux
endif

INCLUDES = $(COMMON)/inc \
	$(COMSRC)/inc \
	$(COMSRC)/utils \
	$(COMSRC)/hal/inc \
	$(COMSRC)/hal/TnetwServices  \
	$(COMSRC)/hal/TnetwServices/TNETW1150  \
	$(COMSRC)/hal/FirmwareApi \
	$(COMSRC)/hal/hl_data \
	$(COMSRC)/hal/hl_ctrl \
	$(COMSRC)/hal/hl_ctrl/PowerCtrl \
	$(COMSRC)/hal/hw_data \
	$(COMSRC)/hal/hw_ctrl \
	$(COMSRC)/hal/security \
	$(COMSRC)/hal/security/CCX \
	$(COMSRC)/hal/security/Privacy \
	$(COMSRC)/hal/security/Privacy/Enc/Ckip \
	$(COMSRC)/hal/security/Privacy/Enc/KP \
	$(COMSRC)/hal/security/Privacy/Enc/KP/Ckip \
	$(COMSRC)/hal/security/Privacy/ICV \
	$(COMSRC)/hal/security/Privacy/IV \
	$(COMSRC)/hal/security/Privacy/IV/WEP \
	$(COMSRC)/hal/security/Privacy/MIC \
	$(COMSRC)/hal/security/Privacy/MIC/Michael \
	$(COMSRC)/hal/security/Privacy/MIC/MMH \
	$(COMSRC)/hal/security/Privacy/Seq \
	$(COMSRC)/hal/security/Privacy/Seq/CCX \
	$(COMSRC)/hal/Export_Inc \
	$(COMSRC)/BusAccess/Shm_Common \
	$(COMSRC)/BusAccess/Export_Inc \
	$(COMSRC)/BusAccess/Shm_Slave \
	$(COMSRC)/core/inc \
	$(COMSRC)/core/ExpInc \
	$(COMSRC)/core/data_ctrl/Tx \
	$(COMSRC)/core/data_ctrl/Ctrl \
	$(COMSRC)/core/data_ctrl/Ctrl/TrafficMonitor \
	$(COMSRC)/core/data_ctrl/Ctrl/4X \
	$(COMSRC)/core/data_ctrl/Rx \
	$(COMSRC)/core/EvHandler \
	$(COMSRC)/core/sme/Inc \
	$(COMSRC)/core/sme/smeSm/ \
	$(COMSRC)/core/sme/siteMgr \
	$(COMSRC)/core/sme/configMgr \
	$(COMSRC)/core/sme/conn \
	$(COMSRC)/core/sme/HealthMonitor \
	$(COMSRC)/core/srv/inc \
	$(COMSRC)/core/srv/scanSrv \
	$(COMSRC)/core/srv/scr \
	$(COMSRC)/core/rsn \
	$(COMSRC)/core/rsn/mainKeysSm \
	$(COMSRC)/core/rsn/mainKeysSm/keyDerive \
	$(COMSRC)/core/rsn/adm_ctrl/WPA \
	$(COMSRC)/core/rsn/adm_ctrl/WPA2 \
	$(COMSRC)/core/rsn/adm_ctrl/WEP/CCX \
	$(COMSRC)/core/rsn/inc \
	$(COMSRC)/core/rsn/algorithms \
	$(COMSRC)/core/mlme \
	$(COMSRC)/core/mlme/Assoc \
	$(COMSRC)/core/mlme/Auth/open \
	$(COMSRC)/core/mlme/Auth/shared \
	$(COMSRC)/Management/QOS/Inc/ \
	$(COMSRC)/Management/CCX/Inc/ \
	$(COMSRC)/Management/Roaming/Inc/ \
	$(COMSRC)/core/NetworkCtrl/inc \
	$(COMSRC)/Management/AirLink/Measurement \
	$(COMSRC)/Management/AirLink/Measurement/CCX \
	$(COMSRC)/Management/AirLink/Measurement/dot11h \
	$(COMSRC)/Management/AirLink/inc \
	$(COMSRC)/Management/scan/inc \
	$(COMSRC)/Management/scan/scanCncn \
	$(COMSRC)/core/NetworkCtrl/RegulatoryDomain \
	$(COMSRC)/core/NetworkCtrl/RegulatoryDomain/CCX \
	$(COMSRC)/Management/PowerMgr/ \
	$(COMSRC)/core/NetworkCtrl/QOS \
	$(COMSRC)/Application/inc \
	$(COMSRC)/Application/\ExpInc \
	$(COMSRC)/Application/ScanMngr \
	$(COMSRC)/Management/apConn \
	$(COMSRC)/core/currBss \
	$(COMSRC)/Management/scan/inc \
	$(CUDK_ROOT)/Inc \
	$(OS_ROOT)/common/inc \
	$(OS_ROOT)/linux/inc \
	$(OS_ROOT)/linux/src \
	$(COMSRC)/core/EvHandler \
	$(CUDK_ROOT)/IPC/Linux \
	$(CUDK_ROOT)/UtilityAdapter \
	external/openssl/include \
	external/wpa_supplicant \
	$(DK_ROOT)/../lib
  
L_CFLAGS += -DCONFIG_DRIVER_CUSTOM -DHOST_COMPILE
L_CFLAGS += -DWPA_SUPPLICANT_VER=$(WPA_SUPPLICANT_VERSION)
ifeq ($(notdir $(BOARD_WLAN_TI_STA_DK_ROOT)),sta_dk_5_0_0_94)
L_CFLAGS += -DSTA_DK_VER_5_0_0_94 
endif
OBJS = driver_ti.c $(LIB)/scanmerge.c $(LIB)/shlist.c

ifdef CONFIG_NO_STDOUT_DEBUG
L_CFLAGS += -DCONFIG_NO_STDOUT_DEBUG
endif

ifdef CONFIG_DEBUG_FILE
L_CFLAGS += -DCONFIG_DEBUG_FILE
endif

ifdef CONFIG_IEEE8021X_EAPOL
L_CFLAGS += -DIEEE8021X_EAPOL
endif

########################
 
include $(CLEAR_VARS)
LOCAL_MODULE := libCustomWifi
LOCAL_STATIC_LIBRARIES := libWifiApi
LOCAL_SHARED_LIBRARIES := libc libcutils
LOCAL_CFLAGS := $(L_CFLAGS)
LOCAL_SRC_FILES := $(OBJS)
LOCAL_C_INCLUDES := $(INCLUDES)
include $(BUILD_STATIC_LIBRARY)

########################
