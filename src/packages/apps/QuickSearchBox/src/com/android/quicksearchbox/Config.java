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

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.util.Log;

import java.util.HashSet;

/**
 * Provides values for configurable parameters in all of QSB.
 *
 * All the methods in this class return fixed default values. Subclasses may
 * make these values server-side settable.
 *
 */
public class Config {

    private static final String TAG = "QSB.Config";

    protected static final long SECOND_MILLIS = 1000L;
    protected static final long MINUTE_MILLIS = 60L * SECOND_MILLIS;
    protected static final long DAY_MILLIS = 86400000L;

    private static final int NUM_SUGGESTIONS_ABOVE_KEYBOARD = 4;
    private static final int NUM_PROMOTED_SOURCES = 3;
    private static final int MAX_PROMOTED_SUGGESTIONS = 8;
    private static final int MAX_RESULTS_PER_SOURCE = 50;
    private static final int MAX_SHORTCUTS_PER_WEB_SOURCE = MAX_PROMOTED_SUGGESTIONS;
    private static final int MAX_SHORTCUTS_PER_NON_WEB_SOURCE = 2;
    private static final long SOURCE_TIMEOUT_MILLIS = 10000;

    private static final int QUERY_THREAD_PRIORITY =
            Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE;

    private static final long MAX_STAT_AGE_MILLIS = 30 * DAY_MILLIS;
    private static final int MIN_CLICKS_FOR_SOURCE_RANKING = 3;

    private static final int NUM_WEB_CORPUS_THREADS = 2;

    private static final int LATENCY_LOG_FREQUENCY = 1000;

    private static final long TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS = 100;
    private static final long PUBLISH_RESULT_DELAY_MILLIS = 200;

    private static final long VOICE_SEARCH_HINT_ACTIVE_PERIOD = 7L * DAY_MILLIS;

    private static final long VOICE_SEARCH_HINT_UPDATE_INTERVAL
            = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    private static final long VOICE_SEARCH_HINT_SHOW_PERIOD_MILLIS
            = AlarmManager.INTERVAL_HOUR * 2;

    private static final long VOICE_SEARCH_HINT_CHANGE_PERIOD = 2L * MINUTE_MILLIS;

    private static final long VOICE_SEARCH_HINT_VISIBLE_PERIOD = 6L * MINUTE_MILLIS;

    private final Context mContext;
    private HashSet<String> mDefaultCorpora;
    private HashSet<String> mHiddenCorpora;

    /**
     * Creates a new config that uses hard-coded default values.
     */
    public Config(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * Releases any resources used by the configuration object.
     *
     * Default implementation does nothing.
     */
    public void close() {
    }

    private HashSet<String> loadResourceStringSet(int res) {
        HashSet<String> defaultCorpora = new HashSet<String>();
        try {
            String[] corpora = mContext.getResources().getStringArray(res);
            for (String corpus : corpora) {
                defaultCorpora.add(corpus);
            }
            return defaultCorpora;
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "Could not load resource string set", ex);
            return defaultCorpora;
        }
    }

    /**
     * Checks if we trust the given source not to be spammy.
     */
    public synchronized boolean isCorpusEnabledByDefault(String corpusName) {
        if (mDefaultCorpora == null) {
            mDefaultCorpora = loadResourceStringSet(R.array.default_corpora);
        }
        return mDefaultCorpora.contains(corpusName);
    }

    /**
     * Checks if the given corpus should be hidden from the corpus selection dialog.
     */
    public synchronized boolean isCorpusHidden(String corpusName) {
        if (mHiddenCorpora == null) {
            mHiddenCorpora = loadResourceStringSet(R.array.hidden_corpora);
        }
        return mHiddenCorpora.contains(corpusName);
    }

    /**
     * The number of promoted sources.
     */
    public int getNumPromotedSources() {
        return NUM_PROMOTED_SOURCES;
    }

