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

.source T_monitorenter_2.java
.class public dxc/junit/opcodes/monitorenter/jm/T_monitorenter_2
.super java/lang/Object

.field private flg I
.field public result Z

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    iconst_0
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.flg I

    aload_0
    iconst_1
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.result Z

    return
.end method



.method public run(I)V
    .throws java/lang/InterruptedException
    .limit stack 3
    .limit locals 4

    aload_0
    dup
    astore_2
    monitorenter

Label13:
    aload_0
    dup
    astore_3
    monitorenter

Label7:
    aload_0
    iload_1
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.flg I

    aload_3
    monitorexit

Label8:
    goto Label0

Label9:
    aload_0
    iconst_0
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.result Z

    aload_3
    monitorexit

Label11:
    athrow

Label0:
    ldc2_w 500
    invokestatic java/lang/Thread/sleep(J)V

    aload_0
    getfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.flg I
    iload_1
    if_icmpeq Label1

    aload_0
    iconst_0
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.result Z

Label1:
    aload_2
    monitorexit

Label14:
    goto Label2

Label15:
    aload_0
    iconst_0
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_2.result Z

    aload_2
    monitorexit

Label17:
    athrow

Label2:
    return

.catch all from Label7 to Label8 using Label9
.catch all from Label9 to Label11 using Label9
.catch all from Label13 to Label14 using Label15
.catch all from Label15 to Label17 using Label15
.end method
