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

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;

@TestTargetClass(android.database.sqlite.SQLiteStatement.class)
public class SQLiteStatementTest extends AndroidTestCase {
    private static final String STRING1 = "this is a test";
    private static final String STRING2 = "another test";

    private static final String DATABASE_NAME = "database_test.db";

    private static final int CURRENT_DATABASE_VERSION = 42;
    private SQLiteDatabase mDatabase;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase(DATABASE_NAME);
        mDatabase = getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        assertNotNull(mDatabase);
        mDatabase.setVersion(CURRENT_DATABASE_VERSION);
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        getContext().deleteDatabase(DATABASE_NAME);
        super.tearDown();
    }

    private void populateDefaultTable() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data TEXT);");
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "execute",
        args = {}
    )
    public void testExecute() {
        populateDefaultTable();

        Cursor c = mDatabase.query("test", null, null, null, null, null, null);
        assertEquals(0, c.getCount());

        // test insert
        SQLiteStatement statement = mDatabase.compileStatement(
                "INSERT INTO test (data) VALUES ('" + STRING1 + "')");
        statement.execute();

        c = mDatabase.query("test", null, null, null, null, null, null);
        assertEquals(1, c.getCount());

        c.moveToFirst();
        assertEquals(STRING1, c.getString(c.getColumnIndex("data")));

        // invalid SQL statement
        statement = mDatabase.compileStatement(
                "SELECT * FROM test WHERE data=\"" + STRING1 + "\"");
        try {
            statement.execute();
            fail("There should be a SQLException thrown out.");
        } catch (SQLException e) {
            // expected.
        }

        c.deactivate();
        statement.close();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "executeInsert",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "execute",
            args = {}
        )
    })
    public void testExecuteInsert() {
        populateDefaultTable();

        Cursor c = mDatabase.query("test", null, null, null, null, null, null);
        assertEquals(0, c.getCount());

        // test insert
        SQLiteStatement statement = mDatabase.compileStatement(
                "INSERT INTO test (data) VALUES ('" + STRING2 + "')");
        assertEquals(1, statement.executeInsert());

        c = mDatabase.query("test", null, null, null, null, null, null);
        assertEquals(1, c.getCount());

        c.moveToFirst();
        assertEquals(STRING2, c.getString(c.getColumnIndex("data")));

        // invalid SQL statement
        statement = mDatabase.compileStatement(
                "SELECT * FROM test WHERE data=\"" + STRING2 + "\"");
        try {
            statement.executeInsert();
            fail("There should be a SQLException thrown out.");
        } catch (SQLException e) {
            // expected.
        }

        c.deactivate();
        statement.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "simpleQueryForLong",
        args = {}
    )
    public void testSimpleQueryForLong() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER NOT NULL, str TEXT NOT NULL);");
        mDatabase.execSQL("INSERT INTO test VALUES (1234, 'hello');");
        SQLiteStatement statement =
                mDatabase.compileStatement("SELECT num FROM test WHERE str = ?");

        // test query long
        statement.bindString(1, "hello");
        long value = statement.simpleQueryForLong();
        assertEquals(1234, value);

        // test query returns zero rows
        statement.bindString(1, "world");

        try {
            statement.simpleQueryForLong();
            fail("There should be a SQLiteDoneException thrown out.");
        } catch (SQLiteDoneException e) {
            // expected.
        }

        statement.close();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "simpleQueryForString",
        args = {}
    )
    public void testSimpleQueryForString() {
        mDatabase.execSQL("CREATE TABLE test (num INTEGER NOT NULL, str TEXT NOT NULL);");
        mDatabase.execSQL("INSERT INTO test VALUES (1234, 'hello');");
        SQLiteStatement statement =
                mDatabase.compileStatement("SELECT str FROM test WHERE num = ?");

        // test query String
        statement.bindLong(1, 1234);
        String value = statement.simpleQueryForString();
        assertEquals("hello", value);

        // test query returns zero rows
        statement.bindLong(1, 5678);

        try {
            statement.simpleQueryForString();
            fail("There should be a SQLiteDoneException thrown out.");
        } catch (SQLiteDoneException e) {
            // expected.
        }

        statement.close();
    }
}
