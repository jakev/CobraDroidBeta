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

package com.android.quicksearchbox;

import com.google.common.annotations.VisibleForTesting;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains all {@link SuggestionCursor} objects that have been reported.
 */
public class Suggestions {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.Suggestions";
    private static int sId = 0;
    // Object ID for debugging
    private final int mId;

    private final int mMaxPromoted;

    private final String mQuery;

    /** The sources that are expected to report. */
    private final List<Corpus> mExpectedCorpora;

    private Corpus mSingleCorpusFilter;

    /**
     * The observers that want notifications of changes to the published suggestions.
     * This object may be accessed on any thread.
     */
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    /**
     * All {@link SuggestionCursor} objects that have been published so far,
     * in the order that they were published.
     * This object may only be accessed on the UI thread.
     * */
    private final ArrayList<CorpusResult> mCorpusResults;

    private ShortcutCursor mShortcuts;

    private final MyShortcutsObserver mShortcutsObserver = new MyShortcutsObserver();

    /** True if {@link Suggestions#close} has been called. */
    private boolean mClosed = false;

    private final Promoter mPromoter;

    private SuggestionCursor mPromoted;

    /**
     * Creates a new empty Suggestions.
     *
     * @param expectedCorpora The sources that are expected to report.
     */
    public Suggestions(Promoter promoter, int maxPromoted,
            String query, List<Corpus> expectedCorpora) {
        mPromoter = promoter;
        mMaxPromoted = maxPromoted;
        mQuery = query;
        mExpectedCorpora = expectedCorpora;
        mCorpusResults = new ArrayList<CorpusResult>(mExpectedCorpora.size());
        mPromoted = null;  // will be set by updatePromoted()
        mId = sId++;
        if (DBG) {
            Log.d(TAG, "new Suggestions [" + mId + "] query \"" + query
                    + "\" expected corpora: " + mExpectedCorpora);
        }
    }

    @VisibleForTesting
    public String getQuery() {
        return mQuery;
    }

    public List<Corpus> getExpectedCorpora() {
        return mExpectedCorpora;
    }

    /**
     * Gets the number of corpora that are expected to report.
     */
    @VisibleForTesting
    int getExpectedResultCount() {
        return mExpectedCorpora.size();
    }

