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

package android.content.pm.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.test.AndroidTestCase;

@TestTargetClass(ResolveInfo.DisplayNameComparator.class)
public class ResolveInfo_DisplayNameComparatorTest extends AndroidTestCase {
    private static final String MAIN_ACTION_NAME = "android.intent.action.MAIN";
    private static final String SERVICE_NAME = "android.content.pm.cts.activity.PMTEST_SERVICE";

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test compare",
            method = "compare",
            args = {android.content.pm.ResolveInfo.class, android.content.pm.ResolveInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "ResolveInfo.DisplayNameComparator",
            args = {android.content.pm.PackageManager.class}
        )
    })
    public void testDisplayNameComparator() {
        PackageManager pm = getContext().getPackageManager();
        DisplayNameComparator dnc = new DisplayNameComparator(pm);

        Intent intent = new Intent(MAIN_ACTION_NAME);
        ResolveInfo activityInfo = pm.resolveActivity(intent, 0);

        intent = new Intent(SERVICE_NAME);
        ResolveInfo serviceInfo = pm.resolveService(intent, PackageManager.GET_RESOLVED_FILTER);

        assertTrue(dnc.compare(activityInfo, serviceInfo) < 0);
        assertTrue(dnc.compare(activityInfo, activityInfo) == 0);
        assertTrue(dnc.compare(serviceInfo, activityInfo) > 0);
    }
}
