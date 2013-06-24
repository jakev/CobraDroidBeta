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

.source T_areturn_6.java
.class public dxc/junit/opcodes/areturn/jm/T_areturn_6
.super java/lang/Object


.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method


.method public run()Ljava/lang/String;
    .limit locals 6
    .limit stack 3
    
    ldc "a"
    astore_1
    ldc "b"
    astore_2
    ldc "c"
    astore 3
    
    ldc "d"
    
    invokestatic dxc/junit/opcodes/areturn/jm/T_areturn_6/test()Ljava/lang/String;
    
    ldc "ddd"
    if_acmpne Label1
    
    ldc "d"
    if_acmpne Label0
    
    aload_1
    ldc "a"
    if_acmpne Label0
    
    aload_2
    ldc "b"
    if_acmpne Label0
    
    aload 3
    ldc "c"
    if_acmpne Label0    
    
    ldc "hello"
    areturn
    
Label1:
    pop
Label0:
    ldc "a"
    areturn    
    
.end method

.method private static test()Ljava/lang/String;
    .limit stack 1
    .limit locals 3

    ldc "aaa"
    astore_0

    ldc "bbb"
    astore_1

    ldc "ccc"
    astore_2

    ldc "ddd"

    areturn

.end method