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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.test.AndroidTestCase;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.style.StrikethroughSpan;

@TestTargetClass(Layout.class)
public class LayoutTest extends AndroidTestCase {
    private final static int LINE_COUNT = 5;
    private final static int LINE_HEIGHT = 12;
    private final static int LINE_DESCENT = 4;
    private final static CharSequence LAYOUT_TEXT = "alwei\t;sdfs\ndf @";

    private int mWidth;
    private Layout.Alignment mAlign;
    private float mSpacingmult;
    private float mSpacingadd;
    private SpannableString mSpannedText;

    private TextPaint mTextPaint;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTextPaint = new TextPaint();
        mSpannedText = new SpannableString(LAYOUT_TEXT);
        mSpannedText.setSpan(new StrikethroughSpan(), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mWidth = 11;
        mAlign = Alignment.ALIGN_CENTER;
        mSpacingmult = 1;
        mSpacingadd = 2;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Layout",
        args = {java.lang.CharSequence.class, android.text.TextPaint.class, int.class,
                android.text.Layout.Alignment.class, float.class, float.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc " +
            " of Layout constructor when the width is smaller than 0")
    public void testConstructor() {
        new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, mAlign, mSpacingmult, mSpacingadd);

        try {
            new MockLayout(null, null, -1, null, 0, 0);
            fail("should throw IllegalArgumentException here");
        } catch (IllegalArgumentException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testDraw1() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        layout.draw(new Canvas());

        try {
            layout.draw(new Canvas(Bitmap.createBitmap(200, 200, Config.ARGB_4444)));
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, android.graphics.Path.class,
                android.graphics.Paint.class, int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testDraw2() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        layout.draw(new Canvas(), null, null, 0);

        try {
            Bitmap bitmap = Bitmap.createBitmap(200, 200,Config.ARGB_4444);
            layout.draw(new Canvas(bitmap), null, null, 0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(200, 200, null);
            layout.draw(new Canvas(bitmap), new Path(), new Paint(), 2);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getText",
        args = {}
    )
    public void testGetText() {
        CharSequence text = "test case 1";
        Layout layout = new MockLayout(text, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(text, layout.getText());

        layout = new MockLayout(null, mTextPaint, mWidth, mAlign, mSpacingmult, mSpacingadd);
        assertNull(layout.getText());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPaint",
        args = {}
    )
    public void testGetPaint() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);

        assertSame(mTextPaint, layout.getPaint());

        layout = new MockLayout(LAYOUT_TEXT, null, mWidth, mAlign, mSpacingmult, mSpacingadd);
        assertNull(layout.getPaint());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getWidth",
        args = {}
    )
    public void testGetWidth() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, 10,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(10,  layout.getWidth());

        layout = new MockLayout(LAYOUT_TEXT, mTextPaint, 0, mAlign, mSpacingmult, mSpacingadd);
        assertEquals(0,  layout.getWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getEllipsizedWidth",
        args = {}
    )
    public void testGetEllipsizedWidth() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, 15,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(15, layout.getEllipsizedWidth());

        layout = new MockLayout(LAYOUT_TEXT, mTextPaint, 0, mAlign, mSpacingmult, mSpacingadd);
        assertEquals(0,  layout.getEllipsizedWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "increaseWidthTo",
        args = {int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc " +
            " of Layout#increaseWidthTo(int) when the new width is smaller than old one")
    public void testIncreaseWidthTo() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        int oldWidth = layout.getWidth();

        layout.increaseWidthTo(oldWidth);
        assertEquals(oldWidth, layout.getWidth());

        try {
            layout.increaseWidthTo(oldWidth - 1);
            fail("should throw runtime exception attempted to reduce Layout width");
        } catch (RuntimeException e) {
        }

        layout.increaseWidthTo(oldWidth + 1);
        assertEquals(oldWidth + 1, layout.getWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHeight",
        args = {}
    )
    public void testGetHeight() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(60, layout.getHeight());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAlignment",
        args = {}
    )
    public void testGetAlignment() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertSame(mAlign, layout.getAlignment());

        layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, null, mSpacingmult, mSpacingadd);
        assertNull(layout.getAlignment());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpacingMultiplier",
        args = {}
    )
    public void testGetSpacingMultiplier() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, mAlign, -1, mSpacingadd);
        assertEquals(-1.0f, layout.getSpacingMultiplier());

        layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, mAlign, 5, mSpacingadd);
        assertEquals(5.0f, layout.getSpacingMultiplier());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpacingAdd",
        args = {}
    )
    public void testGetSpacingAdd() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, mAlign, mSpacingmult, -1);
        assertEquals(-1.0f, layout.getSpacingAdd());

        layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth, mAlign, mSpacingmult, 20);
        assertEquals(20.0f, layout.getSpacingAdd());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineBounds",
        args = {int.class, android.graphics.Rect.class}
    )
    public void testGetLineBounds() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        Rect bounds = new Rect();

        assertEquals(32, layout.getLineBounds(2, bounds));
        assertEquals(0, bounds.left);
        assertEquals(mWidth, bounds.right);
        assertEquals(24, bounds.top);
        assertEquals(36, bounds.bottom);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPrimaryHorizontal",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetPrimaryHorizontal() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getPrimaryHorizontal(0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSecondaryHorizontal",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetSecondaryHorizontal() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getSecondaryHorizontal(0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineLeft",
        args = {int.class}
    )
    public void testGetLineLeft() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(2.0f, layout.getLineLeft(0));
        assertEquals(4.0f, layout.getLineLeft(1));
        assertEquals(1.0f, layout.getLineLeft(2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineRight",
        args = {int.class}
    )
    public void testGetLineRight() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(9.0f, layout.getLineRight(0));
        assertEquals(7.0f, layout.getLineRight(1));
        assertEquals(10.0f, layout.getLineRight(2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineForVertical",
        args = {int.class}
    )
    public void testGetLineForVertical() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(0, layout.getLineForVertical(-1));
        assertEquals(0, layout.getLineForVertical(0));
        assertEquals(0, layout.getLineForVertical(LINE_COUNT));
        assertEquals(LINE_COUNT - 1, layout.getLineForVertical(1000));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineForOffset",
        args = {int.class}
    )
    public void testGetLineForOffset() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(0, layout.getLineForOffset(-1));
        assertEquals(1, layout.getLineForOffset(1));
        assertEquals(LINE_COUNT - 1, layout.getLineForOffset(LINE_COUNT - 1));
        assertEquals(LINE_COUNT - 1, layout.getLineForOffset(1000));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOffsetForHorizontal",
        args = {int.class, float.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetOffsetForHorizontal() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getOffsetForHorizontal(0, 0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineEnd",
        args = {int.class}
    )
    public void testGetLineEnd() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(2, layout.getLineEnd(1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineVisibleEnd",
        args = {int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc " +
            " of Layout#getLineVisibleEnd(int) when the line is out of bound")
    public void testGetLineVisibleEnd() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);

        assertEquals(2, layout.getLineVisibleEnd(1));
        assertEquals(LINE_COUNT, layout.getLineVisibleEnd(LINE_COUNT - 1));
        assertEquals(LAYOUT_TEXT.length(), layout.getLineVisibleEnd(LAYOUT_TEXT.length() - 1));
        try {
            layout.getLineVisibleEnd(LAYOUT_TEXT.length());
            fail("should throw .StringIndexOutOfBoundsException here");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineBottom",
        args = {int.class}
    )
    public void testGetLineBottom() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(LINE_HEIGHT, layout.getLineBottom(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineBaseline",
        args = {int.class}
    )
    public void testGetLineBaseline() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(8, layout.getLineBaseline(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLineAscent",
        args = {int.class}
    )
    public void testGetLineAscent() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(-8, layout.getLineAscent(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOffsetToLeftOf",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetOffsetToLeftOf() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getOffsetToLeftOf(0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOffsetToRightOf",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetOffsetToRightOf() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getOffsetToRightOf(0);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCursorPath",
        args = {int.class, android.graphics.Path.class, java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetCursorPath() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        try {
            layout.getCursorPath(0, new Path(), "test");
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSelectionPath",
        args = {int.class, int.class, android.graphics.Path.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "can not get the" +
            " package protected class Directions")
    public void testGetSelectionPath() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        Path path = new Path();

        layout.getSelectionPath(0, 0, path);

        try {
            layout.getSelectionPath(1, 0, path);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }

        try {
            layout.getSelectionPath(0, 1, path);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getParagraphAlignment",
        args = {int.class}
    )
    public void testGetParagraphAlignment() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertSame(mAlign, layout.getParagraphAlignment(0));

        layout = new MockLayout(mSpannedText, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertSame(mAlign, layout.getParagraphAlignment(0));
        assertSame(mAlign, layout.getParagraphAlignment(1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getParagraphLeft",
        args = {int.class}
    )
    public void testGetParagraphLeft() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(0, layout.getParagraphLeft(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getParagraphRight",
        args = {int.class}
    )
    public void testGetParagraphRight() {
        Layout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertEquals(mWidth, layout.getParagraphRight(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isSpanned",
        args = {}
    )
    public void testIsSpanned() {
        MockLayout layout = new MockLayout(LAYOUT_TEXT, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        // default is not spanned text
        assertFalse(layout.mockIsSpanned());

        // try to create a spanned text
        layout = new MockLayout(mSpannedText, mTextPaint, mWidth,
                mAlign, mSpacingmult, mSpacingadd);
        assertTrue(layout.mockIsSpanned());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDesiredWidth",
        args = {java.lang.CharSequence.class, int.class, int.class, android.text.TextPaint.class}
    )
    public void testGetDesiredWidthRange() {
        CharSequence textShort = "test";
        CharSequence textLonger = "test\ngetDesiredWidth";
        CharSequence textLongest = "test getDesiredWidth";
        TextPaint paint = new TextPaint();
        float widthShort = Layout.getDesiredWidth(textShort, 0, textShort.length(), paint);
        float widthLonger = Layout.getDesiredWidth(textLonger, 0, textLonger.length(), paint);
        float widthLongest = Layout.getDesiredWidth(textLongest, 0, textLongest.length(), paint);
        float widthPartShort = Layout.getDesiredWidth(textShort, 2, textShort.length(), paint);
        float widthZero = Layout.getDesiredWidth(textLonger, 5, textShort.length() - 3, paint);
        assertTrue(widthLonger > widthShort);
        assertTrue(widthLongest > widthLonger);
        assertEquals(0f, widthZero);
        assertTrue(widthShort > widthPartShort);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDesiredWidth",
        args = {java.lang.CharSequence.class, android.text.TextPaint.class}
    )
    public void testGetDesiredWidth() {
        CharSequence textShort = "test";
        CharSequence textLonger = "test\ngetDesiredWidth";
        CharSequence textLongest = "test getDesiredWidth";
        TextPaint paint = new TextPaint();
        float widthShort = Layout.getDesiredWidth(textShort, paint);
        float widthLonger = Layout.getDesiredWidth(textLonger, paint);
        float widthLongest = Layout.getDesiredWidth(textLongest, paint);
        assertTrue(widthLonger > widthShort);
        assertTrue(widthLongest > widthLonger);
    }

    private final class MockLayout extends Layout {
        public MockLayout(CharSequence text, TextPaint paint, int width,
                Alignment align, float spacingmult, float spacingadd) {
            super(text, paint, width, align, spacingmult, spacingadd);
        }

        protected boolean mockIsSpanned() {
            return super.isSpanned();
        }

        @Override
        public int getBottomPadding() {
            return 0;
        }

        @Override
        public int getEllipsisCount(int line) {
            return 0;
        }

        @Override
        public int getEllipsisStart(int line) {
            return 0;
        }

        @Override
        public boolean getLineContainsTab(int line) {
            return false;
        }

        @Override
        public int getLineCount() {
            return LINE_COUNT;
        }

        @Override
        public int getLineDescent(int line) {
            return LINE_DESCENT;
        }

        @Override
        public Directions getLineDirections(int line) {
            return null;
        }

        @Override
        public int getLineStart(int line) {
            if (line < 0) {
                return 0;
            }
            return line;
        }

        @Override
        public int getLineTop(int line) {
            if (line < 0) {
                return 0;
            }
            return LINE_HEIGHT * (line);
        }

        @Override
        public int getParagraphDirection(int line) {
            return 0;
        }

        @Override
        public int getTopPadding() {
            return 0;
        }
    }
}
