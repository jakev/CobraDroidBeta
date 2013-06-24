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
import android.text.style.AbsoluteSizeSpan;

import junit.framework.TestCase;

@TestTargetClass(AbsoluteSizeSpan.class)
public class AbsoluteSizeSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of AbsoluteSizeSpan.",
            method = "AbsoluteSizeSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of AbsoluteSizeSpan.",
            method = "AbsoluteSizeSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new AbsoluteSizeSpan(0);
        new AbsoluteSizeSpan(-5);

        AbsoluteSizeSpan asp = new AbsoluteSizeSpan(10);
        final Parcel p = Parcel.obtain();
        asp.writeToParcel(p, 0);
        p.setDataPosition(0);
        new AbsoluteSizeSpan(p);
        p.recycle();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link AbsoluteSizeSpan#getSize()}",
        method = "getSize",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSize() {
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(5);
        assertEquals(5, absoluteSizeSpan.getSize());

        absoluteSizeSpan = new AbsoluteSizeSpan(-5);
        assertEquals(-5, absoluteSizeSpan.getSize());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link AbsoluteSizeSpan#updateMeasureState(TextPaint)}",
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "AbsoluteSizeSpan#updateMeasureState(TextPaint) when the input TextPaint is null")
    public void testUpdateMeasureState() {
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(1);

        TextPaint tp = new TextPaint();
        absoluteSizeSpan.updateMeasureState(tp);
        assertEquals(1.0f, tp.getTextSize());

        absoluteSizeSpan = new AbsoluteSizeSpan(10);
        absoluteSizeSpan.updateMeasureState(tp);
        assertEquals(10.0f, tp.getTextSize());

        try {
            absoluteSizeSpan.updateMeasureState(null);
            fail("should throw NullPointerException when TextPaint is null.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link AbsoluteSizeSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "AbsoluteSizeSpan#updateDrawState(TextPaint) when the input TextPaint is null")
    public void testUpdateDrawState() {
        // new the AbsoluteSizeSpan instance
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(2);

        TextPaint tp = new TextPaint();
        absoluteSizeSpan.updateDrawState(tp);
        assertEquals(2.0f, tp.getTextSize());

        // new the AbsoluteSizeSpan instance
        absoluteSizeSpan = new AbsoluteSizeSpan(20);
        absoluteSizeSpan.updateDrawState(tp);
        assertEquals(20.0f, tp.getTextSize());

        try {
            absoluteSizeSpan.updateDrawState(null);
            fail("should throw NullPointerException when TextPaint is null.");
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
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(2);
        absoluteSizeSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(2);
        absoluteSizeSpan.getSpanTypeId();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        AbsoluteSizeSpan asp = new AbsoluteSizeSpan(2);
        asp.writeToParcel(p, 0);
        p.setDataPosition(0);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(p);
        assertEquals(2, absoluteSizeSpan.getSize());
        p.recycle();

        p = Parcel.obtain();
        asp = new AbsoluteSizeSpan(-5);
        asp.writeToParcel(p, 0);
        p.setDataPosition(0);
        absoluteSizeSpan = new AbsoluteSizeSpan(p);
        assertEquals(-5, absoluteSizeSpan.getSize());
        p.recycle();
    }
}
