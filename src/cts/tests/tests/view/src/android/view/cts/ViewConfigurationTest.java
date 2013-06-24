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
import android.test.InstrumentationTestCase;
import android.view.ViewConfiguration;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;

/**
 * Test {@link ViewConfiguration}.
 */
@TestTargetClass(ViewConfiguration.class)
public class ViewConfigurationTest extends InstrumentationTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScrollBarSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getFadingEdgeLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getPressedStateDuration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getLongPressTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getTapTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getJumpTapTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getEdgeSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getTouchSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getWindowTouchSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getMinimumFlingVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getMaximumDrawingCacheSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getZoomControlsTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getGlobalActionKeyTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScrollFriction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getDoubleTapTimeout",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testStaticValues() {
        ViewConfiguration.getScrollBarSize();
        ViewConfiguration.getFadingEdgeLength();
        ViewConfiguration.getPressedStateDuration();
        ViewConfiguration.getLongPressTimeout();
        ViewConfiguration.getTapTimeout();
        ViewConfiguration.getJumpTapTimeout();
        ViewConfiguration.getEdgeSlop();
        ViewConfiguration.getTouchSlop();
        ViewConfiguration.getWindowTouchSlop();
        ViewConfiguration.getMinimumFlingVelocity();
        ViewConfiguration.getMaximumDrawingCacheSize();
        ViewConfiguration.getZoomControlsTimeout();
        ViewConfiguration.getGlobalActionKeyTimeout();
        ViewConfiguration.getScrollFriction();
        ViewConfiguration.getDoubleTapTimeout();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "ViewConfiguration",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testConstructor() {
        new ViewConfiguration();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "get",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledDoubleTapSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledEdgeSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledFadingEdgeLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledMaximumDrawingCacheSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledMinimumFlingVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledScrollBarSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledTouchSlop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getScaledWindowTouchSlop",
            args = {}
        )
    })
    public void testInstanceValues() {
        ViewConfiguration vc = ViewConfiguration.get(getInstrumentation().getTargetContext());
        assertNotNull(vc);
        vc.getScaledDoubleTapSlop();
        vc.getScaledEdgeSlop();
        vc.getScaledFadingEdgeLength();
        vc.getScaledMaximumDrawingCacheSize();
        vc.getScaledMinimumFlingVelocity();
        vc.getScaledScrollBarSize();
        vc.getScaledTouchSlop();
        vc.getScaledWindowTouchSlop();
    }
}
