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

import org.xmlpull.v1.XmlPullParser;

import com.android.cts.stub.R;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

/**
 * Test {@link ViewFlipper}.
 */
@TestTargetClass(ViewFlipper.class)
public class ViewFlipperTest extends ActivityInstrumentationTestCase<ViewFlipperStubActivity> {
    private Activity mActivity;

    public ViewFlipperTest() {
        super("com.android.cts.stub", ViewFlipperStubActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewFlipper}",
            method = "ViewFlipper",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewFlipper}",
            method = "ViewFlipper",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="ViewFlipper#ViewFlipper(Context, AttributeSet)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new ViewFlipper(mActivity);

        new ViewFlipper(mActivity, null);

        XmlPullParser parser = mActivity.getResources().getXml(R.layout.viewflipper_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new ViewFlipper(mActivity, attrs);

        try {
            new ViewFlipper(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewFlipper#setFlipInterval(int)}",
        method = "setFlipInterval",
        args = {int.class}
    )
    @ToBeFixed(bug="1386429", explanation="No getter and can't check indirectly")
    public void testSetFlipInterval() {
        ViewFlipper viewFlipper = new ViewFlipper(mActivity);
        viewFlipper.setFlipInterval(0);
        viewFlipper.setFlipInterval(-1);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startFlipping",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopFlipping",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFlipping",
            args = {}
        )
    })
    public void testViewFlipper() {
        ViewFlipper viewFlipper = (ViewFlipper) mActivity.findViewById(R.id.viewflipper_test);

        final int FLIP_INTERVAL = 1000;
        TextView iv1 = (TextView) mActivity.findViewById(R.id.viewflipper_textview1);
        TextView iv2 = (TextView) mActivity.findViewById(R.id.viewflipper_textview2);

        assertFalse(viewFlipper.isFlipping());
        assertSame(iv1, viewFlipper.getCurrentView());

        viewFlipper.startFlipping();
        assertTrue(viewFlipper.isFlipping());
        assertSame(iv1, viewFlipper.getCurrentView());
        assertEquals(View.VISIBLE, iv1.getVisibility());
        assertEquals(View.GONE, iv2.getVisibility());

        // wait for a longer time to make sure the view flipping is completed.
        waitForViewFlipping(FLIP_INTERVAL + 200);
        assertSame(iv2, viewFlipper.getCurrentView());
        assertEquals(View.GONE, iv1.getVisibility());
        assertEquals(View.VISIBLE, iv2.getVisibility());

        waitForViewFlipping(FLIP_INTERVAL + 200);
        assertSame(iv1, viewFlipper.getCurrentView());
        assertEquals(View.VISIBLE, iv1.getVisibility());
        assertEquals(View.GONE, iv2.getVisibility());

        viewFlipper.stopFlipping();
        assertFalse(viewFlipper.isFlipping());
    }

    private void waitForViewFlipping(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
