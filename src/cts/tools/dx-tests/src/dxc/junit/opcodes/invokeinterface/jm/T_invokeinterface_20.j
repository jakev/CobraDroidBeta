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

.source T_invokeinterface_20.java
.class public dxc/junit/opcodes/invokeinterface/jm/T_invokeinterface_20
.super java/lang/Object

.field public static i I

.method static <clinit>()V
    .limit stack 1
    iconst_0
    putstatic dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_20.i I
    return
.end method



.method public <init>()V
    .limit stack 1
    .limit locals 1
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method



.method public run(Ldxc/junit/opcodes/invokeinterface/jm/ITest;)V
    .limit stack 1
    .limit locals 2
    
    aload_0
    invokeinterface dxc/junit/opcodes/invokeinterface/jm/T_invokeinterface_20/<clinit>()V 1
    return

.end method
