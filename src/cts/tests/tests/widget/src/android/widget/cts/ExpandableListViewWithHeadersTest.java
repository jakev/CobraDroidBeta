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

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import android.widget.ExpandableListView;
import android.widget.cts.util.ListUtil;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(ExpandableListView.class)
public class ExpandableListViewWithHeadersTest extends
        ActivityInstrumentationTestCase2<ExpandableListWithHeaders> {
    private ExpandableListView mExpandableListView;
    private ListUtil mListUtil;

    public ExpandableListViewWithHeadersTest() {
        super("com.android.cts.stub", ExpandableListWithHeaders.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mExpandableListView = getActivity().getExpandableListView();
        mListUtil = new ListUtil(mExpandableListView, getInstrumentation());
    }

    @MediumTest
    public void testPreconditions() {
        assertNotNull(mExpandableListView);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#expandGroup(int)}",
        method = "expandGroup",
        args = {int.class}
    )
    @MediumTest
    public void testExpandOnFirstPosition() {
        // Should be a header, and hence the first group should NOT have expanded
        mListUtil.arrowScrollToSelectedPosition(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertFalse(mExpandableListView.isGroupExpanded(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ExpandableListView#expandGroup(int)}",
        method = "expandGroup",
        args = {int.class}
    )
    @LargeTest
    public void testExpandOnFirstGroup() {
        mListUtil.arrowScrollToSelectedPosition(getActivity().getNumOfHeadersAndFooters());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        assertTrue(mExpandableListView.isGroupExpanded(0));
    }
}
