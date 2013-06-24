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

import android.graphics.Typeface;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.StyleSpan;

import junit.framework.TestCase;

@TestTargetClass(StyleSpan.class)
public class StyleSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StyleSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StyleSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        StyleSpan styleSpan = new StyleSpan(2);

        Parcel p = Parcel.obtain();
        styleSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StyleSpan(p);

        new StyleSpan(-2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getStyle",
        args = {}
    )
    public void testGetStyle() {
        StyleSpan styleSpan = new StyleSpan(2);
        assertEquals(2, styleSpan.getStyle());

        styleSpan = new StyleSpan(-2);
        assertEquals(-2, styleSpan.getStyle());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);

        TextPaint tp = new TextPaint();
        Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
        tp.setTypeface(tf);

        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());

        styleSpan.updateMeasureState(tp);

        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.BOLD, tp.getTypeface().getStyle());

        try {
            styleSpan.updateMeasureState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateDrawState() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);

        TextPaint tp = new TextPaint();
        Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
        tp.setTypeface(tf);

        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());

        styleSpan.updateDrawState(tp);

        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.BOLD, tp.getTypeface().getStyle());

        try {
            styleSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.getSpanTypeId();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        StyleSpan newSpan = new StyleSpan(p);
        assertEquals(Typeface.BOLD, newSpan.getStyle());
        p.recycle();
    }
}
