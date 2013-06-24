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

import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RelativeLayout;

/**
 * Test {@link RelativeLayout.LayoutParams}.
 */
@TestTargetClass(RelativeLayout.LayoutParams.class)
public class RelativeLayout_LayoutParamsTest extends
        ActivityInstrumentationTestCase2<RelativeLayoutStubActivity> {

    public RelativeLayout_LayoutParamsTest() {
        super("com.android.cts.stub", RelativeLayoutStubActivity.class);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout.LayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    public void testConstructor() {
        try {
            new RelativeLayout.LayoutParams(getActivity(), null);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 300);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);

        RelativeLayout.LayoutParams tempLayoutParams = layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(tempLayoutParams);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);

        MarginLayoutParams tempMarginLayoutParams = new MarginLayoutParams(400, 500);
        layoutParams = new RelativeLayout.LayoutParams(tempMarginLayoutParams);
        assertEquals(400, layoutParams.width);
        assertEquals(500, layoutParams.height);

        // Test RelativeLayout.Params which generated from the xml file.
        int rules[];
        RelativeLayoutStubActivity activity = getActivity();

        // test attributes used in RelativeLayout.
        RelativeLayout relativeLayout = (RelativeLayout) activity.findViewById(
                R.id.relative_sublayout_attrs);

        // view1, centered within its parent.
        // TEST: android:layout_centerInParent
        View view1 = activity.findViewById(R.id.relative_view1);
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view1);
        ViewAsserts.assertVerticalCenterAligned(relativeLayout, view1);
        layoutParams = (RelativeLayout.LayoutParams) (view1.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.CENTER_IN_PARENT]);

        // view2, below view1 and has same left position with view1.
        // TEST: android:layout_below; android:layout_alignLeft
        View view2 = activity.findViewById(R.id.relative_view2);
        ViewAsserts.assertLeftAligned(view1, view2);
        assertEquals(view1.getBottom(), view2.getTop());
        layoutParams = (RelativeLayout.LayoutParams) (view2.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view1, rules[RelativeLayout.BELOW]);
        assertEquals(R.id.relative_view1, rules[RelativeLayout.ALIGN_LEFT]);

        // view3, has same top position with view1 and same bottom position with view2,
        // and on the right of view1.1.
        // TEST: android:layout_alignTop; android:layout_alignBottom; android:layout_toRightOf
        View view3 = activity.findViewById(R.id.relative_view3);
        ViewAsserts.assertTopAligned(view1, view3);
        ViewAsserts.assertBottomAligned(view2, view3);
        assertEquals(view1.getRight(), view3.getLeft());
        layoutParams = (RelativeLayout.LayoutParams) (view3.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view1, rules[RelativeLayout.ALIGN_TOP]);
        assertEquals(R.id.relative_view2, rules[RelativeLayout.ALIGN_BOTTOM]);
        assertEquals(R.id.relative_view1, rules[RelativeLayout.RIGHT_OF]);

        // view4, has same right position with view3 and above view3.
        // TEST: android:layout_alignRight; android:layout_above
        View view4 = activity.findViewById(R.id.relative_view4);
        ViewAsserts.assertRightAligned(view3, view4);
        assertEquals(view3.getTop(), view4.getBottom());
        layoutParams = (RelativeLayout.LayoutParams) (view4.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view3, rules[RelativeLayout.ALIGN_RIGHT]);
        assertEquals(R.id.relative_view3, rules[RelativeLayout.ABOVE]);

        // view5 goes on the left-bottom.
        // TEST: android:layout_alignParentBottom; android:layout_alignParentLeft
        View view5 = activity.findViewById(R.id.relative_view5);
        ViewAsserts.assertLeftAligned(relativeLayout, view5);
        ViewAsserts.assertBottomAligned(relativeLayout, view5);
        layoutParams = (RelativeLayout.LayoutParams) (view5.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_PARENT_BOTTOM]);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_PARENT_LEFT]);

        // view6 goes on the top-right.
        // TEST: android:layout_alignParentTop; android:layout_alignParentRight
        View view6 = activity.findViewById(R.id.relative_view6);
        ViewAsserts.assertTopAligned(relativeLayout, view6);
        ViewAsserts.assertRightAligned(relativeLayout, view6);
        layoutParams = (RelativeLayout.LayoutParams) (view6.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_PARENT_TOP]);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_PARENT_RIGHT]);

        // view7, has same baseline with view6 and centered horizontally within its parent.
        // TEST: android:layout_alignBaseline; android:layout_centerHorizontal
        View view7 = activity.findViewById(R.id.relative_view7);
        ViewAsserts.assertBaselineAligned(view6, view7);
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view7);
        layoutParams = (RelativeLayout.LayoutParams) (view7.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view6, rules[RelativeLayout.ALIGN_BASELINE]);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.CENTER_HORIZONTAL]);

        // view8, centered vertically within its parent and on the left of view1.
        // TEST: android:layout_toLeftOf; android:layout_centerVertical
        View view8 = activity.findViewById(R.id.relative_view8);
        ViewAsserts.assertVerticalCenterAligned(relativeLayout, view8);
        assertEquals(view1.getLeft(), view8.getRight());
        layoutParams = (RelativeLayout.LayoutParams) (view8.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view1, rules[RelativeLayout.LEFT_OF]);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.CENTER_VERTICAL]);

        // view9, has same top and bottom position with view3 and same left position with its parent
        // TEST: android:layout_alignLeft; android:layout_alignTop; android:layout_alignBottom;
        // android:layout_alignWithParentIfMissing
        View view9 = activity.findViewById(R.id.relative_view9);
        ViewAsserts.assertTopAligned(view3, view9);
        ViewAsserts.assertBottomAligned(view3, view9);
        ViewAsserts.assertLeftAligned(relativeLayout, view9);
        layoutParams = (RelativeLayout.LayoutParams) (view9.getLayoutParams());
        rules = layoutParams.getRules();
        assertEquals(R.id.gravity_bottom, rules[RelativeLayout.ALIGN_LEFT]);
        assertEquals(R.id.relative_view3, rules[RelativeLayout.ALIGN_TOP]);
        assertEquals(R.id.relative_view3, rules[RelativeLayout.ALIGN_BOTTOM]);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRule",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRules",
            args = {}
        )
    })
     @ToBeFixed(bug = "1695243", explanation =
             "the javadoc for getRules() or addRule(int) is incomplete." +
             "1. not clear what is actual value for 'false' mentioned in javadoc of getRules()." +
             "2. not clear what '-' means in '- for false' in javadoc of addRules()." +
             "3. not clear what is supposed to happen when verb is exceptional." +
             "4. not clear what is supposed to happen if verb is must refer to another sibling.")
    public void testAccessRule1() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 300);
        int rules[]= layoutParams.getRules();

        // normal value
        assertEquals(0, rules[RelativeLayout.CENTER_IN_PARENT]);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.CENTER_IN_PARENT]);

        // issue 1695243
        // not clear what is supposed to happen if verb is must refer to another sibling.
        assertEquals(0, rules[RelativeLayout.ALIGN_LEFT]);
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT);
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_LEFT]);

        // exceptional value
        try {
            layoutParams.addRule(-1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // issue 1695243, not clear what is supposed to happen when verb is exceptional.
        }

        try {
            layoutParams.addRule(Integer.MAX_VALUE);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // issue 1695243, not clear what is supposed to happen when verb is exceptional.
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRule",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRules",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addRule(int, int) is incomplete." +
            "1. not clear what is supposed to happen when verb is exceptional.")
    public void testAccessRule2() {
        int rules[];
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 300);

        // normal value
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.relative_view1);
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view1, rules[RelativeLayout.ALIGN_LEFT]);

        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, 0);
        rules = layoutParams.getRules();
        assertEquals(0, rules[RelativeLayout.ALIGN_LEFT]);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        rules = layoutParams.getRules();
        assertEquals(0, rules[RelativeLayout.ALIGN_PARENT_LEFT]);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rules = layoutParams.getRules();
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_PARENT_LEFT]);

        // exceptional value
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
        rules = layoutParams.getRules();
        assertEquals(RelativeLayout.TRUE, rules[RelativeLayout.ALIGN_LEFT]);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.relative_view1);
        rules = layoutParams.getRules();
        assertEquals(R.id.relative_view1, rules[RelativeLayout.ALIGN_PARENT_LEFT]);

        try {
            layoutParams.addRule(-1, 0);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // issue 1695243, not clear what is supposed to happen when verb is exceptional.
        }

        try {
            layoutParams.addRule(Integer.MAX_VALUE, 0);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // issue 1695243, not clear what is supposed to happen when verb is exceptional.
        }

        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, Integer.MAX_VALUE);
        rules = layoutParams.getRules();
        assertEquals(Integer.MAX_VALUE, rules[RelativeLayout.ALIGN_LEFT]);

        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, Integer.MIN_VALUE);
        rules = layoutParams.getRules();
        assertEquals(Integer.MIN_VALUE, rules[RelativeLayout.ALIGN_LEFT]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "debug",
        args = {java.lang.String.class}
    )
    public void testDebug() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 300);
        assertNotNull(layoutParams.debug("test: "));
    }
}
