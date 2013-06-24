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

.source T_lookupswitch_6.java
.class public dxc/junit/opcodes/lookupswitch/jm/T_lookupswitch_6
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public run(I)I
    .limit stack 1
    .limit locals 2
    iload_1

    lookupswitch
            -1 : Label0
            10 : Label1
            15 : Label1
            default: Label3

    Label0:
    iconst_2
    ireturn

    Label1:
    bipush 20
    ireturn

    Label3:
    iconst_m1
    ireturn
.end method
