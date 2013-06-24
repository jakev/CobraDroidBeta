LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := bootanim

PRODUCT_COPY_FILES += $(LOCAL_PATH)/bootanimation.zip:system/media/bootanimation.zip
