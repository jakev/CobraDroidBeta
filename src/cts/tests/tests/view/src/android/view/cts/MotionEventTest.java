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
import dalvik.annotation.ToBeFixed;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Test {@link MotionEvent}.
 */
@TestTargetClass(MotionEvent.class)
public class MotionEventTest extends AndroidTestCase {
    private MotionEvent mMotionEvent1;
    private MotionEvent mMotionEvent2;
    private long mDownTime;
    private long mEventTime;
    private static final float X_3F           = 3.0f;
    private static final float Y_4F           = 4.0f;
    private static final int META_STATE       = KeyEvent.META_SHIFT_ON;
    private static final float PRESSURE_1F    = 1.0f;
    private static final float SIZE_1F        = 1.0f;
    private static final float X_PRECISION_3F  = 3.0f;
    private static final float Y_PRECISION_4F  = 4.0f;
    private static final int DEVICE_ID_1      = 1;
    private static final int EDGE_FLAGS       = MotionEvent.EDGE_TOP;
    private static final float DELTA          = 0.01f;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mDownTime = SystemClock.uptimeMillis();
        mEventTime = SystemClock.uptimeMillis();
        mMotionEvent1 = MotionEvent.obtain(mDownTime, mEventTime,
                MotionEvent.ACTION_MOVE, X_3F, Y_4F, META_STATE);
        mMotionEvent2 = MotionEvent.obtain(mDownTime, mEventTime,
                MotionEvent.ACTION_MOVE, X_3F, Y_4F, PRESSURE_1F, SIZE_1F, META_STATE,
                X_PRECISION_3F, Y_PRECISION_4F, DEVICE_ID_1, EDGE_FLAGS);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != mMotionEvent1) {
            mMotionEvent1.recycle();
        }
        if (null != mMotionEvent2) {
            mMotionEvent2.recycle();
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "obtain",
            args = {long.class, long.class, int.class, float.class, float.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDownTime",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEventTime",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRawX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRawY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMetaState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDeviceId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPressure",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getXPrecision",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getYPrecision",
            args = {}
        )
    })
    public void testObtain1() {
        mMotionEvent1 = MotionEvent.obtain(mDownTime, mEventTime,
                MotionEvent.ACTION_DOWN, X_3F, Y_4F, META_STATE);
        assertNotNull(mMotionEvent1);
        assertEquals(mDownTime, mMotionEvent1.getDownTime());
        assertEquals(mEventTime, mMotionEvent1.getEventTime());
        assertEquals(MotionEvent.ACTION_DOWN, mMotionEvent1.getAction());
        assertEquals(X_3F, mMotionEvent1.getX(), DELTA);
        assertEquals(Y_4F, mMotionEvent1.getY(), DELTA);
        assertEquals(X_3F, mMotionEvent1.getRawX(), DELTA);
        assertEquals(Y_4F, mMotionEvent1.getRawY(), DELTA);
        assertEquals(META_STATE, mMotionEvent1.getMetaState());
        assertEquals(0, mMotionEvent1.getDeviceId());
        assertEquals(0, mMotionEvent1.getEdgeFlags());
        assertEquals(PRESSURE_1F, mMotionEvent1.getPressure(), DELTA);
        assertEquals(SIZE_1F, mMotionEvent1.getSize(), DELTA);
        assertEquals(1.0f, mMotionEvent1.getXPrecision(), DELTA);
        assertEquals(1.0f, mMotionEvent1.getYPrecision(), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "obtain",
        args = {MotionEvent.class}
    )
    public void testObtain2() {
        MotionEvent motionEvent = MotionEvent.obtain(mDownTime, mEventTime,
                MotionEvent.ACTION_DOWN, X_3F, Y_4F, META_STATE);
        mMotionEvent1 = MotionEvent.obtain(motionEvent);
        assertNotNull(mMotionEvent1);
        assertEquals(motionEvent.getDownTime(), mMotionEvent1.getDownTime());
        assertEquals(motionEvent.getEventTime(), mMotionEvent1.getEventTime());
        assertEquals(motionEvent.getAction(), mMotionEvent1.getAction());
        assertEquals(motionEvent.getX(), mMotionEvent1.getX(), DELTA);
        assertEquals(motionEvent.getY(), mMotionEvent1.getY(), DELTA);
        assertEquals(motionEvent.getX(), mMotionEvent1.getRawX(), DELTA);
        assertEquals(motionEvent.getY(), mMotionEvent1.getRawY(), DELTA);
        assertEquals(motionEvent.getMetaState(), mMotionEvent1.getMetaState());
        assertEquals(motionEvent.getDeviceId(), mMotionEvent1.getDeviceId());
        assertEquals(motionEvent.getEdgeFlags(), mMotionEvent1.getEdgeFlags());
        assertEquals(motionEvent.getPressure(), mMotionEvent1.getPressure(), DELTA);
        assertEquals(motionEvent.getSize(), mMotionEvent1.getSize(), DELTA);
        assertEquals(motionEvent.getXPrecision(), mMotionEvent1.getXPrecision(), DELTA);
        assertEquals(motionEvent.getYPrecision(), mMotionEvent1.getYPrecision(), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "obtain",
        args = {long.class, long.class, int.class, float.class, float.class, float.class,
                float.class, int.class, float.class, float.class, int.class, int.class}
    )
    public void testObtain3() {
        mMotionEvent1 = null;
        mMotionEvent1 = MotionEvent.obtain(mDownTime, mEventTime,
                MotionEvent.ACTION_DOWN, X_3F, Y_4F, PRESSURE_1F, SIZE_1F, META_STATE,
                X_PRECISION_3F, Y_PRECISION_4F, DEVICE_ID_1, EDGE_FLAGS);
        assertNotNull(mMotionEvent1);
        assertEquals(mDownTime, mMotionEvent1.getDownTime());
        assertEquals(mEventTime, mMotionEvent1.getEventTime());
        assertEquals(MotionEvent.ACTION_DOWN, mMotionEvent1.getAction());
        assertEquals(X_3F, mMotionEvent1.getX(), DELTA);
        assertEquals(Y_4F, mMotionEvent1.getY(), DELTA);
        assertEquals(X_3F, mMotionEvent1.getRawX(), DELTA);
        assertEquals(Y_4F, mMotionEvent1.getRawY(), DELTA);
        assertEquals(META_STATE, mMotionEvent1.getMetaState());
        assertEquals(DEVICE_ID_1, mMotionEvent1.getDeviceId());
        assertEquals(EDGE_FLAGS, mMotionEvent1.getEdgeFlags());
        assertEquals(PRESSURE_1F, mMotionEvent1.getPressure(), DELTA);
        assertEquals(SIZE_1F, mMotionEvent1.getSize(), DELTA);
        assertEquals(X_PRECISION_3F, mMotionEvent1.getXPrecision(), DELTA);
        assertEquals(Y_PRECISION_4F, mMotionEvent1.getYPrecision(), DELTA);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAction",
            args = {int.class}
        )
    })
    public void testAccessAction() {
        assertEquals(MotionEvent.ACTION_MOVE, mMotionEvent1.getAction());

        mMotionEvent1.setAction(MotionEvent.ACTION_UP);
        assertEquals(MotionEvent.ACTION_UP, mMotionEvent1.getAction());

        mMotionEvent1.setAction(MotionEvent.ACTION_MOVE);
        assertEquals(MotionEvent.ACTION_MOVE, mMotionEvent1.getAction());

        mMotionEvent1.setAction(MotionEvent.ACTION_CANCEL);
        assertEquals(MotionEvent.ACTION_CANCEL, mMotionEvent1.getAction());

        mMotionEvent1.setAction(MotionEvent.ACTION_DOWN);
        assertEquals(MotionEvent.ACTION_DOWN, mMotionEvent1.getAction());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testDescribeContents() {
        // make sure this method never throw any exception.
        mMotionEvent2.describeContents();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEdgeFlags",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEdgeFlags",
            args = {int.class}
        )
    })
    public void testAccessEdgeFlags() {
        assertEquals(EDGE_FLAGS, mMotionEvent2.getEdgeFlags());

        int edgeFlags = 10;
        mMotionEvent2.setEdgeFlags(edgeFlags);
        assertEquals(edgeFlags, mMotionEvent2.getEdgeFlags());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel parcel = Parcel.obtain();
        mMotionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.setDataPosition(0);

        MotionEvent motionEvent = MotionEvent.CREATOR.createFromParcel(parcel);
        assertEquals(mMotionEvent2.getRawY(), motionEvent.getRawY(), DELTA);
        assertEquals(mMotionEvent2.getRawX(), motionEvent.getRawX(), DELTA);
        assertEquals(mMotionEvent2.getY(), motionEvent.getY(), DELTA);
        assertEquals(mMotionEvent2.getX(), motionEvent.getX(), DELTA);
        assertEquals(mMotionEvent2.getAction(), motionEvent.getAction());
        assertEquals(mMotionEvent2.getDownTime(), motionEvent.getDownTime());
        assertEquals(mMotionEvent2.getEventTime(), motionEvent.getEventTime());
        assertEquals(mMotionEvent2.getEdgeFlags(), motionEvent.getEdgeFlags());
        assertEquals(mMotionEvent2.getDeviceId(), motionEvent.getDeviceId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        // make sure this method never throw exception.
        mMotionEvent2.toString();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offsetLocation",
        args = {float.class, float.class}
    )
    public void testOffsetLocation() {
        assertEquals(X_3F, mMotionEvent2.getX(), DELTA);
        assertEquals(Y_4F, mMotionEvent2.getY(), DELTA);

        float offsetX = 1.0f;
        float offsetY = 1.0f;
        mMotionEvent2.offsetLocation(offsetX, offsetY);
        assertEquals(X_3F + offsetX, mMotionEvent2.getX(), DELTA);
        assertEquals(Y_4F + offsetY, mMotionEvent2.getY(), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLocation",
        args = {float.class, float.class}
    )
    public void testSetLocation() {
        assertEquals(X_3F, mMotionEvent2.getX(), DELTA);
        assertEquals(Y_4F, mMotionEvent2.getY(), DELTA);

        float newLocationX = 0.0f;
        float newLocationY = 0.0f;
        mMotionEvent2.setLocation(newLocationX, newLocationY);
        assertEquals(newLocationX, mMotionEvent2.getX(), DELTA);
        assertEquals(newLocationY, mMotionEvent2.getY(), DELTA);

        newLocationX = 2.0f;
        newLocationY = 2.0f;
        mMotionEvent2.setLocation(newLocationX, newLocationY);
        assertEquals(newLocationX, mMotionEvent2.getX(), DELTA);
        assertEquals(newLocationY, mMotionEvent2.getY(), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistoricalX",
        args = {int.class}
    )
    public void testGetHistoricalX() {
        float x = X_3F + 5.0f;
        mMotionEvent2.addBatch(mEventTime, x, 5.0f, 1.0f, 0.0f, 0);
        assertEquals(X_3F, mMotionEvent2.getHistoricalX(0), DELTA);

        mMotionEvent2.addBatch(mEventTime, X_3F + 10.0f, 10.0f, 0.0f, 1.0f, 0);
        assertEquals(x, mMotionEvent2.getHistoricalX(1), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistoricalY",
        args = {int.class}
    )
    public void testGetHistoricalY() {
        float y = Y_4F + 5.0f;
        mMotionEvent2.addBatch(mEventTime, 5.0f, y, 1.0f, 0.0f, 0);
        assertEquals(Y_4F, mMotionEvent2.getHistoricalY(0), DELTA);

        mMotionEvent2.addBatch(mEventTime, 15.0f, Y_4F + 15.0f, 0.0f, 1.0f, 0);
        assertEquals(y, mMotionEvent2.getHistoricalY(1), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistoricalSize",
        args = {int.class}
    )
    public void testGetHistoricalSize() {
        float size = 0.5f;
        mMotionEvent2.addBatch(mEventTime, 5.0f, 5.0f, 1.0f, size, 0);
        assertEquals(SIZE_1F, mMotionEvent2.getHistoricalSize(0), DELTA);

        mMotionEvent2.addBatch(mEventTime, 15.0f, 15.0f, 1.0f, 0.0f, 0);
        assertEquals(size, mMotionEvent2.getHistoricalSize(1), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistoricalPressure",
        args = {int.class}
    )
    public void testGetHistoricalPressure() {
        float pressure = 0.5f;
        mMotionEvent2.addBatch(mEventTime, 5.0f, 5.0f, pressure, 0.0f, 0);
        assertEquals(PRESSURE_1F, mMotionEvent2.getHistoricalPressure(0), DELTA);

        mMotionEvent2.addBatch(mEventTime, 15.0f, 15.0f, 0.0f, 0.0f, 0);
        assertEquals(pressure, mMotionEvent2.getHistoricalPressure(1), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistoricalEventTime",
        args = {int.class}
    )
    public void testGetHistoricalEventTime() {
        long eventTime = mEventTime + 5l;
        mMotionEvent2.addBatch(eventTime, 5.0f, 5.0f, 0.0f, 1.0f, 0);
        assertEquals(mEventTime, mMotionEvent2.getHistoricalEventTime(0));

        mMotionEvent2.addBatch(mEventTime + 10l, 15.0f, 15.0f, 1.0f, 0.0f, 0);
        assertEquals(eventTime, mMotionEvent2.getHistoricalEventTime(1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addBatch",
        args = {long.class, float.class, float.class, float.class, float.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testAddBatch() {
        long eventTime = SystemClock.uptimeMillis();
        float x = 10.0f;
        float y = 20.0f;
        float pressure = 1.0f;
        float size = 1.0f;

        // get original attribute values.
        long origEventTime = mMotionEvent2.getEventTime();
        float origX = mMotionEvent2.getX();
        float origY = mMotionEvent2.getY();
        float origPressure = mMotionEvent2.getPressure();
        float origSize = mMotionEvent2.getSize();

        assertEquals(0, mMotionEvent2.getHistorySize());
        mMotionEvent2.addBatch(eventTime, x, y, pressure, size, 0);
        assertEquals(1, mMotionEvent2.getHistorySize());
        assertEquals(origEventTime, mMotionEvent2.getHistoricalEventTime(0));
        assertEquals(origX, mMotionEvent2.getHistoricalX(0), DELTA);
        assertEquals(origY, mMotionEvent2.getHistoricalY(0), DELTA);
        assertEquals(origPressure, mMotionEvent2.getHistoricalPressure(0), DELTA);
        assertEquals(origSize, mMotionEvent2.getHistoricalSize(0), DELTA);

        mMotionEvent2.addBatch(mEventTime, 6, 6, 0.1f, 0, 0);
        assertEquals(2, mMotionEvent2.getHistorySize());
        assertEquals(eventTime, mMotionEvent2.getHistoricalEventTime(1));
        assertEquals(x, mMotionEvent2.getHistoricalX(1), DELTA);
        assertEquals(y, mMotionEvent2.getHistoricalY(1), DELTA);
        assertEquals(pressure, mMotionEvent2.getHistoricalPressure(1), DELTA);
        assertEquals(size, mMotionEvent2.getHistoricalSize(1), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHistorySize",
        args = {}
    )
    public void testGetHistorySize() {
        long eventTime = SystemClock.uptimeMillis();
        float x = 10.0f;
        float y = 20.0f;
        float pressure = 1.0f;
        float size = 1.0f;

        mMotionEvent2.setAction(MotionEvent.ACTION_DOWN);
        assertEquals(0, mMotionEvent2.getHistorySize());

        mMotionEvent2.setAction(MotionEvent.ACTION_MOVE);
        mMotionEvent2.addBatch(eventTime, x, y, pressure, size, 0);
        assertEquals(1, mMotionEvent2.getHistorySize());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "recycle",
        args = {}
    )
    public void testRecycle() {
        mMotionEvent2.setAction(MotionEvent.ACTION_MOVE);
        assertEquals(0, mMotionEvent2.getHistorySize());
        mMotionEvent2.addBatch(mEventTime, 10.0f, 5.0f, 1.0f, 0.0f, 0);
        assertEquals(1, mMotionEvent2.getHistorySize());

        mMotionEvent2.recycle();
        
        try {
            mMotionEvent2.recycle();
            fail("recycle() should throw an exception when the event has already been recycled.");
        } catch (RuntimeException ex) {
        }
        
        mMotionEvent2 = null; // since it was recycled, don't try to recycle again in tear down
    }
}
