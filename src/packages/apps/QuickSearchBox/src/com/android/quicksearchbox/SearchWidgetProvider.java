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

import com.android.common.Search;
import com.android.common.speech.Recognition;
import com.android.quicksearchbox.ui.CorpusViewFactory;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.text.Annotation;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Random;

/**
 * Search widget provider.
 *
 */
public class SearchWidgetProvider extends BroadcastReceiver {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchWidgetProvider";

    /**
     * Broadcast intent action for showing the next voice search hint
     * (if voice search hints are enabled).
     */
    private static final String ACTION_NEXT_VOICE_SEARCH_HINT =
            "com.android.quicksearchbox.action.NEXT_VOICE_SEARCH_HINT";

    /**
     * Broadcast intent action for hiding voice search hints.
     */
    private static final String ACTION_HIDE_VOICE_SEARCH_HINT =
        "com.android.quicksearchbox.action.HIDE_VOICE_SEARCH_HINT";

    /**
     * Broadcast intent action for updating voice search hint display. Voice search hints will
     * only be displayed with some probability.
     */
    private static final String ACTION_CONSIDER_VOICE_SEARCH_HINT =
            "com.android.quicksearchbox.action.CONSIDER_VOICE_SEARCH_HINT";

    /**
     * Broadcast intent action for displaying voice search hints immediately, and resetting the
     * 'first seen' voice search timestamp, so we continue to show them for a while.
     */
    private static final String ACTION_SHOW_VOICE_SEARCH_HINT_NOW =
            "com.android.quicksearchbox.action.SHOW_VOICE_SEARCH_HINT_NOW";

    private static final String ACTION_RESET_VOICE_SEARCH_HINT_FIRST_SEEN =
            "com.android.quicksearchbox.action.debugonly.RESET_HINT_FIRST_SEEN_TIME";

    private static final String ACTION_SHOW_HINT_TEMPORARILY =
            "com.android.quicksearchbox.action.debugonly.SHOW_HINT_TEMPORARILY";

    /**
     * Preference key used for storing the index of the next voice search hint to show.
     */
    private static final String NEXT_VOICE_SEARCH_HINT_INDEX_PREF = "next_voice_search_hint";

    /**
     * Preference key used to store the time at which the first voice search hint was displayed.
     */
    private static final String FIRST_VOICE_HINT_DISPLAY_TIME = "first_voice_search_hint_time";

    /**
     * Preference key for the version of voice search we last got hints from.
     */
    private static final String LAST_SEEN_VOICE_SEARCH_VERSION = "voice_search_version";

    /**
     * The {@link Search#SOURCE} value used when starting searches from the search widget.
     */
    private static final String WIDGET_SEARCH_SOURCE = "launcher-widget";

