/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.permission.cts;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import java.util.List;

/**
 * Verify that pre-installed packages don't have the debuggable
 * flag set.  The debuggable flag allows should only be used during
 * development, and never for shipping devices.
 */
public class DebuggableTest extends AndroidTestCase {

    public void testNoDebuggable() {
        List<ApplicationInfo> apps = getContext()
                .getPackageManager()
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo app : apps) {
            String appName = app.packageName;
            assertTrue("Package " + appName + " is marked as debuggable.",
                    (app.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0);
        }
    }
}
