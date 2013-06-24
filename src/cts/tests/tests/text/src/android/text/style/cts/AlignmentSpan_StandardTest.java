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
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan.Standard;

import junit.framework.TestCase;

/**
 * Test {@link Standard}.
 */
@TestTargetClass(Standard.class)
public class AlignmentSpan_StandardTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "AlignmentSpan.Standard",
            args = {android.text.Layout.Alignment.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "AlignmentSpan.Standard",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new Standard(Alignment.ALIGN_CENTER);

        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        final Parcel p = Parcel.obtain();
        standard.writeToParcel(p, 0);
        p.setDataPosition(0);
        new Standard(p);
        p.recycle();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAlignment().",
        method = "getAlignment",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetAlignment() {
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        assertEquals(Alignment.ALIGN_NORMAL, standard.getAlignment());

        standard = new Standard(Alignment.ALIGN_OPPOSITE);
        assertEquals(Alignment.ALIGN_OPPOSITE, standard.getAlignment());

        standard = new Standard(Alignment.ALIGN_CENTER);
        assertEquals(Alignment.ALIGN_CENTER, standard.getAlignment());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        standard.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        standard.getSpanTypeId();
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
        Standard s = new Standard(Alignment.ALIGN_NORMAL);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        Standard standard = new Standard(p);
        assertEquals(Alignment.ALIGN_NORMAL, standard.getAlignment());
        p.recycle();

        s = new Standard(Alignment.ALIGN_OPPOSITE);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        standard = new Standard(p);
        assertEquals(Alignment.ALIGN_OPPOSITE, standard.getAlignment());
        p.recycle();

        s = new Standard(Alignment.ALIGN_CENTER);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        standard = new Standard(p);
        assertEquals(Alignment.ALIGN_CENTER, standard.getAlignment());
        p.recycle();
    }
}
