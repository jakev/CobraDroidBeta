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

import com.android.quicksearchbox.util.BatchingNamedTaskExecutor;
import com.android.quicksearchbox.util.Consumer;
import com.android.quicksearchbox.util.NamedTaskExecutor;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Suggestions provider implementation.
 *
 * The provider will only handle a single query at a time. If a new query comes
 * in, the old one is cancelled.
 */
public class SuggestionsProviderImpl implements SuggestionsProvider {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SuggestionsProviderImpl";

    private final Config mConfig;

    private final NamedTaskExecutor mQueryExecutor;

    private final Handler mPublishThread;

    private Promoter mAllPromoter;

    private Promoter mSingleCorpusPromoter;

    private final ShortcutRepository mShortcutRepo;

    private final ShouldQueryStrategy mShouldQueryStrategy = new ShouldQueryStrategy();

    private final Corpora mCorpora;

    private final CorpusRanker mCorpusRanker;

    private final Logger mLogger;

    private BatchingNamedTaskExecutor mBatchingExecutor;

    public SuggestionsProviderImpl(Config config,
            NamedTaskExecutor queryExecutor,
            Handler publishThread,
            ShortcutRepository shortcutRepo,
            Corpora corpora,
            CorpusRanker corpusRanker,
            Logger logger) {
        mConfig = config;
        mQueryExecutor = queryExecutor;
        mPublishThread = publishThread;
        mShortcutRepo = shortcutRepo;
        mCorpora = corpora;
        mCorpusRanker = corpusRanker;
        mLogger = logger;
    }

    /**
     * Sets the promoter used in All mode.
     */
    public void setAllPromoter(Promoter promoter) {
        mAllPromoter = promoter;
    }

    /**
     * Sets the promoter used in single corpus mode.
     */
    public void setSingleCorpusPromoter(Promoter promoter) {
        mSingleCorpusPromoter = promoter;
    }

    public void close() {
        cancelPendingTasks();
    }

    /**
     * Cancels all pending query tasks.
     */
    private void cancelPendingTasks() {
        if (mBatchingExecutor != null) {
            mBatchingExecutor.cancelPendingTasks();
            mBatchingExecutor = null;
        }
    }

    protected ShortcutCursor getShortcutsForQuery(String query, Corpus singleCorpus) {
        if (mShortcutRepo == null) return null;
        Collection<Corpus> allowedCorpora;
        if (singleCorpus == null) {
            allowedCorpora = mCorpora.getEnabledCorpora();
        } else {
            allowedCorpora = Collections.singletonList(singleCorpus);
        }
        return mShortcutRepo.getShortcutsForQuery(query, allowedCorpora);
    }

    /**
     * Gets the sources that should be queried for the given query.
     */
    private List<Corpus> getCorporaToQuery(String query, Corpus singleCorpus) {
        if (singleCorpus != null) return Collections.singletonList(singleCorpus);
        List<Corpus> orderedCorpora = mCorpusRanker.getRankedCorpora();
        if (DBG) Log.d(TAG, "getCorporaToQuery query='"+query+"' orderedCorpora="+orderedCorpora);
        ArrayList<Corpus> corporaToQuery = new ArrayList<Corpus>(orderedCorpora.size());
        for (Corpus corpus : orderedCorpora) {
            if (shouldQueryCorpus(corpus, query)) {
                if (DBG) Log.d(TAG, "should query corpus " + corpus);
                corporaToQuery.add(corpus);
            } else {
                if (DBG) Log.d(TAG, "should NOT query corpus " + corpus);
            }
        }
        if (DBG) Log.d(TAG, "getCorporaToQuery corporaToQuery=" + corporaToQuery);
        return corporaToQuery;
    }

    protected boolean shouldQueryCorpus(Corpus corpus, String query) {
        if (query.length() == 0 && !corpus.isWebCorpus()) {
            // Only the web corpus sees zero length queries.
            return false;
        }
        return mShouldQueryStrategy.shouldQueryCorpus(corpus, query);
    }

    private void updateShouldQueryStrategy(CorpusResult cursor) {
        if (cursor.getCount() == 0) {
            mShouldQueryStrategy.onZeroResults(cursor.getCorpus(),
                    cursor.getUserQuery());
        }
    }

