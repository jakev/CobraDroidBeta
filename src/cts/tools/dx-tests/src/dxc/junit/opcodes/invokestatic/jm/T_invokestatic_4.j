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

.source T_invokestatic_4.java
.class public dxc/junit/opcodes/invokestatic/jm/T_invokestatic_4
.super java/lang/Object

.field static v2 J

.method static <clinit>()V
    .limit stack 2
    .limit locals 0

    ldc2_w 123456789
    putstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_4.v2 J
    return
.end method

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public static run()J
    .limit stack 2
    .limit locals 2

    getstatic dxc.junit.opcodes.invokestatic.jm.T_invokestatic_4.v2 J
    lreturn

.end method