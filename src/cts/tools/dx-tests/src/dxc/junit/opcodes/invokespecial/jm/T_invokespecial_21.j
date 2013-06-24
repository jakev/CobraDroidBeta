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

.source T_invokespecial_21.java
.class public dxc/junit/opcodes/invokespecial/jm/T_invokespecial_21
.super dxc/junit/opcodes/invokespecial/jm/TSuper

.method public <init>()V
    aload_0
    invokespecial dxc/junit/opcodes/invokespecial/jm/TSuper/<init>()V
    return
.end method

.method public run()I
    .limit locals 3
    .limit stack 5
    
    sipush 123
    istore_1
    sipush 456
    istore_2
    
    aload_0
    iconst_1
    iconst_2
    invokespecial dxc/junit/opcodes/invokespecial/jm/T_invokespecial_21/test(II)I
    
    sipush 5
    if_icmpne Label0
    
    iload_1
    sipush 123
    if_icmpne Label0

    iload_2
    sipush 456
    if_icmpne Label0    
    
    iconst_1
    ireturn
    
Label0:    
    iconst_0
    ireturn
.end method

.method private test(II)I
    .limit locals 3
    sipush 987
    istore_1
    sipush 765
    istore_2
    
    sipush 5
    ireturn
.end method
