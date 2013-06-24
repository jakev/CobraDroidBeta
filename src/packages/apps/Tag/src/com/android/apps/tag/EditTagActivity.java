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

package com.android.apps.tag;

import com.android.apps.tag.message.NdefMessageParser;
import com.android.apps.tag.message.ParsedNdefMessage;
import com.android.apps.tag.provider.TagContract.NdefMessages;
import com.android.apps.tag.record.ParsedNdefRecord;
import com.android.apps.tag.record.RecordEditInfo;
import com.android.apps.tag.record.RecordEditInfo.EditCallbacks;
import com.android.apps.tag.record.TextRecord;
import com.android.apps.tag.record.TextRecord.TextRecordEditInfo;
import com.android.apps.tag.record.UriRecord;
import com.android.apps.tag.record.UriRecord.UriRecordEditInfo;
import com.android.apps.tag.record.VCardRecord;
import com.google.common.collect.ImmutableSet;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * A base {@link Activity} class for an editor of an {@link NdefMessage} tag.
 *
 * The core of the editing is done by various a {@link View}s that differs based on
 * {@link ParsedNdefRecord} types. Each type of {@link ParsedNdefRecord} can build a to
 * pick/select a new piece of content, or edit an existing content for the {@link NdefMessage}.
 */
public class EditTagActivity extends Activity implements OnClickListener, EditCallbacks {

    private static final String LOG_TAG = "Tags";

    protected static final String BUNDLE_KEY_RECORD = "outstanding-pick";
    public static final String EXTRA_RESULT_MSG = "com.android.apps.tag.msg";
    public static final String EXTRA_NEW_RECORD_INFO = "com.android.apps.tag.new_record";

    protected static final Set<String> SUPPORTED_RECORD_TYPES = ImmutableSet.of(
        VCardRecord.RECORD_TYPE,
        UriRecord.RECORD_TYPE,
        TextRecord.RECORD_TYPE
    );

    /**
     * The underlying record in the tag being edited.
     */
    private RecordEditInfo mRecord;

    /**
     * The container where the subviews for each record are housed.
     */
    private ViewGroup mContentRoot;

