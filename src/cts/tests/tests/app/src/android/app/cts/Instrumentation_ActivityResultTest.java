/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.app.cts;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Instrumentation.ActivityResult.class)
public class Instrumentation_ActivityResultTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Instrumentation.ActivityResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getResultCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getResultData",
            args = {}
        )
    })
    public void testActivityResultOp() {
        Intent intent = new Intent();
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, intent);
        assertEquals(Activity.RESULT_OK, result.getResultCode());
        assertEquals(intent, result.getResultData());

        result = new ActivityResult(Activity.RESULT_CANCELED, intent);
        assertEquals(Activity.RESULT_CANCELED, result.getResultCode());
    }
}
