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

package android.text.style.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import junit.framework.TestCase;

@TestTargetClass(UnderlineSpan.class)
public class UnderlineSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UnderlineSpan.",
            method = "UnderlineSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UnderlineSpan.",
            method = "UnderlineSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new UnderlineSpan();

        final Parcel p = Parcel.obtain();
        new UnderlineSpan(p);
        p.recycle();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link UnderlineSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
        " into javadoc when input TextPaint is null")
    public void testUpdateDrawState() {
        UnderlineSpan underlineSpan = new UnderlineSpan();

        TextPaint tp = new TextPaint();
        tp.setUnderlineText(false);
        assertFalse(tp.isUnderlineText());

        underlineSpan.updateDrawState(tp);
        assertTrue(tp.isUnderlineText());

        try {
            underlineSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.getSpanTypeId();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.writeToParcel(p, 0);
        new UnderlineSpan(p);
        p.recycle();
    }
}