    /**
     * Whether or not data was already parsed from an {@link Intent}. This happens when the user
     * shares data via the My tag feature.
     */
    private boolean mParsedIntent = false;

    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.edit_tag_activity);
        setTitle(getResources().getString(R.string.edit_tag));

        mInflater = LayoutInflater.from(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        mContentRoot = (ViewGroup) findViewById(R.id.content_parent);

        if (savedState != null) {
            mRecord = savedState.getParcelable(BUNDLE_KEY_RECORD);
            if (mRecord != null) {
                refresh();
            } else {
                Log.w(LOG_TAG, "invalid instance state, loading from intent");
                resolveIntent();
            }
        } else {
            resolveIntent();
        }
    }

    /**
     * @return The list of {@link ParsedNdefRecord} types that this editor supports. Subclasses
     *     may override to filter out specific types.
     */
    public Set<String> getSupportedTypes() {
        return SUPPORTED_RECORD_TYPES;
    }

    /**
     * Builds a snapshot of current value as held in the internal state of this editor.
     */
    public NdefRecord getValue() {
        return mRecord.getValue();
    }

    /**
     * Refreshes the UI with updated content from the record.
     * Typically used when the records require an external {@link Activity} to edit.
     */
    public void refresh() {
        ViewGroup root = mContentRoot;
        View editView = mRecord.getEditView(this, mInflater, root, this);
        root.removeAllViews();
        root.addView(editView);
    }

    @Override
    public void startPickForRecord(RecordEditInfo editInfo, Intent intent) {
        startActivityForResult(intent, 0);
    }

    @Override
    public void deleteRecord(RecordEditInfo editInfo) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode != RESULT_OK) || (data == null)) {
            // No valid data, close the editor.
            finish();
            return;
        }

        // Handles results from another Activity that picked content to write to a tag.
        try {
            mRecord.handlePickResult(this, data);
        } catch (IllegalArgumentException ex) {
            // No valid data, close the editor.
            finish();
            return;
        }

        // Update the title to indicate that we're adding a tag instead of editing.
        setTitle(R.string.add_tag);

        // Setup the tag views
        refresh();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_RECORD, mRecord);
    }

    interface GetTagQuery {
        final static String[] PROJECTION = new String[] {
                NdefMessages.BYTES
        };

        static final int COLUMN_BYTES = 0;
    }

    /**
     * Loads a tag from the database, parses it, and builds the views.
     */
    final class LoadTagTask extends AsyncTask<Uri, Void, Cursor> {
        @Override
        public Cursor doInBackground(Uri... args) {
            Cursor cursor = getContentResolver().query(args[0], GetTagQuery.PROJECTION,
                    null, null, null);

            // Ensure the cursor loads its window.
            if (cursor != null) {
                cursor.getCount();
            }
            return cursor;
        }

        @Override
        public void onPostExecute(Cursor cursor) {
            NdefMessage msg = null;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    msg = new NdefMessage(cursor.getBlob(GetTagQuery.COLUMN_BYTES));
                    populateFromMessage(msg);
                }
            } catch (FormatException e) {
                Log.e(LOG_TAG, "Unable to parse tag for editing.", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    protected void resolveIntent() {
        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction()) && !mParsedIntent) {
            if (buildFromSendIntent(intent)) {
                return;
            }

            mParsedIntent = true;
            return;
        }

        Uri uri = intent.getData();
        if (uri != null) {
            // Edit existing tag.
            new LoadTagTask().execute(uri);
        } else {
            RecordEditInfo newRecord = intent.getParcelableExtra(EXTRA_NEW_RECORD_INFO);
            if (newRecord != null) {
                initializeForNewTag(newRecord);
            }
        }
    }

    private void initializeForNewTag(RecordEditInfo editInfo) {
        mRecord = editInfo;

        Intent pickIntent = editInfo.getPickIntent();
        if (pickIntent != null) {
            startPickForRecord(editInfo, pickIntent);
        } else {
            refresh();
        }
    }

    void populateFromMessage(NdefMessage refMessage) {
        // Locally stored message.
        ParsedNdefMessage parsed = NdefMessageParser.parse(refMessage);
        List<ParsedNdefRecord> records = parsed.getRecords();

        // TODO: loosen this restriction.
        // There is always a "Text" record for a My Tag.
        if (records.size() != 1) {
            Log.w(LOG_TAG, "Message not in expected format");
            return;
        }
        mRecord = records.get(0).getEditInfo(this);
        refresh();
    }

    /**
     * Populates the editor from extras in a given {@link Intent}
     * @param intent the {@link Intent} to parse.
     * @return whether or not the {@link Intent} could be handled.
     */
    private boolean buildFromSendIntent(final Intent intent) {
        String type = intent.getType();

        if ("text/plain".equals(type)) {
            String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            try {
                URL parsed = new URL(text);

                // Valid URL.
                mRecord = new UriRecordEditInfo(text);
                refresh();
                return true;

            } catch (MalformedURLException ex) {
                // Random text
                mRecord = new TextRecordEditInfo(text);
                refresh();
                return true;
            }

        } else if ("text/x-vcard".equals(type)) {
            Uri stream = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (stream != null) {
                RecordEditInfo editInfo = VCardRecord.editInfoForUri(stream);
                if (editInfo != null) {
                    mRecord = editInfo;
                    refresh();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Saves the content of the tag.
     */
    private void saveAndFinish() {
        if (mRecord == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        NdefMessage msg = new NdefMessage(new NdefRecord[] { mRecord.getValue() });

        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            // If opening directly from a different application via ACTION_SEND, save the tag and
            // open the MyTagList so they can enable it.
            Intent openMyTags = new Intent(this, MyTagList.class);
            openMyTags.putExtra(EXTRA_RESULT_MSG, msg);
            startActivity(openMyTags);
            finish();

        } else {
            Intent result = new Intent();
            result.putExtra(EXTRA_RESULT_MSG, msg);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    @Override
    public void onClick(View target) {
        switch (target.getId()) {
            case R.id.save:
                saveAndFinish();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                HelpUtils.openHelp(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
