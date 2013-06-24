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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

@TestTargetClass(SimpleOnGestureListener.class)
public class GestureDetector_SimpleOnGestureListenerTest extends
        ActivityInstrumentationTestCase2<GestureDetectorStubActivity> {

    private GestureDetectorStubActivity mActivity;

    public GestureDetector_SimpleOnGestureListenerTest() {
        super("com.android.cts.stub", GestureDetectorStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
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

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test Constructor ",
        method = "GestureDetector.SimpleOnGestureListener",
        args = {}
    )
    public void testConstructor() {
        new SimpleOnGestureListener();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSingleTapUp",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLongPress",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDown",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onScroll",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onShowPress",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFling",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDoubleTap",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDoubleTapEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSingleTapConfirmed",
            args = {MotionEvent.class}
        )
    })
    public void testSimpleOnGestureListener() {
        GestureDetectorTestUtil.testGestureDetector(this, mActivity);
    }

}
