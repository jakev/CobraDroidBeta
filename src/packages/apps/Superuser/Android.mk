LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := Superuser

PRODUCT_COPY_FILES += $(LOCAL_PATH)/prebuilt/Superuser.apk:system/app/Superuser.apk

#include $(BUILD_PACKAGE)
