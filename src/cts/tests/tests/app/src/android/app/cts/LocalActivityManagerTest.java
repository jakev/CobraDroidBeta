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

package android.app.cts;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(LocalActivityManager.class)
public class LocalActivityManagerTest extends InstrumentationTestCase implements CTSResult {

    private Instrumentation mInstrumentation;

    private Sync mSync = new Sync();
    private static class Sync {
        public boolean mHasNotify;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mSync = new Sync();
    }

    private void setupActivity(final String action) {
        final Intent intent = new Intent(mInstrumentation.getTargetContext(),
                LocalActivityManagerTestHelper.class);
        intent.setAction(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstrumentation.getTargetContext().startActivity(intent);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor of LocalActivityManager",
        method = "LocalActivityManager",
        args = {android.app.Activity.class, boolean.class}
    )
    public void testConstructor() {
        new LocalActivityManager(new Activity(), true);
        new LocalActivityManager(new Activity(), false);
        new LocalActivityManager(null, false);
        new LocalActivityManager(null, true);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchResume",
        args = {}
    )
    public void testDispatchResume() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_RESUME);
        waitForResult();
    }

    private void waitForResult() throws InterruptedException {
        synchronized (mSync) {
            if (!mSync.mHasNotify) {
                mSync.wait();
            }
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {java.lang.String.class, android.content.Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getActivity",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "destroyActivity",
            args = {java.lang.String.class, boolean.class}
        )
    })
    public void testStartActivity() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_START_ACTIIVTY);
        waitForResult();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveInstanceState",
            args = {}
        )
    })
    public void testDispatchCreate() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_CREATE);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchStop",
        args = {}
    )
    public void testDispatchStop() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_STOP);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchPause",
        args = {boolean.class}
    )
    public void testDispatchPauseTrue() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_PAUSE_TRUE);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchPause",
        args = {boolean.class}
    )
    public void testDispatchPauseFalse() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_PAUSE_FALSE);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "saveInstanceState",
        args = {}
    )
    public void testSaveInstanceState() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_SAVE_INSTANCE_STATE);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchDestroy",
        args = {boolean.class}
    )
    public void testDispatchDestroy() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_DISPATCH_DESTROY);
        waitForResult();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeAllActivities",
        args = {}
    )
    public void testRemoveAllActivities() throws InterruptedException {
        LocalActivityManagerTestHelper.setResult(this);
        setupActivity(LocalActivityManagerTestHelper.ACTION_REMOVE_ALL_ACTIVITY);
        waitForResult();
    }

    public void setResult(final int resultCode) {
        synchronized (mSync) {
            mSync.mHasNotify = true;
            mSync.notify();
            assertEquals(CTSResult.RESULT_OK, resultCode);
        }
    }

    public void setResult(Exception e) {
        setResult(CTSResult.RESULT_FAIL);
    }

}
