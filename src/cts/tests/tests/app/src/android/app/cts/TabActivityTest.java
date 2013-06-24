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
import dalvik.annotation.ToBeFixed;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;
import android.widget.TabHost;

@TestTargetClass(TabActivity.class)
public class TabActivityTest extends InstrumentationTestCase {
    private Instrumentation mInstrumentation;
    private MockTabActivity mActivity;
    private Activity mChildActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = super.getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            if (!mActivity.isFinishing()) {
                mActivity.finish();
            } else if (mChildActivity != null) {
                if (!mChildActivity.isFinishing()) {
                    mChildActivity.finish();
                }
            }
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultTab",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultTab",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTabHost",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTabWidget",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {android.os.Bundle.class}
        )
    })
    @ToBeFixed(bug = "1701364", explanation = "When testing TabActivity#setDefaultTab(int index),"
            + " setDefaultTab(String tag), we find that the set values are hard to get, there"
            + " is no proper method or other way to obtain these two default values.")
    public void testTabActivity() throws Throwable {
        // Test constructor
        new TabActivity();

        final String packageName = "com.android.cts.stub";
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(packageName, MockTabActivity.class.getName());
        mActivity = (MockTabActivity) mInstrumentation.startActivitySync(intent);
        // Test onPostCreate, onContentChanged. These two methods are invoked in starting
        // activity. Default values of isOnContentChangedCalled, isOnPostCreateCalled are false.
        assertTrue(mActivity.isOnContentChangedCalled);
        assertTrue(mActivity.isOnPostCreateCalled);

        // Can't get default value.
        final int defaultIndex = 1;
        mActivity.setDefaultTab(defaultIndex);
        final String defaultTab = "DefaultTab";
        mActivity.setDefaultTab(defaultTab);
        // Test getTabHost, getTabWidget
        final TabHost tabHost = mActivity.getTabHost();
        assertNotNull(tabHost);
        assertNotNull(tabHost.getTabWidget());

        // Test onSaveInstanceState
        assertFalse(mActivity.isOnSaveInstanceStateCalled);
        final Intent embedded = new Intent(mInstrumentation.getTargetContext(),
                ChildTabActivity.class);
        mActivity.startActivity(embedded);
        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.isOnSaveInstanceStateCalled);

        // Test onRestoreInstanceState
        sendKeys(KeyEvent.KEYCODE_BACK);
        mInstrumentation.waitForIdleSync();
        assertFalse(MockTabActivity.isOnRestoreInstanceStateCalled);
        OrientationTestUtils.toggleOrientationSync(mActivity, mInstrumentation);
        assertTrue(MockTabActivity.isOnRestoreInstanceStateCalled);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onChildTitleChanged",
        args = {android.app.Activity.class, java.lang.CharSequence.class}
    )
    public void testChildTitleCallback() throws Exception {
        final Context context = mInstrumentation.getTargetContext();
        final Intent intent = new Intent(context, MockTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final MockTabActivity father = new MockTabActivity();
        final ComponentName componentName = new ComponentName(context, MockTabActivity.class);
        final ActivityInfo info = context.getPackageManager().getActivityInfo(componentName, 0);
        mChildActivity = mInstrumentation.newActivity(MockTabActivity.class, mInstrumentation
                .getTargetContext(), null, null, intent, info, MockTabActivity.class.getName(),
                father, null, null);

        assertNotNull(mChildActivity);
        final String newTitle = "New Title";
        mChildActivity.setTitle(newTitle);
        assertTrue(father.isOnChildTitleChangedCalled);
    }
}
