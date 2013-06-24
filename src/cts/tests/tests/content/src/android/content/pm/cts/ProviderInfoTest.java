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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.test.AndroidTestCase;

import java.util.Iterator;
import java.util.List;

@TestTargetClass(ProviderInfo.class)
public class ProviderInfoTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String PROVIDER_NAME = "android.content.cts.MockContentProvider";

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ProviderInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ProviderInfo",
            args = {android.content.pm.ProviderInfo.class}
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
        )
    })
    public void testProviderInfo() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        Parcel p = Parcel.obtain();
        // Test ProviderInfo()
        new ProviderInfo();
        // Test other methods
        ApplicationInfo appInfo = pm.getApplicationInfo(PACKAGE_NAME, 0);
        List<ProviderInfo> providers = pm.queryContentProviders(PACKAGE_NAME, appInfo.uid, 0);
        Iterator<ProviderInfo> providerIterator = providers.iterator();
        ProviderInfo current;
        while (providerIterator.hasNext()) {
            current = providerIterator.next();
            if (current.name.equals(PROVIDER_NAME)) {
                checkProviderInfoMethods(current, p);
                break;
            }
        }
    }

    private void checkProviderInfoMethods(ProviderInfo providerInfo, Parcel p) {
        // Test toString, describeContents
        assertNotNull(providerInfo.toString());
        assertEquals(0, providerInfo.describeContents());

        // Test ProviderInfo(ProviderInfo orig)
        ProviderInfo infoFromExisted = new ProviderInfo(providerInfo);
        checkInfoSame(providerInfo, infoFromExisted);

        // Test writeToParcel
        providerInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ProviderInfo infoFromParcel = ProviderInfo.CREATOR.createFromParcel(p);
        checkInfoSame(providerInfo, infoFromParcel);
        p.recycle();
    }

    private void checkInfoSame(ProviderInfo expected, ProviderInfo actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.authority, actual.authority);
        assertEquals(expected.readPermission, actual.readPermission);
        assertEquals(expected.writePermission, actual.writePermission);
        assertEquals(expected.grantUriPermissions, actual.grantUriPermissions);
        assertEquals(expected.uriPermissionPatterns, actual.uriPermissionPatterns);
        assertEquals(expected.multiprocess, actual.multiprocess);
        assertEquals(expected.initOrder, actual.initOrder);
        assertEquals(expected.isSyncable, actual.isSyncable);
    }
}
