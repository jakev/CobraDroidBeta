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

package android.provider.cts;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.provider.Contacts;
import android.provider.Contacts.Settings;
import android.test.InstrumentationTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

import java.util.ArrayList;

@TestTargetClass(android.provider.Contacts.Settings.class)
public class Contacts_SettingsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private IContentProvider mProvider;

    // the backup for the setting tables which we will modified in test cases
    private ArrayList<ContentValues> mSettingBackup;

    @ToBeFixed(explanation = "The URL: content://contacts/settings does not support" +
            " deleting operation, that makes the table's status can't be recovered.")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
        mProvider = mContentResolver.acquireProvider(Contacts.AUTHORITY);
        mSettingBackup = new ArrayList<ContentValues>();

        // backup the current contents in database
//        Cursor cursor = mProvider.query(Settings.CONTENT_URI, null, null, null, null);
//        if (cursor.moveToFirst()) {
//            while (!cursor.isAfterLast()) {
//                ContentValues value = new ContentValues();
//
//                value.put(Settings._ID, cursor.getInt(0));
//                value.put(Settings._SYNC_ACCOUNT, cursor.getString(1));
//                value.put(Settings.KEY, cursor.getString(2));
//                value.put(Settings.VALUE, cursor.getString(3));
//                mSettingBackup.add(value);
//
//                cursor.moveToNext();
//            }
//        }
//        cursor.close();
    }

    @Override
    protected void tearDown() throws Exception {
        // NOTE: because we cannot delete the URL: content://contacts/settings,
        // the contents added by test cases can't be removed.
//        // clear all contents in current database.
//        mProvider.delete(Settings.CONTENT_URI, null, null);
//
//        // recover the old backup contents
//        for (ContentValues value : mSettingBackup) {
//            mProvider.insert(Settings.CONTENT_URI, value);
//        }

        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access setting",
            method = "getSetting",
            args = {android.content.ContentResolver.class, java.lang.String.class, 
                    java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access setting",
            method = "setSetting",
            args = {android.content.ContentResolver.class, java.lang.String.class, 
                    java.lang.String.class, java.lang.String.class}
        )
    })
    public void testAccessSetting() {
        String key1 = "key 1";
        String value1 = "value 1";
        String key2 = "key 2";
        String value2 = "value 2";
        Settings.setSetting(mContentResolver, "account", key1, value1);
        Settings.setSetting(mContentResolver, "account", key2, value2);
        assertEquals(value1, Settings.getSetting(mContentResolver, "account", key1));
        assertEquals(value2, Settings.getSetting(mContentResolver, "account", key2));
        assertNull(Settings.getSetting(mContentResolver, "account", "key not exist"));

        Settings.setSetting(mContentResolver, "account", key1, value2);
        assertEquals(value2, Settings.getSetting(mContentResolver, "account", key1));
    }
}
