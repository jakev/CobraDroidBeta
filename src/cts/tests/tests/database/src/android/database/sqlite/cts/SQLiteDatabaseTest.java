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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(android.database.sqlite.SQLiteDatabase.class)
public class SQLiteDatabaseTest extends AndroidTestCase {
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    private String mDatabaseFilePath;
    private String mDatabaseDir;

    private boolean mTransactionListenerOnBeginCalled;
    private boolean mTransactionListenerOnCommitCalled;
    private boolean mTransactionListenerOnRollbackCalled;

    private static final String DATABASE_FILE_NAME = "database_test.db";
    private static final String TABLE_NAME = "test";
    private static final int COLUMN_ID_INDEX = 0;
    private static final int COLUMN_NAME_INDEX = 1;
    private static final int COLUMN_AGE_INDEX = 2;
    private static final int COLUMN_ADDR_INDEX = 3;
    private static final String[] TEST_PROJECTION = new String[] {
            "_id",      // 0
            "name",     // 1
            "age",      // 2
            "address"   // 3
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase(DATABASE_FILE_NAME);
        mDatabaseFilePath = getContext().getDatabasePath(DATABASE_FILE_NAME).getPath();
        mDatabaseFile = getContext().getDatabasePath(DATABASE_FILE_NAME);
        mDatabaseDir = mDatabaseFile.getParent();
        mDatabaseFile.getParentFile().mkdirs(); // directory may not exist
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile, null);
        assertNotNull(mDatabase);

        mTransactionListenerOnBeginCalled = false;
        mTransactionListenerOnCommitCalled = false;
        mTransactionListenerOnRollbackCalled = false;
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test openDatabase",
            method = "openDatabase",
            args = {java.lang.String.class,
                    android.database.sqlite.SQLiteDatabase.CursorFactory.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test openOrCreateDatabase",
            method = "openOrCreateDatabase",
            args = {java.io.File.class,
                    android.database.sqlite.SQLiteDatabase.CursorFactory.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test openOrCreateDatabase",
            method = "openOrCreateDatabase",
            args = {java.lang.String.class,
                    android.database.sqlite.SQLiteDatabase.CursorFactory.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test close",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test create",
            method = "create",
            args = {android.database.sqlite.SQLiteDatabase.CursorFactory.class}
        )
    })
    public void testOpenDatabase() {
        CursorFactory factory = new CursorFactory() {
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                    String editTable, SQLiteQuery query) {
                return new MockSQLiteCursor(db, masterQuery, editTable, query);
            }
        };

        SQLiteDatabase db = SQLiteDatabase.openDatabase(mDatabaseFilePath,
                factory, SQLiteDatabase.CREATE_IF_NECESSARY);
        assertNotNull(db);
        db.close();

        try {
            // do not allow to create file in the directory
            SQLiteDatabase.openDatabase("/system/database.db", factory,
                    SQLiteDatabase.CREATE_IF_NECESSARY);
            fail("didn't throw SQLiteException when do not allow to create database file");
        } catch (SQLiteException e) {
        } finally {
            db.close();
        }

        File dbFile = new File(mDatabaseDir, "database_test12345678.db");
        assertFalse(dbFile.exists());
        db = SQLiteDatabase.openOrCreateDatabase(dbFile.getPath(), factory);
        assertNotNull(db);
        db.close();
        dbFile.delete();

        dbFile = new File(mDatabaseDir, DATABASE_FILE_NAME);
        db = SQLiteDatabase.openOrCreateDatabase(dbFile, factory);
        assertNotNull(db);
        db.close();
        dbFile.delete();

