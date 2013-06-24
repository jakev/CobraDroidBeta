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

package android.view.cts;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.test.AndroidTestCase;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.android.internal.util.XmlUtils;
import com.android.cts.stub.R;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(ViewGroup.MarginLayoutParams.class)
public class ViewGroup_MarginLayoutParamsTest extends AndroidTestCase {

    private ViewGroup.MarginLayoutParams mMarginLayoutParams;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMarginLayoutParams = null;
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test MarginLayoutParams constructor",
            method = "ViewGroup.MarginLayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test MarginLayoutParams constructor",
            method = "ViewGroup.MarginLayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test MarginLayoutParams constructor",
            method = "ViewGroup.MarginLayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test MarginLayoutParams constructor",
            method = "ViewGroup.MarginLayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    public void testConstructor() {
        mMarginLayoutParams = null;
        // new the MarginLayoutParams instance
        XmlResourceParser p = mContext.getResources().getLayout(
                R.layout.viewgroup_margin_layout);
        try {
            XmlUtils.beginDocument(p, "LinearLayout");
        } catch (Exception e) {
            fail("Fail in preparing AttibuteSet.");
        }
        mMarginLayoutParams = new ViewGroup.MarginLayoutParams(mContext, p);
        assertNotNull(mMarginLayoutParams);

        mMarginLayoutParams = null;
        // new the MarginLayoutParams instance
        mMarginLayoutParams = new ViewGroup.MarginLayoutParams(320, 480);
        assertNotNull(mMarginLayoutParams);

        mMarginLayoutParams = null;
        // new the MarginLayoutParams instance
        MarginLayoutParams temp = new ViewGroup.MarginLayoutParams(320, 480);
        mMarginLayoutParams = new ViewGroup.MarginLayoutParams(temp);
        assertNotNull(mMarginLayoutParams);

        mMarginLayoutParams = null;
        // new the MarginLayoutParams instance
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(320, 480);
        mMarginLayoutParams = new ViewGroup.MarginLayoutParams(lp);
        assertNotNull(mMarginLayoutParams);

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setMargins function",
        method = "setMargins",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testSetMargins() {

        // new the MarginLayoutParams instance
        mMarginLayoutParams = new ViewGroup.MarginLayoutParams(320, 480);
        mMarginLayoutParams.setMargins(20, 30, 120, 140);
        assertEquals(20, mMarginLayoutParams.leftMargin);
        assertEquals(30, mMarginLayoutParams.topMargin);
        assertEquals(120, mMarginLayoutParams.rightMargin);
        assertEquals(140, mMarginLayoutParams.bottomMargin);
    }

}
