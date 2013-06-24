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

.source T_putfield_18.java
.class public dxc/junit/opcodes/putfield/jm/T_putfield_18
.super java/lang/Object

.method public <init>()V
    .limit stack 1
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V

    return
.end method



.method public run()Ljava/lang/String;
    .limit stack 2
    .limit locals 2

    new dxc/junit/opcodes/putfield/jm/TChild
    dup
    invokespecial dxc/junit/opcodes/putfield/jm/TChild/<init>()V
    astore_1

    aload_1
    ldc "xyz"
    putfield dxc.junit.opcodes.putfield.jm.TSuper.s Ljava/lang/String;

    aload_1
    getfield dxc.junit.opcodes.putfield.jm.TChild.s Ljava/lang/String;

    areturn
.end method
