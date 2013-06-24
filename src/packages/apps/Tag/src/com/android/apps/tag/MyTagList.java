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

package com.android.apps.tag;

import com.android.apps.tag.provider.TagContract.NdefMessages;
import com.android.apps.tag.record.RecordEditInfo;
import com.android.apps.tag.record.TextRecord;
import com.android.apps.tag.record.UriRecord;
import com.android.apps.tag.record.VCardRecord;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Displays the list of tags that can be set as "My tag", and allows the user to select the
 * active tag that the device shares.
 */
public class MyTagList
        extends Activity
        implements OnItemClickListener, View.OnClickListener,
                   TagService.SaveCallbacks,
                   DialogInterface.OnClickListener {

    static final String TAG = "TagList";

    private static final int REQUEST_EDIT = 0;
    private static final int DIALOG_ID_SELECT_ACTIVE_TAG = 0;
    private static final int DIALOG_ID_ADD_NEW_TAG = 1;

    private static final String BUNDLE_KEY_TAG_ID_IN_EDIT = "tag-edit";
    private static final String PREF_KEY_ACTIVE_TAG = "active-my-tag";
    static final String PREF_KEY_TAG_TO_WRITE = "tag-to-write";

    static final String[] SUPPORTED_TYPES = new String[] {
            VCardRecord.RECORD_TYPE,
            UriRecord.RECORD_TYPE,
            TextRecord.RECORD_TYPE,
    };

    private View mSelectActiveTagAnchor;
    private View mActiveTagDetails;
    private CheckBox mEnabled;
    private ListView mList;

    private TagAdapter mAdapter;
    private long mActiveTagId;
    private Uri mTagBeingSaved;
    private NdefMessage mActiveTag;

    private WeakReference<SelectActiveTagDialog> mSelectActiveTagDialog;
    private long mTagIdInEdit = -1;
    private long mTagIdLongPressed;

    private boolean mWriteSupport = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_tag_activity);

        if (savedInstanceState != null) {
            mTagIdInEdit = savedInstanceState.getLong(BUNDLE_KEY_TAG_ID_IN_EDIT, -1);
        }

        // Set up the check box to toggle My tag sharing.
        mEnabled = (CheckBox) findViewById(R.id.toggle_enabled_checkbox);
        mEnabled.setChecked(false);  // Set after initial data load completes.
        findViewById(R.id.toggle_enabled_target).setOnClickListener(this);

        // Setup the active tag selector.
        mActiveTagDetails = findViewById(R.id.active_tag_details);
        mSelectActiveTagAnchor = findViewById(R.id.choose_my_tag);
        findViewById(R.id.active_tag).setOnClickListener(this);
        updateActiveTagView(null);  // Filled in after initial data load.

        mActiveTagId = getPreferences(Context.MODE_PRIVATE).getLong(PREF_KEY_ACTIVE_TAG, -1);

        // Setup the list.
        mAdapter = new TagAdapter(this);
        mList = (ListView) findViewById(android.R.id.list);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        findViewById(R.id.add_tag).setOnClickListener(this);

        // Don't setup the empty view until after the first load
        // so the empty text doesn't flash when first loading the
        // activity.
        mList.setEmptyView(null);

        // Kick off an async task to load the tags.
        new TagLoaderTask().execute((Void[]) null);

        // If we're not on a user build offer a back door for writing tags.
        // The UX is horrible so we don't want to ship it but need it for testing.
        if (!Build.TYPE.equalsIgnoreCase("user")) {
            mWriteSupport = true;
        }
        registerForContextMenu(mList);

        if (getIntent().hasExtra(EditTagActivity.EXTRA_RESULT_MSG)) {
            NdefMessage msg = (NdefMessage) Preconditions.checkNotNull(
                    getIntent().getParcelableExtra(EditTagActivity.EXTRA_RESULT_MSG));
            saveNewMessage(msg);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mTagIdInEdit = -1;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_TAG_ID_IN_EDIT, mTagIdInEdit);
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editTag(id);
    }

    /**
     * Opens the tag editor for a particular tag.
     */
    private void editTag(long id) {
        // TODO: use implicit Intent?
        Intent intent = new Intent(this, EditTagActivity.class);
        intent.setData(ContentUris.withAppendedId(NdefMessages.CONTENT_URI, id));
        mTagIdInEdit = id;
        startActivityForResult(intent, REQUEST_EDIT);
    }

    public void setEmptyView() {
        // TODO: set empty view.
    }

    public interface TagQuery {
        static final String[] PROJECTION = new String[] {
                NdefMessages._ID, // 0
                NdefMessages.DATE, // 1
                NdefMessages.TITLE, // 2
                NdefMessages.BYTES, // 3
        };

        static final int COLUMN_ID = 0;
        static final int COLUMN_DATE = 1;
        static final int COLUMN_TITLE = 2;
        static final int COLUMN_BYTES = 3;
    }

    /**
     * Asynchronously loads the tags info from the database.
     */
    final class TagLoaderTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        public Cursor doInBackground(Void... args) {
            Cursor cursor = getContentResolver().query(
                    NdefMessages.CONTENT_URI,
                    TagQuery.PROJECTION,
                    NdefMessages.IS_MY_TAG + "=1",
                    null, NdefMessages.DATE + " DESC");

            // Ensure the cursor executes and fills its window
            if (cursor != null) cursor.getCount();
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            mAdapter.changeCursor(cursor);

            if (cursor == null || cursor.getCount() == 0) {
                setEmptyView();
            } else {
                // Find the active tag.
                if (mTagBeingSaved != null) {
                    selectTagBeingSaved(mTagBeingSaved);

                } else if (mActiveTagId != -1) {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        if (mActiveTagId == cursor.getLong(TagQuery.COLUMN_ID)) {
                            selectActiveTag(cursor.getPosition());
                            break;
                        }
                    }
                }
            }


            SelectActiveTagDialog dialog = (mSelectActiveTagDialog == null)
                    ? null : mSelectActiveTagDialog.get();
            if (dialog != null) {
                dialog.setData(cursor);
            }
        }
    }

    /**
     * Struct to hold pointers to views in the list items to save time at view binding time.
     */
    static final class ViewHolder {
        public CharArrayBuffer titleBuffer;
        public TextView mainLine;
        public ImageView activeIcon;
    }

    /**
     * Adapter to display the the My tag entries.
     */
    public class TagAdapter extends CursorAdapter {
        private final LayoutInflater mInflater;

        public TagAdapter(Context context) {
            super(context, null, false);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            CharArrayBuffer buf = holder.titleBuffer;
            cursor.copyStringToBuffer(TagQuery.COLUMN_TITLE, buf);
            holder.mainLine.setText(buf.data, 0, buf.sizeCopied);

            boolean isActive = cursor.getLong(TagQuery.COLUMN_ID) == mActiveTagId;
            holder.activeIcon.setVisibility(isActive ? View.VISIBLE : View.GONE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.tag_list_item, null);

            // Cache items for the view
            ViewHolder holder = new ViewHolder();
            holder.titleBuffer = new CharArrayBuffer(64);
            holder.mainLine = (TextView) view.findViewById(R.id.title);
            holder.activeIcon = (ImageView) view.findViewById(R.id.active_tag_icon);
            view.findViewById(R.id.date).setVisibility(View.GONE);
            view.setTag(holder);

            return view;
        }

        @Override
        public void onContentChanged() {
            // Kick off an async query to refresh the list
            new TagLoaderTask().execute((Void[]) null);
        }
    }

    @Override
    public void onClick(View target) {
        switch (target.getId()) {
            case R.id.toggle_enabled_target:
                boolean enabled = !mEnabled.isChecked();
                if (enabled) {
                    if (mActiveTag != null) {
                        enableSharingAndStoreTag();
                        return;
                    }
                    Toast.makeText(
                            this,
                            getResources().getString(R.string.no_tag_selected),
                            Toast.LENGTH_SHORT).show();
                }

                disableSharing();
                break;

            case R.id.add_tag:
                showDialog(DIALOG_ID_ADD_NEW_TAG);
                break;

            case R.id.active_tag:
                if (mAdapter.getCursor() == null || mAdapter.getCursor().isClosed()) {
                    // Hopefully shouldn't happen.
                    return;
                }

                if (mAdapter.getCursor().getCount() == 0) {
                    OnClickListener onAdd = new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                showDialog(DIALOG_ID_ADD_NEW_TAG);
                            }
                        }
                    };
                    new AlertDialog.Builder(this)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.add_tag, onAdd)
                            .setMessage(R.string.no_tags_created)
                            .show();
                    return;
                }
                showDialog(DIALOG_ID_SELECT_ACTIVE_TAG);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor == null
                || cursor.isClosed()
                || !cursor.moveToPosition(((AdapterContextMenuInfo) info).position)) {
            return;
        }

        menu.setHeaderTitle(cursor.getString(TagQuery.COLUMN_TITLE));
        long id = cursor.getLong(TagQuery.COLUMN_ID);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_tag_list_context_menu, menu);

        // Prepare the menu for the item.
        menu.findItem(R.id.set_as_active).setVisible(id != mActiveTagId);
        mTagIdLongPressed = id;

        if (mWriteSupport) {
            menu.add(0, 1, 0, "Write to tag");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        long id = mTagIdLongPressed;
        switch (item.getItemId()) {
            case R.id.delete:
                deleteTag(id);
                return true;

            case R.id.set_as_active:
                Cursor cursor = mAdapter.getCursor();
                if (cursor == null || cursor.isClosed()) {
                    break;
                }

                for (int position = 0; cursor.moveToPosition(position); position++) {
                    if (cursor.getLong(TagQuery.COLUMN_ID) == id) {
                        selectActiveTag(position);
                        return true;
                    }
                }
                break;

            case R.id.edit:
                editTag(id);
                return true;

            case 1:
                AdapterView.AdapterContextMenuInfo info;
                try {
                    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                } catch (ClassCastException e) {
                    Log.e(TAG, "bad menuInfo", e);
                    break;
                }

                Intent intent = new Intent(this, WriteTagActivity.class);
                intent.putExtra("id", info.id);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            NdefMessage msg = (NdefMessage) Preconditions.checkNotNull(
                    data.getParcelableExtra(EditTagActivity.EXTRA_RESULT_MSG));

            if (mTagIdInEdit != -1) {
                TagService.updateMyMessage(this, mTagIdInEdit, msg);
            } else {
                saveNewMessage(msg);
            }
        }
    }

    private void saveNewMessage(NdefMessage msg) {
        TagService.saveMyMessage(this, msg, this);
    }

    @Override
    public void onSaveComplete(Uri newMsgUri) {
        if (isFinishing()) {
            // Callback came asynchronously and was after we finished - ignore.
            return;
        }
        mTagBeingSaved = newMsgUri;
        selectTagBeingSaved(newMsgUri);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        Context lightTheme = new ContextThemeWrapper(this, android.R.style.Theme_Light);
        if (id == DIALOG_ID_SELECT_ACTIVE_TAG) {
            SelectActiveTagDialog dialog = new SelectActiveTagDialog(lightTheme,
                    mAdapter.getCursor());
            dialog.setInverseBackgroundForced(true);
            mSelectActiveTagDialog = new WeakReference<SelectActiveTagDialog>(dialog);
            return dialog;
        } else if (id == DIALOG_ID_ADD_NEW_TAG) {
            ContentSelectorAdapter adapter = new ContentSelectorAdapter(lightTheme,
                    SUPPORTED_TYPES);
            AlertDialog dialog = new AlertDialog.Builder(lightTheme)
                    .setTitle(R.string.select_type)
                    .setIcon(0)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setAdapter(adapter, this)
                    .create();
            adapter.setListView(dialog.getListView());
            dialog.setInverseBackgroundForced(true);
            return dialog;
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.cancel();
        } else {
            RecordEditInfo info = (RecordEditInfo) ((AlertDialog) dialog).getListView()
                    .getAdapter().getItem(which);
            Intent intent = new Intent(this, EditTagActivity.class);
            intent.putExtra(EditTagActivity.EXTRA_NEW_RECORD_INFO, info);
            startActivityForResult(intent, REQUEST_EDIT);
        }
    }

    /**
     * Selects the tag to be used as the "My tag" shared tag.
     *
     * This does not necessarily persist the selection to the {@code NfcAdapter}. That must be done
     * via {@link #enableSharingAndStoreTag()}. However, it will call {@link #disableSharing()}
     * if the tag is invalid.
     */
    private void selectActiveTag(int position) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            mActiveTagId = cursor.getLong(TagQuery.COLUMN_ID);

            try {
                mActiveTag = new NdefMessage(cursor.getBlob(TagQuery.COLUMN_BYTES));

                // Persist active tag info to preferences.
                getPreferences(Context.MODE_PRIVATE)
                        .edit()
                        .putLong(PREF_KEY_ACTIVE_TAG, mActiveTagId)
                        .apply();

                updateActiveTagView(cursor.getString(TagQuery.COLUMN_TITLE));
                mAdapter.notifyDataSetChanged();

                // If there was an existing shared tag, we update the contents, since
                // the active tag contents may have been changed. This also forces the
                // active tag to be in sync with what the NfcAdapter.
                if (NfcAdapter.getDefaultAdapter(this).getLocalNdefMessage() != null) {
                    enableSharingAndStoreTag();
                }

            } catch (FormatException e) {
                // TODO: handle.
                disableSharing();
            }
        } else {
            updateActiveTagView(null);
            disableSharing();
        }
        mTagBeingSaved = null;
    }

    /**
     * Selects the tag to be used as the "My tag" shared tag, if the specified URI is found.
     * If the URI is not found, the next load will attempt to look for a matching tag to select.
     *
     * Commonly used for new tags that was just added to the database, and may not yet be
     * reflected in the {@code Cursor}.
     */
    private void selectTagBeingSaved(Uri uri) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor == null) {
            return;
        }
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            Uri tagUri = ContentUris.withAppendedId(
                    NdefMessages.CONTENT_URI,
                    cursor.getLong(TagQuery.COLUMN_ID));
            if (tagUri.equals(uri)) {
                selectActiveTag(cursor.getPosition());
                return;
            }
        }
    }

    private void enableSharingAndStoreTag() {
        mEnabled.setChecked(true);
        NfcAdapter.getDefaultAdapter(this).setLocalNdefMessage(
                Preconditions.checkNotNull(mActiveTag));
    }

    private void disableSharing() {
        mEnabled.setChecked(false);
        NfcAdapter.getDefaultAdapter(this).setLocalNdefMessage(null);
    }

    private void updateActiveTagView(String title) {
        if (title == null) {
            mActiveTagDetails.setVisibility(View.GONE);
            mSelectActiveTagAnchor.setVisibility(View.VISIBLE);
        } else {
            mActiveTagDetails.setVisibility(View.VISIBLE);
            ((TextView) mActiveTagDetails.findViewById(R.id.active_tag_title)).setText(title);
            mSelectActiveTagAnchor.setVisibility(View.GONE);
        }
    }

    /**
     * Removes the tag from the "My tag" list.
     */
    private void deleteTag(long id) {
        if (id == mActiveTagId) {
            selectActiveTag(-1);
        }
        TagService.delete(this, ContentUris.withAppendedId(NdefMessages.CONTENT_URI, id));
    }

    class SelectActiveTagDialog extends AlertDialog
            implements DialogInterface.OnClickListener, OnItemClickListener {

        private final ArrayList<HashMap<String, String>> mData;
        private final SimpleAdapter mSelectAdapter;

        protected SelectActiveTagDialog(Context context, Cursor cursor) {
            super(context);

            setTitle(context.getResources().getString(R.string.choose_my_tag));
            ListView list = new ListView(context);

            mData = Lists.newArrayList();
            mSelectAdapter = new SimpleAdapter(
                    context,
                    mData,
                    android.R.layout.simple_list_item_1,
                    new String[] { "title" },
                    new int[] { android.R.id.text1 });

            list.setAdapter(mSelectAdapter);
            list.setOnItemClickListener(this);
            setView(list);
            setIcon(0);
            setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    context.getString(android.R.string.cancel),
                    this);

            setData(cursor);
        }

        public void setData(final Cursor cursor) {
            if ((cursor == null) || (cursor.getCount() == 0)) {
                cancel();
                return;
            }
            mData.clear();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                mData.add(new HashMap<String, String>() {{
                    put("title", cursor.getString(MyTagList.TagQuery.COLUMN_TITLE));
                }});
            }

            mSelectAdapter.notifyDataSetChanged();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            cancel();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectActiveTag(position);
            enableSharingAndStoreTag();
            cancel();
        }
    }
}
