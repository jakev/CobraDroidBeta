; Copyright (C) 2008 The Android Open Source Project
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

.source T_invokestatic_12.java
.class public dxc/junit/opcodes/invokestatic/jm/T_invokestatic_12
.super java/lang/Object
.implements java/lang/Runnable

.field static value I
.field static failed Z

.method static <clinit>()V
    .limit stack 1
    iconst_0
    putstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I

    iconst_0
    putstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.failed Z

    return
.end method


.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method


.method public run()V
    .limit stack 2
    .limit locals 2

    iconst_0
    istore_1

    Label1:
    invokestatic dxc/junit/opcodes/invokestatic/jm/T_invokestatic_12/test()V

    iinc 1 1

    iload_1
    sipush 1000
    if_icmplt Label1

    return

.end method


.method private static synchronized test()V
    .limit stack 2
    .limit locals 1

    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I
    iconst_1
    iadd
    putstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I

    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I
    istore_0

    invokestatic java/lang/Thread/yield()V

    iload_0
    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I
    if_icmpeq Label0

    iconst_1
    putstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.failed Z

Label0:
    return
.end method

.method public static execute()Z
    .limit stack 3
    .limit locals 4

    new dxc/junit/opcodes/invokestatic/jm/T_invokestatic_12
    dup
    invokespecial dxc/junit/opcodes/invokestatic/jm/T_invokestatic_12/<init>()V
    astore_0

    new java/lang/Thread
    dup
    aload_0
    invokespecial java/lang/Thread/<init>(Ljava/lang/Runnable;)V
    astore_1

    new java/lang/Thread
    dup
    aload_0
    invokespecial java/lang/Thread/<init>(Ljava/lang/Runnable;)V
    astore_2

    aload_1
    invokevirtual java/lang/Thread/start()V

    aload_2
    invokevirtual java/lang/Thread/start()V

Label12:
    ldc2_w 5000
    invokestatic java/lang/Thread/sleep(J)V

Label13:
    goto Label0

Label14:
    astore_3
    iconst_0
    ireturn

Label0:
    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.value I
    sipush 2000
    if_icmpeq Label1

    iconst_0
    ireturn

Label1:
    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12.failed Z
    ifeq Label2
    iconst_0
    ireturn

Label2:
    iconst_1
    ireturn

.catch java/lang/InterruptedException from Label12 to Label13 using Label14
.end method
