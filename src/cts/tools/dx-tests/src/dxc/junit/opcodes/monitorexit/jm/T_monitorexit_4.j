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

.source T_monitorexit_4.java
.class public dxc/junit/opcodes/monitorexit/jm/T_monitorexit_4
.super java/lang/Object

.method public <init>()V
    .limit stack 1
    .limit locals 1

    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method



.method public run()V
    .limit stack 2
    .limit locals 1

    aload_0
    monitorenter
    
Label1:    
    aconst_null
    monitorexit
Label2:
    goto Label4

Label3:
    aload_0
    monitorexit
    athrow
Label4:
    return
    
.catch all from Label1 to Label2 using Label3    
.end method
