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

.source T_opc_return_1.java
.class public dxc/junit/opcodes/opc_return/jm/T_opc_return_1
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()I
    .limit locals 6
    .limit stack 6
    
    iconst_1
    istore_1
    iconst_2
    istore_2
    iconst_3
    istore 3
    
    iconst_4
    
    invokestatic dxc/junit/opcodes/opc_return/jm/T_opc_return_1/test()V
    
    iconst_4
    if_icmpne Label0
    
    iload_1
    iconst_1
    if_icmpne Label0
    
    iload_2
    iconst_2
    if_icmpne Label0
    
    iload_3
    iconst_3
    if_icmpne Label0    
    
    ldc 123456
    ireturn

Label1:
    pop
Label0:
    iconst_0
    ireturn

.end method

.method private static test()V
    .limit locals 4
    .limit stack 4
    
    ldc 0xaaa
    istore_1
    ldc 0xbbbb
    istore_2
    ldc 0xcccc
    istore_3
    
    ldc 0xdddd
    
    ldc 0xcafe
    return
    
.end method