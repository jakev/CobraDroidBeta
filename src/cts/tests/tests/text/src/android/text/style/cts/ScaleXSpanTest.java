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
import android.text.style.ScaleXSpan;

import junit.framework.TestCase;

@TestTargetClass(ScaleXSpan.class)
public class ScaleXSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ScaleXSpan",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ScaleXSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        ScaleXSpan scaleXSpan = new ScaleXSpan(1.5f);

        Parcel p = Parcel.obtain();
        scaleXSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new ScaleXSpan(p);

        new ScaleXSpan(-2.5f);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateDrawState() {
        float proportion = 3.0f;
        ScaleXSpan scaleXSpan = new ScaleXSpan(proportion);

        TextPaint tp = new TextPaint();
        tp.setTextScaleX(2.0f);
        scaleXSpan.updateDrawState(tp);
        assertEquals(2.0f * proportion, tp.getTextScaleX());

        tp.setTextScaleX(-3.0f);
        scaleXSpan.updateDrawState(tp);
        assertEquals(-3.0f * proportion, tp.getTextScaleX());

        try {
            scaleXSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        float proportion = 3.0f;
        ScaleXSpan scaleXSpan = new ScaleXSpan(proportion);

        TextPaint tp = new TextPaint();
        tp.setTextScaleX(2.0f);
        scaleXSpan.updateMeasureState(tp);
        assertEquals(2.0f * proportion, tp.getTextScaleX());

        tp.setTextScaleX(-3.0f);
        scaleXSpan.updateMeasureState(tp);
        assertEquals(-3.0f * proportion, tp.getTextScaleX());

        try {
            scaleXSpan.updateMeasureState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getScaleX",
        args = {}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testGetScaleX() {
        ScaleXSpan scaleXSpan = new ScaleXSpan(5.0f);
        assertEquals(5.0f, scaleXSpan.getScaleX());

        scaleXSpan = new ScaleXSpan(-5.0f);
        assertEquals(-5.0f, scaleXSpan.getScaleX());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        ScaleXSpan scaleXSpan = new ScaleXSpan(5.0f);
        scaleXSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        ScaleXSpan scaleXSpan = new ScaleXSpan(5.0f);
        scaleXSpan.getSpanTypeId();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        float proportion = 3.0f;
        ScaleXSpan scaleXSpan = new ScaleXSpan(proportion);
        scaleXSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        ScaleXSpan newSpan = new ScaleXSpan(p);
        assertEquals(proportion, newSpan.getScaleX());
        p.recycle();
    }
}
