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

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.method.BaseKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Test the main functionalities of the BaseKeyListener.
 */
@TestTargetClass(BaseKeyListener.class)
public class BaseKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private static final CharSequence TEST_STRING = "123456";
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;

    public BaseKeyListenerTest(){
        super("com.android.cts.stub", KeyListenerStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mTextView = (TextView) mActivity.findViewById(R.id.keylistener_textview);
    }

    /**
     * Check point:
     * 1. Set the cursor and press DEL key, the character before cursor is deleted.
     * 2. Set a selection and press DEL key, the selection is deleted.
     * 3. Press ALT+DEL key, the whole content of TextView is deleted.
     * 4. when there is no any selections and press DEL key, an IndexOutOfBoundsException occurs
     * 5. ALT+DEL does not delete everything where there is a selection
     * 6. DEL key does not take effect when text view does not have BaseKeyListener.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "backspace",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "1. when there is no any selections, " +
            "an IndexOutOfBoundsException occurs. " +
            "2. ALT+DEL does not delete everything where there is a selection, " +
            "javadoc does not explain this situation")
    public void testBackspace() {
        Editable content;
        final MockBaseKeyListener baseKeyListener = new MockBaseKeyListener();
        KeyEvent delKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);

        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        Selection.setSelection(content, 0, 0);
        baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL, delKeyEvent);
        assertEquals("123456", content.toString());

        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        Selection.setSelection(content, 0, 3);
        baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL, delKeyEvent);
        assertEquals("456", content.toString());

        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        try {
            baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL,delKeyEvent);
            fail("did not throw IndexOutOfBoundsException when there is no selections");
        } catch (IndexOutOfBoundsException e) {
            // expected.
        }

        final String str = "123456";
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(baseKeyListener);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 1);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        // delete the first character '1'
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("23456", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 3);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        // delete character '2' and '3'
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("1456", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        // delete everything on the line the cursor is on.
        sendKeys(KeyEvent.KEYCODE_ALT_LEFT);
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 2, 4);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        // ALT+DEL deletes the selection only.
        sendKeys(KeyEvent.KEYCODE_ALT_LEFT);
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("1256", mTextView.getText().toString());

        // text view does not have BaseKeyListener
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 1);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        // DEL key does not take effect
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals(str, mTextView.getText().toString());
    }

    /**
     * Check point:
     * 1. Press 0 key, the content of TextView does not changed.
     * 2. Set a selection and press DEL key, the selection is deleted.
     * 3. ACTION_MULTIPLE KEYCODE_UNKNOWN by inserting the event's text into the content.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {View.class, Editable.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyOther",
            args = {View.class, Editable.class, KeyEvent.class}
        )
    })
    @ToBeFixed(bug = "1731439", explanation = "onKeyOther doesn't inserts the" +
            " event's text into content.")
    public void testPressKey() {
        final CharSequence str = "123456";
        final MockBaseKeyListener baseKeyListener = new MockBaseKeyListener();

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(baseKeyListener);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("123456", mTextView.getText().toString());
        // press '0' key.
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals("123456", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Selection.setSelection((Editable) mTextView.getText(), 1, 2);
            }
        });
        mInstrumentation.waitForIdleSync();
        // delete character '2'
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("13456", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Selection.setSelection((Editable) mTextView.getText(), 2, 2);
            }
        });
        mInstrumentation.waitForIdleSync();
        // test ACTION_MULTIPLE KEYCODE_UNKNOWN key event.
        KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), "abcd",
                KeyCharacterMap.BUILT_IN_KEYBOARD, 0);
        mInstrumentation.sendKeySync(event);
        mInstrumentation.waitForIdleSync();
        // the text of TextView is never changed, onKeyOther never works.
//        assertEquals("13abcd456", mTextView.getText().toString());
    }

    private class MockBaseKeyListener extends BaseKeyListener {
        public int getInputType() {
            return InputType.TYPE_CLASS_DATETIME
                    | InputType.TYPE_DATETIME_VARIATION_DATE;
        }
    }
}