    private static Random sRandom;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DBG) Log.d(TAG, "onReceive(" + intent.toUri(0) + ")");
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            scheduleVoiceHintUpdates(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            updateSearchWidgets(context);
        } else if (ACTION_CONSIDER_VOICE_SEARCH_HINT.equals(action)) {
            considerShowingVoiceSearchHints(context);
        } else if (ACTION_NEXT_VOICE_SEARCH_HINT.equals(action)) {
            getHintsFromVoiceSearch(context);
        } else if (ACTION_HIDE_VOICE_SEARCH_HINT.equals(action)) {
            hideVoiceSearchHint(context);
        } else if (ACTION_SHOW_VOICE_SEARCH_HINT_NOW.equals(action)) {
            showVoiceSearchHintNow(context);
            resetVoiceSearchHintFirstSeenTime(context);
        } else if (ACTION_SHOW_HINT_TEMPORARILY.equals(action)) {
            showVoiceSearchHintNow(context);
        } else if (ACTION_RESET_VOICE_SEARCH_HINT_FIRST_SEEN.equals(action)) {
            resetVoiceSearchHintFirstSeenTime(context);
        } else {
            if (DBG) Log.d(TAG, "Unhandled intent action=" + action);
        }
    }

    private static Random getRandom() {
        if (sRandom == null) {
            sRandom = new Random();
        }
        return sRandom;
    }

    private static void resetVoiceSearchHintFirstSeenTime(Context context) {
        SharedPreferences prefs = SearchSettings.getSearchPreferences(context);
        Editor e = prefs.edit();
        e.putLong(FIRST_VOICE_HINT_DISPLAY_TIME, System.currentTimeMillis());
        e.commit();
    }

    private static boolean haveVoiceSearchHintsExpired(Context context) {
        SharedPreferences prefs = SearchSettings.getSearchPreferences(context);
        QsbApplication app = QsbApplication.get(context);
        int currentVoiceSearchVersion = app.getVoiceSearch().getVersion();

        if (currentVoiceSearchVersion != 0) {
            long currentTime = System.currentTimeMillis();
            int lastVoiceSearchVersion = prefs.getInt(LAST_SEEN_VOICE_SEARCH_VERSION, 0);
            long firstHintTime = prefs.getLong(FIRST_VOICE_HINT_DISPLAY_TIME, 0);
            if (firstHintTime == 0 || currentVoiceSearchVersion != lastVoiceSearchVersion) {
                Editor e = prefs.edit();
                e.putInt(LAST_SEEN_VOICE_SEARCH_VERSION, currentVoiceSearchVersion);
                e.putLong(FIRST_VOICE_HINT_DISPLAY_TIME, currentTime);
                e.commit();
                firstHintTime = currentTime;
            }
            if (currentTime - firstHintTime > getConfig(context).getVoiceSearchHintActivePeriod()) {
                if (DBG) Log.d(TAG, "Voice seach hint period expired; not showing hints.");
                return true;
            } else {
                return false;
            }
        } else {
            if (DBG) Log.d(TAG, "Could not determine voice search version; not showing hints.");
            return true;
        }
    }

    private static boolean shouldShowVoiceSearchHints(Context context) {
        return (getConfig(context).allowVoiceSearchHints()
                && !haveVoiceSearchHintsExpired(context));
    }

    private static SearchWidgetState[] getSearchWidgetStates
            (Context context, boolean enableVoiceSearchHints) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(myComponentName(context));
        SearchWidgetState[] states = new SearchWidgetState[appWidgetIds.length];
        for (int i = 0; i<appWidgetIds.length; ++i) {
            states[i] = getSearchWidgetState(context, appWidgetIds[i], enableVoiceSearchHints);
        }
        return states;
    }

    private static void considerShowingVoiceSearchHints(Context context) {
        if (DBG) Log.d(TAG, "considerShowingVoiceSearchHints");
        if (!shouldShowVoiceSearchHints(context)) return;
        SearchWidgetState[] states = getSearchWidgetStates(context, true);
        boolean changed = false;
        for (SearchWidgetState state : states) {
            changed |= state.considerShowingHint(context);
        }
        if (changed) {
            getHintsFromVoiceSearch(context);
            scheduleNextVoiceSearchHint(context, true);
        }
    }

    private static void showVoiceSearchHintNow(Context context) {
        if (DBG) Log.d(TAG, "showVoiceSearchHintNow");
        SearchWidgetState[] states = getSearchWidgetStates(context, true);
        for (SearchWidgetState state : states) {
            if (state.mVoiceSearchIntent != null) {
                state.showHintNow(context);
            }
        }
        getHintsFromVoiceSearch(context);
        scheduleNextVoiceSearchHint(context, true);
    }

    private void hideVoiceSearchHint(Context context) {
        if (DBG) Log.d(TAG, "hideVoiceSearchHint");
        SearchWidgetState[] states = getSearchWidgetStates(context, true);
        boolean needHint = false;
        for (SearchWidgetState state : states) {
            if (state.isShowingHint()) {
                state.hideVoiceSearchHint(context);
                state.updateWidget(context, AppWidgetManager.getInstance(context));
            }
            needHint |= state.isShowingHint();
        }
        scheduleNextVoiceSearchHint(context, false);
    }

    private static void voiceSearchHintReceived(Context context, CharSequence hint) {
        if (DBG) Log.d(TAG, "voiceSearchHintReceived('" + hint + "')");
        CharSequence formatted = formatVoiceSearchHint(context, hint);
        SearchWidgetState[] states = getSearchWidgetStates(context, true);
        boolean needHint = false;
        for (SearchWidgetState state : states) {
            if (state.isShowingHint()) {
                state.setVoiceSearchHint(formatted);
                state.updateWidget(context, AppWidgetManager.getInstance(context));
                needHint = true;
            }
        }
        if (!needHint) {
            scheduleNextVoiceSearchHint(context, false);
        }
    }

    private static void scheduleVoiceHintUpdates(Context context) {
        if (DBG) Log.d(TAG, "scheduleVoiceHintUpdates");
        if (!shouldShowVoiceSearchHints(context)) return;
        scheduleVoiceSearchHintUpdates(context, true);
    }

    /**
     * Updates all search widgets.
     */
    public static void updateSearchWidgets(Context context) {
        if (DBG) Log.d(TAG, "updateSearchWidgets");
        boolean showVoiceSearchHints = shouldShowVoiceSearchHints(context);
        SearchWidgetState[] states = getSearchWidgetStates(context, showVoiceSearchHints);

        boolean needVoiceSearchHint = false;
        for (SearchWidgetState state : states) {
            if (state.isShowingHint()) {
                needVoiceSearchHint = true;
                // widget update will occur when voice search hint received
            } else {
                state.updateWidget(context, AppWidgetManager.getInstance(context));
            }
        }
        if (DBG) Log.d(TAG, "Need voice search hints=" + needVoiceSearchHint);
        if (needVoiceSearchHint) {
            getHintsFromVoiceSearch(context);
        }
        if (!showVoiceSearchHints) {
            scheduleVoiceSearchHintUpdates(context, false);
        }
    }

    /**
     * Gets the component name of this search widget provider.
     */
    private static ComponentName myComponentName(Context context) {
        String pkg = context.getPackageName();
        String cls = pkg + ".SearchWidgetProvider";
        return new ComponentName(pkg, cls);
    }

    private static Intent createQsbActivityIntent(Context context, String action,
            Bundle widgetAppData, Corpus corpus) {
        Intent qsbIntent = new Intent(action);
        qsbIntent.setPackage(context.getPackageName());
        qsbIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        qsbIntent.putExtra(SearchManager.APP_DATA, widgetAppData);
        qsbIntent.setData(SearchActivity.getCorpusUri(corpus));
        return qsbIntent;
    }

    private static SearchWidgetState getSearchWidgetState(Context context, 
            int appWidgetId, boolean enableVoiceSearchHints) {
        String corpusName =
                SearchWidgetConfigActivity.getWidgetCorpusName(context, appWidgetId);
        Corpus corpus = corpusName == null ? null : getCorpora(context).getCorpus(corpusName);
        if (DBG) Log.d(TAG, "Creating appwidget state " + appWidgetId + ", corpus=" + corpus);
        SearchWidgetState state = new SearchWidgetState(appWidgetId);

        Bundle widgetAppData = new Bundle();
        widgetAppData.putString(Search.SOURCE, WIDGET_SEARCH_SOURCE);

        // Query text view hint
        if (corpus == null || corpus.isWebCorpus()) {
            state.setQueryTextViewBackgroundResource(R.drawable.textfield_search_empty_google);
        } else {
            state.setQueryTextViewHint(corpus.getHint());
            state.setQueryTextViewBackgroundResource(R.drawable.textfield_search_empty);
        }

        // Text field click
        Intent qsbIntent = createQsbActivityIntent(
                context,
                SearchManager.INTENT_ACTION_GLOBAL_SEARCH,
                widgetAppData,
                corpus);
        state.setQueryTextViewIntent(qsbIntent);

        // Voice search button
        Intent voiceSearchIntent = getVoiceSearchIntent(context, corpus, widgetAppData);
        state.setVoiceSearchIntent(voiceSearchIntent);
        if (enableVoiceSearchHints && voiceSearchIntent != null
                && RecognizerIntent.ACTION_WEB_SEARCH.equals(voiceSearchIntent.getAction())) {
            state.setVoiceSearchHintsEnabled(true);

            boolean showingHint =
                    SearchWidgetConfigActivity.getWidgetShowingHint(context, appWidgetId);
            if (DBG) Log.d(TAG, "Widget " + appWidgetId + " showing hint: " + showingHint);
            state.setShowingHint(showingHint);

        }

        // Corpus indicator
        state.setCorpusIconUri(getCorpusIconUri(context, corpus));

        Intent corpusIconIntent = createQsbActivityIntent(context,
                SearchActivity.INTENT_ACTION_QSB_AND_SELECT_CORPUS, widgetAppData, corpus);
        state.setCorpusIndicatorIntent(corpusIconIntent);

        return state;
    }

    private static Intent getVoiceSearchIntent(Context context, Corpus corpus,
            Bundle widgetAppData) {
        VoiceSearch voiceSearch = QsbApplication.get(context).getVoiceSearch();

        if (corpus == null || !voiceSearch.isVoiceSearchAvailable()) {
            return voiceSearch.createVoiceWebSearchIntent(widgetAppData);
        } else {
            return corpus.createVoiceSearchIntent(widgetAppData);
        }
    }

    private static Intent getVoiceSearchHelpIntent(Context context) {
        VoiceSearch voiceSearch = QsbApplication.get(context).getVoiceSearch();
        return voiceSearch.createVoiceSearchHelpIntent();
    }

    private static PendingIntent createBroadcast(Context context, String action) {
        Intent intent = new Intent(action);
        intent.setComponent(myComponentName(context));
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static Uri getCorpusIconUri(Context context, Corpus corpus) {
        if (corpus == null) {
            return getCorpusViewFactory(context).getGlobalSearchIconUri();
        }
        return corpus.getCorpusIconUri();
    }

    private static CharSequence formatVoiceSearchHint(Context context, CharSequence hint) {
        if (TextUtils.isEmpty(hint)) return null;
        SpannableStringBuilder spannedHint = new SpannableStringBuilder(
                context.getString(R.string.voice_search_hint_quotation_start));
        spannedHint.append(hint);
        Object[] items = spannedHint.getSpans(0, spannedHint.length(), Object.class);
        for (Object item : items) {
            if (item instanceof Annotation) {
                Annotation annotation = (Annotation) item;
                if (annotation.getKey().equals("action")
                        && annotation.getValue().equals("true")) {
                    final int start = spannedHint.getSpanStart(annotation);
                    final int end = spannedHint.getSpanEnd(annotation);
                    spannedHint.removeSpan(item);
                    spannedHint.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
                }
            }
        }
        spannedHint.append(context.getString(R.string.voice_search_hint_quotation_end));
        return spannedHint;
    }

    private static void rescheduleAction(Context context, boolean reschedule, String action, long period) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = createBroadcast(context, action);
        alarmManager.cancel(intent);
        if (reschedule) {
            if (DBG) Log.d(TAG, "Scheduling action " + action + " after period " + period);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + period, period, intent);
        } else {
            if (DBG) Log.d(TAG, "Cancelled action " + action);
        }
    }

    public static void scheduleVoiceSearchHintUpdates(Context context, boolean enabled) {
        rescheduleAction(context, enabled, ACTION_CONSIDER_VOICE_SEARCH_HINT,
                getConfig(context).getVoiceSearchHintUpdatePeriod());
    }

    private static void scheduleNextVoiceSearchHint(Context context, boolean needUpdates) {
        rescheduleAction(context, needUpdates, ACTION_NEXT_VOICE_SEARCH_HINT,
                getConfig(context).getVoiceSearchHintChangePeriod());
    }

    /**
     * Requests an asynchronous update of the voice search hints.
     */
    private static void getHintsFromVoiceSearch(Context context) {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        intent.putExtra(Recognition.EXTRA_HINT_CONTEXT, Recognition.HINT_CONTEXT_LAUNCHER);
        if (DBG) Log.d(TAG, "Broadcasting " + intent);
        context.sendOrderedBroadcast(intent, null,
                new HintReceiver(), null, Activity.RESULT_OK, null, null);
    }

    private static class HintReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DBG) Log.d(TAG, "onReceive(" + intent.toUri(0) + ")");
            if (getResultCode() != Activity.RESULT_OK) {
                return;
            }
            ArrayList<CharSequence> hints = getResultExtras(true)
                    .getCharSequenceArrayList(Recognition.EXTRA_HINT_STRINGS);
            CharSequence hint = getNextHint(context, hints);
            voiceSearchHintReceived(context, hint);
        }
    }

    /**
     * Gets the next formatted hint, if there are any hints.
     * Must be called on the application main thread.
     *
     * @return A hint, or {@code null} if no hints are available.
     */
    private static CharSequence getNextHint(Context context, ArrayList<CharSequence> hints) {
        if (hints == null || hints.isEmpty()) return null;
        int i = getNextVoiceSearchHintIndex(context, hints.size());
        return hints.get(i);
    }

    private static int getNextVoiceSearchHintIndex(Context context, int size) {
        int i = getAndIncrementIntPreference(
                SearchSettings.getSearchPreferences(context),
                NEXT_VOICE_SEARCH_HINT_INDEX_PREF);
        return i % size;
    }

    // TODO: Could this be made atomic to avoid races?
    private static int getAndIncrementIntPreference(SharedPreferences prefs, String name) {
        int i = prefs.getInt(name, 0);
        prefs.edit().putInt(name, i + 1).commit();
        return i;
    }

    private static Config getConfig(Context context) {
        return QsbApplication.get(context).getConfig();
    }

    private static Corpora getCorpora(Context context) {
        return QsbApplication.get(context).getCorpora();
    }

    private static CorpusViewFactory getCorpusViewFactory(Context context) {
        return QsbApplication.get(context).getCorpusViewFactory();
    }

    private static class SearchWidgetState {
        private final int mAppWidgetId;
        private Uri mCorpusIconUri;
        private Intent mCorpusIndicatorIntent;
        private CharSequence mQueryTextViewHint;
        private int mQueryTextViewBackgroundResource;
        private Intent mQueryTextViewIntent;
        private Intent mVoiceSearchIntent;
        private boolean mVoiceSearchHintsEnabled;
        private CharSequence mVoiceSearchHint;
        private boolean mShowHint;

        public SearchWidgetState(int appWidgetId) {
            mAppWidgetId = appWidgetId;
        }

        public void setVoiceSearchHintsEnabled(boolean enabled) {
            mVoiceSearchHintsEnabled = enabled;
        }

        public void setShowingHint(boolean show) {
            mShowHint = show;
        }

        public boolean isShowingHint() {
            return mShowHint;
        }

        public void setCorpusIconUri(Uri corpusIconUri) {
            mCorpusIconUri = corpusIconUri;
        }

        public void setCorpusIndicatorIntent(Intent corpusIndicatorIntent) {
            mCorpusIndicatorIntent = corpusIndicatorIntent;
        }

        public void setQueryTextViewHint(CharSequence queryTextViewHint) {
            mQueryTextViewHint = queryTextViewHint;
        }

        public void setQueryTextViewBackgroundResource(int queryTextViewBackgroundResource) {
            mQueryTextViewBackgroundResource = queryTextViewBackgroundResource;
        }

        public void setQueryTextViewIntent(Intent queryTextViewIntent) {
            mQueryTextViewIntent = queryTextViewIntent;
        }

        public void setVoiceSearchIntent(Intent voiceSearchIntent) {
            mVoiceSearchIntent = voiceSearchIntent;
        }

        public void setVoiceSearchHint(CharSequence voiceSearchHint) {
            mVoiceSearchHint = voiceSearchHint;
        }

        private boolean chooseToShowHint(Context context) {
            // this is called every getConfig().getVoiceSearchHintUpdatePeriod() milliseconds
            // we want to return true every getConfig().getVoiceSearchHintShowPeriod() milliseconds
            // so:
            Config cfg = getConfig(context);
            float p = (float) cfg.getVoiceSearchHintUpdatePeriod()
                    / (float) cfg.getVoiceSearchHintShowPeriod();
            float f = getRandom().nextFloat();
            // if p > 1 we won't return true as often as we should (we can't return more times than
            // we're called!) but we will always return true.
            boolean r = (f < p);
            if (DBG) Log.d(TAG, "chooseToShowHint p=" + p +"; f=" + f + "; r=" + r);
            return r;
        }

        private void scheduleHintHiding(Context context) {
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent hideHint = createBroadcast(context, ACTION_HIDE_VOICE_SEARCH_HINT);

            long period = getConfig(context).getVoiceSearchHintVisibleTime();
            if (DBG) {
                Log.d(TAG, "Scheduling action " + ACTION_HIDE_VOICE_SEARCH_HINT +
                        " after period " + period);
            }
            alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + period, hideHint);

        }

        public void updateShowingHint(Context context) {
            SearchWidgetConfigActivity.setWidgetShowingHint(context, mAppWidgetId, mShowHint);
        }

        public boolean considerShowingHint(Context context) {
            if (!mVoiceSearchHintsEnabled || mShowHint) return false;
            if (!chooseToShowHint(context)) return false;
            showHintNow(context);
            return true;
        }

        public void showHintNow(Context context) {
            scheduleHintHiding(context);
            mShowHint = true;
            updateShowingHint(context);
        }

        public void hideVoiceSearchHint(Context context) {
            mShowHint = false;
            updateShowingHint(context);
        }

        public void updateWidget(Context context,AppWidgetManager appWidgetMgr) {
            if (DBG) Log.d(TAG, "Updating appwidget " + mAppWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_widget);
            // Corpus indicator
            // Before Froyo, android.resource URI could not be used in ImageViews.
            if (QsbApplication.isFroyoOrLater()) {
                views.setImageViewUri(R.id.corpus_indicator, mCorpusIconUri);
            }
            setOnClickActivityIntent(context, views, R.id.corpus_indicator,
                    mCorpusIndicatorIntent);
            // Query TextView
            views.setCharSequence(R.id.search_widget_text, "setHint", mQueryTextViewHint);
            setBackgroundResource(views, R.id.search_widget_text, mQueryTextViewBackgroundResource);

            setOnClickActivityIntent(context, views, R.id.search_widget_text,
                    mQueryTextViewIntent);
            // Voice Search button
            if (mVoiceSearchIntent != null) {
                setOnClickActivityIntent(context, views, R.id.search_widget_voice_btn,
                        mVoiceSearchIntent);
                views.setViewVisibility(R.id.search_widget_voice_btn, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.search_widget_voice_btn, View.GONE);
            }

            // Voice Search hints
            if (mShowHint && !TextUtils.isEmpty(mVoiceSearchHint)) {
                views.setTextViewText(R.id.voice_search_hint_text, mVoiceSearchHint);

                Intent voiceSearchHelp = getVoiceSearchHelpIntent(context);
                if (voiceSearchHelp == null) voiceSearchHelp = mVoiceSearchIntent;
                setOnClickActivityIntent(context, views, R.id.voice_search_hint,
                        voiceSearchHelp);

                PendingIntent hideIntent = createBroadcast(context, ACTION_HIDE_VOICE_SEARCH_HINT);
                views.setOnClickPendingIntent(R.id.voice_search_hint_close_button, hideIntent);

                views.setViewVisibility(R.id.voice_search_hint_container, View.VISIBLE);
                views.setViewVisibility(R.id.search_widget_text, View.GONE);
                views.setViewVisibility(R.id.corpus_indicator, View.GONE);
            } else {
                views.setViewVisibility(R.id.voice_search_hint_container, View.GONE);
                views.setViewVisibility(R.id.search_widget_text, View.VISIBLE);
                views.setViewVisibility(R.id.corpus_indicator, View.VISIBLE);
            }
            appWidgetMgr.updateAppWidget(mAppWidgetId, views);
        }

        private void setBackgroundResource(RemoteViews views, int viewId, int bgResource) {
            // setBackgroundResource did not have @RemotableViewMethod before Froyo
            if (QsbApplication.isFroyoOrLater()) {
                views.setInt(viewId, "setBackgroundResource", bgResource);
            }
        }

        private void setOnClickActivityIntent(Context context, RemoteViews views, int viewId,
                Intent intent) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(viewId, pendingIntent);
        }
    }

}
