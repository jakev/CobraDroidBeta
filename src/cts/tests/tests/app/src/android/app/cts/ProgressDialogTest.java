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

package android.app.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;

/**
 * Test {@link ProgressDialog}.
 */
@TestTargetClass(ProgressDialog.class)
public class ProgressDialogTest extends ActivityInstrumentationTestCase2<MockActivity> {
    private final CharSequence TITLE = "title";
    private final CharSequence MESSAGE = "message";

    private boolean mCanceled;
    private Drawable mDrawable;
    private Drawable mActualDrawableNull;
    private Drawable mActualDrawable;
    private ProgressBar mProgressBar;
    private int mProgress1;
    private int mProgress2;

    private Context mContext;
    private Instrumentation mInstrumentation;
    private MockActivity mActivity;
    private ProgressDialog mProgressDialog;

    public ProgressDialogTest() {
        super("com.android.cts.stub", MockActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mCanceled = false;
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mContext = mActivity;
        mProgressDialog = new ProgressDialog(mContext);
        mDrawable = getActivity().getResources().getDrawable(
                com.android.cts.stub.R.drawable.yellow);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ProgressDialog",
        args = {android.content.Context.class}
    )
    public void testProgressDialog1(){
        new ProgressDialog(mContext);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ProgressDialog",
        args = {android.content.Context.class, int.class}
    )
    public void testProgressDialog2(){
        new ProgressDialog(mContext, com.android.cts.stub.R.style.Theme_AlertDialog);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        )
    })
    public void testOnStartCreateStop() {
        MockProgressDialog pd = new MockProgressDialog(mContext);

        assertFalse(pd.mIsOnCreateCalled);
        assertFalse(pd.mIsOnStartCalled);
        pd.show();
        assertTrue(pd.mIsOnCreateCalled);
        assertTrue(pd.mIsOnStartCalled);

        assertFalse(pd.mIsOnStopCalled);
        pd.dismiss();
        assertTrue(pd.mIsOnStopCalled);
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "show",
        args = {android.content.Context.class, java.lang.CharSequence.class,
                java.lang.CharSequence.class}
    )
    @UiThreadTest
    public void testShow1() {
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "show",
        args = {android.content.Context.class, java.lang.CharSequence.class,
                java.lang.CharSequence.class, boolean.class}
    )
    @UiThreadTest
    public void testShow2() {
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, false);

        /*
         * note: the progress bar's style only supports indeterminate mode,
         * so can't change indeterminate
         */
        assertTrue(mProgressDialog.isIndeterminate());

        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, true);
        assertTrue(mProgressDialog.isIndeterminate());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "show",
        args = {android.content.Context.class, java.lang.CharSequence.class,
                java.lang.CharSequence.class, boolean.class, boolean.class}
    )
    public void testShow3() throws Throwable {
        final OnCancelListener cL = new OnCancelListener(){
            public void onCancel(DialogInterface dialog) {
                mCanceled = true;
            }
        };

        // cancelable is false
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, true, false);

                mProgressDialog.setOnCancelListener(cL);
                mProgressDialog.onBackPressed();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertFalse(mCanceled);

        // cancelable is true
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, true, true);

                assertFalse(mCanceled);
                mProgressDialog.setOnCancelListener(cL);
                mProgressDialog.onBackPressed();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(mCanceled);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "show",
        args = {android.content.Context.class, java.lang.CharSequence.class,
                java.lang.CharSequence.class, boolean.class, boolean.class,
                android.content.DialogInterface.OnCancelListener.class}
    )
    public void testShow4() throws Throwable {
        final OnCancelListener cL = new OnCancelListener(){
            public void onCancel(DialogInterface dialog) {
                mCanceled = true;
            }
        };

        // cancelable is false
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, true, false, cL);

                mProgressDialog.onBackPressed();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertFalse(mCanceled);

        // cancelable is true
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE, true, true, cL);

                assertFalse(mCanceled);
                mProgressDialog.onBackPressed();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(mCanceled);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMax",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMax",
            args = {}
        )
    })
    @UiThreadTest
    public void testAccessMax() {
        // mProgress is null
        mProgressDialog.setMax(2008);
        assertEquals(2008, mProgressDialog.getMax());

        // mProgress is not null
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
        mProgressDialog.setMax(2009);
        assertEquals(2009, mProgressDialog.getMax());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProgress",
            args = {}
        )
    })
    @UiThreadTest
    public void testAccessProgress() {
        // mProgress is null
        mProgressDialog.setProgress(11);
        assertEquals(11, mProgressDialog.getProgress());

        /* mProgress is not null
         * note: the progress bar's style only supports indeterminate mode,
         * so can't change progress
         */
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
        mProgressDialog.setProgress(12);
        assertEquals(0, mProgressDialog.getProgress());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSecondaryProgress",
            args = {}
        )
    })
    @UiThreadTest
    public void testAccessSecondaryProgress() {
        // mProgress is null
        mProgressDialog.setSecondaryProgress(17);
        assertEquals(17, mProgressDialog.getSecondaryProgress());

        /* mProgress is not null
         * note: the progress bar's style only supports indeterminate mode,
         * so can't change secondary progress
         */
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
        mProgressDialog.setSecondaryProgress(18);
        assertEquals(0, mProgressDialog.getSecondaryProgress());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isIndeterminate",
            args = {}
        )
    })
    @UiThreadTest
    public void testSetIndeterminate() {
        // mProgress is null
        mProgressDialog.setIndeterminate(true);
        assertTrue(mProgressDialog.isIndeterminate());
        mProgressDialog.setIndeterminate(false);
        assertFalse(mProgressDialog.isIndeterminate());

        /* mProgress is not null
         * note: the progress bar's style only supports indeterminate mode,
         * so can't change indeterminate
         */
        mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
        mProgressDialog.setIndeterminate(true);
        assertTrue(mProgressDialog.isIndeterminate());
        mProgressDialog.setIndeterminate(false);
        assertTrue(mProgressDialog.isIndeterminate());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "incrementProgressBy",
        args = {int.class}
    )
    public void testIncrementProgressBy() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
                mProgressDialog.setProgress(10);
                mProgress1 = mProgressDialog.getProgress();
                mProgressDialog.incrementProgressBy(60);
                mProgress2 = mProgressDialog.getProgress();
                mProgressDialog.cancel();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertEquals(10, mProgress1);
        assertEquals(70, mProgress2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "incrementSecondaryProgressBy",
        args = {int.class}
    )
    public void testIncrementSecondaryProgressBy() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
                mProgressDialog.setSecondaryProgress(10);
                mProgress1 = mProgressDialog.getSecondaryProgress();
                mProgressDialog.incrementSecondaryProgressBy(60);
                mProgress2 = mProgressDialog.getSecondaryProgress();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertEquals(10, mProgress1);
        assertEquals(70, mProgress2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setProgressDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testSetProgressDrawable() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
                final Window w = mProgressDialog.getWindow();
                final ProgressBar progressBar = (ProgressBar) w.findViewById(android.R.id.progress);

                mProgressDialog.setProgressDrawable(mDrawable);
                mActualDrawable = progressBar.getProgressDrawable();

                mProgressDialog.setProgressDrawable(null);
                mActualDrawableNull = progressBar.getProgressDrawable();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(mDrawable, mActualDrawable);
        assertEquals(null, mActualDrawableNull);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIndeterminateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testSetIndeterminateDrawable() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
                final Window w = mProgressDialog.getWindow();
                mProgressBar = (ProgressBar) w.findViewById(android.R.id.progress);

                mProgressDialog.setIndeterminateDrawable(mDrawable);
                mActualDrawable = mProgressBar.getIndeterminateDrawable();
                assertEquals(mDrawable, mActualDrawable);

                mProgressDialog.setIndeterminateDrawable(null);
                mActualDrawableNull = mProgressBar.getIndeterminateDrawable();
                assertEquals(null, mActualDrawableNull);
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setMessage",
        args = {java.lang.CharSequence.class},
        notes= "no way to verify dialog contents"
    )
    public void testSetMessage() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                // mProgress is null
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage(MESSAGE);
                mProgressDialog.show();
            }
        });
        mInstrumentation.waitForIdleSync();

        runTestOnUiThread(new Runnable() {
            public void run() {
                // mProgress is not null
                mProgressDialog = ProgressDialog.show(mContext, TITLE, MESSAGE);
                mProgressDialog.setMessage("Bruce Li");
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setProgressStyle",
        args = {int.class},
        notes="no way to verify dialog conteents"
    )
    public void testSetProgressStyle() throws Throwable {
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setProgressStyle(100);
    }

    private void setProgressStyle(final int style) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(style);

                mProgressDialog.show();
                mProgressDialog.setProgress(10);
                mProgressDialog.setMax(100);
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    private static class MockProgressDialog extends ProgressDialog {
        public boolean mIsOnStopCalled;
        public boolean mIsOnStartCalled;
        public boolean mIsOnCreateCalled;

        public MockProgressDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mIsOnCreateCalled = true;
        }

        @Override
        public void onStart(){
            super.onStart();
            mIsOnStartCalled = true;
        }

        @Override
        public void onStop() {
            super.onStop();
            mIsOnStopCalled = true;
        }
    }
}
