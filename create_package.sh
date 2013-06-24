#!/bin/sh

error () {
	echo "[ERROR] $1"
	exit 1
}

debug () {
	if [ $verbose -eq 1 ]; then
	   echo "[+] $1"
	fi
}

usage () {
	echo "Usage: $0 options"
	echo ""
	echo "Run this script from the root of the CobraDroid repository!"
	echo "OPTIONS:"
	echo "\t-f\t\tForce package even if old package exists."
	echo "\t-h\t\tPrints this usage message."
	echo "\t-v\t\tVerbose output"
	echo ""
}

dir_exists () {
    stat $1 > /dev/null 2>&1 && echo 1 || echo 0
}

# Main

force_flag=0
verbose=0

while getopts "fhv" opt; do
  case $opt in
    v)
      verbose=1
      debug "Verbose output enabled"
      ;;
    f)
      force_flag=1
      debug "Force flag is enabled"
      ;;
    h)
      usage
      exit 1 
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

build_dir=`pwd`
src_images=${build_dir}/"src/out/target/product/goldfish"
kernel_images=${build_dir}/"kernel-src/arch/arm/boot"
packaging_stuff=${build_dir}/"packaging"
output_dir=${build_dir}/"packaged"

debug "Trying to create output directory ${output_dir}..."

if [ $(dir_exists ${output_dir}) -eq 1 ]; then
    debug "Output directory already exists!"
    if [ $force_flag -eq 1 ]; then
        debug "Force flag provided, continuing."
    else
        error "The output directory exists! You must delete it or provide the \"-f\" flag!"
    fi
fi

mkdir ${build_dir}/packaged 2>/dev/null

debug  "Copying required directory structure from ${packaging_stuff}..."

if [ $(dir_exists ${packaging_stuff}) -eq 0 ]; then
   error "The packaging directory does not exist. Stopping."
fi

cp -r $packaging_stuff/* ${build_dir}/packaged/ 2>/dev/null


debug "Copying system images..."
debug  "1. system.img.."
cp ${src_images}/system.img ${build_dir}/packaged/images/ 2>/dev/null|| error "\"system.img\" not found! Did you build the tree?"

debug "2. ramdisk.img.."
cp ${src_images}/ramdisk.img ${build_dir}/packaged/images/ 2>/dev/null|| error "\"ramdisk.img\" not found! Did you build the tree?"

debug "3. userdata.img.."
cp ${src_images}/userdata.img ${build_dir}/packaged/images/ 2>/dev/null|| error "\"userdata.img\" not found! Did you build the tree?"

debug "Copying kernel image..."
debug "1. zImage (as kernel-qemu).."
cp ${kernel_images}/zImage ${build_dir}/packaged/images/kernel-qemu 2>/dev/null|| error "\"zImage\" not found! Did you build the kernel?"

debug "Compressing to \"addon-cobradroid-beta.tar.bz2\"..."
tar cjf ${build_dir}/addon-cobradroid-beta.tar.bz2 packaged/* || error "Could not create compressed image."

debug "Complete!"
