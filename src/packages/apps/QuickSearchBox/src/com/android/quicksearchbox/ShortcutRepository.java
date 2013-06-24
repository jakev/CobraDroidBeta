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

import java.util.Collection;
import java.util.Map;

/**
 * Holds information about shortcuts (results the user has clicked on before), and returns
 * appropriate shortcuts for a given query.
 */
public interface ShortcutRepository {

    /**
     * Checks whether there is any stored history.
     */
    boolean hasHistory();

    /**
     * Clears all shortcut history.
     */
    void clearHistory();

    /**
     * Closes any database connections etc held by this object.
     */
    void close();

    /**
     * Reports a click on a suggestion.
     * Must be called on the UI thread.
     */
    void reportClick(SuggestionCursor suggestions, int position);

    /**
     * Gets shortcuts for a query.
     *
     * @param query The query. May be empty.
     * @param allowedCorpora The corpora to get shortcuts for.
     * @return A cursor containing shortcutted results for the query.
     */
    ShortcutCursor getShortcutsForQuery(String query, Collection<Corpus> allowedCorpora);

    /**
     * Updates a shortcut in the repository after it's been refreshed.
     *
     * @param source The source of the shortcut that's been refreshed
     * @param shortcutId The ID of the shortcut that's been refershed
     * @param refreshed The refreshed shortcut suggestion.
     */
    void updateShortcut(Source source, String shortcutId, SuggestionCursor refreshed);

    /**
     * @return A map for corpus name to score. A higher score means that the corpus
     *         is more important.
     */
    Map<String,Integer> getCorpusScores();
}