    /**
     * Registers an observer that will be notified when the reported results or
     * the done status changes.
     */
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mClosed) {
            throw new IllegalStateException("registerDataSetObserver() when closed");
        }
        mDataSetObservable.registerObserver(observer);
    }

    /**
     * Unregisters an observer.
     */
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public SuggestionCursor getPromoted() {
        if (mPromoted == null) {
            updatePromoted();
        }
        if (DBG) Log.d(TAG, "getPromoted() = " + mPromoted);
        return mPromoted;
    }

    /**
     * Gets the set of corpora that have reported results to this suggestions set.
     *
     * @return A collection of corpora.
     */
    public Set<Corpus> getIncludedCorpora() {
        HashSet<Corpus> corpora = new HashSet<Corpus>();
        for (CorpusResult result : mCorpusResults) {
            corpora.add(result.getCorpus());
        }
        return corpora;
    }

    /**
     * Calls {@link DataSetObserver#onChanged()} on all observers.
     */
    private void notifyDataSetChanged() {
        if (DBG) Log.d(TAG, "notifyDataSetChanged()");
        mDataSetObservable.notifyChanged();
    }

    /**
     * Closes all the source results and unregisters all observers.
     */
    public void close() {
        if (DBG) Log.d(TAG, "close() [" + mId + "]");
        if (mClosed) {
            throw new IllegalStateException("Double close()");
        }
        mDataSetObservable.unregisterAll();
        mClosed = true;
        if (mShortcuts != null) {
            mShortcuts.close();
            mShortcuts = null;
        }
        for (CorpusResult result : mCorpusResults) {
            result.close();
        }
        mCorpusResults.clear();
    }

    public boolean isClosed() {
        return mClosed;
    }

    @Override
    protected void finalize() {
        if (!mClosed) {
            Log.e(TAG, "LEAK! Finalized without being closed: Suggestions[" + mQuery + "]");
        }
    }

    /**
     * Checks whether all sources have reported.
     * Must be called on the UI thread, or before this object is seen by the UI thread.
     */
    public boolean isDone() {
        // TODO: Handle early completion because we have all the results we want.
        return mCorpusResults.size() >= mExpectedCorpora.size();
    }

    /**
     * Sets the shortcut suggestions.
     * Must be called on the UI thread, or before this object is seen by the UI thread.
     *
     * @param shortcuts The shortcuts.
     */
    public void setShortcuts(ShortcutCursor shortcuts) {
        if (DBG) Log.d(TAG, "setShortcuts(" + shortcuts + ")");
        mShortcuts = shortcuts;
        if (shortcuts != null) {
            mShortcuts.registerDataSetObserver(mShortcutsObserver);
        }
    }

    /**
     * Adds a list of corpus results. Must be called on the UI thread, or before this
     * object is seen by the UI thread.
     */
    public void addCorpusResults(List<CorpusResult> corpusResults) {
        if (mClosed) {
            for (CorpusResult corpusResult : corpusResults) {
                corpusResult.close();
            }
            return;
        }

        for (CorpusResult corpusResult : corpusResults) {
            if (DBG) {
                Log.d(TAG, "addCorpusResult["+ mId + "] corpus:" +
                        corpusResult.getCorpus().getName() + " results:" + corpusResult.getCount());
            }
            if (!mQuery.equals(corpusResult.getUserQuery())) {
              throw new IllegalArgumentException("Got result for wrong query: "
                    + mQuery + " != " + corpusResult.getUserQuery());
            }
            mCorpusResults.add(corpusResult);
        }
        mPromoted = null;
        notifyDataSetChanged();
    }

    private void updatePromoted() {
        if (mSingleCorpusFilter == null) {
            ListSuggestionCursor promoted = new ListSuggestionCursorNoDuplicates(mQuery);
            mPromoted = promoted;
            if (mPromoter == null) {
                return;
            }
            mPromoter.pickPromoted(mShortcuts, mCorpusResults, mMaxPromoted, promoted);
            if (DBG) {
                Log.d(TAG, "pickPromoted(" + mShortcuts + "," + mCorpusResults + ","
                        + mMaxPromoted + ") = " + mPromoted);
            }
            refreshShortcuts();
        } else {
            mPromoted = getCorpusResult(mSingleCorpusFilter);
            if (mPromoted == null) {
                mPromoted = new ListSuggestionCursor(mQuery);
            }
        }
    }

    private void refreshShortcuts() {
        if (DBG) Log.d(TAG, "refreshShortcuts(" + mPromoted + ")");
        for (int i = 0; i < mPromoted.getCount(); ++i) {
            mPromoted.moveTo(i);
            if (mPromoted.isSuggestionShortcut()) {
                mShortcuts.refresh(mPromoted);
            }
        }
    }


    private CorpusResult getCorpusResult(Corpus corpus) {
        for (CorpusResult result : mCorpusResults) {
            if (result.getCorpus().equals(mSingleCorpusFilter)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Gets the number of source results.
     * Must be called on the UI thread, or before this object is seen by the UI thread.
     */
    public int getResultCount() {
        if (mClosed) {
            throw new IllegalStateException("Called getSourceCount() when closed.");
        }
        return mCorpusResults == null ? 0 : mCorpusResults.size();
    }

    public void filterByCorpus(Corpus singleCorpus) {
        if (mSingleCorpusFilter == singleCorpus) {
            return;
        }
        mSingleCorpusFilter = singleCorpus;
        if ((mExpectedCorpora.size() == 1) && (mExpectedCorpora.get(0) == singleCorpus)) {
            return;
        }
        updatePromoted();
        notifyDataSetChanged();
    }

    @Override
    public String toString() {
        return "Suggestions{expectedCorpora=" + mExpectedCorpora
                + ",mCorpusResults.size()=" + mCorpusResults.size() + "}";
    }

    private class MyShortcutsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mPromoted = null;
            notifyDataSetChanged();
        }
    }

}
