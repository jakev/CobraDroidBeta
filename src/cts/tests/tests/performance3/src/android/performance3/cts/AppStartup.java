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

package android.performance3.cts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.test.InstrumentationTestCase;

public class AppStartup extends InstrumentationTestCase {
    private static final long MAX_AVG_STARTUP_TIME = 1300;
    private static final String PACKAGE_UNDER_TEST = "com.android.browser";
    private static final String ACTIVITY_UNDER_TEST = "BrowserActivity";
    private static final int NUMBER_OF_ITERS = 10;

    private Intent buildIntent(final String pkgName, String className) {
        final String fullClassName = pkgName + "." + className;
        Intent intent = new Intent();
        intent.setClassName(pkgName, fullClassName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.google.com/m"));
        return intent;
    }

    public void testStartup() throws InterruptedException {
        long totalTime = 0;
        Intent i = buildIntent(PACKAGE_UNDER_TEST, ACTIVITY_UNDER_TEST);

        // Warm up
        for (int x = 0; x < 3; x++) {
          Activity a = getInstrumentation().startActivitySync(i);
          a.finish();
        }

        // Actually test.
        for (int x = 0; x < NUMBER_OF_ITERS; x++) {
            long start = System.currentTimeMillis();
            Activity a = getInstrumentation().startActivitySync(i);
            long end = System.currentTimeMillis();

            long diff = end - start;
            totalTime += diff;

            a.finish();
        }
        long avgStartupTime = totalTime / NUMBER_OF_ITERS;

        android.util.Log.d("AppStartup", "AppStartup for " +
                           PACKAGE_UNDER_TEST + "/" +
                           ACTIVITY_UNDER_TEST + " took " +
                           avgStartupTime + "ms.");

        assertTrue("App Took too long to startup: " + avgStartupTime +
                   " " + MAX_AVG_STARTUP_TIME,
                   avgStartupTime < MAX_AVG_STARTUP_TIME);
    }
}
