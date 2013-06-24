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

package android.view.inputmethod.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.os.Parcel;
import android.test.AndroidTestCase;
import android.view.inputmethod.ExtractedTextRequest;

@TestTargetClass(ExtractedTextRequest.class)
public class ExtractedTextRequestTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {Parcel.class, int.class}
        )
    })
    public void testExtractedTextRequest() {
        ExtractedTextRequest request = new ExtractedTextRequest();
        request.flags = 1;
        request.hintMaxChars = 100;
        request.hintMaxLines = 10;
        request.token = 2;

        assertEquals(0, request.describeContents());

        Parcel p = Parcel.obtain();
        request.writeToParcel(p, 0);
        p.setDataPosition(0);
        ExtractedTextRequest target = ExtractedTextRequest.CREATOR.createFromParcel(p);
        p.recycle();
        assertEquals(request.flags, target.flags);
        assertEquals(request.hintMaxChars, request.hintMaxChars);
        assertEquals(request.hintMaxLines, target.hintMaxLines);
        assertEquals(request.token, target.token);
    }
}
