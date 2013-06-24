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
 * limitations under the License
 */

package com.android.apps.tag.provider;

import com.android.apps.tag.message.NdefMessageParser;
import com.android.apps.tag.message.ParsedNdefMessage;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.provider.OpenableColumns;

import java.util.Locale;

public class TagContract {
    public static final String AUTHORITY = "com.android.apps.tag";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class NdefMessages {
        /**
         * Utility class, cannot be instantiated.
         */
        private NdefMessages() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                AUTHORITY_URI.buildUpon().appendPath("ndef_msgs").build();

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * NDEF messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/ndef_msg";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * NDEF message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/ndef_msg";

        // columns
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String BYTES = "bytes";
        public static final String DATE = "date";
        public static final String STARRED = "starred";
        public static final String IS_MY_TAG = "mytag";

        public static class MIME implements OpenableColumns {
            public static final String CONTENT_DIRECTORY_MIME = "mime";
            public static final String _ID = "_id";
        }


        /**
         * Converts an NdefMessage to ContentValues that can be insrted into this table.
         */
        public static ContentValues toValues(Context context, NdefMessage msg, boolean isStarred,
                boolean isMyTag, long date) {
            ParsedNdefMessage parsedMsg = NdefMessageParser.parse(msg);
            ContentValues values = new ContentValues();
            values.put(BYTES, msg.toByteArray());
            values.put(DATE, date);
            values.put(STARRED, isStarred ? 1 : 0);
            values.put(IS_MY_TAG, isMyTag ? 1 : 0);
            values.put(TITLE, parsedMsg.getSnippet(context, Locale.getDefault()));
            return values;
        }
    }
}
