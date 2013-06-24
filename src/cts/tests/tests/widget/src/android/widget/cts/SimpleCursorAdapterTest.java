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

package android.widget.cts;

import com.android.cts.stub.R;
import com.android.common.ArrayListCursor;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Test {@link SimpleCursorAdapter}.
 */
@TestTargetClass(SimpleCursorAdapter.class)
public class SimpleCursorAdapterTest extends InstrumentationTestCase {
    private static final int ADAPTER_ROW_COUNT = 20;

    private static final int DEFAULT_COLUMN_COUNT = 2;

    private static final int[] VIEWS_TO = new int[] { R.id.cursorAdapter_item0 };

    private static final String[] COLUMNS_FROM = new String[] { "column1" };

    private static final String SAMPLE_IMAGE_NAME = "testimage.jpg";

    /**
     * The simple cursor adapter. Its cursor will be set to
     * {@link SimpleCursorAdapterTest#mCursor} It will use internal
     * R.layout.simple_list_item_1.
     */
    private SimpleCursorAdapter mSimpleCursorAdapter;

    private Context mContext;

    /**
     * The original cursor and its content will be set to:
     * <TABLE>
     * <TR>
     * <TH>Column0</TH>
     * <TH>Column1</TH>
     * </TR>
     * <TR>
     * <TD>00</TD>
     * <TD>01</TD>
     * </TR>
     * <TR>
     * <TD>10</TD>
     * <TD>11</TD>
     * </TR>
     * <TR>
     * <TD>...</TD>
     * <TD>...</TD>
     * </TR>
     * <TR>
     * <TD>190</TD>
     * <TD>191</TD>
     * </TR>
     * </TABLE>
     * It has 2 columns and 20 rows
     */
    private Cursor mCursor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();

