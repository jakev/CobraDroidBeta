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
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

@TestTargetClass(android.database.sqlite.SQLiteQueryBuilder.class)
public class SQLiteQueryBuilderTest extends AndroidTestCase {
    private SQLiteDatabase mDatabase;
    private final String TEST_TABLE_NAME = "test";
    private final String EMPLOYEE_TABLE_NAME = "employee";
    private static final String DATABASE_FILE = "database_test.db";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase(DATABASE_FILE);
        mDatabase = getContext().openOrCreateDatabase(DATABASE_FILE, Context.MODE_PRIVATE, null);
        assertNotNull(mDatabase);
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        getContext().deleteDatabase(DATABASE_FILE);
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SQLiteQueryBuilder",
        args = {}
    )
    public void testConstructor() {
        new SQLiteQueryBuilder();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDistinct",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTables",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTables",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "appendWhere",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "appendWhereEscapeString",
            args = {java.lang.String.class}
        )
    })
    public void testSetDistinct() {
        String expected;
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(TEST_TABLE_NAME);
        sqliteQueryBuilder.setDistinct(false);
        sqliteQueryBuilder.appendWhere("age=20");
        String sql = sqliteQueryBuilder.buildQuery(new String[] { "age", "address" },
                null, null, null, null, null, null);
        assertEquals(TEST_TABLE_NAME, sqliteQueryBuilder.getTables());
        expected = "SELECT age, address FROM " + TEST_TABLE_NAME + " WHERE (age=20)";
        assertEquals(expected, sql);

        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(EMPLOYEE_TABLE_NAME);
        sqliteQueryBuilder.setDistinct(true);
        sqliteQueryBuilder.appendWhere("age>32");
        sql = sqliteQueryBuilder.buildQuery(new String[] { "age", "address" },
                null, null, null, null, null, null);
        assertEquals(EMPLOYEE_TABLE_NAME, sqliteQueryBuilder.getTables());
        expected = "SELECT DISTINCT age, address FROM " + EMPLOYEE_TABLE_NAME + " WHERE (age>32)";
        assertEquals(expected, sql);

        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(EMPLOYEE_TABLE_NAME);
        sqliteQueryBuilder.setDistinct(true);
        sqliteQueryBuilder.appendWhereEscapeString("age>32");
        sql = sqliteQueryBuilder.buildQuery(new String[] { "age", "address" },
                null, null, null, null, null, null);
        assertEquals(EMPLOYEE_TABLE_NAME, sqliteQueryBuilder.getTables());
        expected = "SELECT DISTINCT age, address FROM " + EMPLOYEE_TABLE_NAME
                + " WHERE ('age>32')";
        assertEquals(expected, sql);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setProjectionMap",
        args = {java.util.Map.class}
    )
    public void testSetProjectionMap() {
        String expected;
        Map<String, String> projectMap = new HashMap<String, String>();
        projectMap.put("EmployeeName", "name");
        projectMap.put("EmployeeAge", "age");
        projectMap.put("EmployeeAddress", "address");
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(TEST_TABLE_NAME);
        sqliteQueryBuilder.setDistinct(false);
        sqliteQueryBuilder.setProjectionMap(projectMap);
        String sql = sqliteQueryBuilder.buildQuery(new String[] { "EmployeeName", "EmployeeAge" },
                null, null, null, null, null, null);
        expected = "SELECT name, age FROM " + TEST_TABLE_NAME;
        assertEquals(expected, sql);

        sql = sqliteQueryBuilder.buildQuery(null, // projectionIn is null
                null, null, null, null, null, null);
        // TODO: implement an order-independent way of doing the projection columns comparison
        expected = "SELECT age, name, address FROM " + TEST_TABLE_NAME;
        assertEquals(expected, sql);

        sqliteQueryBuilder.setProjectionMap(null);
        sql = sqliteQueryBuilder.buildQuery(new String[] { "name", "address" },
                null, null, null, null, null, null);
        expected = "SELECT name, address FROM " + TEST_TABLE_NAME;
        assertEquals(expected, sql);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCursorFactory",
        args = {android.database.sqlite.SQLiteDatabase.CursorFactory.class}
    )
    public void testSetCursorFactory() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, " +
                "name TEXT, age INTEGER, address TEXT);");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('Mike', '20', 'LA');");
        mDatabase.execSQL("INSERT INTO test (name, age, address) VALUES ('jack', '40', 'LA');");

        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(TEST_TABLE_NAME);
        Cursor cursor = sqliteQueryBuilder.query(mDatabase, new String[] { "name", "age" },
                null, null, null, null, null);
        assertNotNull(cursor);
        assertTrue(cursor instanceof SQLiteCursor);

        SQLiteDatabase.CursorFactory factory = new SQLiteDatabase.CursorFactory() {
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                    String editTable, SQLiteQuery query) {
                return new MockCursor(db, masterQuery, editTable, query);
            }
        };

        sqliteQueryBuilder.setCursorFactory(factory);
        cursor = sqliteQueryBuilder.query(mDatabase, new String[] { "name", "age" },
                null, null, null, null, null);
        assertNotNull(cursor);
        assertTrue(cursor instanceof MockCursor);
    }

    private static class MockCursor extends SQLiteCursor {
        public MockCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
                String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "buildQueryString",
        args = {boolean.class, java.lang.String.class, java.lang.String[].class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class}
    )
    public void testBuildQueryString() {
        String expected;
        final String[] DEFAULT_TEST_PROJECTION = new String [] { "name", "age", "sum(salary)" };
        final String DEFAULT_TEST_WHERE = "age > 25";
        final String DEFAULT_HAVING = "sum(salary) > 3000";

        String sql = SQLiteQueryBuilder.buildQueryString(false, "Employee",
                DEFAULT_TEST_PROJECTION,
                DEFAULT_TEST_WHERE, "name", DEFAULT_HAVING, "name", "100");

        expected = "SELECT name, age, sum(salary) FROM Employee WHERE " + DEFAULT_TEST_WHERE +
                " GROUP BY name " +
                "HAVING " + DEFAULT_HAVING + " " +
                "ORDER BY name " +
                "LIMIT 100";
        assertEquals(expected, sql);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "buildQuery",
        args = {java.lang.String[].class, java.lang.String.class, java.lang.String[].class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class}
    )
    public void testBuildQuery() {
        final String[] DEFAULT_TEST_PROJECTION = new String[] { "name", "sum(salary)" };
        final String DEFAULT_TEST_WHERE = "age > 25";
        final String DEFAULT_HAVING = "sum(salary) > 2000";

        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(TEST_TABLE_NAME);
        sqliteQueryBuilder.setDistinct(false);
        String sql = sqliteQueryBuilder.buildQuery(DEFAULT_TEST_PROJECTION,
                DEFAULT_TEST_WHERE, null, "name", DEFAULT_HAVING, "name", "2");
        String expected = "SELECT name, sum(salary) FROM " + TEST_TABLE_NAME
                + " WHERE (" + DEFAULT_TEST_WHERE + ") " +
                "GROUP BY name HAVING " + DEFAULT_HAVING + " ORDER BY name LIMIT 2";
        assertEquals(expected, sql);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "appendColumns",
        args = {java.lang.StringBuilder.class, java.lang.String[].class}
    )
    public void testAppendColumns() {
        StringBuilder sb = new StringBuilder();
        String[] columns = new String[] { "name", "age" };

        assertEquals("", sb.toString());
        SQLiteQueryBuilder.appendColumns(sb, columns);
        assertEquals("name, age ", sb.toString());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "query",
            args = {android.database.sqlite.SQLiteDatabase.class, java.lang.String[].class,
                    java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "query",
            args = {android.database.sqlite.SQLiteDatabase.class, java.lang.String[].class,
                    java.lang.String.class, java.lang.String[].class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class}
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

        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables("Employee");
        Cursor cursor = sqliteQueryBuilder.query(mDatabase,
                new String[] { "name", "sum(salary)" }, null, null,
                "name", "sum(salary)>1000", "name");
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

        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(EMPLOYEE_TABLE_NAME);
        cursor = sqliteQueryBuilder.query(mDatabase,
                new String[] { "name", "sum(salary)" }, null, null,
                "name", "sum(salary)>1000", "name", "2" // limit is 2
                );
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("Jim", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4500, cursor.getInt(COLUMN_SALARY_INDEX));
        cursor.moveToNext();
        assertEquals("Mike", cursor.getString(COLUMN_NAME_INDEX));
        assertEquals(4000, cursor.getInt(COLUMN_SALARY_INDEX));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "buildUnionQuery",
            args = {java.lang.String[].class, java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "buildUnionSubQuery",
            args = {java.lang.String.class, java.lang.String[].class, java.util.Set.class,
                    int.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String[].class, java.lang.String.class, java.lang.String.class}
        )
    })
    public void testUnionQuery() {
        String expected;
        String[] innerProjection = new String[] {"name", "age", "location"};
        SQLiteQueryBuilder employeeQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder peopleQueryBuilder = new SQLiteQueryBuilder();

        employeeQueryBuilder.setTables("employee");
        peopleQueryBuilder.setTables("people");

        String employeeSubQuery = employeeQueryBuilder.buildUnionSubQuery(
                "_id", innerProjection,
                null, 2, "employee",
                "age=25",
                null, null, null);
        String peopleSubQuery = peopleQueryBuilder.buildUnionSubQuery(
                "_id", innerProjection,
                null, 2, "people",
                "location=LA",
                null, null, null);
        expected = "SELECT name, age, location FROM employee WHERE (age=25)";
        assertEquals(expected, employeeSubQuery);
        expected = "SELECT name, age, location FROM people WHERE (location=LA)";
        assertEquals(expected, peopleSubQuery);

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { employeeSubQuery, peopleSubQuery }, null, null);
        expected = "SELECT name, age, location FROM employee WHERE (age=25) " +
                "UNION SELECT name, age, location FROM people WHERE (location=LA)";
        assertEquals(expected, unionQuery);
    }
}
