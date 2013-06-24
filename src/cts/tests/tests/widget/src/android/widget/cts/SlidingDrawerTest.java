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

package android.widget.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.cts.DelayedCheck;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;

import java.io.IOException;

/**
 * Test {@link SlidingDrawer}.
 */
@TestTargetClass(SlidingDrawer.class)
public class SlidingDrawerTest
        extends ActivityInstrumentationTestCase2<SlidingDrawerStubActivity> {

    private static final long TEST_TIMEOUT = 5000L;
    private Activity mActivity;
    private Object mLock;

    public SlidingDrawerTest() {
        super("com.android.cts.stub", SlidingDrawerStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mLock = new Object();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SlidingDrawer",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SlidingDrawer",
            args = {Context.class, AttributeSet.class, int.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        XmlPullParser parser = mActivity.getResources().getLayout(R.layout.sliding_drawer_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        try {
            new SlidingDrawer(mActivity, attrs);
            fail("did not throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new SlidingDrawer(mActivity, attrs, 0);
            fail("did not throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHandle",
        args = {}
    )
    public void testGetHandle() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View handle = drawer.getHandle();
        assertTrue(handle instanceof ImageView);
        assertEquals(R.id.handle, handle.getId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContent",
        args = {}
    )
    public void testGetContent() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View content = drawer.getContent();
        assertTrue(content instanceof TextView);
        assertEquals(R.id.content, content.getId());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOpened",
            args = {}
        )
    })
    @UiThreadTest
    public void testOpenAndClose() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View content = drawer.getContent();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        drawer.open();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());

        drawer.close();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "animateOpen",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "animateClose",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOpened",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMoving",
            args = {}
        )
    })
    public void testAnimateOpenAndClose() throws Throwable {
        final SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View content = drawer.getContent();
        assertFalse(drawer.isMoving());
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        runTestOnUiThread(new Runnable() {
            public void run() {
                drawer.animateOpen();
            }
        });
        assertTrue(drawer.isMoving());
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        new DelayedCheck() {
            @Override
            protected boolean check() {
                return !drawer.isMoving();
            }
        }.run();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());

        runTestOnUiThread(new Runnable() {
            public void run() {
                drawer.animateClose();
            }
        });
        assertTrue(drawer.isMoving());
        assertTrue(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        new DelayedCheck() {
            @Override
            protected boolean check() {
                return !drawer.isMoving();
            }
        }.run();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "animateToggle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOpened",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMoving",
            args = {}
        )
    })
    public void testAnimateToggle() throws Throwable {
        final SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View content = drawer.getContent();
        assertFalse(drawer.isMoving());
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        runTestOnUiThread(new Runnable() {
            public void run() {
                drawer.animateToggle();
            }
        });
        assertTrue(drawer.isMoving());
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        new DelayedCheck() {
            @Override
            protected boolean check() {
                return !drawer.isMoving();
            }
        }.run();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());

        runTestOnUiThread(new Runnable() {
            public void run() {
                drawer.animateToggle();
            }
        });
        assertTrue(drawer.isMoving());
        assertTrue(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        new DelayedCheck() {
            @Override
            protected boolean check() {
                return !drawer.isMoving();
            }
        }.run();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toggle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOpened",
            args = {}
        )
    })
    @UiThreadTest
    public void testToggle() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View content = drawer.getContent();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        drawer.toggle();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());

        drawer.toggle();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "lock",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unlock",
            args = {}
        )
    })
    @UiThreadTest
    public void testLockAndUnlock() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        View handle = drawer.getHandle();
        View content = drawer.getContent();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        handle.performClick();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());

        handle.performClick();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        drawer.lock();
        handle.performClick();
        assertFalse(drawer.isOpened());
        assertEquals(View.GONE, content.getVisibility());

        drawer.unlock();
        handle.performClick();
        assertTrue(drawer.isOpened());
        assertEquals(View.VISIBLE, content.getVisibility());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnDrawerOpenListener",
        args = {android.widget.SlidingDrawer.OnDrawerOpenListener.class}
    )
    @UiThreadTest
    public void testSetOnDrawerOpenListener() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        MockOnDrawerOpenListener listener = new MockOnDrawerOpenListener();
        drawer.setOnDrawerOpenListener(listener);

        assertFalse(listener.hadOpenedDrawer());

        drawer.open();
        assertTrue(listener.hadOpenedDrawer());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnDrawerCloseListener",
        args = {android.widget.SlidingDrawer.OnDrawerCloseListener.class}
    )
    @UiThreadTest
    public void testSetOnDrawerCloseListener() {
        SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        MockOnDrawerCloseListener listener = new MockOnDrawerCloseListener();
        drawer.setOnDrawerCloseListener(listener);

        assertFalse(listener.hadClosedDrawer());

        drawer.open();
        assertFalse(listener.hadClosedDrawer());

        drawer.close();
        assertTrue(listener.hadClosedDrawer());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnDrawerScrollListener",
        args = {android.widget.SlidingDrawer.OnDrawerScrollListener.class}
    )
    public void testSetOnDrawerScrollListener() throws Throwable {
        final SlidingDrawer drawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
        MockOnDrawerScrollListener listener = new MockOnDrawerScrollListener();
        drawer.setOnDrawerScrollListener(listener);
        assertFalse(listener.hadStartedScroll());
        assertFalse(listener.hadEndedScroll());

        runTestOnUiThread(new Runnable() {
            public void run() {
                drawer.animateOpen();
            }
        });
        if ( !listener.hadStartedScroll() ) {
            synchronized (mLock) {
                mLock.wait(TEST_TIMEOUT);
            }
        }
        assertTrue(listener.hadStartedScroll());

        if ( !listener.hadEndedScroll() ) {
            synchronized (mLock) {
                mLock.wait(TEST_TIMEOUT);
            }
        }
        assertTrue(listener.hadStartedScroll());
        assertTrue(listener.hadEndedScroll());
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onLayout",
        args = {boolean.class, int.class, int.class, int.class, int.class}
    )
    public void testOnLayout() {
        // onLayout() is implementation details, do NOT test
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
        // onMeasure() is implementation details, do NOT test
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onFinishInflate",
        args = {}
    )
    public void testOnFinishInflate() {
        // onFinishInflate() is implementation details, do NOT test
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "dispatchDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testDispatchDraw() {
        // dispatchDraw() is implementation details, do NOT test
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onInterceptTouchEvent",
        args = {MotionEvent.class}
    )
    public void testOnInterceptTouchEvent() {
        // onInterceptTouchEvent() is implementation details, do NOT test
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onTouchEvent",
        args = {MotionEvent.class}
    )
    public void testOnTouchEvent() {
        // onTouchEvent() is implementation details, do NOT test
    }

    private static final class MockOnDrawerOpenListener implements OnDrawerOpenListener {
        private boolean mHadOpenedDrawer = false;

        public void onDrawerOpened() {
            mHadOpenedDrawer = true;
        }

        public boolean hadOpenedDrawer() {
            return mHadOpenedDrawer;
        }
    }

    private static final class MockOnDrawerCloseListener implements OnDrawerCloseListener {
        private boolean mHadClosedDrawer = false;

        public void onDrawerClosed() {
            mHadClosedDrawer = true;
        }

        public boolean hadClosedDrawer() {
            return mHadClosedDrawer;
        }
    }

    private final class MockOnDrawerScrollListener implements OnDrawerScrollListener {
        private boolean mHadEndedScroll = false;
        private boolean mHadStartedScroll = false;

        public void onScrollEnded() {
            synchronized (mLock) {
                assertTrue(mHadStartedScroll);
                mHadEndedScroll = true;
                mLock.notify();
            }
        }

        public void onScrollStarted() {
            synchronized (mLock) {
                mHadStartedScroll = true;
                mLock.notify();
            }
        }

        public boolean hadEndedScroll() {
            return mHadEndedScroll;
        }

        public boolean hadStartedScroll() {
            return mHadStartedScroll;
        }
    }
}
