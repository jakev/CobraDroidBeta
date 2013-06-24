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

.source T_invokeinterface_14.java
.class public dxc/junit/opcodes/invokeinterface/jm/T_invokeinterface_14
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method



.method public run(Ldxc/junit/opcodes/invokeinterface/jm/ITest;)I
    .limit stack 3
    .limit locals 4

    bipush 123
    istore_2

    sipush 345
    istore_3

    aload_1
    sipush 64
    sipush 2
    invokeinterface dxc/junit/opcodes/invokeinterface/jm/ITest/testArgsOrder(II)I 3
    sipush 32
    if_icmpne Label0

    iload_2
    bipush 123
    if_icmpne Label0

    iload_3
    sipush 345
    if_icmpne Label0

    iconst_1
    ireturn

    Label0:
    iconst_0
    ireturn
.end method
