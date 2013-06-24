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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

@TestTargetClass(android.database.sqlite.SQLiteProgram.class)
public class SQLiteProgramTest extends AndroidTestCase {
    private static final String DATABASE_NAME = "database_test.db";

    private SQLiteDatabase mDatabase;

    @ToBeFixed(bug="1448885", explanation="SQLiteProgram is an abstract class and its " +
            "constructor is package private, so it can not be extended directly to test. " +
            "For this reason, the test uses SQLiteStatement instead.")
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase(DATABASE_NAME);
        mDatabase = getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        assertNotNull(mDatabase);
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        getContext().deleteDatabase(DATABASE_NAME);

        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getUniqueId()",
        method = "getUniqueId",
        args = {}
    )
    public void testGetUniqueId() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");
        final String statement = "DELETE FROM test WHERE _id=?;";
        SQLiteStatement statementOne = mDatabase.compileStatement(statement);
        SQLiteStatement statementTwo = mDatabase.compileStatement(statement);
        // since the same compiled statement is being accessed at the same time by 2 different
        // objects, they each get their own statement id
        assertTrue(statementOne.getUniqueId() != statementTwo.getUniqueId());
        statementOne.close();
        statementTwo.close();
        
        statementOne = mDatabase.compileStatement(statement);
        int n = statementOne.getUniqueId();
        statementOne.close();
        statementTwo = mDatabase.compileStatement(statement);
        assertEquals(n, statementTwo.getUniqueId());
        statementTwo.close();

        // now try to compile 2 different statements and they should have different uniquerIds.
        SQLiteStatement statement1 = mDatabase.compileStatement("DELETE FROM test WHERE _id=1;");
        SQLiteStatement statement2 = mDatabase.compileStatement("DELETE FROM test WHERE _id=2;");
        assertTrue(statement1.getUniqueId() != statement2.getUniqueId());
        statement1.close();
        statement2.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onAllReferencesReleased(). Since sql statements are always cached in " +
        		"SQLiteDatabase, compiledSql should NOT be released " +
        		"when onAllReferencesReleased() is called",
        method = "onAllReferencesReleased",
        args = {}
    )
    public void testOnAllReferencesReleased() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");
        final String statement = "DELETE FROM test WHERE _id=?;";
        SQLiteStatement statementOne = mDatabase.compileStatement(statement);
        assertTrue(statementOne.getUniqueId() > 0);
        int nStatement = statementOne.getUniqueId();
        statementOne.releaseReference();
        assertTrue(statementOne.getUniqueId() == nStatement);
        statementOne.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onAllReferencesReleasedFromContainer(). " +
        		"Since sql statements are always cached in " +
                "SQLiteDatabase, compiledSql should NOT be released " +
                "when onAllReferencesReleasedFromContainer() is called",
        args = {}
    )
    public void testOnAllReferencesReleasedFromContainer() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");
        final String statement = "DELETE FROM test WHERE _id=?;";
        SQLiteStatement statementOne = mDatabase.compileStatement(statement);
        assertTrue(statementOne.getUniqueId() > 0);
        int nStatement = statementOne.getUniqueId();
        statementOne.releaseReferenceFromContainer();
        assertTrue(statementOne.getUniqueId() == nStatement);
        statementOne.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "bindLong",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "bindDouble",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "bindString",
            args = {int.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearBindings",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "close",
            args = {}
        )
    })
    public void testBind() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");
        mDatabase.execSQL("INSERT INTO test (text1, text2, num1, num2, image) " +
                "VALUES ('Mike', 'Jack', 12, 30, 'abcdefg');");
        mDatabase.execSQL("INSERT INTO test (text1, text2, num1, num2, image) " +
                "VALUES ('test1', 'test2', 213, 589, '123456789');");
        SQLiteStatement statement;

        statement = mDatabase.compileStatement("SELECT num1 FROM test WHERE num2 = ?;");
        statement.bindLong(1, 30);
        assertEquals(12, statement.simpleQueryForLong());

        // re-bind without clearing
        statement.bindDouble(1, 589.0);
        assertEquals(213, statement.simpleQueryForLong());
        statement.close();

        statement = mDatabase.compileStatement("SELECT text1 FROM test WHERE text2 = ?;");

        statement.bindDouble(1, 589.0); // Wrong binding
        try {
            statement.simpleQueryForString();
            fail("Should throw exception (no rows found)");
        } catch (SQLiteDoneException expected) {
            // expected
        }
        statement.bindString(1, "test2");
        assertEquals("test1", statement.simpleQueryForString());
        statement.clearBindings();
        try {
            statement.simpleQueryForString();
            fail("Should throw exception (unbound value)");
        } catch (SQLiteDoneException expected) {
            // expected
        }
        statement.close();

        statement = mDatabase.compileStatement("SELECT text1 FROM test;");
        try {
            statement.bindString(1, "foo");
            fail("Should throw exception (no value to bind)");
        } catch (SQLiteException expected) {
            // expected
        }

        statement =
            mDatabase.compileStatement("SELECT text1 FROM test WHERE text2 = ? AND num2 = ?;");

        try {
            statement.bindString(0, "Jack");
            fail("Should throw exception (index is 0)");
        } catch (SQLiteException expected) {
            // expected
        }
        try {
            statement.bindLong(-1, 30);
            fail("Should throw exception (index is negative)");
        } catch (SQLiteException expected) {
            // expected
        }
        try {
            statement.bindDouble(3, 589.0);
            fail("Should throw exception (index too large)");
        } catch (SQLiteException expected) {
            // expected
        }
        // test positive case
        statement.bindString(1, "Jack");
        statement.bindLong(2, 30);
        assertEquals("Mike", statement.simpleQueryForString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test bindNull()",
        method = "bindNull",
        args = {int.class}
    )
    public void testBindNull() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");

        SQLiteStatement statement = mDatabase.compileStatement("INSERT INTO test " +
                "(text1,text2,num1,image) VALUES (?,?,?,?)");
        statement.bindString(1, "string1");
        statement.bindString(2, "string2");
        statement.bindLong(3, 100);
        statement.bindNull(4);
        statement.execute();
        statement.close();

        final int COLUMN_TEXT1_INDEX = 0;
        final int COLUMN_TEXT2_INDEX = 1;
        final int COLUMN_NUM1_INDEX = 2;
        final int COLUMN_IMAGE_INDEX = 3;

        Cursor cursor = mDatabase.query("test", new String[] { "text1", "text2", "num1", "image" },
                null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("string1", cursor.getString(COLUMN_TEXT1_INDEX));
        assertEquals("string2", cursor.getString(COLUMN_TEXT2_INDEX));
        assertEquals(100, cursor.getInt(COLUMN_NUM1_INDEX));
        assertNull(cursor.getString(COLUMN_IMAGE_INDEX));
        cursor.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test bindBlob()",
        method = "bindBlob",
        args = {int.class, byte[].class}
    )
    public void testBindBlob() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, text1 TEXT, text2 TEXT, " +
                "num1 INTEGER, num2 INTEGER, image BLOB);");

        SQLiteStatement statement = mDatabase.compileStatement("INSERT INTO test " +
                "(text1,text2,num1,image) VALUES (?,?,?,?)");
        statement.bindString(1, "string1");
        statement.bindString(2, "string2");
        statement.bindLong(3, 100);
        byte[] blob = new byte[] { '1', '2', '3' };
        statement.bindBlob(4, blob);
        statement.execute();
        statement.close();

        final int COLUMN_TEXT1_INDEX = 0;
        final int COLUMN_TEXT2_INDEX = 1;
        final int COLUMN_NUM1_INDEX = 2;
        final int COLUMN_IMAGE_INDEX = 3;

        Cursor cursor = mDatabase.query("test", new String[] { "text1", "text2", "num1", "image" },
                null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("string1", cursor.getString(COLUMN_TEXT1_INDEX));
        assertEquals("string2", cursor.getString(COLUMN_TEXT2_INDEX));
        assertEquals(100, cursor.getInt(COLUMN_NUM1_INDEX));
        byte[] value = cursor.getBlob(COLUMN_IMAGE_INDEX);
        MoreAsserts.assertEquals(blob, value);
        cursor.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onAllReferencesReleased",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onAllReferencesReleasedFromContainer",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "compile",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "native_bind_blob",
            args = {int.class, byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "native_bind_double",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "native_bind_long",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "native_bind_null",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "native_compile",
            args = {java.lang.String.class}
        )
    })
    @ToBeFixed(bug = "1448885", explanation = "Cannot test protected methods, since constructor" +
        " is private.")
    public void testProtectedMethods() {
        // cannot test
    }

    private void closeDatabaseWithOrphanedStatement(){
        try {
            mDatabase.close();
        } catch (SQLiteException e) {
            // A SQLiteException is thrown if there are some unfinialized exceptions
            // This is expected as some tests explicitly leave statements in this state
            if (!e.getMessage().equals("Unable to close due to unfinalised statements")) {
                throw e;
            }
        }
    }
}
