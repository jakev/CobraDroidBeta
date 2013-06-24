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

package android.graphics.drawable.cts;

import junit.framework.TestCase;
import android.graphics.drawable.Drawable;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Drawable.ConstantState.class)
public class Drawable_ConstantStateTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getChangingConfigurations()",
        method = "getChangingConfigurations",
        args = {}
    )
    public void testGetChangingConfigurations() {
        // getChangingConfigurations is an abstract function.
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test newDrawable()",
        method = "newDrawable",
        args = {}
    )
    public void testNewDrawable() {
        // newDrawable is an abstract function.
    }
}
