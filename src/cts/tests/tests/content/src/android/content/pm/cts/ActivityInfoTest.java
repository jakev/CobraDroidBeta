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
import dalvik.annotation.ToBeFixed;

import android.app.cts.MockActivity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.test.AndroidTestCase;
import android.util.StringBuilderPrinter;

/**
 * Test {@link ActivityInfo}.
 */
@TestTargetClass(ActivityInfo.class)
public class ActivityInfoTest extends AndroidTestCase {
    ActivityInfo mActivityInfo;

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ActivityInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ActivityInfo",
            args = {android.content.pm.ActivityInfo.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "ActivityInfo#ActivityInfo(ActivityInfo), " +
            " should check whether the input ActivityInfo is null")
    public void testConstructor() {
        new ActivityInfo();

        ActivityInfo info = new ActivityInfo();
        new ActivityInfo(info);

        try {
            new ActivityInfo(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NPE is not expected.")
    public void testWriteToParcel() throws NameNotFoundException {
        ComponentName componentName = new ComponentName(mContext, MockActivity.class);

        mActivityInfo = mContext.getPackageManager().getActivityInfo(
                componentName, PackageManager.GET_META_DATA);

        Parcel p = Parcel.obtain();
        mActivityInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ActivityInfo info = ActivityInfo.CREATOR.createFromParcel(p);
        assertEquals(mActivityInfo.theme, info.theme);
        assertEquals(mActivityInfo.launchMode, info.launchMode);
        assertEquals(mActivityInfo.permission, info.permission);
        assertEquals(mActivityInfo.taskAffinity, info.taskAffinity);
        assertEquals(mActivityInfo.targetActivity, info.targetActivity);
        assertEquals(mActivityInfo.flags, info.flags);
        assertEquals(mActivityInfo.screenOrientation, info.screenOrientation);
        assertEquals(mActivityInfo.configChanges, info.configChanges);

        try {
            mActivityInfo.writeToParcel(null, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getThemeResource",
        args = {}
    )
    public void testGetThemeResource() throws NameNotFoundException {
        ComponentName componentName = new ComponentName(mContext, MockActivity.class);

        mActivityInfo = mContext.getPackageManager().getActivityInfo(
                componentName, PackageManager.GET_META_DATA);

        assertEquals(mActivityInfo.applicationInfo.theme, mActivityInfo.getThemeResource());
        mActivityInfo.theme = 1;
        assertEquals(mActivityInfo.theme, mActivityInfo.getThemeResource());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() throws NameNotFoundException {
        mActivityInfo = new ActivityInfo();
        assertNotNull(mActivityInfo.toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() throws NameNotFoundException {
        mActivityInfo = new ActivityInfo();
        assertEquals(0, mActivityInfo.describeContents());

        ComponentName componentName = new ComponentName(mContext, MockActivity.class);

        mActivityInfo = mContext.getPackageManager().getActivityInfo(
                componentName, PackageManager.GET_META_DATA);

        assertEquals(0, mActivityInfo.describeContents());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dump",
        args = {android.util.Printer.class, java.lang.String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "NPE is not expected.")
    public void testDump() {
        mActivityInfo = new ActivityInfo();

        StringBuilder sb = new StringBuilder();
        assertEquals(0, sb.length());
        StringBuilderPrinter p = new StringBuilderPrinter(sb);

        String prefix = "";
        mActivityInfo.dump(p, prefix);

        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);

        try {
            mActivityInfo.dump(null, "");
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }
    }
}
