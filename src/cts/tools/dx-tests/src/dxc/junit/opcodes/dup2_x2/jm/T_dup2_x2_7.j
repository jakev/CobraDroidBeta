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

.source T_dup2_x2_7.java
.class public dxc/junit/opcodes/dup2_x2/jm/T_dup2_x2_7
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()Z
    .limit stack 5
    .limit locals 1
    
    iconst_2
    iconst_3
    iconst_4
    iconst_5
    dup2_x2

    if_icmpeq Label1
    
    iconst_0
    ireturn
    
Label1:
    iconst_1
    ireturn
.end method
