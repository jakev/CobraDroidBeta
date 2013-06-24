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

.source T_freturn_6.java
.class public dxc/junit/opcodes/freturn/jm/T_freturn_6
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()F
    .limit locals 6
    .limit stack 6
    
    fconst_1
    fstore_1
    fconst_2
    fstore_2
    ldc 3.0f
    fstore 3
    
    ldc 4.0f
    
    invokestatic dxc/junit/opcodes/freturn/jm/T_freturn_6/test()F
    
    ldc 4444.0f
    fcmpl
    ifne Label1
    
    ldc 4.0f
    fcmpl
    ifne Label0
    
    fload_1
    fconst_1
    fcmpl
    ifne Label0
    
    fload_2
    fconst_2
    fcmpl
    ifne Label0
    
    fload_3
    ldc 3.0f
    fcmpl
    ifne Label0    
    
    ldc 123456.0f
    freturn

Label1:
    pop
Label0:
    fconst_0
    freturn

.end method

.method private static test()F
    .limit locals 4
    .limit stack 4
    
    ldc 1111.0f
    fstore_1
    ldc 2222.0f
    fstore_2
    ldc 3333.0f
    fstore_3
    
    ldc 5555.0f
    
    ldc 4444.0f
    freturn
    
.end method