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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.provider.cts.MediaStoreAudioTestHelper.Audio1;
import android.provider.cts.MediaStoreAudioTestHelper.Audio2;
import android.test.InstrumentationTestCase;

@TestTargetClass(Members.class)
public class MediaStore_Audio_Playlists_MembersTest extends InstrumentationTestCase {
    private String[] mAudioProjection = {
            Members._ID,
            Members.ALBUM,
            Members.ALBUM_ID,
            Members.ALBUM_KEY,
            Members.ARTIST,
            Members.ARTIST_ID,
            Members.ARTIST_KEY,
            Members.COMPOSER,
            Members.DATA,
            Members.DATE_ADDED,
            Members.DATE_MODIFIED,
            Members.DISPLAY_NAME,
            Members.DURATION,
            Members.IS_ALARM,
            Members.IS_MUSIC,
            Members.IS_NOTIFICATION,
            Members.IS_RINGTONE,
            Members.MIME_TYPE,
            Members.SIZE,
            Members.TITLE,
            Members.TITLE_KEY,
            Members.TRACK,
            Members.YEAR,
    };

    private String[] mMembersProjection = {
            Members._ID,
            Members.AUDIO_ID,
            Members.PLAYLIST_ID,
            Members.PLAY_ORDER,
            Members.ALBUM,
            Members.ALBUM_ID,
            Members.ALBUM_KEY,
            Members.ARTIST,
            Members.ARTIST_ID,
            Members.ARTIST_KEY,
            Members.COMPOSER,
            Members.DATA,
            Members.DATE_ADDED,
            Members.DATE_MODIFIED,
            Members.DISPLAY_NAME,
            Members.DURATION,
            Members.IS_ALARM,
            Members.IS_MUSIC,
            Members.IS_NOTIFICATION,
            Members.IS_RINGTONE,
            Members.MIME_TYPE,
            Members.SIZE,
            Members.TITLE,
            Members.TITLE_KEY,
            Members.TRACK,
            Members.YEAR,
    };

    private ContentResolver mContentResolver;

    private long mIdOfAudio1;

    private long mIdOfAudio2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContentResolver = getInstrumentation().getContext().getContentResolver();
        Uri uri = Audio1.getInstance().insertToExternal(mContentResolver);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mIdOfAudio1 = c.getLong(c.getColumnIndex(Media._ID));
        c.close();

