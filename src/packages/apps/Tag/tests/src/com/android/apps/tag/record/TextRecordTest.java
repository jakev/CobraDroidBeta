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

package com.android.apps.tag.record;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.test.AndroidTestCase;

import com.android.apps.tag.MockNdefMessages;

import java.util.Locale;

/**
 * Tests for {@link TextRecord}
 */
public class TextRecordTest extends AndroidTestCase {

    // Îñţérñåţîöñåļîžåţîờñ
    private static final String I18N = "\\u00ce\\u00f1\\u0163\\u00e9r\\u00f1\\u00e5"
            + "\\u0163\\u00ee\\u00f6\\u00f1\\u00e5\\u013c\\u00ee\\u017e\\u00e5"
            + "\\u0163\\u00ee\\u1edd\\u00f1";

    public void testSimpleText() throws Exception {
        NdefMessage msg = new NdefMessage(MockNdefMessages.ENGLISH_PLAIN_TEXT);
        TextRecord record = TextRecord.parse(msg.getRecords()[0]);
        assertEquals("Some random english text.", record.getText());
        assertEquals("en", record.getLanguageCode());
    }

    public void testNewNdefMsg() throws Exception {
        NdefRecord record = TextRecord.newTextRecord("hello", Locale.US);
        TextRecord textRecord = TextRecord.parse(record);
        assertEquals("hello", textRecord.getText());
        assertEquals("en", textRecord.getLanguageCode());
    }

    public void testToText2() throws Exception {
        NdefRecord record = TextRecord.newTextRecord("Hello", Locale.US, false);
        TextRecord textRecord = TextRecord.parse(record);
        assertEquals("Hello", textRecord.getText());
        assertEquals("en", textRecord.getLanguageCode());
    }

    public void testToText3() throws Exception {
        NdefRecord record = TextRecord.newTextRecord(I18N, Locale.CHINA, true);
        TextRecord textRecord = TextRecord.parse(record);
        assertEquals(I18N, textRecord.getText());
        assertEquals("zh", textRecord.getLanguageCode());
    }

    public void testToText4() throws Exception {
        NdefRecord record = TextRecord.newTextRecord(I18N, Locale.CHINA, false);
        TextRecord textRecord = TextRecord.parse(record);
        assertEquals(I18N, textRecord.getText());
        assertEquals("zh", textRecord.getLanguageCode());
    }
}
