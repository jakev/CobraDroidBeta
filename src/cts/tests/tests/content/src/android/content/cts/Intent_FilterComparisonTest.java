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

package android.content.cts;

import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(Intent.FilterComparison.class)
public class Intent_FilterComparisonTest extends AndroidTestCase {

    FilterComparison mFilterComparison;
    Intent mIntent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFilterComparison = null;
        mIntent = new Intent();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent.FilterComparison",
        args = {android.content.Intent.class}
    )
    public void testConstructor() {
        mFilterComparison = null;
        // new the FilterComparison instance
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertEquals(mIntent.filterHashCode(), mFilterComparison.hashCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        FilterComparison target = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertTrue(mFilterComparison.equals(target));
        target = new Intent.FilterComparison(new Intent("test"));
        assertFalse(mFilterComparison.equals(target));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntent",
        args = {}
    )
    public void testGetIntent() {
        mFilterComparison = new Intent.FilterComparison(mIntent);
        assertNotNull(mFilterComparison);
        assertTrue(mFilterComparison.getIntent().equals(mIntent));
    }

}
