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
import com.android.internal.view.menu.ContextMenuBuilder;
import com.google.android.collect.Lists;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcelable;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManagerImpl;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.cts.DelayedCheck;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.cts.StubActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test {@link View}.
 */
@TestTargetClass(View.class)
public class ViewTest extends ActivityInstrumentationTestCase2<ViewTestStubActivity> {
    public ViewTest() {
        super(ViewTestStubActivity.class);
    }

    private Resources mResources;
    private MockViewParent mMockParent;
    private Activity mActivity;

    /** timeout delta when wait in case the system is sluggish */
    private static final long TIMEOUT_DELTA = 1000;

    private static final String LOG_TAG = "ViewTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mResources = mActivity.getResources();
        mMockParent = new MockViewParent(mActivity);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "View",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "View",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "View",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new View(mActivity);

        final XmlResourceParser parser = mResources.getLayout(R.layout.view_layout);
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        new View(mActivity, attrs);

        new View(mActivity, null);

        try {
            new View(null, attrs);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        new View(mActivity, attrs, 0);

        new View(mActivity, null, 1);

        try {
            new View(null, null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContext",
        args = {}
    )
    public void testGetContext() {
        View view = new View(mActivity);
        assertSame(mActivity, view.getContext());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getResources",
        args = {}
    )
    public void testGetResources() {
        View view = new View(mActivity);
        assertSame(mResources, view.getResources());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAnimation",
        args = {}
    )
    public void testGetAnimation() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        View view = new View(mActivity);
        assertNull(view.getAnimation());

        view.setAnimation(animation);
        assertSame(animation, view.getAnimation());

        view.clearAnimation();
        assertNull(view.getAnimation());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAnimation",
        args = {android.view.animation.Animation.class}
    )
    public void testSetAnimation() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        View view = new View(mActivity);
        assertNull(view.getAnimation());

        animation.initialize(100, 100, 100, 100);
        assertTrue(animation.isInitialized());
        view.setAnimation(animation);
        assertSame(animation, view.getAnimation());
        assertFalse(animation.isInitialized());

        view.setAnimation(null);
        assertNull(view.getAnimation());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearAnimation",
        args = {}
    )
    public void testClearAnimation() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        View view = new View(mActivity);

        assertNull(view.getAnimation());
        view.clearAnimation();
        assertNull(view.getAnimation());

        view.setAnimation(animation);
        assertNotNull(view.getAnimation());
        view.clearAnimation();
        assertNull(view.getAnimation());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startAnimation",
        args = {android.view.animation.Animation.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for startAnimation() is incomplete." +
            "1. not clear what is supposed to happen if animation is null.")
    public void testStartAnimation() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        View view = new View(mActivity);

