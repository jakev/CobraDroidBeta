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

import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.test.AndroidTestCase;

@TestTargetClass(ConfigurationInfo.class)
public class ConfigurationInfoTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ConfigurationInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ConfigurationInfo",
            args = {android.content.pm.ConfigurationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test toString",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test writeToParcel",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        )
    })
    public void testConfigPreferences() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();

        // Test constructors
        new ConfigurationInfo();
        PackageInfo pkgInfo = pm.getPackageInfo(getContext().getPackageName(),
                PackageManager.GET_CONFIGURATIONS);
        ConfigurationInfo[] configInfoArray = pkgInfo.configPreferences;
        assertTrue(configInfoArray.length > 0);
        ConfigurationInfo configInfo = configInfoArray[0];
        ConfigurationInfo infoFromExisted = new ConfigurationInfo(configInfo);
        checkInfoSame(configInfo, infoFromExisted);

        // Test toString, describeContents
        assertEquals(0, configInfo.describeContents());
        assertNotNull(configInfo.toString());

        // Test writeToParcel
        Parcel p = Parcel.obtain();
        configInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ConfigurationInfo infoFromParcel = ConfigurationInfo.CREATOR.createFromParcel(p);
        checkInfoSame(configInfo, infoFromParcel);
        p.recycle();
    }

    private void checkInfoSame(ConfigurationInfo expected, ConfigurationInfo actual) {
        assertEquals(expected.reqKeyboardType, actual.reqKeyboardType);
        assertEquals(expected.reqTouchScreen, actual.reqTouchScreen);
        assertEquals(expected.reqInputFeatures, actual.reqInputFeatures);
        assertEquals(expected.reqNavigation, actual.reqNavigation);
    }
}