        uri = Audio2.getInstance().insertToExternal(mContentResolver);
        c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mIdOfAudio2 = c.getLong(c.getColumnIndex(Media._ID));
        c.close();
    }

    @Override
    protected void tearDown() throws Exception {
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mIdOfAudio1, null);
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mIdOfAudio2, null);
        super.tearDown();
    }

    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class, long.class}
    )
    public void testGetContentUri() {
        assertEquals("content://media/external/audio/playlists/1337/members",
                Members.getContentUri("external", 1337).toString());
        assertEquals("content://media/internal/audio/playlists/3007/members",
                Members.getContentUri("internal", 3007).toString());
    }

    public void testStoreAudioPlaylistsMembersExternal() {
        ContentValues values = new ContentValues();
        values.put(Playlists.NAME, "My favourites");
        values.put(Playlists.DATA, "");
        long dateAdded = System.currentTimeMillis();
        values.put(Playlists.DATE_ADDED, dateAdded);
        long dateModified = System.currentTimeMillis();
        values.put(Playlists.DATE_MODIFIED, dateModified);
        // insert
        Uri uri = mContentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, values);
        assertNotNull(uri);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        long playlistId = c.getLong(c.getColumnIndex(Playlists._ID));

        // insert audio as the member of the playlist
        values.clear();
        values.put(Members.AUDIO_ID, mIdOfAudio1);
        values.put(Members.PLAY_ORDER, 1);
        Uri membersUri = Members.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME,
                playlistId);
        Uri audioUri = mContentResolver.insert(membersUri, values);
        assertNotNull(audioUri);
        assertEquals(Uri.withAppendedPath(membersUri, Long.toString(mIdOfAudio1)), audioUri);

        try {
            // query the audio info
            c = mContentResolver.query(audioUri, mAudioProjection, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            long memberId = c.getLong(c.getColumnIndex(Members._ID));
            assertTrue(memberId > 0);
            assertEquals(Audio1.EXTERNAL_DATA, c.getString(c.getColumnIndex(Members.DATA)));
            assertTrue(c.getLong(c.getColumnIndex(Members.DATE_ADDED)) > 0);
            assertEquals(Audio1.DATE_MODIFIED, c.getLong(c.getColumnIndex(Members.DATE_MODIFIED)));
            assertEquals(Audio1.FILE_NAME, c.getString(c.getColumnIndex(Members.DISPLAY_NAME)));
            assertEquals(Audio1.MIME_TYPE, c.getString(c.getColumnIndex(Members.MIME_TYPE)));
            assertEquals(Audio1.SIZE, c.getInt(c.getColumnIndex(Members.SIZE)));
            assertEquals(Audio1.TITLE, c.getString(c.getColumnIndex(Members.TITLE)));
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Members.ALBUM)));
            assertNotNull(c.getString(c.getColumnIndex(Members.ALBUM_KEY)));
            assertTrue(c.getLong(c.getColumnIndex(Members.ALBUM_ID)) > 0);
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Members.ARTIST)));
            assertNotNull(c.getString(c.getColumnIndex(Members.ARTIST_KEY)));
            assertTrue(c.getLong(c.getColumnIndex(Members.ARTIST_ID)) > 0);
            assertEquals(Audio1.COMPOSER, c.getString(c.getColumnIndex(Members.COMPOSER)));
            assertEquals(Audio1.DURATION, c.getLong(c.getColumnIndex(Members.DURATION)));
            assertEquals(Audio1.IS_ALARM, c.getInt(c.getColumnIndex(Members.IS_ALARM)));
            assertEquals(Audio1.IS_MUSIC, c.getInt(c.getColumnIndex(Members.IS_MUSIC)));
            assertEquals(Audio1.IS_NOTIFICATION,
                    c.getInt(c.getColumnIndex(Members.IS_NOTIFICATION)));
            assertEquals(Audio1.IS_RINGTONE, c.getInt(c.getColumnIndex(Members.IS_RINGTONE)));
            assertEquals(Audio1.TRACK, c.getInt(c.getColumnIndex(Members.TRACK)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Members.YEAR)));
            assertNotNull(c.getString(c.getColumnIndex(Members.TITLE_KEY)));
            c.close();

            // query the play order of the audio
            c = mContentResolver.query(membersUri, new String[] { Members.PLAY_ORDER },
                    Members.AUDIO_ID + "=" + mIdOfAudio1, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals(1, c.getInt(c.getColumnIndex(Members.PLAY_ORDER)));
            c.close();

            // update the member
            values.clear();
            values.put(Members.PLAY_ORDER, 2);
            values.put(Members.AUDIO_ID, mIdOfAudio2);
            int result = mContentResolver.update(membersUri, values, Members.AUDIO_ID + "="
                    + mIdOfAudio1, null);
            assertEquals(1, result);

            // query all info
            c = mContentResolver.query(membersUri, mMembersProjection, null, null,
                    Members.DEFAULT_SORT_ORDER);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals(2, c.getInt(c.getColumnIndex(Members.PLAY_ORDER)));
            assertEquals(memberId, c.getLong(c.getColumnIndex(Members._ID)));
            assertEquals(Audio2.EXTERNAL_DATA, c.getString(c.getColumnIndex(Members.DATA)));
            assertTrue(c.getLong(c.getColumnIndex(Members.DATE_ADDED)) > 0);
            assertEquals(Audio2.DATE_MODIFIED, c.getLong(c.getColumnIndex(Members.DATE_MODIFIED)));
            assertEquals(Audio2.FILE_NAME, c.getString(c.getColumnIndex(Members.DISPLAY_NAME)));
            assertEquals(Audio2.MIME_TYPE, c.getString(c.getColumnIndex(Members.MIME_TYPE)));
            assertEquals(Audio2.SIZE, c.getInt(c.getColumnIndex(Members.SIZE)));
            assertEquals(Audio2.TITLE, c.getString(c.getColumnIndex(Members.TITLE)));
            assertEquals(Audio2.ALBUM, c.getString(c.getColumnIndex(Members.ALBUM)));
            assertNotNull(c.getString(c.getColumnIndex(Members.ALBUM_KEY)));
            assertTrue(c.getLong(c.getColumnIndex(Members.ALBUM_ID)) > 0);
            assertEquals(Audio2.ARTIST, c.getString(c.getColumnIndex(Members.ARTIST)));
            assertNotNull(c.getString(c.getColumnIndex(Members.ARTIST_KEY)));
            assertTrue(c.getLong(c.getColumnIndex(Members.ARTIST_ID)) > 0);
            assertEquals(Audio2.COMPOSER, c.getString(c.getColumnIndex(Members.COMPOSER)));
            assertEquals(Audio2.DURATION, c.getLong(c.getColumnIndex(Members.DURATION)));
            assertEquals(Audio2.IS_ALARM, c.getInt(c.getColumnIndex(Members.IS_ALARM)));
            assertEquals(Audio2.IS_MUSIC, c.getInt(c.getColumnIndex(Members.IS_MUSIC)));
            assertEquals(Audio2.IS_NOTIFICATION,
                    c.getInt(c.getColumnIndex(Members.IS_NOTIFICATION)));
            assertEquals(Audio2.IS_RINGTONE, c.getInt(c.getColumnIndex(Members.IS_RINGTONE)));
            assertEquals(Audio2.TRACK, c.getInt(c.getColumnIndex(Members.TRACK)));
            assertEquals(Audio2.YEAR, c.getInt(c.getColumnIndex(Members.YEAR)));
            assertNotNull(c.getString(c.getColumnIndex(Members.TITLE_KEY)));
            c.close();

            // delete the member
            result = mContentResolver.delete(membersUri, null, null);
            assertEquals(1, result);
        } finally {
            // delete the playlist
            mContentResolver.delete(Playlists.EXTERNAL_CONTENT_URI,
                    Playlists._ID + "=" + playlistId, null);
        }
    }

    public void testStoreAudioPlaylistsMembersInternal() {
        // the internal database does not have play lists
        ContentValues values = new ContentValues();
        values.put(Playlists.NAME, "My favourites");
        values.put(Playlists.DATA, "/data/data/com.android.cts.stub/files/my_favorites.pl");
        long dateAdded = System.currentTimeMillis();
        values.put(Playlists.DATE_ADDED, dateAdded);
        long dateModified = System.currentTimeMillis();
        values.put(Playlists.DATE_MODIFIED, dateModified);
        Uri uri = mContentResolver.insert(Playlists.INTERNAL_CONTENT_URI, values);
        assertNull(uri);
    }
}
