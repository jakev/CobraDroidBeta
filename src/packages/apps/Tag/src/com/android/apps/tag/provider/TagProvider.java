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

import com.android.apps.tag.R;
import com.android.apps.tag.provider.TagContract.NdefMessages;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Stores NFC tags in a database. The contract is defined in {@link TagContract}.
 */
public class TagProvider extends SQLiteContentProvider implements TagProviderPipeDataWriter {
    private static final String TAG = "TagProvider";

    private static final int NDEF_MESSAGES = 1000;
    private static final int NDEF_MESSAGES_ID = 1001;

    private static final int NDEF_MESSAGES_ID_MIME = 2002;

    private static final UriMatcher MATCHER;


    private static final Map<String, String> NDEF_MESSAGES_PROJECTION_MAP =
            ImmutableMap.<String, String>builder()
                .put(NdefMessages._ID, NdefMessages._ID)
                .put(NdefMessages.TITLE, NdefMessages.TITLE)
                .put(NdefMessages.BYTES, NdefMessages.BYTES)
                .put(NdefMessages.DATE, NdefMessages.DATE)
                .put(NdefMessages.STARRED, NdefMessages.STARRED)
                .build();

    private Map<String, String> mNdefRecordsMimeProjectionMap;

    static {
        MATCHER = new UriMatcher(0);
        String auth = TagContract.AUTHORITY;

        MATCHER.addURI(auth, "ndef_msgs", NDEF_MESSAGES);
        MATCHER.addURI(auth, "ndef_msgs/#", NDEF_MESSAGES_ID);
        MATCHER.addURI(auth, "ndef_msgs/#/#/mime", NDEF_MESSAGES_ID_MIME);
    }

    @Override
    public boolean onCreate() {
        boolean result = super.onCreate();

        // Build the projection map for the MIME records using a localized display name
        mNdefRecordsMimeProjectionMap = ImmutableMap.<String, String>builder()
                .put(NdefMessages.MIME._ID, NdefMessages.MIME._ID)
                .put(NdefMessages.MIME.SIZE, NdefMessages.MIME.SIZE)
                .put(NdefMessages.MIME.DISPLAY_NAME,
                        "'" + getContext().getString(R.string.mime_display_name) + "' AS "
                        + NdefMessages.MIME.DISPLAY_NAME)
                .build();

        return result;
    }

    @Override
    protected SQLiteOpenHelper getDatabaseHelper(Context context) {
        return new TagDBHelper(context);
    }

    /**
     * Appends one set of selection args to another. This is useful when adding a selection
     * argument to a user provided set.
     */
    public static String[] appendSelectionArgs(String[] originalValues, String[] newValues) {
        if (originalValues == null || originalValues.length == 0) {
            return newValues;
        }
        String[] result = new String[originalValues.length + newValues.length ];
        System.arraycopy(originalValues, 0, result, 0, originalValues.length);
        System.arraycopy(newValues, 0, result, originalValues.length, newValues.length);
        return result;
    }

    /**
     * Concatenates two SQL WHERE clauses, handling empty or null values.
     */
    public static String concatenateWhere(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }

