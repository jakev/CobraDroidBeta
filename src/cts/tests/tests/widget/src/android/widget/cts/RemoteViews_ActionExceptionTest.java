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

package android.widget.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.test.InstrumentationTestCase;
import android.widget.RemoteViews;
import android.widget.RemoteViews.ActionException;

/**
 * Test {@link ActionException}.
 */
@TestTargetClass(ActionException.class)
public class RemoteViews_ActionExceptionTest extends InstrumentationTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RemoteViews.ActionException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RemoteViews.ActionException",
            args = {java.lang.Exception.class}
        )
    })
    public void testConstructor() {
        String message = "This is exception message";
        new RemoteViews.ActionException(message);

        new RemoteViews.ActionException(new Exception());
    }
}
