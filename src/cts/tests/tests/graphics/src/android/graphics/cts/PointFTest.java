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

package android.graphics.cts;

import android.graphics.Point;
import android.graphics.PointF;
import android.test.AndroidTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(PointF.class)
public class PointFTest extends AndroidTestCase {

    private PointF mPointF;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPointF = null;
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PointF",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PointF",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PointF",
            args = {android.graphics.Point.class}
        )
    })
    public void testConstructor() {

        mPointF = null;
        // new the PointF instance
        mPointF = new PointF();

        mPointF = null;
        // new the PointF instance
        mPointF = new PointF(10.0f, 10.0f);

        mPointF = null;
        Point point = new Point(10, 10);
        // new the PointF instance
        mPointF = new PointF(point);

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "negate",
        args = {}
    )
    public void testNegate() {
        mPointF = new PointF(10, 10);
        mPointF.negate();
        assertEquals(-10.0f, mPointF.x);
        assertEquals(-10.0f, mPointF.y);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "length",
        args = {}
    )
    public void testLength1() {
        mPointF = new PointF(0.3f, 0.4f);
        assertEquals(0.5f, mPointF.length());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "length",
        args = {float.class, float.class}
    )
    public void testLength2() {
        assertEquals(0.5f, PointF.length(0.3f, 0.4f));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {float.class, float.class}
    )
    public void testSet1() {
        mPointF = new PointF();
        mPointF.set(0.3f, 0.4f);
        assertEquals(0.3f, mPointF.x);
        assertEquals(0.4f, mPointF.y);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.graphics.PointF.class}
    )
    public void testSet2() {
        mPointF = new PointF();
        PointF pointF = new PointF(0.3f, 0.4f);
        mPointF.set(pointF);
        assertEquals(0.3f, mPointF.x);
        assertEquals(0.4f, mPointF.y);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {float.class, float.class}
    )
    public void testEquals() {
        mPointF = new PointF(0.3f, 0.4f);
        assertTrue(mPointF.equals(0.3f, 0.4f));
        assertFalse(mPointF.equals(0.4f, 0.3f));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {float.class, float.class}
    )
    public void testOffset() {
        mPointF = new PointF(10.0f, 10.0f);
        mPointF.offset(1.0f, 1.1f);
        assertEquals(11.0f, mPointF.x);
        assertEquals(11.1f, mPointF.y);
    }

}
