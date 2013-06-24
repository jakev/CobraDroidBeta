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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcelable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

@TestTargetClass(AbsSpinner.class)
public class AbsSpinnerTest extends ActivityInstrumentationTestCase2<RelativeLayoutStubActivity> {
    private Context mContext;

    public AbsSpinnerTest() {
        super("com.android.cts.stub", RelativeLayoutStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    @ToBeFixed(bug="1448885", explanation="AbsSpinner is an abstract class and its abstract " +
            "method layout(int, boolean) is package private, we can not extends it directly " +
            "to test. So, we use its subclass Spinner and Gallery to test")

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new Spinner(mContext);

        new Spinner(mContext, null);

        new Spinner(mContext, null, com.android.internal.R.attr.spinnerStyle);

        new Gallery(mContext);
        new Gallery(mContext, null);
        new Gallery(mContext, null, 0);

        XmlPullParser parser = mContext.getResources().getXml(R.layout.gallery_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Gallery(mContext, attrs);
        new Gallery(mContext, attrs, 0);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSelection",
            args = {int.class, boolean.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setSelection() is incomplete." +
            "1. no description about the @param and @return.")
    @UiThreadTest
    /**
     * Check points:
     * 1. Jump to the specific item.
     */
    public void testSetSelectionIntBoolean() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());

        absSpinner.setSelection(1, true);
        assertEquals(1, absSpinner.getSelectedItemPosition());

        absSpinner.setSelection(absSpinner.getCount() - 1, false);
        assertEquals(absSpinner.getCount() - 1, absSpinner.getSelectedItemPosition());

        // The animation effect depends on implementation in AbsSpinner's subClass.
        // It is not meaningful to check it.
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSelection",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setSelection() is incomplete." +
            "1. no description about the @param and @return.")
    @UiThreadTest
    /**
     * Check points:
     * 1. the currently selected item should be the one which set using this method.
     */
    public void testSetSelectionInt() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());

        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());

        absSpinner.setSelection(absSpinner.getCount() - 1);
        assertEquals(absSpinner.getCount() - 1, absSpinner.getSelectedItemPosition());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setAdapter",
            args = {android.widget.SpinnerAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAdapter",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setAdapter() is incomplete." +
            "1. no description about the situation that adapter is null.")
    @UiThreadTest
    /**
     * Check points:
     * 1. the adapter returned from getAdapter() should be the one specified using setAdapter().
     * 2. the adapter provides methods to transform spinner items based on their position
     * relative to the selected item.
     */
    public void testAccessAdapter() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        absSpinner.setAdapter(adapter);
        assertSame(adapter, absSpinner.getAdapter());
        assertEquals(adapter.getCount(), absSpinner.getCount());
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(1), absSpinner.getSelectedItemId());

        // issue 1695243, if adapter is null, NullPointerException will be thrown when do layout.
        // There is neither limit in code nor description about it in javadoc.
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "requestLayout",
        args = {}
    )
    public void testRequestLayout() {
        AbsSpinner absSpinner = new Spinner(mContext);
        absSpinner.layout(0, 0, 200, 300);
        assertFalse(absSpinner.isLayoutRequested());

        absSpinner.requestLayout();
        assertTrue(absSpinner.isLayoutRequested());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCount",
        args = {}
    )
    @UiThreadTest
    /**
     * Check points:
     * 1. The value returned from getCount() equals the count of Adapter associated with
     * this AdapterView.
     */
    public void testGetCount() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);

        absSpinner.setAdapter(adapter1);
        assertEquals(adapter1.getCount(), absSpinner.getCount());

        CharSequence anotherStringArray[] = { "another array string 1", "another array string 2" };
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(mContext,
                android.R.layout.simple_spinner_item, anotherStringArray);

        absSpinner.setAdapter(adapter2);
        assertEquals(anotherStringArray.length, absSpinner.getCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "pointToPosition",
        args = {int.class, int.class}
    )
    /**
     * Check points:
     * 1. Should return the position of the item which contains the specified point.
     * 2. Should return INVALID_POSITION if the point does not intersect an item
     */
    public void testPointToPosition() {
        AbsSpinner absSpinner = new Gallery(mContext);
        MockSpinnerAdapter adapter = new MockSpinnerAdapter();
        assertEquals(AdapterView.INVALID_POSITION, absSpinner.pointToPosition(10, 10));

        adapter.setCount(3);
        absSpinner.setAdapter(adapter);
        absSpinner.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Rect rc = new Rect(0, 0, 100, 100);
        Rect rcChild0 = new Rect(0, 0, 20, rc.bottom);
        Rect rcChild1 = new Rect(rcChild0.right, 0, 70, rc.bottom);
        Rect rcChild2 = new Rect(rcChild1.right, 0, rc.right, rc.bottom);
        absSpinner.layout(rc.left, rc.top, rc.right, rc.bottom);
        absSpinner.getChildAt(0).layout(rcChild0.left, rcChild0.top,
                rcChild0.right, rcChild0.bottom);
        absSpinner.getChildAt(1).layout(rcChild1.left, rcChild1.top,
                rcChild1.right, rcChild1.bottom);
        absSpinner.getChildAt(2).layout(rcChild2.left, rcChild2.top,
                rcChild2.right, rcChild2.bottom);

        assertEquals(AdapterView.INVALID_POSITION, absSpinner.pointToPosition(-1, -1));
        assertEquals(0, absSpinner.pointToPosition(rcChild0.left + 1, rc.bottom - 1));
        assertEquals(1, absSpinner.pointToPosition(rcChild1.left + 1, rc.bottom - 1));
        assertEquals(2, absSpinner.pointToPosition(rcChild2.left + 1, rc.bottom - 1));
        assertEquals(AdapterView.INVALID_POSITION,
                absSpinner.pointToPosition(rc.right + 1, rc.bottom - 1));

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSelectedView",
        args = {}
    )
    /**
     * Check points:
     * 1. Should return the view corresponding to the currently selected item.
     * 2. Should return null if nothing is selected.
     */
    public void testGetSelectedView() {
        AbsSpinner absSpinner = new Gallery(mContext);
        MockSpinnerAdapter adapter = new MockSpinnerAdapter();
        assertNull(absSpinner.getSelectedView());

        absSpinner.setAdapter(adapter);
        absSpinner.layout(0, 0, 20, 20);
        assertSame(absSpinner.getChildAt(0), absSpinner.getSelectedView());

        absSpinner.setSelection(1, true);
        assertSame(absSpinner.getChildAt(1), absSpinner.getSelectedView());

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        )
    })
    @UiThreadTest
    /**
     * Check points:
     * 1. the view's current state saved by onSaveInstanceState() should be correctly restored
     * after onRestoreInstanceState().
     */
    public void testOnSaveAndRestoreInstanceState() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
        Parcelable parcelable = absSpinner.onSaveInstanceState();

        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(1), absSpinner.getSelectedItemId());

        absSpinner.onRestoreInstanceState(parcelable);
        absSpinner.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        absSpinner.layout(absSpinner.getLeft(), absSpinner.getTop(), absSpinner.getRight(),
                absSpinner.getBottom());
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "generateDefaultLayoutParams",
        args = {}
    )
    @ToBeFixed(bug = "1448885", explanation = "AbsSpinner#generateDefaultLayoutParams() is" +
            " protected method, we have to test it in MockSpinner which extends from" +
            " AbsSpinner. class MockSpinner must implement the inherited abstract method" +
            " AbsSpinner.layout(int, boolean), but cannot override it since it is not" +
            " visible from MockSpinner. So we cannot test this method.")
    public void testGenerateDefaultLayoutParams() {
//        final MockSpinner absSpinner = new MockSpinner(mContext);
//        LayoutParams layoutParams = (LayoutParams) absSpinner.generateDefaultLayoutParams();
//        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
//        assertEquals(LayoutParams.WRAP_CONTENT, layoutParams.height);
    }

    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onMeasure(int, int) function.",
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
        // onMeasure() is implementation details, do NOT test
    }

    /*
     * The Mock class of SpinnerAdapter to be used in test.
     */
    private class MockSpinnerAdapter implements SpinnerAdapter {
        public static final int DEFAULT_COUNT = 1;
        private int mCount = DEFAULT_COUNT;

        public View getDropDownView(int position, View convertView,
                ViewGroup parent) {
            return null;
        }

        public int getCount() {
            return mCount;
        }

        public void setCount(int count) {
            mCount = count;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return new ImageView(mContext);
        }

        public int getViewTypeCount() {
            return 0;
        }

        public boolean hasStableIds() {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public void registerDataSetObserver(DataSetObserver observer) {
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
        }
    }
}
