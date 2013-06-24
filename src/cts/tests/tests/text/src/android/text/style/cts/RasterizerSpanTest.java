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

import android.graphics.Rasterizer;
import android.text.TextPaint;
import android.text.style.RasterizerSpan;

import junit.framework.TestCase;

@TestTargetClass(RasterizerSpan.class)
public class RasterizerSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link RasterizerSpan}",
        method = "RasterizerSpan",
        args = {android.graphics.Rasterizer.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        Rasterizer r = new Rasterizer();

        new RasterizerSpan(r);
        new RasterizerSpan(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link RasterizerSpan#getRasterizer()}",
        method = "getRasterizer",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetRasterizer() {
        Rasterizer expected = new Rasterizer();

        RasterizerSpan rasterizerSpan = new RasterizerSpan(expected);
        assertSame(expected, rasterizerSpan.getRasterizer());

        rasterizerSpan = new RasterizerSpan(null);
        assertNull(rasterizerSpan.getRasterizer());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link RasterizerSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testUpdateDrawState() {
        Rasterizer rasterizer = new Rasterizer();
        RasterizerSpan rasterizerSpan = new RasterizerSpan(rasterizer);

        TextPaint tp = new TextPaint();
        assertNull(tp.getRasterizer());

        rasterizerSpan.updateDrawState(tp);
        assertSame(rasterizer, tp.getRasterizer());

        try {
            rasterizerSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }
}