        try {
            view.startAnimation(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        animation.setStartTime(1L);
        assertEquals(1L, animation.getStartTime());
        view.startAnimation(animation);
        assertEquals(Animation.START_ON_FIRST_FRAME, animation.getStartTime());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onAnimationStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onAnimationEnd",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSetAlpha",
            args = {int.class}
        )
    })
    public void testOnAnimation() throws Throwable {
        final Animation animation = new AlphaAnimation(0.0f, 1.0f);
        long duration = 2000L;
        animation.setDuration(duration);
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        // check whether it has started
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.startAnimation(animation);
            }
        });
        getInstrumentation().waitForIdleSync();

        assertTrue(view.hasCalledOnAnimationStart());

        // check whether it has ended after duration, and alpha changed during this time.
        new DelayedCheck(duration + TIMEOUT_DELTA) {
            @Override
            protected boolean check() {
                return view.hasCalledOnSetAlpha() && view.hasCalledOnAnimationEnd();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getParent",
        args = {}
    )
    public void testGetParent() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        ViewGroup parent = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        assertSame(parent, view.getParent());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findViewById",
        args = {int.class}
    )
    public void testFindViewById() {
        View parent = mActivity.findViewById(R.id.viewlayout_root);
        assertSame(parent, parent.findViewById(R.id.viewlayout_root));

        View view = parent.findViewById(R.id.mock_view);
        assertTrue(view instanceof MockView);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTouchDelegate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTouchDelegate",
            args = {android.view.TouchDelegate.class}
        )
    })
    public void testAccessTouchDelegate() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        Rect rect = new Rect();
        final Button button = new Button(mActivity);
        final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.addContentView(button,
                        new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            }
        });
        getInstrumentation().waitForIdleSync();
        button.getHitRect(rect);
        MockTouchDelegate delegate = new MockTouchDelegate(rect, button);

        assertNull(view.getTouchDelegate());

        view.setTouchDelegate(delegate);
        assertSame(delegate, view.getTouchDelegate());
        assertFalse(delegate.hasCalledOnTouchEvent());
        TouchUtils.clickView(this, view);
        assertTrue(view.hasCalledOnTouchEvent());
        assertTrue(delegate.hasCalledOnTouchEvent());

        view.setTouchDelegate(null);
        assertNull(view.getTouchDelegate());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTag",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTag",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewWithTag",
            args = {java.lang.Object.class}
        )
    })
    @UiThreadTest
    public void testAccessTag() {
        ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        MockView mockView = (MockView) mActivity.findViewById(R.id.mock_view);
        MockView scrollView = (MockView) mActivity.findViewById(R.id.scroll_view);

        ViewData viewData = new ViewData();
        viewData.childCount = 3;
        viewData.tag = "linearLayout";
        viewData.firstChild = mockView;
        viewGroup.setTag(viewData);
        viewGroup.setFocusable(true);
        assertSame(viewData, viewGroup.getTag());

        final String tag = "mock";
        assertNull(mockView.getTag());
        mockView.setTag(tag);
        assertEquals(tag, mockView.getTag());

        scrollView.setTag(viewGroup);
        assertSame(viewGroup, scrollView.getTag());

        assertSame(viewGroup, viewGroup.findViewWithTag(viewData));
        assertSame(mockView, viewGroup.findViewWithTag(tag));
        assertSame(scrollView, viewGroup.findViewWithTag(viewGroup));

        mockView.setTag(null);
        assertNull(mockView.getTag());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onSizeChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testOnSizeChanged() throws Throwable {
        final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        final MockView mockView = new MockView(mActivity);
        assertEquals(-1, mockView.getOldWOnSizeChanged());
        assertEquals(-1, mockView.getOldHOnSizeChanged());
        runTestOnUiThread(new Runnable() {
            public void run() {
                viewGroup.addView(mockView);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.hasCalledOnSizeChanged());
        assertEquals(0, mockView.getOldWOnSizeChanged());
        assertEquals(0, mockView.getOldHOnSizeChanged());

        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnSizeChanged());
        view.reset();
        assertEquals(-1, view.getOldWOnSizeChanged());
        assertEquals(-1, view.getOldHOnSizeChanged());
        int oldw = view.getWidth();
        int oldh = view.getHeight();
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 100);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnSizeChanged());
        assertEquals(oldw, view.getOldWOnSizeChanged());
        assertEquals(oldh, view.getOldHOnSizeChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHitRect",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getHitRect() is incomplete." +
            "1. not clear what is supposed to happen if outRect is null.")
    public void testGetHitRect() {
        MockView view = new MockView(mActivity);
        Rect outRect = new Rect();

        try {
            view.getHitRect(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        View mockView = mActivity.findViewById(R.id.mock_view);
        mockView.getHitRect(outRect);
        assertEquals(0, outRect.left);
        assertEquals(0, outRect.top);
        assertEquals(mockView.getWidth(), outRect.right);
        assertEquals(mockView.getHeight(), outRect.bottom);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "forceLayout",
        args = {}
    )
    public void testForceLayout() {
        View view = new View(mActivity);

        assertFalse(view.isLayoutRequested());
        view.forceLayout();
        assertTrue(view.isLayoutRequested());

        view.forceLayout();
        assertTrue(view.isLayoutRequested());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isLayoutRequested",
        args = {}
    )
    public void testIsLayoutRequested() {
        View view = new View(mActivity);

        assertFalse(view.isLayoutRequested());
        view.forceLayout();
        assertTrue(view.isLayoutRequested());

        view.layout(0, 0, 0, 0);
        assertFalse(view.isLayoutRequested());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestLayout",
        args = {}
    )
    public void testRequestLayout() {
        MockView view = new MockView(mActivity);
        assertFalse(view.isLayoutRequested());
        assertNull(view.getParent());

        view.requestLayout();
        assertTrue(view.isLayoutRequested());

        view.setParent(mMockParent);
        assertFalse(mMockParent.hasRequestLayout());
        view.requestLayout();
        assertTrue(view.isLayoutRequested());
        assertTrue(mMockParent.hasRequestLayout());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "layout",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLayout",
            args = {boolean.class, int.class, int.class, int.class, int.class}
        )
    })
    public void testLayout() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnLayout());

        view.reset();
        assertFalse(view.hasCalledOnLayout());
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.requestLayout();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnLayout());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBaseline",
        args = {}
    )
    public void testGetBaseline() {
        View view = new View(mActivity);

        assertEquals(-1, view.getBaseline());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBackground",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBackgroundDrawable",
            args = {android.graphics.drawable.Drawable.class}
        )
    })
    public void testAccessBackground() {
        View view = new View(mActivity);
        Drawable d1 = mResources.getDrawable(R.drawable.scenery);
        Drawable d2 = mResources.getDrawable(R.drawable.pass);

        assertNull(view.getBackground());

        view.setBackgroundDrawable(d1);
        assertEquals(d1, view.getBackground());

        view.setBackgroundDrawable(d2);
        assertEquals(d2, view.getBackground());

        view.setBackgroundDrawable(null);
        assertNull(view.getBackground());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setBackgroundResource",
        args = {int.class}
    )
    public void testSetBackgroundResource() {
        View view = new View(mActivity);

        assertNull(view.getBackground());

        view.setBackgroundResource(R.drawable.pass);
        assertNotNull(view.getBackground());

        view.setBackgroundResource(0);
        assertNull(view.getBackground());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawingCacheBackgroundColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheBackgroundColor",
            args = {int.class}
        )
    })
    public void testAccessDrawingCacheBackgroundColor() {
        View view = new View(mActivity);

        assertEquals(0, view.getDrawingCacheBackgroundColor());

        view.setDrawingCacheBackgroundColor(0xFF00FF00);
        assertEquals(0xFF00FF00, view.getDrawingCacheBackgroundColor());

        view.setDrawingCacheBackgroundColor(-1);
        assertEquals(-1, view.getDrawingCacheBackgroundColor());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setBackgroundColor",
        args = {int.class}
    )
    public void testSetBackgroundColor() {
        View view = new View(mActivity);
        ColorDrawable colorDrawable;
        assertNull(view.getBackground());

        view.setBackgroundColor(0xFFFF0000);
        colorDrawable = (ColorDrawable) view.getBackground();
        assertNotNull(colorDrawable);
        assertEquals(0xFF, colorDrawable.getAlpha());

        view.setBackgroundColor(0);
        colorDrawable = (ColorDrawable) view.getBackground();
        assertNotNull(colorDrawable);
        assertEquals(0, colorDrawable.getAlpha());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "verifyDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testVerifyDrawable() {
        MockView view = new MockView(mActivity);
        Drawable d1 = mResources.getDrawable(R.drawable.scenery);
        Drawable d2 = mResources.getDrawable(R.drawable.pass);

        assertNull(view.getBackground());
        assertTrue(view.verifyDrawable(null));
        assertFalse(view.verifyDrawable(d1));

        view.setBackgroundDrawable(d1);
        assertTrue(view.verifyDrawable(d1));
        assertFalse(view.verifyDrawable(d2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDrawingRect",
        args = {android.graphics.Rect.class}
    )
    public void testGetDrawingRect() {
        MockView view = new MockView(mActivity);
        Rect outRect = new Rect();

        view.getDrawingRect(outRect);
        assertEquals(0, outRect.left);
        assertEquals(0, outRect.top);
        assertEquals(0, outRect.right);
        assertEquals(0, outRect.bottom);

        view.scrollTo(10, 100);
        view.getDrawingRect(outRect);
        assertEquals(10, outRect.left);
        assertEquals(100, outRect.top);
        assertEquals(10, outRect.right);
        assertEquals(100, outRect.bottom);

        View mockView = mActivity.findViewById(R.id.mock_view);
        mockView.getDrawingRect(outRect);
        assertEquals(0, outRect.left);
        assertEquals(0, outRect.top);
        assertEquals(mockView.getWidth(), outRect.right);
        assertEquals(mockView.getHeight(), outRect.bottom);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFocusedRect",
        args = {android.graphics.Rect.class}
    )
    public void testGetFocusedRect() {
        MockView view = new MockView(mActivity);
        Rect outRect = new Rect();

        view.getFocusedRect(outRect);
        assertEquals(0, outRect.left);
        assertEquals(0, outRect.top);
        assertEquals(0, outRect.right);
        assertEquals(0, outRect.bottom);

        view.scrollTo(10, 100);
        view.getFocusedRect(outRect);
        assertEquals(10, outRect.left);
        assertEquals(100, outRect.top);
        assertEquals(10, outRect.right);
        assertEquals(100, outRect.bottom);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getGlobalVisibleRect",
        args = {android.graphics.Rect.class, android.graphics.Point.class}
    )
    public void testGetGlobalVisibleRectPoint() throws Throwable {
        final View view = mActivity.findViewById(R.id.mock_view);
        final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        Rect rect = new Rect();
        Point point = new Point();

        assertTrue(view.getGlobalVisibleRect(rect, point));
        Rect rcParent = new Rect();
        Point ptParent = new Point();
        viewGroup.getGlobalVisibleRect(rcParent, ptParent);
        assertEquals(rcParent.left, rect.left);
        assertEquals(rcParent.top, rect.top);
        assertEquals(rect.left + view.getWidth(), rect.right);
        assertEquals(rect.top + view.getHeight(), rect.bottom);
        assertEquals(ptParent.x, point.x);
        assertEquals(ptParent.y, point.y);

        // width is 0
        final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(0, 300);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams1);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getGlobalVisibleRect(rect, point));

        // height is -10
        final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(200, -10);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams2);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getGlobalVisibleRect(rect, point));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int halfWidth = display.getWidth() / 2;
        int halfHeight = display.getHeight() /2;

        final LinearLayout.LayoutParams layoutParams3 =
                new LinearLayout.LayoutParams(halfWidth, halfHeight);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams3);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.getGlobalVisibleRect(rect, point));
        assertEquals(rcParent.left, rect.left);
        assertEquals(rcParent.top, rect.top);
        assertEquals(rect.left + halfWidth, rect.right);
        assertEquals(rect.top + halfHeight, rect.bottom);
        assertEquals(ptParent.x, point.x);
        assertEquals(ptParent.y, point.y);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getGlobalVisibleRect",
        args = {android.graphics.Rect.class}
    )
    public void testGetGlobalVisibleRect() throws Throwable {
        final View view = mActivity.findViewById(R.id.mock_view);
        final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        Rect rect = new Rect();

        assertTrue(view.getGlobalVisibleRect(rect));
        Rect rcParent = new Rect();
        viewGroup.getGlobalVisibleRect(rcParent);
        assertEquals(rcParent.left, rect.left);
        assertEquals(rcParent.top, rect.top);
        assertEquals(rect.left + view.getWidth(), rect.right);
        assertEquals(rect.top + view.getHeight(), rect.bottom);

        // width is 0
        final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(0, 300);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams1);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getGlobalVisibleRect(rect));

        // height is -10
        final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(200, -10);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams2);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getGlobalVisibleRect(rect));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int halfWidth = display.getWidth() / 2;
        int halfHeight = display.getHeight() /2;

        final LinearLayout.LayoutParams layoutParams3 =
                new LinearLayout.LayoutParams(halfWidth, halfHeight);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams3);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.getGlobalVisibleRect(rect));
        assertEquals(rcParent.left, rect.left);
        assertEquals(rcParent.top, rect.top);
        assertEquals(rect.left + halfWidth, rect.right);
        assertEquals(rect.top + halfHeight, rect.bottom);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeHorizontalScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeHorizontalScrollRange",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeHorizontalScrollExtent",
            args = {}
        )
    })
    public void testComputeHorizontalScroll() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        assertEquals(0, view.computeHorizontalScrollOffset());
        assertEquals(view.getWidth(), view.computeHorizontalScrollRange());
        assertEquals(view.getWidth(), view.computeHorizontalScrollExtent());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollTo(12, 0);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(12, view.computeHorizontalScrollOffset());
        assertEquals(view.getWidth(), view.computeHorizontalScrollRange());
        assertEquals(view.getWidth(), view.computeHorizontalScrollExtent());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollBy(12, 0);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(24, view.computeHorizontalScrollOffset());
        assertEquals(view.getWidth(), view.computeHorizontalScrollRange());
        assertEquals(view.getWidth(), view.computeHorizontalScrollExtent());

        int newWidth = 200;
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(newWidth, 100);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(24, view.computeHorizontalScrollOffset());
        assertEquals(newWidth, view.getWidth());
        assertEquals(view.getWidth(), view.computeHorizontalScrollRange());
        assertEquals(view.getWidth(), view.computeHorizontalScrollExtent());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollRange",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollExtent",
            args = {}
        )
    })
    public void testComputeVerticalScroll() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        assertEquals(0, view.computeVerticalScrollOffset());
        assertEquals(view.getHeight(), view.computeVerticalScrollRange());
        assertEquals(view.getHeight(), view.computeVerticalScrollExtent());

        final int scrollToY = 34;
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollTo(0, scrollToY);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(scrollToY, view.computeVerticalScrollOffset());
        assertEquals(view.getHeight(), view.computeVerticalScrollRange());
        assertEquals(view.getHeight(), view.computeVerticalScrollExtent());

        final int scrollByY = 200;
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollBy(0, scrollByY);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(scrollToY + scrollByY, view.computeVerticalScrollOffset());
        assertEquals(view.getHeight(), view.computeVerticalScrollRange());
        assertEquals(view.getHeight(), view.computeVerticalScrollExtent());

        int newHeight = 333;
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, newHeight);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(scrollToY + scrollByY, view.computeVerticalScrollOffset());
        assertEquals(newHeight, view.getHeight());
        assertEquals(view.getHeight(), view.computeVerticalScrollRange());
        assertEquals(view.getHeight(), view.computeVerticalScrollExtent());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLeftFadingEdgeStrength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRightFadingEdgeStrength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTopFadingEdgeStrength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBottomFadingEdgeStrength",
            args = {}
        )
    })
    public void testGetFadingEdgeStrength() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        assertEquals(0f, view.getLeftFadingEdgeStrength());
        assertEquals(0f, view.getRightFadingEdgeStrength());
        assertEquals(0f, view.getTopFadingEdgeStrength());
        assertEquals(0f, view.getBottomFadingEdgeStrength());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollTo(10, 10);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(1f, view.getLeftFadingEdgeStrength());
        assertEquals(0f, view.getRightFadingEdgeStrength());
        assertEquals(1f, view.getTopFadingEdgeStrength());
        assertEquals(0f, view.getBottomFadingEdgeStrength());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.scrollTo(-10, -10);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(0f, view.getLeftFadingEdgeStrength());
        assertEquals(1f, view.getRightFadingEdgeStrength());
        assertEquals(0f, view.getTopFadingEdgeStrength());
        assertEquals(1f, view.getBottomFadingEdgeStrength());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLeftFadingEdgeStrength",
        args = {}
    )
    public void testGetLeftFadingEdgeStrength() {
        MockView view = new MockView(mActivity);

        assertEquals(0.0f, view.getLeftFadingEdgeStrength());

        view.scrollTo(1, 0);
        assertEquals(1.0f, view.getLeftFadingEdgeStrength());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRightFadingEdgeStrength",
        args = {}
    )
    public void testGetRightFadingEdgeStrength() {
        MockView view = new MockView(mActivity);

        assertEquals(0.0f, view.getRightFadingEdgeStrength());

        view.scrollTo(-1, 0);
        assertEquals(1.0f, view.getRightFadingEdgeStrength());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBottomFadingEdgeStrength",
        args = {}
    )
    public void testGetBottomFadingEdgeStrength() {
        MockView view = new MockView(mActivity);

        assertEquals(0.0f, view.getBottomFadingEdgeStrength());

        view.scrollTo(0, -2);
        assertEquals(1.0f, view.getBottomFadingEdgeStrength());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTopFadingEdgeStrength",
        args = {}
    )
    public void testGetTopFadingEdgeStrength() {
        MockView view = new MockView(mActivity);

        assertEquals(0.0f, view.getTopFadingEdgeStrength());

        view.scrollTo(0, 2);
        assertEquals(1.0f, view.getTopFadingEdgeStrength());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveSize",
        args = {int.class, int.class}
    )
    public void testResolveSize() {
        assertEquals(50, View.resolveSize(50, View.MeasureSpec.UNSPECIFIED));

        assertEquals(40, View.resolveSize(50, 40 | View.MeasureSpec.EXACTLY));

        assertEquals(30, View.resolveSize(50, 30 | View.MeasureSpec.AT_MOST));

        assertEquals(20, View.resolveSize(20, 30 | View.MeasureSpec.AT_MOST));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDefaultSize",
        args = {int.class, int.class}
    )
    public void testGetDefaultSize() {
        assertEquals(50, View.getDefaultSize(50, View.MeasureSpec.UNSPECIFIED));

        assertEquals(40, View.getDefaultSize(50, 40 | View.MeasureSpec.EXACTLY));

        assertEquals(30, View.getDefaultSize(50, 30 | View.MeasureSpec.AT_MOST));

        assertEquals(30, View.getDefaultSize(20, 30 | View.MeasureSpec.AT_MOST));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setId",
            args = {int.class}
        )
    })
    public void testAccessId() {
        View view = new View(mActivity);

        assertEquals(View.NO_ID, view.getId());

        view.setId(10);
        assertEquals(10, view.getId());

        view.setId(0xFFFFFFFF);
        assertEquals(0xFFFFFFFF, view.getId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLongClickable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isLongClickable",
            args = {}
        )
    })
    public void testAccessLongClickable() {
        View view = new View(mActivity);

        assertFalse(view.isLongClickable());

        view.setLongClickable(true);
        assertTrue(view.isLongClickable());

        view.setLongClickable(false);
        assertFalse(view.isLongClickable());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setClickable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isClickable",
            args = {}
        )
    })
    public void testAccessClickable() {
        View view = new View(mActivity);

        assertFalse(view.isClickable());

        view.setClickable(true);
        assertTrue(view.isClickable());

        view.setClickable(false);
        assertFalse(view.isClickable());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContextMenuInfo",
        args = {}
    )
    public void testGetContextMenuInfo() {
        MockView view = new MockView(mActivity);

        assertNull(view.getContextMenuInfo());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnCreateContextMenuListener",
        args = {android.view.View.OnCreateContextMenuListener.class}
    )
    public void testSetOnCreateContextMenuListener() {
        View view = new View(mActivity);
        assertFalse(view.isLongClickable());

        view.setOnCreateContextMenuListener(null);
        assertTrue(view.isLongClickable());

        view.setOnCreateContextMenuListener(new OnCreateContextMenuListenerImpl());
        assertTrue(view.isLongClickable());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "parameter ContextMenu must be a MenuBuilder or its subclass instance, " +
                    "but there is no document to indicate it. So there is a potential" +
                    " ClassCastException",
            method = "createContextMenu",
            args = {android.view.ContextMenu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {android.view.ContextMenu.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for createContextMenu() is incomplete." +
            "1. not clear what is supposed to happen if menu is null.")
    public void testCreateContextMenu() {
        OnCreateContextMenuListenerImpl listener = new OnCreateContextMenuListenerImpl();
        MockView view = new MockView(mActivity);
        ContextMenu contextMenu = new ContextMenuBuilder(mActivity);
        view.setParent(mMockParent);
        view.setOnCreateContextMenuListener(listener);
        assertFalse(view.hasCalledOnCreateContextMenu());
        assertFalse(mMockParent.hasCreateContextMenu());
        assertFalse(listener.hasOnCreateContextMenu());

        view.createContextMenu(contextMenu);
        assertTrue(view.hasCalledOnCreateContextMenu());
        assertTrue(mMockParent.hasCreateContextMenu());
        assertTrue(listener.hasOnCreateContextMenu());

        try {
            view.createContextMenu(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addFocusables",
        args = {java.util.ArrayList.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addFocusables() is incomplete." +
            "1. not clear what is supposed to happen if the input ArrayList<View> is null.")
    public void testAddFocusables() {
        View view = new View(mActivity);
        ArrayList<View> viewList = new ArrayList<View>();

        // view is not focusable
        assertFalse(view.isFocusable());
        assertEquals(0, viewList.size());
        view.addFocusables(viewList, 0);
        assertEquals(0, viewList.size());

        // view is focusable
        view.setFocusable(true);
        view.addFocusables(viewList, 0);
        assertEquals(1, viewList.size());
        assertEquals(view, viewList.get(0));

        // null array should be ignored
        view.addFocusables(null, 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFocusables",
        args = {int.class}
    )
    public void testGetFocusables() {
        View view = new View(mActivity);
        ArrayList<View> viewList;

        // view is not focusable
        assertFalse(view.isFocusable());
        viewList = view.getFocusables(0);
        assertEquals(0, viewList.size());

        // view is focusable
        view.setFocusable(true);
        viewList = view.getFocusables(0);
        assertEquals(1, viewList.size());
        assertEquals(view, viewList.get(0));

        viewList = view.getFocusables(-1);
        assertEquals(1, viewList.size());
        assertEquals(view, viewList.get(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRootView",
        args = {}
    )
    public void testGetRootView() {
        MockView view = new MockView(mActivity);

        assertNull(view.getParent());
        assertEquals(view, view.getRootView());

        view.setParent(mMockParent);
        assertEquals(mMockParent, view.getRootView());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSolidColor",
        args = {}
    )
    public void testGetSolidColor() {
        View view = new View(mActivity);

        assertEquals(0, view.getSolidColor());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setMinimumWidth",
        args = {int.class}
    )
    public void testSetMinimumWidth() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getSuggestedMinimumWidth());

        view.setMinimumWidth(100);
        assertEquals(100, view.getSuggestedMinimumWidth());

        view.setMinimumWidth(-100);
        assertEquals(-100, view.getSuggestedMinimumWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSuggestedMinimumWidth",
        args = {}
    )
    public void testGetSuggestedMinimumWidth() {
        MockView view = new MockView(mActivity);
        Drawable d = mResources.getDrawable(R.drawable.scenery);
        int drawableMinimumWidth = d.getMinimumWidth();

        // drawable is null
        view.setMinimumWidth(100);
        assertNull(view.getBackground());
        assertEquals(100, view.getSuggestedMinimumWidth());

        // drawable minimum width is larger than mMinWidth
        view.setBackgroundDrawable(d);
        view.setMinimumWidth(drawableMinimumWidth - 10);
        assertEquals(drawableMinimumWidth, view.getSuggestedMinimumWidth());

        // drawable minimum width is smaller than mMinWidth
        view.setMinimumWidth(drawableMinimumWidth + 10);
        assertEquals(drawableMinimumWidth + 10, view.getSuggestedMinimumWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setMinimumHeight",
        args = {int.class}
    )
    public void testSetMinimumHeight() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getSuggestedMinimumHeight());

        view.setMinimumHeight(100);
        assertEquals(100, view.getSuggestedMinimumHeight());

        view.setMinimumHeight(-100);
        assertEquals(-100, view.getSuggestedMinimumHeight());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSuggestedMinimumHeight",
        args = {}
    )
    public void testGetSuggestedMinimumHeight() {
        MockView view = new MockView(mActivity);
        Drawable d = mResources.getDrawable(R.drawable.scenery);
        int drawableMinimumHeight = d.getMinimumHeight();

        // drawable is null
        view.setMinimumHeight(100);
        assertNull(view.getBackground());
        assertEquals(100, view.getSuggestedMinimumHeight());

        // drawable minimum height is larger than mMinHeight
        view.setBackgroundDrawable(d);
        view.setMinimumHeight(drawableMinimumHeight - 10);
        assertEquals(drawableMinimumHeight, view.getSuggestedMinimumHeight());

        // drawable minimum height is smaller than mMinHeight
        view.setMinimumHeight(drawableMinimumHeight + 10);
        assertEquals(drawableMinimumHeight + 10, view.getSuggestedMinimumHeight());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWillNotCacheDrawing",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "willNotCacheDrawing",
            args = {}
        )
    })
    public void testAccessWillNotCacheDrawing() {
        View view = new View(mActivity);

        assertFalse(view.willNotCacheDrawing());

        view.setWillNotCacheDrawing(true);
        assertTrue(view.willNotCacheDrawing());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDrawingCacheEnabled",
            args = {}
        )
    })
    public void testAccessDrawingCacheEnabled() {
        View view = new View(mActivity);

        assertFalse(view.isDrawingCacheEnabled());

        view.setDrawingCacheEnabled(true);
        assertTrue(view.isDrawingCacheEnabled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDrawingCache",
        args = {}
    )
    public void testGetDrawingCache() {
        MockView view = new MockView(mActivity);

        // should not call buildDrawingCache when getDrawingCache
        assertNull(view.getDrawingCache());

        // should call buildDrawingCache when getDrawingCache
        view = (MockView) mActivity.findViewById(R.id.mock_view);
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = view.getDrawingCache();
        assertNotNull(bitmap1);
        assertEquals(view.getWidth(), bitmap1.getWidth());
        assertEquals(view.getHeight(), bitmap1.getHeight());

        view.setWillNotCacheDrawing(true);
        assertNull(view.getDrawingCache());

        view.setWillNotCacheDrawing(false);
        // build a new drawingcache
        Bitmap bitmap2 = view.getDrawingCache();
        assertNotSame(bitmap1, bitmap2);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "destroyDrawingCache",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "buildDrawingCache",
            args = {}
        )
    })
    public void testBuildAndDestroyDrawingCache() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        assertNull(view.getDrawingCache());

        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        assertNotNull(bitmap);
        assertEquals(view.getWidth(), bitmap.getWidth());
        assertEquals(view.getHeight(), bitmap.getHeight());

        view.destroyDrawingCache();
        assertNull(view.getDrawingCache());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWillNotDraw",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "willNotDraw",
            args = {}
        )
    })
    public void testAccessWillNotDraw() {
        View view = new View(mActivity);

        assertFalse(view.willNotDraw());

        view.setWillNotDraw(true);
        assertTrue(view.willNotDraw());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawingCacheQuality",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheQuality",
            args = {int.class}
        )
    })
    public void testAccessDrawingCacheQuality() {
        View view = new View(mActivity);

        assertEquals(0, view.getDrawingCacheQuality());

        view.setDrawingCacheQuality(1);
        assertEquals(0, view.getDrawingCacheQuality());

        view.setDrawingCacheQuality(0x00100000);
        assertEquals(0x00100000, view.getDrawingCacheQuality());

        view.setDrawingCacheQuality(0x00080000);
        assertEquals(0x00080000, view.getDrawingCacheQuality());

        view.setDrawingCacheQuality(0xffffffff);
        // 0x00180000 is View.DRAWING_CACHE_QUALITY_MASK
        assertEquals(0x00180000, view.getDrawingCacheQuality());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchSetSelected",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the java doc said it dispatch setSelected to" +
            " all of this View's children, but it doesn't.")
    public void testDispatchSetSelected() {
        MockView mockView1 = new MockView(mActivity);
        MockView mockView2 = new MockView(mActivity);
        mockView1.setParent(mMockParent);
        mockView2.setParent(mMockParent);

        mMockParent.dispatchSetSelected(true);
        assertFalse(mockView1.isSelected());
        assertFalse(mockView2.isSelected());

        mMockParent.dispatchSetSelected(false);
        assertFalse(mockView1.isSelected());
        assertFalse(mockView2.isSelected());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelected",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSelected",
            args = {}
        )
    })
    public void testAccessSelected() {
        View view = new View(mActivity);

        assertFalse(view.isSelected());

        view.setSelected(true);
        assertTrue(view.isSelected());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchSetPressed",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the java doc said it dispatch setPressed to" +
            " all of this View's children, but it doesn't.")
    public void testDispatchSetPressed() {
        MockView mockView1 = new MockView(mActivity);
        MockView mockView2 = new MockView(mActivity);
        mockView1.setParent(mMockParent);
        mockView2.setParent(mMockParent);

        mMockParent.dispatchSetPressed(true);
        assertFalse(mockView1.isPressed());
        assertFalse(mockView2.isPressed());

        mMockParent.dispatchSetPressed(false);
        assertFalse(mockView1.isPressed());
        assertFalse(mockView2.isPressed());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPressed",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPressed",
            args = {}
        )
    })
    public void testAccessPressed() {
        View view = new View(mActivity);

        assertFalse(view.isPressed());

        view.setPressed(true);
        assertTrue(view.isPressed());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSoundEffectsEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSoundEffectsEnabled",
            args = {}
        )
    })
    public void testAccessSoundEffectsEnabled() {
        View view = new View(mActivity);

        assertTrue(view.isSoundEffectsEnabled());

        view.setSoundEffectsEnabled(false);
        assertFalse(view.isSoundEffectsEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeepScreenOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getKeepScreenOn",
            args = {}
        )
    })
    public void testAccessKeepScreenOn() {
        View view = new View(mActivity);

        assertFalse(view.getKeepScreenOn());

        view.setKeepScreenOn(true);
        assertTrue(view.getKeepScreenOn());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDuplicateParentStateEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDuplicateParentStateEnabled",
            args = {}
        )
    })
    public void testAccessDuplicateParentStateEnabled() {
        View view = new View(mActivity);

        assertFalse(view.isDuplicateParentStateEnabled());

        view.setDuplicateParentStateEnabled(true);
        assertTrue(view.isDuplicateParentStateEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEnabled",
            args = {}
        )
    })
    public void testAccessEnabled() {
        View view = new View(mActivity);

        assertTrue(view.isEnabled());

        view.setEnabled(false);
        assertFalse(view.isEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSaveEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSaveEnabled",
            args = {}
        )
    })
    public void testAccessSaveEnabled() {
        View view = new View(mActivity);

        assertTrue(view.isSaveEnabled());

        view.setSaveEnabled(false);
        assertFalse(view.isSaveEnabled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "showContextMenu",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for showContextMenu() is incomplete." +
            "1. not clear what is supposed to happen if view hasn't a parent.")
    public void testShowContextMenu() {
        MockView view = new MockView(mActivity);

        assertNull(view.getParent());
        try {
            view.showContextMenu();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        view.setParent(mMockParent);
        assertFalse(mMockParent.hasShowContextMenuForChild());

        assertFalse(view.showContextMenu());
        assertTrue(mMockParent.hasShowContextMenuForChild());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "fitSystemWindows",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "", explanation = "can not test the cast when fitSystemWindows return" +
            " true, because it's a protected method, we have to test it by MockView, but" +
            " we can not construct a MockView instance which is FITS_SYSTEM_WINDOWS")
    public void testFitSystemWindows() {
        final XmlResourceParser parser = mResources.getLayout(R.layout.view_layout);
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        Rect insets = new Rect(10, 20, 30, 50);

        MockView view = new MockView(mActivity);
        assertFalse(view.fitSystemWindows(insets));
        assertFalse(view.fitSystemWindows(null));

        view = new MockView(mActivity, attrs, com.android.internal.R.attr.fitsSystemWindows);
        assertFalse(view.fitSystemWindows(insets));
        assertFalse(view.fitSystemWindows(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "performClick",
        args = {}
    )
    public void testPerformClick() {
        View view = new View(mActivity);
        OnClickListenerImpl listener = new OnClickListenerImpl();

        assertFalse(view.performClick());

        assertFalse(listener.hasOnClick());
        view.setOnClickListener(listener);

        assertTrue(view.performClick());
        assertTrue(listener.hasOnClick());

        view.setOnClickListener(null);
        assertFalse(view.performClick());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnClickListener",
        args = {android.view.View.OnClickListener.class}
    )
    public void testSetOnClickListener() {
        View view = new View(mActivity);
        assertFalse(view.performClick());
        assertFalse(view.isClickable());

        view.setOnClickListener(null);
        assertFalse(view.performClick());
        assertTrue(view.isClickable());

        view.setOnClickListener(new OnClickListenerImpl());
        assertTrue(view.performClick());
        assertTrue(view.isClickable());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "performLongClick",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for performLongClick() is incomplete." +
            "1. not clear what is supposed to happen if view hasn't a parent.")
    public void testPerformLongClick() {
        MockView view = new MockView(mActivity);
        OnLongClickListenerImpl listener = new OnLongClickListenerImpl();

        try {
            view.performLongClick();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        view.setParent(mMockParent);
        assertFalse(mMockParent.hasShowContextMenuForChild());
        assertFalse(view.performLongClick());
        assertTrue(mMockParent.hasShowContextMenuForChild());

        view.setOnLongClickListener(listener);
        mMockParent.reset();
        assertFalse(mMockParent.hasShowContextMenuForChild());
        assertFalse(listener.hasOnLongClick());
        assertTrue(view.performLongClick());
        assertFalse(mMockParent.hasShowContextMenuForChild());
        assertTrue(listener.hasOnLongClick());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnLongClickListener",
        args = {android.view.View.OnLongClickListener.class}
    )
    public void testSetOnLongClickListener() {
        MockView view = new MockView(mActivity);
        view.setParent(mMockParent);
        assertFalse(view.performLongClick());
        assertFalse(view.isLongClickable());

        view.setOnLongClickListener(null);
        assertFalse(view.performLongClick());
        assertTrue(view.isLongClickable());

        view.setOnLongClickListener(new OnLongClickListenerImpl());
        assertTrue(view.performLongClick());
        assertTrue(view.isLongClickable());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnFocusChangeListener",
            args = {android.view.View.OnFocusChangeListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnFocusChangeListener",
            args = {}
        )
    })
    public void testAccessOnFocusChangeListener() {
        View view = new View(mActivity);
        OnFocusChangeListener listener = new OnFocusChangeListenerImpl();

        assertNull(view.getOnFocusChangeListener());

        view.setOnFocusChangeListener(listener);
        assertSame(listener, view.getOnFocusChangeListener());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusUpId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusUpId",
            args = {}
        )
    })
    public void testAccessNextFocusUpId() {
        View view = new View(mActivity);

        assertEquals(View.NO_ID, view.getNextFocusUpId());

        view.setNextFocusUpId(1);
        assertEquals(1, view.getNextFocusUpId());

        view.setNextFocusUpId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, view.getNextFocusUpId());

        view.setNextFocusUpId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, view.getNextFocusUpId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusDownId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusDownId",
            args = {}
        )
    })
    public void testAccessNextFocusDownId() {
        View view = new View(mActivity);

        assertEquals(View.NO_ID, view.getNextFocusDownId());

        view.setNextFocusDownId(1);
        assertEquals(1, view.getNextFocusDownId());

        view.setNextFocusDownId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, view.getNextFocusDownId());

        view.setNextFocusDownId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, view.getNextFocusDownId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusLeftId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusLeftId",
            args = {}
        )
    })
    public void testAccessNextFocusLeftId() {
        View view = new View(mActivity);

        assertEquals(View.NO_ID, view.getNextFocusLeftId());

        view.setNextFocusLeftId(1);
        assertEquals(1, view.getNextFocusLeftId());

        view.setNextFocusLeftId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, view.getNextFocusLeftId());

        view.setNextFocusLeftId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, view.getNextFocusLeftId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusRightId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusRightId",
            args = {}
        )
    })
    public void testAccessNextFocusRightId() {
        View view = new View(mActivity);

        assertEquals(View.NO_ID, view.getNextFocusRightId());

        view.setNextFocusRightId(1);
        assertEquals(1, view.getNextFocusRightId());

        view.setNextFocusRightId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, view.getNextFocusRightId());

        view.setNextFocusRightId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, view.getNextFocusRightId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMeasuredDimension",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMeasuredWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMeasuredHeight",
            args = {}
        )
    })
    public void testAccessMeasuredDimension() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getMeasuredWidth());
        assertEquals(0, view.getMeasuredHeight());

        view.setMeasuredDimensionWrapper(20, 30);
        assertEquals(20, view.getMeasuredWidth());
        assertEquals(30, view.getMeasuredHeight());

        view.setMeasuredDimensionWrapper(-20, -30);
        assertEquals(-20, view.getMeasuredWidth());
        assertEquals(-30, view.getMeasuredHeight());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "measure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMeasure",
            args = {int.class, int.class}
        )
    })
    public void testMeasure() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnMeasure());
        assertEquals(100, view.getMeasuredWidth());
        assertEquals(200, view.getMeasuredHeight());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.requestLayout();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnMeasure());
        assertEquals(100, view.getMeasuredWidth());
        assertEquals(200, view.getMeasuredHeight());

        view.reset();
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 100);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnMeasure());
        assertEquals(200, view.getMeasuredWidth());
        assertEquals(100, view.getMeasuredHeight());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutParams",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setLayoutParams() is incomplete." +
            "1. not clear what is supposed to happen if params is null.")
    public void testAccessLayoutParams() {
        View view = new View(mActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(10, 20);

        assertNull(view.getLayoutParams());

        try {
            view.setLayoutParams(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        assertFalse(view.isLayoutRequested());
        view.setLayoutParams(params);
        assertSame(params, view.getLayoutParams());
        assertTrue(view.isLayoutRequested());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isShown",
        args = {}
    )
    public void testIsShown() {
        MockView view = new MockView(mActivity);

        view.setVisibility(View.INVISIBLE);
        assertFalse(view.isShown());

        view.setVisibility(View.VISIBLE);
        assertNull(view.getParent());
        assertFalse(view.isShown());

        view.setParent(mMockParent);
        // mMockParent is not a instance of ViewRoot
        assertFalse(view.isShown());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDrawingTime",
        args = {}
    )
    public void testGetDrawingTime() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertEquals(0, view.getDrawingTime());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertEquals(SystemClock.uptimeMillis(), view.getDrawingTime(), 1000);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scheduleDrawable",
        args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class, long.class}
    )
    public void testScheduleDrawable() {
        View view = new View(mActivity);
        Drawable drawable = new StateListDrawable();
        Runnable what = new Runnable() {
            public void run() {
                // do nothing
            }
        };

        // mAttachInfo is null
        view.scheduleDrawable(drawable, what, 1000);

        view.setBackgroundDrawable(drawable);
        view.scheduleDrawable(drawable, what, 1000);

        view.scheduleDrawable(null, null, -1000);

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.scheduleDrawable(drawable, what, 1000);

        view.scheduleDrawable(view.getBackground(), what, 1000);
        view.unscheduleDrawable(view.getBackground(), what);

        view.scheduleDrawable(null, null, -1000);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unscheduleDrawable",
            args = {android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unscheduleDrawable",
            args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class}
        )
    })
    public void testUnscheduleDrawable() {
        View view = new View(mActivity);
        Drawable drawable = new StateListDrawable();
        Runnable what = new Runnable() {
            public void run() {
                // do nothing
            }
        };

        // mAttachInfo is null
        view.unscheduleDrawable(drawable, what);

        view.setBackgroundDrawable(drawable);
        view.unscheduleDrawable(drawable);

        view.unscheduleDrawable(null, null);
        view.unscheduleDrawable(null);

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.unscheduleDrawable(drawable);

        view.scheduleDrawable(view.getBackground(), what, 1000);
        view.unscheduleDrawable(view.getBackground(), what);

        view.unscheduleDrawable(null);
        view.unscheduleDrawable(null, null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getWindowVisibility",
        args = {}
    )
    public void testGetWindowVisibility() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertEquals(View.GONE, view.getWindowVisibility());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertEquals(View.VISIBLE, view.getWindowVisibility());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getWindowToken",
        args = {}
    )
    public void testGetWindowToken() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertNull(view.getWindowToken());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertNotNull(view.getWindowToken());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasWindowFocus",
        args = {}
    )
    public void testHasWindowFocus() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertFalse(view.hasWindowFocus());

        // mAttachInfo is not null
        final View view2 = mActivity.findViewById(R.id.fit_windows);
        // Wait until the window has been focused.
        new DelayedCheck(TIMEOUT_DELTA) {
            @Override
            protected boolean check() {
                return view2.hasWindowFocus();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHandler",
        args = {}
    )
    @ToBeFixed(bug = "1400249", explanation = "View#getHandler() is protected, so we should" +
            " test it by MockView, but we cannot access mAttachInfo which is package protected")
    public void testGetHandler() {
        MockView view = new MockView(mActivity);
        // mAttachInfo is null
        assertNull(view.getHandler());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeCallbacks",
        args = {java.lang.Runnable.class}
    )
    public void testRemoveCallbacks() throws InterruptedException {
        final long delay = 500L;
        View view = mActivity.findViewById(R.id.mock_view);
        MockRunnable runner = new MockRunnable();
        assertTrue(view.postDelayed(runner, delay));
        assertTrue(view.removeCallbacks(runner));
        assertTrue(view.removeCallbacks(null));
        assertTrue(view.removeCallbacks(new MockRunnable()));
        Thread.sleep(delay * 2);
        assertFalse(runner.hasRun);
        // check that the runner actually works
        runner = new MockRunnable();
        assertTrue(view.postDelayed(runner, delay));
        Thread.sleep(delay * 2);
        assertTrue(runner.hasRun);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "cancelLongPress",
        args = {}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test for this method, " +
            "should be tested by functional test")
    public void testCancelLongPress() {
        View view = new View(mActivity);
        // mAttachInfo is null
        view.cancelLongPress();

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.cancelLongPress();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getViewTreeObserver",
        args = {}
    )
    @ToBeFixed(bug = "1400249", explanation = "we cannot access both mFloatingTreeObserver" +
            " and mAttachInfo which are package protected")
    public void testGetViewTreeObserver() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertNotNull(view.getViewTreeObserver());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertNotNull(view.getViewTreeObserver());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getWindowAttachCount",
        args = {}
    )
    @ToBeFixed(bug = "1400249", explanation = "View#getHandler() is protected, so we should" +
            " test it by MockView, but we cannot access method" +
            " View#dispatchAttachedToWindow(AttachInfo, int) which is package protected" +
            " to update mWindowAttachCount")
    public void testGetWindowAttachCount() {
        MockView view = new MockView(mActivity);
        // mAttachInfo is null
        assertEquals(0, view.getWindowAttachCount());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onAttachedToWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDetachedFromWindow",
            args = {}
        )
    })
    @UiThreadTest
    public void testOnAttachedToAndDetachedFromWindow() {
        MockView mockView = new MockView(mActivity);
        ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);

        viewGroup.addView(mockView);
        assertTrue(mockView.hasCalledOnAttachedToWindow());

        viewGroup.removeView(mockView);
        assertTrue(mockView.hasCalledOnDetachedFromWindow());

        mockView.reset();
        mActivity.setContentView(mockView);
        assertTrue(mockView.hasCalledOnAttachedToWindow());

        mActivity.setContentView(R.layout.view_layout);
        assertTrue(mockView.hasCalledOnDetachedFromWindow());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLocationInWindow",
        args = {int[].class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for getLocationInWindow() is incomplete." +
            "1. not clear what is supposed to happen if the input int[] is null or short than 2.")
    public void testGetLocationInWindow() {
        int[] location = new int[] { -1, -1 };

        View layout = mActivity.findViewById(R.id.viewlayout_root);
        int[] layoutLocation = new int[] { -1, -1 };
        layout.getLocationInWindow(layoutLocation);

        final View mockView = mActivity.findViewById(R.id.mock_view);
        mockView.getLocationInWindow(location);
        assertEquals(layoutLocation[0], location[0]);
        assertEquals(layoutLocation[1], location[1]);

        View scrollView = mActivity.findViewById(R.id.scroll_view);
        scrollView.getLocationInWindow(location);
        assertEquals(layoutLocation[0], location[0]);
        assertEquals(layoutLocation[1] + mockView.getHeight(), location[1]);

        try {
            mockView.getLocationInWindow(null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            mockView.getLocationInWindow(new int[] { 0 });
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLocationOnScreen",
        args = {int[].class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for getLocationOnScreen() is incomplete." +
            "1. not clear what is supposed to happen if mAttachInfo is null.")
    public void testGetLocationOnScreen() {
        View view = new View(mActivity);
        int[] location = new int[] { -1, -1 };

        // mAttachInfo is not null
        View layout = mActivity.findViewById(R.id.viewlayout_root);
        int[] layoutLocation = new int[] { -1, -1 };
        layout.getLocationOnScreen(layoutLocation);

        View mockView = mActivity.findViewById(R.id.mock_view);
        mockView.getLocationOnScreen(location);
        assertEquals(0, location[0]);
        assertEquals(layoutLocation[1], location[1]);

        View scrollView = mActivity.findViewById(R.id.scroll_view);
        scrollView.getLocationOnScreen(location);
        assertEquals(0, location[0]);
        assertEquals(layoutLocation[1] + mockView.getHeight(), location[1]);

        try {
            scrollView.getLocationOnScreen(null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            scrollView.getLocationOnScreen(new int[] { 0 });
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addTouchables",
        args = {java.util.ArrayList.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addTouchables() is incomplete." +
            "1. not clear what is supposed to happen if the input ArrayList<View> is null.")
    public void testAddTouchables() {
        View view = new View(mActivity);
        ArrayList<View> result = new ArrayList<View>();
        assertEquals(0, result.size());

        view.addTouchables(result);
        assertEquals(0, result.size());

        view.setClickable(true);
        view.addTouchables(result);
        assertEquals(1, result.size());
        assertSame(view, result.get(0));

        try {
            view.addTouchables(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        result.clear();
        view.setEnabled(false);
        assertTrue(view.isClickable());
        view.addTouchables(result);
        assertEquals(0, result.size());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTouchables",
        args = {}
    )
    public void testGetTouchables() {
        View view = new View(mActivity);
        ArrayList<View> result;

        result = view.getTouchables();
        assertEquals(0, result.size());

        view.setClickable(true);
        result = view.getTouchables();
        assertEquals(1, result.size());
        assertSame(view, result.get(0));

        result.clear();
        view.setEnabled(false);
        assertTrue(view.isClickable());
        result = view.getTouchables();
        assertEquals(0, result.size());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "inflate",
            args = {android.content.Context.class, int.class, android.view.ViewGroup.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFinishInflate",
            args = {}
        )
    })
    public void testInflate() {
        View view = View.inflate(mActivity, R.layout.view_layout, null);
        assertNotNull(view);
        assertTrue(view instanceof LinearLayout);

        MockView mockView = (MockView) view.findViewById(R.id.mock_view);
        assertNotNull(mockView);
        assertTrue(mockView.hasCalledOnFinishInflate());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isInTouchMode",
        args = {}
    )
    public void testIsInTouchMode() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertFalse(view.isInTouchMode());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertFalse(view.isInTouchMode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isInEditMode",
        args = {}
    )
    public void testIsInEditMode() {
        View view = new View(mActivity);
        assertFalse(view.isInEditMode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "postInvalidate",
        args = {}
    )
    public void testPostInvalidate1() {
        View view = new View(mActivity);
        // mAttachInfo is null
        view.postInvalidate();

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.postInvalidate();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "postInvalidate",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testPostInvalidate2() {
        View view = new View(mActivity);
        // mAttachInfo is null
        view.postInvalidate(0, 1, 2, 3);

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.postInvalidate(10, 20, 30, 40);
        view.postInvalidate(0, -20, -30, -40);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "postInvalidateDelayed",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "postInvalidateDelayed",
            args = {long.class, int.class, int.class, int.class, int.class}
        )
    })
    public void testPostInvalidateDelayed() {
        View view = new View(mActivity);
        // mAttachInfo is null
        view.postInvalidateDelayed(1000);
        view.postInvalidateDelayed(500, 0, 0, 100, 200);

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        view.postInvalidateDelayed(1000);
        view.postInvalidateDelayed(500, 0, 0, 100, 200);
        view.postInvalidateDelayed(-1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "post",
        args = {java.lang.Runnable.class}
    )
    public void testPost() {
        View view = new View(mActivity);
        MockRunnable action = new MockRunnable();

        // mAttachInfo is null
        assertTrue(view.post(action));
        assertTrue(view.post(null));

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertTrue(view.post(action));
        assertTrue(view.post(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "postDelayed",
        args = {java.lang.Runnable.class, long.class}
    )
    public void testPostDelayed() {
        View view = new View(mActivity);
        MockRunnable action = new MockRunnable();

        // mAttachInfo is null
        assertTrue(view.postDelayed(action, 1000));
        assertTrue(view.postDelayed(null, -1));

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertTrue(view.postDelayed(action, 1000));
        assertTrue(view.postDelayed(null, 0));
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "playSoundEffect",
        args = {int.class}
    )
    @UiThreadTest
    public void testPlaySoundEffect() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        // sound effect enabled
        view.playSoundEffect(SoundEffectConstants.CLICK);

        // sound effect disabled
        view.setSoundEffectsEnabled(false);
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);

        // no way to assert the soundConstant be really played.
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyShortcut",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testOnKeyShortcut() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setFocusable(true);
            }
        });

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
        getInstrumentation().sendKeySync(event);
        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        getInstrumentation().sendKeySync(event);
        assertTrue(view.hasCalledOnKeyShortcut());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyMultiple",
        args = {int.class, int.class, android.view.KeyEvent.class}
    )
    public void testOnKeyMultiple() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setFocusable(true);
            }
        });

        assertFalse(view.hasCalledOnKeyMultiple());
        view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_ENTER));
        assertTrue(view.hasCalledOnKeyMultiple());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchKeyShortcutEvent",
        args = {android.view.KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "View#dispatchKeyShortcutEvent(KeyEvent) when the input KeyEvent is null")
    @UiThreadTest
    public void testDispatchKeyShortcutEvent() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        view.setFocusable(true);

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        view.dispatchKeyShortcutEvent(event);
        assertTrue(view.hasCalledOnKeyShortcut());

        try {
            view.dispatchKeyShortcutEvent(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onTrackballEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTrackballEvent() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setEnabled(true);
                view.setFocusable(true);
                view.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        int[] xy = new int[2];
        view.getLocationOnScreen(xy);

        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        final float x = xy[0] + viewWidth / 2.0f;
        final float y = xy[1] + viewHeight / 2.0f;

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN,
                x, y, 0);
        getInstrumentation().sendTrackballEventSync(event);
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnTrackballEvent());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchTrackballEvent",
        args = {android.view.MotionEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for dispatchTrackballEvent() is " +
            "incomplete. It passes a trackball motion event down to itself even if it is not " +
            "the focused view.")
    @UiThreadTest
    public void testDispatchTrackballEvent() {
        ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        MockView mockView1 = new MockView(mActivity);
        MockView mockView2 = new MockView(mActivity);
        viewGroup.addView(mockView1);
        viewGroup.addView(mockView2);
        mockView1.setFocusable(true);
        mockView2.setFocusable(true);
        mockView2.requestFocus();

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN,
                1, 2, 0);
        mockView1.dispatchTrackballEvent(event);
        // issue 1695243
        // It passes a trackball motion event down to itself even if it is not the focused view.
        assertTrue(mockView1.hasCalledOnTrackballEvent());
        assertFalse(mockView2.hasCalledOnTrackballEvent());

        mockView1.reset();
        mockView2.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1, 2, 0);
        mockView2.dispatchTrackballEvent(event);
        assertFalse(mockView1.hasCalledOnTrackballEvent());
        assertTrue(mockView2.hasCalledOnTrackballEvent());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchUnhandledMove",
        args = {android.view.View.class, int.class}
    )
    public void testDispatchUnhandledMove() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setFocusable(true);
                view.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
        getInstrumentation().sendKeySync(event);

        assertTrue(view.hasCalledDispatchUnhandledMove());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowVisibilityChanged",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchWindowVisibilityChanged",
            args = {int.class}
        )
    })
    public void testWindowVisibilityChanged() throws Throwable {
        final MockView mockView = new MockView(mActivity);
        final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);

        runTestOnUiThread(new Runnable() {
            public void run() {
                viewGroup.addView(mockView);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.hasCalledOnWindowVisibilityChanged());

        mockView.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                getActivity().setVisible(false);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.hasCalledDispatchWindowVisibilityChanged());
        assertTrue(mockView.hasCalledOnWindowVisibilityChanged());

        mockView.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                getActivity().setVisible(true);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.hasCalledDispatchWindowVisibilityChanged());
        assertTrue(mockView.hasCalledOnWindowVisibilityChanged());

        mockView.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                viewGroup.removeView(mockView);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.hasCalledOnWindowVisibilityChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLocalVisibleRect",
        args = {android.graphics.Rect.class}
    )
    public void testGetLocalVisibleRect() throws Throwable {
        final View view = mActivity.findViewById(R.id.mock_view);
        Rect rect = new Rect();

        assertTrue(view.getLocalVisibleRect(rect));
        assertEquals(0, rect.left);
        assertEquals(0, rect.top);
        assertEquals(100, rect.right);
        assertEquals(200, rect.bottom);

        final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(0, 300);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams1);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getLocalVisibleRect(rect));

        final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(200, -10);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams2);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.getLocalVisibleRect(rect));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int halfWidth = display.getWidth() / 2;
        int halfHeight = display.getHeight() /2;

        final LinearLayout.LayoutParams layoutParams3 =
                new LinearLayout.LayoutParams(halfWidth, halfHeight);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setLayoutParams(layoutParams3);
                view.scrollTo(20, -30);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.getLocalVisibleRect(rect));
        assertEquals(20, rect.left);
        assertEquals(-30, rect.top);
        assertEquals(halfWidth + 20, rect.right);
        assertEquals(halfHeight - 30, rect.bottom);

        try {
            view.getLocalVisibleRect(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "mergeDrawableStates",
        args = {int[].class, int[].class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for mergeDrawableStates() is incomplete." +
            "1. not clear what is supposed to happen if the input int[] is null or" +
            "   baseState is not long enough to append additionalState.")
    public void testMergeDrawableStates() {
        MockView view = new MockView(mActivity);

        int[] states = view.mergeDrawableStatesWrapper(new int[] { 0, 1, 2, 0, 0 },
                new int[] { 3 });
        assertNotNull(states);
        assertEquals(5, states.length);
        assertEquals(0, states[0]);
        assertEquals(1, states[1]);
        assertEquals(2, states[2]);
        assertEquals(3, states[3]);
        assertEquals(0, states[4]);

        try {
            view.mergeDrawableStatesWrapper(new int[] { 1, 2 }, new int[] { 3 });
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            view.mergeDrawableStatesWrapper(null, new int[] { 0 });
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            view.mergeDrawableStatesWrapper(new int [] { 0 }, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onSaveInstanceState",
            args = {}
        )
    })
    public void testOnSaveAndRestoreInstanceState() {
        // it is hard to simulate operation to make callback be called.
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchRestoreInstanceState",
            args = {android.util.SparseArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restoreHierarchyState",
            args = {android.util.SparseArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchSaveInstanceState",
            args = {android.util.SparseArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveHierarchyState",
            args = {android.util.SparseArray.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc of saveHierarchyState() and " +
            "restoreHierarchyState() are incomplete." +
            "1, not clear what is supposed to happen if input SparseArray<Parcelable> is null." +
            "2, no description about IllegalArgumentException which thrown in a certain condition.")
    public void testSaveAndRestoreHierarchyState() {
        int viewId = R.id.mock_view;
        MockView view = (MockView) mActivity.findViewById(viewId);
        SparseArray<Parcelable> container = new SparseArray<Parcelable>();
        view.saveHierarchyState(container);
        assertTrue(view.hasCalledDispatchSaveInstanceState());
        assertTrue(view.hasCalledOnSaveInstanceState());
        assertEquals(viewId, container.keyAt(0));

        view.reset();
        container.put(R.id.mock_view, BaseSavedState.EMPTY_STATE);
        view.restoreHierarchyState(container);
        assertTrue(view.hasCalledDispatchRestoreInstanceState());
        assertTrue(view.hasCalledOnRestoreInstanceState());
        container.clear();
        view.saveHierarchyState(container);
        assertTrue(view.hasCalledDispatchSaveInstanceState());
        assertTrue(view.hasCalledOnSaveInstanceState());
        assertEquals(viewId, container.keyAt(0));

        container.clear();
        container.put(viewId, new BaseSavedState(BaseSavedState.EMPTY_STATE));
        try {
            view.restoreHierarchyState(container);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            view.restoreHierarchyState(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            view.saveHierarchyState(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
        )
    })
    public void testOnKeyDownOrUp() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setFocusable(true);
            }
        });

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        getInstrumentation().sendKeySync(event);
        assertTrue(view.hasCalledOnKeyDown());

        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0);
        getInstrumentation().sendKeySync(event);
        assertTrue(view.hasCalledOnKeyUp());

        view.reset();
        assertTrue(view.isEnabled());
        assertFalse(view.isClickable());
        assertFalse(view.isPressed());
        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
        getInstrumentation().sendKeySync(event);
        assertFalse(view.isPressed());
        assertTrue(view.hasCalledOnKeyDown());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setEnabled(true);
                view.setClickable(true);
            }
        });
        view.reset();
        OnClickListenerImpl listener = new OnClickListenerImpl();
        view.setOnClickListener(listener);

        assertFalse(view.isPressed());
        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
        getInstrumentation().sendKeySync(event);
        assertTrue(view.isPressed());
        assertTrue(view.hasCalledOnKeyDown());
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
        getInstrumentation().sendKeySync(event);
        assertFalse(view.isPressed());
        assertTrue(view.hasCalledOnKeyUp());
        assertTrue(listener.hasOnClick());

        view.setPressed(false);
        listener.reset();
        view.reset();

        assertFalse(view.isPressed());
        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().sendKeySync(event);
        assertTrue(view.isPressed());
        assertTrue(view.hasCalledOnKeyDown());
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().sendKeySync(event);
        assertFalse(view.isPressed());
        assertTrue(view.hasCalledOnKeyUp());
        assertTrue(listener.hasOnClick());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnKeyListener",
            args = {android.view.View.OnKeyListener.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for dispatchKeyEvent() is incomplete." +
            "1. not clear what is supposed to happen if the KeyEvent is null." +
            "2. When the view has NOT focus, it dispatchs to itself, which disobey the javadoc.")
    @UiThreadTest
    public void testDispatchKeyEvent() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        MockView mockView1 = new MockView(mActivity);
        MockView mockView2 = new MockView(mActivity);
        ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        viewGroup.addView(mockView1);
        viewGroup.addView(mockView2);
        view.setFocusable(true);
        mockView1.setFocusable(true);
        mockView2.setFocusable(true);

        assertFalse(view.hasCalledOnKeyDown());
        assertFalse(mockView1.hasCalledOnKeyDown());
        assertFalse(mockView2.hasCalledOnKeyDown());
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        assertFalse(view.dispatchKeyEvent(event));
        assertTrue(view.hasCalledOnKeyDown());
        assertFalse(mockView1.hasCalledOnKeyDown());
        assertFalse(mockView2.hasCalledOnKeyDown());

        view.reset();
        mockView1.reset();
        mockView2.reset();
        assertFalse(view.hasCalledOnKeyDown());
        assertFalse(mockView1.hasCalledOnKeyDown());
        assertFalse(mockView2.hasCalledOnKeyDown());
        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        assertFalse(mockView1.dispatchKeyEvent(event));
        assertFalse(view.hasCalledOnKeyDown());
        // issue 1695243
        // When the view has NOT focus, it dispatches to itself, which disobey the javadoc.
        assertTrue(mockView1.hasCalledOnKeyDown());
        assertFalse(mockView2.hasCalledOnKeyDown());

        assertFalse(view.hasCalledOnKeyUp());
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0);
        assertFalse(view.dispatchKeyEvent(event));
        assertTrue(view.hasCalledOnKeyUp());

        assertFalse(view.hasCalledOnKeyMultiple());
        event = new KeyEvent(1, 2, KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_0, 2);
        assertFalse(view.dispatchKeyEvent(event));
        assertTrue(view.hasCalledOnKeyMultiple());

        try {
            view.dispatchKeyEvent(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        view.reset();
        event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0);
        OnKeyListenerImpl listener = new OnKeyListenerImpl();
        view.setOnKeyListener(listener);
        assertFalse(listener.hasOnKey());
        assertTrue(view.dispatchKeyEvent(event));
        assertTrue(listener.hasOnKey());
        assertFalse(view.hasCalledOnKeyUp());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnTouchListener",
            args = {android.view.View.OnTouchListener.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for dispatchTouchEvent() is incomplete" +
            "1. it passes the touch screen motion event down to itself even if it is not " +
            "   the target view.")
    @UiThreadTest
    public void testDispatchTouchEvent() {
        ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        MockView mockView1 = new MockView(mActivity);
        MockView mockView2 = new MockView(mActivity);
        viewGroup.addView(mockView1);
        viewGroup.addView(mockView2);

        int[] xy = new int[2];
        mockView1.getLocationOnScreen(xy);

        final int viewWidth = mockView1.getWidth();
        final int viewHeight = mockView1.getHeight();
        final float x = xy[0] + viewWidth / 2.0f;
        final float y = xy[1] + viewHeight / 2.0f;

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                x, y, 0);

        assertFalse(mockView1.hasCalledOnTouchEvent());
        assertFalse(mockView1.dispatchTouchEvent(event));
        assertTrue(mockView1.hasCalledOnTouchEvent());

        assertFalse(mockView2.hasCalledOnTouchEvent());
        assertFalse(mockView2.dispatchTouchEvent(event));
        // issue 1695243
        // it passes the touch screen motion event down to itself even if it is not the target view.
        assertTrue(mockView2.hasCalledOnTouchEvent());

        assertFalse(mockView1.dispatchTouchEvent(null));

        mockView1.reset();
        OnTouchListenerImpl listener = new OnTouchListenerImpl();
        mockView1.setOnTouchListener(listener);
        assertFalse(listener.hasOnTouch());
        assertTrue(mockView1.dispatchTouchEvent(event));
        assertTrue(listener.hasOnTouch());
        assertFalse(mockView1.hasCalledOnTouchEvent());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidate",
        args = {}
    )
    public void testInvalidate1() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.invalidate();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(View.INVISIBLE);
                view.invalidate();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.hasCalledOnDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidate",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for invalidate() is incomplete." +
            "1. not clear what is supposed to happen if the input Rect is null.")
    public void testInvalidate2() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnDraw());

        try {
            view.invalidate(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        view.reset();
        final Rect dirty = new Rect(view.getLeft() + 1, view.getTop() + 1,
                view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() / 2);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.invalidate(dirty);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(View.INVISIBLE);
                view.invalidate(dirty);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.hasCalledOnDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidate",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testInvalidate3() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        final Rect dirty = new Rect(view.getLeft() + 1, view.getTop() + 1,
                view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() / 2);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.invalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(View.INVISIBLE);
                view.invalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.hasCalledOnDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for invalidateDrawable() is incomplete." +
            "1. not clear what is supposed to happen if the input Drawable is null.")
    public void testInvalidateDrawable() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        final Drawable d1 = mResources.getDrawable(R.drawable.scenery);
        final Drawable d2 = mResources.getDrawable(R.drawable.pass);

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setBackgroundDrawable(d1);
                view.invalidateDrawable(d1);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.hasCalledOnDraw());

        view.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.invalidateDrawable(d2);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(view.hasCalledOnDraw());

        MockView viewTestNull = new MockView(mActivity);
        try {
            viewTestNull.invalidateDrawable(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onFocusChanged",
        args = {boolean.class, int.class, android.graphics.Rect.class}
    )
    @UiThreadTest
    public void testOnFocusChanged() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        mActivity.findViewById(R.id.fit_windows).setFocusable(true);
        view.setFocusable(true);
        assertFalse(view.hasCalledOnFocusChanged());

        view.requestFocus();
        assertTrue(view.hasCalledOnFocusChanged());

        view.reset();
        view.clearFocus();
        assertTrue(view.hasCalledOnFocusChanged());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDrawableState",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawableState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "drawableStateChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "refreshDrawableState",
            args = {}
        )
    })
    public void testDrawableState() {
        MockView view = new MockView(mActivity);
        view.setParent(mMockParent);

        assertFalse(view.hasCalledOnCreateDrawableState());
        assertTrue(Arrays.equals(MockView.getEnabledStateSet(), view.getDrawableState()));
        assertTrue(view.hasCalledOnCreateDrawableState());

        view.reset();
        assertFalse(view.hasCalledOnCreateDrawableState());
        assertTrue(Arrays.equals(MockView.getEnabledStateSet(), view.getDrawableState()));
        assertFalse(view.hasCalledOnCreateDrawableState());

        view.reset();
        assertFalse(view.hasCalledDrawableStateChanged());
        view.setPressed(true);
        assertTrue(view.hasCalledDrawableStateChanged());
        assertFalse(view.hasCalledOnCreateDrawableState());
        assertTrue(Arrays.equals(MockView.getPressedEnabledStateSet(), view.getDrawableState()));
        assertTrue(view.hasCalledOnCreateDrawableState());

        view.reset();
        mMockParent.reset();
        assertFalse(view.hasCalledDrawableStateChanged());
        assertFalse(mMockParent.hasChildDrawableStateChanged());
        view.refreshDrawableState();
        assertTrue(view.hasCalledDrawableStateChanged());
        assertTrue(mMockParent.hasChildDrawableStateChanged());
        assertFalse(view.hasCalledOnCreateDrawableState());
        assertTrue(Arrays.equals(MockView.getPressedEnabledStateSet(), view.getDrawableState()));
        assertTrue(view.hasCalledOnCreateDrawableState());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchWindowFocusChanged",
            args = {boolean.class}
        )
    })
    public void testWindowFocusChanged() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        assertTrue(view.hasCalledOnWindowFocusChanged());
        assertTrue(view.hasCalledDispatchWindowFocusChanged());

        view.reset();
        assertFalse(view.hasCalledOnWindowFocusChanged());
        assertFalse(view.hasCalledDispatchWindowFocusChanged());

        StubActivity activity = launchActivity("com.android.cts.stub", StubActivity.class, null);
        assertTrue(view.hasCalledOnWindowFocusChanged());
        assertTrue(view.hasCalledDispatchWindowFocusChanged());

        activity.finish();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDraw",
            args = {android.graphics.Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchDraw",
            args = {android.graphics.Canvas.class}
        )
    })
    public void testDraw() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        runTestOnUiThread(new Runnable() {
            public void run() {
                view.requestLayout();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertTrue(view.hasCalledOnDraw());
        assertTrue(view.hasCalledDispatchDraw());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestFocusFromTouch",
        args = {}
    )
    public void testRequestFocusFromTouch() {
        View view = new View(mActivity);
        view.setFocusable(true);
        assertFalse(view.isFocused());

        view.requestFocusFromTouch();
        assertTrue(view.isFocused());

        view.requestFocusFromTouch();
        assertTrue(view.isFocused());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestRectangleOnScreen",
        args = {android.graphics.Rect.class, boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for requestRectangleOnScreen() is incomplete." +
            "1. not clear what is supposed to happen if the input Rect is null.")
    public void testRequestRectangleOnScreen1() {
        MockView view = new MockView(mActivity);
        Rect rectangle = new Rect(10, 10, 20, 30);
        MockViewGroupParent parent = new MockViewGroupParent(mActivity);

        // parent is null
        assertFalse(view.requestRectangleOnScreen(rectangle, true));
        assertFalse(view.requestRectangleOnScreen(rectangle, false));
        assertFalse(view.requestRectangleOnScreen(null, true));

        view.setParent(parent);
        view.scrollTo(1, 2);
        assertFalse(parent.hasRequestChildRectangleOnScreen());

        assertFalse(view.requestRectangleOnScreen(rectangle, true));
        assertTrue(parent.hasRequestChildRectangleOnScreen());

        parent.reset();
        view.scrollTo(11, 22);
        assertFalse(parent.hasRequestChildRectangleOnScreen());

        assertFalse(view.requestRectangleOnScreen(rectangle, true));
        assertTrue(parent.hasRequestChildRectangleOnScreen());

        try {
            view.requestRectangleOnScreen(null, true);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestRectangleOnScreen",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for requestRectangleOnScreen() is incomplete." +
            "1. not clear what is supposed to happen if the input Rect is null.")
    public void testRequestRectangleOnScreen2() {
        MockView view = new MockView(mActivity);
        Rect rectangle = new Rect();
        MockViewGroupParent parent = new MockViewGroupParent(mActivity);

        // parent is null
        assertFalse(view.requestRectangleOnScreen(rectangle));
        assertFalse(view.requestRectangleOnScreen(null));
        assertEquals(0, rectangle.left);
        assertEquals(0, rectangle.top);
        assertEquals(0, rectangle.right);
        assertEquals(0, rectangle.bottom);

        view.setParent(parent);
        view.scrollTo(1, 2);
        assertFalse(parent.hasRequestChildRectangleOnScreen());

        assertFalse(view.requestRectangleOnScreen(rectangle));
        assertTrue(parent.hasRequestChildRectangleOnScreen());
        assertEquals(-1, rectangle.left);
        assertEquals(-2, rectangle.top);
        assertEquals(-1, rectangle.right);
        assertEquals(-2, rectangle.bottom);

        try {
            view.requestRectangleOnScreen(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * For the duration of the tap timeout we are in a 'prepressed' state
     * to differentiate between taps and touch scrolls.
     * Wait at least this long before testing if the view is pressed
     * by calling this function.
     */
    private void waitPrepressedTimeout() {
        try {
            Thread.sleep(ViewConfiguration.getTapTimeout() + 10);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "waitPrepressedTimeout() interrupted! Test may fail!", e);
        }
        getInstrumentation().waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onTouchEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTouchEvent() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        assertTrue(view.isEnabled());
        assertFalse(view.isClickable());
        assertFalse(view.isLongClickable());

        TouchUtils.clickView(this, view);
        assertTrue(view.hasCalledOnTouchEvent());

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setEnabled(true);
                view.setClickable(true);
                view.setLongClickable(true);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(view.isEnabled());
        assertTrue(view.isClickable());
        assertTrue(view.isLongClickable());

        // MotionEvent.ACTION_DOWN
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);

        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        float x = xy[0] + viewWidth / 2.0f;
        float y = xy[1] + viewHeight / 2.0f;

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN,
                x, y, 0);
        assertFalse(view.isPressed());
        getInstrumentation().sendPointerSync(event);
        waitPrepressedTimeout();
        assertTrue(view.hasCalledOnTouchEvent());
        assertTrue(view.isPressed());

        // MotionEvent.ACTION_MOVE
        // move out of the bound.
        view.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        int slop = ViewConfiguration.get(mActivity).getScaledTouchSlop();
        x = xy[0] + viewWidth + slop;
        y = xy[1] + viewHeight + slop;
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
        getInstrumentation().sendPointerSync(event);
        assertTrue(view.hasCalledOnTouchEvent());
        assertFalse(view.isPressed());

        // move into view
        view.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        x = xy[0] + viewWidth - 1;
        y = xy[1] + viewHeight - 1;
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
        getInstrumentation().sendPointerSync(event);
        waitPrepressedTimeout();
        assertTrue(view.hasCalledOnTouchEvent());
        assertFalse(view.isPressed());

        // MotionEvent.ACTION_UP
        OnClickListenerImpl listener = new OnClickListenerImpl();
        view.setOnClickListener(listener);
        view.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        getInstrumentation().sendPointerSync(event);
        assertTrue(view.hasCalledOnTouchEvent());
        assertFalse(listener.hasOnClick());

        view.reset();
        x = xy[0] + viewWidth / 2.0f;
        y = xy[1] + viewHeight / 2.0f;
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        getInstrumentation().sendPointerSync(event);
        assertTrue(view.hasCalledOnTouchEvent());

        // MotionEvent.ACTION_CANCEL
        view.reset();
        listener.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_CANCEL, x, y, 0);
        getInstrumentation().sendPointerSync(event);
        assertTrue(view.hasCalledOnTouchEvent());
        assertFalse(view.isPressed());
        assertFalse(listener.hasOnClick());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "bringToFront",
        args = {}
    )
    public void testBringToFront() {
        MockView view = new MockView(mActivity);
        view.setParent(mMockParent);

        assertFalse(mMockParent.hasBroughtChildToFront());
        view.bringToFront();
        assertTrue(mMockParent.hasBroughtChildToFront());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getApplicationWindowToken",
        args = {}
    )
    public void testGetApplicationWindowToken() {
        View view = new View(mActivity);
        // mAttachInfo is null
        assertNull(view.getApplicationWindowToken());

        // mAttachInfo is not null
        view = mActivity.findViewById(R.id.fit_windows);
        assertNotNull(view.getApplicationWindowToken());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBottomPaddingOffset",
        args = {}
    )
    public void testGetBottomPaddingOffset() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getBottomPaddingOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLeftPaddingOffset",
        args = {}
    )
    public void testGetLeftPaddingOffset() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getLeftPaddingOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRightPaddingOffset",
        args = {}
    )
    public void testGetRightPaddingOffset() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getRightPaddingOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTopPaddingOffset",
        args = {}
    )
    public void testGetTopPaddingOffset() {
        MockView view = new MockView(mActivity);
        assertEquals(0, view.getTopPaddingOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isPaddingOffsetRequired",
        args = {}
    )
    public void testIsPaddingOffsetRequired() {
        MockView view = new MockView(mActivity);
        assertFalse(view.isPaddingOffsetRequired());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getWindowVisibleDisplayFrame",
        args = {android.graphics.Rect.class}
    )
    public void testGetWindowVisibleDisplayFrame() {
        Rect outRect = new Rect();
        View view = new View(mActivity);
        // mAttachInfo is null
        Display d = WindowManagerImpl.getDefault().getDefaultDisplay();
        view.getWindowVisibleDisplayFrame(outRect);
        assertEquals(0, outRect.left);
        assertEquals(0, outRect.top);
        assertEquals(d.getWidth(), outRect.right);
        assertEquals(d.getHeight(), outRect.bottom);

        // mAttachInfo is not null
        outRect = new Rect();
        view = mActivity.findViewById(R.id.fit_windows);
        // it's implementation detail
        view.getWindowVisibleDisplayFrame(outRect);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setScrollContainer",
        args = {boolean.class}
    )
    public void testSetScrollContainer() throws Throwable {
        final MockView mockView = (MockView) mActivity.findViewById(R.id.mock_view);
        final MockView scrollView = (MockView) mActivity.findViewById(R.id.scroll_view);
        Bitmap bitmap = Bitmap.createBitmap(200, 300, Bitmap.Config.RGB_565);
        final BitmapDrawable d = new BitmapDrawable(bitmap);
        final InputMethodManager imm = InputMethodManager.getInstance(getActivity());
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 500);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mockView.setBackgroundDrawable(d);
                mockView.setHorizontalFadingEdgeEnabled(true);
                mockView.setVerticalFadingEdgeEnabled(true);
                mockView.setLayoutParams(layoutParams);
                scrollView.setLayoutParams(layoutParams);

                mockView.setFocusable(true);
                mockView.requestFocus();
                mockView.setScrollContainer(true);
                scrollView.setScrollContainer(false);
                imm.showSoftInput(mockView, 0);
            }
        });
        getInstrumentation().waitForIdleSync();

        // FIXME: why the size of view doesn't change?

        runTestOnUiThread(new Runnable() {
            public void run() {
                imm.hideSoftInputFromInputMethod(mockView.getWindowToken(), 0);
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFocusableInTouchMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusableInTouchMode",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isInTouchMode",
            args = {}
        )
    })
    public void testTouchMode() throws Throwable {
        final MockView mockView = (MockView) mActivity.findViewById(R.id.mock_view);
        final View fitWindowsView = mActivity.findViewById(R.id.fit_windows);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mockView.setFocusableInTouchMode(true);
                fitWindowsView.setFocusable(true);
                fitWindowsView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.isFocusableInTouchMode());
        assertFalse(fitWindowsView.isFocusableInTouchMode());
        assertTrue(mockView.isFocusable());
        assertTrue(fitWindowsView.isFocusable());
        assertFalse(mockView.isFocused());
        assertTrue(fitWindowsView.isFocused());
        assertFalse(mockView.isInTouchMode());
        assertFalse(fitWindowsView.isInTouchMode());

        TouchUtils.tapView(this, mockView);
        assertFalse(fitWindowsView.isFocused());
        assertFalse(mockView.isFocused());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mockView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mockView.isFocused());
        runTestOnUiThread(new Runnable() {
            public void run() {
                fitWindowsView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(fitWindowsView.isFocused());
        assertTrue(mockView.isInTouchMode());
        assertTrue(fitWindowsView.isInTouchMode());

        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        getInstrumentation().sendKeySync(keyEvent);
        assertTrue(mockView.isFocused());
        assertFalse(fitWindowsView.isFocused());
        runTestOnUiThread(new Runnable() {
            public void run() {
                fitWindowsView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(mockView.isFocused());
        assertTrue(fitWindowsView.isFocused());
        assertFalse(mockView.isInTouchMode());
        assertFalse(fitWindowsView.isInTouchMode());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalScrollBarEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalScrollBarEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHorizontalScrollBarEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isVerticalScrollBarEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVerticalScrollbarWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHorizontalScrollbarHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "setScrollBarStyle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScrollBarStyle",
            args = {}
        )
    })
    @ToBeFixed(bug = "", explanation = "when scrollbar is with INSET style, the bottom padding" +
            "should be increased.")
    @UiThreadTest
    public void testScrollbarStyle() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        Bitmap bitmap = Bitmap.createBitmap(200, 300, Bitmap.Config.RGB_565);
        BitmapDrawable d = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(d);
        view.setHorizontalFadingEdgeEnabled(true);
        view.setVerticalFadingEdgeEnabled(true);

        view.setHorizontalScrollBarEnabled(true);
        view.setVerticalScrollBarEnabled(true);
        view.initializeScrollbars(mActivity.obtainStyledAttributes(android.R.styleable.View));
        assertTrue(view.isHorizontalScrollBarEnabled());
        assertTrue(view.isVerticalScrollBarEnabled());
        int verticalScrollBarWidth = view.getVerticalScrollbarWidth();
        int horizontalScrollBarHeight = view.getHorizontalScrollbarHeight();
        assertTrue(verticalScrollBarWidth > 0);
        assertTrue(horizontalScrollBarHeight > 0);
        assertEquals(0, view.getPaddingRight());
        assertEquals(0, view.getPaddingBottom());

        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        assertEquals(View.SCROLLBARS_INSIDE_INSET, view.getScrollBarStyle());
        assertEquals(verticalScrollBarWidth, view.getPaddingRight());
        // issue, mockView.getPaddingBottom() is expected to equal horizontalScrollBarHeight.
        assertEquals(0, view.getPaddingBottom());

        view.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        assertEquals(View.SCROLLBARS_OUTSIDE_OVERLAY, view.getScrollBarStyle());
        assertEquals(0, view.getPaddingRight());
        assertEquals(0, view.getPaddingBottom());

        view.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        assertEquals(View.SCROLLBARS_OUTSIDE_INSET, view.getScrollBarStyle());
        assertEquals(verticalScrollBarWidth, view.getPaddingRight());
        // issue, mockView.getPaddingBottom() is expected to equal horizontalScrollBarHeight.
        assertEquals(0, view.getPaddingBottom());

        // TODO: how to get the position of the Scrollbar to assert it is inside or outside.
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHorizontalFadingEdgeEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isVerticalFadingEdgeEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHorizontalFadingEdgeLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVerticalFadingEdgeLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalFadingEdgeEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalFadingEdgeEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFadingEdgeLength",
            args = {int.class}
        )
    })
    @UiThreadTest
    public void testScrollFading() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        Bitmap bitmap = Bitmap.createBitmap(200, 300, Bitmap.Config.RGB_565);
        BitmapDrawable d = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(d);

        assertFalse(view.isHorizontalFadingEdgeEnabled());
        assertFalse(view.isVerticalFadingEdgeEnabled());
        assertEquals(0, view.getHorizontalFadingEdgeLength());
        assertEquals(0, view.getVerticalFadingEdgeLength());

        view.setHorizontalFadingEdgeEnabled(true);
        view.setVerticalFadingEdgeEnabled(true);
        assertTrue(view.isHorizontalFadingEdgeEnabled());
        assertTrue(view.isVerticalFadingEdgeEnabled());
        assertTrue(view.getHorizontalFadingEdgeLength() > 0);
        assertTrue(view.getVerticalFadingEdgeLength() > 0);

        final int fadingLength = 20;
        view.setFadingEdgeLength(fadingLength);
        assertEquals(fadingLength, view.getHorizontalFadingEdgeLength());
        assertEquals(fadingLength, view.getVerticalFadingEdgeLength());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "scrollBy",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "scrollTo",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScrollX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScrollY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onScrollChanged",
            args = {int.class, int.class, int.class, int.class}
        )
    })
    @UiThreadTest
    public void testScrolling() {
        MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        view.reset();
        assertEquals(0, view.getScrollX());
        assertEquals(0, view.getScrollY());
        assertFalse(view.hasCalledOnScrollChanged());
        assertFalse(view.hasCalledInvalidate());

        view.scrollTo(0, 0);
        assertEquals(0, view.getScrollX());
        assertEquals(0, view.getScrollY());
        assertFalse(view.hasCalledOnScrollChanged());
        assertFalse(view.hasCalledInvalidate());

        view.scrollBy(0, 0);
        assertEquals(0, view.getScrollX());
        assertEquals(0, view.getScrollY());
        assertFalse(view.hasCalledOnScrollChanged());
        assertFalse(view.hasCalledInvalidate());

        view.scrollTo(10, 100);
        assertEquals(10, view.getScrollX());
        assertEquals(100, view.getScrollY());
        assertTrue(view.hasCalledOnScrollChanged());
        assertTrue(view.hasCalledInvalidate());

        view.reset();
        assertFalse(view.hasCalledOnScrollChanged());
        view.scrollBy(-10, -100);
        assertEquals(0, view.getScrollX());
        assertEquals(0, view.getScrollY());
        assertTrue(view.hasCalledOnScrollChanged());
        assertTrue(view.hasCalledInvalidate());

        view.reset();
        assertFalse(view.hasCalledOnScrollChanged());
        view.scrollTo(-1, -2);
        assertEquals(-1, view.getScrollX());
        assertEquals(-2, view.getScrollY());
        assertTrue(view.hasCalledOnScrollChanged());
        assertTrue(view.hasCalledInvalidate());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "initializeScrollbars",
            args = {android.content.res.TypedArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "initializeFadingEdge",
            args = {android.content.res.TypedArray.class}
        )
    })
    public void testInitializeScrollbarsAndFadingEdge() {
        MockView view = (MockView) mActivity.findViewById(R.id.scroll_view);
        final int fadingEdgeLength = 20;

        assertTrue(view.isHorizontalScrollBarEnabled());
        assertTrue(view.isVerticalScrollBarEnabled());
        assertTrue(view.isHorizontalFadingEdgeEnabled());
        assertTrue(view.isVerticalFadingEdgeEnabled());
        assertEquals(fadingEdgeLength, view.getHorizontalFadingEdgeLength());
        assertEquals(fadingEdgeLength, view.getVerticalFadingEdgeLength());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStartTemporaryDetach",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFinishTemporaryDetach",
            args = {}
        )
    })
    @ToBeFixed(bug = "", explanation = "onStartTemporaryDetach and onFinishTemporaryDetach" +
            " is not called when ListView is going to temporarily detach a child that currently" +
            " has focus, with detachViewFromParent.")
    public void testOnStartAndFinishTemporaryDetach() throws Throwable {
        final MockListView listView = new MockListView(mActivity);
        List<String> items = Lists.newArrayList("1", "2", "3");
        final Adapter<String> adapter = new Adapter<String>(mActivity, 0, items);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.setContentView(listView);
                listView.setAdapter(adapter);
            }
        });
        getInstrumentation().waitForIdleSync();
        final MockView focusChild = (MockView) listView.getChildAt(0);

        runTestOnUiThread(new Runnable() {
            public void run() {
                focusChild.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(listView.getChildAt(0).isFocused());

        runTestOnUiThread(new Runnable() {
            public void run() {
                listView.detachViewFromParent(focusChild);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(listView.hasCalledOnStartTemporaryDetach());
        assertFalse(listView.hasCalledOnFinishTemporaryDetach());
    }

    private static class MockListView extends ListView {
        private boolean mCalledOnStartTemporaryDetach = false;
        private boolean mCalledOnFinishTemporaryDetach = false;

        public MockListView(Context context) {
            super(context);
        }

        @Override
        protected void detachViewFromParent(View child) {
            super.detachViewFromParent(child);
        }

        @Override
        public void onFinishTemporaryDetach() {
            super.onFinishTemporaryDetach();
            mCalledOnFinishTemporaryDetach = true;
        }

        public boolean hasCalledOnFinishTemporaryDetach() {
            return mCalledOnFinishTemporaryDetach;
        }

        @Override
        public void onStartTemporaryDetach() {
            super.onStartTemporaryDetach();
            mCalledOnStartTemporaryDetach = true;
        }

        public boolean hasCalledOnStartTemporaryDetach() {
            return mCalledOnStartTemporaryDetach;
        }

        public void reset() {
            mCalledOnStartTemporaryDetach = false;
            mCalledOnFinishTemporaryDetach = false;
        }
    }

    private static class Adapter<T> extends ArrayAdapter<T> {
        ArrayList<MockView> views = new ArrayList<MockView>();

        public Adapter(Context context, int textViewResourceId, List<T> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); i++) {
                views.add(new MockView(context));
                views.get(i).setFocusable(true);
            }
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return views.get(position);
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEventPreIme",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyPreIme",
            args = {int.class, KeyEvent.class}
        )
    })
    public void testKeyPreIme() throws Throwable {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);

        runTestOnUiThread(new Runnable() {
            public void run() {
                view.setFocusable(true);
                view.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        getInstrumentation().sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        assertTrue(view.hasCalledDispatchKeyEventPreIme());
        assertTrue(view.hasCalledOnKeyPreIme());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHapticFeedbackEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHapticFeedbackEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performHapticFeedback",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performHapticFeedback",
            args = {int.class, int.class}
        )
    })
    public void testHapticFeedback() {
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        final int LONG_PRESS = HapticFeedbackConstants.LONG_PRESS;
        final int FLAG_IGNORE_VIEW_SETTING = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
        final int FLAG_IGNORE_GLOBAL_SETTING = HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
        final int ALWAYS = FLAG_IGNORE_VIEW_SETTING | FLAG_IGNORE_GLOBAL_SETTING;

        view.setHapticFeedbackEnabled(false);
        assertFalse(view.isHapticFeedbackEnabled());
        assertFalse(view.performHapticFeedback(LONG_PRESS));
        assertFalse(view.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING));
        assertTrue(view.performHapticFeedback(LONG_PRESS, ALWAYS));

        view.setHapticFeedbackEnabled(true);
        assertTrue(view.isHapticFeedbackEnabled());
        assertTrue(view.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateInputConnection",
            args = {EditorInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "checkInputConnectionProxy",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCheckIsTextEditor",
            args = {}
        )
    })
    @UiThreadTest
    public void testInputConnection() {
        final InputMethodManager imm = InputMethodManager.getInstance(getActivity());
        final MockView view = (MockView) mActivity.findViewById(R.id.mock_view);
        final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(R.id.viewlayout_root);
        final MockEditText editText = new MockEditText(mActivity);

        viewGroup.addView(editText);
        editText.requestFocus();

        new DelayedCheck(TIMEOUT_DELTA) {
            @Override
            protected boolean check() {
                return editText.isFocused();
            }
        }.run();

        imm.showSoftInput(editText, 0);
        assertTrue(editText.hasCalledOnCreateInputConnection());
        assertTrue(editText.hasCalledOnCheckIsTextEditor());
        assertTrue(imm.isActive(editText));

        assertFalse(editText.hasCalledCheckInputConnectionProxy());
        imm.isActive(view);
        assertTrue(editText.hasCalledCheckInputConnectionProxy());
    }

    private static class MockEditText extends EditText {
        private boolean mCalledCheckInputConnectionProxy = false;
        private boolean mCalledOnCreateInputConnection = false;
        private boolean mCalledOnCheckIsTextEditor = false;

        public MockEditText(Context context) {
            super(context);
        }

        @Override
        public boolean checkInputConnectionProxy(View view) {
            mCalledCheckInputConnectionProxy = true;
            return super.checkInputConnectionProxy(view);
        }

        public boolean hasCalledCheckInputConnectionProxy() {
            return mCalledCheckInputConnectionProxy;
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            mCalledOnCreateInputConnection = true;
            return super.onCreateInputConnection(outAttrs);
        }

        public boolean hasCalledOnCreateInputConnection() {
            return mCalledOnCreateInputConnection;
        }

        @Override
        public boolean onCheckIsTextEditor() {
            mCalledOnCheckIsTextEditor = true;
            return super.onCheckIsTextEditor();
        }

        public boolean hasCalledOnCheckIsTextEditor() {
            return mCalledOnCheckIsTextEditor;
        }

        public void reset() {
            mCalledCheckInputConnectionProxy = false;
            mCalledOnCreateInputConnection = false;
            mCalledOnCheckIsTextEditor = false;
        }
    }

    private final static class MockViewParent extends View implements ViewParent {
        private boolean mHasClearChildFocus = false;
        private boolean mHasRequestLayout = false;
        private boolean mHasCreateContextMenu = false;
        private boolean mHasShowContextMenuForChild = false;
        private boolean mHasGetChildVisibleRect = false;
        private boolean mHasInvalidateChild = false;
        private boolean mHasOnCreateDrawableState = false;
        private boolean mHasChildDrawableStateChanged = false;
        private boolean mHasBroughtChildToFront = false;
        private Rect mTempRect;

        private final static int[] DEFAULT_PARENT_STATE_SET = new int[] { 789 };

        public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
                boolean immediate) {
            return false;
        }

        public MockViewParent(Context context) {
            super(context);
        }

        public void bringChildToFront(View child) {
            mHasBroughtChildToFront = true;
        }

        public boolean hasBroughtChildToFront() {
            return mHasBroughtChildToFront;
        }

        public void childDrawableStateChanged(View child) {
            mHasChildDrawableStateChanged = true;
        }

        public boolean hasChildDrawableStateChanged() {
            return mHasChildDrawableStateChanged;
        }

        @Override
        protected void dispatchSetPressed(boolean pressed) {
            super.dispatchSetPressed(pressed);
        }

        @Override
        protected void dispatchSetSelected(boolean selected) {
            super.dispatchSetSelected(selected);
        }

        public void clearChildFocus(View child) {
            mHasClearChildFocus = true;
        }

        public boolean hasClearChildFocus() {
            return mHasClearChildFocus;
        }

        public void createContextMenu(ContextMenu menu) {
            mHasCreateContextMenu = true;
        }

        public boolean hasCreateContextMenu() {
            return mHasCreateContextMenu;
        }

        public View focusSearch(View v, int direction) {
            return v;
        }

        public void focusableViewAvailable(View v) {

        }

        public boolean getChildVisibleRect(View child, Rect r, Point offset) {
            mHasGetChildVisibleRect = true;
            return false;
        }

        public boolean hasGetChildVisibleRect() {
            return mHasGetChildVisibleRect;
        }

        public void invalidateChild(View child, Rect r) {
            mTempRect = new Rect(r);
            mHasInvalidateChild = true;
        }

        public Rect getTempRect() {
            return mTempRect;
        }

        public boolean hasInvalidateChild() {
            return mHasInvalidateChild;
        }

        public ViewParent invalidateChildInParent(int[] location, Rect r) {
            return null;
        }

        public boolean isLayoutRequested() {
            return false;
        }

        public void recomputeViewAttributes(View child) {

        }

        public void requestChildFocus(View child, View focused) {

        }

        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        public void requestLayout() {
            mHasRequestLayout = true;
        }

        public boolean hasRequestLayout() {
            return mHasRequestLayout;
        }

        public void requestTransparentRegion(View child) {

        }

        public boolean showContextMenuForChild(View originalView) {
            mHasShowContextMenuForChild = true;
            return false;
        }

        public boolean hasShowContextMenuForChild() {
            return mHasShowContextMenuForChild;
        }

        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            mHasOnCreateDrawableState = true;
            return DEFAULT_PARENT_STATE_SET;
        }

        public static int[] getDefaultParentStateSet() {
            return DEFAULT_PARENT_STATE_SET;
        }

        public boolean hasOnCreateDrawableState() {
            return mHasOnCreateDrawableState;
        }

        public void reset() {
            mHasClearChildFocus = false;
            mHasRequestLayout = false;
            mHasCreateContextMenu = false;
            mHasShowContextMenuForChild = false;
            mHasGetChildVisibleRect = false;
            mHasInvalidateChild = false;
            mHasOnCreateDrawableState = false;
            mHasChildDrawableStateChanged = false;
            mHasBroughtChildToFront = false;
        }

        public void childOverlayStateChanged(View child) {

        }
    }

    private final class OnCreateContextMenuListenerImpl implements OnCreateContextMenuListener {
        private boolean mHasOnCreateContextMenu = false;

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            mHasOnCreateContextMenu = true;
        }

        public boolean hasOnCreateContextMenu() {
            return mHasOnCreateContextMenu;
        }

        public void reset() {
            mHasOnCreateContextMenu = false;
        }
    }

    private final static class MockViewGroupParent extends ViewGroup implements ViewParent {
        private boolean mHasRequestChildRectangleOnScreen = false;

        public MockViewGroupParent(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }

        @Override
        public boolean requestChildRectangleOnScreen(View child,
                Rect rectangle, boolean immediate) {
            mHasRequestChildRectangleOnScreen = true;
            return super.requestChildRectangleOnScreen(child, rectangle, immediate);
        }

        public boolean hasRequestChildRectangleOnScreen() {
            return mHasRequestChildRectangleOnScreen;
        }

        @Override
        protected void detachViewFromParent(View child) {
            super.detachViewFromParent(child);
        }

        public void reset() {
            mHasRequestChildRectangleOnScreen = false;
        }
    }

    private static final class OnClickListenerImpl implements OnClickListener {
        private boolean mHasOnClick = false;

        public void onClick(View v) {
            mHasOnClick = true;
        }

        public boolean hasOnClick() {
            return mHasOnClick;
        }

        public void reset() {
            mHasOnClick = false;
        }
    }

    private static final class OnLongClickListenerImpl implements OnLongClickListener {
        private boolean mHasOnLongClick = false;

        public boolean hasOnLongClick() {
            return mHasOnLongClick;
        }

        public void reset() {
            mHasOnLongClick = false;
        }

        public boolean onLongClick(View v) {
            mHasOnLongClick = true;
            return true;
        }
    }

    private static final class OnFocusChangeListenerImpl implements OnFocusChangeListener {
        private boolean mHasOnFocusChange = false;

        public void onFocusChange(View v, boolean hasFocus) {
            mHasOnFocusChange = true;
        }

        public boolean hasOnFocusChange() {
            return mHasOnFocusChange;
        }

        public void reset() {
            mHasOnFocusChange = false;
        }
    }

    private static final class OnKeyListenerImpl implements OnKeyListener {
        private boolean mHasOnKey = false;

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            mHasOnKey = true;
            return true;
        }

        public void reset() {
            mHasOnKey = false;
        }

        public boolean hasOnKey() {
            return mHasOnKey;
        }
    }

    private static final class OnTouchListenerImpl implements OnTouchListener {
        private boolean mHasOnTouch = false;

        public boolean onTouch(View v, MotionEvent event) {
            mHasOnTouch = true;
            return true;
        }

        public void reset() {
            mHasOnTouch = false;
        }

        public boolean hasOnTouch() {
            return mHasOnTouch;
        }
    }

    private static final class MockTouchDelegate extends TouchDelegate {
        public MockTouchDelegate(Rect bounds, View delegateView) {
            super(bounds, delegateView);
        }

        private boolean mCalledOnTouchEvent = false;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mCalledOnTouchEvent = true;
            return super.onTouchEvent(event);
        }

        public boolean hasCalledOnTouchEvent() {
            return mCalledOnTouchEvent;
        }

        public void reset() {
            mCalledOnTouchEvent = false;
        }
    };

    private static final class ViewData {
        public int childCount;
        public String tag;
        public View firstChild;
    }

    private static final class MockRunnable implements Runnable {
        public boolean hasRun = false;

        public void run() {
            hasRun = true;
        }
    }
}
