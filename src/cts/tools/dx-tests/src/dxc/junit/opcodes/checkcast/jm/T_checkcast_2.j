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

.source T_checkcast_2.java
.class public dxc/junit/opcodes/checkcast/jm/T_checkcast_2
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()I
    .limit stack 20
    .limit locals 2
    
    iconst_0
    istore_1
    
; (SubClass instanceof SuperClass)    
    new        dxc/junit/opcodes/checkcast/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/checkcast/jm/SubClass.<init>()V
    checkcast dxc/junit/opcodes/checkcast/jm/SuperClass
    pop
    
; (SubClass[] instanceof SuperClass[])    
    iconst_1
    anewarray dxc/junit/opcodes/checkcast/jm/SubClass
    checkcast [Ldxc/junit/opcodes/checkcast/jm/SuperClass;
    pop    
    
; (SubClass[] instanceof Object)    
    iconst_1
    anewarray dxc/junit/opcodes/checkcast/jm/SubClass
    checkcast java/lang/Object
    pop
    
; (SubClass instanceof SuperInterface)    
    new dxc/junit/opcodes/checkcast/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/checkcast/jm/SubClass.<init>()V    
    checkcast dxc/junit/opcodes/checkcast/jm/SuperInterface
    pop
        

; !(SuperClass instanceof SubClass)    
Label1:
    new dxc/junit/opcodes/checkcast/jm/SuperClass
    dup
    invokespecial dxc/junit/opcodes/checkcast/jm/SuperClass.<init>()V
Label10:    
    checkcast dxc/junit/opcodes/checkcast/jm/SubClass
    pop
Label11:    
    goto Label2
Label12:
    pop
    iinc 1 1
    goto Label2
        
; !(SubClass instanceof SuperInterface2)    
Label2:
    new dxc/junit/opcodes/checkcast/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/checkcast/jm/SubClass.<init>()V    
Label20:    
    checkcast dxc/junit/opcodes/checkcast/jm/SuperInterface2
    pop
Label21:    
    goto Label3
Label22:
    pop
    iinc 1 1
    goto Label3

; !(SubClass[] instanceof SuperInterface)    
Label3:
    iconst_1
    anewarray dxc/junit/opcodes/checkcast/jm/SubClass
Label30:    
    checkcast dxc/junit/opcodes/checkcast/jm/SuperInterface
    pop
Label31:    
    goto Label4
Label32:    
    pop
    iinc 1 1
    goto Label4

; !(SubClass[] instanceof SubClass)    
Label4:
    iconst_1
    anewarray dxc/junit/opcodes/checkcast/jm/SubClass
Label40:    
    checkcast dxc/junit/opcodes/checkcast/jm/SubClass
    pop
Label41:    
    goto Label5
Label42:
    pop
    iinc 1 1
    goto Label5    
    
; !(SuperClass[] instanceof SubClass[])    
Label5:
    iconst_1
    anewarray dxc/junit/opcodes/checkcast/jm/SuperClass
Label50:    
    checkcast [Ldxc/junit/opcodes/checkcast/jm/SubClass;
    pop
Label51:    
    goto Label6
Label52:
    pop
    iinc 1 1
    
Label6:        
    iload_1
    ireturn
    
.catch java/lang/ClassCastException from Label10 to Label11 using Label12
.catch java/lang/ClassCastException from Label20 to Label21 using Label22
.catch java/lang/ClassCastException from Label30 to Label31 using Label32
.catch java/lang/ClassCastException from Label40 to Label41 using Label42
.catch java/lang/ClassCastException from Label50 to Label51 using Label52
.end method
