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

import android.database.DataSetObserver;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

/**
 * Test {@link BaseExpandableListAdapter}.
 */
@TestTargetClass(BaseExpandableListAdapter.class)
public class BaseExpandableListAdapterTest extends InstrumentationTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test areAllItemsEnabled(), this function always returns true.",
        method = "areAllItemsEnabled",
        args = {}
    )
    public void testAreAllItemsEnabled() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        assertTrue(adapter.areAllItemsEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getCombinedChildId(long, long) function.",
            method = "getCombinedChildId",
            args = {long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getCombinedChildId(long, long) function.",
            method = "getCombinedGroupId",
            args = {long.class}
        )
    })
    @ToBeFixed(bug = "1502158", explanation = "getCombinedChildId() always returns a group id, " +
            "it never returns a child id; because bit 0 always be 1; getCombinedGroupId() " +
            "always returns a child id, it never returns a group id; because bit 0 always be 0")
    public void testGetCombinedId() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();

        long childID = adapter.getCombinedChildId(10, 100);
        long groupID = adapter.getCombinedGroupId(10);

        // there should be no clash in group and child IDs
        assertTrue(childID != groupID);

        childID = adapter.getCombinedChildId(0, 0);
        groupID = adapter.getCombinedGroupId(0);
        assertTrue(childID != groupID);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isEmpty() function.",
        method = "isEmpty",
        args = {}
    )
    public void testIsEmpty() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        assertTrue(adapter.isEmpty());
        adapter.setGroupCount(10);
        assertFalse(adapter.isEmpty());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test notifyDataSetChanged() function.",
        method = "notifyDataSetChanged",
        args = {}
    )
    public void testNotifyDataSetChanged() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        MockDataSetObserver dataSetObserver = new MockDataSetObserver();
        adapter.registerDataSetObserver(dataSetObserver);

        assertFalse(dataSetObserver.hasCalledOnChanged());
        adapter.notifyDataSetChanged();
        assertTrue(dataSetObserver.hasCalledOnChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test notifyDataSetInvalidated() function.",
        method = "notifyDataSetInvalidated",
        args = {}
    )
    public void testNotifyDataSetInvalidated() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        MockDataSetObserver dataSetObserver = new MockDataSetObserver();
        adapter.registerDataSetObserver(dataSetObserver);

        assertFalse(dataSetObserver.hasCalledOnInvalidated());
        adapter.notifyDataSetInvalidated();
        assertTrue(dataSetObserver.hasCalledOnInvalidated());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onGroupCollapsed(int), this function is non-operation.",
        method = "onGroupCollapsed",
        args = {int.class}
    )
    public void testOnGroupCollapsed() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        // this function is non-operation.
        adapter.onGroupCollapsed(0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onGroupExpanded(int), this function is non-operation.",
        method = "onGroupExpanded",
        args = {int.class}
    )
    public void testOnGroupExpanded() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        // this function is non-operation.
        adapter.onGroupExpanded(0);
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
    public void testDataSetObserver() {
        MockBaseExpandableListAdapter adapter = new MockBaseExpandableListAdapter();
        MockDataSetObserver dataSetObserver = new MockDataSetObserver();
        adapter.registerDataSetObserver(dataSetObserver);

        assertFalse(dataSetObserver.hasCalledOnChanged());
        adapter.notifyDataSetChanged();
        assertTrue(dataSetObserver.hasCalledOnChanged());

        dataSetObserver.reset();
        assertFalse(dataSetObserver.hasCalledOnChanged());
        adapter.unregisterDataSetObserver(dataSetObserver);
        adapter.notifyDataSetChanged();
        assertFalse(dataSetObserver.hasCalledOnChanged());
    }

    private class MockDataSetObserver extends DataSetObserver {
        private boolean mCalledOnChanged = false;
        private boolean mCalledOnInvalidated = false;

        @Override
        public void onChanged() {
            super.onChanged();
            mCalledOnChanged = true;
        }

        public boolean hasCalledOnChanged() {
            return mCalledOnChanged;
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mCalledOnInvalidated = true;
        }

        public boolean hasCalledOnInvalidated() {
            return mCalledOnInvalidated;
        }

        public void reset() {
            mCalledOnChanged = false;
        }
    }

    private class MockBaseExpandableListAdapter extends BaseExpandableListAdapter {
        private int mGroupCount;

        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        public int getChildrenCount(int groupPosition) {
            return 0;
        }

        public Object getGroup(int groupPosition) {
            return null;
        }

        public int getGroupCount() {
            return mGroupCount;
        }

        public void setGroupCount(int count) {
            mGroupCount = count;
        }

        public long getGroupId(int groupPosition) {
            return 0;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            return null;
        }

        public boolean hasStableIds() {
            return false;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
