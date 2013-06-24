CobraDroid 1.0 Beta
===================

About
-----
CobraDroid is a custom build of the Android operating system geared specifically for application security analysts and for individuals dealing with mobile malware. This release is a modified build of Android 2.3.7_r1 for the Android emulator, but future builds will include support for developer phones (such as the Google Nexus series), and newer iterations of the Android OS. It includes functionality and packages not included with vanilla Android images. Some features are:

-Configurable radio values (MIED, MDN, IMSI, SIM card serial number, voicemail number)
-Dynamically configurable "build.prop" values
-Configurable SSL certificate validation bypassing 
-Enhanced proxy capabilities
-Additional user-space utilities

Prerequisites
-------------
**For a full walkthrough on building CobraDroid from source, check out the page http://www.thecobraden.com/projects/cobradroid/dev.**

This reposity consists of a few directories: 
-src - The CobraDroid source tree
-kernel-src - The CobraDroid kernel
-packaging - Additional required files to intergrate with Android SDK

At this time, each of these projects must be built seperately.  I include an optional script to help package all of your output files together to use with your emulator.  I recommend first reading the AOSP pages on how to configure a building environment (https://source.android.com).  Also, to cut down on size, you will need to obtain your own toolchain for both the tree and kernel (the "/prebuilt/" directory of a fresh tree download).  I recommend downloading the 2.3.7_r1 branch of the AOSP, and using the "/prebuilt/" directory that is included.  In fact, you can use this same toolchain to compile your kernel (more on that below).

Building the Kernel
-------------------
Building the kernel is quite easy.  I've already included my kernel ".config" file so all you really need to do is set up your toolchain, and compile.  This example assumes you used the "/prebuilt/" toolchain method from above.

    root@building# cd CobraDroidBeta/kernel-src
    root@building# export PATH=../src/prebuilt/linux-x86/toolchain/arm-eabi-4.4.3/bin:$PATH
    root@building# export ARCH=arm
    root@building# export SUBARCH=arm
    root@building# export CROSS_COMPILE=arm-eabi-
    root@building# make

If you plan on using my create_package.sh script, you don't need to bother grabbing the output files yet.  If you just want the kernel, its in the directory "/kernel-src/arch/arm/boot/" and is called "zImage".

Building the Tree
-----------------
Building the tree is also easy.  Make sure you have the toolchain and perform the following commmands:

    root@building# cd CobraDroidBeta/src
    root@building# . build/envsetup.sh
    root@building# lunch CobraDroidBeta-eng
    root@building# make

**Note: You can append the "-jN" argument, where N is the number of threads, to your make command to speed things up if you have a beefy system.

create_package.sh
-----------------
If you want to make changes to the source and create your own "addon" to the Android SDK (this is how CobraDroid interfaces with the AVD Manager), you can use the "create_package.sh" script I've included in the root directory of the repo.  This will take your kernel image, tree images, and other required files, and bundle them together into a Unix tarball that you then install the exact same as if you downloaded the CobraDroid package.

After successfully compiling the kernel and tree, simple run the script.  Your output file will be in the current working directory.

    root@building# sh create_package.sh -v

Licenses
--------
Licenses are hard, and I'm not a lawyer.  If you feel that I've made a terrible mistake or used your code incorrectly, please don't hesitate to ask me to remove your code or change the way I doing something.  All code written by myself is released under the Apache License Version 2.0, the same as the AOSP.  The modified Linux kernel is released under the GPLv2 license.  Each package included in the AOSP tree carries the same license (check the "NOTICES" file produced from "create_package.sh").

Questions & Comments
--------------------
Any bug reports, questions, comments, frustrations, please direct to javallet[at]gmail[dot].com.  I'll do my best to help!
