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

import junit.framework.TestCase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Handler.Callback;
import android.util.Printer;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Handler.class)
public class HandlerTest extends TestCase {

    public static final int MESSAGE_WHAT = 3;

    // time when message should be handled.
    static final int RUNTIME = 300;

    // time when check whether the message is handled.
    static final long DELAYED = RUNTIME + 50;

    // Handler
    Handler mHandler = new Handler();
    MockHandler mHandler1 = new MockHandler();

    @Override
    protected void tearDown() throws Exception {
        mHandler1.reset();
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Handler}",
            method = "Handler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Handler}",
            method = "Handler",
            args = {android.os.Looper.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Handler}",
            method = "Handler",
            args = {Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Handler}",
            method = "Handler",
            args = {Looper.class, Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test constructor of Handler",
            method = "toString",
            args = {}
        )
    })
    public void testConstructor() {
        Callback cb = new Callback() {
            public boolean handleMessage(Message msg) {
                return false;
            }
        };

        // new the Handler instance
        new Handler();
        new Handler(Looper.myLooper());
        new Handler(cb);
        new Handler(Looper.myLooper(), cb);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test postAtTime with params Runnable, uptimeMillis",
        method = "postAtTime",
        args = {Runnable.class, long.class}
    )
    public void testPostAtTime1() {
        MockRunnable r = new MockRunnable();
        assertTrue(mHandler.postAtTime(r, SystemClock.uptimeMillis() + RUNTIME));
        assertFalse(r.isRun());
        sleep(DELAYED);
        assertTrue(r.isRun());
        mHandler.removeCallbacks(r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test postAtTime with params Runnable, Object, uptimeMillis",
        method = "postAtTime",
        args = {Runnable.class, Object.class, long.class}
    )
    public void testPostAtTime2() {
        MockRunnable r = new MockRunnable();
        Object token = new Object();
        assertTrue(mHandler.postAtTime(r, token, SystemClock.uptimeMillis() + RUNTIME));
        assertFalse(r.isRun());
        sleep(DELAYED);
        assertTrue(r.isRun());
        mHandler.removeCallbacks(r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendMessageAtTime with params Message, uptimeMillis",
        method = "sendMessageAtTime",
        args = {Message.class, long.class}
    )
    public void testSendMessageAtTime() {
        Message msg = mHandler1.obtainMessage();
        assertTrue(mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME));
        assertNull(mHandler1.message);
        sleep(DELAYED);
        assertSame(msg, mHandler1.message);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test dump function",
        method = "dump",
        args = {Printer.class, String.class}
    )
    public void testDump() {
        final String prefix = "AndroidTest";
        MockPrinter pw = new MockPrinter();
        mHandler.dump(pw, prefix);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test hasMessages with params int",
            method = "hasMessages",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test hasMessages with params int",
            method = "removeMessages",
            args = {int.class}
        )
    })
    public void testHasMessagesWithInt() {
        Message msg = mHandler.obtainMessage();
        assertFalse(mHandler.hasMessages(msg.what));
        mHandler.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        assertTrue(mHandler.hasMessages(msg.what));
        mHandler.removeMessages(msg.what);
        assertFalse(mHandler.hasMessages(msg.what));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test hasMessages with params int, Object",
        method = "hasMessages",
        args = {int.class, Object.class}
    )
    public void testHasMessagesWithObject() {
        Message msg = mHandler.obtainMessage();
        msg.obj = new Object();
        assertFalse(mHandler.hasMessages(msg.what, msg.obj));
        mHandler.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        assertTrue(mHandler.hasMessages(msg.what, msg.obj));
        mHandler.removeMessages(msg.what);
        assertFalse(mHandler.hasMessages(msg.what, msg.obj));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test removeCallbacksAndMessages with null Object",
        method = "removeCallbacksAndMessages",
        args = {Object.class}
    )
    public void testRemoveCallbacksAndMessages() {
        Message msg = mHandler1.obtainMessage();
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(RUNTIME / 2);

        // Test the obj == null
        mHandler1.removeCallbacksAndMessages(null);
        sleep(RUNTIME / 2);
        assertNull(mHandler1.message);
        mHandler1.reset();

        msg = mHandler1.obtainMessage();
        msg.obj = new Object();
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(RUNTIME / 2);

        // Test the obj == p.obj for message
        mHandler1.removeCallbacksAndMessages(msg.obj);
        sleep(RUNTIME / 2);
        assertNull(mHandler1.message);
        mHandler1.reset();

        // Test remove a callback
        final Object obj = new Object();
        MockRunnable mr1 = new MockRunnable();
        mHandler1.postAtTime(mr1, obj, SystemClock.uptimeMillis() + RUNTIME);
        sleep(RUNTIME / 2);
        mHandler1.removeCallbacksAndMessages(obj);
        sleep(RUNTIME / 2);
        assertFalse(mr1.isRun());

        // test remove a wrong callback
        mr1 = new MockRunnable();
        mHandler1.postAtTime(mr1, obj, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler1.removeCallbacksAndMessages(new Object());
        sleep(DELAYED / 2);
        assertTrue(mr1.isRun());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendEmptyMessageAtTime with param Message and uptimeMillis",
        method = "sendEmptyMessageAtTime",
        args = {int.class, long.class}
    )
    public void testSendEmptyMessageAtTime() {
        long uptime = SystemClock.uptimeMillis() + RUNTIME;
        assertTrue(mHandler1.sendEmptyMessageAtTime(MESSAGE_WHAT, uptime));
        assertEquals(0, mHandler1.what);
        sleep(DELAYED);
        assertEquals(MESSAGE_WHAT, mHandler1.what);
        mHandler1.removeMessages(MESSAGE_WHAT);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test getLooper",
        method = "getLooper",
        args = {}
    )
    public void testGetLooper() {
        // new the Handler instance
        Looper looper = Looper.myLooper();
        mHandler = new Handler(looper);
        assertSame(looper, mHandler.getLooper());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test removeCallbacks with param Runnable",
        method = "removeCallbacks",
        args = {Runnable.class}
    )
    public void testRemoveCallbacks() {
        // test remove right object.
        MockRunnable r = new MockRunnable();
        mHandler.postAtTime(r, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler.removeCallbacks(r);
        sleep(DELAYED / 2);
        assertFalse(r.isRun());

        // test remove wrong object.
        r = new MockRunnable();
        MockRunnable mr = new MockRunnable();
        mHandler.postAtTime(r, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler.removeCallbacks(mr);
        sleep(DELAYED / 2);
        assertTrue(r.isRun());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test removeCallbacks with params Runnable and Object",
        method = "removeCallbacks",
        args = {Runnable.class, Object.class}
    )
    public void testRemoveCallbacksWithObject() {
        // test remove right object.
        MockRunnable r1 = new MockRunnable();
        Object token = new Object();
        mHandler.postAtTime(r1, token, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler.removeCallbacks(r1, token);
        sleep(DELAYED / 2);
        assertFalse(r1.isRun());

        // test remove wrong object.
        r1 = new MockRunnable();
        MockRunnable r2 = new MockRunnable();

        mHandler.postAtTime(r1, token, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler.removeCallbacks(r2, token);
        sleep(DELAYED / 2);
        assertTrue(r1.isRun());

        // test remove with right callback and wrong token
        mHandler.postAtTime(r1, token, SystemClock.uptimeMillis() + RUNTIME);
        Object wrongToken = new Object();
        sleep(DELAYED / 2);
        mHandler.removeCallbacks(r1, wrongToken);
        sleep(DELAYED / 2);
        assertTrue(r1.isRun());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test removeMessages with params Message",
        method = "removeMessages",
        args = {int.class}
    )
    public void testRemoveMessages() {
        // test remove right message
        Message msg = mHandler1.obtainMessage();
        msg.what = 100;
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler1.removeMessages(msg.what);
        sleep(DELAYED / 2);
        assertNull(mHandler1.message);
        assertEquals(0, mHandler1.what);
        mHandler1.reset();

        // test remove wrong message
        msg = mHandler1.obtainMessage();
        msg.what = 100;
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler1.removeMessages(101);
        sleep(DELAYED / 2);
        assertEquals(100, mHandler1.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test removeMessages with params Message, and uptimeMillis",
        method = "removeMessages",
        args = {int.class, Object.class}
    )
    public void testRemoveMessagesWithObject() {
        // test remove right message
        Message msg = mHandler1.obtainMessage();
        msg.obj = new Object();
        msg.what = 100;
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler1.removeMessages(msg.what, msg.obj);
        sleep(DELAYED / 2);
        assertNull(mHandler1.message);
        assertEquals(0, mHandler1.what);
        mHandler1.reset();

        // test remove wrong message
        msg = mHandler1.obtainMessage();
        msg.obj = new Object();
        msg.what = 100;
        Message wrongMessage = mHandler1.obtainMessage();
        wrongMessage.obj = new Object();
        wrongMessage.what = 111;
        mHandler1.sendMessageAtTime(msg, SystemClock.uptimeMillis() + RUNTIME);
        sleep(DELAYED / 2);
        mHandler1.removeMessages(msg.what, wrongMessage.obj);
        sleep(DELAYED / 2);
        assertEquals(100, mHandler1.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendMessage with param Message",
        method = "sendMessage",
        args = {Message.class}
    )
    public void testSendMessage() {
        Message msg = mHandler1.obtainMessage();
        assertTrue(mHandler1.sendMessage(msg));
        sleep(DELAYED);
        assertSame(msg, mHandler1.message);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test obtainMessage",
        method = "obtainMessage",
        args = {}
    )
    public void testObtainMessage() {
         Message msg = mHandler.obtainMessage();
         assertNotNull(msg);
         assertEquals(mHandler, msg.getTarget());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test obtainMessage function",
        method = "obtainMessage",
        args = {int.class}
    )
    public void testObtainMessageWithInt() {
         // new the Handler instance
         Handler handler = new Handler();
         Message msg = handler.obtainMessage();
         msg.what = 100;
         Message msg1 = mHandler.obtainMessage(msg.what);
         assertNotNull(msg1);
         assertEquals(mHandler, msg1.getTarget());
         assertEquals(msg.what, msg1.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test obtainMessage with params Message and Object",
        method = "obtainMessage",
        args = {int.class, java.lang.Object.class}
    )
    public void testObtainMessageWithIntObject() {
        // new the Handler instance
        Handler handler = new Handler();
        Message msg = handler.obtainMessage();
        msg.what = 100;
        msg.obj = new Object();
        Message msg1 = mHandler.obtainMessage(msg.what, msg.obj);
        assertNotNull(msg1);
        assertEquals(mHandler, msg1.getTarget());
        assertEquals(msg.what, msg1.what);
        assertSame(msg.obj, msg1.obj);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test obtainMessage with params Message,arg1 and arg2",
        method = "obtainMessage",
        args = {int.class, int.class, int.class}
    )
    public void testObtainMessageWithMutiInt() {
        // new the Handler instance
        Handler handler = new Handler();
        Message msg = handler.obtainMessage();
        msg.what = 100;
        msg.arg1 = 101;
        msg.arg2 = 102;
        Message msg1 = mHandler.obtainMessage(msg.what, msg.arg1, msg.arg2);
        assertNotNull(msg1);
        assertEquals(mHandler, msg1.getTarget());
        assertEquals(msg.what, msg1.what);
        assertEquals(msg.arg1, msg1.arg1);
        assertEquals(msg.arg2, msg1.arg2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test obtainMessage with params Message, Object,arg1 and arg2",
        method = "obtainMessage",
        args = {int.class, int.class, int.class, Object.class}
    )
    public void testObtainMessageWithMutiIntObject() {
        // new the Handler instance
        Handler handler = new Handler();
        Message msg = handler.obtainMessage();
        msg.what = 100;
        msg.arg1 = 1000;
        msg.arg2 = 2000;
        msg.obj = new Object();
        Message msg1 = mHandler.obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
        assertNotNull(msg1);
        assertEquals(mHandler, msg1.getTarget());
        assertEquals(msg.arg1, msg1.arg1);
        assertEquals(msg.arg2, msg1.arg2);
        assertSame(msg.obj, msg1.obj);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendMessageAtFrontOfQueue with param Message",
        method = "sendMessageAtFrontOfQueue",
        args = {Message.class}
    )
    public void testSendMessageAtFrontOfQueue() {
        Message lateMsg = mHandler1.obtainMessage();
        mHandler1.sendEmptyMessageAtTime(lateMsg.what, SystemClock.uptimeMillis() + RUNTIME * 5);
        Message msg = mHandler1.obtainMessage();
        msg.what = 100;
        assertTrue(mHandler1.sendMessageAtFrontOfQueue(msg));
        sleep(DELAYED);
        assertSame(msg, mHandler1.message);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test postDelayed with params Runnable, delaytime",
        method = "postDelayed",
        args = {Runnable.class, long.class}
    )
    public void testPostDelayed() {
        MockRunnable r = new MockRunnable();
        assertTrue(mHandler.postDelayed(r, DELAYED));
        assertFalse(r.isRun());
        sleep(DELAYED + 500);
        assertTrue(r.isRun());
        mHandler.removeCallbacks(r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test postAtFrontOfQueue with param Runnable",
        method = "postAtFrontOfQueue",
        args = {Runnable.class}
    )
    public void testPostAtFrontOfQueue() {
        MockRunnable r = new MockRunnable();
        MockRunnable mr = new MockRunnable();
        assertFalse(r.isRun());
        assertTrue(mHandler.postDelayed(mr, DELAYED));
        assertTrue(mHandler.postAtFrontOfQueue(r));
        sleep(DELAYED / 2);
        assertTrue(r.isRun());
        mHandler.removeCallbacks(r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendMessageDelayed with params Message, delaytime",
        method = "sendMessageDelayed",
        args = {Message.class, long.class}
    )
    public void testSendMessageDelayed() {
        Message msg = mHandler1.obtainMessage();
        assertTrue(mHandler1.sendMessageDelayed(msg, DELAYED));
        assertNull(mHandler1.message);
        sleep(DELAYED + 500);
        assertSame(msg, mHandler1.message);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test post with param Runnalbe",
        method = "post",
        args = {Runnable.class}
    )
    public void testPost() {
        MockRunnable r = new MockRunnable();
        assertFalse(r.isRun());
        assertTrue(mHandler.post(r));
        sleep(DELAYED);
        assertTrue(r.isRun());
        mHandler.removeCallbacks(r);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendEmptyMessageDelayed with params Message,delaytime",
        method = "sendEmptyMessageDelayed",
        args = {int.class, long.class}
    )
    public void testSendEmptyMessageDelayed() {
        Message msg = mHandler1.obtainMessage();
        msg.what = 100;
        assertTrue(mHandler1.sendEmptyMessageDelayed(msg.what, DELAYED));
        sleep(DELAYED + 500);
        assertEquals(msg.what, mHandler1.what);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test dispatchMessage with params Message, test its if branch",
        method = "dispatchMessage",
        args = {android.os.Message.class}
    )
    public void testDispatchMessage1() {
        // new the Handler instance
        MockHandler handler = new MockHandler();
        MockRunnable callback = new MockRunnable();
        Message msg = Message.obtain(handler, callback);
        handler.dispatchMessage(msg);
        assertNotNull(msg.getCallback());

        // Test the if branch
        assertTrue(callback.isRun());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test dispatchMessage with params Message, test its elsebranch",
            method = "dispatchMessage",
            args = {Message.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test dispatchMessage with params Message, test its elsebranch",
            method = "handleMessage",
            args = {Message.class}
        )
    })
    public void testDispatchMessage2() {
        // new the Handler instance
        MockHandler handler = new MockHandler();
        Message msg = handler.obtainMessage();
        handler.dispatchMessage(msg);

        // Test the else branch
        assertSame(msg, handler.message);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test sendEmptyMessage with param int what",
        method = "sendEmptyMessage",
        args = {int.class}
    )
    public void testSendEmptyMessage() {
        Message msg = mHandler1.obtainMessage();
        msg.what = 100;
        assertTrue(mHandler1.sendEmptyMessage(msg.what));
        sleep(DELAYED);
        assertEquals(msg.what, mHandler1.what);
        mHandler1.removeMessages(msg.what);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test toString",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(mHandler1.toString());
    }

    /**
     * MockRunnable
     */
    private class MockRunnable implements Runnable {
        // MockRunnable run
        private boolean mIsRun;

        public void run() {
            mIsRun = true;
        }

        public boolean isRun() {
            return mIsRun;
        }
    }

    /**
     * MockPrinter
     */
    private class MockPrinter implements Printer {
        String mOutput;

        public void println(String x) {
            mOutput = x;
        }

        public String getPrint() {
            return mOutput;
        }
    }

    /**
     * MockHandler
     */
    private class MockHandler extends Handler {
        public Message message;
        public int what;

        @Override
        public void handleMessage(Message msg) {
            message = msg;
            what = message.what;
        }

        public void reset() {
            message = null;
            what = 0;
        }
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
