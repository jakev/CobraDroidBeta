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

package com.android.quicksearchbox;

import com.android.quicksearchbox.ui.CorporaAdapter;
import com.android.quicksearchbox.ui.CorpusViewFactory;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * The configuration screen for search widgets.
 */
public class SearchWidgetConfigActivity extends ChoiceActivity {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchWidgetConfigActivity";

    private static final String PREFS_NAME = "SearchWidgetConfig";
    private static final String WIDGET_CORPUS_NAME_PREFIX = "widget_corpus_";
    private static final String WIDGET_CORPUS_SHOWING_HINT_PREFIX = "widget_showing_hint_";

    private CorporaAdapter mAdapter;

    private int mAppWidgetId;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setHeading(R.string.search_widget);
        setOnItemClickListener(new SourceClickListener());

        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // If there is only All, or only All and one other corpus, there is no
        // point in asking the user to select a corpus.
        if (getCorpusRanker().getRankedCorpora().size() <= 1) {
            selectCorpus(null);
        }
    }

    @Override
    protected void onStart() {
        setAdapter(CorporaAdapter.createListAdapter(getViewFactory(), getCorpusRanker()));
        super.onStart();
    }

    @Override
    protected void onStop() {
        setAdapter(null);
        super.onStop();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter == mAdapter) return;
        if (mAdapter != null) mAdapter.close();
        mAdapter = (CorporaAdapter) adapter;
        super.setAdapter(adapter);
    }

    protected void selectCorpus(Corpus corpus) {
        setWidgetCorpusName(mAppWidgetId, corpus);
        SearchWidgetProvider.updateSearchWidgets(this);

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private static SharedPreferences getWidgetPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private static String getCorpusNameKey(int appWidgetId) {
        return WIDGET_CORPUS_NAME_PREFIX + appWidgetId;
    }

    private static String getShowingHintKey(int appWidgetId) {
        return WIDGET_CORPUS_SHOWING_HINT_PREFIX + appWidgetId;
    }

    private void setWidgetCorpusName(int appWidgetId, Corpus corpus) {
        String corpusName = corpus == null ? null : corpus.getName();
        SharedPreferences.Editor prefs = getWidgetPreferences(this).edit();
        prefs.putString(getCorpusNameKey(appWidgetId), corpusName);
        prefs.commit();
    }

    public static String getWidgetCorpusName(Context context, int appWidgetId) {
        SharedPreferences prefs = getWidgetPreferences(context);
        return prefs.getString(getCorpusNameKey(appWidgetId), null);
    }

    public static void setWidgetShowingHint(Context context, int appWidgetId, boolean showing) {
        SharedPreferences.Editor prefs = getWidgetPreferences(context).edit();
        prefs.putBoolean(getShowingHintKey(appWidgetId), showing);
        boolean c = prefs.commit();
        if (DBG) Log.d(TAG, "Widget " + appWidgetId + " set showing hint " + showing + "("+c+")");
    }

    public static boolean getWidgetShowingHint(Context context, int appWidgetId) {
        SharedPreferences prefs = getWidgetPreferences(context);
        boolean r = prefs.getBoolean(getShowingHintKey(appWidgetId), false);
        if (DBG) Log.d(TAG, "Widget " + appWidgetId + " showing hint: " + r);
        return r;
    }

    private CorpusRanker getCorpusRanker() {
        return QsbApplication.get(this).getCorpusRanker();
    }

    private CorpusViewFactory getViewFactory() {
        return QsbApplication.get(this).getCorpusViewFactory();
    }

    private class SourceClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Corpus corpus = (Corpus) parent.getItemAtPosition(position);
            selectCorpus(corpus);
        }
    }
}
