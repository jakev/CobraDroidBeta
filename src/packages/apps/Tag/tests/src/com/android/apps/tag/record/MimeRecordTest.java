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
import android.test.AndroidTestCase;

import com.android.apps.tag.MockNdefMessages;

/**
 * Unittests for {@link MimeRecord}.
 */
public class MimeRecordTest extends AndroidTestCase {

    public void testVCardMimeEntry() throws Exception {
        NdefMessage msg = new NdefMessage(MockNdefMessages.VCARD);
        MimeRecord record = MimeRecord.parse(msg.getRecords()[0]);
        assertEquals("text/x-vCard", record.getMimeType());
        String expectedRecord = "BEGIN:VCARD\r\n"
                + "VERSION:3.0\r\n"
                + "FN:Joe Google Employee\r\n"
                + "ADR;TYPE=WORK:;;1600 Amphitheatre Parkway;94043 Mountain View\r\n"
                + "TEL;TYPE=PREF,WORK:650-253-0000\r\n"
                + "EMAIL;TYPE=INTERNET:support@google.com\r\n"
                + "TITLE:Software Engineer\r\n"
                + "ORG:Google\r\n"
                + "URL:http://www.google.com\r\n"
                + "END:VCARD\r\n";
        assertEquals(expectedRecord, new String(record.getContent(), "UTF-8"));
    }

    public void testIsMime() throws Exception {
        NdefMessage msg = new NdefMessage(MockNdefMessages.VCARD);
        assertTrue(MimeRecord.isMime(msg.getRecords()[0]));
    }

    public void testIsNotMime() throws Exception {
        NdefMessage msg = new NdefMessage(MockNdefMessages.REAL_NFC_MSG);
        assertFalse(MimeRecord.isMime(msg.getRecords()[0]));
    }
}
