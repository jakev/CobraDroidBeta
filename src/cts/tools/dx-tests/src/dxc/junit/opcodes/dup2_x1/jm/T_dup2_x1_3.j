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

.source T_dup2_x1_3.java
.class public dxc/junit/opcodes/dup2_x1/jm/T_dup2_x1_3
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()Z
    .limit stack 8
    .limit locals 3
    
    iconst_5
    dconst_1
    dup2_x1        ; [1] 5 [1]
    
    dconst_1    ; [1] 5 [1] [1]
    dcmpl        ; [1] 5
    ifne Label2
    iconst_5    ; [1] 5 5
    if_icmpne Label1
       dconst_1    ; [1][1]
    dcmpl        ; 
    ifne Label0
    
    iconst_1
    ireturn
    
 Label2:
     pop
 Label1:
     pop2
 Label0:
    iconst_0
    ireturn
.end method
