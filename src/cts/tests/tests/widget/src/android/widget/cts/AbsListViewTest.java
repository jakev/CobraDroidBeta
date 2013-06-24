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
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TestTargetClass(AbsListView.class)
public class AbsListViewTest extends ActivityInstrumentationTestCase2<ListViewStubActivity> {
    private final String[] mCountryList = new String[] {
        "Argentina", "Australia", "China", "France", "Germany", "Italy", "Japan", "United States"
    };

    private ListView mListView;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private AttributeSet mAttributeSet;
    private ArrayAdapter<String> mAdapter_countries;

    private static final float DELTA = 0.001f;

    public AbsListViewTest() {
        super("com.android.cts.stub", ListViewStubActivity.class);
    }

    @ToBeFixed(bug="1448885", explanation="AbsListView is an abstract class and its abstract " +
            "methods: fillGap(boolean), findMotionRow(int) and setSelectionInt(int) are " +
            "package private, we can not extends it directly to test. So, we use its subclass " +
            "ListView to test")

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
        mInstrumentation = getInstrumentation();

        XmlPullParser parser = mActivity.getResources().getXml(R.layout.listview_layout);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        mAttributeSet = Xml.asAttributeSet(parser);

        mAdapter_countries = new ArrayAdapter<String>(mActivity,
                android.R.layout.simple_list_item_1, mCountryList);

