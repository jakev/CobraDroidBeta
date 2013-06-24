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

package android.text.method.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MetaKeyKeyListener;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Test {@link ArrowKeyMovementMethod}. The class is an implementation of interface
 * {@link MovementMethod}. The typical usage of {@link MovementMethod} is tested in
 * {@link android.widget.cts.TextViewTest} and this test case is only focused on the
 * implementation of the methods.
 *
 * @see android.widget.cts.TextViewTest
 */
@TestTargetClass(ArrowKeyMovementMethod.class)
public class ArrowKeyMovementMethodTest extends ActivityInstrumentationTestCase2<StubActivity> {
    private static final String THREE_LINES_TEXT = "first line\nsecond line\nlast line";
    private static final int END_OF_ALL_TEXT = THREE_LINES_TEXT.length();
    private static final int END_OF_1ST_LINE = THREE_LINES_TEXT.indexOf('\n');
    private static final int START_OF_2ND_LINE = END_OF_1ST_LINE + 1;
    private static final int END_OF_2ND_LINE = THREE_LINES_TEXT.indexOf('\n', START_OF_2ND_LINE);
    private static final int START_OF_3RD_LINE = END_OF_2ND_LINE + 1;
    private static final int SPACE_IN_2ND_LINE = THREE_LINES_TEXT.indexOf(' ', START_OF_2ND_LINE);
    private TextView mTextView;
    private ArrowKeyMovementMethod mArrowKeyMovementMethod;
    private Editable mEditable;
    private MyMetaKeyKeyListener mMetaListener;

