# Add a couple include paths to use stlport.

# Only use this on the device or emulator.
ifeq ($(TARGET_SIMULATOR),true)
$(error STLPort not suitable for the simulator! $(LOCAL_PATH))
endif

ifdef LOCAL_NDK_VERSION
stlport_NDK_VERSION_ROOT := $(HISTORICAL_NDK_VERSIONS_ROOT)/android-ndk-r$(LOCAL_NDK_VERSION)/platforms/android-$(LOCAL_SDK_VERSION)/arch-$(TARGET_ARCH)
LOCAL_C_INCLUDES := \
	$(stlport_NDK_VERSION_ROOT) \
	external/stlport/stlport \
	$(LOCAL_C_INCLUDES)

LOCAL_CFLAGS += -DBUILD_WITH_NDK=1
else
# Make sure bionic is first so we can include system headers.
LOCAL_C_INCLUDES := \
	bionic \
	external/stlport/stlport \
	$(LOCAL_C_INCLUDES)
endif
