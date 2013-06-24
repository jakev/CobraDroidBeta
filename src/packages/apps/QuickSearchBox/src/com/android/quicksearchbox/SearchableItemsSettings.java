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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

/**
 * Activity for selecting searchable items.
 */
public class SearchableItemsSettings extends PreferenceActivity
        implements OnPreferenceChangeListener {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchableItemsSettings";

    // Only used to find the preferences after inflating
    private static final String SEARCH_CORPORA_PREF = "search_corpora";

    // References to the top-level preference objects
    private PreferenceGroup mCorporaPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(SearchSettings.PREFERENCES_NAME);

        addPreferencesFromResource(R.xml.preferences_searchable_items);

        mCorporaPreferences = (PreferenceGroup) getPreferenceScreen().findPreference(
                SEARCH_CORPORA_PREF);

        populateSourcePreference();
    }

    private Corpora getCorpora() {
        return QsbApplication.get(this).getCorpora();
    }

    /**
     * Fills the suggestion source list.
     */
    private void populateSourcePreference() {
        mCorporaPreferences.setOrderingAsAdded(false);
        for (Corpus corpus : getCorpora().getAllCorpora()) {
            Preference pref = createCorpusPreference(corpus);
            if (pref != null) {
                if (DBG) Log.d(TAG, "Adding corpus: " + corpus);
                mCorporaPreferences.addPreference(pref);
            }
        }
    }

    /**
     * Adds a suggestion source to the list of suggestion source checkbox preferences.
     */
    private Preference createCorpusPreference(Corpus corpus) {
        SearchableItemPreference sourcePref = new SearchableItemPreference(this);
        sourcePref.setKey(SearchSettings.getCorpusEnabledPreference(corpus));
        // Put web corpus first. The rest are alphabetical.
        if (corpus.isWebCorpus()) {
            sourcePref.setOrder(0);
        }
        sourcePref.setDefaultValue(corpus.isCorpusDefaultEnabled());
        sourcePref.setOnPreferenceChangeListener(this);
        CharSequence label = corpus.getLabel();
        sourcePref.setTitle(label);
        CharSequence description = corpus.getSettingsDescription();
        sourcePref.setSummaryOn(description);
        sourcePref.setSummaryOff(description);
        sourcePref.setIcon(corpus.getCorpusIcon());
        return sourcePref;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SearchSettings.broadcastSettingsChanged(this);
        return true;
    }

}
