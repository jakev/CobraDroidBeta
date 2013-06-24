/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.hardware.cts;

import junit.framework.TestCase;
import android.hardware.Camera.Parameters;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Parameters.class)
public class Camera_ParametersTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "get",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "set",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "getInt",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "set",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPictureFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPictureFormat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPictureSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPictureSize",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewFormat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewFrameRate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewFrameRate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewSize",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "flatten",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "unflatten",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.TODO,
            notes = "test removed due to invalid assumptions",
            method = "remove",
            args = {java.lang.String.class}
        )
    })
    public void testAccessMethods() {
        // Camera.Parameters methods are tested in CameraTest#testAccessParameters().
    }

}

