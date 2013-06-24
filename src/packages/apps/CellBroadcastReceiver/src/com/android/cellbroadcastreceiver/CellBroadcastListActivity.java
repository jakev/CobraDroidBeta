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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity provides a list view of received cell broadcasts.
 */
public class CellBroadcastListActivity extends ListActivity {
    private static final String TAG = "CellBroadcastListActivity";

    // IDs of the main menu items.
    public static final int MENU_DELETE_ALL           = 3;
    public static final int MENU_PREFERENCES          = 4;

    // IDs of the context menu items for the list of broadcasts.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;

    private CellBroadcastListAdapter mListAdapter;

    private SQLiteDatabase mBroadcastDb;

    private Cursor mAdapterCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cell_broadcast_list_screen);

        ListView listView = getListView();
        listView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);

        if (mBroadcastDb == null) {
            CellBroadcastDatabase.DatabaseHelper helper =
                    new CellBroadcastDatabase.DatabaseHelper(this);
            mBroadcastDb = helper.getReadableDatabase();
        }

        if (mAdapterCursor == null) {
            mAdapterCursor = CellBroadcastDatabase.getCursor(mBroadcastDb);
        }

        mListAdapter = new CellBroadcastListAdapter(this, mAdapterCursor);
        setListAdapter(mListAdapter);

        CellBroadcastDatabaseService.setActiveListActivity(this);

        parseIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        if (mAdapterCursor != null) {
            mAdapterCursor.close();
            mAdapterCursor = null;
        }
        if (mBroadcastDb != null) {
            mBroadcastDb.close();
            mBroadcastDb = null;
        }
        CellBroadcastDatabaseService.setActiveListActivity(null);
        super.onDestroy();
    }

    /** Callback from CellBroadcastDatabaseService after content changes. */
    void databaseContentChanged() {
        runOnUiThread(new Runnable() {
            public void run() {
                mAdapterCursor.requery();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO: how do multiple messages stack together?
        // removeDialog(DIALOG_SHOW_MESSAGE);
        parseIntent(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (mListAdapter.getCount() > 0) {
            menu.add(0, MENU_DELETE_ALL, 0, R.string.menu_delete_all).setIcon(
                    android.R.drawable.ic_menu_delete);
        }

        menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences).setIcon(
                android.R.drawable.ic_menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_DELETE_ALL:
                confirmDeleteThread(-1);
                break;

            case MENU_PREFERENCES:
                Intent intent = new Intent(this, CellBroadcastSettings.class);
                startActivityIfNeeded(intent, -1);
                break;

            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
            showDialogAndMarkRead(cursor);
        }
    }

    private final OnCreateContextMenuListener mOnCreateContextMenuListener =
            new OnCreateContextMenuListener() {
                public void onCreateContextMenu(ContextMenu menu, View v,
                        ContextMenuInfo menuInfo) {
                    menu.add(0, MENU_VIEW, 0, R.string.menu_view);
                    menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
                }
            };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
            switch (item.getItemId()) {
                case MENU_DELETE:
                    confirmDeleteThread(cursor.getLong(CellBroadcastDatabase.COLUMN_ID));
                    break;

                case MENU_VIEW:
                    showDialogAndMarkRead(cursor);
                    break;

                default:
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void showDialogAndMarkRead(Cursor cursor) {
        CellBroadcastMessage cbm = CellBroadcastMessage.createFromCursor(cursor);
        // show emergency alerts with the warning icon, but don't play alert tone
        CellBroadcastAlertDialog dialog = new CellBroadcastAlertDialog(this,
                cbm.getDialogTitleResource(), cbm.getMessageBody(),
                cbm.isPublicAlertMessage(), cbm.getDeliveryTime());
        dialog.show();
    }

    /**
     * Start the process of putting up a dialog to confirm deleting a broadcast.
     * @param rowId the row ID of the broadcast to delete, or -1 to delete all broadcasts
     */
    public void confirmDeleteThread(long rowId) {
        DeleteThreadListener listener = new DeleteThreadListener(rowId);
        confirmDeleteThreadDialog(listener, (rowId == -1), this);
    }

    /**
     * Build and show the proper delete broadcast dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting a single broadcast or all broadcasts.
     * @param listener gets called when the delete button is pressed
     * @param deleteAll whether to show a single thread or all threads UI
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(DeleteThreadListener listener,
            boolean deleteAll, Context context) {
        View contents = View.inflate(context, R.layout.delete_broadcast_dialog_view, null);
        TextView msg = (TextView)contents.findViewById(R.id.message);
        msg.setText(deleteAll
                ? R.string.confirm_delete_all_broadcasts
                        : R.string.confirm_delete_broadcast);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_dialog_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(true)
        .setPositiveButton(R.string.button_delete, listener)
        .setNegativeButton(R.string.button_cancel, null)
        .setView(contents)
        .show();
    }

    public class DeleteThreadListener implements OnClickListener {
        private final long mRowId;

        public DeleteThreadListener(long rowId) {
            mRowId = rowId;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            if (mRowId != -1) {
                // delete from database on a separate service thread
                Intent dbWriteIntent = new Intent(CellBroadcastListActivity.this,
                        CellBroadcastDatabaseService.class);
                dbWriteIntent.setAction(CellBroadcastDatabaseService.ACTION_DELETE_BROADCAST);
                dbWriteIntent.putExtra(CellBroadcastDatabaseService.DATABASE_ROW_ID_EXTRA, mRowId);
                startService(dbWriteIntent);
            } else {
                // delete from database on a separate service thread
                Intent dbWriteIntent = new Intent(CellBroadcastListActivity.this,
                        CellBroadcastDatabaseService.class);
                dbWriteIntent.setAction(CellBroadcastDatabaseService.ACTION_DELETE_ALL_BROADCASTS);
                startService(dbWriteIntent);
            }
            dialog.dismiss();
        }
    }

    private void parseIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        CellBroadcastMessage cbm = extras.getParcelable(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);
        int notificationId = extras.getInt(CellBroadcastAlertService.SMS_CB_NOTIFICATION_ID_EXTRA);

        // Dismiss the notification that brought us here.
        NotificationManager notificationManager =
            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);

        boolean isEmergencyAlert = cbm.isPublicAlertMessage();

        CellBroadcastAlertDialog dialog = new CellBroadcastAlertDialog(this,
                cbm.getDialogTitleResource(), cbm.getMessageBody(),
                isEmergencyAlert, cbm.getDeliveryTime());
        dialog.show();
    }
}
