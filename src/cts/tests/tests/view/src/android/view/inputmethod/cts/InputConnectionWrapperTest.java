/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.view.inputmethod.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

@TestTargetClass(InputConnectionWrapper.class)
public class InputConnectionWrapperTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InputConnectionWrapper",
            args = {InputConnection.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTarget",
            args = {InputConnection.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "beginBatchEdit",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "commitCompletion",
            args = {CompletionInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "endBatchEdit",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExtractedText",
            args = {ExtractedTextRequest.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performContextMenuAction",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performEditorAction",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performPrivateCommand",
            args = {String.class, Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelection",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextAfterCursor",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextBeforeCursor",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "getSelectedText",
                args = {int.class}
            ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCursorCapsMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearMetaKeyStates",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "commitText",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "deleteSurroundingText",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishComposingText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setComposingText",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "setComposingRegion",
                args = {int.class, int.class}
            ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "sendKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reportFullscreenMode",
            args = {boolean.class}
        )
    })
    public void testInputConnectionWrapper() {
        MockInputConnection inputConnection = new MockInputConnection();
        InputConnectionWrapper wrapper = new InputConnectionWrapper(null, true);
        try {
            wrapper.beginBatchEdit();
            fail("Failed to throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
        wrapper.setTarget(inputConnection);

        wrapper.beginBatchEdit();
        assertTrue(inputConnection.isBeginBatchEditCalled);
        wrapper.clearMetaKeyStates(KeyEvent.META_ALT_ON);
        assertTrue(inputConnection.isClearMetaKeyStatesCalled);
        wrapper.commitCompletion(new CompletionInfo(1, 1, "testText"));
        assertTrue(inputConnection.isCommitCompletionCalled);
        wrapper.commitText("Text", 1);
        assertTrue(inputConnection.isCommitTextCalled);
        wrapper.deleteSurroundingText(10, 100);
        assertTrue(inputConnection.isDeleteSurroundingTextCalled);
        wrapper.endBatchEdit();
        assertTrue(inputConnection.isEndBatchEditCalled);
        wrapper.finishComposingText();
        assertTrue(inputConnection.isFinishComposingTextCalled);
        wrapper.getCursorCapsMode(TextUtils.CAP_MODE_CHARACTERS);
        assertTrue(inputConnection.isGetCursorCapsModeCalled);
        wrapper.getExtractedText(new ExtractedTextRequest(), 0);
        assertTrue(inputConnection.isGetExtractedTextCalled);
        wrapper.getTextAfterCursor(5, 0);
        assertTrue(inputConnection.isGetTextAfterCursorCalled);
        wrapper.getTextBeforeCursor(3, 0);
        assertTrue(inputConnection.isGetTextBeforeCursorCalled);
        wrapper.performContextMenuAction(1);
        assertTrue(inputConnection.isPerformContextMenuActionCalled);
        wrapper.performEditorAction(EditorInfo.IME_ACTION_GO);
        assertTrue(inputConnection.isPerformEditorActionCalled);
        wrapper.performPrivateCommand("com.android.action.MAIN", new Bundle());
        assertTrue(inputConnection.isPerformPrivateCommandCalled);
        wrapper.reportFullscreenMode(true);
        assertTrue(inputConnection.isReportFullscreenModeCalled);
        wrapper.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
        assertTrue(inputConnection.isSendKeyEventCalled);
        wrapper.setComposingText("Text", 1);
        assertTrue(inputConnection.isSetComposingTextCalled);
        wrapper.setSelection(0, 10);
        assertTrue(inputConnection.isSetSelectionCalled);
        wrapper.getSelectedText(0);
        assertTrue(inputConnection.isGetSelectedTextCalled);
        wrapper.setComposingRegion(0, 3);
        assertTrue(inputConnection.isSetComposingRegionCalled);
    }

    private class MockInputConnection implements InputConnection {
        public boolean isBeginBatchEditCalled;
        public boolean isClearMetaKeyStatesCalled;
        public boolean isCommitCompletionCalled;
        public boolean isCommitTextCalled;
        public boolean isDeleteSurroundingTextCalled;
        public boolean isEndBatchEditCalled;
        public boolean isFinishComposingTextCalled;
        public boolean isGetCursorCapsModeCalled;
        public boolean isGetExtractedTextCalled;
        public boolean isGetTextAfterCursorCalled;
        public boolean isGetTextBeforeCursorCalled;
        public boolean isGetSelectedTextCalled;
        public boolean isPerformContextMenuActionCalled;
        public boolean isPerformEditorActionCalled;
        public boolean isPerformPrivateCommandCalled;
        public boolean isReportFullscreenModeCalled;
        public boolean isSendKeyEventCalled;
        public boolean isSetComposingTextCalled;
        public boolean isSetComposingRegionCalled;
        public boolean isSetSelectionCalled;

        public boolean beginBatchEdit() {
            isBeginBatchEditCalled = true;
            return false;
        }

        public boolean clearMetaKeyStates(int states) {
            isClearMetaKeyStatesCalled = true;
            return false;
        }

        public boolean commitCompletion(CompletionInfo text) {
            isCommitCompletionCalled = true;
            return false;
        }

        public boolean commitText(CharSequence text, int newCursorPosition) {
            isCommitTextCalled = true;
            return false;
        }

        public boolean deleteSurroundingText(int leftLength, int rightLength) {
            isDeleteSurroundingTextCalled = true;
            return false;
        }

        public boolean endBatchEdit() {
            isEndBatchEditCalled = true;
            return false;
        }

        public boolean finishComposingText() {
            isFinishComposingTextCalled = true;
            return false;
        }

        public int getCursorCapsMode(int reqModes) {
            isGetCursorCapsModeCalled = true;
            return 0;
        }

        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            isGetExtractedTextCalled = true;
            return null;
        }

        public CharSequence getTextAfterCursor(int n, int flags) {
            isGetTextAfterCursorCalled = true;
            return null;
        }

        public CharSequence getTextBeforeCursor(int n, int flags) {
            isGetTextBeforeCursorCalled = true;
            return null;
        }

        public CharSequence getSelectedText(int flags) {
            isGetSelectedTextCalled = true;
            return null;
        }

        public boolean performContextMenuAction(int id) {
            isPerformContextMenuActionCalled = true;
            return false;
        }

        public boolean performEditorAction(int editorAction) {
            isPerformEditorActionCalled = true;
            return false;
        }

        public boolean performPrivateCommand(String action, Bundle data) {
            isPerformPrivateCommandCalled = true;
            return false;
        }

        public boolean reportFullscreenMode(boolean enabled) {
            isReportFullscreenModeCalled = true;
            return false;
        }

        public boolean sendKeyEvent(KeyEvent event) {
            isSendKeyEventCalled = true;
            return false;
        }

        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            isSetComposingTextCalled = true;
            return false;
        }

        public boolean setComposingRegion(int start, int end) {
            isSetComposingRegionCalled = true;
            return false;
        }

        public boolean setSelection(int start, int end) {
            isSetSelectionCalled = true;
            return false;
        }
    }
}
