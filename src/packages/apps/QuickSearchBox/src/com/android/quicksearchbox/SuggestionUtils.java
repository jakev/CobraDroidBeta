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

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Some utilities for suggestions.
 */
public class SuggestionUtils {

    private SuggestionUtils() {
    }

    public static Intent getSuggestionIntent(SuggestionCursor suggestion, Bundle appSearchData) {
        Source source = suggestion.getSuggestionSource();
        String action = suggestion.getSuggestionIntentAction();

        String data = suggestion.getSuggestionIntentDataString();
        String query = suggestion.getSuggestionQuery();
        String userQuery = suggestion.getUserQuery();
        String extraData = suggestion.getSuggestionIntentExtraData();

        // Now build the Intent
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // We need CLEAR_TOP to avoid reusing an old task that has other activities
        // on top of the one we want.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (data != null) {
            intent.setData(Uri.parse(data));
        }
        intent.putExtra(SearchManager.USER_QUERY, userQuery);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (extraData != null) {
            intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
        }
        if (appSearchData != null) {
            intent.putExtra(SearchManager.APP_DATA, appSearchData);
        }

        intent.setComponent(source.getIntentComponent());
        return intent;
    }

}
