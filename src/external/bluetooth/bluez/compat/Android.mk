BUILD_PAND := true
ifeq ($(BUILD_PAND),true)

LOCAL_PATH:= $(call my-dir)

#
# pand
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	pand.c bnep.c sdp.c

LOCAL_CFLAGS:= \
	-DVERSION=\"4.69\" -DSTORAGEDIR=\"/data/misc/bluetoothd\" -DNEED_PPOLL -D__ANDROID__

LOCAL_C_INCLUDES:=\
	$(LOCAL_PATH)/../lib \
	$(LOCAL_PATH)/../src \

LOCAL_SHARED_LIBRARIES := \
	libbluetoothd \
	libbluetooth \
	libcutils

LOCAL_MODULE_TAGS :=
LOCAL_MODULE:=pand

include $(BUILD_EXECUTABLE)
endif
