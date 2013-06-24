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

package android.net.cts;

import android.net.NetworkInfo.State;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(State.class)
public class NetworkInfo_StateTest extends AndroidTestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test valueOf(String name).",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(State.CONNECTED, State.valueOf("CONNECTED"));
        assertEquals(State.CONNECTING, State.valueOf("CONNECTING"));
        assertEquals(State.DISCONNECTED, State.valueOf("DISCONNECTED"));
        assertEquals(State.DISCONNECTING, State.valueOf("DISCONNECTING"));
        assertEquals(State.SUSPENDED, State.valueOf("SUSPENDED"));
        assertEquals(State.UNKNOWN, State.valueOf("UNKNOWN"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test values().",
        method = "values",
        args = {}
    )
    public void testValues() {
        State[] expected = State.values();
        assertEquals(6, expected.length);
        assertEquals(State.CONNECTING, expected[0]);
        assertEquals(State.CONNECTED, expected[1]);
        assertEquals(State.SUSPENDED, expected[2]);
        assertEquals(State.DISCONNECTING, expected[3]);
        assertEquals(State.DISCONNECTED, expected[4]);
        assertEquals(State.UNKNOWN, expected[5]);
    }
}
