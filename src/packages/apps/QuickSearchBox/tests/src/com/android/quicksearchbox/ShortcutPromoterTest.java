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

import static com.android.quicksearchbox.SuggestionCursorUtil.assertSameSuggestion;
import static com.android.quicksearchbox.SuggestionCursorUtil.assertSameSuggestions;
import static com.android.quicksearchbox.SuggestionCursorUtil.slice;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link ShortcutPromoter}.
 */
@MediumTest
public class ShortcutPromoterTest extends AndroidTestCase {

    private String mQuery;

    private SuggestionCursor mShortcuts;

    private ArrayList<CorpusResult> mSuggestions;

    private int mSuggestionCount;

    @Override
    protected void setUp() throws Exception {
        mQuery = "foo";
        List<Corpus> corpora = Arrays.asList(MockCorpus.CORPUS_1, MockCorpus.CORPUS_2);
        mShortcuts = new MockShortcutRepository().getShortcutsForQuery(mQuery, null);
        mSuggestions = new ArrayList<CorpusResult>();
        for (Corpus corpus : corpora) {
            mSuggestions.add(corpus.getSuggestions(mQuery, 10, false));
        }
        mSuggestionCount = countSuggestions(mSuggestions);
    }

    @Override
    protected void tearDown() throws Exception {
        mQuery = null;
        mShortcuts.close();
        for (SuggestionCursor c : mSuggestions) {
            c.close();
        }
        mSuggestions = null;
    }

    public void testPickPromotedNoNext() {
        maxPromotedTestNoNext(0);
        maxPromotedTestNoNext(1);
        maxPromotedTestNoNext(2);
        maxPromotedTestNoNext(5);
    }

    public void testPickPromotedConcatNext() {
        maxPromotedTestConcatNext(0);
        maxPromotedTestConcatNext(1);
        maxPromotedTestConcatNext(2);
        maxPromotedTestConcatNext(6);
        maxPromotedTestConcatNext(7);
    }

    private void maxPromotedTestNoNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(null);
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount());
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        int count = Math.min(maxPromoted, mShortcuts.getCount());
        assertSameSuggestions(slice(promoted, 0, count), slice(mShortcuts, 0, count));
    }

    private void maxPromotedTestConcatNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(new ConcatPromoter());
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount() + mSuggestionCount);
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        int count = Math.min(maxPromoted, mShortcuts.getCount());
        assertSameSuggestions(slice(promoted, 0, count), slice(mShortcuts, 0, count));
        if (mShortcuts.getCount() < expectedCount) {
            assertSameSuggestion("wrong suggestion after shortcuts",
                    promoted, mShortcuts.getCount(), mSuggestions.get(0), 0);
        }
    }

    private static int countSuggestions(ArrayList<CorpusResult> suggestions) {
        int count = 0;
        for (SuggestionCursor c : suggestions) {
            count += c.getCount();
        }
        return count;
    }
}
