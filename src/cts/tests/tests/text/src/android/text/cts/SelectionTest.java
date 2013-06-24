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

import android.test.AndroidTestCase;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(Selection.class)
public class SelectionTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSelectionStart",
        args = {java.lang.CharSequence.class}
    )
    public void testGetSelectionStart() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionStart(builder));

        Selection.setSelection(builder, 3, 8);
        assertEquals(3, Selection.getSelectionStart(builder));

        Selection.setSelection(builder, 3, 9);
        assertEquals(3, Selection.getSelectionStart(builder));

        Selection.setSelection(builder, 5, 7);
        assertEquals(5, Selection.getSelectionStart(builder));

        assertEquals(-1, Selection.getSelectionStart(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSelectionEnd",
        args = {java.lang.CharSequence.class}
    )
    public void testGetSelectionEnd() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 0, 10);
        assertEquals(10, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 8);
        assertEquals(8, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 2, 8);
        assertEquals(8, Selection.getSelectionEnd(builder));

        assertEquals(-1, Selection.getSelectionStart(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSelection",
        args = {android.text.Spannable.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testSetSelection1() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 6);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 7);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 7);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 6, 2);
        assertEquals(6, Selection.getSelectionStart(builder));
        assertEquals(2, Selection.getSelectionEnd(builder));

        try {
            Selection.setSelection(builder, -1, 100);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.setSelection(null, 3, 6);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSelection",
        args = {android.text.Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testSetSelection2() {
        SpannableStringBuilder builder = new SpannableStringBuilder("hello, world");
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 4);
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(4, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(3, Selection.getSelectionEnd(builder));

        try {
            Selection.setSelection(builder, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.setSelection(builder, 100);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.setSelection(null, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeSelection",
        args = {android.text.Spannable.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testRemoveSelection() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.removeSelection(builder);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 6);
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.removeSelection(builder);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.removeSelection(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "selectAll",
        args = {android.text.Spannable.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testSelectAll() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));;

        Selection.selectAll(builder);
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 4, 5);
        Selection.selectAll(builder);
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 8, 4);
        Selection.selectAll(builder);
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        SpannableStringBuilder empty = new SpannableStringBuilder();
        Selection.selectAll(empty);
        assertEquals(0, Selection.getSelectionStart(empty));
        assertEquals(0, Selection.getSelectionEnd(empty));

        try {
            Selection.selectAll(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveLeft",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testMoveLeft() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 50, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.moveLeft(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder, 6, 8);
        assertTrue(Selection.moveLeft(builder, layout));
        assertEquals(6, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveLeft(builder, layout));
        assertEquals(5, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 0, 0);
        assertFalse(Selection.moveLeft(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.selectAll(builder);
        assertTrue(Selection.moveLeft(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        try {
            Selection.moveLeft(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.moveLeft(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.moveLeft(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveRight",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testMoveRight() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.moveRight(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder,1, 5);
        assertTrue(Selection.moveRight(builder, layout));
        assertEquals(5, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveRight(builder, layout));
        assertEquals(6, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveRight(builder, layout));
        assertEquals(7, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, text.length(), text.length());
        assertFalse(Selection.moveRight(builder, layout));
        assertEquals(text.length(), Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        Selection.selectAll(builder);
        assertTrue(Selection.moveRight(builder, layout));
        assertEquals(text.length(), Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        try {
            Selection.moveRight(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.moveRight(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.moveRight(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveUp",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testMoveUp() {
        CharSequence text = "Google\nhello,world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.moveUp(builder, layout);

        Selection.setSelection(builder, 7, 10);
        assertTrue(Selection.moveUp(builder, layout));
        assertEquals(7, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 0, text.length());
        assertFalse(Selection.moveUp(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 14);
        assertTrue(Selection.moveUp(builder, layout));
        assertEquals(4, Selection.getSelectionStart(builder));
        assertEquals(4, Selection.getSelectionEnd(builder));

        assertFalse(Selection.moveUp(builder, layout));
        assertEquals(4, Selection.getSelectionStart(builder));
        assertEquals(4, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 5);
        assertFalse(Selection.moveUp(builder, layout));
        assertEquals(5, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        try {
            Selection.moveUp(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        Selection.moveUp(null, layout);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveDown",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testMoveDown() {
        CharSequence text = "hello,world\nGoogle";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.moveDown(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder, 1, 3);
        assertTrue(Selection.moveDown(builder, layout));
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(3, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 0, text.length());
        assertFalse(Selection.moveDown(builder, layout));
        assertEquals(text.length(), Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 5);
        assertTrue(Selection.moveDown(builder, layout));
        assertEquals(14, Selection.getSelectionStart(builder));
        assertEquals(14, Selection.getSelectionEnd(builder));

        assertFalse(Selection.moveDown(builder, layout));
        assertEquals(14, Selection.getSelectionStart(builder));
        assertEquals(14, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 10);
        Selection.moveDown(builder, layout);
        assertEquals(18, Selection.getSelectionStart(builder));
        assertEquals(18, Selection.getSelectionEnd(builder));

        try {
            Selection.moveDown(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.moveDown(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendSelection",
        args = {android.text.Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testExtendSelection() {
        CharSequence text = "hello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 6);
        Selection.extendSelection(builder, 6);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 6);
        Selection.extendSelection(builder, 8);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(8, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 3, 6);
        Selection.extendSelection(builder, 1);
        assertEquals(3, Selection.getSelectionStart(builder));
        assertEquals(1, Selection.getSelectionEnd(builder));

        try {
            Selection.extendSelection(builder, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendSelection(builder, 100);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendSelection(null, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendSelection(new SpannableStringBuilder(), 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendLeft",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testExtendLeft() {
        CharSequence text = "Google\nhello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.extendLeft(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder, 7, 8);
        assertTrue(Selection.extendLeft(builder, layout));
        assertEquals(7, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendLeft(builder, layout));
        assertEquals(7, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 0, 1);
        assertTrue(Selection.extendLeft(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendLeft(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        try {
            Selection.extendLeft(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendLeft(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendLeft(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendRight",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testExtendRight() {
        CharSequence text = "Google\nhello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.extendRight(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder, 1, 6);
        assertTrue(Selection.extendRight(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(7, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendRight(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(8, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 12, text.length());
        assertTrue(Selection.extendRight(builder, layout));
        assertEquals(12, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        try {
            Selection.extendRight(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendRight(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendRight(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendUp",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testExtendUp() {
        CharSequence text = "Google\nhello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendUp(builder, layout));
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 8, 15);
        assertTrue(Selection.extendUp(builder, layout));
        assertEquals(8, Selection.getSelectionStart(builder));
        assertEquals(4, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendUp(builder, layout));
        assertEquals(8, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendUp(builder, layout));
        assertEquals(8, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        try {
            Selection.extendUp(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendUp(null, layout);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        builder = new SpannableStringBuilder();
        assertTrue(Selection.extendUp(builder, layout));
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendDown",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testExtendDown() {
        CharSequence text = "Google\nhello, world";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        try {
            Selection.extendDown(builder, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        Selection.setSelection(builder, 1, 3);
        assertTrue(Selection.extendDown(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(14, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendDown(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendDown(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        try {
            Selection.extendDown(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendDown(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendDown(null, layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendToLeftEdge",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testExtendToLeftEdge() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 50, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendToLeftEdge(builder, layout));
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 4, 9);
        assertTrue(Selection.extendToLeftEdge(builder, layout));
        assertEquals(4, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 5);
        assertTrue(Selection.extendToLeftEdge(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 2, 2);
        assertTrue(Selection.extendToLeftEdge(builder, layout));
        assertEquals(2, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        try {
            Selection.extendToLeftEdge(new SpannableStringBuilder(), null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendToLeftEdge(null, layout);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        builder = new SpannableStringBuilder();
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendToLeftEdge(builder, layout));
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendToRightEdge",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testExtendToRightEdge() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 50, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendToRightEdge(builder, layout));
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 3);
        assertTrue(Selection.extendToRightEdge(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 7);
        assertTrue(Selection.extendToRightEdge(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        assertTrue(Selection.extendToRightEdge(builder, layout));
        assertEquals(1, Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        try {
            Selection.extendToRightEdge(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            Selection.extendToRightEdge(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.extendToRightEdge(null, layout);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveToLeftEdge",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1371108",explanation = "throw unexpected NullPointerException")
    public void testMoveToLeftEdge() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0, false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveToLeftEdge(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 10);
        assertTrue(Selection.moveToLeftEdge(builder, layout));
        assertEquals(6, Selection.getSelectionStart(builder));
        assertEquals(6, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 3);
        assertTrue(Selection.moveToLeftEdge(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveToLeftEdge(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));

        try {
            Selection.moveToLeftEdge(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.moveToLeftEdge(null, layout);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        builder = new SpannableStringBuilder();
        assertTrue(Selection.moveToLeftEdge(builder, layout));
        assertEquals(0, Selection.getSelectionStart(builder));
        assertEquals(0, Selection.getSelectionEnd(builder));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveToRightEdge",
        args = {android.text.Spannable.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1417734",explanation = "throw unexpected IndexOutOfBoundsException" +
            "and NullPointerException")
    public void testMoveToRightEdge() {
        CharSequence text = "hello\nworld";
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StaticLayout layout = new StaticLayout(text, new TextPaint(), 200, null, 0, 0,false);
        assertEquals(-1, Selection.getSelectionStart(builder));
        assertEquals(-1, Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveToRightEdge(builder, layout));
        assertEquals(5, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 2);
        assertTrue(Selection.moveToRightEdge(builder, layout));
        assertEquals(5, Selection.getSelectionStart(builder));
        assertEquals(5, Selection.getSelectionEnd(builder));

        Selection.setSelection(builder, 1, 7);
        assertTrue(Selection.moveToRightEdge(builder, layout));
        assertEquals(text.length(), Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        assertTrue(Selection.moveToRightEdge(builder, layout));
        assertEquals(text.length(), Selection.getSelectionStart(builder));
        assertEquals(text.length(), Selection.getSelectionEnd(builder));

        try {
            Selection.moveToRightEdge(builder, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.moveToRightEdge(null, layout);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Selection.moveToRightEdge(new SpannableStringBuilder(), layout);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
