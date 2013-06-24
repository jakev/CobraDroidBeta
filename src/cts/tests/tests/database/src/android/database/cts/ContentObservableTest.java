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

package android.database.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.database.ContentObservable;
import android.database.ContentObserver;
import android.test.InstrumentationTestCase;

@TestTargetClass(ContentObservable.class)
public class ContentObservableTest extends InstrumentationTestCase {
    ContentObservable mContentObservable;
    MyContentObserver mObserver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContentObservable = new ContentObservable();
        mObserver = new MyContentObserver();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerObserver",
            args = {android.database.ContentObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "notifyChange",
            args = {boolean.class}
        )
    })
    @ToBeFixed(bug = "1486189", explanation = "registerObserver is a redundant override method")
    public void testNotifyChange() {
        mContentObservable.registerObserver(mObserver);
        mObserver.resetStatus();
        assertFalse(mObserver.hasChanged());
        mContentObservable.notifyChange(false);
        assertTrue(mObserver.hasChanged());

        try {
            mContentObservable.registerObserver(mObserver);
            fail("Re-registering observer did not cause exception.");
        } catch (IllegalStateException expected) {
            // expected
        }

        try {
            mContentObservable.registerObserver(null);
            fail("Registering null observer did not cause exception.");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        MyContentObserver second = new MyContentObserver();
        mContentObservable.registerObserver(second);

        mContentObservable.unregisterObserver(mObserver);
        mObserver.resetStatus();
        assertFalse(mObserver.hasChanged());
        mContentObservable.notifyChange(false);
        assertFalse(mObserver.hasChanged());
        assertTrue(second.hasChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchChange",
        args = {boolean.class}
    )
    public void testDispatchChange() {
        mContentObservable.registerObserver(mObserver);
        mObserver.resetStatus();
        assertFalse(mObserver.hasChanged());
        mContentObservable.dispatchChange(false);
        assertTrue(mObserver.hasChanged());

        mObserver.resetStatus();
        assertFalse(mObserver.hasChanged());
        mContentObservable.dispatchChange(true);
        assertFalse(mObserver.hasChanged());
        mObserver.setDeliverSelfNotifications(true);
        mContentObservable.dispatchChange(true);
        assertTrue(mObserver.hasChanged());

        mContentObservable.unregisterObserver(mObserver);
        mObserver.resetStatus();
        assertFalse(mObserver.hasChanged());
        mContentObservable.dispatchChange(false);
        assertFalse(mObserver.hasChanged());
    }

    private static class MyContentObserver extends ContentObserver {
        private boolean mHasChanged = false;
        private boolean mDeliverSelfNotifications = false;

        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHasChanged = true;
        }

        public boolean deliverSelfNotifications() {
            return mDeliverSelfNotifications ;
        }

        protected boolean hasChanged() {
            return mHasChanged;
        }

        protected void resetStatus() {
            mHasChanged = false;
        }

        protected void setDeliverSelfNotifications(boolean b) {
            mDeliverSelfNotifications = b;
        }
    }
}
