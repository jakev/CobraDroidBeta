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

package android.widget.cts;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.test.AndroidTestCase;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.EditText;
import android.widget.TextView.BufferType;

import com.android.cts.stub.R;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(EditText.class)
public class EditTextTest extends AndroidTestCase {
    private Context mContext;
    private AttributeSet mAttributeSet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContext = getContext();
        XmlPullParser parser = mContext.getResources().getXml(R.layout.edittext_layout);
        mAttributeSet = Xml.asAttributeSet(parser);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link EditText}",
            method = "EditText",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link EditText}",
            method = "EditText",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link EditText}",
            method = "EditText",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="EditText#EditText(Context), " +
            "EditText#EditText(Context, AttributeSet) and " +
            "EditText#EditText(Context, AttributeSet, int)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new EditText(mContext);

        new EditText(mContext, null);

        new EditText(mContext, null, 0);

        new EditText(mContext, mAttributeSet);

        new EditText(mContext, mAttributeSet, 0);

        try {
            new EditText(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }

        try {
            new EditText(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }

        try {
            new EditText(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setText",
            args = {java.lang.CharSequence.class, android.widget.TextView.BufferType.class}
        )
    })
    public void testAccessText() {
        EditText editText = new EditText(mContext, mAttributeSet);

        editText.setText("android", BufferType.NORMAL);
        assertEquals("android", editText.getText().toString());

        editText.setText("", BufferType.SPANNABLE);
        assertEquals("", editText.getText().toString());

        editText.setText(null, BufferType.EDITABLE);
        assertEquals("", editText.getText().toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link EditText#setSelection(int)}",
        method = "setSelection",
        args = {int.class}
    )
    public void testSetSelectionIndex() {
        EditText editText = new EditText(mContext, mAttributeSet);

        String string = "android";
        editText.setText(string, BufferType.EDITABLE);
        int position = 4;
        editText.setSelection(position);
        assertEquals(position, editText.getSelectionStart());
        assertEquals(position, editText.getSelectionEnd());

        position = 0;
        editText.setSelection(position);
        assertEquals(position, editText.getSelectionStart());
        assertEquals(position, editText.getSelectionEnd());

        try {
            editText.setSelection(-1);
            fail("An IndexOutOfBoundsException should be thrown out.");
        } catch (IndexOutOfBoundsException e) {
            //expected, test success.
        }

        try {
            editText.setSelection(string.length() + 1);
            fail("An IndexOutOfBoundsException should be thrown out.");
        } catch (IndexOutOfBoundsException e) {
            //expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link EditText#setSelection(int, int)}",
        method = "setSelection",
        args = {int.class, int.class}
    )
    public void testSetSelectionStartstop() {
        EditText editText = new EditText(mContext, mAttributeSet);

        String string = "android";
        editText.setText(string, BufferType.EDITABLE);
        int start = 1;
        int end = 2;
        editText.setSelection(start, end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        start = 0;
        end = 0;
        editText.setSelection(start, end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        start = 7;
        end = 1;
        editText.setSelection(start, end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        try {
            editText.setSelection(-5, -1);
            fail("An IndexOutOfBoundsException should be thrown out.");
        } catch (IndexOutOfBoundsException e) {
            //expected, test success.
        }

        try {
            editText.setSelection(5, string.length() + 1);
            fail("An IndexOutOfBoundsException should be thrown out.");
        } catch (IndexOutOfBoundsException e) {
            //expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link EditText#selectAll()}",
        method = "selectAll",
        args = {}
    )
    public void testSelectAll() {
        EditText editText = new EditText(mContext, mAttributeSet);

        String string = "android";
        editText.setText(string, BufferType.EDITABLE);
        editText.selectAll();
        assertEquals(0, editText.getSelectionStart());
        assertEquals(string.length(), editText.getSelectionEnd());

        editText.setText("", BufferType.EDITABLE);
        editText.selectAll();
        assertEquals(0, editText.getSelectionStart());
        assertEquals(0, editText.getSelectionEnd());

        editText.setText(null, BufferType.EDITABLE);
        editText.selectAll();
        assertEquals(0, editText.getSelectionStart());
        assertEquals(0, editText.getSelectionEnd());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link EditText#extendSelection(int)}",
        method = "extendSelection",
        args = {int.class}
    )
    public void testExtendSelection() {
        EditText editText = new EditText(mContext, mAttributeSet);

        editText.setText("android", BufferType.EDITABLE);
        int start = 0;
        int end = 0;
        editText.setSelection(start, end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        end = 6;
        editText.extendSelection(end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        start = 0;
        end = 0;
        editText.setSelection(start);
        editText.extendSelection(end);
        assertEquals(start, editText.getSelectionStart());
        assertEquals(end, editText.getSelectionEnd());

        try {
            editText.setSelection(0, 4);
            editText.extendSelection(10);
            fail("An IndexOutOfBoundsException should be thrown out.");
        } catch (IndexOutOfBoundsException e) {
            //expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link EditText#getDefaultEditable()}, this function always returns true",
        method = "getDefaultEditable",
        args = {}
    )
    public void testGetDefaultEditable() {
        MockEditText mockEditText = new MockEditText(mContext, mAttributeSet);

        assertTrue(mockEditText.getDefaultEditable());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "{@link EditText#getDefaultMovementMethod()}",
        method = "getDefaultMovementMethod",
        args = {}
    )
    public void testGetDefaultMovementMethod() {
        MockEditText mockEditText = new MockEditText(mContext, mAttributeSet);
        MovementMethod method1 = mockEditText.getDefaultMovementMethod();
        MovementMethod method2 = mockEditText.getDefaultMovementMethod();

        assertNotNull(method1);
        assertTrue(method1 instanceof ArrowKeyMovementMethod);

        assertSame(method1, method2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test {@link EditText#setEllipsize(TextUtils.TruncateAt)}",
        method = "setEllipsize",
        args = {android.text.TextUtils.TruncateAt.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of " +
            "EditText#setEllipsize(TextUtils.TruncateAt)")
    public void testSetEllipsize() {
        EditText editText = new EditText(mContext);
        assertNull(editText.getEllipsize());

        editText.setEllipsize(TextUtils.TruncateAt.START);
        assertSame(TextUtils.TruncateAt.START, editText.getEllipsize());

        try {
            editText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected, test success.
        }
    }

    private class MockEditText extends EditText {
        public MockEditText(Context context) {
            super(context);
        }

        public MockEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MockEditText(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected boolean getDefaultEditable() {
            return super.getDefaultEditable();
        }

        @Override
        protected MovementMethod getDefaultMovementMethod() {
            return super.getDefaultMovementMethod();
        }
    }
}
