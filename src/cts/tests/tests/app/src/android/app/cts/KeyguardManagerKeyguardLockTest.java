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

import android.app.KeyguardManager;
import android.test.InstrumentationTestCase;

@TestTargetClass(KeyguardManager.KeyguardLock.class)
public class KeyguardManagerKeyguardLockTest extends InstrumentationTestCase {

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "disableKeyguard",
        args = {}
    )
    public void testDisableKeyguard() {
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "reenableKeyguard",
        args = {}
    )
    public void testReenableKeyguard() {
    }
}
