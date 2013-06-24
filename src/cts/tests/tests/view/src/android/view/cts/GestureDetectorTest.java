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

import android.content.Context;
import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(GestureDetector.class)
public class GestureDetectorTest extends
        ActivityInstrumentationTestCase2<GestureDetectorStubActivity> {

    private GestureDetector mGestureDetector;
    private GestureDetectorStubActivity mActivity;
    private Context mContext;

    public GestureDetectorTest() {
        super("com.android.cts.stub", GestureDetectorStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGestureDetector = mActivity.getGestureDetector();
        mContext = getInstrumentation().getTargetContext();
        mActivity.isDown = false;
        mActivity.isScroll = false;
        mActivity.isFling = false;
        mActivity.isSingleTapUp = false;
        mActivity.onShowPress = false;
        mActivity.onLongPress = false;
        mActivity.onDoubleTap = false;
        mActivity.onDoubleTapEvent = false;
        mActivity.onSingleTapConfirmed = false;
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {OnGestureListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {OnGestureListener.class, Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {Context.class, OnGestureListener.class, Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {Context.class, OnGestureListener.class}
         )
    })
    public void testConstructor() {

        new GestureDetector(mContext, new SimpleOnGestureListener(), new Handler());
        new GestureDetector(mContext, new SimpleOnGestureListener());
        new GestureDetector(new SimpleOnGestureListener(), new Handler());
        new GestureDetector(new SimpleOnGestureListener());

        try {
            mGestureDetector = new GestureDetector(null);
            fail("should throw null exception");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method onTouchEvent",
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method onTouchEvent",
            method = "setOnDoubleTapListener",
            args = {OnDoubleTapListener.class}
        )
    })
    public void testOnTouchEvent() {
        GestureDetectorTestUtil.testGestureDetector(this, mActivity);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setIsLongpressEnabled",
            method = "setIsLongpressEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setIsLongpressEnabled",
            method = "isLongpressEnabled",
            args = {}
        )
    })
    public void testLongpressEnabled() {
        mGestureDetector.setIsLongpressEnabled(true);
        assertTrue(mGestureDetector.isLongpressEnabled());
        mGestureDetector.setIsLongpressEnabled(false);
        assertFalse(mGestureDetector.isLongpressEnabled());
    }
}
