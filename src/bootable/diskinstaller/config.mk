# note: requires x86 because we assume grub is the mbr bootloader.
ifeq ($(TARGET_ARCH),x86)
ifeq ($(TARGET_USE_DISKINSTALLER),true)

diskinstaller_root := bootable/diskinstaller

android_sysbase_modules := \
	libc \
	libcutils \
	libdl \
	liblog \
	libm \
	libstdc++ \
	linker \
	sh \
	toolbox \
	logcat \
	gdbserver \
	strace \
	netcfg
android_sysbase_files = \
	$(call module-installed-files,$(android_sysbase_modules))

# $(1): source base dir
# $(2): target base dir
define sysbase-copy-files
$(hide) $(foreach _f,$(android_sysbase_files), \
	f=$(patsubst $(1)/%,$(2)/%,$(_f)); \
	mkdir -p `dirname $$f`; \
	echo "Copy: $$f" ; \
	cp -fR $(_f) $$f; \
)
endef

installer_base_modules := \
	libdiskconfig \
	libext2fs \
	libext2_com_err \
	libext2_e2p \
	libext2_blkid \
	libext2_uuid \
	libext2_profile \
	badblocks \
	resize2fs \
	tune2fs \
	mke2fs \
	e2fsck
installer_base_files = \
	$(call module-built-files,$(installer_base_modules))

# $(1): source base dir
# $(2): target base dir
define installer-copy-modules
$(hide) $(foreach m,$(installer_base_modules), \
	src=$(firstword $(strip $(call module-built-files,$(m)))); \
	dest=$(patsubst $(strip $(1))/%,$(strip $(2))/%,\
		$(firstword $(strip $(call module-installed-files,$(m))))); \
	echo "Copy: $$src -> $$dest"; \
	mkdir -p `dirname $$dest`; \
	cp -fdp $$src $$dest; \
)
endef

# Build the installer ramdisk image
installer_initrc := $(diskinstaller_root)/init.rc
installer_kernel := $(INSTALLED_KERNEL_TARGET)
installer_ramdisk := $(TARGET_INSTALLER_OUT)/ramdisk-installer.img
installer_build_prop := $(INSTALLED_BUILD_PROP_TARGET)
installer_config := $(diskinstaller_root)/installer.conf
installer_binary := \
	$(call intermediates-dir-for,EXECUTABLES,diskinstaller)/diskinstaller

$(installer_ramdisk): $(diskinstaller_root)/config.mk \
		$(MKBOOTFS) \
		$(INSTALLED_RAMDISK_TARGET) \
		$(INSTALLED_BOOTIMAGE_TARGET) \
		$(TARGET_DISK_LAYOUT_CONFIG) \
		$(installer_binary) \
		$(installer_initrc) \
		$(installer_kernel) \
		$(installer_config) \
		$(android_sysbase_files) \
		$(installer_base_files) \
		$(installer_build_prop)
	@echo ----- Making installer image ------
	rm -rf $(TARGET_INSTALLER_OUT)
	mkdir -p $(TARGET_INSTALLER_OUT)
	mkdir -p $(TARGET_INSTALLER_ROOT_OUT)
	mkdir -p $(TARGET_INSTALLER_ROOT_OUT)/sbin
	mkdir -p $(TARGET_INSTALLER_ROOT_OUT)/data
	mkdir -p $(TARGET_INSTALLER_SYSTEM_OUT)
	mkdir -p $(TARGET_INSTALLER_SYSTEM_OUT)/etc
	mkdir -p $(TARGET_INSTALLER_SYSTEM_OUT)/bin
	@echo Copying baseline ramdisk...
	cp -fR $(TARGET_ROOT_OUT) $(TARGET_INSTALLER_OUT)
	@echo Copying sysbase files...
	$(call sysbase-copy-files,$(TARGET_OUT),$(TARGET_INSTALLER_SYSTEM_OUT))
	@echo Copying installer base files...
	$(call installer-copy-modules,$(TARGET_OUT),\
		$(TARGET_INSTALLER_SYSTEM_OUT))
	@echo Modifying ramdisk contents...
	cp -f $(installer_initrc) $(TARGET_INSTALLER_ROOT_OUT)/
	cp -f $(TARGET_DISK_LAYOUT_CONFIG) \
		$(TARGET_INSTALLER_SYSTEM_OUT)/etc/disk_layout.conf
	cp -f $(installer_config) \
		$(TARGET_INSTALLER_SYSTEM_OUT)/etc/installer.conf
	cp -f $(installer_binary) $(TARGET_INSTALLER_SYSTEM_OUT)/bin/installer
	$(hide) chmod ug+rw $(TARGET_INSTALLER_ROOT_OUT)/default.prop
	cat $(installer_build_prop) >> $(TARGET_INSTALLER_ROOT_OUT)/default.prop
	$(MKBOOTFS) $(TARGET_INSTALLER_ROOT_OUT) | gzip > $(installer_ramdisk)
	@echo ----- Made installer ramdisk -[ $@ ]-