        return "(" + a + ") AND (" + b + ")";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = MATCHER.match(uri);
        switch (match) {
            case NDEF_MESSAGES_ID: {
                selection = concatenateWhere(selection,
                        TagDBHelper.TABLE_NAME_NDEF_MESSAGES + "._id=?");
                selectionArgs = appendSelectionArgs(selectionArgs,
                        new String[] { Long.toString(ContentUris.parseId(uri)) });
                // fall through
            }
            case NDEF_MESSAGES: {
                qb.setTables(TagDBHelper.TABLE_NAME_NDEF_MESSAGES);
                qb.setProjectionMap(NDEF_MESSAGES_PROJECTION_MAP);
                break;
            }

            case NDEF_MESSAGES_ID_MIME: {
                selection = concatenateWhere(selection,
                        TagDBHelper.TABLE_NAME_NDEF_MESSAGES + "._id=?");
                selectionArgs = appendSelectionArgs(selectionArgs,
                        new String[] { Long.toString(ContentUris.parseId(uri)) });
                qb.setTables(TagDBHelper.TABLE_NAME_NDEF_MESSAGES);
                qb.setProjectionMap(mNdefRecordsMimeProjectionMap);
                break;
            }

            default: {
                throw new IllegalArgumentException("unkown uri " + uri);
            }
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), TagContract.AUTHORITY_URI);
        }
        return cursor;
    }

    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        int match = MATCHER.match(uri);
        long id = -1;
        switch (match) {
            case NDEF_MESSAGES: {
                id = db.insert(TagDBHelper.TABLE_NAME_NDEF_MESSAGES, NdefMessages.TITLE, values);
                break;
            }

            default: {
                throw new IllegalArgumentException("unkown uri " + uri);
            }
        }

        if (id >= 0) {
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        int match = MATCHER.match(uri);
        int count = 0;
        switch (match) {
            case NDEF_MESSAGES_ID: {
                selection = concatenateWhere(selection,
                        TagDBHelper.TABLE_NAME_NDEF_MESSAGES + "._id=?");
                selectionArgs = appendSelectionArgs(selectionArgs,
                        new String[] { Long.toString(ContentUris.parseId(uri)) });
                // fall through
            }
            case NDEF_MESSAGES: {
                count = db.update(TagDBHelper.TABLE_NAME_NDEF_MESSAGES, values, selection,
                        selectionArgs);
                break;
            }

            default: {
                throw new IllegalArgumentException("unkown uri " + uri);
            }
        }

        return count;
    }

    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        int match = MATCHER.match(uri);
        int count = 0;
        switch (match) {
            case NDEF_MESSAGES_ID: {
                selection = concatenateWhere(selection,
                        TagDBHelper.TABLE_NAME_NDEF_MESSAGES + "._id=?");
                selectionArgs = appendSelectionArgs(selectionArgs,
                        new String[] { Long.toString(ContentUris.parseId(uri)) });
                // fall through
            }
            case NDEF_MESSAGES: {
                count = db.delete(TagDBHelper.TABLE_NAME_NDEF_MESSAGES, selection, selectionArgs);
                break;
            }

            default: {
                throw new IllegalArgumentException("unkown uri " + uri);
            }
        }

        return count;
    }

    private NdefRecord getRecord(Uri uri) {
        NdefRecord record = null;

        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            cursor = db.query(TagDBHelper.TABLE_NAME_NDEF_MESSAGES,
                    new String[] { NdefMessages.BYTES }, "_id=?",
                    new String[] { uri.getPathSegments().get(1) }, null, null, null, null);
            if (cursor.moveToFirst()) {
                NdefMessage msg = new NdefMessage(cursor.getBlob(0));
                NdefRecord[] records = msg.getRecords();

                int offset = Integer.parseInt(uri.getPathSegments().get(2));

                if (records != null && offset < records.length) {
                    record = records[offset];
                }
            }
        } catch (FormatException e) {
            Log.e(TAG, "Invalid NdefMessage format", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return record;
    }


    @Override
    public String getType(Uri uri) {
        int match = MATCHER.match(uri);
        switch (match) {

            case NDEF_MESSAGES_ID: {
                return NdefMessages.CONTENT_ITEM_TYPE;
            }
            case NDEF_MESSAGES: {
                return NdefMessages.CONTENT_TYPE;
            }

            case NDEF_MESSAGES_ID_MIME: {
                NdefRecord record = getRecord(uri);
                if (record != null) {
                    return new String(record.getType(), Charsets.US_ASCII).toLowerCase();
                }
                return null;
            }

            default: {
                throw new IllegalArgumentException("unknown uri " + uri);
            }
        }
    }

    @Override
    protected void notifyChange() {
        getContext().getContentResolver().notifyChange(TagContract.AUTHORITY_URI, null, false);
    }

    @Override
    public void writeMimeDataToPipe(ParcelFileDescriptor output, Uri uri) {
        NdefRecord record = getRecord(uri);
        if (record == null) return;

        try {
            byte[] data = record.getPayload();
            FileOutputStream os = new FileOutputStream(output.getFileDescriptor());
            os.write(data);
            os.flush();
        } catch (IOException e) {
            Log.e(TAG, "failed to write MIME data to " + uri, e);
        }
    }

    /**
     * A helper function for implementing {@link #openFile}, for
     * creating a data pipe and background thread allowing you to stream
     * generated data back to the client.  This function returns a new
     * ParcelFileDescriptor that should be returned to the caller (the caller
     * is responsible for closing it).
     *
     * @param uri The URI whose data is to be written.
     * @param func Interface implementing the function that will actually
     * stream the data.
     * @return Returns a new ParcelFileDescriptor holding the read side of
     * the pipe.  This should be returned to the caller for reading; the caller
     * is responsible for closing it when done.
     */
    public ParcelFileDescriptor openMimePipe(final Uri uri,
            final TagProviderPipeDataWriter func) throws FileNotFoundException {
        try {
            final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();

            AsyncTask<Object, Object, Object> task = new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    func.writeMimeDataToPipe(fds[1], uri);
                    try {
                        fds[1].close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failure closing pipe", e);
                    }
                    return null;
                }
            };
            task.execute((Object[])null);

            return fds[0];
        } catch (IOException e) {
            throw new FileNotFoundException("failure making pipe");
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Preconditions.checkArgument("r".equals(mode));
        Preconditions.checkArgument(MATCHER.match(uri) == NDEF_MESSAGES_ID_MIME);
        return openMimePipe(uri, this);
    }
}
