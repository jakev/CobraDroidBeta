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

.source T_ret_5.java
.class public dxc/junit/opcodes/ret/jm/T_ret_5
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()V
    .limit locals 5
    .limit stack 5

    iconst_1
    istore_2

    jsr Label3

    return
    
Label3:
    astore_1    
    
    iload_2
    iconst_0
    if_icmpeq Label1
    ret 1
Label1:
    ret 1    
    
.end method
