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

.source T_t482_10_1.java
.class public dxc/junit/verify/t482_10/jm/T_t482_10_1
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V

    return
.end method



.method public run()V
    .limit stack 2
    .limit locals 5

;    iconst_2
;    newarray int
;    astore_1

    iconst_1
    istore_2

Label8:
    iconst_3
    newarray int
    astore_1

    iload_2
    iconst_1
    if_icmpne Label0

    new java/lang/Exception
    dup
    invokespecial java/lang/Exception/<init>()V
    athrow

Label13:
    goto Label0

Label14:
    astore_3

Label10:
    aload_1
    iconst_0
    iaload
    istore_2

Label11:
    goto Label2

Label16:
    astore 4

    iconst_1
    istore_2

    aload 4
    athrow

Label0:
    iconst_1
    istore_2

    goto Label3

Label2:
    iconst_1
    istore_2

Label3:
    return

.catch java/lang/Exception from Label8 to Label13 using Label14
.catch all from Label8 to Label16 using Label16
.end method