        db = SQLiteDatabase.create(factory);
        assertNotNull(db);
        db.close();
    }

    private class MockSQLiteCursor extends SQLiteCursor {
        public MockSQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
                String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test beginTransaction()",
            method = "beginTransaction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test endTransaction()",
            method = "endTransaction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setTransactionSuccessful()",
            method = "setTransactionSuccessful",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test inTransaction()",
            method = "inTransaction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test isDbLockedByCurrentThread()",
            method = "isDbLockedByCurrentThread",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test isDbLockedByOtherThreads()",
            method = "isDbLockedByOtherThreads",
            args = {}
        )
    })
    public void testTransaction() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER);");
        mDatabase.execSQL("INSERT INTO test (num) VALUES (0)");

        // test execSQL without any explicit transactions.
        setNum(1);
        assertNum(1);

        // Test a single-level transaction.
        setNum(0);
        assertFalse(mDatabase.inTransaction());
        mDatabase.beginTransaction();
        assertTrue(mDatabase.inTransaction());
        setNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertFalse(mDatabase.inTransaction());
        assertNum(1);
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());

        // Test a rolled-back transaction.
        setNum(0);
        assertFalse(mDatabase.inTransaction());
        mDatabase.beginTransaction();
        setNum(1);
        assertTrue(mDatabase.inTransaction());
        mDatabase.endTransaction();
        assertFalse(mDatabase.inTransaction());
        assertNum(0);
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());

        // it should throw IllegalStateException if we end a non-existent transaction.
        assertThrowsIllegalState(new Runnable() {
            public void run() {
                mDatabase.endTransaction();
            }
        });

        // it should throw IllegalStateException if a set a non-existent transaction as clean.
        assertThrowsIllegalState(new Runnable() {
            public void run() {
                mDatabase.setTransactionSuccessful();
            }
        });

        mDatabase.beginTransaction();
        mDatabase.setTransactionSuccessful();
        // it should throw IllegalStateException if we mark a transaction as clean twice.
        assertThrowsIllegalState(new Runnable() {
            public void run() {
                mDatabase.setTransactionSuccessful();
            }
        });
        // it should throw IllegalStateException if we begin a transaction after marking the
        // parent as clean.
        assertThrowsIllegalState(new Runnable() {
            public void run() {
                mDatabase.beginTransaction();
            }
        });
        mDatabase.endTransaction();
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());

        assertFalse(mDatabase.inTransaction());
        // Test a two-level transaction.
        setNum(0);
        mDatabase.beginTransaction();
        assertTrue(mDatabase.inTransaction());
        mDatabase.beginTransaction();
        assertTrue(mDatabase.inTransaction());
        setNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertTrue(mDatabase.inTransaction());
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertFalse(mDatabase.inTransaction());
        assertNum(1);
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());

        // Test rolling back an inner transaction.
        setNum(0);
        mDatabase.beginTransaction();
        mDatabase.beginTransaction();
        setNum(1);
        mDatabase.endTransaction();
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertNum(0);
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());

        // Test rolling back an outer transaction.
        setNum(0);
        mDatabase.beginTransaction();
        mDatabase.beginTransaction();
        setNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        mDatabase.endTransaction();
        assertNum(0);
        assertFalse(mDatabase.isDbLockedByCurrentThread());
        assertFalse(mDatabase.isDbLockedByOtherThreads());
    }

    private void setNum(int num) {
        mDatabase.execSQL("UPDATE test SET num = " + num);
    }

    private void assertNum(int num) {
        assertEquals(num, DatabaseUtils.longForQuery(mDatabase,
                "SELECT num FROM test", null));
    }

    private void assertThrowsIllegalState(Runnable r) {
        try {
            r.run();
            fail("did not throw expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @SuppressWarnings("deprecation")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getSyncedTables()",
            method = "getSyncedTables",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test markTableSyncable(String, String)",
            method = "markTableSyncable",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testGetSyncedTables() {
        mDatabase.execSQL("CREATE TABLE people (_id INTEGER PRIMARY KEY, name TEXT, "
                + "_sync_dirty INTEGER);");
        mDatabase.execSQL("CREATE TABLE _delete_people (name TEXT);");
        Map<String, String> tableMap = mDatabase.getSyncedTables();
        assertEquals(0, tableMap.size());
        mDatabase.markTableSyncable("people", "_delete_people");
        tableMap = mDatabase.getSyncedTables();
        assertEquals(1, tableMap.size());
        Set<String> keys = tableMap.keySet();
        Iterator<String> iterator = keys.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("people", iterator.next());
        assertEquals("_delete_people", tableMap.get("people"));
        assertFalse(iterator.hasNext());

        // test sync
        mDatabase.execSQL("INSERT INTO people VALUES (0, \"foo\", 0);");
        Cursor c = mDatabase.query("people", new String[] {"_id", "name" },
                "_id = 0", null, null, null, null);
        assertTrue(c.moveToFirst());
        c.updateString(1, "updated");
        c.commitUpdates();
        c.close();
        c = mDatabase.query("people", new String[] {"_id", "_sync_dirty" },
                "_id = 0", null, null, null, null);
        assertTrue(c.moveToFirst());
        // _sync_dirty flag has been set
        assertEquals(1, c.getInt(1));
        c.close();
    }

    @SuppressWarnings("deprecation")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test markTableSyncable(String, String, String)",
        method = "markTableSyncable",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testMarkTableSyncable() {
        mDatabase.execSQL("CREATE TABLE phone (_id INTEGER PRIMARY KEY, _people_id INTEGER, " +
                "name TEXT);");
        mDatabase.execSQL("CREATE TABLE people (_id INTEGER PRIMARY KEY, " +
                "name TEXT, _sync_dirty INTEGER);");
        mDatabase.markTableSyncable("phone", "_people_id", "people");

        Map<String, String> tableMap = mDatabase.getSyncedTables();
        // since no delete table was given, there is no mapping
        assertEquals(0, tableMap.size());

        // test sync
        mDatabase.execSQL("INSERT INTO people VALUES (13, \"foo\", 0);");
        mDatabase.execSQL("INSERT INTO phone VALUES (0, 13, \"bar\");");
        Cursor c = mDatabase.query("phone", new String[] {"_id", "name" },
                "_id = 0", null, null, null, null);
        assertTrue(c.moveToFirst());
        c.updateString(1, "updated");
        c.commitUpdates();
        c.close();
        c = mDatabase.query("people", new String[] {"_id", "_sync_dirty" },
                "_id = 13", null, null, null, null);
        assertTrue(c.moveToFirst());
        // _sync_dirty flag has been set
        assertEquals(1, c.getInt(1));
        c.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getMaximumSize()",
            method = "getMaximumSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setMaximumSize(long)",
            method = "setMaximumSize",
            args = {long.class}
        )
    })
    public void testAccessMaximumSize() {
        long curMaximumSize = mDatabase.getMaximumSize();

        // the new maximum size is less than the current size.
        mDatabase.setMaximumSize(curMaximumSize - 1);
        assertEquals(curMaximumSize, mDatabase.getMaximumSize());

        // the new maximum size is more than the current size.
        mDatabase.setMaximumSize(curMaximumSize + 1);
        assertEquals(curMaximumSize + mDatabase.getPageSize(), mDatabase.getMaximumSize());
        assertTrue(mDatabase.getMaximumSize() > curMaximumSize);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPageSize()",
            method = "getPageSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setPageSize(long)",
            method = "setPageSize",
            args = {long.class}
        )
    })
    @ToBeFixed(bug = "1676383", explanation = "setPageSize does not work as javadoc declares.")
    public void testAccessPageSize() {
        File databaseFile = new File(mDatabaseDir, "database.db");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openOrCreateDatabase(databaseFile.getPath(), null);

            long initialValue = database.getPageSize();
            // check that this does not throw an exception
            // setting a different page size may not be supported after the DB has been created
            database.setPageSize(initialValue);
            assertEquals(initialValue, database.getPageSize());

        } finally {
            if (database != null) {
                database.close();
                databaseFile.delete();
            }
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test compileStatement(String)",
        method = "compileStatement",
        args = {java.lang.String.class}
    )
    public void testCompileStatement() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, "
                + "name TEXT, age INTEGER, address TEXT);");

        String name = "Mike";
        int age = 21;
        String address = "LA";

        // at the beginning, there is no record in the database.
        Cursor cursor = mDatabase.query("test", TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());

        String sql = "INSERT INTO test (name, age, address) VALUES (?, ?, ?);";
        SQLiteStatement insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, name);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, age);
        DatabaseUtils.bindObjectToProgram(insertStatement, 3, address);
        insertStatement.execute();
        insertStatement.close();
        cursor.close();

        cursor = mDatabase.query("test", TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(name, cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(age, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals(address, cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        SQLiteStatement deleteStatement = mDatabase.compileStatement("DELETE FROM test");
        deleteStatement.execute();

        cursor = mDatabase.query("test", null, null, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.deactivate();
        deleteStatement.close();
        cursor.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test delete(String, String, String[])",
        method = "delete",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String[].class}
    )
    public void testDelete() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, "
                + "name TEXT, age INTEGER, address TEXT);");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Mike', 20, 'LA');");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Jack', 30, 'London');");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Jim', 35, 'Chicago');");

        // delete one record.
        mDatabase.delete(TABLE_NAME, "name = 'Mike'", null);

        Cursor cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null,
                null, null, null, null);
        assertNotNull(cursor);
        // there are 2 records here.
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(30, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.moveToNext();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(35, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("Chicago", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        // delete another record.
        mDatabase.delete(TABLE_NAME, "name = ?", new String[] { "Jack" });

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null,
                null, null);
        assertNotNull(cursor);
        // there are 1 records here.
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(35, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("Chicago", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Mike', 20, 'LA');");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Jack', 30, 'London');");

        // delete all records.
        mDatabase.delete(TABLE_NAME, null, null);

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test execSQL(String)",
            method = "execSQL",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test execSQL(String, Object[])",
            method = "execSQL",
            args = {java.lang.String.class, java.lang.Object[].class}
        )
    })
    public void testExecSQL() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, "
                + "name TEXT, age INTEGER, address TEXT);");

        // add a new record.
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Mike', 20, 'LA');");

        Cursor cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null,
                null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        // add other new record.
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Jack', 30, 'London');");

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.moveToNext();
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(30, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        // delete a record.
        mDatabase.execSQL("DELETE FROM test WHERE name = ?;", new String[] { "Jack" });

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        // delete a non-exist record.
        mDatabase.execSQL("DELETE FROM test WHERE name = ?;", new String[] { "Wrong Name" });

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        try {
            // execSQL can not use for query.
            mDatabase.execSQL("SELECT * FROM test;");
            fail("should throw SQLException.");
        } catch (SQLException e) {
        }

        // make sure execSQL can't be used to execute more than 1 sql statement at a time
        mDatabase.execSQL("UPDATE test SET age = 40 WHERE name = 'Mike';" + 
                "UPDATE test SET age = 50 WHERE name = 'Mike';");
        // age should be updated to 40 not to 50
        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(40, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        // make sure sql injection is NOT allowed or has no effect when using query()
        String harmfulQuery = "name = 'Mike';UPDATE test SET age = 50 WHERE name = 'Mike'";
        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, harmfulQuery, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        // row's age column SHOULD NOT be 50
        assertEquals(40, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test findEditTable(String)",
        method = "findEditTable",
        args = {java.lang.String.class}
    )
    public void testFindEditTable() {
        String tables = "table1 table2 table3";
        assertEquals("table1", SQLiteDatabase.findEditTable(tables));

        tables = "table1,table2,table3";
        assertEquals("table1", SQLiteDatabase.findEditTable(tables));

        tables = "table1";
        assertEquals("table1", SQLiteDatabase.findEditTable(tables));

        try {
            SQLiteDatabase.findEditTable("");
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getPath()",
        method = "getPath",
        args = {}
    )
    public void testGetPath() {
        assertEquals(mDatabaseFilePath, mDatabase.getPath());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getVersion()",
            method = "getVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setVersion(int)",
            method = "setVersion",
            args = {int.class}
        )
    })
    public void testAccessVersion() {
        mDatabase.setVersion(1);
        assertEquals(1, mDatabase.getVersion());

        mDatabase.setVersion(3);
        assertEquals(3, mDatabase.getVersion());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test insert(String, String, ContentValues)",
            method = "insert",
            args = {java.lang.String.class, java.lang.String.class,
                    android.content.ContentValues.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test insertOrThrow(String, String, ContentValues)",
            method = "insertOrThrow",
            args = {java.lang.String.class, java.lang.String.class,
                    android.content.ContentValues.class}
        )
    })
    public void testInsert() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, "
                + "name TEXT, age INTEGER, address TEXT);");

        ContentValues values = new ContentValues();
        values.put("name", "Jack");
        values.put("age", 20);
        values.put("address", "LA");
        mDatabase.insert(TABLE_NAME, "name", values);

        Cursor cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null,
                null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        mDatabase.insert(TABLE_NAME, "name", null);
        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null,
                null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.moveToNext();
        assertNull(cursor.getString(COLUMN_NAME_INDEX));
        cursor.close();

        values = new ContentValues();
        values.put("Wrong Key", "Wrong value");
        mDatabase.insert(TABLE_NAME, "name", values);
        // there are still 2 records.
        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null,
                null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.close();

        // delete all record.
        mDatabase.execSQL("DELETE FROM test;");

        values = new ContentValues();
        values.put("name", "Mike");
        values.put("age", 30);
        values.put("address", "London");
        mDatabase.insertOrThrow(TABLE_NAME, "name", values);

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null,
                null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(30, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        mDatabase.insertOrThrow(TABLE_NAME, "name", null);
        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null,
                null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(30, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.moveToNext();
        assertNull(cursor.getString(COLUMN_NAME_INDEX));
        cursor.close();

        values = new ContentValues();
        values.put("Wrong Key", "Wrong value");
        try {
            mDatabase.insertOrThrow(TABLE_NAME, "name", values);
            fail("should throw SQLException.");
        } catch (SQLException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isOpen()",
        method = "isOpen",
        args = {}
    )
    public void testIsOpen() {
        assertTrue(mDatabase.isOpen());

        mDatabase.close();
        assertFalse(mDatabase.isOpen());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isReadOnly()",
        method = "isReadOnly",
        args = {}
    )
    public void testIsReadOnly() {
        assertFalse(mDatabase.isReadOnly());

        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(mDatabaseFilePath, null,
                    SQLiteDatabase.OPEN_READONLY);
            assertTrue(database.isReadOnly());
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test releaseMemory()",
        method = "releaseMemory",
        args = {}
    )
    public void testReleaseMemory() {
        SQLiteDatabase.releaseMemory();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setLockingEnabled()",
        method = "setLockingEnabled",
        args = {boolean.class}
    )
    public void testSetLockingEnabled() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER);");
        mDatabase.execSQL("INSERT INTO test (num) VALUES (0)");

        mDatabase.setLockingEnabled(false);

        mDatabase.beginTransaction();
        setNum(1);
        assertNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test yieldIfContended()",
            method = "yieldIfContended",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test yieldIfContendedSafely()",
            method = "yieldIfContendedSafely",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testYieldIfContended() {
        assertFalse(mDatabase.yieldIfContended());

        mDatabase.execSQL("CREATE TABLE test (num INTEGER);");
        mDatabase.execSQL("INSERT INTO test (num) VALUES (0)");

        // Make sure that things work outside an explicit transaction.
        setNum(1);
        assertNum(1);

        setNum(0);
        assertFalse(mDatabase.inTransaction());
        mDatabase.beginTransaction();
        assertTrue(mDatabase.inTransaction());
        assertFalse(mDatabase.yieldIfContended());
        setNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        mDatabase.beginTransaction();
        assertTrue(mDatabase.inTransaction());
        assertFalse(mDatabase.yieldIfContendedSafely());
        setNum(1);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test query",
            method = "query",
            args = {boolean.class, java.lang.String.class, java.lang.String[].class,
                    java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryWithFactory",
            method = "queryWithFactory",
            args = {android.database.sqlite.SQLiteDatabase.CursorFactory.class, boolean.class,
                    java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String[].class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test query",
            method = "query",
            args = {java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String[].class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test query",
            method = "query",
            args = {java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String[].class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test rawQuery",
            method = "rawQuery",
            args = {java.lang.String.class, java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test rawQueryWithFactory",
            method = "rawQueryWithFactory",
            args = {android.database.sqlite.SQLiteDatabase.CursorFactory.class,
                    java.lang.String.class, java.lang.String[].class, java.lang.String.class}
        )
    })
    public void testQuery() {
        mDatabase.execSQL("CREATE TABLE employee (_id INTEGER PRIMARY KEY, " +
                "name TEXT, month INTEGER, salary INTEGER);");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('Mike', '1', '1000');");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('Mike', '2', '3000');");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('jack', '1', '2000');");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('jack', '3', '1500');");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('Jim', '1', '1000');");
        mDatabase.execSQL("INSERT INTO employee (name, month, salary) " +
                "VALUES ('Jim', '3', '3500');");

        Cursor cursor = mDatabase.query(true, "employee", new String[] { "name", "sum(salary)" },
                null, null, "name", "sum(salary)>1000", "name", null);
        assertNotNull(cursor);
        assertEquals(3, cursor.getCount());

        final int COLUMN_NAME_INDEX = 0;
        final int COLUMN_SALARY_INDEX = 1;
        cursor.moveToFirst();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4000, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(3500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.close();

        CursorFactory factory = new CursorFactory() {
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                    String editTable, SQLiteQuery query) {
                return new MockSQLiteCursor(db, masterQuery, editTable, query);
            }
        };
        cursor = mDatabase.queryWithFactory(factory, true, "employee",
                new String[] { "name", "sum(salary)" },
                null, null, "name", "sum(salary) > 1000", "name", null);
        assertNotNull(cursor);
        assertTrue(cursor instanceof MockSQLiteCursor);
        cursor.moveToFirst();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4000, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(3500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.close();

        cursor = mDatabase.query("employee", new String[] { "name", "sum(salary)" },
                null, null, "name", "sum(salary) <= 4000", "name");
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4000, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(3500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.close();

        cursor = mDatabase.query("employee", new String[] { "name", "sum(salary)" },
                null, null, "name", "sum(salary) > 1000", "name", "2");
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        cursor.moveToFirst();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4000, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.close();

        String sql = "SELECT name, month FROM employee WHERE salary > ?;";
        cursor = mDatabase.rawQuery(sql, new String[] { "2000" });
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        final int COLUMN_MONTH_INDEX = 1;
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(2, cursor.getInt(COLUMN_MONTH_INDEX));
        cursor.moveToNext();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(3, cursor.getInt(COLUMN_MONTH_INDEX));
        cursor.close();

        cursor = mDatabase.rawQueryWithFactory(factory, sql, new String[] { "2000" }, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        assertTrue(cursor instanceof MockSQLiteCursor);
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(2, cursor.getInt(COLUMN_MONTH_INDEX));
        cursor.moveToNext();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(3, cursor.getInt(COLUMN_MONTH_INDEX));
        cursor.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test replace()",
            method = "replace",
            args = {java.lang.String.class, java.lang.String.class,
                    android.content.ContentValues.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test replaceOrThrow()",
            method = "replaceOrThrow",
            args = {java.lang.String.class, java.lang.String.class,
                    android.content.ContentValues.class}
        )
    })
    public void testReplace() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, "
                + "name TEXT, age INTEGER, address TEXT);");

        ContentValues values = new ContentValues();
        values.put("name", "Jack");
        values.put("age", 20);
        values.put("address", "LA");
        mDatabase.replace(TABLE_NAME, "name", values);

        Cursor cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION,
                null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        int id = cursor.getInt(COLUMN_ID_INDEX);
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        values = new ContentValues();
        values.put("_id", id);
        values.put("name", "Mike");
        values.put("age", 40);
        values.put("address", "London");
        mDatabase.replace(TABLE_NAME, "name", values);

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount()); // there is still ONLY 1 record.
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(40, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        values = new ContentValues();
        values.put("name", "Jack");
        values.put("age", 20);
        values.put("address", "LA");
        mDatabase.replaceOrThrow(TABLE_NAME, "name", values);

        cursor = mDatabase.query(TABLE_NAME, TEST_PROJECTION, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(40, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("London", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.moveToNext();
        assertEquals("Jack", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(20, cursor.getInt(COLUMN_AGE_INDEX));
        assertEquals("LA", cursor.getString(COLUMN_ADDR_INDEX));
        cursor.close();

        values = new ContentValues();
        values.put("Wrong Key", "Wrong value");
        try {
            mDatabase.replaceOrThrow(TABLE_NAME, "name", values);
            fail("should throw SQLException.");
        } catch (SQLException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test update()",
        method = "update",
        args = {java.lang.String.class, android.content.ContentValues.class,
                java.lang.String.class, java.lang.String[].class}
    )
    public void testUpdate() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data TEXT);");

        mDatabase.execSQL("INSERT INTO test (data) VALUES ('string1');");
        mDatabase.execSQL("INSERT INTO test (data) VALUES ('string2');");
        mDatabase.execSQL("INSERT INTO test (data) VALUES ('string3');");

        String updatedString = "this is an updated test";
        ContentValues values = new ContentValues(1);
        values.put("data", updatedString);
        assertEquals(1, mDatabase.update("test", values, "_id=1", null));
        Cursor cursor = mDatabase.query("test", null, "_id=1", null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        String value = cursor.getString(cursor.getColumnIndexOrThrow("data"));
        assertEquals(updatedString, value);
        cursor.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test needUpgrade(int)",
        method = "needUpgrade",
        args = {int.class}
    )
    public void testNeedUpgrade() {
        mDatabase.setVersion(0);
        assertTrue(mDatabase.needUpgrade(1));
        mDatabase.setVersion(1);
        assertFalse(mDatabase.needUpgrade(1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setLocale(Locale)",
        method = "setLocale",
        args = {java.util.Locale.class}
    )
    public void testSetLocale() {
        final String[] STRINGS = {
                "c\u00f4t\u00e9",
                "cote",
                "c\u00f4te",
                "cot\u00e9",
                "boy",
                "dog",
                "COTE",
        };

        mDatabase.execSQL("CREATE TABLE test (data TEXT COLLATE LOCALIZED);");
        for (String s : STRINGS) {
            mDatabase.execSQL("INSERT INTO test VALUES('" + s + "');");
        }

        mDatabase.setLocale(new Locale("en", "US"));

        String sql = "SELECT data FROM test ORDER BY data COLLATE LOCALIZED ASC";
        Cursor cursor = mDatabase.rawQuery(sql, null);
        assertNotNull(cursor);
        ArrayList<String> items = new ArrayList<String>();
        while (cursor.moveToNext()) {
            items.add(cursor.getString(0));
        }
        String[] results = items.toArray(new String[items.size()]);
        assertEquals(STRINGS.length, results.length);
        cursor.close();

        // The database code currently uses PRIMARY collation strength,
        // meaning that all versions of a character compare equal (regardless
        // of case or accents), leaving the "cote" flavors in database order.
        MoreAsserts.assertEquals(results, new String[] {
                STRINGS[4],  // "boy"
                STRINGS[0],  // sundry forms of "cote"
                STRINGS[1],
                STRINGS[2],
                STRINGS[3],
                STRINGS[6],  // "COTE"
                STRINGS[5],  // "dog"
        });
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onAllReferencesReleased()",
        method = "onAllReferencesReleased",
        args = {}
    )
    public void testOnAllReferencesReleased() {
        assertTrue(mDatabase.isOpen());
        mDatabase.releaseReference();
        assertFalse(mDatabase.isOpen());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test transaction with SQLTransactionListener()",
        method = "beginTransactionWithListener",
        args = {SQLiteTransactionListener.class}
    )
    public void testTransactionWithSQLiteTransactionListener() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER);");
        mDatabase.execSQL("INSERT INTO test (num) VALUES (0)");

        assertEquals(mTransactionListenerOnBeginCalled, false);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);
        mDatabase.beginTransactionWithListener(new TestSQLiteTransactionListener());

        // Assert that the transcation has started
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);

        setNum(1);

        // State shouldn't have changed
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);

        // commit the transaction
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        // the listener should have been told that commit was called
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, true);
        assertEquals(mTransactionListenerOnRollbackCalled, false);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test transaction w/rollback with SQLTransactionListener()",
        method = "beginTransactionWithListener",
        args = {SQLiteTransactionListener.class}
    )
    public void testRollbackTransactionWithSQLiteTransactionListener() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER);");
        mDatabase.execSQL("INSERT INTO test (num) VALUES (0)");

        assertEquals(mTransactionListenerOnBeginCalled, false);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);
        mDatabase.beginTransactionWithListener(new TestSQLiteTransactionListener());

        // Assert that the transcation has started
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);

        setNum(1);

        // State shouldn't have changed
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, false);

        // commit the transaction
        mDatabase.endTransaction();

        // the listener should have been told that commit was called
        assertEquals(mTransactionListenerOnBeginCalled, true);
        assertEquals(mTransactionListenerOnCommitCalled, false);
        assertEquals(mTransactionListenerOnRollbackCalled, true);
    }

    private class TestSQLiteTransactionListener implements SQLiteTransactionListener {
        public void onBegin() {
            mTransactionListenerOnBeginCalled = true;
        }

        public void onCommit() {
            mTransactionListenerOnCommitCalled = true;
        }

        public void onRollback() {
            mTransactionListenerOnRollbackCalled = true;
        }
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "removed the android-provided group_concat built-in function." +
                    "Instead we are now using the sqlite3.c provided group_concat function." +
                    "and it returns NULL if the columnds to be concatenated have null values" +
                    " in them",
            method = "sqlite3::group_concat built-in function",
            args = {java.lang.String.class}
        )
    public void testGroupConcat() {
        mDatabase.execSQL("CREATE TABLE test (i INT, j TEXT);");

        // insert 2 rows
        String sql = "INSERT INTO test (i) VALUES (?);";
        SQLiteStatement insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 1);
        insertStatement.execute();
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 2);
        insertStatement.execute();
        insertStatement.close();

        // make sure there are 2 rows in the table
        Cursor cursor = mDatabase.rawQuery("SELECT count(*) FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(2, cursor.getInt(0));
        cursor.close();

        // concatenate column j from all the rows. should return NULL
        cursor = mDatabase.rawQuery("SELECT group_concat(j, ' ') FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertNull(cursor.getString(0));
        cursor.close();

        // drop the table
        mDatabase.execSQL("DROP TABLE test;");
        // should get no exceptions
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test schema changes - change existing table.",
            method = "compileStatement",
            args = {java.lang.String.class}
        )
    public void testSchemaChanges() {
        mDatabase.execSQL("CREATE TABLE test (i INT, j INT);");

        // at the beginning, there is no record in the database.
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM test", null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();

        String sql = "INSERT INTO test VALUES (?, ?);";
        SQLiteStatement insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 1);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, 2);
        insertStatement.execute();
        insertStatement.close();

        // read the data from the table and make sure it is correct
        cursor = mDatabase.rawQuery("SELECT i,j FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        cursor.close();

        // alter the table and execute another statement
        mDatabase.execSQL("ALTER TABLE test ADD COLUMN k int;");
        sql = "INSERT INTO test VALUES (?, ?, ?);";
        insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 3);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, 4);
        DatabaseUtils.bindObjectToProgram(insertStatement, 3, 5);
        insertStatement.execute();
        insertStatement.close();

        // read the data from the table and make sure it is correct
        cursor = mDatabase.rawQuery("SELECT i,j,k FROM test", null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        assertNull(cursor.getString(2));
        cursor.moveToNext();
        assertEquals(3, cursor.getInt(0));
        assertEquals(4, cursor.getInt(1));
        assertEquals(5, cursor.getInt(2));
        cursor.close();

        // make sure the old statement - which should *try to reuse* cached query plan -
        // still works
        cursor = mDatabase.rawQuery("SELECT i,j FROM test", null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        cursor.moveToNext();
        assertEquals(3, cursor.getInt(0));
        assertEquals(4, cursor.getInt(1));
        cursor.close();

        SQLiteStatement deleteStatement = mDatabase.compileStatement("DELETE FROM test");
        deleteStatement.execute();
        deleteStatement.close();
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test schema changes - add new table.",
            method = "compileStatement",
            args = {java.lang.String.class}
        )
    public void testSchemaChangesNewTable() {
        mDatabase.execSQL("CREATE TABLE test (i INT, j INT);");

        // at the beginning, there is no record in the database.
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM test", null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();

        String sql = "INSERT INTO test VALUES (?, ?);";
        SQLiteStatement insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 1);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, 2);
        insertStatement.execute();
        insertStatement.close();

        // read the data from the table and make sure it is correct
        cursor = mDatabase.rawQuery("SELECT i,j FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        cursor.close();

        // alter the table and execute another statement
        mDatabase.execSQL("CREATE TABLE test_new (i INT, j INT, k INT);");
        sql = "INSERT INTO test_new VALUES (?, ?, ?);";
        insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 3);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, 4);
        DatabaseUtils.bindObjectToProgram(insertStatement, 3, 5);
        insertStatement.execute();
        insertStatement.close();

        // read the data from the table and make sure it is correct
        cursor = mDatabase.rawQuery("SELECT i,j,k FROM test_new", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(3, cursor.getInt(0));
        assertEquals(4, cursor.getInt(1));
        assertEquals(5, cursor.getInt(2));
        cursor.close();

        // make sure the old statement - which should *try to reuse* cached query plan -
        // still works
        cursor = mDatabase.rawQuery("SELECT i,j FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        cursor.close();

        SQLiteStatement deleteStatement = mDatabase.compileStatement("DELETE FROM test");
        deleteStatement.execute();
        deleteStatement.close();

        SQLiteStatement deleteStatement2 = mDatabase.compileStatement("DELETE FROM test_new");
        deleteStatement2.execute();
        deleteStatement2.close();
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test schema changes - drop existing table.",
            method = "compileStatement",
            args = {java.lang.String.class}
        )
    public void testSchemaChangesDropTable() {
        mDatabase.execSQL("CREATE TABLE test (i INT, j INT);");

        // at the beginning, there is no record in the database.
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM test", null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();

        String sql = "INSERT INTO test VALUES (?, ?);";
        SQLiteStatement insertStatement = mDatabase.compileStatement(sql);
        DatabaseUtils.bindObjectToProgram(insertStatement, 1, 1);
        DatabaseUtils.bindObjectToProgram(insertStatement, 2, 2);
        insertStatement.execute();
        insertStatement.close();

        // read the data from the table and make sure it is correct
        cursor = mDatabase.rawQuery("SELECT i,j FROM test", null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
    }
}
