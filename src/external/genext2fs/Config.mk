# $(1): src directory
# $(2): output file
# $(3): label (if any)
# $(4): if true, add journal
define build-userimage-ext2-target
	@mkdir -p $(dir $(2))
	$(hide) num_blocks=`du -sk $(1) | tail -n1 | awk '{print $$1;}'`;\
	if [ $$num_blocks -lt 20480 ]; then extra_blocks=3072; \
	else extra_blocks=20480; fi ; \
	num_blocks=`expr $$num_blocks + $$extra_blocks` ; \
	num_inodes=`find $(1) | wc -l` ; num_inodes=`expr $$num_inodes + 500`; \
	$(MKEXT2IMG) -a -d $(1) -b $$num_blocks -N $$num_inodes -m 0 $(2)
	$(if $(strip $(3)),\
		$(hide) $(TUNE2FS) -L $(strip $(3)) $(2))
	$(if $(strip $(4)),\
		$(hide) $(TUNE2FS) -j $(2))
	$(TUNE2FS) -C 1 $(2)
	$(E2FSCK) -fy $(2) ; [ $$? -lt 4 ]
endef
