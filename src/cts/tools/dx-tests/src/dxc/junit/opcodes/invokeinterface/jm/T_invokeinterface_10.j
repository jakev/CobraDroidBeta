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

.source T_invokeinterface_10.java
.class public dxc/junit/opcodes/invokeinterface/jm/T_invokeinterface_10
.super java/lang/Object

.method public <init>(Ldxc/junit/opcodes/invokeinterface/jm/ITest;)V
    .limit stack 1
    .limit locals 2

    aload_0
    invokespecial java/lang/Object/<init>()V

;    aload_1
    iconst_1
    invokeinterface dxc/junit/opcodes/invokeinterface/jm/ITest/doit()V 1
    return
.end method
