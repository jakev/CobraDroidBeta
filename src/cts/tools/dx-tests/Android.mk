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

LOCAL_PATH := $(call my-dir)

# run-dx-tests host module
#===========================================================

include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE := dx-tests

LOCAL_JAVA_LIBRARIES := dx


include $(BUILD_SYSTEM)/base_rules.mk

ALL_SRC_FILES := $(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find src/dxc/junit -name "*.java" -o -name "*.j" -o -name "*.cfh"))
ABSOLUTE_SRC_FILES := $(addprefix $(LOCAL_PATH)/,$(ALL_SRC_FILES))

$(LOCAL_BUILT_MODULE): PRIVATE_CURRENT_MODULE_SCRIPT := $(LOCAL_PATH)/etc/compileall
$(LOCAL_BUILT_MODULE): PRIVATE_BASE := $(LOCAL_PATH)
$(LOCAL_BUILT_MODULE): PRIVATE_INTERMEDIATES := $(intermediates)
$(LOCAL_BUILT_MODULE): $(DX) $(TARGET_OUT_EXECUTABLES)/dalvikvm
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/starttests $(LOCAL_PATH)/etc/compileall ${ABSOLUTE_SRC_FILES} $(HOST_OUT_JAVA_LIBRARIES)/dx.jar $(HOST_OUT_JAVA_LIBRARIES)/cfassembler.jar | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)
	$(hide) chmod 755 $@
	@$(PRIVATE_CURRENT_MODULE_SCRIPT) "$(PRIVATE_BASE)" "$(HOST_JAVAC)" "$(PRIVATE_INTERMEDIATES)" "$(HOST_OUT_JAVA_LIBRARIES)/dx.jar:$(HOST_OUT_JAVA_LIBRARIES)/cfassembler.jar" "$(HOST_OUT)"

# cfassembler host module
#============================================================

include $(CLEAR_VARS)

LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE := cfassembler

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/cfassembler$(COMMON_JAVA_PACKAGE_SUFFIX)
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/cfassembler | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)
	$(hide) chmod 755 $@

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

# cfassembler java library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := src/dxconvext/ClassFileAssembler.java src/dxconvext/util/FileUtils.java
LOCAL_JAR_MANIFEST := etc/cfassembler_manifest.txt

LOCAL_MODULE:= cfassembler

include $(BUILD_HOST_JAVA_LIBRARY)

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)
