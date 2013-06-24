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

.source T_areturn_7.java
.class public dxc/junit/opcodes/areturn/jm/T_areturn_7
.super java/lang/Object
.implements java/lang/Runnable

.field  value Ljava/lang/Integer;
.field  failed Z

.method public <init>()V
    .limit stack 4
    .limit locals 1
    
    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    new java/lang/Integer
    dup
    iconst_0
    invokespecial java/lang/Integer/<init>(I)V
    putfield dxc.junit.opcodes.areturn.jm.T_areturn_7.value Ljava/lang/Integer;

    aload_0
    iconst_0
    putfield dxc.junit.opcodes.areturn.jm.T_areturn_7.failed Z

    return

.end method


.method public run()V
    .limit stack 2
    .limit locals 2
    
    iconst_0
    istore_1

Label4:
    goto Label0

Label1:
    aload_0
    invokespecial dxc/junit/opcodes/areturn/jm/T_areturn_7/test()Ljava/lang/Integer;
    pop

    iinc 1 1

Label0:
    iload_1
    sipush 1000
    if_icmplt Label1

    return
.end method



.method private synchronized test()Ljava/lang/Integer;
    .limit stack 4
    .limit locals 2
    
    new java/lang/Integer
    dup
    aload_0
    getfield dxc.junit.opcodes.areturn.jm.T_areturn_7.value Ljava/lang/Integer;
    invokevirtual java/lang/Integer/intValue()I
    iconst_1
    iadd
    invokespecial java/lang/Integer/<init>(I)V
    astore_1

    aload_0
    aload_1
    putfield dxc.junit.opcodes.areturn.jm.T_areturn_7.value Ljava/lang/Integer;

    invokestatic java/lang/Thread/yield()V

    aload_1
    aload_0
    getfield dxc.junit.opcodes.areturn.jm.T_areturn_7.value Ljava/lang/Integer;
    if_acmpeq Label0

    aload_0
    iconst_1
    putfield dxc.junit.opcodes.areturn.jm.T_areturn_7.failed Z

Label0:
    aload_1
    areturn
.end method



.method public static execute()Z
    .limit stack 3
    .limit locals 4

    new dxc/junit/opcodes/areturn/jm/T_areturn_7
    dup
    invokespecial dxc/junit/opcodes/areturn/jm/T_areturn_7/<init>()V
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

Label10:
    iconst_0
    ireturn

Label0:
    aload_0
    getfield dxc.junit.opcodes.areturn.jm.T_areturn_7.value Ljava/lang/Integer;
    invokevirtual java/lang/Integer/intValue()I
    sipush 2000
    if_icmpeq Label1

    iconst_0
    ireturn

Label1:
    aload_0
    getfield dxc.junit.opcodes.areturn.jm.T_areturn_7.failed Z
    ifeq Label2
    iconst_0
    goto Label3

Label2:
    iconst_1

Label3:
    ireturn

.catch java/lang/InterruptedException from Label12 to Label13 using Label14
.end method
