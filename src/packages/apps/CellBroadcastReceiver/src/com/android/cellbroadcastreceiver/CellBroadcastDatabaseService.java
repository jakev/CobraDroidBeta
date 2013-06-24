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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Service to update the SQLite database to add a new broadcast message,
 * or to delete one or all previously received broadcasts.
 */
public class CellBroadcastDatabaseService extends IntentService {
    private static final String TAG = "CellBroadcastDatabaseService";

    /** Action to insert a new message (passed as CellBroadcastMessage extra). */
    static final String ACTION_INSERT_NEW_BROADCAST = "ACTION_INSERT_NEW_BROADCAST";

    /** Action to delete a single broadcast (row ID passed as extra). */
    static final String ACTION_DELETE_BROADCAST = "ACTION_DELETE_BROADCAST";

    /** Action to mark a broadcast as read by the user (by row ID or delivery time extra). */
    static final String ACTION_MARK_BROADCAST_READ = "ACTION_MARK_BROADCAST_READ";

    /** Action to delete all broadcasts from database (no extras). */
    static final String ACTION_DELETE_ALL_BROADCASTS = "ACTION_DELETE_ALL_BROADCASTS";

    /** Identifier for getExtra() for row ID to delete or mark read. */
    public static final String DATABASE_ROW_ID_EXTRA =
            "com.android.cellbroadcastreceiver.DATABASE_ROW_ID";

    /** Identifier for getExtra() for delivery time of broadcast to mark read. */
    public static final String DATABASE_DELIVERY_TIME_EXTRA =
            "com.android.cellbroadcastreceiver.DATABASE_DELIVERY_TIME";

    private SQLiteDatabase mBroadcastDb;

    /** Callback for the active list activity when the contents change. */
    private static CellBroadcastListActivity sActiveListActivity;

    public CellBroadcastDatabaseService() {
        super(TAG);     // use class name for worker thread name
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mBroadcastDb == null) {
            CellBroadcastDatabase.DatabaseHelper helper =
                    new CellBroadcastDatabase.DatabaseHelper(this);
            mBroadcastDb = helper.getWritableDatabase();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBroadcastDb != null) {
            mBroadcastDb.close();
            mBroadcastDb = null;
        }
    }

    static void setActiveListActivity(CellBroadcastListActivity activity) {
        sActiveListActivity = activity;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // TODO: security check to detect malicious broadcast injections
        String action = intent.getAction();
        boolean notifyActiveListActivity = false;
        if (ACTION_INSERT_NEW_BROADCAST.equals(action)) {
            CellBroadcastMessage cbm = intent.getParcelableExtra(
                    CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);
            if (cbm == null) {
                Log.e(TAG, "ACTION_INSERT_NEW_BROADCAST with no CB message extra");
                return;
            }

            ContentValues cv = cbm.getContentValues();
            long rowId = mBroadcastDb.insert(CellBroadcastDatabase.TABLE_NAME, null, cv);
            if (rowId == -1) {
                Log.e(TAG, "failed to insert new broadcast into database!");
            } else {
                notifyActiveListActivity = true;
            }
        } else if (ACTION_DELETE_BROADCAST.equals(action)) {
            long rowId = intent.getLongExtra(DATABASE_ROW_ID_EXTRA, -1);
            if (rowId == -1) {
                Log.e(TAG, "ACTION_DELETE_BROADCAST missing row ID to delete");
                return;
            }

            int rowCount = mBroadcastDb.delete(CellBroadcastDatabase.TABLE_NAME,
                    CellBroadcastDatabase.Columns._ID + "=?",
                    new String[]{Long.toString(rowId)});
            if (rowCount != 0) {
                notifyActiveListActivity = true;
            }
        } else if (ACTION_DELETE_ALL_BROADCASTS.equals(action)) {
            mBroadcastDb.delete(CellBroadcastDatabase.TABLE_NAME, null, null);
            notifyActiveListActivity = true;
        } else if (ACTION_MARK_BROADCAST_READ.equals(action)) {
            long rowId = intent.getLongExtra(DATABASE_ROW_ID_EXTRA, -1);
            long deliveryTime = intent.getLongExtra(DATABASE_DELIVERY_TIME_EXTRA, -1);
            if (rowId == -1 && deliveryTime == -1) {
                Log.e(TAG, "ACTION_MARK_BROADCAST_READ missing row ID or delivery time");
                return;
            }
            ContentValues cv = new ContentValues(1);
            cv.put(CellBroadcastDatabase.Columns.MESSAGE_READ, 1);
            int rowCount;
            if (rowId != -1) {
                rowCount = mBroadcastDb.update(CellBroadcastDatabase.TABLE_NAME, cv,
                    CellBroadcastDatabase.Columns._ID + "=?",
                    new String[]{Long.toString(rowId)});
            } else {
                rowCount = mBroadcastDb.update(CellBroadcastDatabase.TABLE_NAME, cv,
                    CellBroadcastDatabase.Columns.DELIVERY_TIME + "=?",
                    new String[]{Long.toString(deliveryTime)});
            }
            if (rowCount != 0) {
                notifyActiveListActivity = true;
            }
        } else {
            Log.e(TAG, "ignoring unexpected Intent with action " + action);
        }
        if (notifyActiveListActivity && sActiveListActivity != null) {
            sActiveListActivity.databaseContentChanged();
        }
    }
}
