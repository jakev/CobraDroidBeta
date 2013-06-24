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

.source T_opc_new_3.java
.class public dxc/junit/opcodes/opc_new/jm/T_opc_new_3
.super java/lang/Object

.field static i I

.method static <clinit>()V
    .limit stack 2
    .limit locals 0

    bipush 123
    iconst_0
    idiv
    putstatic dxc.junit.opcodes.opc_new.jm.T_opc_new_3.i I

    return

.end method



.method public <init>()V
    .limit stack 1
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V

    return
.end method



.method public static run()I
    .limit stack 2
    .limit locals 1

    new dxc/junit/opcodes/opc_new/jm/T_opc_new_3
    dup
    invokespecial dxc/junit/opcodes/opc_new/jm/T_opc_new_3/<init>()V
    astore_0

    getstatic dxc.junit.opcodes.opc_new.jm.T_opc_new_3.i I

    ireturn

.end method
