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

package android.app.cts;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.animation.cts.DelayedCheck;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(IntentService.class)
public class IntentServiceTest extends ActivityTestsBase {

    private Intent mIntent;
    private static final int TIMEOUT_MSEC = 5000;
    private boolean mConnected;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IntentServiceStub.reset();
        mIntent = new Intent(mContext, IntentServiceStub.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (!IntentServiceStub.onDestroyCalled) {
            mContext.stopService(mIntent);
        }
    }

    public void testIntents() throws Throwable {
        final int value = 42;
        final int adds = 3;

        Intent addIntent = new Intent(mContext, IntentServiceStub.class);

        addIntent.setAction(IntentServiceStub.ISS_ADD);
        addIntent.putExtra(IntentServiceStub.ISS_VALUE, 42);

        for (int i = 0; i < adds; i++) {
            mContext.startService(addIntent);
        }

        // service should terminate automatically once all intents are handled
        IntentServiceStub.waitToFinish(TIMEOUT_MSEC);
        assertEquals(adds, IntentServiceStub.onHandleIntentCalled);
        assertEquals(adds * value, IntentServiceStub.accumulator);

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onHandleIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onBind",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentService",
            args = {String.class}
        )
    })
    public void testIntentServiceLifeCycle() throws Throwable {
        // start service
        mContext.startService(mIntent);
        new DelayedCheck(TIMEOUT_MSEC) {
            protected boolean check() {
                return IntentServiceStub.onHandleIntentCalled > 0;
            }
        }.run();
        assertTrue(IntentServiceStub.onCreateCalled);
        assertTrue(IntentServiceStub.onStartCalled);

        // bind service
        ServiceConnection conn = new TestConnection();
        mContext.bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
        new DelayedCheck(TIMEOUT_MSEC) {
            protected boolean check() {
                return mConnected;
            }
        }.run();
        assertTrue(IntentServiceStub.onBindCalled);

        // unbind service
        mContext.unbindService(conn);
        // stop service
        mContext.stopService(mIntent);
        IntentServiceStub.waitToFinish(TIMEOUT_MSEC);
    }

    private class TestConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnected = true;
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