    public ArrowKeyMovementMethodTest() {
        super("com.android.cts.stub", StubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMetaListener = new MyMetaKeyKeyListener();
        mArrowKeyMovementMethod = new ArrowKeyMovementMethod();

        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                getActivity().setContentView(mTextView);
                mTextView.setFocusable(true);
                mTextView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertNotNull(mTextView.getLayout());
        assertTrue(mTextView.isFocused());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor ArrowKeyMovementMethod#ArrowKeyMovementMethod().",
        method = "ArrowKeyMovementMethod",
        args = {}
    )
    public void testConstructor() {
        new ArrowKeyMovementMethod();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#canSelectArbitrarily()}. "
                + "It always returns true.",
        method = "canSelectArbitrarily",
        args = {}
    )
    public void testCanSelectArbitrarily() {
        assertTrue(new ArrowKeyMovementMethod().canSelectArbitrarily());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#getInstance()}. "
                + "This is a method for creating singleton.",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        MovementMethod method0 = ArrowKeyMovementMethod.getInstance();
        assertNotNull(method0);

        MovementMethod method1 = ArrowKeyMovementMethod.getInstance();
        assertNotNull(method1);
        assertSame(method0, method1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTakeFocus(TextView, Spannable, int)}. "
                + "Test the method after the widget get layouted.",
        method = "onTakeFocus",
        args = {TextView.class, Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    public void testOnTakeFocus() throws Throwable {
        /*
         * The following assertions depend on whether the TextView has a layout.
         * The text view will not get layout in setContent method but in other
         * handler's function. Assertion which is following the setContent will
         * not get the expecting result. It have to wait all the handlers'
         * operations on the UiTread to finish. So all these cases are divided
         * into several steps, setting the content at first, waiting the layout,
         * and checking the assertion at last.
         */
        assertSelection(-1);
        runTestOnUiThread(new Runnable() {
            public void run() {
                Selection.removeSelection(mEditable);
                mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_UP);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertSelection(END_OF_ALL_TEXT);

        runTestOnUiThread(new Runnable() {
            public void run() {
                Selection.removeSelection(mEditable);
                mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_LEFT);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertSelection(END_OF_ALL_TEXT);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mTextView.setSingleLine();
            }
        });
        // wait until the textView gets layout
        getInstrumentation().waitForIdleSync();
        assertNotNull(mTextView.getLayout());
        assertEquals(1, mTextView.getLayout().getLineCount());

        runTestOnUiThread(new Runnable() {
            public void run() {
                Selection.removeSelection(mEditable);
                mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_UP);
            }
        });
        assertSelection(END_OF_ALL_TEXT);

        runTestOnUiThread(new Runnable() {
            public void run() {
                Selection.removeSelection(mEditable);
                mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_LEFT);
            }
        });
        assertSelection(END_OF_ALL_TEXT);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTakeFocus(TextView, Spannable, int)}. "
                + "Test the method before the widget get layouted.",
        method = "onTakeFocus",
        args = {TextView.class, Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    public void testOnTakeFoucusWithNullLayout() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();

        assertSelectEndOfContent();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTakeFocus(TextView, Spannable, int)}. "
                + "Test the method with null parameters.",
        method = "onTakeFocus",
        args = {TextView.class, Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#onTakeFocus(TextView, "
            + "Spannable, int)} when the params view or text is null")
    public void testOnTakeFocusWithNullParameters() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();
        try {
            mArrowKeyMovementMethod.onTakeFocus(null, mEditable, View.FOCUS_DOWN);
            fail("The method did not throw NullPointerException when param textView is null.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            mArrowKeyMovementMethod.onTakeFocus(mTextView, null, View.FOCUS_DOWN);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. KeyEvent parameter is never read.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyDownWithKeyCodeUp() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // |first line
        // second |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, 0);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // first lin|e
        // second |line
        // last line
        assertEquals(SPACE_IN_2ND_LINE, Selection.getSelectionStart(mEditable));
        int correspondingIn1stLine = Selection.getSelectionEnd(mEditable);
        assertTrue(correspondingIn1stLine >= 0);
        assertTrue(correspondingIn1stLine <= END_OF_1ST_LINE);

        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // |first line
        // second |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, 0);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // |first line
        // second line
        // last line
        assertSelection(0);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        MetaKeyKeyListener.resetMetaState(mEditable);
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // first lin|e
        // second line
        // last line
        assertSelection(correspondingIn1stLine);

        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_UP, null));
        // first lin|e
        // second line
        // last line
        assertSelection(correspondingIn1stLine);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. KeyEvent parameter is never read.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyDownWithKeyCodeDown() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second |line
        // last line|
        assertSelection(SPACE_IN_2ND_LINE, END_OF_ALL_TEXT);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second |line
        // last lin|e
        assertEquals(SPACE_IN_2ND_LINE, Selection.getSelectionStart(mEditable));
        int correspondingIn3rdLine = Selection.getSelectionEnd(mEditable);
        assertTrue(correspondingIn3rdLine >= START_OF_3RD_LINE);
        assertTrue(correspondingIn3rdLine <= END_OF_ALL_TEXT);

        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second |line
        // last line|
        assertSelection(SPACE_IN_2ND_LINE, END_OF_ALL_TEXT);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second line
        // last line|
        assertSelection(END_OF_ALL_TEXT);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        MetaKeyKeyListener.resetMetaState(mEditable);
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second line
        // last lin|e
        assertSelection(correspondingIn3rdLine);

        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_DOWN, null));
        // first line
        // second line
        // last lin|e
        assertSelection(correspondingIn3rdLine);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. KeyEvent parameter is never read.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyDownWithKeyCodeLeft() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // |second |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, START_OF_2ND_LINE);

        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // |second |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, START_OF_2ND_LINE);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // second| |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, SPACE_IN_2ND_LINE - 1);

        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // secon|d |line
        // last line
        assertSelection(SPACE_IN_2ND_LINE, SPACE_IN_2ND_LINE - 2);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // |second line
        // last line
        assertSelection(START_OF_2ND_LINE);

        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // |second line
        // last line
        assertSelection(START_OF_2ND_LINE);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        MetaKeyKeyListener.resetMetaState(mEditable);
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line
        // second| line
        // last line
        assertSelection(SPACE_IN_2ND_LINE - 1);

