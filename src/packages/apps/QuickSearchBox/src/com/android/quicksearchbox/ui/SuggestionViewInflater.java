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

import com.android.quicksearchbox.R;
import com.android.quicksearchbox.Suggestion;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Inflates suggestion views.
 */
public class SuggestionViewInflater implements SuggestionViewFactory {

    // The suggestion view classes that may be returned by this factory.
    private static final Class<?>[] SUGGESTION_VIEW_CLASSES = {
            DefaultSuggestionView.class,
            ContactSuggestionView.class,
    };

    // The layout ids associated with each of the above classes.
    private static final int[] SUGGESTION_VIEW_LAYOUTS = {
            R.layout.suggestion,
            R.layout.contact_suggestion,
    };

    private static final String CONTACT_LOOKUP_URI
            = ContactsContract.Contacts.CONTENT_LOOKUP_URI.toString();

    private final Context mContext;

    public SuggestionViewInflater(Context context) {
        mContext = context;
    }

    protected LayoutInflater getInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getSuggestionViewTypeCount() {
        return SUGGESTION_VIEW_CLASSES.length;
    }

    public int getSuggestionViewType(Suggestion suggestion) {
        return isContactSuggestion(suggestion) ? 1 : 0;
    }

    public SuggestionView getSuggestionView(int viewType, View convertView,
            ViewGroup parentViewType) {
        if (convertView == null || !convertView.getClass().equals(
                SUGGESTION_VIEW_CLASSES[viewType])) {
            int layoutId = SUGGESTION_VIEW_LAYOUTS[viewType];
            convertView = getInflater().inflate(layoutId, parentViewType, false);
        }
        return (SuggestionView) convertView;
    }

    private boolean isContactSuggestion(Suggestion suggestion) {
        String intentData = suggestion.getSuggestionIntentDataString();
        return intentData != null && intentData.startsWith(CONTACT_LOOKUP_URI);
    }
}
