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
package android.view.inputmethod.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

@TestTargetClass(InputMethodManager.class)
public class InputMethodManagerTest
                  extends ActivityInstrumentationTestCase2<InputMethodStubActivity> {

    public InputMethodManagerTest() {
        super("com.android.cts.stub", InputMethodStubActivity.class);
    }

    private InputMethodStubActivity mActivity;
    private Instrumentation mInstrumentation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        // Close soft input just in case.
        sendKeys(KeyEvent.KEYCODE_BACK);
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "hideSoftInputFromInputMethod",
            args = {IBinder.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "hideSoftInputFromWindow",
            args = {IBinder.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "hideSoftInputFromWindow",
            args = {IBinder.class, int.class, ResultReceiver.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "isAcceptingText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "isActive",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "isActive",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFullscreenMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isWatchingCursor",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "restartInput",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getEnabledInputMethodList",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getInputMethodList",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setInputMethod",
            args = {IBinder.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "showSoftInput",
            args = {View.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "showSoftInput",
            args = {View.class, int.class, ResultReceiver.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "showSoftInputFromInputMethod",
            args = {IBinder.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "toggleSoftInputFromWindow",
            args = {IBinder.class, int.class, int.class}
        )
    })
    @UiThreadTest
    public void testInputMethodManager() {
        Window window = mActivity.getWindow();
        EditText view = (EditText) window.findViewById(R.id.entry);

        BaseInputConnection connection = new BaseInputConnection(view, false);
        Context context = mInstrumentation.getTargetContext();
        InputMethodManager imManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        assertTrue(imManager.isActive());
        assertTrue(imManager.isAcceptingText());
        assertTrue(imManager.isActive(view));

        connection.reportFullscreenMode(false);
        assertFalse(imManager.isFullscreenMode());
        connection.reportFullscreenMode(true);
        assertTrue(imManager.isFullscreenMode());

        IBinder token = view.getWindowToken();

        // Show and hide input method.
        assertTrue(imManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT));
        assertTrue(imManager.hideSoftInputFromWindow(token, 0));

        Handler handler = new Handler();
        ResultReceiver receiver = new ResultReceiver(handler);
        assertTrue(imManager.showSoftInput(view, 0, receiver));
        receiver = new ResultReceiver(handler);
        assertTrue(imManager.hideSoftInputFromWindow(token, 0, receiver));

        imManager.showSoftInputFromInputMethod(token, InputMethodManager.SHOW_FORCED);
        imManager.hideSoftInputFromInputMethod(token, InputMethodManager.HIDE_NOT_ALWAYS);

        // status: hide to show to hide
        imManager.toggleSoftInputFromWindow(token, 0, InputMethodManager.HIDE_NOT_ALWAYS);
        imManager.toggleSoftInputFromWindow(token, 0, InputMethodManager.HIDE_NOT_ALWAYS);

        List<InputMethodInfo> enabledImList = imManager.getEnabledInputMethodList();
        if (enabledImList != null && enabledImList.size() > 0) {
            imManager.setInputMethod(token, enabledImList.get(0).getId());
            // cannot test whether setting was successful
        }

        List<InputMethodInfo> imList = imManager.getInputMethodList();
        if (imList != null && enabledImList != null) {
            assertTrue(imList.size() >= enabledImList.size());
        }
    }
}
