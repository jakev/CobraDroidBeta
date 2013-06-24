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

package android.view.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.cts.MockActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Test {@link ViewTreeObserver}.
 */
@TestTargetClass(ViewTreeObserver.class)
public class ViewTreeObserverTest extends ActivityInstrumentationTestCase2<MockActivity> {
    ViewTreeObserver mViewTreeObserver;

    private Activity mActivity;
    private Instrumentation mInstrumentation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mViewTreeObserver = null;
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        layout(R.layout.viewtreeobserver_layout);
    }

    public ViewTreeObserverTest() {
        super("com.android.cts.stub", MockActivity.class);
    }

    private void layout(final int layoutId) {
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                mActivity.setContentView(layoutId);
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener)",
        method = "addOnGlobalFocusChangeListener",
        args = {android.view.ViewTreeObserver.OnGlobalFocusChangeListener.class}
    )
    public void testAddOnGlobalFocusChangeListener() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        final ListView lv1 = (ListView) mActivity.findViewById(R.id.listview1);
        final ListView lv2 = (ListView) mActivity.findViewById(R.id.listview2);

        mViewTreeObserver = layout.getViewTreeObserver();
        MockOnGlobalFocusChangeListener listener = new MockOnGlobalFocusChangeListener();
        mViewTreeObserver.addOnGlobalFocusChangeListener(listener);
        assertFalse(listener.hasCalledOnGlobalFocusChanged());

        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                layout.requestChildFocus(lv1, lv2);
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(listener.hasCalledOnGlobalFocusChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addOnGlobalLayoutListener(OnGlobalLayoutListener)",
        method = "addOnGlobalLayoutListener",
        args = {android.view.ViewTreeObserver.OnGlobalLayoutListener.class}
    )
    public void testAddOnGlobalLayoutListener() {
        final LinearLayout layout =
            (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnGlobalLayoutListener listener = new MockOnGlobalLayoutListener();
        assertFalse(listener.hasCalledOnGlobalLayout());
        mViewTreeObserver.addOnGlobalLayoutListener(listener);
        mViewTreeObserver.dispatchOnGlobalLayout();
        assertTrue(listener.hasCalledOnGlobalLayout());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addOnPreDrawListener(OnPreDrawListener)",
        method = "addOnPreDrawListener",
        args = {android.view.ViewTreeObserver.OnPreDrawListener.class}
    )
    public void testAddOnPreDrawListener() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnPreDrawListener listener = new MockOnPreDrawListener();
        assertFalse(listener.hasCalledOnPreDraw());
        mViewTreeObserver.addOnPreDrawListener(listener);
        mViewTreeObserver.dispatchOnPreDraw();
        assertTrue(listener.hasCalledOnPreDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addOnTouchModeChangeListener(OnTouchModeChangeListener)",
        method = "addOnTouchModeChangeListener",
        args = {android.view.ViewTreeObserver.OnTouchModeChangeListener.class}
    )
    public void testAddOnTouchModeChangeListener() {
        final Button b = (Button) mActivity.findViewById(R.id.button1);
        // let the button be touch mode.
        TouchUtils.tapView(this, b);

        mViewTreeObserver = b.getViewTreeObserver();

        MockOnTouchModeChangeListener listener = new MockOnTouchModeChangeListener();
        assertFalse(listener.hasCalledOnTouchModeChanged());
        mViewTreeObserver.addOnTouchModeChangeListener(listener);

        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                b.requestFocusFromTouch();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(listener.hasCalledOnTouchModeChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener)",
        method = "addOnComputeInternalInsetsListener",
        args = {android.view.ViewTreeObserver.OnComputeInternalInsetsListener.class}
    )
    public void testAddOnComputeInternalInsetsListener() {
        final ListView lv1 = (ListView) mActivity.findViewById(R.id.listview1);
        mViewTreeObserver = lv1.getViewTreeObserver();

        MockOnComputeInternalInsetsListener listener = new MockOnComputeInternalInsetsListener();
        mViewTreeObserver.addOnComputeInternalInsetsListener(listener);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener)",
        method = "removeOnComputeInternalInsetsListener",
        args = {android.view.ViewTreeObserver.OnComputeInternalInsetsListener.class}
    )
    public void testRemoveOnComputeInternalInsetsListener() {
        final ListView lv1 = (ListView) mActivity.findViewById(R.id.listview1);
        mViewTreeObserver = lv1.getViewTreeObserver();

        MockOnComputeInternalInsetsListener listener = new MockOnComputeInternalInsetsListener();
        mViewTreeObserver.removeOnComputeInternalInsetsListener(listener);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test dispatchOnGlobalLayout()",
        method = "dispatchOnGlobalLayout",
        args = {}
    )
    public void testDispatchOnGlobalLayout() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnGlobalLayoutListener listener = new MockOnGlobalLayoutListener();
        assertFalse(listener.hasCalledOnGlobalLayout());
        mViewTreeObserver.addOnGlobalLayoutListener(listener);
        mViewTreeObserver.dispatchOnGlobalLayout();
        assertTrue(listener.hasCalledOnGlobalLayout());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test dispatchOnPreDraw()",
        method = "dispatchOnPreDraw",
        args = {}
    )
    public void testDispatchOnPreDraw() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnPreDrawListener listener = new MockOnPreDrawListener();
        assertFalse(listener.hasCalledOnPreDraw());
        mViewTreeObserver.addOnPreDrawListener(listener);
        mViewTreeObserver.dispatchOnPreDraw();
        assertTrue(listener.hasCalledOnPreDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isAlive()",
        method = "isAlive",
        args = {}
    )
    public void testIsAlive() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);

        mViewTreeObserver = layout.getViewTreeObserver();
        assertTrue(mViewTreeObserver.isAlive());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test removeGlobalOnLayoutListener(OnGlobalLayoutListener)",
        method = "removeGlobalOnLayoutListener",
        args = {android.view.ViewTreeObserver.OnGlobalLayoutListener.class}
    )
    public void testRemoveGlobalOnLayoutListener() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnGlobalLayoutListener listener = new MockOnGlobalLayoutListener();
        assertFalse(listener.hasCalledOnGlobalLayout());
        mViewTreeObserver.addOnGlobalLayoutListener(listener);
        mViewTreeObserver.dispatchOnGlobalLayout();
        assertTrue(listener.hasCalledOnGlobalLayout());

        listener.reset();
        assertFalse(listener.hasCalledOnGlobalLayout());
        mViewTreeObserver.removeGlobalOnLayoutListener(listener);
        mViewTreeObserver.dispatchOnGlobalLayout();
        assertFalse(listener.hasCalledOnGlobalLayout());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener)",
        method = "removeOnGlobalFocusChangeListener",
        args = {android.view.ViewTreeObserver.OnGlobalFocusChangeListener.class}
    )
    public void testRemoveOnGlobalFocusChangeListener() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        final ListView lv1 = (ListView) mActivity.findViewById(R.id.listview1);
        final ListView lv2 = (ListView) mActivity.findViewById(R.id.listview2);

        mViewTreeObserver = layout.getViewTreeObserver();
        MockOnGlobalFocusChangeListener listener = new MockOnGlobalFocusChangeListener();
        mViewTreeObserver.addOnGlobalFocusChangeListener(listener);
        assertFalse(listener.hasCalledOnGlobalFocusChanged());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                layout.requestChildFocus(lv1, lv2);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.hasCalledOnGlobalFocusChanged());

        listener.reset();
        mViewTreeObserver.removeOnGlobalFocusChangeListener(listener);
        assertFalse(listener.hasCalledOnGlobalFocusChanged());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                layout.requestChildFocus(lv1, lv2);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertFalse(listener.hasCalledOnGlobalFocusChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test removeOnPreDrawListener(OnPreDrawListener)",
        method = "removeOnPreDrawListener",
        args = {android.view.ViewTreeObserver.OnPreDrawListener.class}
    )
    public void testRemoveOnPreDrawListener() {
        final LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.linearlayout);
        mViewTreeObserver = layout.getViewTreeObserver();

        MockOnPreDrawListener listener = new MockOnPreDrawListener();
        assertFalse(listener.hasCalledOnPreDraw());
        mViewTreeObserver.addOnPreDrawListener(listener);
        mViewTreeObserver.dispatchOnPreDraw();
        assertTrue(listener.hasCalledOnPreDraw());

        listener.reset();
        assertFalse(listener.hasCalledOnPreDraw());
        mViewTreeObserver.removeOnPreDrawListener(listener);
        mViewTreeObserver.dispatchOnPreDraw();
        assertFalse(listener.hasCalledOnPreDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test removeOnTouchModeChangeListener(OnTouchModeChangeListener)",
        method = "removeOnTouchModeChangeListener",
        args = {android.view.ViewTreeObserver.OnTouchModeChangeListener.class}
    )
    public void testRemoveOnTouchModeChangeListener() {
        final Button b = (Button) mActivity.findViewById(R.id.button1);
        // let the button be touch mode.
        TouchUtils.tapView(this, b);

        mViewTreeObserver = b.getViewTreeObserver();

        MockOnTouchModeChangeListener listener = new MockOnTouchModeChangeListener();
        mViewTreeObserver.addOnTouchModeChangeListener(listener);
        assertFalse(listener.hasCalledOnTouchModeChanged());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                b.requestFocusFromTouch();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(listener.hasCalledOnTouchModeChanged());

        listener = new MockOnTouchModeChangeListener();
        assertFalse(listener.hasCalledOnTouchModeChanged());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                b.requestFocusFromTouch();
            }
        });
        mInstrumentation.waitForIdleSync();

        assertFalse(listener.hasCalledOnTouchModeChanged());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addOnScrollChangedListener",
            args = {android.view.ViewTreeObserver.OnScrollChangedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeOnScrollChangedListener",
            args = {android.view.ViewTreeObserver.OnScrollChangedListener.class}
        )
    })
    public void testAccessOnScrollChangedListener() throws Throwable {
        layout(R.layout.scrollview_layout);
        final ScrollView scrollView = (ScrollView) mActivity.findViewById(R.id.scroll_view);

        mViewTreeObserver = scrollView.getViewTreeObserver();

        MockOnScrollChangedListener listener = new MockOnScrollChangedListener();
        assertFalse(listener.hasCalledOnScrollChanged());
        mViewTreeObserver.addOnScrollChangedListener(listener);

        runTestOnUiThread(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.hasCalledOnScrollChanged());

        listener.reset();
        assertFalse(listener.hasCalledOnScrollChanged());

        mViewTreeObserver.removeOnScrollChangedListener(listener);
        runTestOnUiThread(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
        assertFalse(listener.hasCalledOnScrollChanged());
    }

    private class MockOnGlobalFocusChangeListener implements OnGlobalFocusChangeListener {
        private boolean mCalledOnGlobalFocusChanged = false;

        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            mCalledOnGlobalFocusChanged = true;
        }

        public boolean hasCalledOnGlobalFocusChanged() {
            return mCalledOnGlobalFocusChanged;
        }

        public void reset() {
            mCalledOnGlobalFocusChanged = false;
        }
    }

    private class MockOnGlobalLayoutListener implements OnGlobalLayoutListener {
        private boolean mCalledOnGlobalLayout = false;

        public void onGlobalLayout() {
            mCalledOnGlobalLayout = true;
        }

        public boolean hasCalledOnGlobalLayout() {
            return mCalledOnGlobalLayout;
        }

        public void reset() {
            mCalledOnGlobalLayout = false;
        }
    }

    private class MockOnPreDrawListener implements OnPreDrawListener {
        private boolean mCalledOnPreDraw = false;

        public boolean onPreDraw() {
            mCalledOnPreDraw = true;
            return true;
        }

        public boolean hasCalledOnPreDraw() {
            return mCalledOnPreDraw;
        }

        public void reset() {
            mCalledOnPreDraw = false;
        }
    }

    private class MockOnTouchModeChangeListener implements OnTouchModeChangeListener {
        private boolean mCalledOnTouchModeChanged = false;

        public void onTouchModeChanged(boolean isInTouchMode) {
            mCalledOnTouchModeChanged = true;
        }

        public boolean hasCalledOnTouchModeChanged() {
            return mCalledOnTouchModeChanged;
        }
    }

    private class MockOnComputeInternalInsetsListener implements OnComputeInternalInsetsListener {
        private boolean mCalledOnComputeInternalInsets = false;

        public void onComputeInternalInsets(InternalInsetsInfo inoutInfo) {
            mCalledOnComputeInternalInsets = true;
        }

        public boolean hasCalledOnComputeInternalInsets() {
            return mCalledOnComputeInternalInsets;
        }
    }

    private static class MockOnScrollChangedListener implements OnScrollChangedListener {
        private boolean mCalledOnScrollChanged = false;

        public boolean hasCalledOnScrollChanged() {
            return mCalledOnScrollChanged;
        }

        public void onScrollChanged() {
            mCalledOnScrollChanged = true;
        }

        public void reset() {
            mCalledOnScrollChanged = false;
        }
    }
}
