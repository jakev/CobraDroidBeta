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
import com.android.internal.view.menu.ContextMenuBuilder;

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
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.IOException;

/**
 * Test {@link Gallery}.
 */
@TestTargetClass(Gallery.class)
public class GalleryTest extends ActivityInstrumentationTestCase2<GalleryStubActivity>  {
    private Gallery mGallery;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private Context mContext;
    private final static float DELTA = 0.01f;

    public GalleryTest() {
        super("com.android.cts.stub", GalleryStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mGallery = (Gallery) mActivity.findViewById(R.id.gallery_test);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        new Gallery(mContext);

        new Gallery(mContext, null);

        new Gallery(mContext, null, 0);

        XmlPullParser parser = getActivity().getResources().getXml(R.layout.gallery_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Gallery(mContext, attrs);
        new Gallery(mContext, attrs, 0);

        try {
            new Gallery(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new Gallery(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new Gallery(null, null, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "setAnimationDuration",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "No getter and can't check indirectly. "
            + "It is hard to get transition animation to check if the duration is right.")
    public void testSetAnimationDuration() {
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSpacing",
        args = {int.class}
    )
    public void testSetSpacing() throws Throwable {
        setSpacingAndCheck(0);

        setSpacingAndCheck(5);

        setSpacingAndCheck(-1);
    }

    private void setSpacingAndCheck(final int spacing) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mGallery.setSpacing(spacing);
                mGallery.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();

        View v0 = mGallery.getChildAt(0);
        View v1 = mGallery.getChildAt(1);
        assertEquals(v0.getRight() + spacing, v1.getLeft());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setUnselectedAlpha",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChildStaticTransformation",
            args = {android.view.View.class, android.view.animation.Transformation.class}
        )
    })
    public void testSetUnselectedAlpha() {
        final MyGallery gallery = (MyGallery) mActivity.findViewById(R.id.gallery_test);

        checkUnselectedAlpha(gallery, 0.0f);

        checkUnselectedAlpha(gallery, 0.5f);
    }

    private void checkUnselectedAlpha(MyGallery gallery, float alpha) {
        final float DEFAULT_ALPHA = 1.0f;
        View v0 = gallery.getChildAt(0);
        View v1 = gallery.getChildAt(1);

        gallery.setUnselectedAlpha(alpha);
        Transformation t = new Transformation();
        gallery.getChildStaticTransformation(v0, t);
        // v0 is selected by default.
        assertEquals(DEFAULT_ALPHA, t.getAlpha(), DELTA);
        gallery.getChildStaticTransformation(v1, t);
        assertEquals(alpha, t.getAlpha(), DELTA);
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
        final int width = 320;
        final int height = 240;
        LayoutParams lp = new LayoutParams(width, height);
        MyGallery gallery = new MyGallery(mContext);
        LayoutParams layoutParams = gallery.generateLayoutParams(lp);
        assertEquals(width, layoutParams.width);
        assertEquals(height, layoutParams.height);

        XmlPullParser parser = getActivity().getResources().getXml(R.layout.gallery_test);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        AttributeSet attrs = Xml.asAttributeSet(parser);
        mGallery = new Gallery(mContext, attrs);

        layoutParams = mGallery.generateLayoutParams(attrs);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSingleTapUp",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onScroll",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onLongPress",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onShowPress",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyDown",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
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
        )
    })
    public void testFoo() {
        // Do not test these APIs. They are callbacks which:
        // 1. The callback machanism has been tested in super class
        // 2. The functionality is implmentation details, no need to test
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "showContextMenuForChild",
        args = {android.view.View.class}
    )
    public void testShowContextMenuForChild() {
        // how to check whether the context menu for child is showing.
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "showContextMenu",
        args = {}
    )
    public void testShowContextMenu() {
        // how to check whether the context menu is showing.
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchKeyEvent",
        args = {android.view.KeyEvent.class}
    )
    public void testDispatchKeyEvent() {
        mGallery = new Gallery(mContext);
        final KeyEvent validKeyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
        assertTrue(mGallery.dispatchKeyEvent(validKeyEvent));
        final long time = SystemClock.uptimeMillis();
        final KeyEvent invalidKeyEvent
                = new KeyEvent(time, time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A, 5);
        assertFalse(mGallery.dispatchKeyEvent(invalidKeyEvent));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setGravity",
        args = {int.class}
    )
    public void testSetGravity() throws Throwable {
        setGalleryGravity(Gravity.CENTER_HORIZONTAL);
        View v0 = mGallery.getChildAt(0);
        ViewAsserts.assertHorizontalCenterAligned(mGallery, v0);

        setGalleryGravity(Gravity.TOP);
        v0 = mGallery.getChildAt(0);
        ViewAsserts.assertTopAligned(mGallery, v0, mGallery.getPaddingTop());

        setGalleryGravity(Gravity.BOTTOM);
        v0 = mGallery.getChildAt(0);
        ViewAsserts.assertBottomAligned(mGallery, v0, mGallery.getPaddingBottom());
    }

    private void setGalleryGravity(final int gravity) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mGallery.setGravity(gravity);
                mGallery.invalidate();
                mGallery.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testCheckLayoutParams() {
        MyGallery gallery = new MyGallery(mContext);
        ViewGroup.LayoutParams p1 = new ViewGroup.LayoutParams(320, 480);
        assertFalse(gallery.checkLayoutParams(p1));

        Gallery.LayoutParams p2 = new Gallery.LayoutParams(320, 480);
        assertTrue(gallery.checkLayoutParams(p2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollExtent",
        args = {}
    )
    public void testComputeHorizontalScrollExtent() {
        MyGallery gallery = new MyGallery(mContext);

        // only one item is considered to be selected.
        assertEquals(1, gallery.computeHorizontalScrollExtent());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollOffset",
        args = {}
    )
    public void testComputeHorizontalScrollOffset() {
        MyGallery gallery = new MyGallery(mContext);
        assertEquals(AdapterView.INVALID_POSITION, gallery.computeHorizontalScrollOffset());
        gallery.setAdapter(new ImageAdapter(mActivity));

        // Current scroll position is the same as the selected position
        assertEquals(gallery.getSelectedItemPosition(), gallery.computeHorizontalScrollOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollRange",
        args = {}
    )
    public void testComputeHorizontalScrollRange() {
        MyGallery gallery = new MyGallery(mContext);
        ImageAdapter adapter = new ImageAdapter(mActivity);
        gallery.setAdapter(adapter);

        // Scroll range is the same as the item count
        assertEquals(adapter.getCount(), gallery.computeHorizontalScrollRange());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchSetPressed",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are not right, "
            + "dispatchSetPressed did not dispatch setPressed to "
            + "all of this View's children, but only the selected view")
    @UiThreadTest
    public void testDispatchSetPressed() {
        final MyGallery gallery = (MyGallery) getActivity().findViewById(R.id.gallery_test);

        gallery.setSelection(0);
        gallery.dispatchSetPressed(true);
        assertTrue(gallery.getSelectedView().isPressed());
        assertFalse(gallery.getChildAt(1).isPressed());

        gallery.dispatchSetPressed(false);
        assertFalse(gallery.getSelectedView().isPressed());
        assertFalse(gallery.getChildAt(1).isPressed());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "generateDefaultLayoutParams",
        args = {}
    )
    public void testGenerateDefaultLayoutParams() {
        MyGallery gallery = new MyGallery(mContext);
        ViewGroup.LayoutParams p = gallery.generateDefaultLayoutParams();
        assertNotNull(p);
        assertTrue(p instanceof Gallery.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, p.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, p.height);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChildDrawingOrder",
        args = {int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetChildDrawingOrder() {
        final MyGallery gallery = (MyGallery) getActivity().findViewById(R.id.gallery_test);

        int childCount = 3;
        int index = 2;
        assertEquals(gallery.getSelectedItemPosition(),
                gallery.getChildDrawingOrder(childCount, index));

        childCount = 5;
        index = 2;
        assertEquals(index + 1, gallery.getChildDrawingOrder(childCount, index));

        childCount = 5;
        index = 3;
        assertEquals(index + 1, gallery.getChildDrawingOrder(childCount, index));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContextMenuInfo",
        args = {}
    )
    public void testGetContextMenuInfo() {
        MockOnCreateContextMenuListener listener = new MockOnCreateContextMenuListener();
        MyGallery gallery = new MyGallery(mContext);
        gallery.setOnCreateContextMenuListener(listener);
        assertFalse(listener.hasCreatedContextMenu());
        gallery.createContextMenu(new ContextMenuBuilder(mContext));
        assertTrue(listener.hasCreatedContextMenu());
        assertSame(gallery.getContextMenuInfo(), listener.getContextMenuInfo());
    }

    private static class MockOnCreateContextMenuListener implements OnCreateContextMenuListener {
        private boolean hasCreatedContextMenu;
        private ContextMenuInfo mContextMenuInfo;

        public boolean hasCreatedContextMenu() {
            return hasCreatedContextMenu;
        }

        public ContextMenuInfo getContextMenuInfo() {
            return mContextMenuInfo;
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            hasCreatedContextMenu = true;
            mContextMenuInfo = menuInfo;
        }
    }

    private static class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);

            i.setImageResource(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(136, 88));

            return i;
        }

        private Context mContext;

        private Integer[] mImageIds = {
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
        };
    }

    private static class MockOnItemSelectedListener implements OnItemSelectedListener {
        private boolean mIsItemSelected;
        private boolean mNothingSelected;
        private int mItemSelectedCalledCount;

        public boolean isItemSelected() {
            return mIsItemSelected;
        }

        public boolean hasNothingSelected() {
            return mNothingSelected;
        }

        public int getItemSelectedCalledCount() {
            return mItemSelectedCalledCount;
        }

        public void reset() {
            mIsItemSelected = false;
            mNothingSelected = true;
            mItemSelectedCalledCount = 0;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mIsItemSelected = true;
            mItemSelectedCalledCount++;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            mNothingSelected = true;
        }
    }
}
