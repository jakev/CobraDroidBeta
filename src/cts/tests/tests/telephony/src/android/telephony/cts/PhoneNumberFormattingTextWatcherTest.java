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
package android.telephony.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.test.AndroidTestCase;
import android.text.Editable;
import android.widget.TextView;

@TestTargetClass(PhoneNumberFormattingTextWatcher.class)
public class PhoneNumberFormattingTextWatcherTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PhoneNumberFormattingTextWatcher",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "beforeTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "afterTextChanged",
            args = {android.text.Editable.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        )
    })
    public void testPhoneNumberFormattingTextWatcher() {
        TextView text = new TextView(getContext());
        text.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        text.setText("+15551212");
        assertEquals("+1-555-1212", text.getText().toString());

        // delete first dash and first 5
        Editable edit = (Editable) text.getText();
        edit.delete(2, 3);
        assertEquals("+1-551-212", text.getText().toString());

        // already formatted correctly
        text.setText("+1-555-1212");
        assertEquals("+1-555-1212", text.getText().toString());
    }
}
