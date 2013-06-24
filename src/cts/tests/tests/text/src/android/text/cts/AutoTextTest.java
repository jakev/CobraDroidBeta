/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.text.cts;

import java.util.Locale;
import android.test.AndroidTestCase;
import android.text.AutoText;
import android.view.View;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(AutoText.class)
public class AutoTextTest extends AndroidTestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "get",
        args = {java.lang.CharSequence.class, int.class, int.class, android.view.View.class}
    )
    public void testGet() {
        // Define the necessary sources.
        CharSequence src;
        String actual;

        // set local as English.
        Locale.setDefault(Locale.ENGLISH);
        // New a View instance.
        View view = new View(getContext());

        // Test a word key not in the autotext.xml.
        src = "can";
        actual = AutoText.get(src, 0, src.length(), view);
        assertNull(actual);

        // get possible spelling correction in the scope of current
        // local/language
        src = "acn";
        actual = AutoText.get(src, 0, src.length(), view);
        assertNotNull(actual);
        assertEquals("can", actual);

        /*
         * get possible spelling correction in the scope of current
         * local/language, with end bigger than end
         */
        src = "acn";
        actual = AutoText.get(src, 0, src.length() + 1, view);
        assertNull(actual);

        /*
         * get possible spelling correction in the scope of current
         * local/language, with end smaller than end
         */
        src = "acn";
        actual = AutoText.get(src, 0, src.length() - 1, view);
        assertNull(actual);

        // get possible spelling correction outside of the scope of current
        // local/language
        src = "acnh";
        actual = AutoText.get(src, 0, src.length() - 1, view);
        assertNotNull(actual);
        assertEquals("can", actual);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSize",
        args = {android.view.View.class}
    )
    public void testGetSize() {
        Locale.setDefault(Locale.ENGLISH);
        View view = new View(getContext());
        // Returns the size of the auto text dictionary. Just make sure it is bigger than 0.
        assertTrue(AutoText.getSize(view) > 0);
    }
}

