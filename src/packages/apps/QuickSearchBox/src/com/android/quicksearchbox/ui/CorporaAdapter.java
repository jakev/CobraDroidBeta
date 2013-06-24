/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox.ui;

import com.android.quicksearchbox.Corpus;
import com.android.quicksearchbox.CorpusRanker;

import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing a list of sources in the source selection activity.
 */
public class CorporaAdapter extends BaseAdapter {

    private static final String TAG = "CorporaAdapter";
    private static final boolean DBG = false;

    private final CorpusViewFactory mViewFactory;

    private final CorpusRanker mRanker;

    private final DataSetObserver mCorporaObserver = new CorporaObserver();

    private List<Corpus> mRankedEnabledCorpora;

    private boolean mGridView;

    private CorporaAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker, boolean gridView) {
        mViewFactory = viewFactory;
        mRanker = ranker;
        mGridView = gridView;
        mRanker.registerDataSetObserver(mCorporaObserver);
        updateCorpora();
    }

    public static CorporaAdapter createListAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker) {
        return new CorporaAdapter(viewFactory, ranker, false);
    }

    public static CorporaAdapter createGridAdapter(CorpusViewFactory viewFactory,
            CorpusRanker ranker) {
        return new CorporaAdapter(viewFactory, ranker, true);
    }

    private void updateCorpora() {
        mRankedEnabledCorpora = new ArrayList<Corpus>();
        for (Corpus corpus : mRanker.getRankedCorpora()) {
            if (!corpus.isCorpusHidden()) {
                mRankedEnabledCorpora.add(corpus);
            }
        }
        notifyDataSetChanged();
    }

    public void close() {
        mRanker.unregisterDataSetObserver(mCorporaObserver);
    }

    public int getCount() {
        return 1 + mRankedEnabledCorpora.size();
    }

    public Corpus getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return mRankedEnabledCorpora.get(position - 1);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Gets the position of the given corpus.
     */
    public int getCorpusPosition(Corpus corpus) {
        if (corpus == null) {
            return 0;
        }
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (corpus.equals(getItem(i))) {
                return i;
            }
        }
        Log.w(TAG, "Corpus not in adapter: " + corpus);
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CorpusView view = (CorpusView) convertView;
        if (view == null) {
            view = createView(parent);
        }
        Corpus corpus = getItem(position);
        Drawable icon;
        CharSequence label;
        if (corpus == null) {
            icon = mViewFactory.getGlobalSearchIcon();
            label = mViewFactory.getGlobalSearchLabel();
        } else {
            icon = corpus.getCorpusIcon();
            label = corpus.getLabel();
        }
        if (DBG) Log.d(TAG, "Binding " + position + ", label=" + label);
        view.setIcon(icon);
        view.setLabel(label);
        return view;
    }

    protected CorpusView createView(ViewGroup parent) {
        if (mGridView) {
            return mViewFactory.createGridCorpusView(parent);
        } else {
            return mViewFactory.createListCorpusView(parent);
        }
    }

    private class CorporaObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            updateCorpora();
        }

        @Override
        public void onInvalidated() {
            updateCorpora();
        }
    }

}
