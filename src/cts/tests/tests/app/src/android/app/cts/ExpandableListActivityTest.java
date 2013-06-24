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

package android.app.cts;

import android.app.ExpandableListActivity;
import android.content.ComponentName;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(ExpandableListActivity.class)
public class ExpandableListActivityTest extends ActivityTestsBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntent.putExtra("component", new ComponentName(getContext(),
                ExpandableListTestActivity.class));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelectedChild",
            args = {int.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelectedGroup",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListAdapter",
            args = {}
        )
    })
    public void testSelect() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_SELECT);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        )
    })
    public void testView() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_VIEW);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListAdapter",
            args = {android.widget.ExpandableListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupExpand",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onGroupCollapse",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {android.view.ContextMenu.class, android.view.View.class,
                    android.view.ContextMenu.ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ExpandableListActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildClick",
            args = {android.widget.ExpandableListView.class, android.view.View.class,
                    int.class, int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpandableListAdapter",
            args = {}
        )
    })
    public void testCallback() {
        runLaunchpad(LaunchpadActivity.EXPANDLIST_CALLBACK);
    }
}