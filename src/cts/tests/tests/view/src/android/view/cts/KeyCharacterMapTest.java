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

package android.view.cts;

import android.test.AndroidTestCase;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.KeyCharacterMap.KeyData;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(KeyCharacterMap.class)
public class KeyCharacterMapTest extends AndroidTestCase {

    private KeyCharacterMap mKeyCharacterMap;
    private final char[] chars = {'A', 'B', 'C'};

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mKeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isPrintingKey",
        args = {int.class}
    )
    public void testIsPrintingKey() throws Exception {

        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_UNKNOWN));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SOFT_LEFT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SOFT_RIGHT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_HOME));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_BACK));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_CALL));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ENDCALL));

        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_0));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_1));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_2));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_3));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_4));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_5));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_6));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_7));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_8));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_9));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_STAR));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_POUND));

        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DPAD_UP));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DPAD_DOWN));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DPAD_LEFT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DPAD_RIGHT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DPAD_CENTER));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_VOLUME_UP));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_VOLUME_DOWN));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_POWER));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_CAMERA));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_CLEAR));

        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_A));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_B));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_C));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_D));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_E));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_F));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_G));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_H));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_I));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_J));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_K));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_L));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_M));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_N));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_O));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_P));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_Q));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_R));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_S));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_T));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_U));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_V));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_W));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_X));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_Y));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_Z));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_COMMA));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_PERIOD));

        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ALT_LEFT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ALT_RIGHT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SHIFT_LEFT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SHIFT_RIGHT));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_TAB));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SPACE));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_NUM));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_EXPLORER));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ENVELOPE));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ENTER));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_DEL));

        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_GRAVE));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_MINUS));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_EQUALS));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_LEFT_BRACKET));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_RIGHT_BRACKET));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_BACKSLASH));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SEMICOLON));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_APOSTROPHE));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SLASH));
        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_AT));

        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_NUM));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_HEADSETHOOK));

        assertTrue(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_PLUS));

        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_MENU));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_NOTIFICATION));
        assertFalse(mKeyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_SEARCH));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "load",
        args = {int.class}
    )
    public void testLoad() throws Exception {
        mKeyCharacterMap = null;
        mKeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        assertNotNull(mKeyCharacterMap);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNumber",
        args = {int.class}
    )
    public void testGetNumber() throws Exception {
        assertEquals('0', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_0));
        assertEquals('1', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_1));
        assertEquals('2', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_2));
        assertEquals('3', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_3));
        assertEquals('4', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_4));
        assertEquals('5', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_5));
        assertEquals('6', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_6));
        assertEquals('7', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_7));
        assertEquals('8', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_8));
        assertEquals('9', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_9));
        assertEquals('*', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_STAR));
        assertEquals('#', mKeyCharacterMap.getNumber(KeyEvent.KEYCODE_POUND));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMatch",
        args = {int.class, char[].class}
    )
    public void testGetMatch1() throws Exception {
        try {
            mKeyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null);
            fail("should throw exception");
        } catch (Exception e) {
        }

        assertEquals('\0', mKeyCharacterMap.getMatch(getCharacterKeyCode('E'), chars));
        assertEquals('A', mKeyCharacterMap.getMatch(getCharacterKeyCode('A'), chars));
        assertEquals('B', mKeyCharacterMap.getMatch(getCharacterKeyCode('B'), chars));
    }

    private int getCharacterKeyCode(char oneChar) {
        // Lowercase the character to avoid getting modifiers in the KeyEvent array.
        char[] chars = new char[] {Character.toLowerCase(oneChar)};
        KeyEvent[] events = mKeyCharacterMap.getEvents(chars);
        return events[0].getKeyCode();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMatch",
        args = {int.class, char[].class, int.class}
    )
    public void testGetMatch2() throws Exception {
        try {
            mKeyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null, 1);
            fail("should throw exception");
        } catch (Exception e) {
        }
        assertEquals('\0', mKeyCharacterMap.getMatch(1000, chars, 2));
        assertEquals('\0', mKeyCharacterMap.getMatch(10000, chars, 2));
        assertEquals('\0', mKeyCharacterMap.getMatch(getCharacterKeyCode('E'), chars));
        assertEquals('A', mKeyCharacterMap.getMatch(getCharacterKeyCode('A'), chars));
        assertEquals('B', mKeyCharacterMap.getMatch(getCharacterKeyCode('B'), chars));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getKeyboardType",
        args = {}
    )
    public void testGetKeyboardType() throws Exception {
        mKeyCharacterMap.getKeyboardType();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEvents",
            args = {char[].class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "getDisplayLabel",
            args = {int.class}
        )
    })
    public void testGetEvents() {
        try {
            mKeyCharacterMap.getEvents(null);
            fail("should throw exception");
        } catch (Exception e) {
        }
        CharSequence mCharSequence = "TestMessage123";
        int len = mCharSequence.length();
        char[] charsArray = new char[len];
        TextUtils.getChars(mCharSequence, 1, len, charsArray, 0);
        mKeyCharacterMap.getEvents(charsArray);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getKeyData",
            args = {int.class, android.view.KeyCharacterMap.KeyData.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "get",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "getDeadChar",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "deviceHasKey",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "deviceHasKeys",
            args = {int[].class}
        )
    })
    public void testGetKeyData() throws Exception {
        KeyData result = new KeyData();
        result.meta = new char[2];
        try {
            mKeyCharacterMap.getKeyData(KeyEvent.KEYCODE_HOME, result);
            fail("should throw exception");
        } catch (Exception e) {
        }
        result.meta = new char[4];
        assertFalse(mKeyCharacterMap.getKeyData(KeyEvent.KEYCODE_HOME, result));
        assertTrue(mKeyCharacterMap.getKeyData(KeyEvent.KEYCODE_0, result));
        assertEquals(48, result.meta[0]);

        // here just call deviceHasKey and deviceHasKeys.
        KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_0);
        final int[] keyChar = new int[] {
                KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_3
        };
        boolean[] keys = KeyCharacterMap.deviceHasKeys(keyChar);
        assertEquals(keyChar.length, keys.length);
    }

}