        Selection.setSelection(mEditable, START_OF_2ND_LINE);
        // first line
        // |second line
        // last line
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_LEFT, null));
        // first line|
        // second line
        // last line
        assertSelection(END_OF_1ST_LINE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. KeyEvent parameter is never read.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyDownWithKeyCodeRight() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second |line|
        // last line
        assertSelection(SPACE_IN_2ND_LINE, END_OF_2ND_LINE);

        pressBothShiftAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second |line|
        // last line
        assertSelection(SPACE_IN_2ND_LINE, END_OF_2ND_LINE);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second |l|ine
        // last line
        assertSelection(SPACE_IN_2ND_LINE, SPACE_IN_2ND_LINE + 1);

        pressShift();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second |li|ne
        // last line
        assertSelection(SPACE_IN_2ND_LINE, SPACE_IN_2ND_LINE + 2);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second line|
        // last line
        assertSelection(END_OF_2ND_LINE);

        pressAlt();
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second line|
        // last line
        assertSelection(END_OF_2ND_LINE);

        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        MetaKeyKeyListener.resetMetaState(mEditable);
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second l|ine
        // last line
        assertSelection(SPACE_IN_2ND_LINE + 1);

        Selection.setSelection(mEditable, END_OF_2ND_LINE);
        // first line
        // second line|
        // last line
        assertTrue(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_RIGHT, null));
        // first line
        // second line
        // |last line
        assertSelection(START_OF_3RD_LINE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. Test the method before the widget get layouted.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#onKeyDown(TextView, "
            + "Spannable, int, KeyEvent)} when the view does not get layout")
    public void testOnKeyDownWithNullLayout() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();

        try {
            mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable, KeyEvent.KEYCODE_DPAD_RIGHT,
                    null);
            fail("The method did not throw NullPointerException when layout of the view is null.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyOther(TextView, Spannable, KeyEvent)}.",
        method = "onKeyOther",
        args = {TextView.class, Spannable.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyOther() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);

        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_DPAD_CENTER, 2)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_0, 2)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_E, 2)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_UNKNOWN, 2)));

        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP, 0)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, 0)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0)));
        assertFalse(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0)));

        // only repeat arrow key events get handled
        assertTrue(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_DPAD_UP, 2)));
        assertTrue(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_DPAD_DOWN, 2)));
        assertTrue(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_DPAD_LEFT, 2)));
        assertTrue(mArrowKeyMovementMethod.onKeyOther(mTextView, mEditable,
                new KeyEvent(0, 0, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_DPAD_RIGHT, 2)));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. Test the method with other key code except up, down, left ,right.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    @UiThreadTest
    public void testOnKeyDownWithOtherKeyCode() {
        // first line
        // second |line
        // last line
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);

        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_DPAD_CENTER, null));
        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_0, null));
        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_E, null));
        assertFalse(mArrowKeyMovementMethod.onKeyDown(mTextView, mEditable,
                KeyEvent.KEYCODE_UNKNOWN, null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyDown(TextView, Spannable, int, "
                + "KeyEvent)}. Test the method with null parameters.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#onKeyDown(TextView, "
            + "Spannable, int, KeyEvent)} when the params view or buffer is null")
    public void testOnKeyDownWithNullParameters() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();
        try {
            mArrowKeyMovementMethod.onKeyDown(null, mEditable, KeyEvent.KEYCODE_DPAD_RIGHT,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
            fail("The method did not throw NullPointerException when param textView is null.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            mArrowKeyMovementMethod.onKeyDown(mTextView, null, KeyEvent.KEYCODE_DPAD_RIGHT,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTouchEvent(TextView, Spannable,"
            + " MotionEvent)}. Test the method while the widget is focused.",
        method = "onTouchEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1400249", explanation = "There is a side effect that the "
            + "view scroll while dragging on the screen. Should be tested in functional test.")
    public void testOnTouchEvent() throws Throwable {
        long now = SystemClock.currentThreadTimeMillis();
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        assertFalse(mArrowKeyMovementMethod.onTouchEvent(mTextView, mEditable,
                MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 1, 1, 0)));
        assertSelection(SPACE_IN_2ND_LINE);

        assertFalse(mArrowKeyMovementMethod.onTouchEvent(mTextView, mEditable,
                MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 1, 1,
                        KeyEvent.META_SHIFT_ON)));
        assertSelection(SPACE_IN_2ND_LINE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTouchEvent(TextView, Spannable, "
                + "MotionEvent)}. Test the method before the widget get layouted.",
        method = "onTouchEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#onTouchEvent(TextView, "
            + "Spannable, MotionEvent)} when the view does not get layout")
    public void testOnTouchEventWithNullLayout() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();
        mTextView.setFocusable(true);
        mTextView.requestFocus();
        assertTrue(mTextView.isFocused());

        long now = SystemClock.currentThreadTimeMillis();
        assertFalse(mArrowKeyMovementMethod.onTouchEvent(mTextView, mEditable,
                    MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 1, 1, 0)));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTouchEvent(TextView, Spannable, "
                + "MotionEvent)}. Test the method while the widget is not focused.",
        method = "onTouchEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    public void testOnTouchEventWithoutFocus() {
        long now = SystemClock.currentThreadTimeMillis();
        Selection.setSelection(mEditable, SPACE_IN_2ND_LINE);
        assertFalse(mArrowKeyMovementMethod.onTouchEvent(mTextView, mEditable,
                MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 1, 1, 0)));
        assertSelection(SPACE_IN_2ND_LINE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTouchEvent(TextView, Spannable, "
            + "MotionEvent)}. Test the method with null parameters.",
        method = "onTouchEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#onTouchEvent(TextView, "
            + "Spannable, MotionEvent)} when the params view, buffer or event is null")
    public void testOnTouchEventWithNullParameters() {
        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();
        try {
            mArrowKeyMovementMethod.onTouchEvent(null, mEditable,
                    MotionEvent.obtain(0, 0, 0, 1, 1, 0));
            fail("The method did not throw NullPointerException when param textView is null.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            mArrowKeyMovementMethod.onTouchEvent(mTextView, null,
                    MotionEvent.obtain(0, 0, 0, 1, 1, 0));
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            mArrowKeyMovementMethod.onTouchEvent(mTextView, mEditable, null);
            fail("The method did not throw NullPointerException when param motionEvent is null.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#initialize(TextView, Spannable)}. "
                + "TextView parameter is never read.",
        method = "initialize",
        args = {TextView.class, Spannable.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of ArrowKeyMovementMethod#initialize(TextView, "
            + "Spannable)} when the params text is null")
    public void testInitialize() {
        Spannable spannable = new SpannableString("test content");
        ArrowKeyMovementMethod method = new ArrowKeyMovementMethod();

        assertEquals(-1, Selection.getSelectionStart(spannable));
        assertEquals(-1, Selection.getSelectionEnd(spannable));
        method.initialize(null, spannable);
        assertEquals(0, Selection.getSelectionStart(spannable));
        assertEquals(0, Selection.getSelectionEnd(spannable));

        Selection.setSelection(spannable, 2);
        assertEquals(2, Selection.getSelectionStart(spannable));
        assertEquals(2, Selection.getSelectionEnd(spannable));
        method.initialize(null, spannable);
        assertEquals(0, Selection.getSelectionStart(spannable));
        assertEquals(0, Selection.getSelectionEnd(spannable));

        try {
            method.initialize(mTextView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onTrackballEvent(TextView, Spannable, "
                + "MotionEvent)}. This method always returns false.",
        method = "onTrackballEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. "
            + "There is no document about behaviour of this method.")
    public void testOnTrackballEven() {
        assertFalse(mArrowKeyMovementMethod.onTrackballEvent(mTextView, mEditable,
                MotionEvent.obtain(0, 0, 0, 1, 1, 0)));

        initTextViewWithNullLayout();
        mEditable = (Editable) mTextView.getText();

        assertFalse(mArrowKeyMovementMethod.onTrackballEvent(mTextView, mEditable,
                MotionEvent.obtain(0, 0, 0, 1, 1, 0)));

        assertFalse(mArrowKeyMovementMethod.onTrackballEvent(mTextView, null,
                MotionEvent.obtain(0, 0, 0, 1, 1, 0)));

        assertFalse(mArrowKeyMovementMethod.onTrackballEvent(mTextView, mEditable, null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ArrowKeyMovementMethod#onKeyUp(TextView, Spannable, int, KeyEvent)}. "
                + "It always returns false.",
        method = "onKeyUp",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. "
            + "There is no document about behaviour of this method.")
    public void testOnKeyUp() {
        ArrowKeyMovementMethod method = new ArrowKeyMovementMethod();
        SpannableString spannable = new SpannableString("Test Content");
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        TextView view = new TextView(getActivity());

        assertFalse(method.onKeyUp(view, spannable, KeyEvent.KEYCODE_0, event));
        assertFalse(method.onKeyUp(null, null, 0, null));
        assertFalse(method.onKeyUp(null, spannable, KeyEvent.KEYCODE_0, event));
        assertFalse(method.onKeyUp(view, null, KeyEvent.KEYCODE_0, event));
        assertFalse(method.onKeyUp(view, spannable, 0, event));
        assertFalse(method.onKeyUp(view, spannable, KeyEvent.KEYCODE_0, null));
    }

    private void initTextViewWithNullLayout() {
        mTextView = new TextView(getActivity());
        mTextView.setText(THREE_LINES_TEXT, BufferType.EDITABLE);
        assertNull(mTextView.getLayout());
    }

    private void pressMetaKey(int metakey, int expectedState) {
        mMetaListener.onKeyDown(null, mEditable, metakey, null);
        assertEquals(1, MetaKeyKeyListener.getMetaState(mEditable, expectedState));
    }

    private void pressShift() {
        MetaKeyKeyListener.resetMetaState(mEditable);
        pressMetaKey(KeyEvent.KEYCODE_SHIFT_LEFT, MetaKeyKeyListener.META_SHIFT_ON);
    }

    private void pressAlt() {
        MetaKeyKeyListener.resetMetaState(mEditable);
        pressMetaKey(KeyEvent.KEYCODE_ALT_LEFT, MetaKeyKeyListener.META_ALT_ON);
    }

    private void pressBothShiftAlt() {
        MetaKeyKeyListener.resetMetaState(mEditable);
        pressMetaKey(KeyEvent.KEYCODE_SHIFT_LEFT, MetaKeyKeyListener.META_SHIFT_ON);
        pressMetaKey(KeyEvent.KEYCODE_ALT_LEFT, MetaKeyKeyListener.META_ALT_ON);
    }

    private void assertSelection(int position) {
        assertSelection(position, position);
    }

    private void assertSelection(int start, int end) {
        assertEquals(start, Selection.getSelectionStart(mEditable));
        assertEquals(end, Selection.getSelectionEnd(mEditable));
    }

    private void assertSelectEndOfContent() {
        Selection.removeSelection(mEditable);
        mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_DOWN);
        assertSelection(END_OF_ALL_TEXT);

        Selection.removeSelection(mEditable);
        mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_RIGHT);
        assertSelection(END_OF_ALL_TEXT);

        assertSelectEndOfContentExceptFocusForward();
    }

    private void assertSelectEndOfContentExceptFocusForward() {
        Selection.removeSelection(mEditable);
        mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_UP);
        assertSelection(END_OF_ALL_TEXT);

        Selection.removeSelection(mEditable);
        mArrowKeyMovementMethod.onTakeFocus(mTextView, mEditable, View.FOCUS_LEFT);
        assertSelection(END_OF_ALL_TEXT);
    }

    private static class MyMetaKeyKeyListener extends MetaKeyKeyListener {
    }
}
