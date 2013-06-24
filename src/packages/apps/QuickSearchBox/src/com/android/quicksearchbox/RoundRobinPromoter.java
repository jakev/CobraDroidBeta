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
 * A promoter that promotes one suggestion from each source.
 *
 */
public class RoundRobinPromoter implements Promoter {

    private static final String TAG = "QSB.RoundRobinPromoter";
    private static final boolean DBG = false;

    /**
     * Creates a new RoundRobinPromoter.
     */
    public RoundRobinPromoter() {
    }

    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        if (DBG) Log.d(TAG, "pickPromoted(maxPromoted = " + maxPromoted + ")");
        final int sourceCount = suggestions.size();
        if (sourceCount == 0) return;
        int sourcePos = 0;
        int suggestionPos = 0;
        int maxCount = 0;
        // TODO: This is inefficient when there are several exhausted sources.
        while (promoted.getCount() < maxPromoted) {
            SuggestionCursor sourceResult = suggestions.get(sourcePos);
            int count = sourceResult.getCount();
            if (count > maxCount) maxCount = count;
            if (suggestionPos < count) {
                if (DBG) Log.d(TAG, "Promoting " + sourcePos + ":" + suggestionPos);
                promoted.add(new SuggestionPosition(sourceResult, suggestionPos));
            }
            sourcePos++;
            if (sourcePos >= sourceCount) {
                sourcePos = 0;
                suggestionPos++;
                if (suggestionPos >= maxCount) break;
            }
        }
    }

}
