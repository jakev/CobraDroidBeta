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

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

import junit.framework.TestCase;

@TestTargetClass(ForegroundColorSpan.class)
public class ForegroundColorSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ForegroundColorSpan.",
            method = "ForegroundColorSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ForegroundColorSpan.",
            method = "ForegroundColorSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        ForegroundColorSpan f = new ForegroundColorSpan(Color.GREEN);

        final Parcel p = Parcel.obtain();
        f.writeToParcel(p, 0);
        p.setDataPosition(0);
        new ForegroundColorSpan(p);
        p.recycle();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ForegroundColorSpan#getForegroundColor()}",
        method = "getForegroundColor",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetForegroundColor() {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
        assertEquals(Color.BLUE, foregroundColorSpan.getForegroundColor());

        foregroundColorSpan = new ForegroundColorSpan(Color.BLACK);
        assertEquals(Color.BLACK, foregroundColorSpan.getForegroundColor());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ForegroundColorSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testUpdateDrawState() {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.CYAN);

        TextPaint tp = new TextPaint();
        tp.setColor(0);
        assertEquals(0, tp.getColor());
        foregroundColorSpan.updateDrawState(tp);
        assertEquals(Color.CYAN, tp.getColor());

        foregroundColorSpan = new ForegroundColorSpan(Color.DKGRAY);
        foregroundColorSpan.updateDrawState(tp);
        assertEquals(Color.DKGRAY, tp.getColor());

        try {
            foregroundColorSpan.updateDrawState(null);
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
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        foregroundColorSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        foregroundColorSpan.getSpanTypeId();
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
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        foregroundColorSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        ForegroundColorSpan f = new ForegroundColorSpan(p);
        assertEquals(Color.RED, f.getForegroundColor());
        p.recycle();

        p = Parcel.obtain();
        foregroundColorSpan = new ForegroundColorSpan(Color.MAGENTA);
        foregroundColorSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        f = new ForegroundColorSpan(p);
        assertEquals(Color.MAGENTA, f.getForegroundColor());
        p.recycle();
    }
}
