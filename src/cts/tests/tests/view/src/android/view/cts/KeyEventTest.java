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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.KeyCharacterMap.KeyData;
import android.view.KeyEvent.Callback;

/**
 * Test {@link KeyEvent}.
 */
@TestTargetClass(KeyEvent.class)
public class KeyEventTest extends AndroidTestCase {
    private KeyEvent mKeyEvent;
    private long mDownTime;
    private long mEventTime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);

        mDownTime = SystemClock.uptimeMillis();
        mEventTime = SystemClock.uptimeMillis();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {android.view.KeyEvent.class, long.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {long.class, long.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {long.class, long.class, int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {long.class, long.class, int.class, int.class, int.class, int.class,
                    int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {long.class, long.class, int.class, int.class, int.class, int.class,
                    int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {long.class, String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link KeyEvent}",
            method = "KeyEvent",
            args = {android.view.KeyEvent.class}
        )
    })
    public void testConstructor() {
        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);

        new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5);

        new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON);

        new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON, 1, 1);

        new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_SOFT_KEYBOARD);

        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        new KeyEvent(keyEvent);
        new KeyEvent(keyEvent, mEventTime, 1);

        new KeyEvent(mDownTime, "test", 0, KeyEvent.FLAG_SOFT_KEYBOARD);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getCharacters()}",
        method = "getCharacters",
        args = {}
    )
    public void testGetCharacters() {
        String characters = "android_test";
        mKeyEvent = new KeyEvent(mDownTime, characters, 0, KeyEvent.FLAG_SOFT_KEYBOARD);
        assertEquals(KeyEvent.ACTION_MULTIPLE, mKeyEvent.getAction());
        assertEquals(KeyEvent.KEYCODE_UNKNOWN, mKeyEvent.getKeyCode());
        assertEquals(characters, mKeyEvent.getCharacters());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        assertNull(mKeyEvent.getCharacters());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getMaxKeyCode()}",
        method = "getMaxKeyCode",
        args = {}
    )
    public void testGetMaxKeyCode() {
        assertTrue(KeyEvent.getMaxKeyCode() > 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isShiftPressed()}",
        method = "isShiftPressed",
        args = {}
    )
    public void testIsShiftPressed() {
        assertFalse(mKeyEvent.isShiftPressed());
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON);
        assertTrue(mKeyEvent.isShiftPressed());
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_ALT_ON);
        assertFalse(mKeyEvent.isShiftPressed());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getDeadChar(int, int)}",
        method = "getDeadChar",
        args = {int.class, int.class}
    )
    public void testGetDeadChar() {
        // decimal number of &egrave; is 232.
        assertEquals(232, KeyEvent.getDeadChar('`', 'e'));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getKeyData(KeyData)}",
        method = "getKeyData",
        args = {android.view.KeyCharacterMap.KeyData.class}
    )
    public void testGetKeyData() {
        KeyData keyData = new KeyData();
        char origDisplayLabel = keyData.displayLabel;
        char origNumber = keyData.number;
        char[] origMeta = new char[KeyData.META_LENGTH];
        origMeta[0] = keyData.meta[0];
        origMeta[1] = keyData.meta[1];
        origMeta[2] = keyData.meta[2];
        origMeta[3] = keyData.meta[3];

        assertTrue(mKeyEvent.getKeyData(keyData));
        // check whether KeyData has been updated.
        assertTrue(keyData.displayLabel != origDisplayLabel);
        assertTrue(keyData.number != origNumber);
        assertTrue(keyData.meta[0] != origMeta[0]);
        assertTrue(keyData.meta[1] != origMeta[1]);
        assertTrue(keyData.meta[2] != origMeta[2]);
        assertTrue(keyData.meta[3] != origMeta[3]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#dispatch(Callback)}",
        method = "dispatch",
        args = {android.view.KeyEvent.Callback.class}
    )
    public void testDispatch() {
        MockCallback callback = new MockCallback();
        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        callback.reset();
        assertFalse(callback.isKeyDown());
        assertTrue(mKeyEvent.dispatch(callback));
        assertTrue(callback.isKeyDown());
        assertEquals(KeyEvent.KEYCODE_0, callback.getKeyCode());
        assertSame(mKeyEvent, callback.getKeyEvent());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0);
        callback.reset();
        assertFalse(callback.isKeyUp());
        assertTrue(mKeyEvent.dispatch(callback));
        assertTrue(callback.isKeyUp());
        assertEquals(KeyEvent.KEYCODE_0, callback.getKeyCode());
        assertSame(mKeyEvent, callback.getKeyEvent());

        callback.reset();
        int count = 2;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_MULTIPLE,
                KeyEvent.KEYCODE_0, count);
        assertFalse(callback.isKeyMultiple());
        assertTrue(mKeyEvent.dispatch(callback));
        assertTrue(callback.isKeyMultiple());
        assertEquals(KeyEvent.KEYCODE_0, callback.getKeyCode());
        assertSame(mKeyEvent, callback.getKeyEvent());
        assertEquals(count, callback.getCount());

        callback.reset();
        count = 0;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_MULTIPLE,
                KeyEvent.KEYCODE_0, count);
        assertTrue(mKeyEvent.dispatch(callback));
        assertTrue(callback.isKeyMultiple());
        assertTrue(callback.isKeyDown());
        assertTrue(callback.isKeyUp());
        assertEquals(count, callback.getCount());
        assertEquals(KeyEvent.KEYCODE_0, callback.getKeyCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getMetaState()}",
        method = "getMetaState",
        args = {}
    )
    public void testGetMetaState() {
        int metaState = KeyEvent.META_ALT_ON;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_MULTIPLE,
                KeyEvent.KEYCODE_1, 1, metaState);
        assertEquals(metaState, mKeyEvent.getMetaState());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getEventTime()}",
        method = "getEventTime",
        args = {}
    )
    public void testGetEventTime() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5);
        assertEquals(mEventTime, mKeyEvent.getEventTime());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getDownTime()}",
        method = "getDownTime",
        args = {}
    )
    public void testGetDownTime() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5);
        assertEquals(mDownTime, mKeyEvent.getDownTime());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getUnicodeChar()}",
        method = "getUnicodeChar",
        args = {}
    )
    public void testGetUnicodeChar1() {
        // 48 is Unicode character of '0'
        assertEquals(48, mKeyEvent.getUnicodeChar());
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_9, 5, 0);
        // 57 is Unicode character of '9'
        assertEquals(57, mKeyEvent.getUnicodeChar());

        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_ALT_LEFT, 5, KeyEvent.META_SHIFT_ON);
        // 'ALT' key is not a type Unicode character.
        assertEquals(0, mKeyEvent.getUnicodeChar());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getUnicodeChar(int)}",
        method = "getUnicodeChar",
        args = {int.class}
    )
    public void testGetUnicodeChar2() {
        // 48 is Unicode character of '0'
        assertEquals(48, mKeyEvent.getUnicodeChar(MetaKeyKeyListener.META_CAP_LOCKED));
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_9, 5, 0);
        // 57 is Unicode character of '9'
        assertEquals(57, mKeyEvent.getUnicodeChar(0));

        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_ALT_LEFT, 5, KeyEvent.META_SHIFT_ON);
        // 'ALT' key is not a type Unicode character.
        assertEquals(0, mKeyEvent.getUnicodeChar(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getNumber()}",
        method = "getNumber",
        args = {}
    )
    public void testGetNumber() {
        // 48 is associated with key '0'
        assertEquals(48, mKeyEvent.getNumber());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3);
        // 51 is associated with key '3'
        assertEquals(51, mKeyEvent.getNumber());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isSymPressed()}",
        method = "isSymPressed",
        args = {}
    )
    public void testIsSymPressed() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SYM_ON);
        assertTrue(mKeyEvent.isSymPressed());

        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON);
        assertFalse(mKeyEvent.isSymPressed());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getDeviceId()}",
        method = "getDeviceId",
        args = {}
    )
    public void testGetDeviceId() {
        int deviceId = 1;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON, deviceId, 1);
        assertEquals(deviceId, mKeyEvent.getDeviceId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#toString()}",
        method = "toString",
        args = {}
    )
    public void testToString() {
        // make sure it does not throw any exception.
        mKeyEvent.toString();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isAltPressed()}",
        method = "isAltPressed",
        args = {}
    )
    public void testIsAltPressed() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_ALT_ON);
        assertTrue(mKeyEvent.isAltPressed());

        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0, 5,
                KeyEvent.META_SHIFT_ON);
        assertFalse(mKeyEvent.isAltPressed());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isModifierKey(int)}",
        method = "isModifierKey",
        args = {int.class}
    )
    public void testIsModifierKey() {
        assertTrue(KeyEvent.isModifierKey(KeyEvent.KEYCODE_SHIFT_LEFT));
        assertTrue(KeyEvent.isModifierKey(KeyEvent.KEYCODE_SHIFT_RIGHT));
        assertTrue(KeyEvent.isModifierKey(KeyEvent.KEYCODE_ALT_LEFT));
        assertTrue(KeyEvent.isModifierKey(KeyEvent.KEYCODE_ALT_RIGHT));
        assertTrue(KeyEvent.isModifierKey(KeyEvent.KEYCODE_SYM));
        assertFalse(KeyEvent.isModifierKey(KeyEvent.KEYCODE_0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getDisplayLabel()}",
        method = "getDisplayLabel",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetDisplayLabel() {
        assertTrue(mKeyEvent.getDisplayLabel() > 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isSystem()}",
        method = "isSystem",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "javadoc does not tell user the system key set.")
    public void testIsSystem() {
        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SOFT_RIGHT);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CALL);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENDCALL);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAMERA);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FOCUS);
        assertTrue(mKeyEvent.isSystem());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        assertFalse(mKeyEvent.isSystem());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#isPrintingKey()}",
        method = "isPrintingKey",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "javadoc does not tell user the printing key set.")
    public void testIsPrintingKey() {
        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, Character.SPACE_SEPARATOR);
        assertTrue(mKeyEvent.isPrintingKey());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, Character.LINE_SEPARATOR);
        assertTrue(mKeyEvent.isPrintingKey());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, Character.PARAGRAPH_SEPARATOR);
        assertTrue(mKeyEvent.isPrintingKey());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, Character.CONTROL);
        assertTrue(mKeyEvent.isPrintingKey());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, Character.FORMAT);
        assertTrue(mKeyEvent.isPrintingKey());

        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        assertTrue(mKeyEvent.isPrintingKey());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getMatch(char[])}",
        method = "getMatch",
        args = {char[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add NPE description in javadoc.")
    public void testGetMatch1() {
        char[] codes1 = new char[] { '0', '1', '2' };
        assertEquals('0', mKeyEvent.getMatch(codes1));

        char[] codes2 = new char[] { 'A', 'B', 'C' };
        assertEquals('\0', mKeyEvent.getMatch(codes2));

        char[] codes3 = { '2', 'S' };
        mKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_S);
        assertEquals('S', mKeyEvent.getMatch(codes3));

        try {
            mKeyEvent.getMatch(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // empty
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getAction()}",
        method = "getAction",
        args = {}
    )
    public void testGetAction() {
        assertEquals(KeyEvent.ACTION_DOWN, mKeyEvent.getAction());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getRepeatCount()}",
        method = "getRepeatCount",
        args = {}
    )
    public void testGetRepeatCount() {
        int repeatCount = 1;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_MULTIPLE,
                KeyEvent.KEYCODE_0, repeatCount);
        assertEquals(repeatCount, mKeyEvent.getRepeatCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#writeToParcel(Parcel, int)}",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel parcel = Parcel.obtain();
        mKeyEvent.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.setDataPosition(0);

        KeyEvent keyEvent = KeyEvent.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        assertEquals(mKeyEvent.getAction(), keyEvent.getAction());
        assertEquals(mKeyEvent.getKeyCode(), keyEvent.getKeyCode());
        assertEquals(mKeyEvent.getRepeatCount(), keyEvent.getRepeatCount());
        assertEquals(mKeyEvent.getMetaState(), keyEvent.getMetaState());
        assertEquals(mKeyEvent.getDeviceId(), keyEvent.getDeviceId());
        assertEquals(mKeyEvent.getScanCode(), keyEvent.getScanCode());
        assertEquals(mKeyEvent.getFlags(), keyEvent.getFlags());
        assertEquals(mKeyEvent.getDownTime(), keyEvent.getDownTime());
        assertEquals(mKeyEvent.getEventTime(), keyEvent.getEventTime());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#describeContents()}, this function always returns 0",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testDescribeContents() {
        // make sure it never shrow any exception.
        mKeyEvent.describeContents();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getKeyCode()}",
        method = "getKeyCode",
        args = {}
    )
    public void testGetKeyCode() {
        assertEquals(KeyEvent.KEYCODE_0, mKeyEvent.getKeyCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getFlags()}",
        method = "getFlags",
        args = {}
    )
    public void testGetFlags() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_WOKE_HERE);
        assertEquals(KeyEvent.FLAG_WOKE_HERE, mKeyEvent.getFlags());

        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_SOFT_KEYBOARD);
        assertEquals(KeyEvent.FLAG_SOFT_KEYBOARD, mKeyEvent.getFlags());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link KeyEvent#getScanCode()}",
        method = "getScanCode",
        args = {}
    )
    public void testGetScanCode() {
        int scanCode = 1;
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, scanCode);
        assertEquals(scanCode, mKeyEvent.getScanCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "changeAction",
        args = {android.view.KeyEvent.class, int.class}
    )
    public void testChangeAction() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_WOKE_HERE);

        KeyEvent newEvent = KeyEvent.changeAction(mKeyEvent, KeyEvent.ACTION_UP);
        assertEquals(KeyEvent.ACTION_UP, newEvent.getAction());
        assertEquals(mKeyEvent.getFlags(), newEvent.getFlags());
        assertEquals(mKeyEvent.getCharacters(), newEvent.getCharacters());
        assertEquals(mKeyEvent.getDisplayLabel(), newEvent.getDisplayLabel());
        assertEquals(mKeyEvent.getDeviceId(), newEvent.getDeviceId());
        assertEquals(mKeyEvent.getDownTime(), newEvent.getDownTime());
        assertEquals(mKeyEvent.getEventTime(), newEvent.getEventTime());
        assertEquals(mKeyEvent.getKeyCode(), newEvent.getKeyCode());
        assertEquals(mKeyEvent.getRepeatCount(), newEvent.getRepeatCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "changeFlags",
        args = {android.view.KeyEvent.class, int.class}
    )
    public void testChangeFlags() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_WOKE_HERE);

        KeyEvent newEvent = KeyEvent.changeFlags(mKeyEvent, KeyEvent.FLAG_FROM_SYSTEM);
        assertEquals(KeyEvent.FLAG_FROM_SYSTEM, newEvent.getFlags());
        assertEquals(mKeyEvent.getAction(), newEvent.getAction());
        assertEquals(mKeyEvent.getCharacters(), newEvent.getCharacters());
        assertEquals(mKeyEvent.getDisplayLabel(), newEvent.getDisplayLabel());
        assertEquals(mKeyEvent.getDeviceId(), newEvent.getDeviceId());
        assertEquals(mKeyEvent.getDownTime(), newEvent.getDownTime());
        assertEquals(mKeyEvent.getEventTime(), newEvent.getEventTime());
        assertEquals(mKeyEvent.getKeyCode(), newEvent.getKeyCode());
        assertEquals(mKeyEvent.getRepeatCount(), newEvent.getRepeatCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "changeTimeRepeat",
        args = {android.view.KeyEvent.class, long.class, int.class}
    )
    public void testChangeTimeRepeat() {
        mKeyEvent = new KeyEvent(mDownTime, mEventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_0, 5, KeyEvent.META_SHIFT_ON, 1, 1, KeyEvent.FLAG_WOKE_HERE);

        long newEventTime = SystemClock.uptimeMillis();
        int newRepeat = mKeyEvent.getRepeatCount() + 1;
        KeyEvent newEvent = KeyEvent.changeTimeRepeat(mKeyEvent, newEventTime, newRepeat);
        assertEquals(newEventTime, newEvent.getEventTime());
        assertEquals(newRepeat, newEvent.getRepeatCount());
        assertEquals(mKeyEvent.getFlags(), newEvent.getFlags());
        assertEquals(mKeyEvent.getAction(), newEvent.getAction());
        assertEquals(mKeyEvent.getCharacters(), newEvent.getCharacters());
        assertEquals(mKeyEvent.getDisplayLabel(), newEvent.getDisplayLabel());
        assertEquals(mKeyEvent.getDeviceId(), newEvent.getDeviceId());
        assertEquals(mKeyEvent.getDownTime(), newEvent.getDownTime());
        assertEquals(mKeyEvent.getKeyCode(), newEvent.getKeyCode());
    }

    private class MockCallback implements Callback {
        private boolean mIsKeyDown;
        private boolean mIsKeyUp;
        private boolean mIsMultiple;
        private int mKeyCode;
        private KeyEvent mKeyEvent;
        private int mCount;

        public boolean isKeyDown() {
            return mIsKeyDown;
        }

        public boolean isKeyUp() {
            return mIsKeyUp;
        }

        public boolean isKeyMultiple() {
            return mIsMultiple;
        }

        public int getKeyCode() {
            return mKeyCode;
        }

        public KeyEvent getKeyEvent() {
            return mKeyEvent;
        }

        public int getCount() {
            return mCount;
        }

        public void reset() {
            mIsKeyDown = false;
            mIsKeyUp = false;
            mIsMultiple = false;
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            mIsKeyDown = true;
            mKeyCode = keyCode;
            mKeyEvent = event;
            return true;
        }

        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            return false;
        }
        
        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
            mIsMultiple = true;
            mKeyCode = keyCode;
            mKeyEvent = event;
            mCount = count;
            if (count < 1) {
                return false; // this key event never repeat.
            }
            return true;
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            mIsKeyUp = true;
            mKeyCode = keyCode;
            mKeyEvent = event;
            return true;
        }
    }
}
