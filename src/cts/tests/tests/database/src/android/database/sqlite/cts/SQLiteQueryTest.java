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

package android.database.sqlite.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;
import junit.framework.TestCase;

@TestTargetClass(android.database.sqlite.SQLiteQuery.class)
public class SQLiteQueryTest extends TestCase {
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindDouble",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindLong",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindNull",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindString",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "toString",
            args = {}
        )
    })
    @ToBeFixed(bug = "1686574", explanation = "can not get an instance of SQLiteQuery" +
            " or construct it directly for testing")
    public void testMethods() {
        // cannot obtain an instance of SQLiteQuery
    }
}
