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

.source T_dup2_x2_4.java
.class public dxc/junit/opcodes/dup2_x2/jm/T_dup2_x2_4
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()Z
    .limit stack 8
    .limit locals 2
    
    iconst_3
    ldc 4.0f
    ldc2_w 5    ; 3 4.0 [5]
    dup2_x2        ; [5] 3 4.0 [5]
    
    ldc2_w 5
    lcmp
    ifne Label3    ; [5] 3 4.0

    ldc 4.0f
    fcmpl        ; [5] 3
    ifne Label2
         
    iconst_3
    if_icmpne Label1 ; [5]

    ldc2_w 5
    lcmp        ;
    ifne Label0

    iconst_1
    ireturn
   
Label3:
    pop
Label2:
    pop
Label1:
    pop2
Label0:    
    iconst_0
    ireturn
.end method
