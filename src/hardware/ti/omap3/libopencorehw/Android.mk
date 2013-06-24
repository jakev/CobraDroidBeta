ifneq ($(BUILD_WITHOUT_PV),true)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# Set up the OpenCore variables.
include external/opencore/Config.mk
LOCAL_C_INCLUDES := \
    $(PV_INCLUDES) \
    hardware/ti/omap3/liboverlay

LOCAL_SRC_FILES := \
	android_surface_output_omap34xx.cpp buffer_alloc_omap34xx.cpp

LOCAL_CFLAGS := -Wno-non-virtual-dtor -DENABLE_SHAREDFD_PLAYBACK -D_LARGEFILE_SOURCE -D_LARGEFILE64_SOURCE -D_FILE_OFFSET_BITS=64 -DUSE_CML2_CONFIG -DPV_ARM_GCC_V5 -DHAS_OSCL_LIB_SUPPORT

LOCAL_SHARED_LIBRARIES := \
    libutils \
    libcutils \
    libui \
    libhardware\
    libandroid_runtime \
    libmedia \
    libopencore_common \
    libicuuc \
    libopencore_player

# do not prelink
LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libopencorehw

LOCAL_LDLIBS += 

include $(BUILD_SHARED_LIBRARY)

endif
