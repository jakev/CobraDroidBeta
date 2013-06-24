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

package android.text.cts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.test.AndroidTestCase;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(BoringLayout.class)
public class BoringLayoutTest extends AndroidTestCase {
    private static final float SPACING_MULT_NO_SCALE = 1.0f;
    private static final float SPACING_ADD_NO_SCALE = 0.0f;
    private static final int DEFAULT_OUTER_WIDTH = 100;
    private static final int METRICS_TOP = 10;
    private static final int METRICS_ASCENT = 20;
    private static final int METRICS_DESCENT = 40;
    private static final int METRICS_BOTTOM = 50;
    private static final int METRICS_WIDTH = 50;
    private static final int METRICS_LEADING = 50;

    private static final CharSequence DEFAULT_CHAR_SEQUENCE = "default";
    private static final TextPaint DEFAULT_PAINT = new TextPaint();
    private static final Layout.Alignment DEFAULT_ALIGN = Layout.Alignment.ALIGN_CENTER;
    private static final BoringLayout.Metrics DEFAULT_METRICS = createMetrics(
            METRICS_TOP,
            METRICS_ASCENT,
            METRICS_DESCENT,
            METRICS_BOTTOM,
            METRICS_WIDTH,
            METRICS_LEADING);

    private BoringLayout mBoringLayout;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mBoringLayout = makeDefaultBoringLayout();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BoringLayout",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BoringLayout",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class,
                    android.text.TextUtils.TruncateAt.class, int.class}
        )
    })
    public void testConstructors() {
        new BoringLayout(DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true);

        new BoringLayout(DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true,
                TextUtils.TruncateAt.START,
                DEFAULT_OUTER_WIDTH);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the scalibility of BoringLayout."
                  + " 1. No scale."
                  + " 2. Enlarge to 2/1.5 times of the font size."
                  + " 3. Reduce to 0.5 times of the font size."
                  + " 4. Add 1.5/1.4/3.0 to the original font."
                  + " 5. Minus 1.6/1.4/3.0 from the original font.",
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the scalibility of BoringLayout."
                  + " 1. No scale."
                  + " 2. Enlarge to 2/1.5 times of the font size."
                  + " 3. Reduce to 0.5 times of the font size."
                  + " 4. Add 1.5/1.4/3.0 to the original font."
                  + " 5. Minus 1.6/1.4/3.0 from the original font.",
            method = "getLineDirections",
            args = {int.class}
        )
    })
    public void testScale() {
        final int metricsBottomToTop = METRICS_BOTTOM - METRICS_TOP;

        //no scale
        BoringLayout boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, SPACING_ADD_NO_SCALE);

        assertEquals(metricsBottomToTop, boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // scale two times
        float spacingMult = 2.0f;
        boringLayout = makeBoringLayout(spacingMult, SPACING_ADD_NO_SCALE);

        assertEquals(metricsBottomToTop * spacingMult, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // scale 0.5 times
        spacingMult = 0.5f;
        boringLayout = makeBoringLayout(spacingMult, SPACING_ADD_NO_SCALE);
        assertEquals(metricsBottomToTop * spacingMult, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // add 1.5f
        float spacingAdd = 1.5f;
        float roundOff = 2.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingAdd);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // minus 1.6f
        float spacingMinus = -1.6f;
        roundOff = -2.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingMinus);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // add 1.4f
        spacingAdd = 1.4f;
        roundOff = 1.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingAdd);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // minus 1.4f
        spacingMinus = -1.4f;
        roundOff = -1.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingMinus);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // add 3.0f
        spacingAdd = 3.0f;
        roundOff = 3.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingAdd);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));

        // minus 3.0f
        spacingMinus = -3.0f;
        roundOff = -3.0f;
        boringLayout = makeBoringLayout(SPACING_MULT_NO_SCALE, spacingMinus);
        assertEquals(metricsBottomToTop + roundOff, (float) boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_TOP, boringLayout.getLineDescent(0));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the precondition of a BoringLayout according to the definition."
                  + " The preconditions include:"
                  + " 1. One line text layout."
                  + " 2. Left to right text direction."
                  + " 3. Won't be ellipsis. Use default parameters to construct the BoringLayout."
                  + " Test getLineCount, and followed methods are in same condition",
            method = "getLineCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLineTop",
            method = "getLineTop",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getHeight",
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLineStart",
            method = "getLineStart",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getParagraphDirection",
            method = "getParagraphDirection",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLineContainsTab",
            method = "getLineContainsTab",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getEllipsisCount",
            method = "getEllipsisCount",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getEllipsisStart",
            method = "getEllipsisStart",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLineMax",
            method = "getLineMax",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test ellipsized",
            method = "ellipsized",
            args = {int.class, int.class}
        )
    })
    public void testPreconditions() {
        assertEquals(1, mBoringLayout.getLineCount());
        assertEquals(0, mBoringLayout.getLineTop(0));
        assertEquals(mBoringLayout.getHeight(), mBoringLayout.getLineTop(1));
        assertEquals(mBoringLayout.getHeight(), mBoringLayout.getLineTop(10));
        assertEquals(0, mBoringLayout.getLineStart(0));
        assertEquals(DEFAULT_CHAR_SEQUENCE.length(), mBoringLayout.getLineStart(1));
        assertEquals(DEFAULT_CHAR_SEQUENCE.length(), mBoringLayout.getLineStart(10));
        assertEquals(Layout.DIR_LEFT_TO_RIGHT, mBoringLayout.getParagraphDirection(0));
        assertFalse(mBoringLayout.getLineContainsTab(0));
        assertEquals((float) METRICS_WIDTH, mBoringLayout.getLineMax(0));
        assertEquals(Layout.DIR_LEFT_TO_RIGHT, mBoringLayout.getParagraphDirection(0));
        assertEquals(0, mBoringLayout.getEllipsisCount(0));
        mBoringLayout.ellipsized(0, 1);
        assertEquals(1, mBoringLayout.getEllipsisCount(0));
        mBoringLayout.ellipsized(1, 2);
        assertEquals(1, mBoringLayout.getEllipsisStart(0));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the static maker method of BoringLayout. This method will return a"
                  + " suitable instance of BoringLayout according  to the specific parameters."
                  + " 1. Alignment is {@link android.text.Layout.Alignment#ALIGN_CENTER} and the"
                  + " source string is a {@link android.text.Spanned}."
                  + " 2. Alignment is {@link android.text.Layout.Alignment#ALIGN_NORMAL} and the"
                  + " source string is not a {@link android.text.Spanned}also test"
                  + " getEllipsizedWidth. Test replaceOrMake, and followed methods are in same"
                  + " condition",
            method = "replaceOrMake",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test replaceOrMake",
            method = "replaceOrMake",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class,
                    android.text.TextUtils.TruncateAt.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getEllipsizedWidth",
            method = "getEllipsizedWidth",
            args = {}
        )
    })
    public void testReplaceOrMake() {
        String source = "This is a SpannableString.";
        BoringLayout layout_1 = mBoringLayout.replaceOrMake(
                source,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true);
        assertSame(mBoringLayout, layout_1);
        layout_1 = null;
        layout_1 = mBoringLayout.replaceOrMake(
                source,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true,
                TextUtils.TruncateAt.START,
                100);
        assertSame(mBoringLayout, layout_1);
        assertEquals(100, mBoringLayout.getEllipsizedWidth());
    }


    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the alignment of BoringLayout."
                  + " 1. {@link android.text.Layout.Alignment#ALIGN_NORMAL}."
                  + " 2. {@link android.text.Layout.Alignment#ALIGN_CENTER}."
                  + " 3. {@link android.text.Layout.Alignment#ALIGN_OPPOSITE}."
                  + " Also the getLineLeft and getLineRight method",
            method = "getLineLeft",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLineRight",
            method = "getLineRight",
            args = {int.class}
        )
    })
    public void testAlignment() {
        BoringLayout boringLayout = makeBoringLayoutAlign(Layout.Alignment.ALIGN_NORMAL);
        assertEquals(0.0f, boringLayout.getLineLeft(0));
        assertEquals((float) DEFAULT_METRICS.width, boringLayout.getLineRight(0));

        boringLayout = makeBoringLayoutAlign(Layout.Alignment.ALIGN_CENTER);
        int expectedWidth = DEFAULT_OUTER_WIDTH - METRICS_WIDTH;
        assertEquals((float) expectedWidth / 2, boringLayout.getLineLeft(0));
        expectedWidth = DEFAULT_OUTER_WIDTH + METRICS_WIDTH;
        assertEquals((float) expectedWidth / 2, boringLayout.getLineRight(0));

        boringLayout = makeBoringLayoutAlign(Layout.Alignment.ALIGN_OPPOSITE);
        expectedWidth = DEFAULT_OUTER_WIDTH - METRICS_WIDTH;
        assertEquals((float) expectedWidth, boringLayout.getLineLeft(0));
        assertEquals((float) DEFAULT_OUTER_WIDTH, boringLayout.getLineRight(0));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test whether include the padding to calculate the layout."
                  + " 1. Include padding while calculate the layout."
                  + " 2. Don't include padding while calculate the layout."
                  + " Also test other related methods",
            method = "getTopPadding",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBottomPadding",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineDescent",
            args = {int.class}
        )
    })
    public void testIncludePadding() {
        assertEquals(METRICS_TOP - METRICS_ASCENT, mBoringLayout.getTopPadding());
        assertEquals(METRICS_BOTTOM - METRICS_DESCENT, mBoringLayout.getBottomPadding());
        assertEquals(METRICS_BOTTOM - METRICS_TOP, mBoringLayout.getHeight());
        assertEquals(mBoringLayout.getHeight() + METRICS_TOP, mBoringLayout.getLineDescent(0));

        BoringLayout boringLayout = new BoringLayout(
                DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                false);

        assertEquals(0, boringLayout.getTopPadding());
        assertEquals(0, boringLayout.getBottomPadding());
        assertEquals(METRICS_DESCENT - METRICS_ASCENT, boringLayout.getHeight());
        assertEquals(boringLayout.getHeight() + METRICS_ASCENT, boringLayout.getLineDescent(0));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test the static method which is to verify whether the given source string is"
                  + " suitable for BoringLayout or not."
                  + " 1. A normal simple string."
                  + " 2. Exceptional strings, including:"
                  + "   2.1 Hebrew characters, which are read from right to left."
                  + "   2.2 Strings with whitespaces in it.",
            method = "isBoring",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBoring",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class,
                    android.text.BoringLayout.Metrics.class}
        )
    })
    public void testIsBoringString() {
        TextPaint paint = new TextPaint();
        assertNotNull(BoringLayout.isBoring("hello android", paint));

        BoringLayout.Metrics metrics = new BoringLayout.Metrics();
        metrics.width = 100;
        assertNotNull(BoringLayout.isBoring("hello android", paint, metrics));

        assertNull(BoringLayout.isBoring("\u0590 \u0591", paint));
        assertNull(BoringLayout.isBoring("hello \t android", paint));
        assertNull(BoringLayout.isBoring("hello \n android", paint));
        assertNull(BoringLayout.isBoring("hello \n\n\n android", paint));
        assertNull(BoringLayout.isBoring("\nhello \n android\n", paint));
        assertNull(BoringLayout.isBoring("hello android\n\n\n", paint));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineDirections",
        args = {int.class}
    )
    public void testGetLineDirections() {
        assertNotNull(mBoringLayout.getLineDirections(0));
        assertNotNull(mBoringLayout.getLineDirections(2));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "make",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "make",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                    android.text.Layout.Alignment.class, float.class, float.class,
                    android.text.BoringLayout.Metrics.class, boolean.class,
                    android.text.TextUtils.TruncateAt.class, int.class}
        )
    })
    public void testMake() {
        BoringLayout boringLayout = BoringLayout.make(DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true);
        assertNotNull(boringLayout);

        boringLayout = null;
        boringLayout = BoringLayout.make(DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true,
                TextUtils.TruncateAt.START,
                DEFAULT_OUTER_WIDTH);
        assertNotNull(boringLayout);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, android.graphics.Path.class,
                android.graphics.Paint.class, int.class}
    )
    public void testDraw() {
        BoringLayout boringLayout = BoringLayout.make((String)DEFAULT_CHAR_SEQUENCE,
                DEFAULT_PAINT,
                DEFAULT_OUTER_WIDTH,
                Alignment.ALIGN_NORMAL,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                DEFAULT_METRICS,
                true);

        Bitmap mMutableBitmap = Bitmap.createBitmap(10, 28, Config.ARGB_8888);
        MockCanvas c = new MockCanvas(mMutableBitmap);
        boringLayout.draw(c, null, null, 0);
        assertTrue(c.isCanvasCalling);
    }

    private class MockCanvas extends Canvas {
        public boolean isCanvasCalling = false;

        public MockCanvas(Bitmap bitmap) {
            super(bitmap);
        }

        @Override
        public void drawText(String text, float x, float y, Paint paint) {
            super.drawText(text, x, y, paint);
            isCanvasCalling = true;
        }
    }

    private static BoringLayout.Metrics createMetrics(
            final int top,
            final int ascent,
            final int descent,
            final int bottom,
            final int width,
            final int leading) {

        final BoringLayout.Metrics metrics = new BoringLayout.Metrics();

        metrics.top = top;
        metrics.ascent = ascent;
        metrics.descent = descent;
        metrics.bottom = bottom;
        metrics.width = width;
        metrics.leading = leading;

        return metrics;
    }

    private BoringLayout makeDefaultBoringLayout(){
        return new BoringLayout(DEFAULT_CHAR_SEQUENCE,
                                DEFAULT_PAINT,
                                DEFAULT_OUTER_WIDTH,
                                DEFAULT_ALIGN,
                                SPACING_MULT_NO_SCALE,
                                SPACING_ADD_NO_SCALE,
                                DEFAULT_METRICS,
                                true);
    }

    private BoringLayout makeBoringLayout(float spacingMult,float spacingAdd){
        return new BoringLayout(DEFAULT_CHAR_SEQUENCE,
                                DEFAULT_PAINT,
                                DEFAULT_OUTER_WIDTH,
                                DEFAULT_ALIGN,
                                spacingMult,
                                spacingAdd,
                                DEFAULT_METRICS,
                                true);
    }

    private BoringLayout makeBoringLayoutAlign(Alignment align){
        return new BoringLayout(DEFAULT_CHAR_SEQUENCE,
                                DEFAULT_PAINT,
                                DEFAULT_OUTER_WIDTH,
                                align,
                                SPACING_MULT_NO_SCALE,
                                SPACING_ADD_NO_SCALE,
                                DEFAULT_METRICS,
                                true);
    }
}
