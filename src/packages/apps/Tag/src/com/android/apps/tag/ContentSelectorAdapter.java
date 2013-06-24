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

import com.android.apps.tag.record.TextRecord;
import com.android.apps.tag.record.UriRecord;
import com.android.apps.tag.record.VCardRecord;

import android.content.Context;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * An {@link Adapter} that presents options to select data which can be written into a
 * {@link NdefRecord} for an NFC tag.
 */
public class ContentSelectorAdapter extends BaseAdapter {
    private Context mContext;
    private final LayoutInflater mInflater;
    private String[] mSupportedTypes;
    private View[] mViews;

    public ContentSelectorAdapter(Context context, String[] supportedTypes) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mSupportedTypes = supportedTypes;
        mViews = new View[supportedTypes.length];
    }

    public void setListView(ListView list) {
        int size = mSupportedTypes.length;
        for (int i = 0; i < size; i++) {
            String type = mSupportedTypes[i];
            if (UriRecord.RECORD_TYPE.equals(type)) {
                mViews[i] = UriRecord.getAddView(mContext, mInflater, list);
            } else if (VCardRecord.RECORD_TYPE.equals(type)) {
                mViews[i] = VCardRecord.getAddView(mContext, mInflater, list);
            } else if (TextRecord.RECORD_TYPE.equals(type)) {
                mViews[i] = TextRecord.getAddView(mContext, mInflater, list);
            } else {
                throw new IllegalArgumentException("Not a supported view type");
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public int getCount() {
        return mSupportedTypes.length;
    }

    @Override
    public View getView(int position, View recycle, ViewGroup parent) {
        return mViews[position];
    }

    @Override
    public Object getItem(int position) {
        return mViews[position].getTag();
    }
}