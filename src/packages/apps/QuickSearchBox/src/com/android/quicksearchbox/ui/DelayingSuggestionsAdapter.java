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

import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.Suggestions;

import android.database.DataSetObserver;
import android.util.Log;

/**
 * A {@link SuggestionsAdapter} that doesn't expose the new suggestions
 * until there are some results to show.
 */
public class DelayingSuggestionsAdapter extends SuggestionsAdapter {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.DelayingSuggestionsAdapter";

    private DataSetObserver mPendingDataSetObserver;

    private Suggestions mPendingSuggestions;

    public DelayingSuggestionsAdapter(SuggestionViewFactory viewFactory) {
        super(viewFactory);
    }

    @Override
    public void close() {
        setPendingSuggestions(null);
        super.close();
    }

    @Override
    public void setSuggestions(Suggestions suggestions) {
        if (suggestions == null) {
            super.setSuggestions(null);
            setPendingSuggestions(null);
            return;
        }
        if (shouldPublish(suggestions)) {
            if (DBG) Log.d(TAG, "Publishing suggestions immediately: " + suggestions);
            super.setSuggestions(suggestions);
            // Clear any old pending suggestions.
            setPendingSuggestions(null);
        } else {
            if (DBG) Log.d(TAG, "Delaying suggestions publishing: " + suggestions);
            setPendingSuggestions(suggestions);
        }
    }

    /**
     * Gets whether the given suggestions are non-empty for the selected source.
     */
    private boolean shouldPublish(Suggestions suggestions) {
        if (suggestions.isDone()) return true;
        SuggestionCursor cursor = getCorpusCursor(suggestions, getCorpus());
        return cursor != null && cursor.getCount() > 0;
    }

    private void setPendingSuggestions(Suggestions suggestions) {
        if (mPendingSuggestions == suggestions) {
            return;
        }
        if (isClosed()) {
            if (suggestions != null) {
                suggestions.close();
            }
            return;
        }
        if (mPendingDataSetObserver == null) {
            mPendingDataSetObserver = new PendingSuggestionsObserver();
        }
        if (mPendingSuggestions != null) {
            mPendingSuggestions.unregisterDataSetObserver(mPendingDataSetObserver);
            // Close old suggestions, but only if they are not also the current
            // suggestions.
            if (mPendingSuggestions != getSuggestions()) {
                mPendingSuggestions.close();
            }
        }
        mPendingSuggestions = suggestions;
        if (mPendingSuggestions != null) {
            mPendingSuggestions.registerDataSetObserver(mPendingDataSetObserver);
        }
    }

    protected void onPendingSuggestionsChanged() {
        if (DBG) {
            Log.d(TAG, "onPendingSuggestionsChanged(), mPendingSuggestions="
                    + mPendingSuggestions);
        }
        if (shouldPublish(mPendingSuggestions)) {
            if (DBG) Log.d(TAG, "Suggestions now available, publishing: " + mPendingSuggestions);
            super.setSuggestions(mPendingSuggestions);
            // The suggestions are no longer pending.
            setPendingSuggestions(null);
        }
    }

    private class PendingSuggestionsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onPendingSuggestionsChanged();
        }
    }

}