    /**
     * The number of suggestions visible above the onscreen keyboard.
     */
    public int getNumSuggestionsAboveKeyboard() {
        try {
            // Get the list of default corpora from a resource, which allows vendor overlays.
            return mContext.getResources().getInteger(R.integer.num_suggestions_above_keyboard);
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "Could not load num_suggestions_above_keyboard", ex);
            return NUM_SUGGESTIONS_ABOVE_KEYBOARD;
        }
    }

    /**
     * The maximum number of suggestions to promote.
     */
    public int getMaxPromotedSuggestions() {
        return MAX_PROMOTED_SUGGESTIONS;
    }

    /**
     * The number of results to ask each source for.
     */
    public int getMaxResultsPerSource() {
        return MAX_RESULTS_PER_SOURCE;
    }

    /**
     * The maximum number of shortcuts to show for the web source in All mode.
     */
    public int getMaxShortcutsPerWebSource() {
        return MAX_SHORTCUTS_PER_WEB_SOURCE;
    }

    /**
     * The maximum number of shortcuts to show for each non-web source in All mode.
     */
    public int getMaxShortcutsPerNonWebSource() {
        return MAX_SHORTCUTS_PER_NON_WEB_SOURCE;
    }

    /**
     * The timeout for querying each source, in milliseconds.
     */
    public long getSourceTimeoutMillis() {
        return SOURCE_TIMEOUT_MILLIS;
    }

    /**
     * The priority of query threads.
     *
     * @return A thread priority, as defined in {@link Process}.
     */
    public int getQueryThreadPriority() {
        return QUERY_THREAD_PRIORITY;
    }

    /**
     * The maximum age of log data used for shortcuts.
     */
    public long getMaxStatAgeMillis(){
        return MAX_STAT_AGE_MILLIS;
    }

    /**
     * The minimum number of clicks needed to rank a source.
     */
    public int getMinClicksForSourceRanking(){
        return MIN_CLICKS_FOR_SOURCE_RANKING;
    }

    public int getNumWebCorpusThreads() {
        return NUM_WEB_CORPUS_THREADS;
    }

    /**
     * How often query latency should be logged.
     *
     * @return An integer in the range 0-1000. 0 means that no latency events
     *         should be logged. 1000 means that all latency events should be logged.
     */
    public int getLatencyLogFrequency() {
        return LATENCY_LOG_FREQUENCY;
    }

    /**
     * The delay in milliseconds before suggestions are updated while typing.
     * If a new character is typed before this timeout expires, the timeout is reset.
     */
    public long getTypingUpdateSuggestionsDelayMillis() {
        return TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS;
    }

    /**
     * The delay in milliseconds before corpus results are published.
     * If a new result arrives before this timeout expires, the timeout is reset.
     */
    public long getPublishResultDelayMillis() {
        return PUBLISH_RESULT_DELAY_MILLIS;
    }

    public boolean allowVoiceSearchHints() {
        return true;
    }

    /**
     * The period of time for which after installing voice search we should consider showing voice
     * search hints.
     *
     * @return The period in milliseconds.
     */
    public long getVoiceSearchHintActivePeriod() {
        return VOICE_SEARCH_HINT_ACTIVE_PERIOD;
    }

    /**
     * The time interval at which we should consider whether or not to show some voice search hints.
     *
     * @return The period in milliseconds.
     */
    public long getVoiceSearchHintUpdatePeriod() {
        return VOICE_SEARCH_HINT_UPDATE_INTERVAL;
    }

    /**
     * The time interval at which, on average, voice search hints are displayed.
     *
     * @return The period in milliseconds.
     */
    public long getVoiceSearchHintShowPeriod() {
        return VOICE_SEARCH_HINT_SHOW_PERIOD_MILLIS;
    }

    /**
     * The amount of time for which voice search hints are displayed in one go.
     *
     * @return The period in milliseconds.
     */
    public long getVoiceSearchHintVisibleTime() {
        return VOICE_SEARCH_HINT_VISIBLE_PERIOD;
    }

    /**
     * The period that we change voice search hints at while they're being displayed.
     *
     * @return The period in milliseconds.
     */
    public long getVoiceSearchHintChangePeriod() {
        return VOICE_SEARCH_HINT_CHANGE_PERIOD;
    }
}
