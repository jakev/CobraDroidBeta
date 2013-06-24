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

.source T_ret_4_w.java
.class public dxc/junit/opcodes/ret/jm/T_ret_4_w
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()Z
    .limit locals 5
    .limit stack 5

    goto Label1
    
Label3:
    astore_1    
    ret_w 1
    
Label2:
    astore_1
    jsr_w Label3
    ret_w 1    
    
Label1:    
    jsr_w Label2
    
    iconst_0
    ireturn
.end method
