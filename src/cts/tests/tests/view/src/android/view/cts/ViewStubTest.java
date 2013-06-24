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

import com.android.cts.stub.R;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.widget.LinearLayout;

/**
 * Test {@link ViewStub}.
 */
@TestTargetClass(ViewStub.class)
public class ViewStubTest extends ActivityInstrumentationTestCase<ViewStubStubActivity> {
    private Context mContext;
    private Activity mActivity;

    public ViewStubTest() {
        super("com.android.cts.stub", ViewStubStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getContext();
        mActivity = getActivity();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewStub}",
            method = "ViewStub",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewStub}",
            method = "ViewStub",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewStub}",
            method = "ViewStub",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ViewStub}",
            method = "ViewStub",
            args = {android.content.Context.class, int.class}
        )
    })
    public void testConstructor() {
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.viewstub_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        assertNotNull(attrs);

        new ViewStub(mContext);

        new ViewStub(mContext, 10);

        new ViewStub(mContext, attrs);

        new ViewStub(mContext, attrs, 30);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewStub#draw(Canvas)}",
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    public void testDraw() {
        ViewStub viewStub = new ViewStub(mContext);
        // if the function draw() does not throw any exception,
        // we think it is right, because it's an empty method.
        viewStub.draw(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewStub#setVisibility(int)}",
        method = "setVisibility",
        args = {int.class}
    )
    @UiThreadTest
    public void testSetVisibility() {
        final ViewStub viewStub1 = (ViewStub) mActivity.findViewById(R.id.viewstub);
        MockOnInflateListener listener = new MockOnInflateListener();
        viewStub1.setOnInflateListener(listener);
        assertFalse(listener.hasCalledOnInflate());
        assertNotNull(viewStub1.getParent());

        // set GONE
        viewStub1.setVisibility(View.GONE);
        assertEquals(View.GONE, viewStub1.getVisibility());
        // does not call inflate
        assertFalse(listener.hasCalledOnInflate());
        assertNotNull(viewStub1.getParent());

        // set VISIBLE
        viewStub1.setVisibility(View.VISIBLE);
        assertEquals(View.VISIBLE, viewStub1.getVisibility());
        //assure the inflate is called
        assertTrue(listener.hasCalledOnInflate());
        assertNull(viewStub1.getParent());

        // set INVISIBLE when parent is null
        final ViewStub viewStub2 = new ViewStub(mContext);
        assertNull(viewStub2.getParent());
        try {
            viewStub2.setVisibility(View.INVISIBLE);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertEquals(View.INVISIBLE, viewStub2.getVisibility());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutResource",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLayoutResource",
            args = {int.class}
        )
    })
    public void testAccessLayoutResource() {
        ViewStub viewStub = new ViewStub(mContext);

        viewStub.setLayoutResource(R.layout.viewstub_layout);
        assertEquals(R.layout.viewstub_layout, viewStub.getLayoutResource());

        viewStub.setLayoutResource(0);
        assertEquals(0, viewStub.getLayoutResource());

        viewStub.setLayoutResource(-1);
        assertEquals(-1, viewStub.getLayoutResource());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewStub#onMeasure(int, int)}",
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testViewStubHasNoDimensions() {
        ViewStub viewStub = new ViewStub(mContext);

        viewStub.forceLayout();
        viewStub.measure(200, 300);
        assertEquals(0, viewStub.getMeasuredWidth());
        assertEquals(0, viewStub.getMeasuredHeight());

        viewStub.measure(100, 200);
        assertEquals(0, viewStub.getMeasuredWidth());
        assertEquals(0, viewStub.getMeasuredHeight());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewStub#setOnInflateListener(OnInflateListener)}",
        method = "setOnInflateListener",
        args = {android.view.ViewStub.OnInflateListener.class}
    )
    @UiThreadTest
    public void testSetOnInflateListener() {
        final ViewStub viewStub = (ViewStub) mActivity.findViewById(R.id.viewstub);
        final MockOnInflateListener listener = new MockOnInflateListener();

        viewStub.setOnInflateListener(listener);
        assertFalse(listener.hasCalledOnInflate());
        viewStub.inflate();
        assertTrue(listener.hasCalledOnInflate());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnInflateListener",
        args = {android.view.ViewStub.OnInflateListener.class}
    )
    @UiThreadTest
    public void testSetOnInflateListenerError() {
        final ViewStub viewStub = (ViewStub) mActivity.findViewById(R.id.viewstub);

        viewStub.setOnInflateListener(null);
        try {
            viewStub.inflate();
        } catch (NullPointerException e) {
            fail("should not throw NullPointerException");
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link ViewStub#getInflatedId()} and {@link ViewStub#setInflatedId(int)}",
            method = "getInflatedId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link ViewStub#getInflatedId()} and {@link ViewStub#setInflatedId(int)}",
            method = "setInflatedId",
            args = {int.class}
        )
    })
    public void testAccessInflatedId() {
        ViewStub viewStub = new ViewStub(mContext);
        assertEquals(0, viewStub.getInflatedId());

        viewStub.setInflatedId(R.id.inflated_id);
        assertEquals(R.id.inflated_id, viewStub.getInflatedId());

        viewStub.setInflatedId(-1);
        assertEquals(-1, viewStub.getInflatedId());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ViewStub#inflate()}",
        method = "inflate",
        args = {}
    )
    @UiThreadTest
    public void testInflate() {
        final ViewStub viewStub = (ViewStub) mActivity.findViewById(R.id.viewstub);
        final ViewParent vsParent = viewStub.getParent();
        final MockOnInflateListener listener = new MockOnInflateListener();

        viewStub.setOnInflateListener(listener);
        assertFalse(listener.hasCalledOnInflate());
        assertNotNull(vsParent);

        View view = viewStub.inflate();
        assertNotNull(view);
        assertTrue(view instanceof LinearLayout);
        assertEquals(viewStub.getLayoutParams().width, view.getLayoutParams().width);
        assertEquals(viewStub.getLayoutParams().height, view.getLayoutParams().height);
        assertNull(viewStub.getParent());
        assertSame(vsParent, view.getParent());
        assertEquals(R.id.inflated_id, view.getId());
        assertTrue(listener.hasCalledOnInflate());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test abnormal condition of {@link ViewStub#inflate()}",
        method = "inflate",
        args = {}
    )
    @ToBeFixed(bug="", explanation="should add javadoc for ViewStub#inflate(): it will" +
            " throw IllegalArgumentException when resource is invalid and " +
            " throw IllegalStateException when parent is null")
    public void testInflateError() {
        final ViewStub viewStub = (ViewStub) mActivity.findViewById(R.id.viewstub);

        // mLayoutResource is 0
        viewStub.setLayoutResource(0);
        try {
            viewStub.inflate();
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // parent is null
        ViewStub stub = new ViewStub(mContext);
        assertNull(stub.getParent());
        try {
            stub.inflate();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    private class MockOnInflateListener implements OnInflateListener {
        private boolean mCalledOnInflate = false;

        public void onInflate(ViewStub stub, View inflated) {
            mCalledOnInflate = true;
        }

        public boolean hasCalledOnInflate() {
            return mCalledOnInflate;
        }

        public void reset() {
            mCalledOnInflate = false;
        }
    }
}
