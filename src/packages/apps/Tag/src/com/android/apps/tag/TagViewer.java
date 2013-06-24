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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device just discovered.
 */
public class TagViewer extends Activity implements OnClickListener {
    static final String TAG = "SaveTag";
    static final String EXTRA_TAG_DB_ID = "db_id";
    static final String EXTRA_MESSAGE = "msg";
    static final String EXTRA_KEEP_TITLE = "keepTitle";

    static final boolean SHOW_OVER_LOCK_SCREEN = false;

    /** This activity will finish itself in this amount of time if the user doesn't do anything. */
    static final int ACTIVITY_TIMEOUT_MS = 7 * 1000;

    Uri mTagUri;
    ImageView mIcon;
    TextView mTitle;
    TextView mDate;
    CheckBox mStar;
    Button mDeleteButton;
    Button mDoneButton;
    LinearLayout mTagContent;

    BroadcastReceiver mReceiver;

    private class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (!isFinishing()) {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SHOW_OVER_LOCK_SCREEN) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        setContentView(R.layout.tag_viewer);

        mTagContent = (LinearLayout) findViewById(R.id.list);
        mTitle = (TextView) findViewById(R.id.title);
        mDate = (TextView) findViewById(R.id.date);
        mIcon = (ImageView) findViewById(R.id.icon);
        mStar = (CheckBox) findViewById(R.id.star);
        mDeleteButton = (Button) findViewById(R.id.button_delete);
        mDoneButton = (Button) findViewById(R.id.button_done);

        mDeleteButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);
        mStar.setOnClickListener(this);
        mIcon.setImageResource(R.drawable.ic_launcher_nfc);

        resolveIntent(getIntent());
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (mTagUri == null) {
            // Someone how the user was fast enough to navigate away from the activity
            // before the service was able to save the tag and call back onto this
            // activity with the pending intent. Since we don't know what do display here
            // just finish the activity.
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        PendingIntent pending = getPendingIntent();
        pending.cancel();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private PendingIntent getPendingIntent() {
        Intent callback = new Intent();
        callback.setClass(this, TagViewer.class);
        callback.setAction(Intent.ACTION_VIEW);
        callback.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
        callback.putExtra(EXTRA_KEEP_TITLE, true);

        return PendingIntent.getActivity(this, 0, callback, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    void resolveIntent(Intent intent) {
        // Parse the intent
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            if (SHOW_OVER_LOCK_SCREEN) {
                // A tag was just scanned so poke the user activity wake lock to keep
                // the screen on a bit longer in the event that the activity has
                // hidden the lock screen.
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
                // This lock CANNOT be manually released in onStop() since that may
                // cause a lock under run exception to be thrown when the timeout
                // hits.
                wakeLock.acquire(ACTIVITY_TIMEOUT_MS);

                if (mReceiver == null) {
                    mReceiver = new ScreenOffReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mReceiver, filter);
                }
            }

            // When a tag is discovered we send it to the service to be save. We
            // include a PendingIntent for the service to call back onto. This
            // will cause this activity to be restarted with onNewIntent(). At
            // that time we read it from the database and view it.
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null && rawMsgs.length > 0) {
                // stupid java, need to cast one-by-one
                msgs = new NdefMessage[rawMsgs.length];
                for (int i=0; i<rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
            TagService.saveMessages(this, msgs, false, getPendingIntent());

            // Setup the views
            setTitle(R.string.title_scanned_tag);
            mDate.setVisibility(View.GONE);
            mStar.setChecked(false);
            mStar.setEnabled(true);

            // Play notification.
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(
                        R.raw.discovered_tag_notification);
                if (afd != null) {
                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(
                            afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                    player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    player.prepare();
                    player.start();
                }
            } catch (IOException ex) {
                Log.d(TAG, "Unable to play sound for tag discovery", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "Unable to play sound for tag discovery", ex);
            } catch (SecurityException ex) {
                Log.d(TAG, "Unable to play sound for tag discovery", ex);
            }

        } else if (Intent.ACTION_VIEW.equals(action)) {
            // Setup the views
            if (!intent.getBooleanExtra(EXTRA_KEEP_TITLE, false)) {
                setTitle(R.string.title_existing_tag);
                mDate.setVisibility(View.VISIBLE);
            }

            mStar.setVisibility(View.VISIBLE);
            mStar.setEnabled(false); // it's reenabled when the async load completes

            // Read the tag from the database asynchronously
            mTagUri = intent.getData();
            new LoadTagTask().execute(mTagUri);
        } else {
            Log.e(TAG, "Unknown intent " + intent);
            finish();
            return;
        }
    }

    void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTagContent;

        // Clear out any old views in the content area, for example if you scan two tags in a row.
        content.removeAllViews();

        // Parse the first message in the list
        //TODO figure out what to do when/if we support multiple messages per tag
        ParsedNdefMessage parsedMsg = NdefMessageParser.parse(msgs[0]);

        // Build views for all of the sub records
        List<ParsedNdefRecord> records = parsedMsg.getRecords();
        final int size = records.size();

        for (int i = 0 ; i < size ; i++) {
            ParsedNdefRecord record = records.get(i);
            content.addView(record.getView(this, inflater, content, i));
            inflater.inflate(R.layout.tag_divider, content, true);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    public void onClick(View view) {
        if (view == mDeleteButton) {
            if (mTagUri == null) {
                finish();
            } else {
                // The tag came from the database, start a service to delete it
                TagService.delete(this, mTagUri);
                finish();
            }
            Toast.makeText(this, getResources().getString(R.string.tag_deleted), Toast.LENGTH_SHORT)
                    .show();
        } else if (view == mDoneButton) {
            finish();
        } else if (view == mStar) {
            if (mTagUri != null) {
                TagService.setStar(this, mTagUri, mStar.isChecked());
            }
        }
    }

    interface ViewTagQuery {
        final static String[] PROJECTION = new String[] {
                NdefMessages.BYTES, // 0
                NdefMessages.STARRED, // 1
                NdefMessages.DATE, // 2
        };

        static final int COLUMN_BYTES = 0;
        static final int COLUMN_STARRED = 1;
        static final int COLUMN_DATE = 2;
    }

    /**
     * Loads a tag from the database, parses it, and builds the views
     */
    final class LoadTagTask extends AsyncTask<Uri, Void, Cursor> {
        @Override
        public Cursor doInBackground(Uri... args) {
            Cursor cursor = getContentResolver().query(args[0], ViewTagQuery.PROJECTION,
                    null, null, null);

            // Ensure the cursor loads its window
            if (cursor != null) cursor.getCount();
            return cursor;
        }

        @Override
        public void onPostExecute(Cursor cursor) {
            NdefMessage msg = null;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    msg = new NdefMessage(cursor.getBlob(ViewTagQuery.COLUMN_BYTES));
                    if (msg != null) {
                        mDate.setText(DateUtils.getRelativeTimeSpanString(TagViewer.this,
                                cursor.getLong(ViewTagQuery.COLUMN_DATE)));
                        mStar.setChecked(cursor.getInt(ViewTagQuery.COLUMN_STARRED) != 0);
                        mStar.setEnabled(true);
                        buildTagViews(new NdefMessage[] { msg });
                    }
                }
            } catch (FormatException e) {
                Log.e(TAG, "invalid tag format", e);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }
}