        mListView = (ListView)mActivity.findViewById(R.id.listview_default);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "AbsListView",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "AbsListView",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "AbsListView",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        /**
         * We can not test the constructors.
         */
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFastScrollEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFastScrollEnabled",
            args = {}
        )
    })
    public void testAccessFastScrollEnabled() {
        mListView.setFastScrollEnabled(false);
        assertFalse(mListView.isFastScrollEnabled());

        mListView.setFastScrollEnabled(true);
        assertTrue(mListView.isFastScrollEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSmoothScrollbarEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSmoothScrollbarEnabled",
            args = {}
        )
    })
    public void testAccessSmoothScrollbarEnabled() {
        mListView.setSmoothScrollbarEnabled(false);
        assertFalse(mListView.isSmoothScrollbarEnabled());

        mListView.setSmoothScrollbarEnabled(true);
        assertTrue(mListView.isSmoothScrollbarEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScrollingCacheEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isScrollingCacheEnabled",
            args = {}
        )
    })
    public void testAccessScrollingCacheEnabled() {
        mListView.setScrollingCacheEnabled(false);
        assertFalse(mListView.isScrollingCacheEnabled());

        mListView.setScrollingCacheEnabled(true);
        assertTrue(mListView.isScrollingCacheEnabled());
    }

    private void setAdapter() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.setAdapter(mAdapter_countries);
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    private void setListSelection(int index) throws Throwable {
        final int i = index;

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.setSelection(i);
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFocusedRect",
        args = {android.graphics.Rect.class}
    )
    public void testGetFocusedRect() throws Throwable {
        setAdapter();
        setListSelection(0);

        Rect r1 = new Rect();
        mListView.getFocusedRect(r1);

        assertEquals(0, r1.top);
        assertTrue(r1.bottom > 0);
        assertEquals(0, r1.left);
        assertTrue(r1.right > 0);

        setListSelection(7);
        Rect r2 = new Rect();
        mListView.getFocusedRect(r2);
        assertTrue(r2.top > 0);
        assertTrue(r2.bottom > 0);
        assertEquals(0, r2.left);
        assertTrue(r2.right > 0);

        assertTrue(r2.top > r1.top);
        assertEquals(r1.bottom - r1.top, r2.bottom - r2.top);
        assertEquals(r1.right, r2.right);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStackFromBottom",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isStackFromBottom",
            args = {}
        )
    })
    public void testAccessStackFromBottom() throws Throwable {
        setAdapter();

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.setStackFromBottom(false);
            }
        });
        assertFalse(mListView.isStackFromBottom());
        assertEquals(0, mListView.getSelectedItemPosition());

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.setStackFromBottom(true);
            }
        });

        mInstrumentation.waitForIdleSync();
        assertTrue(mListView.isStackFromBottom());
        // ensure last item in list is selected
        assertEquals(mCountryList.length-1, mListView.getSelectedItemPosition());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSelectedView",
        args = {android.graphics.Rect.class}
    )
    public void testAccessSelectedItem() throws Throwable {
        assertNull(mListView.getSelectedView());

        setAdapter();
        TextView tv = (TextView) mListView.getSelectedView();
        assertEquals(mCountryList[0], tv.getText().toString());

        setListSelection(5);
        tv = (TextView) mListView.getSelectedView();
        assertEquals(mCountryList[5], tv.getText().toString());

        setListSelection(2);
        tv = (TextView) mListView.getSelectedView();
        assertEquals(mCountryList[2], tv.getText().toString());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingTop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingLeft",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingBottom",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingRight",
            args = {}
        )
    })
    public void testAccessListPadding() throws Throwable {
        setAdapter();

        assertEquals(0, mListView.getListPaddingLeft());
        assertEquals(0, mListView.getListPaddingTop());
        assertEquals(0, mListView.getListPaddingRight());
        assertEquals(0, mListView.getListPaddingBottom());

        final Rect r = new Rect(0, 0, 40, 60);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.setPadding(r.left, r.top, r.right, r.bottom);
            }
        });
        mInstrumentation.waitForIdleSync();

        assertEquals(r.left, mListView.getListPaddingLeft());
        assertEquals(r.top, mListView.getListPaddingTop());
        assertEquals(r.right, mListView.getListPaddingRight());
        assertEquals(r.bottom, mListView.getListPaddingBottom());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelector",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelector",
            args = {android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelector",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawSelectorOnTop",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "verifyDrawable",
            args = {android.graphics.drawable.Drawable.class}
        )
    })
    public void testAccessSelector() throws Throwable {
        setAdapter();

        final Drawable d = mActivity.getResources().getDrawable(R.drawable.pass);
        mListView.setSelector(d);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertSame(d, mListView.getSelector());
        assertTrue(mListView.verifyDrawable(d));

        mListView.setSelector(R.drawable.failed);
        mListView.setDrawSelectorOnTop(true);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();

        Drawable drawable = mListView.getSelector();
        assertNotNull(drawable);
        Rect r = drawable.getBounds();

        TextView v = (TextView) mListView.getSelectedView();
        assertEquals(v.getLeft(), r.left);
        assertEquals(v.getTop(), r.top);
        assertEquals(v.getRight(), r.right);
        assertEquals(v.getBottom(), r.bottom);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScrollIndicators",
            args = {android.view.View.class, android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestLayout",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testSetScrollIndicators() throws Throwable {
        TextView tv1 = (TextView) mActivity.findViewById(R.id.headerview1);
        TextView tv2 = (TextView) mActivity.findViewById(R.id.footerview1);

        setAdapter();

        mListView.setScrollIndicators(tv1, tv2);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "showContextMenuForChild",
        args = {android.view.View.class}
    )
    public void testShowContextMenuForChild() throws Throwable {
        setAdapter();
        setListSelection(1);

        TextView tv = (TextView) mListView.getSelectedView();
        assertFalse(mListView.showContextMenuForChild(tv));

        // TODO: how to show the contextMenu success
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pointToPosition",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pointToRowId",
            args = {int.class, int.class}
        )
    })
    public void testPointToPosition() throws Throwable {
        assertEquals(AbsListView.INVALID_POSITION, mListView.pointToPosition(-1, -1));
        assertEquals(AbsListView.INVALID_ROW_ID, mListView.pointToRowId(-1, -1));

        setAdapter();

        View row = mListView.getChildAt(0);
        int rowHeight = row.getHeight();
        int middleOfSecondRow = rowHeight + rowHeight/2;

        int position1 = mListView.pointToPosition(0, 0);
        int position2 = mListView.pointToPosition(50, middleOfSecondRow);

        assertEquals(mAdapter_countries.getItemId(position1), mListView.pointToRowId(0, 0));
        assertEquals(mAdapter_countries.getItemId(position2),
                mListView.pointToRowId(50, middleOfSecondRow));

        assertTrue(position2 > position1);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "draw",
            args = {android.graphics.Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "dispatchDraw",
            args = {android.graphics.Canvas.class}
        )
    })
    public void testDraw() {
        Canvas canvas = new Canvas();
        mListView.draw(canvas);

        MyListView listView = new MyListView(mActivity);
        listView.dispatchDraw(canvas);

        // TODO: how to check
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRecyclerListener",
            args = {android.widget.AbsListView.RecyclerListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reclaimViews",
            args = {java.util.List.class}
        )
    })
    public void testSetRecyclerListener() throws Throwable {
        setAdapter();

        MockRecyclerListener recyclerListener = new MockRecyclerListener();
        List<View> views = new ArrayList<View>();

        assertNull(recyclerListener.getView());
        mListView.setRecyclerListener(recyclerListener);
        mListView.reclaimViews(views);

        assertTrue(views.size() > 0);
        assertNotNull(recyclerListener.getView());

        assertSame(recyclerListener.getView(), views.get(views.size() - 1));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCacheColorHint",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCacheColorHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSolidColor",
            args = {}
        )
    })
    public void testAccessCacheColorHint() {
        mListView.setCacheColorHint(Color.RED);
        assertEquals(Color.RED, mListView.getCacheColorHint());
        assertEquals(Color.RED, mListView.getSolidColor());

        mListView.setCacheColorHint(Color.LTGRAY);
        assertEquals(Color.LTGRAY, mListView.getCacheColorHint());
        assertEquals(Color.LTGRAY, mListView.getSolidColor());

        mListView.setCacheColorHint(Color.GRAY);
        assertEquals(Color.GRAY, mListView.getCacheColorHint());
        assertEquals(Color.GRAY, mListView.getSolidColor());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTranscriptMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTranscriptMode",
            args = {}
        )
    })
    public void testAccessTranscriptMode() {
        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        assertEquals(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL, mListView.getTranscriptMode());

        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        assertEquals(AbsListView.TRANSCRIPT_MODE_DISABLED, mListView.getTranscriptMode());

        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        assertEquals(AbsListView.TRANSCRIPT_MODE_NORMAL, mListView.getTranscriptMode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    public void testCheckLayoutParams() {
        MyListView listView = new MyListView(mActivity);

        AbsListView.LayoutParams param1 = new AbsListView.LayoutParams(10, 10);
        assertTrue(listView.checkLayoutParams(param1));

        ViewGroup.LayoutParams param2 = new ViewGroup.LayoutParams(10, 10);
        assertFalse(listView.checkLayoutParams(param2));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollRange",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeVerticalScrollExtent",
            args = {}
        )
    })
    public void testComputeVerticalScrollValues() {
        MyListView listView = new MyListView(mActivity);
        assertEquals(0, listView.computeVerticalScrollRange());
        assertEquals(0, listView.computeVerticalScrollOffset());
        assertEquals(0, listView.computeVerticalScrollExtent());

        listView.setAdapter(mAdapter_countries);
        listView.setSmoothScrollbarEnabled(false);
        assertEquals(mAdapter_countries.getCount(), listView.computeVerticalScrollRange());
        assertEquals(0, listView.computeVerticalScrollOffset());
        assertEquals(0, listView.computeVerticalScrollExtent());

        listView.setSmoothScrollbarEnabled(true);
        assertEquals(0, listView.computeVerticalScrollOffset());
        assertEquals(0, listView.computeVerticalScrollExtent());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "generateLayoutParams",
            args = {android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "generateLayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testGenerateLayoutParams() throws XmlPullParserException, IOException {
        ViewGroup.LayoutParams res = mListView.generateLayoutParams(mAttributeSet);
        assertNotNull(res);
        assertTrue(res instanceof AbsListView.LayoutParams);

        MyListView listView = new MyListView(mActivity);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                              ViewGroup.LayoutParams.WRAP_CONTENT);

        res = listView.generateLayoutParams(p);
        assertNotNull(res);
        assertTrue(res instanceof AbsListView.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, res.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, res.height);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "beforeTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "afterTextChanged",
            args = {android.text.Editable.class}
        )
    })
    public void testBeforeAndAfterTextChanged() {
        // The java doc says these two methods do nothing
        CharSequence str = "test";
        SpannableStringBuilder sb = new SpannableStringBuilder();

        mListView.beforeTextChanged(str, 0, str.length(), str.length());
        mListView.afterTextChanged(sb);

        // test callback
        MyListView listView = new MyListView(mActivity);
        TextView tv = new TextView(mActivity);

        assertFalse(listView.isBeforeTextChangedCalled());
        assertFalse(listView.isOnTextChangedCalled());
        assertFalse(listView.isAfterTextChangedCalled());

        tv.addTextChangedListener(listView);
        assertFalse(listView.isBeforeTextChangedCalled());
        assertFalse(listView.isOnTextChangedCalled());
        assertFalse(listView.isAfterTextChangedCalled());

        tv.setText("abc");
        assertTrue(listView.isBeforeTextChangedCalled());
        assertTrue(listView.isOnTextChangedCalled());
        assertTrue(listView.isAfterTextChangedCalled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addTouchables",
        args = {java.util.ArrayList.class}
    )
    public void testAddTouchables() throws Throwable {
        ArrayList<View> views = new ArrayList<View>();
        assertEquals(0, views.size());

        setAdapter();

        mListView.addTouchables(views);
        assertEquals(mListView.getChildCount(), views.size());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidateViews",
        args = {}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test, should be tested by" +
            " functional test.")
    public void testInvalidateViews() throws Throwable {
        TextView tv1 = (TextView) mActivity.findViewById(R.id.headerview1);
        TextView tv2 = (TextView) mActivity.findViewById(R.id.footerview1);

        setAdapter();

        mListView.setScrollIndicators(tv1, tv2);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mListView.invalidateViews();
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContextMenuInfo",
        args = {}
    )
    public void testGetContextMenuInfo() throws Throwable {
        final MyListView listView = new MyListView(mActivity, mAttributeSet);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.setContentView(listView);
                listView.setAdapter(mAdapter_countries);
                listView.setSelection(2);
            }
        });
        mInstrumentation.waitForIdleSync();

        TextView v = (TextView) listView.getSelectedView();
        assertNull(listView.getContextMenuInfo());

        MockOnItemLongClickListener listener = new MockOnItemLongClickListener();
        listView.setOnItemLongClickListener(listener);

        assertNull(listener.getParent());
        assertNull(listener.getView());
        assertEquals(0, listener.getPosition());
        assertEquals(0, listener.getID());

        TouchUtils.longClickView(this, v);

        assertSame(listView, listener.getParent());
        assertSame(v, listener.getView());
        assertEquals(2, listener.getPosition());
        assertEquals(listView.getItemIdAtPosition(2), listener.getID());

        ContextMenuInfo cmi = listView.getContextMenuInfo();
        assertNotNull(cmi);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTopFadingEdgeStrength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getBottomFadingEdgeStrength",
            args = {}
        )
    })
    public void testGetTopBottomFadingEdgeStrength() {
        MyListView listView = new MyListView(mActivity);

        assertEquals(0.0f, listView.getTopFadingEdgeStrength(), DELTA);
        assertEquals(0.0f, listView.getBottomFadingEdgeStrength(), DELTA);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "handleDataChanged",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testHandleDataChanged() {
        MyListView listView = new MyListView(mActivity, mAttributeSet, 0);
        listView.handleDataChanged();
        // TODO: how to check?
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasTextFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextFilterEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTextFilterEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFilterText",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearTextFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isInFilterMode",
            args = {}
        )
    })
    public void testSetFilterText() {
        MyListView listView = new MyListView(mActivity, mAttributeSet, 0);
        String filterText = "xyz";

        assertFalse(listView.isTextFilterEnabled());
        assertFalse(listView.hasTextFilter());
        assertFalse(listView.isInFilterMode());
        assertTrue(mListView.checkInputConnectionProxy(null));

        listView.setTextFilterEnabled(false);
        listView.setFilterText(filterText);
        assertFalse(listView.isTextFilterEnabled());
        assertFalse(listView.hasTextFilter());
        assertFalse(listView.isInFilterMode());

        listView.setTextFilterEnabled(true);
        listView.setFilterText(null);
        assertTrue(listView.isTextFilterEnabled());
        assertFalse(listView.hasTextFilter());
        assertFalse(listView.isInFilterMode());

        listView.setTextFilterEnabled(true);
        listView.setFilterText(filterText);
        assertTrue(listView.isTextFilterEnabled());
        assertTrue(listView.hasTextFilter());
        assertTrue(listView.isInFilterMode());

        listView.clearTextFilter();
        assertTrue(listView.isTextFilterEnabled());
        assertFalse(listView.hasTextFilter());
        assertFalse(listView.isInFilterMode());
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "layoutChildren",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testLayoutChildren() {
        /**
         * the subclass ListView and GridView override this method, so we can not test
         * this method.
         */
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "drawableStateChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onCreateInputConnection",
            args = {android.view.inputmethod.EditorInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onFilterComplete",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onGlobalLayout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onInterceptTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTouchModeChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onAttachedToWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onCreateDrawableState",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDetachedFromWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onFocusChanged",
            args = {boolean.class, int.class, android.graphics.Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onLayout",
            args = {boolean.class, int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSizeChanged",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "dispatchSetPressed",
            args = {boolean.class}
        )
    })
    public void testFoo() {
        /**
         * Do not test these APIs. They are callbacks which:
         *
         * 1. The callback machanism has been tested in super class
         * 2. The functionality is implmentation details, no need to test
         */
    }

    private static class MockOnScrollListener implements OnScrollListener {
        private AbsListView mView;
        private int mFirstVisibleItem;
        private int mVisibleItemCount;
        private int mTotalItemCount;
        private int mScrollState;

        private boolean mIsOnScrollCalled;
        private boolean mIsOnScrollStateChangedCalled;

        private MockOnScrollListener() {
            mView = null;
            mFirstVisibleItem = 0;
            mVisibleItemCount = 0;
            mTotalItemCount = 0;
            mScrollState = -1;

            mIsOnScrollCalled = false;
            mIsOnScrollStateChangedCalled = false;
        }

        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            mView = view;
            mFirstVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;
            mTotalItemCount = totalItemCount;
            mIsOnScrollCalled = true;
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mScrollState = scrollState;
            mIsOnScrollStateChangedCalled = true;
        }

        public AbsListView getView() {
            return mView;
        }

        public int getFirstVisibleItem() {
            return mFirstVisibleItem;
        }

        public int getVisibleItemCount() {
            return mVisibleItemCount;
        }

        public int getTotalItemCount() {
            return mTotalItemCount;
        }

        public int getScrollState() {
            return mScrollState;
        }

        public boolean isOnScrollCalled() {
            return mIsOnScrollCalled;
        }

        public boolean isOnScrollStateChangedCalled() {
            return mIsOnScrollStateChangedCalled;
        }

        public void reset() {
            mIsOnScrollCalled = false;
            mIsOnScrollStateChangedCalled = false;
        }
    }

    private static class MockRecyclerListener implements RecyclerListener {
        private View mView;

        private MockRecyclerListener() {
            mView = null;
        }

        public void onMovedToScrapHeap(View view) {
            mView = view;
        }

        public View getView() {
            return mView;
        }
    }

    private static class MockOnItemLongClickListener implements OnItemLongClickListener {
        private AdapterView<?> parent;
        private View view;
        private int position;
        private long id;

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            this.parent = parent;
            this.view = view;
            this.position = position;
            this.id = id;
            return false;
        }

        public AdapterView<?> getParent() {
            return parent;
        }

        public View getView() {
            return view;
        }

        public int getPosition() {
            return position;
        }

        public long getID() {
            return id;
        }
    }

    /**
     * MyListView for test
     */
    private static class MyListView extends ListView {
        public MyListView(Context context) {
            super(context);
        }

        public MyListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return super.checkLayoutParams(p);
        }

        @Override
        protected int computeVerticalScrollExtent() {
            return super.computeVerticalScrollExtent();
        }

        @Override
        protected int computeVerticalScrollOffset() {
            return super.computeVerticalScrollOffset();
        }

        @Override
        protected int computeVerticalScrollRange() {
            return super.computeVerticalScrollRange();
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
        }

        @Override
        protected void dispatchSetPressed(boolean pressed) {
            super.dispatchSetPressed(pressed);
        }

        @Override
        protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return super.generateLayoutParams(p);
        }

        @Override
        protected float getBottomFadingEdgeStrength() {
            return super.getBottomFadingEdgeStrength();
        }

        @Override
        protected ContextMenuInfo getContextMenuInfo() {
            return super.getContextMenuInfo();
        }

        @Override
        protected float getTopFadingEdgeStrength() {
            return super.getTopFadingEdgeStrength();
        }

        @Override
        protected void handleDataChanged() {
            super.handleDataChanged();
        }

        @Override
        protected boolean isInFilterMode() {
            return super.isInFilterMode();
        }

        private boolean mIsBeforeTextChangedCalled;
        private boolean mIsOnTextChangedCalled;
        private boolean mIsAfterTextChangedCalled;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mIsBeforeTextChangedCalled = true;
            super.beforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mIsOnTextChangedCalled = true;
            super.onTextChanged(s, start, before, count);
        }

        @Override
        public void afterTextChanged(Editable s) {
            mIsAfterTextChangedCalled = true;
            super.afterTextChanged(s);
        }

        public boolean isBeforeTextChangedCalled() {
            return mIsBeforeTextChangedCalled;
        }

        public boolean isOnTextChangedCalled() {
            return mIsOnTextChangedCalled;
        }

        public boolean isAfterTextChangedCalled() {
            return mIsAfterTextChangedCalled;
        }
    }
}
