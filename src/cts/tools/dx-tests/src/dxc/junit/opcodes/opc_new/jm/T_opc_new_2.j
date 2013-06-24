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

.source T_opc_new_2.java
.class public dxc/junit/opcodes/opc_new/jm/T_opc_new_2
.super java/lang/Object

.field  i I

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    aload_0
    bipush 123
    putfield dxc.junit.opcodes.opc_new.jm.T_opc_new_2.i I

    return
.end method


.method public static run()I
    .limit stack 2
    .limit locals 1

    new dxc/junit/opcodes/opc_new/jm/T_opc_new_2
    dup
;    invokespecial dxc/junit/opcodes/opc_new/jm/T_opc_new_2/<init>()V
    astore_0

    aload_0
    getfield dxc.junit.opcodes.opc_new.jm.T_opc_new_2.i I

    ireturn

.end method
