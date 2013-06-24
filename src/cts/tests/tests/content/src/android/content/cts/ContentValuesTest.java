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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.os.Parcel;
import android.test.AndroidTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(ContentValues.class)
public class ContentValuesTest extends AndroidTestCase {
    ContentValues mContentValues;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentValues = new ContentValues();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ContentValues.",
            method = "ContentValues",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ContentValues.",
            method = "ContentValues",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ContentValues.",
            method = "ContentValues",
            args = {android.content.ContentValues.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NullPointerException and" +
            " IllegalArgumentException")
    public void testConstructor() {
        new ContentValues();
        new ContentValues(5);
        new ContentValues(mContentValues);

        try {
            new ContentValues(-1);
            fail("There should be a IllegalArgumentException thrown out.");
        } catch (IllegalArgumentException e) {
            // expected, test success.
        }

        try {
            new ContentValues(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test valueSet().",
        method = "valueSet",
        args = {}
    )
    public void testValueSet() {
        Set<Map.Entry<String, Object>> map;
        assertNotNull(map = mContentValues.valueSet());
        assertTrue(map.isEmpty());

        mContentValues.put("Long", 10L);
        mContentValues.put("Integer", 201);

        assertNotNull(map = mContentValues.valueSet());
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test putNull(String key).",
        method = "putNull",
        args = {java.lang.String.class}
    )
    public void testPutNull() {
        mContentValues.putNull("key");
        assertNull(mContentValues.get("key"));

        mContentValues.putNull("value");
        assertNull(mContentValues.get("value"));

        mContentValues.putNull("");
        assertNull(mContentValues.get(""));

        // input null as param
        mContentValues.putNull(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsLong(String key).",
        method = "getAsLong",
        args = {java.lang.String.class}
    )
    public void testGetAsLong() {
        Long expected = 10L;
        mContentValues.put("Long", expected);
        assertEquals(expected, mContentValues.getAsLong("Long"));

        expected = -1000L;
        mContentValues.put("Long", expected);
        assertEquals(expected, mContentValues.getAsLong("Long"));

        // input null as param
        assertNull(mContentValues.getAsLong(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsByte(String key).",
        method = "getAsByte",
        args = {java.lang.String.class}
    )
    public void testGetAsByte() {
        Byte expected = 'a';
        mContentValues.put("Byte", expected);
        assertEquals(expected, mContentValues.getAsByte("Byte"));

        expected = 'z';
        mContentValues.put("Byte", expected);
        assertEquals(expected, mContentValues.getAsByte("Byte"));

        // input null as param
        assertNull(mContentValues.getAsByte(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsInteger(String key).",
        method = "getAsInteger",
        args = {java.lang.String.class}
    )
    public void testGetAsInteger() {
        Integer expected = 20;
        mContentValues.put("Integer", expected);
        assertEquals(expected, mContentValues.getAsInteger("Integer"));

        expected = -20000;
        mContentValues.put("Integer", expected);
        assertEquals(expected, mContentValues.getAsInteger("Integer"));

        // input null as param
        assertNull(mContentValues.getAsInteger(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test size().",
        method = "size",
        args = {}
    )
    public void testSize() {
        assertEquals(0, mContentValues.size());

        mContentValues.put("Integer", 10);
        mContentValues.put("Long", 10L);
        assertEquals(2, mContentValues.size());

        mContentValues.put("String", "b");
        mContentValues.put("Boolean", false);
        assertEquals(4, mContentValues.size());

        mContentValues.clear();
        assertEquals(0, mContentValues.size());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsShort(String key).",
        method = "getAsShort",
        args = {java.lang.String.class}
    )
    public void testGetAsShort() {
        Short expected = 20;
        mContentValues.put("Short", expected);
        assertEquals(expected, mContentValues.getAsShort("Short"));

        expected = -200;
        mContentValues.put("Short", expected);
        assertEquals(expected, mContentValues.getAsShort("Short"));

        // input null as param
        assertNull(mContentValues.getAsShort(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test hashCode().",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        assertEquals(0, mContentValues.hashCode());

        mContentValues.put("Float", 2.2F);
        mContentValues.put("Short", 12);
        assertTrue(0 != mContentValues.hashCode());

        int hashcode = mContentValues.hashCode();
        mContentValues.remove("Short");
        assertTrue(hashcode != mContentValues.hashCode());

        mContentValues.put("Short", 12);
        assertTrue(hashcode == mContentValues.hashCode());

        mContentValues.clear();
        assertEquals(0, mContentValues.hashCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsFloat(String key).",
        method = "getAsFloat",
        args = {java.lang.String.class}
    )
    public void testGetAsFloat() {
        Float expected = 1.0F;
        mContentValues.put("Float", expected);
        assertEquals(expected, mContentValues.getAsFloat("Float"));

        expected = -5.5F;
        mContentValues.put("Float", expected);
        assertEquals(expected, mContentValues.getAsFloat("Float"));

        // input null as param
        assertNull(mContentValues.getAsFloat(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsBoolean(String key).",
        method = "getAsBoolean",
        args = {java.lang.String.class}
    )
    public void testGetAsBoolean() {
        mContentValues.put("Boolean", true);
        assertTrue(mContentValues.getAsBoolean("Boolean"));

        mContentValues.put("Boolean", false);
        assertFalse(mContentValues.getAsBoolean("Boolean"));

        // input null as param
        assertNull(mContentValues.getAsBoolean(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test toString().",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(mContentValues.toString());

        mContentValues.put("Float", 1.1F);
        assertNotNull(mContentValues.toString());
        assertTrue(mContentValues.toString().length() > 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test get(String key).",
        method = "get",
        args = {java.lang.String.class}
    )
    public void testGet() {
        Object expected = "android";
        mContentValues.put("Object", "android");
        assertSame(expected, mContentValues.get("Object"));

        expected = 20;
        mContentValues.put("Object", 20);
        assertSame(expected, mContentValues.get("Object"));

        // input null as params
        assertNull(mContentValues.get(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test equals(Object object).",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        mContentValues.put("Boolean", false);
        mContentValues.put("String", "string");

        ContentValues cv = new ContentValues();
        cv.put("Boolean", false);
        cv.put("String", "string");

        assertTrue(mContentValues.equals(cv));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test equals(Object object).",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsFailure() {
        // the target object is not an instance of ContentValues.
        assertFalse(mContentValues.equals(new String()));

        // the two object is not equals
        mContentValues.put("Boolean", false);
        mContentValues.put("String", "string");

        ContentValues cv = new ContentValues();
        cv.put("Boolean", true);
        cv.put("String", "111");

        assertFalse(mContentValues.equals(cv));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsDouble(String key).",
        method = "getAsDouble",
        args = {java.lang.String.class}
    )
    public void testGetAsDouble() {
        Double expected = 10.2;
        mContentValues.put("Double", expected);
        assertEquals(expected, mContentValues.getAsDouble("Double"));

        expected = -15.4;
        mContentValues.put("Double", expected);
        assertEquals(expected, mContentValues.getAsDouble("Double"));

        // input null as params
        assertNull(mContentValues.getAsDouble(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, String value).",
        method = "put",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testPutString() {
        String expected = "cts";
        mContentValues.put("String", expected);
        assertSame(expected, mContentValues.getAsString("String"));

        expected = "android";
        mContentValues.put("String", expected);
        assertSame(expected, mContentValues.getAsString("String"));

        // input null as params
        mContentValues.put(null, (String)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Byte value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Byte.class}
    )
    public void testPutByte() {
        Byte expected = 'a';
        mContentValues.put("Byte", expected);
        assertSame(expected, mContentValues.getAsByte("Byte"));

        expected = 'z';
        mContentValues.put("Byte", expected);
        assertSame(expected, mContentValues.getAsByte("Byte"));

        // input null as params
        mContentValues.put(null, (Byte)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Short value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Short.class}
    )
    public void testPutShort() {
        Short expected = 20;
        mContentValues.put("Short", expected);
        assertEquals(expected, mContentValues.getAsShort("Short"));

        expected = -200;
        mContentValues.put("Short", expected);
        assertEquals(expected, mContentValues.getAsShort("Short"));

        // input null as params
        mContentValues.put(null, (Short)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Integer value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Integer.class}
    )
    public void testPutInteger() {
        Integer expected = 20;
        mContentValues.put("Integer", expected);
        assertEquals(expected, mContentValues.getAsInteger("Integer"));

        expected = -20000;
        mContentValues.put("Integer", expected);
        assertEquals(expected, mContentValues.getAsInteger("Integer"));

        // input null as params
        mContentValues.put(null, (Integer)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Long value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Long.class}
    )
    public void testPutLong() {
        Long expected = 10L;
        mContentValues.put("Long", expected);
        assertEquals(expected, mContentValues.getAsLong("Long"));

        expected = -1000L;
        mContentValues.put("Long", expected);
        assertEquals(expected, mContentValues.getAsLong("Long"));

        // input null as params
        mContentValues.put(null, (Long)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Float value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Float.class}
    )
    public void testPutFloat() {
        Float expected = 1.0F;
        mContentValues.put("Float", expected);
        assertEquals(expected, mContentValues.getAsFloat("Float"));

        expected = -5.5F;
        mContentValues.put("Float", expected);
        assertEquals(expected, mContentValues.getAsFloat("Float"));

        // input null as params
        mContentValues.put(null, (Float)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Double value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Double.class}
    )
    public void testPutDouble() {
        Double expected = 10.2;
        mContentValues.put("Double", expected);
        assertEquals(expected, mContentValues.getAsDouble("Double"));

        expected = -15.4;
        mContentValues.put("Double", expected);
        assertEquals(expected, mContentValues.getAsDouble("Double"));

        // input null as params
        mContentValues.put(null, (Double)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, Boolean value).",
        method = "put",
        args = {java.lang.String.class, java.lang.Boolean.class}
    )
    public void testPutBoolean() {
        // set the expected value
        mContentValues.put("Boolean", true);
        assertTrue(mContentValues.getAsBoolean("Boolean"));

        mContentValues.put("Boolean", false);
        assertFalse(mContentValues.getAsBoolean("Boolean"));

        // input null as params
        mContentValues.put(null, (Boolean)null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test put(String key, byte[] value).",
        method = "put",
        args = {java.lang.String.class, byte[].class}
    )
    public void testPutByteArray() {
        byte[] expected = new byte[] {'1', '2', '3', '4'};
        mContentValues.put("byte[]", expected);
        assertSame(expected, mContentValues.getAsByteArray("byte[]"));

        // input null as params
        mContentValues.put(null, (byte[])null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test containsKey(String key).",
        method = "containsKey",
        args = {java.lang.String.class}
    )
    public void testContainsKey() {
        mContentValues.put("Double", 10.2);
        mContentValues.put("Float", 1.0F);

        assertTrue(mContentValues.containsKey("Double"));
        assertTrue(mContentValues.containsKey("Float"));

        assertFalse(mContentValues.containsKey("abc"));
        assertFalse(mContentValues.containsKey("cts"));

        // input null as param
        assertFalse(mContentValues.containsKey(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test clear().",
        method = "clear",
        args = {}
    )
    public void testClear() {
        assertEquals(0, mContentValues.size());

        mContentValues.put("Double", 10.2);
        mContentValues.put("Float", 1.0F);
        assertEquals(2, mContentValues.size());

        mContentValues.clear();
        assertEquals(0, mContentValues.size());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test putStringArrayList(String key, ArrayList<String> value) and "
                    + "getStringArrayList(String key).",
            method = "putStringArrayList",
            args = {java.lang.String.class, java.util.ArrayList.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test putStringArrayList(String key, ArrayList<String> value) and "
                    + "getStringArrayList(String key).",
            method = "getStringArrayList",
            args = {java.lang.String.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testAccessStringArrayList() {
        // set the expected value
        ArrayList<String> expected = new ArrayList<String>();
        expected.add(0, "cts");
        expected.add(1, "android");

        mContentValues.putStringArrayList("StringArrayList", expected);
        assertSame(expected, mContentValues.getStringArrayList("StringArrayList"));

        // input null as params
        mContentValues.putStringArrayList(null, null);
        assertNull(mContentValues.getStringArrayList(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test remove(String key).",
        method = "remove",
        args = {java.lang.String.class}
    )
    public void testRemove() {
        assertEquals(0, mContentValues.size());

        mContentValues.put("Double", 10.2);
        mContentValues.put("Float", 1.0F);
        mContentValues.put("Integer", -11);
        mContentValues.put("Boolean", false);
        assertEquals(4, mContentValues.size());

        mContentValues.remove("Integer");
        assertEquals(3, mContentValues.size());

        mContentValues.remove("Double");
        assertEquals(2, mContentValues.size());

        mContentValues.remove("Boolean");
        assertEquals(1, mContentValues.size());

        mContentValues.remove("Float");
        assertEquals(0, mContentValues.size());

        // remove null
        mContentValues.remove(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsString(String key).",
        method = "getAsString",
        args = {java.lang.String.class}
    )
    public void testGetAsString() {
        String expected = "cts";
        mContentValues.put("String", expected);
        assertSame(expected, mContentValues.getAsString("String"));

        expected = "android";
        mContentValues.put("String", expected);
        assertSame(expected, mContentValues.getAsString("String"));

        // input null as param
        assertNull(mContentValues.getAsString(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAsByteArray(String key).",
        method = "getAsByteArray",
        args = {java.lang.String.class}
    )
    public void testGetAsByteArray() {
        byte[] expected = new byte[] {'1', '2', '3', '4'};
        mContentValues.put("byte[]", expected);
        assertSame(expected, mContentValues.getAsByteArray("byte[]"));

        // input null as param
        assertNull(mContentValues.getAsByteArray(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel parcel, int flags).",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    @SuppressWarnings({ "unchecked" })
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();

        mContentValues.put("Integer", -110);
        mContentValues.put("String", "cts");
        mContentValues.put("Boolean", false);

        mContentValues.writeToParcel(p, 0);

        p.setDataPosition(0);
        HashMap<String, Object> values = p.readHashMap(ClassLoader.getSystemClassLoader());
        assertNotNull(values);
        assertEquals(3, values.size());

        assertEquals(-110, values.get("Integer"));
        assertEquals("cts", values.get("String"));
        assertEquals(false, values.get("Boolean"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel parcel, int flags).",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NullPointerException")
    public void testWriteToParcelFailure() {
        try {
            mContentValues.writeToParcel(null, -1);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents(). This method does nothing, and always return 0",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mContentValues.describeContents());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test putAll(ContentValues other).",
        method = "putAll",
        args = {android.content.ContentValues.class}
    )
    public void testPutAll() {
        assertEquals(0, mContentValues.size());

        mContentValues.put("Integer", -11);
        assertEquals(1, mContentValues.size());

        ContentValues cv = new ContentValues();
        cv.put("String", "cts");
        cv.put("Boolean", true);
        assertEquals(2, cv.size());

        mContentValues.putAll(cv);
        assertEquals(3, mContentValues.size());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test putAll(ContentValues other).",
        method = "putAll",
        args = {android.content.ContentValues.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NullPointerException")
    public void testPutAllFailure() {
        try {
            mContentValues.putAll(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }
}
