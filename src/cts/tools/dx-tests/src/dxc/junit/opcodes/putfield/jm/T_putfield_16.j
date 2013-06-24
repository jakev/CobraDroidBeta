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

.source T_putfield_16.java
.class public dxc/junit/opcodes/putfield/jm/T_putfield_16
.super java/lang/Object

.field public o Ljava/lang/Object;


.method public <init>()V
    .limit stack 2
    aload_0
    invokespecial java/lang/Object/<init>()V

    aload_0
    aconst_null
    putfield dxc.junit.opcodes.putfield.jm.T_putfield_16.o Ljava/lang/Object;
    return
.end method


.method public run()V
    .limit stack 4
    .limit locals 2

    aload_0
    new java/lang/String
    dup
    ldc ""
    invokespecial java/lang/String/<init>(Ljava/lang/String;)V

    putfield dxc.junit.opcodes.putfield.jm.T_putfield_16.o Ljava/lang/Object;

    return
.end method
