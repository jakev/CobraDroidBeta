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

package android.app.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.test.InstrumentationTestCase;

/**
 * Test {@link Application}.
 */
@TestTargetClass(Application.class)
public class ApplicationTest extends InstrumentationTestCase {

    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Application",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onConfigurationChanged",
        args = {android.content.res.Configuration.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onCreate",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "According to issue 1653192, a Java app can't allocate memory without" +
                " restriction, thus it's hard to test this callback.",
        method = "onLowMemory",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "The documentation states that one cannot rely on this method being called.",
        method = "onTerminate",
        args = {}
      )
    })
    public void testApplication() throws Throwable {
        final Instrumentation instrumentation = getInstrumentation();
        final Context targetContext = instrumentation.getTargetContext();

        final Intent intent = new Intent(targetContext, MockApplicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final Activity activity = instrumentation.startActivitySync(intent);
        final MockApplication mockApp = (MockApplication) activity.getApplication();
        assertTrue(mockApp.isConstructorCalled);
        assertTrue(mockApp.isOnCreateCalled);

        runTestOnUiThread(new Runnable() {
            public void run() {
               OrientationTestUtils.toggleOrientation(activity);
            }
        });
        instrumentation.waitForIdleSync();
        assertTrue(mockApp.isOnConfigurationChangedCalled);
    }

}
