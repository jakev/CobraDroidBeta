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
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Genres.Members;
import android.provider.cts.MediaStoreAudioTestHelper.Audio1;
import android.provider.cts.MediaStoreAudioTestHelper.Audio2;
import android.test.InstrumentationTestCase;

@TestTargetClass(Members.class)
public class MediaStore_Audio_Genres_MembersTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;

    private long mAudioIdOfJam;

    private long mAudioIdOfJamLive;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContentResolver = getInstrumentation().getContext().getContentResolver();
        Uri uri = Audio1.getInstance().insertToExternal(mContentResolver);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mAudioIdOfJam = c.getLong(c.getColumnIndex(Media._ID));
        c.close();

        uri = Audio2.getInstance().insertToExternal(mContentResolver);
        c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mAudioIdOfJamLive = c.getLong(c.getColumnIndex(Media._ID));
        c.close();
    }

    @Override
    protected void tearDown() throws Exception {
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mAudioIdOfJam, null);
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mAudioIdOfJamLive,
                null);
        super.tearDown();
    }

    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class, long.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document related to the possible values of param volumeName. @throw clause "
            + "should be added in to javadoc when getting uri for internal volume.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Members.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME, 1), null,
                    null, null, null));

        try {
            assertNotNull(mContentResolver.query(
                    Members.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME, 1), null,
                        null, null, null));
            fail("Should throw SQLException as the internal datatbase has no genre");
        } catch (SQLException e) {
            // expected
        }

        // can not accept any other volume names
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Members.getContentUri(volume, 1), null, null, null,
                null));
    }

    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain the column Members.ALBUM_ART, Members.GENRE_ID and  Members.AUDIO_ID.")
    public void testStoreAudioGenresMembersExternal() {
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, Audio1.GENRE);
        Uri uri = mContentResolver.insert(Genres.EXTERNAL_CONTENT_URI, values);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();

        long genreId = c.getLong(c.getColumnIndex(Genres._ID));
        c.close();

        // insert audio as the member of the genre
        values.clear();
        values.put(Members.AUDIO_ID, mAudioIdOfJam);
        Uri membersUri = Members.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME,
                genreId);
        assertNotNull(mContentResolver.insert(membersUri, values));

        try {
            // query
            c = mContentResolver.query(membersUri, null, null, null, null);
            // the ALBUM_ART column does not exist
            try {
                c.getColumnIndexOrThrow(Members.ALBUM_ART);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.ALBUM_ART\" in the table");
            } catch (IllegalArgumentException e) {
                // expected
            }

            try {
                c.getColumnIndexOrThrow(Members.AUDIO_ID);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.AUDIO_ID\" in the table");
            } catch (IllegalArgumentException e) {
                // expected
            }

            try {
                c.getColumnIndexOrThrow(Members.GENRE_ID);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.GENRE_ID\" in the table");
            } catch (IllegalArgumentException e) {
                // expected
            }

            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertTrue(c.getLong(c.getColumnIndex(Members._ID)) > 0);
            assertEquals(Audio1.EXTERNAL_DATA, c.getString(c.getColumnIndex(Members.DATA)));
            assertTrue(c.getLong(c.getColumnIndex(Members.DATE_ADDED)) > 0);
            assertEquals(Audio1.DATE_MODIFIED, c.getLong(c.getColumnIndex(Members.DATE_MODIFIED)));
            assertEquals(Audio1.FILE_NAME, c.getString(c.getColumnIndex(Members.DISPLAY_NAME)));
            assertEquals(Audio1.MIME_TYPE, c.getString(c.getColumnIndex(Members.MIME_TYPE)));
            assertEquals(Audio1.SIZE, c.getInt(c.getColumnIndex(Members.SIZE)));
            assertEquals(Audio1.TITLE, c.getString(c.getColumnIndex(Members.TITLE)));
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Members.ALBUM)));
            String albumKey = c.getString(c.getColumnIndex(Members.ALBUM_KEY));
            assertNotNull(albumKey);
            long albumId = c.getLong(c.getColumnIndex(Members.ALBUM_ID));
            assertTrue(albumId > 0);
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Members.ARTIST)));
            String artistKey = c.getString(c.getColumnIndex(Members.ARTIST_KEY));
            assertNotNull(artistKey);
            long artistId = c.getLong(c.getColumnIndex(Members.ARTIST_ID));
            assertTrue(artistId > 0);
            assertEquals(Audio1.COMPOSER, c.getString(c.getColumnIndex(Members.COMPOSER)));
            assertEquals(Audio1.DURATION, c.getLong(c.getColumnIndex(Members.DURATION)));
            assertEquals(Audio1.IS_ALARM, c.getInt(c.getColumnIndex(Members.IS_ALARM)));
            assertEquals(Audio1.IS_MUSIC, c.getInt(c.getColumnIndex(Members.IS_MUSIC)));
            assertEquals(Audio1.IS_NOTIFICATION,
                    c.getInt(c.getColumnIndex(Members.IS_NOTIFICATION)));
            assertEquals(Audio1.IS_RINGTONE, c.getInt(c.getColumnIndex(Members.IS_RINGTONE)));
            assertEquals(Audio1.TRACK, c.getInt(c.getColumnIndex(Members.TRACK)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Members.YEAR)));
            String titleKey = c.getString(c.getColumnIndex(Members.TITLE_KEY));
            assertNotNull(titleKey);
            c.close();

            // update the member
            values.clear();
            values.put(Members.AUDIO_ID, mAudioIdOfJamLive);
            try {
                mContentResolver.update(membersUri, values, null, null);
                fail("Should throw SQLException because there is no column with name "
                        + "\"Members.AUDIO_ID\" in the table");
            } catch (SQLException e) {
                // expected
            }

            // delete the member
            try {
                mContentResolver.delete(membersUri, null, null);
                fail("Should throw SQLException because there is no column with name "
                        + "\"Members.GENRE_ID\" in the table");
            } catch (SQLException e) {
                // expected
            }
        } finally {
            // the members are deleted when deleting the genre which they belong to
            mContentResolver.delete(Genres.EXTERNAL_CONTENT_URI, Genres._ID + "=" + genreId, null);
            c = mContentResolver.query(membersUri, null, null, null, null);
            assertEquals(0, c.getCount());
            c.close();
        }
    }

    public void testStoreAudioGenresMembersInternal() {
        // the internal database can not have genres
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, Audio1.GENRE);
        Uri uri = mContentResolver.insert(Genres.INTERNAL_CONTENT_URI, values);
        assertNull(uri);
    }
}
