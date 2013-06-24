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

import java.util.ArrayList;

/**
 * A simple promoter that concatenates the source results and ignores the shortcuts.
 */
public class ConcatPromoter implements Promoter {

    public void pickPromoted(SuggestionCursor shortcuts,
            ArrayList<CorpusResult> suggestions, int maxPromoted,
            ListSuggestionCursor promoted) {
        for (SuggestionCursor c : suggestions) {
            for (int i = 0; i < c.getCount(); i++) {
                if (promoted.getCount() >= maxPromoted) {
                    return;
                }
                promoted.add(new SuggestionPosition(c, i));
            }
        }
    }

}
