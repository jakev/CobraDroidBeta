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

package android.os.cts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Message.class)
public class MessageTest extends AndroidTestCase {
    public static final int SLEEP_TIME = 300;
    public static final int WHAT = 1;
    public static final int ARG1 = 1;
    public static final int ARG2 = 2;
    public static final String KEY = "android";
    public static final int VALUE = 3;

    private Message mMessage;
    private boolean mMessageHandlerCalled;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mMessageHandlerCalled = true;
        }
    };

    private Runnable mRunnable = new Runnable() {
        public void run() {
        }
    };

    final Object OBJ = new Object();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMessage = new Message();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link Message}",
        method = "Message",
        args = {}
    )
    public void testConstructor() {
        new Message();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: getWhen",
            method = "getWhen",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: getTarget",
            method = "getTarget",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: setTarget",
            method = "setTarget",
            args = {android.os.Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: getCallback",
            method = "getCallback",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: setData",
            method = "setData",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: getData",
            method = "getData",
            args = {}
        )
    })
    public void testAccessMessageProperties() {
        assertEquals(0, mMessage.getWhen());
        mMessage.setTarget(mHandler);
        assertEquals(mHandler, mMessage.getTarget());

        assertNull(mMessage.getCallback());
        Message expected = Message.obtain(mHandler, mRunnable);
        assertEquals(mRunnable, expected.getCallback());

        Bundle bundle = mMessage.getData();
        assertNotNull(bundle);
        Bundle expectedBundle = new Bundle();
        mMessage.setData(expectedBundle);
        assertNotNull(mMessage.getData());
        assertNotSame(bundle, mMessage.getData());
        assertEquals(expectedBundle, mMessage.getData());

        assertEquals(0, mMessage.describeContents());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {}
    )
    public void testObtain() {
        Message message = Message.obtain();
        assertNotNull(message);
        assertEquals(0, message.what);
        assertEquals(0, message.arg1);
        assertEquals(0, message.arg2);
        assertNull(message.obj);
        assertNull(message.replyTo);
        assertNull(message.getTarget());
        assertNull(message.getCallback());
        assertNull(message.peekData());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Message.class}
    )
    public void testObtain2() {
        Message message = Message.obtain(mHandler, WHAT, ARG1, ARG2, OBJ);
        Message expected = Message.obtain(message);

        assertEquals(message.getTarget(), expected.getTarget());
        assertEquals(message.what, expected.what);
        assertEquals(message.arg1, expected.arg1);
        assertEquals(message.arg2, expected.arg2);
        assertEquals(message.obj, expected.obj);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class}
    )
    public void testObtain3() {
        Message expected = Message.obtain(mHandler);
        assertEquals(mHandler, expected.getTarget());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class, Runnable.class}
    )
    public void testObtain4() {
        Message expected = Message.obtain(mHandler, mRunnable);
        assertEquals(mHandler, expected.getTarget());
        assertEquals(mRunnable, expected.getCallback());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class, int.class}
    )
    public void testObtain5() {
        Message expected = Message.obtain(mHandler, WHAT);
        assertEquals(mHandler, expected.getTarget());
        assertEquals(WHAT, expected.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class, int.class, Object.class}
    )
    public void testObtain6() {
        Message expected = Message.obtain(mHandler, WHAT, OBJ);
        assertEquals(mHandler, expected.getTarget());
        assertEquals(WHAT, expected.what);
        assertEquals(OBJ, expected.obj);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class, int.class, int.class, int.class}
    )
    public void testObtain7() {
        Message expected = Message.obtain(mHandler, WHAT, ARG1, ARG2);
        assertEquals(mHandler, expected.getTarget());
        assertEquals(WHAT, expected.what);
        assertEquals(ARG1, expected.arg1);
        assertEquals(ARG2, expected.arg2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: obtain",
        method = "obtain",
        args = {Handler.class, int.class, int.class, int.class, Object.class}
    )
    public void testObtain8() {
        Message expected = Message.obtain(mHandler, WHAT, ARG1, ARG2, OBJ);
        assertEquals(mHandler, expected.getTarget());
        assertEquals(WHAT, expected.what);
        assertEquals(ARG1, expected.arg1);
        assertEquals(ARG2, expected.arg2);
        assertEquals(OBJ, expected.obj);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: toString",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(mMessage.toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: peekData",
        method = "peekData",
        args = {}
    )
    public void testPeekData() {
        Bundle expected = new Bundle();
        assertNull(mMessage.peekData());
        mMessage.setData(expected);
        assertNotNull(mMessage.peekData());
        assertEquals(expected, mMessage.peekData());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: copyFrom",
        method = "copyFrom",
        args = {Message.class}
    )
    public void testCopyFrom() {
        Message message = Message.obtain(mHandler, WHAT, ARG1, ARG2, OBJ);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY, VALUE);
        message.setData(bundle);
        mMessage.copyFrom(message);
        assertEquals(WHAT, mMessage.what);
        assertEquals(ARG1, mMessage.arg1);
        assertEquals(ARG2, mMessage.arg2);
        assertEquals(OBJ, mMessage.obj);
        assertEquals(VALUE, mMessage.getData().getInt(KEY));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: recycle",
        method = "recycle",
        args = {}
    )
    public void testRecycle() {
        Message message = Message.obtain(mHandler, WHAT, ARG1, ARG2, OBJ);
        message.recycle();
        assertEquals(0, message.what);
        assertEquals(0, message.arg1);
        assertEquals(0, message.arg2);
        assertNull(message.obj);
        assertNull(message.replyTo);
        assertNull(message.getTarget());
        assertNull(message.getCallback());
        assertNull(message.peekData());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: writeToParcel",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Message message = Message.obtain(mHandler, WHAT, ARG1, ARG2);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY, VALUE);
        message.setData(bundle);
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        mMessage = Message.CREATOR.createFromParcel(parcel);
        assertNull(mMessage.getTarget());
        assertEquals(WHAT, mMessage.what);
        assertEquals(ARG1, mMessage.arg1);
        assertEquals(ARG2, mMessage.arg2);
        assertEquals(VALUE, mMessage.getData().getInt(KEY));

        message = Message.obtain(mHandler, WHAT, ARG1, ARG2, OBJ);
        try {
            message.writeToParcel(parcel, 1);
            fail("should throw excetion");
        } catch (RuntimeException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: sendToTarget",
        method = "sendToTarget",
        args = {}
    )
    public void testSendToTarget() {
        try {
            mMessage.sendToTarget();
            fail("should throw exception");
        } catch (Exception e) {
            //expected
        }

        Message message = Message.obtain(mHandler);
        assertFalse(mMessageHandlerCalled);
        message.sendToTarget();
        sleep(SLEEP_TIME);
        assertTrue(mMessageHandlerCalled);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
