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

import android.util.Log;

import java.util.ArrayList;

/**
 * A promoter that first promotes any shortcuts, and then delegates to another
 * promoter.
 */
public class ShortcutPromoter extends PromoterWrapper {

    private static final String TAG = "QSB.ShortcutPromoter";
    private static final boolean DBG = false;

    /**
     * Creates a new ShortcutPromoter.
     *
     * @param nextPromoter The promoter to use when there are no more shortcuts.
     *        May be {@code null}.
     */
    public ShortcutPromoter(Promoter nextPromoter) {
        super(nextPromoter);
    }

    @Override
    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        int shortcutCount = shortcuts == null ? 0 : shortcuts.getCount();
        int promotedShortcutCount = Math.min(shortcutCount, maxPromoted);
        if (DBG) {
            Log.d(TAG, "pickPromoted(shortcutCount = " + shortcutCount +
                    ", maxPromoted = " + maxPromoted + ")");
        }

        for (int i = 0; i < promotedShortcutCount; i++) {
            promoted.add(new SuggestionPosition(shortcuts, i));
        }

        super.pickPromoted(null, suggestions, maxPromoted, promoted);
    }

}
