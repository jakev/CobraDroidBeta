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

package android.provider.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.content.ContentResolver;
import android.content.IContentProvider;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.cts.ContactsContract_TestDataBuilder.TestData;
import android.provider.cts.ContactsContract_TestDataBuilder.TestRawContact;
import android.test.InstrumentationTestCase;

@TestTargetClass(Photo.class)
public class ContactsContract_PhotoTest extends InstrumentationTestCase {
    private ContactsContract_TestDataBuilder mBuilder;

    private static final byte[] TEST_PHOTO_DATA = "ABCDEFG".getBytes();
    private static final byte[] EMPTY_TEST_PHOTO_DATA = "".getBytes();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContentResolver contentResolver =
                getInstrumentation().getTargetContext().getContentResolver();
        IContentProvider provider = contentResolver.acquireProvider(ContactsContract.AUTHORITY);
        mBuilder = new ContactsContract_TestDataBuilder(provider);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mBuilder.cleanup();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for photo"
        )
    })

    public void testAddPhoto() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert();
        TestData photoData = rawContact.newDataRow(Photo.CONTENT_ITEM_TYPE)
                .with(Photo.PHOTO, TEST_PHOTO_DATA)
                .insert();

        photoData.load();
        photoData.assertColumn(Photo.RAW_CONTACT_ID, rawContact.getId());
        photoData.assertColumn(Photo.PHOTO, TEST_PHOTO_DATA);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for empty photo"
        )
    })

    public void testAddEmptyPhoto() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert();
        TestData photoData = rawContact.newDataRow(Photo.CONTENT_ITEM_TYPE)
                .with(Photo.PHOTO, EMPTY_TEST_PHOTO_DATA)
                .insert();

        photoData.load();
        photoData.assertColumn(Photo.RAW_CONTACT_ID, rawContact.getId());
        photoData.assertColumn(Photo.PHOTO, EMPTY_TEST_PHOTO_DATA);
    }
}

