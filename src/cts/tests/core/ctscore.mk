# Copyright (C) 2009 The Android Open Source Project
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

# don't include these packages in any target
LOCAL_MODULE_TAGS := optional
# and when built explicitly put them in the data partition
LOCAL_MODULE_PATH := $(TARGET_OUT_DATA_APPS)

LOCAL_JAVA_LIBRARIES := android.test.runner bouncycastle

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

CORETESTS_INTERMEDIATES := $(call intermediates-dir-for,JAVA_LIBRARIES,core-tests,,COMMON)

PACKAGE_INTERMEDIATES := $(intermediates)
PACKAGE_RESOURCES := $(PACKAGE_INTERMEDIATES)/package_res.apk

$(LOCAL_INSTALLED_MODULE): $(PACKAGE_RESOURCES)

$(PACKAGE_RESOURCES): PRIVATE_INTERMEDIATES_COMMON := $(intermediates.COMMON)
$(PACKAGE_RESOURCES): PRIVATE_CORETESTS_INTERMEDIATES_COMMON := $(CORETESTS_INTERMEDIATES)
$(PACKAGE_RESOURCES): PRIVATE_INTERMEDIATES := $(intermediates)
$(PACKAGE_RESOURCES): PRIVATE_MODULE_STEM := $(LOCAL_BUILT_MODULE_STEM)
$(PACKAGE_RESOURCES): PRIVATE_RESOURCE_DIRS := $(call all-core-resource-dirs,test)
$(PACKAGE_RESOURCES): PRIVATE_LOCAL_PATH := $(LOCAL_PATH)
$(PACKAGE_RESOURCES): PRIVATE_PRIVATE_KEY := $(private_key)
$(PACKAGE_RESOURCES): PRIVATE_CERTIFICATE := $(certificate)
$(PACKAGE_RESOURCES): $(LOCAL_BUILT_MODULE) $(CORETESTS_INTERMEDIATES)/javalib.jar
	@echo "Add resources to package ($@)"
	@rm -rf $(PRIVATE_INTERMEDIATES_COMMON)/ctsclasses
# javalib.jar should only contain .dex files, but the harmony tests also include
# some .class files, so get rid of them
	$(call unzip-jar-files,$(PRIVATE_CORETESTS_INTERMEDIATES_COMMON)/javalib.jar,\
		$(PRIVATE_INTERMEDIATES_COMMON)/ctsclasses)
	@find $(PRIVATE_INTERMEDIATES_COMMON)/ctsclasses -type f -name "*.class" -delete
	@rm -f $(PRIVATE_INTERMEDIATES_COMMON)/ctsclasses/classes.dex
	@cp $< $@
	@jar uf $@ -C $(PRIVATE_INTERMEDIATES_COMMON)/ctsclasses .
	$(sign-package)
	$(align-package)
	$(hide) cp $@ $<
