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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.UserDictionary;
import android.test.AndroidTestCase;

import java.util.ArrayList;

@TestTargetClass(android.provider.UserDictionary.Words.class)
public class UserDictionary_WordsTest extends AndroidTestCase {

    private Context mContext;
    private ContentResolver mContentResolver;
    private ArrayList<Uri> mAddedBackup;

    private static final String[] WORDS_PROJECTION = new String[] {
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE };

    private static final int ID_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int FREQUENCY_INDEX = 2;
    private static final int LOCALE_INDEX = 3;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContext = getContext();
        mContentResolver = mContext.getContentResolver();

        mAddedBackup = new ArrayList<Uri>();
    }

    @Override
    protected void tearDown() throws Exception {
        for (Uri row : mAddedBackup) {
            mContentResolver.delete(row, null, null);
        }
        mAddedBackup.clear();

        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addWord",
        args = {Context.class, String.class, int.class, int.class}
    )
    public void testAddWord() throws RemoteException {
        Cursor cursor;

        String word = "UserDictionary_WordsTest";
        int frequency = 1;
        UserDictionary.Words.addWord(getContext(), word, frequency,
                UserDictionary.Words.LOCALE_TYPE_ALL);
        cursor = mContentResolver.query(
                UserDictionary.Words.CONTENT_URI,
                WORDS_PROJECTION,
                UserDictionary.Words.WORD + "='" + word + "'", null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(word, cursor.getString(WORD_INDEX));
        assertEquals(frequency, cursor.getInt(FREQUENCY_INDEX));
        assertNull(cursor.getString(LOCALE_INDEX));
        mAddedBackup.add(
                Uri.withAppendedPath(UserDictionary.Words.CONTENT_URI, cursor.getString(ID_INDEX)));
    }
}
