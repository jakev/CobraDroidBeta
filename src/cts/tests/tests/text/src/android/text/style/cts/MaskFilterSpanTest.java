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
import dalvik.annotation.ToBeFixed;

import android.graphics.MaskFilter;
import android.text.TextPaint;
import android.text.style.MaskFilterSpan;

import junit.framework.TestCase;

@TestTargetClass(MaskFilterSpan.class)
public class MaskFilterSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link MaskFilterSpan}",
        method = "MaskFilterSpan",
        args = {android.graphics.MaskFilter.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        MaskFilter mf = new MaskFilter();
        new MaskFilterSpan(mf);
        new MaskFilterSpan(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link MaskFilterSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testUpdateDrawState() {
        MaskFilter mf = new MaskFilter();
        MaskFilterSpan maskFilterSpan = new MaskFilterSpan(mf);

        TextPaint tp = new TextPaint();
        assertNull(tp.getMaskFilter());

        maskFilterSpan.updateDrawState(tp);
        assertSame(mf, tp.getMaskFilter());

        try {
            maskFilterSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link MaskFilterSpan#getMaskFilter()}",
        method = "getMaskFilter",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetMaskFilter() {
        MaskFilter expected = new MaskFilter();

        MaskFilterSpan maskFilterSpan = new MaskFilterSpan(expected);
        assertSame(expected, maskFilterSpan.getMaskFilter());

        maskFilterSpan = new MaskFilterSpan(null);
        assertNull(maskFilterSpan.getMaskFilter());
    }
}
