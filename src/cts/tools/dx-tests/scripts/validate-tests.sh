#!/bin/bash
# Quick and dirty way to validate the DX tests against a JVM. Note that
# even the JVM has different bugs and verification behavior depending
# on the version. The tests pass 100% for the following setup:
# 
# java version "1.6.0"
# Java(TM) SE Runtime Environment (build 1.6.0-b105)
# Java HotSpot(TM) Server VM (build 1.6.0-b105, mixed mode)
#
# Linux ... 2.6.18.5-gg42workstation-mixed64-32 #1 SMP
# Tue Nov 25 21:45:59 PST 2008 x86_64 GNU/Linux
#
# You can only run the tests if the "dx-tests" target has been built before.
# 
java -cp ./lib/junit.jar:$ANDROID_BUILD_TOP/out/target/common/cts/dxconverter/classout/ junit.textui.TestRunner dxc.junit.AllTests


