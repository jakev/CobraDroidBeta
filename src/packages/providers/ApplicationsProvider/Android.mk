LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES := ext

LOCAL_PACKAGE_NAME := ApplicationsProvider
LOCAL_CERTIFICATE := shared

include $(BUILD_PACKAGE)