    public Suggestions getSuggestions(String query, Corpus singleCorpus, int maxSuggestions) {
        if (DBG) Log.d(TAG, "getSuggestions(" + query + ")");
        cancelPendingTasks();
        List<Corpus> corporaToQuery = getCorporaToQuery(query, singleCorpus);
        Promoter promoter = singleCorpus == null ? mAllPromoter : mSingleCorpusPromoter;
        final Suggestions suggestions = new Suggestions(promoter,
                maxSuggestions,
                query,
                corporaToQuery);
        ShortcutCursor shortcuts = getShortcutsForQuery(query, singleCorpus);
        if (shortcuts != null) {
            suggestions.setShortcuts(shortcuts);
        }

        // Fast path for the zero sources case
        if (corporaToQuery.size() == 0) {
            return suggestions;
        }

        int initialBatchSize = countDefaultCorpora(corporaToQuery);
        if (initialBatchSize == 0) {
            initialBatchSize = mConfig.getNumPromotedSources();
        }

        mBatchingExecutor = new BatchingNamedTaskExecutor(mQueryExecutor);

        long publishResultDelayMillis = mConfig.getPublishResultDelayMillis();
        SuggestionCursorReceiver receiver = new SuggestionCursorReceiver(
                mBatchingExecutor, suggestions, initialBatchSize,
                publishResultDelayMillis);

        int maxResultsPerSource = mConfig.getMaxResultsPerSource();
        QueryTask.startQueries(query, maxResultsPerSource, corporaToQuery, mBatchingExecutor,
                mPublishThread, receiver, singleCorpus != null);
        mBatchingExecutor.executeNextBatch(initialBatchSize);

        return suggestions;
    }

    private int countDefaultCorpora(List<Corpus> corpora) {
        int count = 0;
        for (Corpus corpus : corpora) {
            if (corpus.isCorpusDefaultEnabled()) {
                count++;
            }
        }
        return count;
    }

    private class SuggestionCursorReceiver implements Consumer<CorpusResult> {
        private final BatchingNamedTaskExecutor mExecutor;
        private final Suggestions mSuggestions;
        private final long mResultPublishDelayMillis;
        private final ArrayList<CorpusResult> mPendingResults;
        private final Runnable mResultPublishTask = new Runnable () {
            public void run() {
                if (DBG) Log.d(TAG, "Publishing delayed results");
                publishPendingResults();
            }
        };

        private int mCountAtWhichToExecuteNextBatch;

        public SuggestionCursorReceiver(BatchingNamedTaskExecutor executor,
                Suggestions suggestions, int initialBatchSize,
                long publishResultDelayMillis) {
            mExecutor = executor;
            mSuggestions = suggestions;
            mCountAtWhichToExecuteNextBatch = initialBatchSize;
            mResultPublishDelayMillis = publishResultDelayMillis;
            mPendingResults = new ArrayList<CorpusResult>();
        }

        public boolean consume(CorpusResult cursor) {
            updateShouldQueryStrategy(cursor);
            mPendingResults.add(cursor);
            if (mResultPublishDelayMillis > 0
                    && !mSuggestions.isClosed()
                    && mSuggestions.getResultCount() + mPendingResults.size()
                            < mCountAtWhichToExecuteNextBatch) {
                // This is not the last result of the batch, delay publishing
                if (DBG) Log.d(TAG, "Delaying result by " + mResultPublishDelayMillis + " ms");
                mPublishThread.removeCallbacks(mResultPublishTask);
                mPublishThread.postDelayed(mResultPublishTask, mResultPublishDelayMillis);
            } else {
                // This is the last result, publish immediately
                if (DBG) Log.d(TAG, "Publishing result immediately");
                mPublishThread.removeCallbacks(mResultPublishTask);
                publishPendingResults();
            }
            if (!mSuggestions.isClosed()) {
                executeNextBatchIfNeeded();
            }
            if (cursor != null && mLogger != null) {
                mLogger.logLatency(cursor);
            }
            return true;
        }

        private void publishPendingResults() {
            mSuggestions.addCorpusResults(mPendingResults);
            mPendingResults.clear();
        }

        private void executeNextBatchIfNeeded() {
            if (mSuggestions.getResultCount() == mCountAtWhichToExecuteNextBatch) {
                // We've just finished one batch
                if (mSuggestions.getPromoted().getCount() < mConfig.getMaxPromotedSuggestions()) {
                    // But we still don't have enough results, ask for more
                    int nextBatchSize = mConfig.getNumPromotedSources();
                    mCountAtWhichToExecuteNextBatch += nextBatchSize;
                    mExecutor.executeNextBatch(nextBatchSize);
                }
            }
        }
    }

}
