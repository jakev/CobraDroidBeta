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

.source T_lreturn_6.java
.class public dxc/junit/opcodes/lreturn/jm/T_lreturn_6
.super java/lang/Object


.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method


.method public run()J
    .limit locals 12
    .limit stack 6
    
    lconst_1
    lstore_1
    ldc2_w 22222222222
    lstore_3
    ldc2_w 33333333333
    lstore 5
    
    ldc2_w 44444444444
    
    invokestatic dxc/junit/opcodes/lreturn/jm/T_lreturn_6/test()J
    
    ldc2_w 4444444444444
    lcmp
    ifne Label1
    
    ldc2_w 44444444444
    lcmp
    ifne Label0
    
    lload_1
    lconst_1
    lcmp
    ifne Label0
    
    lload_3
    ldc2_w 22222222222
    lcmp
    ifne Label0
    
    lload 5
    ldc2_w 33333333333
    lcmp
    ifne Label0    
    
    ldc2_w 12345612345
    lreturn

Label1:
    pop2
Label0:
    lconst_0
    lreturn

.end method


.method private static test()J
    .limit locals 8
    .limit stack 4
    
    ldc2_w 11111111111
    lstore_1
    ldc2_w 22222222222
    lstore_3
    ldc2_w 33333333333
    lstore 5
    
    ldc2_w 55555555555
    
    ldc2_w 44444444444
    lreturn
    
.end method