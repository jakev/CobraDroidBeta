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

package com.android.apps.tag.provider;

import com.android.apps.tag.provider.TagContract.NdefMessages;
import com.google.common.annotations.VisibleForTesting;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database utilities for the saved tags.
 */
public class TagDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tags.db";
    private static final int DATABASE_VERSION = 16;

    public static final String TABLE_NAME_NDEF_MESSAGES = "ndef_msgs";

    TagDBHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    @VisibleForTesting
    TagDBHelper(Context context, String dbFile) {
        super(context, dbFile, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME_NDEF_MESSAGES + " (" +
                NdefMessages._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NdefMessages.DATE + " INTEGER NOT NULL, " +
                NdefMessages.TITLE + " TEXT NOT NULL DEFAULT ''," +
                NdefMessages.BYTES + " BLOB NOT NULL, " +
                NdefMessages.STARRED + " INTEGER NOT NULL DEFAULT 0," +  // boolean
                NdefMessages.IS_MY_TAG + " INTEGER NOT NULL DEFAULT 0" + // boolean
                ");");
    }

    /**
     * Drop data and recreate everything.
     */
    private void recreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_NDEF_MESSAGES);
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 14) {
            // Pre-release version.
            recreate(db);
            db.setVersion(newVersion);
        } else if (oldVersion == 14) {
            // GB release - does not have My tags yet.
            db.execSQL("ALTER TABLE " + TABLE_NAME_NDEF_MESSAGES + " ADD COLUMN "
                    + NdefMessages.IS_MY_TAG + " INTEGER NOT NULL DEFAULT 0");
            db.setVersion(newVersion);
        } else if (oldVersion < DATABASE_VERSION) {
            // Unreleased version with improperly formatted tags.
            db.execSQL("DELETE FROM " + TABLE_NAME_NDEF_MESSAGES + " WHERE "
                    + NdefMessages.IS_MY_TAG + "=1");
            db.setVersion(newVersion);
        }
    }
}
