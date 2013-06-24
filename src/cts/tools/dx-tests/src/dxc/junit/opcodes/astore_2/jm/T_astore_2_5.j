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

.source T_astore_2_5.java
.class public dxc/junit/opcodes/astore_2/jm/T_astore_2_5
.super java/lang/Object


.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method


.method public static run()Z
    .limit stack 4
    .limit locals 3

    ldc "hello"
    astore_2
    aload_2

    ldc "hello"
    astore 2
    aload_2
    
    if_acmpne Label0
    iconst_1
    ireturn

Label0:
    iconst_0
    ireturn

.end method