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
 * limitations under the License
 */

package com.android.apps.tag;

import com.android.apps.tag.provider.TagContract.NdefMessages;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class WriteTagActivity extends Activity {
    static final String TAG = WriteTagActivity.class.getName();

    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    TextView mTitle;
    TextView mStatus;
    TextView mCountView;
    NdefMessage mMessage;
    int mSize;
    int mCount = 1;

    final class MessageLoaderTask extends AsyncTask<String, Void, Cursor> {
        @Override
        public Cursor doInBackground(String... args) {
            Cursor cursor = getContentResolver().query(
                    NdefMessages.CONTENT_URI,
                    new String[] { NdefMessages.TITLE, NdefMessages.BYTES },
                    NdefMessages._ID + "=?",
                    new String[] { args[0] }, null);

            // Ensure the cursor executes and fills its window
            if (cursor != null) cursor.getCount();
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            try {
                if (cursor == null || !cursor.moveToFirst()) {
                    setStatus("Failed to load tag for writing.", false);
                    return;
                }
                byte[] blob = cursor.getBlob(1);
                mSize = blob.length;
                mMessage = new NdefMessage(blob);
                mTitle.setText("Scan a tag to write\n" + cursor.getString(0));
            } catch (FormatException e) {
                setStatus("Invalid tag.", false);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.write_tag);
        mTitle = (TextView) findViewById(R.id.title);
        mStatus = (TextView) findViewById(R.id.status);
        mCountView = (TextView) findViewById(R.id.count);

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey("id")) {
            setStatus("Nothing to write.", true);
            return;
        }

        mTitle.setText("Loading tag.");
        long id = extras.getLong("id");
        new MessageLoaderTask().execute(Long.toString(id));

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        mCountView.setText("Tag " + mCount++);
        if (mMessage != null) {
            writeTag(tag);
        } else {
            setStatus("Not ready to write.", false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    void setStatus(String message, boolean success) {
        mStatus.setText(message);
        if (!success) {
            mStatus.setTextColor(Color.RED);
        } else {
            mStatus.setTextColor(Color.GREEN);
        }
    }

    boolean writeTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    setStatus("Tag is read-only.", false);
                    return false;
                }
                if (ndef.getMaxSize() < mSize) {
                    setStatus("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " +
                            mSize + " bytes.", false);
                    return false;
                }

                ndef.writeNdefMessage(mMessage);
                setStatus("Wrote message to pre-formatted tag.", true);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(mMessage);
                        setStatus("Formatted tag and wrote message.", true);
                        return true;
                    } catch (IOException e) {
                        setStatus("Failed to format tag.", false);
                        return false;
                    }
                } else {
                    setStatus("Tag doesn't support NDEF.", false);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write tag", e);
        }

        setStatus("Failed to write tag", false);
        return false;
    }
}
