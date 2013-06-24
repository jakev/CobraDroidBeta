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

package android.provider.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.MediaStore.Audio.Genres;
import android.test.InstrumentationTestCase;

@TestTargetClass(Genres.class)
public class MediaStore_Audio_GenresTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContentResolver = getInstrumentation().getContext().getContentResolver();
    }

    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document related to the possible values of param volumeName. @throw clause "
            + "should be added in to javadoc when getting uri for internal volume.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Genres.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME), null, null,
                    null, null));

        try {
            assertNotNull(mContentResolver.query(
                    Genres.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null,
                        null, null, null));
            fail("Should throw SQLException as the internal datatbase has no genre");
        } catch (SQLException e) {
            // expected
        }

        // can not accept any other volume names
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Genres.getContentUri(volume), null, null, null, null));
    }

    public void testStoreAudioGenresExternal() {
        // insert
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, "POP");
        Uri uri = mContentResolver.insert(Genres.EXTERNAL_CONTENT_URI, values);
        assertNotNull(uri);

        try {
            // query
            Cursor c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals("POP", c.getString(c.getColumnIndex(Genres.NAME)));
            assertTrue(c.getLong(c.getColumnIndex(Genres._ID)) > 0);
            c.close();

            // update
            values.clear();
            values.put(Genres.NAME, "ROCK");
            assertEquals(1, mContentResolver.update(uri, values, null, null));
            c = mContentResolver.query(uri, null, null, null, null);
            c.moveToFirst();
            assertEquals("ROCK", c.getString(c.getColumnIndex(Genres.NAME)));
            c.close();
        } finally {
            assertEquals(1, mContentResolver.delete(uri, null, null));
        }
    }

    public void testStoreAudioGenresInternal() {
        // the internal database does not have genres
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, "POP");
        Uri uri = mContentResolver.insert(Genres.INTERNAL_CONTENT_URI, values);
        assertNull(uri);
    }
}
