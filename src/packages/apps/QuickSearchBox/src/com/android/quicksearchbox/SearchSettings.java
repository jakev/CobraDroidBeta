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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.view.Menu;

import java.util.List;

/**
 * Activity for setting global search preferences.
 */
public class SearchSettings extends PreferenceActivity
        implements OnPreferenceClickListener {

    private static final boolean DBG = false;
    private static final String TAG = "SearchSettings";

    // Name of the preferences file used to store search preference
    public static final String PREFERENCES_NAME = "SearchSettings";

    // Intent action that opens the "Searchable Items" preference
    public static final String ACTION_SEARCHABLE_ITEMS =
            "com.android.quicksearchbox.action.SEARCHABLE_ITEMS";

    // Only used to find the preferences after inflating
    private static final String CLEAR_SHORTCUTS_PREF = "clear_shortcuts";
    private static final String SEARCH_ENGINE_SETTINGS_PREF = "search_engine_settings";
    private static final String SEARCH_CORPORA_PREF = "search_corpora";

    // Prefix of per-corpus enable preference
    private static final String CORPUS_ENABLED_PREF_PREFIX = "enable_corpus_";

    // References to the top-level preference objects
    private Preference mClearShortcutsPreference;
    private PreferenceScreen mSearchEngineSettingsPreference;

    // Dialog ids
    private static final int CLEAR_SHORTCUTS_CONFIRM_DIALOG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);

        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        mClearShortcutsPreference = preferenceScreen.findPreference(CLEAR_SHORTCUTS_PREF);
        mSearchEngineSettingsPreference = (PreferenceScreen) preferenceScreen.findPreference(
                SEARCH_ENGINE_SETTINGS_PREF);
        Preference corporaPreference = preferenceScreen.findPreference(SEARCH_CORPORA_PREF);
        corporaPreference.setIntent(getSearchableItemsIntent(this));

        mClearShortcutsPreference.setOnPreferenceClickListener(this);

        updateClearShortcutsPreference();
        populateSearchEnginePreference();
    }

    public static Intent getSearchableItemsIntent(Context context) {
        Intent intent = new Intent(SearchSettings.ACTION_SEARCHABLE_ITEMS);
        intent.setPackage(context.getPackageName());
        return intent;
    }

    /**
     * Gets the preference key of the preference for whether the given corpus
     * is enabled. The preference is stored in the {@link #PREFERENCES_NAME}
     * preferences file.
     */
    public static String getCorpusEnabledPreference(Corpus corpus) {
        return CORPUS_ENABLED_PREF_PREFIX + corpus.getName();
    }

    public static SharedPreferences getSearchPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private ShortcutRepository getShortcuts() {
        return QsbApplication.get(this).getShortcutRepository();
    }

    private Config getConfig() {
        return QsbApplication.get(this).getConfig();
    }

    /**
     * Enables/disables the "Clear search shortcuts" preference depending
     * on whether there is any search history.
     */
    private void updateClearShortcutsPreference() {
        boolean hasHistory = getShortcuts().hasHistory();
        if (DBG) Log.d(TAG, "hasHistory()=" + hasHistory);
        mClearShortcutsPreference.setEnabled(hasHistory);
    }

    /**
     * Populates the preference item for the web search engine, which links to further
     * search settings.
     */
    private void populateSearchEnginePreference() {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_WEB_SEARCH_SETTINGS);
        intent.setPackage(getPackageName());

        CharSequence webSearchSettingsLabel = getActivityLabel(intent);
        mSearchEngineSettingsPreference.setTitle(webSearchSettingsLabel);
        mSearchEngineSettingsPreference.setIntent(intent);
    }

    private CharSequence getActivityLabel(Intent intent) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (resolveInfos.size() == 0) {
            Log.e(TAG, "No web search settings activity");
            return null;
        }
        if (resolveInfos.size() > 1) {
            Log.e(TAG, "More than one web search settings activity");
            return null;
        }
        return resolveInfos.get(0).activityInfo.loadLabel(pm);
    }

    public synchronized boolean onPreferenceClick(Preference preference) {
        if (preference == mClearShortcutsPreference) {
            showDialog(CLEAR_SHORTCUTS_CONFIRM_DIALOG);
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case CLEAR_SHORTCUTS_CONFIRM_DIALOG:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_shortcuts)
                        .setMessage(R.string.clear_shortcuts_prompt)
                        .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (DBG) Log.d(TAG, "Clearing history...");
                                getShortcuts().clearHistory();
                                mClearShortcutsPreference.setEnabled(false);
                            }
                        })
                        .setNegativeButton(R.string.disagree, null).create();
            default:
                Log.e(TAG, "unknown dialog" + id);
                return null;
        }
    }

    /**
     * Informs our listeners about the updated settings data.
     */
    public static void broadcastSettingsChanged(Context context) {
        // We use a message broadcast since the listeners could be in multiple processes.
        Intent intent = new Intent(SearchManager.INTENT_ACTION_SEARCH_SETTINGS_CHANGED);
        Log.i(TAG, "Broadcasting: " + intent);
        context.sendBroadcast(intent);
    }

    public static boolean getShowWebSuggestions(Context context) {
        return (Settings.System.getInt(context.getContentResolver(),
                Settings.System.SHOW_WEB_SUGGESTIONS,
                1 /* default on until user actually changes it */) == 1);
    }

    public static void setShowWebSuggestions(Context context, boolean showWebSuggestions) {
        System.putInt(context.getContentResolver(), System.SHOW_WEB_SUGGESTIONS,
            showWebSuggestions ? 1 : 0);
    }

    public static void registerShowWebSuggestionsSettingObserver(
            Context context, ContentObserver observer) {
        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SHOW_WEB_SUGGESTIONS),
                false, observer);
    }

    public static void unregisterShowWebSuggestionsSettingObserver(
            Context context, ContentObserver observer) {
        context.getContentResolver().unregisterContentObserver(observer);
    }

    public static void addSearchSettingsMenuItem(Context context, Menu menu) {
        Intent settings = new Intent(SearchManager.INTENT_ACTION_SEARCH_SETTINGS);
        settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        // Don't show activity chooser if there are multiple search settings activities,
        // e.g. from different QSB implementations.
        settings.setPackage(context.getPackageName());
        menu.add(Menu.NONE, Menu.NONE, 0, R.string.menu_settings)
                .setIcon(R.drawable.ic_menu_preferences).setAlphabeticShortcut('P')
                .setIntent(settings);
    }
}
