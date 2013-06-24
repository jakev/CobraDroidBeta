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

.source T_invokevirtual_14.java
.class public dxc/junit/opcodes/invokevirtual/jm/T_invokevirtual_14
.super dxc/junit/opcodes/invokevirtual/jm/TSuper

.method public <init>()V
    aload_0
    invokespecial dxc/junit/opcodes/invokevirtual/jm/TSuper/<init>()V
    return
.end method


.method public run()Z
    .limit stack 3
    .limit locals 3

    bipush 123
    istore_1

    sipush 659
    istore_2

    aload_0
    sipush 300
    iconst_3
    invokevirtual dxc/junit/opcodes/invokevirtual/jm/T_invokevirtual_14/testArgsOrder(II)I

    bipush 100
    if_icmpne Label0

    iload_1
    bipush 123
    if_icmpne Label0

    iload_2
    sipush 659
    if_icmpne Label0

    iconst_1
    ireturn

Label0:
    iconst_0
    ireturn

.end method
