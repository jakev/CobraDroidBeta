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

.source T_t482_8_2.java
.class public dxc/junit/verify/t482_8/jm/T_t482_8_2
.super java/lang/Object


.method  <init>(I)V
    .limit stack 1
    .limit locals 2

    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    invokespecial java/lang/Object/<init>()V


    return

.end method



.method public run()V
    .limit stack 3
    .limit locals 2

    new dxc/junit/verify/t482_8/jm/T_t482_8_1
    dup
    iconst_1
    invokespecial dxc/junit/verify/t482_8/jm/T_t482_8_1/<init>(I)V
    astore_1

    return

.end method
