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

import com.android.apps.tag.provider.TagContract.NdefMessages;
import com.android.apps.tag.provider.TagProvider;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An {@link Activity} that displays a flat list of tags that can be "opened".
 */
public class TagList extends ListActivity implements OnClickListener {
    static final String TAG = "TagList";

    static final String EXTRA_SHOW_STARRED_ONLY = "show_starred_only";

    TagAdapter mAdapter;
    boolean mShowStarredOnly;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tag_list);
        findViewById(R.id.more_info).setVisibility(View.GONE);

        mShowStarredOnly = getIntent().getBooleanExtra(EXTRA_SHOW_STARRED_ONLY, false);

        new TagLoaderTask().execute((Void[]) null);
        mAdapter = new TagAdapter(this);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        super.onDestroy();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                ContentUris.withAppendedId(NdefMessages.CONTENT_URI, id));
        startActivity(intent);
    }

    public void setEmptyView() {
        TextView empty = (TextView) findViewById(R.id.text);
        View button = findViewById(R.id.more_info);

        if (mShowStarredOnly) {
            empty.setText(R.string.empty_list_starred);
            button.setVisibility(View.GONE);
        } else {
            empty.setText(Html.fromHtml(getString(R.string.empty_list)));
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        HelpUtils.openHelp(this);
    }

    interface TagQuery {
        static final String[] PROJECTION = new String[] {
                NdefMessages._ID, // 0
                NdefMessages.DATE, // 1
                NdefMessages.TITLE, // 2
                NdefMessages.STARRED, // 3
        };

        static final int COLUMN_ID = 0;
        static final int COLUMN_DATE = 1;
        static final int COLUMN_TITLE = 2;
        static final int COLUMN_STARRED = 3;
    }

    /**
     * Asynchronously loads the tag info from the database.
     */
    final class TagLoaderTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        public Cursor doInBackground(Void... args) {
            String starred = mShowStarredOnly ? NdefMessages.STARRED + "=1" : null;
            String notMyTag = NdefMessages.IS_MY_TAG + "!=1";

            Cursor cursor = getContentResolver().query(
                    NdefMessages.CONTENT_URI,
                    TagQuery.PROJECTION,
                    TagProvider.concatenateWhere(starred, notMyTag),
                    null, NdefMessages.DATE + " DESC");

            // Ensure the cursor executes and fills its window
            if (cursor != null) cursor.getCount();
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null || cursor.getCount() == 0) {
                // Don't setup the empty view until after the first load
                // so the empty text doesn't flash when first loading the
                // activity.
                setEmptyView();
            }
            mAdapter.changeCursor(cursor);
        }
    }

    /**
     * Struct to hold pointers to views in the list items to save time at view binding time.
     */
    static final class ViewHolder {
        public CharArrayBuffer titleBuffer;
        public TextView mainLine;
        public TextView dateLine;
    }

    /**
     * Adapter to display the tag entries.
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

            holder.dateLine.setText(DateUtils.getRelativeTimeSpanString(
                    context, cursor.getLong(TagQuery.COLUMN_DATE)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.tag_list_item, null);

            // Cache items for the view
            ViewHolder holder = new ViewHolder();
            holder.titleBuffer = new CharArrayBuffer(64);
            holder.mainLine = (TextView) view.findViewById(R.id.title);
            holder.dateLine = (TextView) view.findViewById(R.id.date);
            view.setTag(holder);

            return view;
        }

        @Override
        public void onContentChanged() {
            // Kick off an async query to refresh the list
            new TagLoaderTask().execute((Void[]) null);
        }
    }
}
