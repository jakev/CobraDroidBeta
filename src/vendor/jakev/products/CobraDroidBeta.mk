# Inherit the full for emulator specific
#$(call inherit-product, $(SRC_TARGET_DIR)/product/full.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/generic.mk)
#
# Overrides
PRODUCT_NAME := CobraDroidBeta
PRODUCT_MODEL := CD001B
PRODUCT_DEVICE := goldfish
PRODUCT_MANUFACTURER := CobraDenSec
PRODUCT_LOCALES := en_US en_GB


PRODUCT_COPY_FILES += \
        system/core/rootdir/etc/vold.fstab:system/etc/vold.fstab \
	external/bootanim/bootanimation.zip:system/media/bootanimation.zip \
	external/bootanim/initlogo.rle:root/initlogo.rle

