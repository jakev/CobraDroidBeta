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

import android.content.Context;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Test {@link AbstractCursor}.
 */
@TestTargetClass(AbstractCursor.class)
public class AbstractCursorTest extends InstrumentationTestCase {
    private static final int POSITION0 = 0;
    private static final int POSITION1 = 1;
    private  static final int ROW_MAX = 10;
    private static final int DATA_COUNT = 10;
    private static final String[] COLUMN_NAMES1 = new String[] {
        "_id",             // 0
        "number"           // 1
    };
    private static final String[] COLUMN_NAMES = new String[] { "name", "number", "profit" };
    private TestAbstractCursor mTestAbstractCursor;
    private Object mLockObj = new Object();

    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    private AbstractCursor mDatabaseCursor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setupDatabase();
        ArrayList<ArrayList> list = createTestList(ROW_MAX, COLUMN_NAMES.length);
        mTestAbstractCursor = new TestAbstractCursor(COLUMN_NAMES, list);
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabaseCursor.close();
        mTestAbstractCursor.close();
        mDatabase.close();
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AbstractCursor",
        args = {}
    )
    public void testConstructor() {
        TestAbstractCursor abstractCursor = new TestAbstractCursor();
        assertEquals(-1, abstractCursor.getPosition());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBlob",
        args = {int.class}
    )
    public void testGetBlob() {
        try {
            mTestAbstractCursor.getBlob(0);
            fail("getBlob should throws a UnsupportedOperationException here");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerDataSetObserver",
            args = {android.database.DataSetObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterDataSetObserver",
            args = {android.database.DataSetObserver.class}
        )
    })
    public void testRegisterDataSetObserver() {
        MockDataSetObserver datasetObserver = new MockDataSetObserver();

        try {
            mDatabaseCursor.unregisterDataSetObserver(datasetObserver);
            fail("Can't unregister DataSetObserver before it is registered.");
        } catch (IllegalStateException e) {
            // expected
        }

        mDatabaseCursor.registerDataSetObserver(datasetObserver);

        try {
            mDatabaseCursor.registerDataSetObserver(datasetObserver);
            fail("Can't register DataSetObserver twice before unregister it.");
        } catch (IllegalStateException e) {
            // expected
        }

        mDatabaseCursor.unregisterDataSetObserver(datasetObserver);
        mDatabaseCursor.registerDataSetObserver(datasetObserver);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerContentObserver",
            args = {android.database.ContentObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterContentObserver",
            args = {android.database.ContentObserver.class}
        )
    })
    public void testRegisterContentObserver() {
        MockContentObserver contentObserver = new MockContentObserver();

        try {
            mDatabaseCursor.unregisterContentObserver(contentObserver);
            fail("Can't unregister ContentObserver before it is registered.");
        } catch (IllegalStateException e) {
            // expected
        }

        mDatabaseCursor.registerContentObserver(contentObserver);

        try {
            mDatabaseCursor.registerContentObserver(contentObserver);
            fail("Can't register DataSetObserver twice before unregister it.");
        } catch (IllegalStateException e) {
            // expected
        }

        mDatabaseCursor.unregisterContentObserver(contentObserver);
        mDatabaseCursor.registerContentObserver(contentObserver);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setNotificationUri",
        args = {android.content.ContentResolver.class, android.net.Uri.class}
    )
    public void testSetNotificationUri() {
        String MOCK_URI = "content://abstractrcursortest/testtable";
        mDatabaseCursor.setNotificationUri(getInstrumentation().getContext().getContentResolver(),
                Uri.parse(MOCK_URI));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "respond",
        args = {android.os.Bundle.class}
    )
    public void testRespond() {
        Bundle b = new Bundle();
        Bundle bundle = mDatabaseCursor.respond(b);
        assertSame(Bundle.EMPTY, bundle);

        bundle = mDatabaseCursor.respond(null);
        assertSame(Bundle.EMPTY, bundle);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requery",
        args = {}
    )
    public void testRequery() {
        MockDataSetObserver mock = new MockDataSetObserver();
        mDatabaseCursor.registerDataSetObserver(mock);
        assertFalse(mock.hadCalledOnChanged());
        mDatabaseCursor.requery();
        assertTrue(mock.hadCalledOnChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onChange().Subclasses must call this method when they finish committing" +
                " updates to notify all observers.",
        method = "onChange",
        args = {boolean.class}
    )
    public void testOnChange() throws InterruptedException {
        MockContentObserver mock = new MockContentObserver();
        mTestAbstractCursor.registerContentObserver(mock);
        assertFalse(mock.hadCalledOnChange());
        mTestAbstractCursor.onChange(true);
        synchronized(mLockObj) {
            if ( !mock.hadCalledOnChange() ) {
                mLockObj.wait(5000);
            }
        }
        assertTrue(mock.hadCalledOnChange());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMove",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToFirst",
            args = {}
        )
    })
    public void testOnMove() {
        mTestAbstractCursor.resetOnMoveRet();
        assertFalse(mTestAbstractCursor.getOnMoveRet());
        mTestAbstractCursor.moveToFirst();
        mTestAbstractCursor.moveToPosition(5);
        assertTrue(mTestAbstractCursor.getOnMoveRet());
        assertEquals(0, mTestAbstractCursor.getOldPos());
        assertEquals(5, mTestAbstractCursor.getNewPos());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToPrevious",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToPosition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToNext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBeforeFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isAfterLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveToFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "move",
            args = {int.class}
        )
    })
    public void testMoveToPrevious() {
        // Test moveToFirst, isFirst, moveToNext, getPosition
        assertTrue(mDatabaseCursor.moveToFirst());
        assertTrue(mDatabaseCursor.isFirst());
        assertEquals(0, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.moveToNext());
        assertEquals(1, mDatabaseCursor.getPosition());
        assertFalse(mDatabaseCursor.isFirst());
        assertTrue(mDatabaseCursor.moveToNext());
        assertEquals(2, mDatabaseCursor.getPosition());

        // invoke moveToPosition with a number larger than row count.
        assertFalse(mDatabaseCursor.moveToPosition(30000));
        assertEquals(mDatabaseCursor.getCount(), mDatabaseCursor.getPosition());

        assertFalse(mDatabaseCursor.moveToPosition(-1));
        assertEquals(-1, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.isBeforeFirst());

        mDatabaseCursor.moveToPosition(5);
        assertEquals(5, mDatabaseCursor.getPosition());

        // Test moveToPrevious
        assertTrue(mDatabaseCursor.moveToPrevious());
        assertEquals(4, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.moveToPrevious());
        assertEquals(3, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.moveToPrevious());
        assertEquals(2, mDatabaseCursor.getPosition());

        // Test moveToLast, isLast, moveToPrevius, isAfterLast.
        assertFalse(mDatabaseCursor.isLast());
        assertTrue(mDatabaseCursor.moveToLast());
        assertTrue(mDatabaseCursor.isLast());
        assertFalse(mDatabaseCursor.isAfterLast());

        assertFalse(mDatabaseCursor.moveToNext());
        assertTrue(mDatabaseCursor.isAfterLast());
        assertFalse(mDatabaseCursor.moveToNext());
        assertTrue(mDatabaseCursor.isAfterLast());
        assertFalse(mDatabaseCursor.isLast());
        assertTrue(mDatabaseCursor.moveToPrevious());
        assertTrue(mDatabaseCursor.isLast());
        assertTrue(mDatabaseCursor.moveToPrevious());
        assertFalse(mDatabaseCursor.isLast());

        // Test move(int).
        mDatabaseCursor.moveToFirst();
        assertEquals(0, mDatabaseCursor.getPosition());
        assertFalse(mDatabaseCursor.move(-1));
        assertEquals(-1, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.move(1));
        assertEquals(0, mDatabaseCursor.getPosition());

        assertTrue(mDatabaseCursor.move(5));
        assertEquals(5, mDatabaseCursor.getPosition());
        assertTrue(mDatabaseCursor.move(-1));
        assertEquals(4, mDatabaseCursor.getPosition());

        mDatabaseCursor.moveToLast();
        assertTrue(mDatabaseCursor.isLast());
        assertFalse(mDatabaseCursor.isAfterLast());
        assertFalse(mDatabaseCursor.move(1));
        assertFalse(mDatabaseCursor.isLast());
        assertTrue(mDatabaseCursor.isAfterLast());
        assertTrue(mDatabaseCursor.move(-1));
        assertTrue(mDatabaseCursor.isLast());
        assertFalse(mDatabaseCursor.isAfterLast());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isClosed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        )
    })
    public void testIsClosed() {
        assertFalse(mDatabaseCursor.isClosed());
        mDatabaseCursor.close();
        assertTrue(mDatabaseCursor.isClosed());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "fillWindow",
            args = {int.class, android.database.CursorWindow.class}
        )
    })
    public void testGetWindow() {
        CursorWindow window = new CursorWindow(false);
        assertEquals(0, window.getNumRows());
        // fill window from position 0
        mDatabaseCursor.fillWindow(0, window);

        assertNotNull(mDatabaseCursor.getWindow());
        assertEquals(mDatabaseCursor.getCount(), window.getNumRows());

        while (mDatabaseCursor.moveToNext()) {
            assertEquals(mDatabaseCursor.getInt(POSITION1),
                    window.getInt(mDatabaseCursor.getPosition(), POSITION1));
        }
        window.clear();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getWantsAllOnMoveCalls(), it always returns false.",
        method = "getWantsAllOnMoveCalls",
        args = {}
    )
    public void testGetWantsAllOnMoveCalls() {
        assertFalse(mDatabaseCursor.getWantsAllOnMoveCalls());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isFieldUpdated",
        args = {int.class}
    )
    @ToBeFixed(bug = "1569265", explanation = "All other updating-related methods are 'hide' and "
        + "'deprecated.")
    public void testIsFieldUpdated() {
        mTestAbstractCursor.moveToFirst();
        assertFalse(mTestAbstractCursor.isFieldUpdated(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getUpdatedField",
        args = {int.class}
    )
    @ToBeFixed(bug = "1569265", explanation = "All other updating-related methods are 'hide' and "
        + "'deprecated.")
    public void testGetUpdatedField() {
        mTestAbstractCursor.moveToFirst();
        try {
            assertEquals("hello", mTestAbstractCursor.getUpdatedField(0));
            fail("getUpdatedField should throws a NullPointerException here.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getExtras",
        args = {}
    )
    public void testGetExtras() {
        assertSame(Bundle.EMPTY, mDatabaseCursor.getExtras());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCount",
        args = {}
    )
    public void testGetCount() {
        assertEquals(DATA_COUNT, mDatabaseCursor.getCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getColumnNames",
        args = {}
    )
    public void testGetColumnNames() {
        String[] names = mDatabaseCursor.getColumnNames();
        assertEquals(COLUMN_NAMES1.length, names.length);

        for (int i = 0; i < COLUMN_NAMES1.length; i++) {
            assertEquals(COLUMN_NAMES1[i], names[i]);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getColumnName",
        args = {int.class}
    )
    public void testGetColumnName() {
        assertEquals(COLUMN_NAMES1[0], mDatabaseCursor.getColumnName(0));
        assertEquals(COLUMN_NAMES1[1], mDatabaseCursor.getColumnName(1));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnIndexOrThrow",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnIndex",
            args = {java.lang.String.class}
        )
    })
    public void testGetColumnIndexOrThrow() {
        final String COLUMN_FAKE = "fake_name";
        assertEquals(POSITION0, mDatabaseCursor.getColumnIndex(COLUMN_NAMES1[POSITION0]));
        assertEquals(POSITION1, mDatabaseCursor.getColumnIndex(COLUMN_NAMES1[POSITION1]));
        assertEquals(POSITION0, mDatabaseCursor.getColumnIndexOrThrow(COLUMN_NAMES1[POSITION0]));
        assertEquals(POSITION1, mDatabaseCursor.getColumnIndexOrThrow(COLUMN_NAMES1[POSITION1]));

        try {
            mDatabaseCursor.getColumnIndexOrThrow(COLUMN_FAKE);
            fail("IllegalArgumentException expected, but not thrown");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getColumnIndex.",
        args = {java.lang.String.class}
    )
    public void testGetColumnIndex() {
        assertEquals(POSITION0, mDatabaseCursor.getColumnIndex(COLUMN_NAMES1[POSITION0]));
        assertEquals(POSITION1, mDatabaseCursor.getColumnIndex(COLUMN_NAMES1[POSITION1]));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getColumnCount",
        args = {}
    )
    public void testGetColumnCount() {
        assertEquals(COLUMN_NAMES1.length, mDatabaseCursor.getColumnCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "deactivateInternal",
        args = {}
    )
    public void testDeactivateInternal() {
        MockDataSetObserver mock = new MockDataSetObserver();
        mDatabaseCursor.registerDataSetObserver(mock);
        assertFalse(mock.hadCalledOnInvalid());
        mDatabaseCursor.deactivateInternal();
        assertTrue(mock.hadCalledOnInvalid());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "deactivate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerDataSetObserver",
            args = {android.database.DataSetObserver.class}
        )
    })
    public void testDeactivate() {
        MockDataSetObserver mock = new MockDataSetObserver();
        mDatabaseCursor.registerDataSetObserver(mock);
        assertFalse(mock.hadCalledOnInvalid());
        mDatabaseCursor.deactivate();
        assertTrue(mock.hadCalledOnInvalid());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "copyStringToBuffer",
        args = {int.class, android.database.CharArrayBuffer.class}
    )
    public void testCopyStringToBuffer() {
        CharArrayBuffer ca = new CharArrayBuffer(1000);
        mTestAbstractCursor.moveToFirst();
        mTestAbstractCursor.copyStringToBuffer(0, ca);
        CursorWindow window = new CursorWindow(false);
        mTestAbstractCursor.fillWindow(0, window);

        StringBuffer sb = new StringBuffer();
        sb.append(window.getString(0, 0));
        String str = mTestAbstractCursor.getString(0);
        assertEquals(str.length(), ca.sizeCopied);
        assertEquals(sb.toString(), new String(ca.data, 0, ca.sizeCopied));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkPosition",
        args = {}
    )
    public void testCheckPosition() {
        // Test with position = -1.
        try {
            mTestAbstractCursor.checkPosition();
            fail("copyStringToBuffer() should throws CursorIndexOutOfBoundsException here.");
        } catch (CursorIndexOutOfBoundsException e) {
            // expected
        }

        // Test with position = count.
        assertTrue(mTestAbstractCursor.moveToPosition(mTestAbstractCursor.getCount() - 1));
        mTestAbstractCursor.checkPosition();

        try {
            assertFalse(mTestAbstractCursor.moveToPosition(mTestAbstractCursor.getCount()));
            assertEquals(mTestAbstractCursor.getCount(), mTestAbstractCursor.getPosition());
            mTestAbstractCursor.checkPosition();
            fail("copyStringToBuffer() should throws CursorIndexOutOfBoundsException here.");
        } catch (CursorIndexOutOfBoundsException e) {
            // expected
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<ArrayList> createTestList(int rows, int cols) {
        ArrayList<ArrayList> list = new ArrayList<ArrayList>();
        Random ran = new Random();

        for (int i = 0; i < rows; i++) {
            ArrayList<Integer> col = new ArrayList<Integer>();
            list.add(col);

            for (int j = 0; j < cols; j++) {
                // generate random number
                Integer r = ran.nextInt();
                col.add(r);
            }
        }

        return list;
    }

    private void setupDatabase() {
        File dbDir = getInstrumentation().getTargetContext().getDir("tests",
                Context.MODE_WORLD_WRITEABLE);
        mDatabaseFile = new File(dbDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabaseFile);
        mDatabase.execSQL("CREATE TABLE test1 (_id INTEGER PRIMARY KEY, number TEXT);");
        generateData();
        mDatabaseCursor = (AbstractCursor) mDatabase.query("test1", null, null, null, null, null,
                null);
    }

    private void generateData() {
        for ( int i = 0; i < DATA_COUNT; i++) {
            mDatabase.execSQL("INSERT INTO test1 (number) VALUES ('" + i + "');");
        }
    }

    private class TestAbstractCursor extends AbstractCursor {
        private boolean mOnMoveReturnValue;
        private int mOldPosition;
        private int mNewPosition;
        private String[] mColumnNames;
        private ArrayList<Object>[] mRows;
        private boolean mHadCalledOnChange = false;

        public TestAbstractCursor() {
            super();
        }
        @SuppressWarnings("unchecked")
        public TestAbstractCursor(String[] columnNames, ArrayList<ArrayList> rows) {
            int colCount = columnNames.length;
            boolean foundID = false;
            mRowIdColumnIndex = 0;

            // Add an _id column if not in columnNames
            for (int i = 0; i < colCount; ++i) {
                if (columnNames[i].compareToIgnoreCase("_id") == 0) {
                    mColumnNames = columnNames;
                    foundID = true;
                    break;
                }
            }

            if (!foundID) {
                mColumnNames = new String[colCount + 1];
                System.arraycopy(columnNames, 0, mColumnNames, 0, columnNames.length);
                mColumnNames[colCount] = "_id";
            }

            int rowCount = rows.size();
            mRows = new ArrayList[rowCount];

            for (int i = 0; i < rowCount; ++i) {
                mRows[i] = rows.get(i);

                if (!foundID) {
                    mRows[i].add(Long.valueOf(i));
                }
            }
        }

        public boolean getOnMoveRet() {
            return mOnMoveReturnValue;
        }

        public void resetOnMoveRet() {
            mOnMoveReturnValue = false;
        }

        public int getOldPos() {
            return mOldPosition;
        }

        public int getNewPos() {
            return mNewPosition;
        }

        @Override
        public boolean onMove(int oldPosition, int newPosition) {
            mOnMoveReturnValue = super.onMove(oldPosition, newPosition);
            mOldPosition = oldPosition;
            mNewPosition = newPosition;
            return mOnMoveReturnValue;
        }

        @Override
        public int getCount() {
            return mRows.length;
        }

        @Deprecated
        @Override
        public boolean deleteRow() {
            return false;
        }

        @Override
        public String[] getColumnNames() {
            return mColumnNames;
        }

        @Override
        public String getString(int columnIndex) {
            Object cell = mRows[mPos].get(columnIndex);
            return (cell == null) ? null : cell.toString();
        }

        @Override
        public short getShort(int columnIndex) {
            Number num = (Number) mRows[mPos].get(columnIndex);
            return num.shortValue();
        }

        @Override
        public int getInt(int columnIndex) {
            Number num = (Number) mRows[mPos].get(columnIndex);
            return num.intValue();
        }

        @Override
        public long getLong(int columnIndex) {
            Number num = (Number) mRows[mPos].get(columnIndex);
            return num.longValue();
        }

        @Override
        public float getFloat(int columnIndex) {
            Number num = (Number) mRows[mPos].get(columnIndex);
            return num.floatValue();
        }

        @Override
        public double getDouble(int columnIndex) {
            Number num = (Number) mRows[mPos].get(columnIndex);
            return num.doubleValue();
        }

        @Override
        public boolean isNull(int column) {
            return false;
        }

        public boolean hadCalledOnChange() {
            return mHadCalledOnChange;
        }

        // the following are protected methods
        @Override
        protected void checkPosition() {
            super.checkPosition();
        }

        @Override
        protected Object getUpdatedField(int columnIndex) {
            return super.getUpdatedField(columnIndex);
        }

        @Override
        protected boolean isFieldUpdated(int columnIndex) {
            return super.isFieldUpdated(columnIndex);
        }

        @Override
        protected void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHadCalledOnChange = true;
        }
    }

    private class MockContentObserver extends ContentObserver {
        public boolean mHadCalledOnChange;

        public MockContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHadCalledOnChange = true;
            synchronized(mLockObj) {
                mLockObj.notify();
            }
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        public boolean hadCalledOnChange() {
            return mHadCalledOnChange;
        }
    }

    private class MockDataSetObserver extends DataSetObserver {
        private boolean mHadCalledOnChanged;
        private boolean mHadCalledOnInvalid;

        @Override
        public void onChanged() {
            super.onChanged();
            mHadCalledOnChanged = true;
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mHadCalledOnInvalid = true;
        }

        public boolean hadCalledOnChanged() {
            return mHadCalledOnChanged;
        }

        public boolean hadCalledOnInvalid() {
            return mHadCalledOnInvalid;
        }
    }
}
