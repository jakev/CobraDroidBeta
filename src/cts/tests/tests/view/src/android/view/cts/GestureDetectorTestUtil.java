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

import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.View;

class GestureDetectorTestUtil {

    /**
     * Test GestureDetector.SimpleOnGestureListener
     * @param testcase InstrumentationTestCase
     * @param activity GestureDetectorStubActivity
     */
    public static void testGestureDetector(InstrumentationTestCase testcase,
            GestureDetectorStubActivity activity) {
        View view = activity.getView();
        TouchUtils.clickView(testcase, view);
        TouchUtils.longClickView(testcase, view);
        TouchUtils.scrollToBottom(testcase, activity, activity.getViewGroup());
        TouchUtils.touchAndCancelView(testcase, view);
        int fromX = 1;
        int toX = 10;
        // Y has to be outside the status bar bounding box
        int fromY = 50;
        int toY = 100;
        int stepCount = 20;
        TouchUtils.drag(testcase, fromX, toX, fromY, toY, stepCount);
        InstrumentationTestCase.assertTrue(activity.isDown);
        InstrumentationTestCase.assertTrue(activity.isScroll);
        InstrumentationTestCase.assertTrue(activity.isFling);
        InstrumentationTestCase.assertTrue(activity.isSingleTapUp);
        InstrumentationTestCase.assertTrue(activity.onLongPress);
        InstrumentationTestCase.assertTrue(activity.onShowPress);
        InstrumentationTestCase.assertTrue(activity.onSingleTapConfirmed);

        // Test onDoubleTap
        InstrumentationTestCase.assertFalse(activity.onDoubleTap);
        InstrumentationTestCase.assertFalse(activity.onDoubleTapEvent);
        TouchUtils.tapView(testcase, view);
        TouchUtils.tapView(testcase, view);
        InstrumentationTestCase.assertTrue(activity.onDoubleTap);
        InstrumentationTestCase.assertTrue(activity.onDoubleTapEvent);
    }
}
