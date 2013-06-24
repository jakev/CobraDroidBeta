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

.source T_monitorenter_1.java
.class public dxc/junit/opcodes/monitorenter/jm/T_monitorenter_1
.super java/lang/Object

.field public counter I

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    iconst_0
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_1.counter I

    return
.end method


.method public run()V
    .throws java/lang/InterruptedException
    .limit stack 3
    .limit locals 3
    
Label1:
    aload_0
    dup
    astore_1
    monitorenter

Label5:
    aload_0
    getfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_1.counter I
    istore_2

Label3:
    ldc2_w 500
    invokestatic java/lang/Thread/sleep(J)V

    aload_0
    iinc 2 1
    iload_2
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_1.counter I

Label4:
    aload_1
    monitorexit

Label6:
    goto Label0

Label7:
    aload_1
    monitorexit

Label9:
    aload_0
    iconst_m1
    putfield dxc.junit.opcodes.monitorenter.jm.T_monitorenter_1.counter I
    athrow

Label0:
    return

.catch all from Label5 to Label6 using Label7
.catch all from Label7 to Label9 using Label7
.end method
