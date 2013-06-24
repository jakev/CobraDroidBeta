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

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class TagService extends IntentService {
    private static final String TAG = "TagService";

    private static final String EXTRA_SAVE_MSGS = "msgs";
    private static final String EXTRA_DELETE_URI = "delete";
    private static final String EXTRA_STAR_URI = "set_star";
    private static final String EXTRA_UNSTAR_URI = "remove_star";
    private static final String EXTRA_STARRED = "starred";
    private static final String EXTRA_PENDING_INTENT = "pending";
    private static final String EXTRA_SAVE_IN_MY_TAGS = "my_tags";
    private static final String EXTRA_REPLACE_ID = "replace";

    private static final boolean DEBUG = true;

    public TagService() {
        super("SaveTagService");
    }

    public interface SaveCallbacks {
        void onSaveComplete(Uri uri);
    }

    private static final class EmptyService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


    @Override
    public void onHandleIntent(Intent intent) {
        if (intent.hasExtra(EXTRA_SAVE_MSGS)) {
            Parcelable[] msgs = intent.getParcelableArrayExtra(EXTRA_SAVE_MSGS);
            NdefMessage msg = (NdefMessage) msgs[0];

            ContentValues values = NdefMessages.toValues(
                    this, msg,
                    intent.getBooleanExtra(EXTRA_STARRED, false),
                    intent.getBooleanExtra(EXTRA_SAVE_IN_MY_TAGS, false),
                    System.currentTimeMillis());

            if (intent.hasExtra(EXTRA_REPLACE_ID)) {
                long id = intent.getLongExtra(EXTRA_REPLACE_ID, 0);
                String where = NdefMessages._ID + "=" + id;
                getContentResolver().update(NdefMessages.CONTENT_URI, values, where, null);
            } else {
                Uri uri = getContentResolver().insert(NdefMessages.CONTENT_URI, values);

                if (intent.hasExtra(EXTRA_PENDING_INTENT)) {
                    Intent result = new Intent();
                    result.setData(uri);

                    PendingIntent pending = (PendingIntent) intent.getParcelableExtra(
                            EXTRA_PENDING_INTENT);
                    if (pending != null) {
                        try {
                            pending.send(this, 0, result);
                        } catch (CanceledException e) {
                            if (DEBUG) Log.d(TAG, "Pending intent was canceled.");
                        }
                    }
                }
            }

            return;
        }

        if (intent.hasExtra(EXTRA_DELETE_URI)) {
            Uri uri = (Uri) intent.getParcelableExtra(EXTRA_DELETE_URI);
            getContentResolver().delete(uri, null, null);
            return;
        }

        if (intent.hasExtra(EXTRA_STAR_URI)) {
            Uri uri = (Uri) intent.getParcelableExtra(EXTRA_STAR_URI);
            ContentValues values = new ContentValues();
            values.put(NdefMessages.STARRED, 1);
            getContentResolver().update(uri, values, null, null);
        }

        if (intent.hasExtra(EXTRA_UNSTAR_URI)) {
            Uri uri = (Uri) intent.getParcelableExtra(EXTRA_UNSTAR_URI);
            ContentValues values = new ContentValues();
            values.put(NdefMessages.STARRED, 0);
            getContentResolver().update(uri, values, null, null);
        }
    }

    public static void saveMessages(Context context, NdefMessage[] msgs, boolean starred,
            PendingIntent pending) {
        Intent intent = new Intent(context, TagService.class);
        intent.putExtra(TagService.EXTRA_SAVE_MSGS, msgs);
        intent.putExtra(TagService.EXTRA_STARRED, starred);
        intent.putExtra(TagService.EXTRA_PENDING_INTENT, pending);
        context.startService(intent);
    }

    public static void saveMyMessages(Context context, NdefMessage[] msgs, PendingIntent pending) {
        Intent intent = new Intent(context, TagService.class);
        intent.putExtra(TagService.EXTRA_SAVE_MSGS, msgs);
        intent.putExtra(TagService.EXTRA_SAVE_IN_MY_TAGS, true);
        if (pending != null) {
            intent.putExtra(TagService.EXTRA_PENDING_INTENT, pending);
        }
        context.startService(intent);
    }

    public static void saveMyMessage(
            final Context context, final NdefMessage msg, final SaveCallbacks callbacks) {
        final Handler handler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                // Start service to ensure the save completes in case this app gets thrown into the
                // background.
                context.startService(new Intent(context, EmptyService.class));


                ContentValues values = NdefMessages.toValues(
                        context, msg,
                        false /* starred */, true /* is one of "my tags" */,
                        System.currentTimeMillis());

                // Start dummy service to ensure the save completes.
                context.startService(new Intent(context, EmptyService.class));

                final Uri result =
                        context.getContentResolver().insert(NdefMessages.CONTENT_URI, values);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbacks.onSaveComplete(result);
                    }
                });

                // Stop service so we can be killed.
                context.stopService(new Intent(context, EmptyService.class));
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


    public static void updateMyMessage(Context context, long id, NdefMessage msg) {
        Intent intent = new Intent(context, TagService.class);
        intent.putExtra(TagService.EXTRA_SAVE_MSGS, new NdefMessage[] { msg });
        intent.putExtra(TagService.EXTRA_SAVE_IN_MY_TAGS, true);
        intent.putExtra(TagService.EXTRA_REPLACE_ID, id);
        context.startService(intent);
    }

    public static void delete(Context context, Uri uri) {
        Intent intent = new Intent(context, TagService.class);
        intent.putExtra(TagService.EXTRA_DELETE_URI, uri);
        context.startService(intent);
    }

    public static void setStar(Context context, Uri message, boolean star) {
        Intent intent = new Intent(context, TagService.class);
        if (star) {
            intent.putExtra(EXTRA_STAR_URI, message);
        } else {
            intent.putExtra(EXTRA_UNSTAR_URI, message);
        }
        context.startService(intent);
    }
}
