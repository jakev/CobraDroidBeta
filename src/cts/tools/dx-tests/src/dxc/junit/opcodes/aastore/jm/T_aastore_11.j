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

.source T_aastore_11.java
.class public dxc/junit/opcodes/aastore/jm/T_aastore_11
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run()I
    .limit stack 20
    .limit locals 10
    
    iconst_0
    istore_1

    iconst_1
    anewarray dxc/junit/opcodes/aastore/jm/SubClass
    astore_2    ; SubClass[]
    
    iconst_1
    anewarray dxc/junit/opcodes/aastore/jm/SuperClass
    astore_3    ; SuperClass[]
    
    new        dxc/junit/opcodes/aastore/jm/SubClass
    dup
    invokespecial dxc/junit/opcodes/aastore/jm/SubClass.<init>()V
    astore 4    ; SubClass
    
    new        dxc/junit/opcodes/aastore/jm/SuperClass
    dup
    invokespecial dxc/junit/opcodes/aastore/jm/SuperClass.<init>()V
    astore 5    ; SuperClass
    
    iconst_1
    anewarray dxc/junit/opcodes/aastore/jm/SuperInterface
    astore 6    ; SuperInterface[]
    
    iconst_1
    anewarray java/lang/Object
    astore 7    ; Object[]
    
    iconst_1
    anewarray dxc/junit/opcodes/aastore/jm/SuperInterface2
    astore 8    ; SuperInterface2[]    
    
; (SubClass -> SuperClass[])
    aload 3
    iconst_0
    aload 4
    aastore
    
; (SubClass -> SuperInterface[])
    aload 6
    iconst_0
    aload 4
    aastore
    
; (SubClass -> Object[])
    aload 7
    iconst_0
    aload 4
    aastore        
        
; !(SuperClass -> SubClass[])    
Label1:
    aload 2
    iconst_0
    aload 5
Label10:        
    nop
    aastore
Label11:    
    goto Label2
Label12:
    pop
    iinc 1 1
    goto Label2
        
; !(SuperClass -> SuperInterface2[])    
Label2:
    aload 8
    iconst_0
    aload 5
Label20:    
    aastore
Label21:    
    goto Label3
Label22:
    pop
    iinc 1 1
    goto Label3

; !(SubClass[] -> SuperInterface[])    
Label3:
    aload 6
    iconst_0
    aload 2
Label30:    
    aastore
Label31:    
    goto Label4
Label32:    
    pop
    iinc 1 1
    goto Label4

Label4:
Label6:        
    iload_1
    ireturn
    
.catch java/lang/ArrayStoreException from Label10 to Label11 using Label12
.catch java/lang/ArrayStoreException from Label20 to Label21 using Label22
.catch java/lang/ArrayStoreException from Label30 to Label31 using Label32

.end method
