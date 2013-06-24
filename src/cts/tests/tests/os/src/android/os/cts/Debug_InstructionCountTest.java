/*
 * Copyright (C) 2010 The Android Open Source Project
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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.os.Debug;

import junit.framework.TestCase;

@TestTargetClass(Debug.InstructionCount.class)
public class Debug_InstructionCountTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Debug.InstructionCount",
            args = {}
            ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetAndStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "collect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "globalTotal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "globalMethodInvocations",
            args = {}
        )
    })
    public void testDebugInstructionCount() {
        Debug.InstructionCount instructionCount = new Debug.InstructionCount();

        assertTrue(instructionCount.resetAndStart());
        addTest(1, 2);
        instructionCount.collect();
        int insCountsFirst = instructionCount.globalTotal();
        int methodInvFirst = instructionCount.globalMethodInvocations();

        assertTrue(instructionCount.resetAndStart());
        addTest(1, 2);
        addTest(1, 2);
        instructionCount.collect();
        int insCountsSecond = instructionCount.globalTotal();
        int methodInvSecond = instructionCount.globalMethodInvocations();

        assertTrue(instructionCount.resetAndStart());
        addTest(1, 2);
        addTest(1, 2);
        addTest(1, 2);
        instructionCount.collect();
        int insCountsThird = instructionCount.globalTotal();
        int methodInvThird = instructionCount.globalMethodInvocations();

        assertEquals(insCountsThird - insCountsFirst, (insCountsSecond - insCountsFirst) * 2);
        assertEquals(methodInvThird - methodInvFirst, (methodInvSecond - methodInvFirst) * 2);
    }

    // must not be private, otherwise javac may inline the code
    protected int addTest(int a, int b) {
        return a + b;
    }
}
