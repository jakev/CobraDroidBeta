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
import android.text.style.StrikethroughSpan;

import junit.framework.TestCase;

@TestTargetClass(StrikethroughSpan.class)
public class StrikethroughSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StrikethroughSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StrikethroughSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        Parcel p = Parcel.obtain();
        strikethroughSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StrikethroughSpan(p);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateDrawState() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        TextPaint tp = new TextPaint();
        tp.setStrikeThruText(false);
        assertFalse(tp.isStrikeThruText());

        strikethroughSpan.updateDrawState(tp);
        assertTrue(tp.isStrikeThruText());

        try {
            strikethroughSpan.updateDrawState(null);
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
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.getSpanTypeId();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StrikethroughSpan(p);
        p.recycle();
    }
}
