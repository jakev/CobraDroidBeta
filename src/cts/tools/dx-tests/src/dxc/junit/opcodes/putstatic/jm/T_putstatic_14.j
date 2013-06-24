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

.source T_putstatic_14.java
.class public dxc/junit/opcodes/putstatic/jm/T_putstatic_14
.super dxc/junit/opcodes/putstatic/jm/T_putstatic_1


.method public <init>()V
    aload_0
    invokespecial dxc/junit/opcodes/putstatic/jm/T_putstatic_1/<init>()V
    return
.end method

.method public run()V
    ldc 1000000
    putstatic dxc.junit.opcodes.putstatic.jm.T_putstatic_1.st_p1 I
    return
.end method

.method public static getProtectedField()I
   getstatic dxc.junit.opcodes.putstatic.jm.T_putstatic_1.st_p1 I
   ireturn
.end method