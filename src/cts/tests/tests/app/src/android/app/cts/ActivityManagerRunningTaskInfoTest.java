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

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(ActivityManager.RunningTaskInfo.class)
public class ActivityManagerRunningTaskInfoTest extends AndroidTestCase {
    protected ActivityManager.RunningTaskInfo mRunningTaskInfo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRunningTaskInfo = new ActivityManager.RunningTaskInfo();

        mRunningTaskInfo.id = 1;
        mRunningTaskInfo.baseActivity = null;
        mRunningTaskInfo.topActivity = null;
        mRunningTaskInfo.thumbnail = null;
        mRunningTaskInfo.numActivities = 1;
        mRunningTaskInfo.numRunning = 2;
        mRunningTaskInfo.description = null;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "ActivityManager.RunningTaskInfo",
        args = {}
    )
    public void testConstructor() {
        new ActivityManager.RunningTaskInfo();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mRunningTaskInfo.describeContents());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {

        Parcel parcel = Parcel.obtain();
        mRunningTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RunningTaskInfo values = ActivityManager.RunningTaskInfo.CREATOR
                .createFromParcel(parcel);
        assertEquals(1, values.id);
        assertNull(values.baseActivity);
        assertNull(values.topActivity);
        assertNull(values.thumbnail);
        assertEquals(1, values.numActivities);
        assertEquals(2, values.numRunning);
        // test thumbnail is not null
        mRunningTaskInfo.thumbnail = Bitmap.createBitmap(480, 320,
                Bitmap.Config.RGB_565);
        parcel = Parcel.obtain();
        mRunningTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        values = ActivityManager.RunningTaskInfo.CREATOR
                .createFromParcel(parcel);
        assertNotNull(values.thumbnail);
        assertEquals(320, values.thumbnail.getHeight());
        assertEquals(480, values.thumbnail.getWidth());
        assertEquals(Bitmap.Config.RGB_565, values.thumbnail.getConfig());

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readFromParcel",
        args = {android.os.Parcel.class}
    )
    public void testReadFromParcel() throws Exception {

        Parcel parcel = Parcel.obtain();
        mRunningTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RunningTaskInfo values = new ActivityManager.RunningTaskInfo();
        values.readFromParcel(parcel);
        assertEquals(1, values.id);
        assertNull(values.baseActivity);
        assertNull(values.topActivity);
        assertNull(values.thumbnail);
        assertEquals(1, values.numActivities);
        assertEquals(2, values.numRunning);
        // test thumbnail is not null
        mRunningTaskInfo.thumbnail = Bitmap.createBitmap(480, 320,
                Bitmap.Config.RGB_565);
        parcel = Parcel.obtain();
        mRunningTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        values.readFromParcel(parcel);
        assertNotNull(values.thumbnail);
        assertEquals(320, values.thumbnail.getHeight());
        assertEquals(480, values.thumbnail.getWidth());
        assertEquals(Bitmap.Config.RGB_565, values.thumbnail.getConfig());
    }

}