        mCursor = createTestCursor(DEFAULT_COLUMN_COUNT, ADAPTER_ROW_COUNT);
        mSimpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.cursoradapter_item0,
                mCursor, COLUMNS_FROM, VIEWS_TO);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SimpleCursorAdapter",
        args = {android.content.Context.class, int.class, android.database.Cursor.class,
                java.lang.String[].class, int[].class}
    )
    public void testConstructor() {
        new SimpleCursorAdapter(mContext, R.layout.cursoradapter_item0,
                createTestCursor(DEFAULT_COLUMN_COUNT, ADAPTER_ROW_COUNT),
                COLUMNS_FROM, VIEWS_TO);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "bindView",
        args = {android.view.View.class, android.content.Context.class,
                android.database.Cursor.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleCursorAdapter#bindView(View, Context, Cursor) if the param view is "
            + "not TextView or ImageView and ViewBinder failed to bind the view")
    public void testBindView() {
        TextView listItem = (TextView) mSimpleCursorAdapter.newView(mContext, null, null);

        listItem.setText("");
        mCursor.moveToFirst();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertEquals("01", listItem.getText().toString());

        mCursor.moveToLast();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertEquals("191", listItem.getText().toString());

        // the binder take care of binding
        listItem.setText("");
        MockViewBinder binder = new MockViewBinder(true);
        mSimpleCursorAdapter.setViewBinder(binder);
        binder.reset();
        mCursor.moveToFirst();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertTrue(binder.hasCalledSetViewValueCalledCount());
        assertEquals("", listItem.getText().toString());

        // the binder try to bind but fail
        binder = new MockViewBinder(false);
        mSimpleCursorAdapter.setViewBinder(binder);
        mCursor.moveToLast();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertTrue(binder.hasCalledSetViewValueCalledCount());
        assertEquals("191", listItem.getText().toString());

        final int [] to = { R.id.cursorAdapter_host };
        mSimpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.cursoradapter_host,
                mCursor, COLUMNS_FROM, to);
        LinearLayout illegalView = (LinearLayout)mSimpleCursorAdapter.newView(mContext, null, null);
        try {
            // The IllegalStateException already gets thrown in the line above.
            mSimpleCursorAdapter.bindView(illegalView, null, mCursor);
            fail("Should throw IllegalStateException if the view is not TextView or ImageView");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getViewBinder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setViewBinder",
            args = {ViewBinder.class}
        )
    })
    public void testAccessViewBinder() {
        assertNull(mSimpleCursorAdapter.getViewBinder());

        MockViewBinder binder = new MockViewBinder(true);
        mSimpleCursorAdapter.setViewBinder(binder);
        assertSame(binder, mSimpleCursorAdapter.getViewBinder());

        binder = new MockViewBinder(false);
        mSimpleCursorAdapter.setViewBinder(binder);
        assertSame(binder, mSimpleCursorAdapter.getViewBinder());

        mSimpleCursorAdapter.setViewBinder(null);
        assertNull(mSimpleCursorAdapter.getViewBinder());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setViewText",
        args = {TextView.class, String.class}
    )
    public void testSetViewText() {
        TextView view = new TextView(mContext);
        mSimpleCursorAdapter.setViewText(view, "expected");
        assertEquals("expected", view.getText().toString());

        mSimpleCursorAdapter.setViewText(view, null);
        assertEquals("", view.getText().toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setViewImage",
        args = {ImageView.class, String.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleCursorAdapter#setViewImage(ImageView, String) if the param String is null")
    public void testSetViewImage() {
        // resId
        int sceneryImgResId = com.android.cts.stub.R.drawable.scenery;
        ImageView view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleCursorAdapter.setViewImage(view, String.valueOf(sceneryImgResId));
        assertNotNull(view.getDrawable());
        BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(
                sceneryImgResId);
        WidgetTestUtils.assertEquals(d.getBitmap(),
                ((BitmapDrawable) view.getDrawable()).getBitmap());

        // blank
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleCursorAdapter.setViewImage(view, "");
        assertNull(view.getDrawable());

        // null
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        try {
            // Should declare NullPoinertException if the uri or value is null
            mSimpleCursorAdapter.setViewImage(view, null);
            fail("Should throw NullPointerException if the uri or value is null");
        } catch (NullPointerException e) {
            // expected
        }

        // uri
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        try {
            int testimgRawId = com.android.cts.stub.R.raw.testimage;
            mSimpleCursorAdapter.setViewImage(view,
                    createTestImage(mContext, SAMPLE_IMAGE_NAME, testimgRawId));
            assertNotNull(view.getDrawable());
            Bitmap actualBitmap = ((BitmapDrawable) view.getDrawable()).getBitmap();
            Bitmap testBitmap = WidgetTestUtils.getUnscaledAndDitheredBitmap(mContext.getResources(),
                    testimgRawId, actualBitmap.getConfig());
            WidgetTestUtils.assertEquals(testBitmap, actualBitmap);
        } finally {
            destroyTestImage(mContext, SAMPLE_IMAGE_NAME);
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStringConversionColumn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStringConversionColumn",
            args = {int.class}
        )
    })
    @ToBeFixed(explanation = "SimpleCursorAdapter#setStringConversionColumn(int) "
            + "should check whether the param stringConversionColumn is out of index")
    public void testAccessStringConversionColumn() {
        // default is -1
        assertEquals(-1, mSimpleCursorAdapter.getStringConversionColumn());

        mSimpleCursorAdapter.setStringConversionColumn(1);
        assertEquals(1, mSimpleCursorAdapter.getStringConversionColumn());

        // Should check whether the column index is out of bounds
        mSimpleCursorAdapter.setStringConversionColumn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, mSimpleCursorAdapter.getStringConversionColumn());

        // Should check whether the column index is out of bounds
        mSimpleCursorAdapter.setStringConversionColumn(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, mSimpleCursorAdapter.getStringConversionColumn());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCursorToStringConverter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCursorToStringConverter",
            args = {CursorToStringConverter.class}
        )
    })
    public void testAccessCursorToStringConverter() {
        // default is null
        assertNull(mSimpleCursorAdapter.getCursorToStringConverter());

        CursorToStringConverter converter = new MockCursorToStringConverter();
        mSimpleCursorAdapter.setCursorToStringConverter(converter);
        assertSame(converter, mSimpleCursorAdapter.getCursorToStringConverter());

        mSimpleCursorAdapter.setCursorToStringConverter(null);
        assertNull(mSimpleCursorAdapter.getCursorToStringConverter());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleCursorAdapter#changeCursor(Cursor)}",
        method = "changeCursor",
        args = {Cursor.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleCursorAdapter#changeCursor(Cursor) if the param cursor does not contain"
            + "any column passed in the constructor")
    public void testChangeCursor() {
        // have "column1"
        Cursor curWith3Columns = createTestCursor(3, ADAPTER_ROW_COUNT);
        mSimpleCursorAdapter.changeCursor(curWith3Columns);
        assertSame(curWith3Columns, mSimpleCursorAdapter.getCursor());

        // does not have "column1"
        Cursor curWith1Column = createTestCursor(1, ADAPTER_ROW_COUNT);
        try {
            mSimpleCursorAdapter.changeCursor(curWith1Column);
            fail("Should throw exception if the cursor does not have the "
                    + "original column passed in the constructor");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleCursorAdapter#convertToString(Cursor)}",
        method = "convertToString",
        args = {Cursor.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleCursorAdapter#convertToString(Cursor) if the StringConversionColumn set "
            + "by  SimpleCursorAdapter#setStringConversionColumn(int) is beyond the columns count")
    public void testConvertToString() {
        mCursor.moveToFirst();
        assertEquals("", mSimpleCursorAdapter.convertToString(null));

        // converter is null, StringConversionColumn is set to negative
        mSimpleCursorAdapter.setStringConversionColumn(Integer.MIN_VALUE);
        assertEquals(mCursor.toString(), mSimpleCursorAdapter.convertToString(mCursor));

        // converter is null, StringConversionColumn is set to 1
        mSimpleCursorAdapter.setStringConversionColumn(1);
        assertEquals("01", mSimpleCursorAdapter.convertToString(mCursor));

        // converter is null, StringConversionColumn is set to 3 (larger than columns count)
        // the cursor has 3 columns including column0, column1 and _id which is added automatically
        mSimpleCursorAdapter.setStringConversionColumn(DEFAULT_COLUMN_COUNT + 1);
        try {
            mSimpleCursorAdapter.convertToString(mCursor);
            fail("Should throw IndexOutOfBoundsException if index is beyond the columns count");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        Cursor curWith3Columns = createTestCursor(DEFAULT_COLUMN_COUNT + 1, ADAPTER_ROW_COUNT);
        curWith3Columns.moveToFirst();

        // converter is null, StringConversionColumn is set to 3
        // and covert with a cursor which has 4 columns
        mSimpleCursorAdapter.setStringConversionColumn(2);
        assertEquals("02", mSimpleCursorAdapter.convertToString(curWith3Columns));

        // converter is not null, StringConversionColumn is 1
        CursorToStringConverter converter = new MockCursorToStringConverter();
        mSimpleCursorAdapter.setCursorToStringConverter(converter);
        mSimpleCursorAdapter.setStringConversionColumn(1);
        ((MockCursorToStringConverter) converter).reset();
        mSimpleCursorAdapter.convertToString(curWith3Columns);
        assertTrue(((MockCursorToStringConverter) converter).hasCalledConvertToString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newView",
        args = {Context.class, Cursor.class, ViewGroup.class}
    )
    public void testNewView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(
                com.android.cts.stub.R.layout.cursoradapter_host, null);
        View result = mSimpleCursorAdapter.newView(mContext, null, viewGroup);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());

        result = mSimpleCursorAdapter.newView(mContext, null, null);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newDropDownView",
        args = {Context.class, Cursor.class, ViewGroup.class}
    )
    public void testNewDropDownView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(
                com.android.cts.stub.R.layout.cursoradapter_host, null);
        View result = mSimpleCursorAdapter.newDropDownView(null, null, viewGroup);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "changeCursorAndColumns",
        args = {Cursor.class, String[].class, int[].class}
    )
    public void testChangeCursorAndColumns() {
        assertSame(mCursor, mSimpleCursorAdapter.getCursor());

        TextView listItem = (TextView) mSimpleCursorAdapter.newView(mContext, null, null);

        mCursor.moveToFirst();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertEquals("01", listItem.getText().toString());

        mCursor.moveToLast();
        mSimpleCursorAdapter.bindView(listItem, null, mCursor);
        assertEquals("191", listItem.getText().toString());

        Cursor newCursor = createTestCursor(3, ADAPTER_ROW_COUNT);
        final String[] from = new String[] { "column2" };
        mSimpleCursorAdapter.changeCursorAndColumns(newCursor, from, VIEWS_TO);
        assertSame(newCursor, mSimpleCursorAdapter.getCursor());
        newCursor.moveToFirst();
        mSimpleCursorAdapter.bindView(listItem, null, newCursor);
        assertEquals("02", listItem.getText().toString());

        newCursor.moveToLast();
        mSimpleCursorAdapter.bindView(listItem, null, newCursor);
        assertEquals("192", listItem.getText().toString());

        mSimpleCursorAdapter.changeCursorAndColumns(null, null, null);
        assertNull(mSimpleCursorAdapter.getCursor());
    }

    /**
     * Creates the test cursor.
     *
     * @param colCount the column count
     * @param rowCount the row count
     * @return the cursor
     */
    @SuppressWarnings("unchecked")
    private Cursor createTestCursor(int colCount, int rowCount) {
        ArrayList<ArrayList> list = new ArrayList<ArrayList>();
        String[] columns = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            columns[i] = "column" + i;
        }

        for (int i = 0; i < rowCount; i++) {
            ArrayList<String> row = new ArrayList<String>();
            for (int j = 0; j < colCount; j++) {
                row.add("" + i + "" + j);
            }
            list.add(row);
        }

        return new ArrayListCursor(columns, list);
    }

    private static class MockViewBinder implements ViewBinder {
        private boolean mExpectedResult;

        private boolean mHasCalledSetViewValue;

        public MockViewBinder(boolean expectedResult) {
            mExpectedResult = expectedResult;
        }

        public void reset(){
            mHasCalledSetViewValue = false;
        }

        public boolean hasCalledSetViewValueCalledCount() {
            return mHasCalledSetViewValue;
        }

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            mHasCalledSetViewValue = true;
            return mExpectedResult;
        }
    }

    public static String createTestImage(Context context, String fileName, int resId) {
        InputStream source = null;
        OutputStream target = null;

        try {
            source = context.getResources().openRawResource(resId);
            target = context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);

            byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                target.write(buffer, 0, len);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (target != null) {
                    target.close();
                }
            } catch (IOException e) {
                // Ignore the IOException.
            }
        }

        return context.getFileStreamPath(fileName).getAbsolutePath();
    }

    public static void destroyTestImage(Context context, String fileName) {
        context.deleteFile(fileName);
    }

    private static class MockCursorToStringConverter implements CursorToStringConverter {
        private boolean mHasCalledConvertToString;

        public boolean hasCalledConvertToString() {
            return mHasCalledConvertToString;
        }

        public void reset(){
            mHasCalledConvertToString = false;
        }

        public CharSequence convertToString(Cursor cursor) {
            mHasCalledConvertToString = true;
            return null;
        }
    }
}
