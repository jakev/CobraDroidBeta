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

.source T_jsr_w_5.java
.class public dxc/junit/opcodes/jsr_w/jm/T_jsr_w_5
.super java/lang/Object

.field public i1 I

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()Z
    .limit stack 2
    .limit locals 2
    
    jsr_w SetField

    aload_0
    getfield dxc.junit.opcodes.jsr_w.jm.T_jsr_w_5.i1 I
    sipush 1000
    if_icmpne Label0
    
    iconst_1
    ireturn
    
Label0:
    iconst_0    
    ireturn

SetField:
    astore_1
    aload_0
    sipush 1000
    putfield dxc.junit.opcodes.jsr_w.jm.T_jsr_w_5.i1 I
    
    ret 1
.end method
