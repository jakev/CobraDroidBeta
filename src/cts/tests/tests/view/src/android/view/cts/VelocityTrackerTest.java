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

import android.test.AndroidTestCase;
import android.view.MotionEvent;
import android.view.VelocityTracker;
/**
 * Test {@link VelocityTracker}.
 */
@TestTargetClass(VelocityTracker.class)
public class VelocityTrackerTest extends AndroidTestCase {
    private static final float ERROR_TOLERANCE = 0.0001f;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "obtain",
        args = {}
    )
    public void testObtain() {
        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);
        vt.recycle();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "recycle",
        args = {}
    )
    public void testRecycle() {
        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);
        vt.recycle();

        VelocityTracker vt2 = VelocityTracker.obtain();
        assertEquals(vt, vt2);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeCurrentVelocity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getXVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getYVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addMovement",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clear",
            args = {}
        )
    })
    public void testComputeCurrentVelocity() {
        // XVelocity & YVelocity calculated by the original algorithm from android
        float XVelocity;
        float YVelocity;

        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);

        MotionEvent me = MotionEvent.obtain(0L, 10, 1, .0f, .0f, 0);

        vt.clear();
        me.addBatch(20L, 20, 20, .0f, .0f, 0);
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 2.0f;
        YVelocity = 2.0f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);
        vt.computeCurrentVelocity(10);
        XVelocity = 20.0f;
        YVelocity = 20.0f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);

        for (int i = 30; i < 100; i += 10) {
            me.addBatch((long)i, (float)i, (float)i, .0f, .0f, 0);
        }
        vt.clear();
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 1.1875744f;
        YVelocity = 1.1875744f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);

        vt.clear();
        me.addBatch(100L, 100, 100, .0f, .0f, 0);
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 1.1562872f;
        YVelocity = 1.1562872f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);

        vt.recycle();
    }
}
