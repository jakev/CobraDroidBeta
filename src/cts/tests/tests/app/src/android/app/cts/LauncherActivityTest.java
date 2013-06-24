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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Instrumentation;
import android.app.LauncherActivity;
import android.app.LauncherActivity.ListItem;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;

import java.util.List;

@TestTargetClass(LauncherActivity.class)
public class LauncherActivityTest extends InstrumentationTestCase {
    private Instrumentation mInstrumentation;
    private LauncherActivityStub mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            if (!mActivity.isFinishing()) {
                mActivity.finish();
            }
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LauncherActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTargetIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "makeListItems",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onListItemClick",
            args = {android.widget.ListView.class, android.view.View.class, int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "intentForPosition",
            args = {int.class}
        )
    })
    public void testLaunchActivity() {
        // Constructor of LaunchActivity can't be invoked directly.
        new LauncherActivityStub();
        final String packageName = "com.android.cts.stub";
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(packageName, LauncherActivityStub.class.getName());
        mActivity = (LauncherActivityStub) mInstrumentation.startActivitySync(intent);
        // Test onCreate
        assertTrue(mActivity.isOnCreateCalled);

        // Test getTargetIntent. LaunchActivity#getTargetIntent() just returns a Intent() instance
        // with no content, so we use LaunchActivityStub#getSuperIntent() to get the default Intent,
        // and create a new intent for other tests.
        assertNotNull(mActivity.getSuperIntent());

        // Test makeListItems. Make sure the size > 0. The sorted order is related to the sort
        // way, so it's mutable.
        final List<ListItem> list = mActivity.makeListItems();
        assertTrue(list.size() > 0);

        // There should be an activity(but with uncertain content) in position 0.
        assertNotNull(mActivity.intentForPosition(0));
        // Test onListItemClick
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(mActivity.isOnListItemClick);
    }
}
