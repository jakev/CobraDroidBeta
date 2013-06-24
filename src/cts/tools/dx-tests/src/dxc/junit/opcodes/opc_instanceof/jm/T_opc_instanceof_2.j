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

.source T_opc_instanceof_2.java
.class public dxc/junit/opcodes/opc_instanceof/jm/T_opc_instanceof_2
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()I
    .limit stack 2
    .limit locals 2
    
; (SubClass instanceof SuperClass)    
    new        dxc/junit/opcodes/opc_instanceof/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/opc_instanceof/jm/SubClass.<init>()V
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SuperClass
    ifne Label1
    iconst_1
    goto LabelExit

Label1:        
; !(SuperClass instanceof SubClass)    
    new dxc/junit/opcodes/opc_instanceof/jm/SuperClass
    dup
    invokespecial dxc/junit/opcodes/opc_instanceof/jm/SuperClass.<init>()V
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SubClass
    ifeq Label2
    iconst_2
    goto LabelExit
    
Label2:        
; (SubClass instanceof SuperInterface)    
    new dxc/junit/opcodes/opc_instanceof/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/opc_instanceof/jm/SubClass.<init>()V    
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SuperInterface
    ifne Label3
    iconst_3
    goto LabelExit    
    
Label3:        
; !(SubClass instanceof SuperInterface2)    
    new dxc/junit/opcodes/opc_instanceof/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/opc_instanceof/jm/SubClass.<init>()V    
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SuperInterface2
    ifeq Label4
    iconst_4
    goto LabelExit        
    
Label4:
; !(SubClass[] instanceof SuperInterface)    
    iconst_1
    anewarray dxc/junit/opcodes/opc_instanceof/jm/SubClass
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SuperInterface
    ifeq Label6
    sipush 5
    goto LabelExit        
    
Label6:        
; (SubClass[] instanceof Object)    
    iconst_1
    anewarray dxc/junit/opcodes/opc_instanceof/jm/SubClass
    instanceof java/lang/Object
    ifne Label7
    sipush 7
    goto LabelExit        

Label7:        
; !(SubClass[] instanceof SubClass)    
    iconst_1
    anewarray dxc/junit/opcodes/opc_instanceof/jm/SubClass
    instanceof dxc/junit/opcodes/opc_instanceof/jm/SubClass
    ifeq Label8
    sipush 8
    goto LabelExit    
    
Label8:        
; (SubClass[] instanceof SuperClass[])    
    iconst_1
    anewarray dxc/junit/opcodes/opc_instanceof/jm/SubClass
    instanceof [Ldxc/junit/opcodes/opc_instanceof/jm/SuperClass;
    ifne Label9
    sipush 9
    goto LabelExit
    
Label9:        
; !(SuperClass[] instanceof SubClass[])    
    iconst_1
    anewarray dxc/junit/opcodes/opc_instanceof/jm/SuperClass
    instanceof [Ldxc/junit/opcodes/opc_instanceof/jm/SubClass;
    ifeq Label0
    sipush 10
    goto LabelExit    
        
Label0:    
    iconst_0
LabelExit:
    ireturn
.end method
