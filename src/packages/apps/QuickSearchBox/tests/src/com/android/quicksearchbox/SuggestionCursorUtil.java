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

import junit.framework.Assert;

/**
 * Test utilities for {@link ShortcutCursor}.
 */
public class SuggestionCursorUtil extends Assert {

    public static void assertNoSuggestions(SuggestionCursor suggestions) {
        assertNoSuggestions("", suggestions);
    }

    public static void assertNoSuggestions(String message, SuggestionCursor suggestions) {
        assertNotNull(suggestions);
        assertEquals(message, 0, suggestions.getCount());
    }

    public static void assertSameSuggestion(String message, int position,
            SuggestionCursor expected, SuggestionCursor observed) {
        assertSameSuggestion(message, expected, position, observed, position);
    }

    public static void assertSameSuggestion(String message,
            SuggestionCursor expected, int positionExpected,
            SuggestionCursor observed, int positionObserved) {
        message +=  " at positions " + positionExpected + "(expected) "
                + positionObserved + " (observed)";
        expected.moveTo(positionExpected);
        observed.moveTo(positionObserved);
        assertSuggestionEquals(message, expected, observed);
    }

    public static void assertSameSuggestions(SuggestionCursor expected, SuggestionCursor observed) {
        assertSameSuggestions("", expected, observed, false);
    }

    public static void assertSameSuggestions(SuggestionCursor expected, SuggestionCursor observed,
            boolean allowExtras) {
        assertSameSuggestions("", expected, observed, allowExtras);
    }

    public static void assertSameSuggestions(
            String message, SuggestionCursor expected, SuggestionCursor observed) {
        assertSameSuggestions(message, expected, observed, false);
    }

    public static void assertSameSuggestions(
            String message, SuggestionCursor expected, SuggestionCursor observed,
            boolean allowExtras) {
        assertNotNull(expected);
        assertNotNull(message, observed);
        if (!allowExtras) {
            assertEquals(message + ", count", expected.getCount(), observed.getCount());
        } else {
            assertTrue(message + "count", expected.getCount() <= observed.getCount());
        }
        assertEquals(message + ", userQuery", expected.getUserQuery(), observed.getUserQuery());
        int count = expected.getCount();
        for (int i = 0; i < count; i++) {
            assertSameSuggestion(message, i, expected, observed);
        }
    }

    public static ListSuggestionCursor slice(SuggestionCursor cursor, int start, int length) {
        ListSuggestionCursor out = new ListSuggestionCursor(cursor.getUserQuery());
        for (int i = start; i < start + length; i++) {
            out.add(new SuggestionPosition(cursor, i));
        }
        return out;
    }

    public static void assertSuggestionEquals(Suggestion expected, Suggestion observed) {
        assertSuggestionEquals(null, expected, observed);
    }

    public static void assertSuggestionEquals(String message, Suggestion expected,
            Suggestion observed) {
        assertFieldEquals(message, "source", expected.getSuggestionSource(),
                observed.getSuggestionSource());
        assertFieldEquals(message, "shortcutId", expected.getShortcutId(),
                observed.getShortcutId());
        assertFieldEquals(message, "spinnerWhileRefreshing", expected.isSpinnerWhileRefreshing(),
                observed.isSpinnerWhileRefreshing());
        assertFieldEquals(message, "format", expected.getSuggestionFormat(),
                observed.getSuggestionFormat());
        assertFieldEquals(message, "icon1", expected.getSuggestionIcon1(),
                observed.getSuggestionIcon1());
        assertFieldEquals(message, "icon2", expected.getSuggestionIcon2(),
                observed.getSuggestionIcon2());
        assertFieldEquals(message, "text1", expected.getSuggestionText1(),
                observed.getSuggestionText1());
        assertFieldEquals(message, "text2", expected.getSuggestionText2(),
                observed.getSuggestionText2());
        assertFieldEquals(message, "text2Url", expected.getSuggestionText2Url(),
                observed.getSuggestionText2Url());
        assertFieldEquals(message, "action", expected.getSuggestionIntentAction(),
                observed.getSuggestionIntentAction());
        assertFieldEquals(message, "data", expected.getSuggestionIntentDataString(),
                observed.getSuggestionIntentDataString());
        assertFieldEquals(message, "extraData", expected.getSuggestionIntentExtraData(),
                observed.getSuggestionIntentExtraData());
        assertFieldEquals(message, "query", expected.getSuggestionQuery(),
                observed.getSuggestionQuery());
        assertFieldEquals(message, "logType", expected.getSuggestionLogType(),
                observed.getSuggestionLogType());
    }

    private static void assertFieldEquals(String message, String field,
            Object expected, Object observed) {
        String msg = (message == null) ? field : message + ", " + field;
        assertEquals(msg, expected, observed);
    }

    public static void addAll(ListSuggestionCursor to, SuggestionCursor from) {
        if (from == null) return;
        int count = from.getCount();
        for (int i = 0; i < count; i++) {
            to.add(new SuggestionPosition(from, i));
        }
    }
}
