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

.source T_t482_9_2.java
.class public dxc/junit/verify/t482_9/jm/T_t482_9_2
.super java/lang/Object

.field  v I

.method public <init>()V
    .limit stack 2
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    iconst_0
    putfield dxc.junit.verify.t482_9.jm.T_t482_9_2.v I

    return
.end method



.method  test()V
    .limit stack 0
    .limit locals 1
    return
.end method



.method public static run()V
    .limit stack 2
    .limit locals 1

    new dxc/junit/verify/t482_9/jm/T_t482_9_2
;    dup
;    invokespecial dxc/junit/verify/t482_9/jm/T_t482_9_2/<init>()V

    invokevirtual dxc/junit/verify/t482_9/jm/T_t482_9_2/test()V

    return

.end method
