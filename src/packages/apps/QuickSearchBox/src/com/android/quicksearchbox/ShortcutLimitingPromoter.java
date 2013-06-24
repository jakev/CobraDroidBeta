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

import com.google.common.collect.HashMultiset;

import android.util.Log;

import java.util.ArrayList;

/**
 * A promoter that limits the maximum number of shortcuts per source
 * (from non-web soruces), and then delegates promotion to another promoter.
 */
public class ShortcutLimitingPromoter extends PromoterWrapper {

    private static final String TAG = "QSB.ShortcutLimitingPromoter";
    private static final boolean DBG = false;

    private final int mMaxShortcutsPerWebSource;
    private final int mMaxShortcutsPerNonWebSource;

    /**
     * Creates a new ShortcutPromoter.
     *
     * @param nextPromoter The promoter to use when there are no more shortcuts.
     *        May be {@code null}.
     */
    public ShortcutLimitingPromoter(int maxShortcutsPerWebSource,
            int maxShortcutsPerNonWebSource, Promoter nextPromoter) {
        super(nextPromoter);
        mMaxShortcutsPerWebSource = maxShortcutsPerWebSource;
        mMaxShortcutsPerNonWebSource = maxShortcutsPerNonWebSource;
    }

    @Override
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        final int shortcutCount = shortcuts == null ? 0 : shortcuts.getCount();
        ListSuggestionCursor filteredShortcuts = null;
        if (shortcutCount > 0) {
            filteredShortcuts = new ListSuggestionCursor(shortcuts.getUserQuery());
            HashMultiset<Source> sourceShortcutCounts = HashMultiset.create(shortcutCount);
            int numPromoted = 0;
            for (int i = 0; i < shortcutCount; i++) {
                shortcuts.moveTo(i);
                Source source = shortcuts.getSuggestionSource();
                if (source != null) {
                    int prevCount = sourceShortcutCounts.add(source, 1);
                    if (DBG) Log.d(TAG, "Source: " + source + ", count: " + prevCount);
                    int maxShortcuts = source.isWebSuggestionSource()
                            ? mMaxShortcutsPerWebSource : mMaxShortcutsPerNonWebSource;
                    if (prevCount < maxShortcuts) {
                        numPromoted++;
                        filteredShortcuts.add(new SuggestionPosition(shortcuts));
                    }
                    if (numPromoted >= maxPromoted) break;
                }
            }
        }
        if (DBG) {
            Log.d(TAG, "pickPromoted shortcuts=" + shortcutCount + " filtered=" +
                    filteredShortcuts.getCount());
        }
        super.pickPromoted(filteredShortcuts, suggestions, maxPromoted, promoted);
    }

}