######################################################################
# Now the installer boot image which includes the kernel and the ramdisk
internal_installerimage_args := \
	--kernel $(installer_kernel) \
	--ramdisk $(installer_ramdisk)

internal_installerimage_files := \
	$(filter-out --%,$(internal_installerimage_args))

BOARD_INSTALLER_CMDLINE := $(strip $(BOARD_INSTALLER_CMDLINE))
ifdef BOARD_INSTALLER_CMDLINE
  internal_installerimage_args += --cmdline "$(BOARD_INSTALLER_CMDLINE)"
endif

installer_tmp_img := $(TARGET_INSTALLER_OUT)/installer_tmp.img
tmp_dir_for_inst_image := \
	$(call intermediates-dir-for,EXECUTABLES,installer_img)/installer_img
internal_installerimage_args += --tmpdir $(tmp_dir_for_inst_image)
internal_installerimage_args += --genext2fs $(MKEXT2IMG)
$(installer_tmp_img): $(MKEXT2IMG) $(internal_installerimage_files)
	$(call pretty,"Target installer image: $@")
	$(hide) $(MKEXT2BOOTIMG) $(internal_installerimage_args) --output $@

######################################################################
# Now make a data image that contains all the target image files for the
# installer.
bootldr_bin := $(PRODUCT_OUT)/grub/grub.bin
installer_target_data_files := \
	$(INSTALLED_BOOTIMAGE_TARGET) \
	$(INSTALLED_SYSTEMIMAGE) \
	$(INSTALLED_USERDATAIMAGE_TARGET) \
	$(bootldr_bin)

installer_data_img := $(TARGET_INSTALLER_OUT)/installer_data.img
$(installer_data_img): $(diskinstaller_root)/config.mk \
			$(installer_target_data_files) \
			$(MKEXT2IMG) \
			$(installer_ramdisk)
	@echo --- Making installer data image ------
	mkdir -p $(TARGET_INSTALLER_OUT)
	mkdir -p $(TARGET_INSTALLER_OUT)/data
	cp -f $(bootldr_bin) $(TARGET_INSTALLER_OUT)/data/bootldr.bin
	cp -f $(INSTALLED_BOOTIMAGE_TARGET) $(TARGET_INSTALLER_OUT)/data/boot.img
	cp -f $(INSTALLED_SYSTEMIMAGE) \
		$(TARGET_INSTALLER_OUT)/data/system.img
	cp -f $(INSTALLED_USERDATAIMAGE_TARGET) \
		$(TARGET_INSTALLER_OUT)/data/userdata.img
	$(call build-userimage-ext2-target,$(TARGET_INSTALLER_OUT)/data,$@,\
		inst_data,)
	@echo --- Finished installer data image -[ $@ ]-

######################################################################
# now combine the installer image with the grub bootloader
grub_bin := $(PRODUCT_OUT)/grub/grub.bin
installer_layout := $(diskinstaller_root)/installer_img_layout.conf
edit_mbr := $(HOST_OUT_EXECUTABLES)/editdisklbl

INSTALLED_DISKINSTALLERIMAGE_TARGET := $(PRODUCT_OUT)/installer.img
$(INSTALLED_DISKINSTALLERIMAGE_TARGET): \
					$(installer_tmp_img) \
					$(installer_data_img) \
					$(grub_bin) \
					$(edit_mbr) \
					$(installer_layout)
	@echo "Creating bootable installer image: $@"
	@rm -f $@
	@cat $(grub_bin) > $@
	@$(edit_mbr) -l $(installer_layout) -i $@ \
		inst_boot=$(installer_tmp_img) \
		inst_data=$(installer_data_img)
	@echo "Done with bootable installer image -[ $@ ]-"


######################################################################
# now convert the installer_img (disk image) to a VirtualBox image

INSTALLED_VBOXINSTALLERIMAGE_TARGET := $(PRODUCT_OUT)/installer.vdi
virtual_box_manager := VBoxManage
virtual_box_manager_options := convertfromraw

$(INSTALLED_VBOXINSTALLERIMAGE_TARGET): $(INSTALLED_DISKINSTALLERIMAGE_TARGET)
	@rm -f $(INSTALLED_VBOXINSTALLERIMAGE_TARGET)
	@$(virtual_box_manager) $(virtual_box_manager_options) $(INSTALLED_DISKINSTALLERIMAGE_TARGET) $(INSTALLED_VBOXINSTALLERIMAGE_TARGET)
	@echo "Done with VirtualBox bootable installer image -[ $@ ]-"

else  # ! TARGET_USE_DISKINSTALLER
INSTALLED_DISKINSTALLERIMAGE_TARGET :=
INSTALLED_VBOXINSTALLERIMAGE_TARGET :=
endif
endif # TARGET_ARCH == x86

.PHONY: installer_img
installer_img: $(INSTALLED_DISKINSTALLERIMAGE_TARGET)

.PHONY: installer_vdi
installer_vdi: $(INSTALLED_VBOXINSTALLERIMAGE_TARGET)
