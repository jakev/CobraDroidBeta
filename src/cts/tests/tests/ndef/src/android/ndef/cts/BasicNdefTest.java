/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.ndef.cts;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.FormatException;

import junit.framework.TestCase;

public class BasicNdefTest extends TestCase {
    /**
     * A Smart Poster containing a URL and no text.
     */
    public static final byte[] SMART_POSTER_URL_NO_TEXT = new byte[] {
            (byte) 0xd1, (byte) 0x02, (byte) 0x0f, (byte) 0x53, (byte) 0x70, (byte) 0xd1,
            (byte) 0x01, (byte) 0x0b, (byte) 0x55, (byte) 0x01, (byte) 0x67, (byte) 0x6f,
            (byte) 0x6f, (byte) 0x67, (byte) 0x6c, (byte) 0x65, (byte) 0x2e, (byte) 0x63,
            (byte) 0x6f, (byte) 0x6d
    };

    public void test_parseSmartPoster() throws FormatException {
        NdefMessage msg = new NdefMessage(SMART_POSTER_URL_NO_TEXT);
        NdefRecord[] records = msg.getRecords();

        assertEquals(1, records.length);

        assertEquals(0, records[0].getId().length);

        assertEquals(NdefRecord.TNF_WELL_KNOWN, records[0].getTnf());

        assertByteArrayEquals(NdefRecord.RTD_SMART_POSTER, records[0].getType());

        assertByteArrayEquals(new byte[] {
                (byte) 0xd1, (byte) 0x01, (byte) 0x0b, (byte) 0x55, (byte) 0x01,
                (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x67, (byte) 0x6c,
                (byte) 0x65, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d},
                records[0].getPayload());
    }

    private static void assertByteArrayEquals(byte[] b1, byte[] b2) {
        assertEquals(b1.length, b2.length);
        for (int i = 0; i < b1.length; i++) {
            assertEquals(b1[i], b2[i]);
        }
    }
}
