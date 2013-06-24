/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CellBroadcastDatabase {
    private static final String TAG = "CellBroadcastDatabase";

    private CellBroadcastDatabase() {}

    static final String DATABASE_NAME = "cell_broadcasts.db";
    static final String TABLE_NAME = "broadcasts";

    static final int DATABASE_VERSION = 1;

    static final class Columns implements BaseColumns {

        private Columns() {}

        /**
         * Message geographical scope.
         * <P>Type: INTEGER</P>
         */
        public static final String GEOGRAPHICAL_SCOPE = "geo_scope";

        /**
         * Message serial number.
         * <P>Type: INTEGER</P>
         */
        public static final String SERIAL_NUMBER = "serial_number";

        /**
         * Message code.
         * <P>Type: INTEGER</P>
         */
        public static final String MESSAGE_CODE = "message_code";

        /**
         * Message identifier.
         * <P>Type: INTEGER</P>
         */
        public static final String MESSAGE_IDENTIFIER = "message_id";

        /**
         * Message language code.
         * <P>Type: TEXT</P>
         */
        public static final String LANGUAGE_CODE = "language";

        /**
         * Message body.
         * <P>Type: TEXT</P>
         */
        public static final String MESSAGE_BODY = "body";

        /**
         * Message delivery time.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DELIVERY_TIME = "date";

        /**
         * Has the message been viewed?
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String MESSAGE_READ = "read";

        /**
         * Query for list view adapter.
         */
        static final String[] QUERY_COLUMNS = {
                _ID,
                GEOGRAPHICAL_SCOPE,
                SERIAL_NUMBER,
                MESSAGE_CODE,
                MESSAGE_IDENTIFIER,
                LANGUAGE_CODE,
                MESSAGE_BODY,
                DELIVERY_TIME,
                MESSAGE_READ,
        };
    }

    /* Column indexes for reading from cursor. */

    static final int COLUMN_ID                  = 0;
    static final int COLUMN_GEOGRAPHICAL_SCOPE  = 1;
    static final int COLUMN_SERIAL_NUMBER       = 2;
    static final int COLUMN_MESSAGE_CODE        = 3;
    static final int COLUMN_MESSAGE_IDENTIFIER  = 4;
    static final int COLUMN_LANGUAGE_CODE       = 5;
    static final int COLUMN_MESSAGE_BODY        = 6;
    static final int COLUMN_DELIVERY_TIME       = 7;
    static final int COLUMN_MESSAGE_READ        = 8;

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Columns.GEOGRAPHICAL_SCOPE + " INTEGER,"
                    + Columns.SERIAL_NUMBER + " INTEGER,"
                    + Columns.MESSAGE_CODE + " INTEGER,"
                    + Columns.MESSAGE_IDENTIFIER + " INTEGER,"
                    + Columns.LANGUAGE_CODE + " TEXT,"
                    + Columns.MESSAGE_BODY + " TEXT,"
                    + Columns.DELIVERY_TIME + " INTEGER,"
                    + Columns.MESSAGE_READ + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // ignored for now
        }
    }

    /**
     * Returns a Cursor for the list view adapter, in reverse chronological order.
     * @param db an open readable database
     * @return the cursor for the list view adapter
     */
    static Cursor getCursor(SQLiteDatabase db) {
        return db.query(false, TABLE_NAME, Columns.QUERY_COLUMNS,
                null, null, null, null, Columns.DELIVERY_TIME + " DESC", null);
    }
}