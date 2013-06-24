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
import android.net.Uri;
import android.provider.MediaStore.Audio.Artists;
import android.provider.cts.MediaStoreAudioTestHelper.Audio1;
import android.provider.cts.MediaStoreAudioTestHelper.Audio2;
import android.test.InstrumentationTestCase;

@TestTargetClass(Artists.class)
public class MediaStore_Audio_ArtistsTest extends InstrumentationTestCase {
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
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. This is no "
            + "document which describs possible values of the param volumeName.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Artists.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                    null, null));
        assertNotNull(mContentResolver.query(
                Artists.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                null, null));

        // can not accept any other volume names
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Artists.getContentUri(volume), null, null, null, null));
    }

    public void testStoreAudioArtistsInternal() {
        testStoreAudioArtists(true);
    }

    public void testStoreAudioArtistsExternal() {
        testStoreAudioArtists(false);
    }

    private void testStoreAudioArtists(boolean isInternal) {
        Uri artistsUri = isInternal ? Artists.INTERNAL_CONTENT_URI : Artists.EXTERNAL_CONTENT_URI;
        // do not support insert operation of the artists
        try {
            mContentResolver.insert(artistsUri, new ContentValues());
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // the artist items are inserted when inserting audio media
        Uri uri = isInternal ? Audio1.getInstance().insertToInternal(mContentResolver)
                : Audio1.getInstance().insertToExternal(mContentResolver);

        String selection = Artists.ARTIST + "=?";
        String[] selectionArgs = new String[] { Audio1.ARTIST };
        try {
            // query
            Cursor c = mContentResolver.query(artistsUri, null, selection, selectionArgs, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();

            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Artists.ARTIST)));
            assertTrue(c.getLong(c.getColumnIndex(Artists._ID)) > 0);
            assertNotNull(c.getString(c.getColumnIndex(Artists.ARTIST_KEY)));
            assertEquals(1, c.getInt(c.getColumnIndex(Artists.NUMBER_OF_ALBUMS)));
            assertEquals(1, c.getInt(c.getColumnIndex(Artists.NUMBER_OF_TRACKS)));
            c.close();

            // do not support update operation of the artists
            ContentValues artistValues = new ContentValues();
            artistValues.put(Artists.ARTIST, Audio2.ALBUM);
            try {
                mContentResolver.update(artistsUri, artistValues, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
                // expected
            }

            // do not support delete operation of the artists
            try {
                mContentResolver.delete(artistsUri, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
                // expected
            }
        } finally {
            mContentResolver.delete(uri, null, null);
        }
        // the artist items are deleted when deleting the audio media which belongs to the album
        Cursor c = mContentResolver.query(artistsUri, null, selection, selectionArgs, null);
        assertEquals(0, c.getCount());
        c.close();
    }
}
