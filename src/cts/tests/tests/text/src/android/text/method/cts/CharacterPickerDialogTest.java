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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.text.Editable;
import android.text.Selection;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.TextView;

@TestTargetClass(CharacterPickerDialog.class)
public class CharacterPickerDialogTest extends
        ActivityInstrumentationTestCase2<StubActivity> {
    private Activity mActivity;

    public CharacterPickerDialogTest() {
        super("com.android.cts.stub", StubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "CharacterPickerDialog",
        args = {Context.class, View.class, Editable.class, String.class, boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throw in the javadoc.")
    public void testConstructor() {
        final CharSequence str = "123456";
        final Editable content = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        new CharacterPickerDialog(view.getContext(), view, content, "\u00A1", false);

        try {
            new CharacterPickerDialog(null, view, content, "\u00A1", false);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected.
        }
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onCreate",
        args = {Bundle.class}
    )
    public void testOnCreate() {
        // Do not test. Implementation details.
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "only position is read in this method",
        method = "onItemClick",
        args = {AdapterView.class, View.class, int.class, long.class}
    )
    public void testOnItemClick() {
        final Gallery parent = new Gallery(mActivity);
        final CharSequence str = "123456";
        Editable text = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        CharacterPickerDialog replacePickerDialog =
                new CharacterPickerDialog(view.getContext(), view, text, "abc", false);

        // insert 'a' to the beginning of text
        replacePickerDialog.show();
        Selection.setSelection(text, 0, 0);
        assertEquals(str, text.toString());
        assertTrue(replacePickerDialog.isShowing());

        replacePickerDialog.onItemClick(parent, view, 0, 0);
        assertEquals("a123456", text.toString());
        assertFalse(replacePickerDialog.isShowing());

        // replace the second character '1' with 'c'
        replacePickerDialog.show();
        Selection.setSelection(text, 2, 2);
        assertTrue(replacePickerDialog.isShowing());

        replacePickerDialog.onItemClick(parent, view, 2, 0);
        assertEquals("ac23456", text.toString());
        assertFalse(replacePickerDialog.isShowing());

        // insert character 'c' between '2' and '3'
        text = Editable.Factory.getInstance().newEditable(str);
        CharacterPickerDialog insertPickerDialog =
            new CharacterPickerDialog(view.getContext(), view, text, "abc", true);
        Selection.setSelection(text, 2, 2);
        assertEquals(str, text.toString());
        insertPickerDialog.show();
        assertTrue(insertPickerDialog.isShowing());

        insertPickerDialog.onItemClick(parent, view, 2, 0);
        assertEquals("12c3456", text.toString());
        assertFalse(insertPickerDialog.isShowing());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onClick",
        args = {View.class}
    )
    public void testOnClick() {
        final CharSequence str = "123456";
        final Editable content = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        CharacterPickerDialog characterPickerDialog =
                new CharacterPickerDialog(view.getContext(), view, content, "\u00A1", false);

        characterPickerDialog.show();
        assertTrue(characterPickerDialog.isShowing());

        // nothing to test here, just make sure onClick does not throw exception
        characterPickerDialog.onClick(view);

    }
}
