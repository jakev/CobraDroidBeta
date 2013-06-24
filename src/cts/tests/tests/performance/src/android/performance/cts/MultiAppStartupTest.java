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

package android.performance.cts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.test.InstrumentationTestCase;

public class MultiAppStartupTest extends InstrumentationTestCase {
    private static final String PACKAGE_UNDER_TEST = "com.android.calculator2";
    private static final String ACTIVITY_UNDER_TEST = "Calculator";
    private static final int ACTIVITY_STARTUP_WAIT_TIME = 1000;

    private Intent buildIntent(final String pkgName, String className, boolean isMain) {
        final String fullClassName = pkgName + "." + className;
        Intent intent = new Intent();
        intent.setClassName(pkgName, fullClassName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isMain) {
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        return intent;
    }

    private void launchActivity(final String pkgName, String className, boolean isMain) {
        Context ctx = getInstrumentation().getContext();
        ctx.startActivity(buildIntent(pkgName, className, isMain));
    }

    private long launchActivityUnderTest() {
        long start = System.currentTimeMillis();
        Intent i = buildIntent(PACKAGE_UNDER_TEST,
                               ACTIVITY_UNDER_TEST,
                               true);
        Activity a = getInstrumentation().startActivitySync(i);
        long end = System.currentTimeMillis();
        long diff = end - start;
        a.finish();
        return diff;
    }

    public void testMultipleApps() throws InterruptedException {
        // Measure how long the initial startup of the application takes
        long initialStartDuration =  launchActivityUnderTest();

        // Re-launch the activity.  It was finished in
        // launchActivityUnderTest, so this ensures that it is around
        // for the ActivityManager to possibly kill it.
        launchActivity(PACKAGE_UNDER_TEST,
                       ACTIVITY_UNDER_TEST,
                       true);

        // Then launch a few more
        launchActivity("com.android.browser", "BrowserActivity", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.mms", "ui.ConversationList", true);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.contacts", "TwelveKeyDialer", false);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);
        launchActivity("com.android.contacts", "RecentCallsListActivity", false);
        Thread.sleep(ACTIVITY_STARTUP_WAIT_TIME);

        long finalStartDuration = launchActivityUnderTest();

        // assure that the time to re-start the application is less
        // than the original start time.
        assertTrue("Restart of inital app took to long: " +
                   finalStartDuration + " " + initialStartDuration,
                   finalStartDuration < initialStartDuration);

        // TODO: Change this check to use RunningProcesses from
        // ActivityManager which should provide better results.
    }
}
