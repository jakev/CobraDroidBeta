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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.Xml;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.cts.stub.R;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

/**
 * Test {@link ImageView}.
 */
@TestTargetClass(ImageView.class)
public class ImageViewTest extends ActivityInstrumentationTestCase<ImageViewStubActivity> {
    private ImageView mImageView;
    private Activity mActivity;

    public ImageViewTest() {
        super("com.android.cts.stub", ImageViewStubActivity.class);
    }

    /**
     * Find the ImageView specified by id.
     *
     * @param id the id
     * @return the ImageView
     */
    private ImageView findImageViewById(int id) {
        return (ImageView) mActivity.findViewById(id);
    }

    private void createSampleImage(File imagefile, int resid) {
        InputStream source = null;
        OutputStream target = null;

        try {
            source = mActivity.getResources().openRawResource(resid);
            target = new FileOutputStream(imagefile);

            byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                target.write(buffer, 0, len);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (target != null) {
                    target.close();
                }
            } catch (IOException _) {
                // Ignore the IOException.
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mImageView = null;
        mActivity = getActivity();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ImageView}",
            method = "ImageView",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ImageView}",
            method = "ImageView",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link ImageView}",
            method = "ImageView",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "ImageView#ImageView(Context, AttributeSet) and " +
            "ImageView#ImageView(Context, AttributeSet, int)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new ImageView(mActivity);

        new ImageView(mActivity, null);

        new ImageView(mActivity, null, 0);

        XmlPullParser parser = mActivity.getResources().getXml(R.layout.imageview_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new ImageView(mActivity, attrs);
        new ImageView(mActivity, attrs, 0);

        try {
            new ImageView(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }

        try {
            new ImageView(null, null, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#invalidateDrawable(Drawable)}",
        method = "invalidateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    @ToBeFixed(bug="1400249", explanation="It will be tested by functional test.")
    public void testInvalidateDrawable() {
        ImageView imageView = new ImageView(mActivity);
        imageView.invalidateDrawable(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setAdjustViewBounds(boolean)}",
        method = "setAdjustViewBounds",
        args = {boolean.class}
    )
    public void testSetAdjustViewBounds() {
        ImageView imageView = new ImageView(mActivity);
        imageView.setScaleType(ScaleType.FIT_XY);

        imageView.setAdjustViewBounds(false);
        assertEquals(ScaleType.FIT_XY, imageView.getScaleType());

        imageView.setAdjustViewBounds(true);
        assertEquals(ScaleType.FIT_CENTER, imageView.getScaleType());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setMaxWidth(int)}",
        method = "setMaxWidth",
        args = {int.class}
    )
    @ToBeFixed(bug="1400249", explanation="It will be tested by functional test.")
    public void testSetMaxWidth() {
        ImageView imageView = new ImageView(mActivity);
        imageView.setMaxWidth(120);
        imageView.setMaxWidth(-1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setMaxHeight(int)}",
        method = "setMaxHeight",
        args = {int.class}
    )
    @ToBeFixed(bug="1400249", explanation="It will be tested by functional test.")
    public void testSetMaxHeight() {
        ImageView imageView = new ImageView(mActivity);
        imageView.setMaxHeight(120);
        imageView.setMaxHeight(-1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#getDrawable()}",
        method = "getDrawable",
        args = {}
    )
    public void testGetDrawable() {
        final ImageView imageView = new ImageView(mActivity);
        final PaintDrawable drawable1 = new PaintDrawable();
        final PaintDrawable drawable2 = new PaintDrawable();

        assertNull(imageView.getDrawable());

        imageView.setImageDrawable(drawable1);
        assertEquals(drawable1, imageView.getDrawable());
        assertNotSame(drawable2, imageView.getDrawable());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageResource(int)}",
        method = "setImageResource",
        args = {int.class}
    )
    @UiThreadTest
    public void testSetImageResource() {
        mImageView = findImageViewById(R.id.imageview);
        mImageView.setImageResource(-1);
        assertNull(mImageView.getDrawable());

        mImageView.setImageResource(R.drawable.testimage);
        assertTrue(mImageView.isLayoutRequested());
        assertNotNull(mImageView.getDrawable());
        Drawable drawable = mActivity.getResources().getDrawable(R.drawable.testimage);
        BitmapDrawable testimageBitmap = (BitmapDrawable) drawable;
        Drawable imageViewDrawable = mImageView.getDrawable();
        BitmapDrawable imageViewBitmap = (BitmapDrawable) imageViewDrawable;
        WidgetTestUtils.assertEquals(testimageBitmap.getBitmap(), imageViewBitmap.getBitmap());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageURI(Uri)}",
        method = "setImageURI",
        args = {android.net.Uri.class}
    )
    @UiThreadTest
    public void testSetImageURI() {
        mImageView = findImageViewById(R.id.imageview);
        mImageView.setImageURI(null);
        assertNull(mImageView.getDrawable());

        File dbDir = getInstrumentation().getTargetContext().getDir("tests",
                Context.MODE_PRIVATE);
        File imagefile = new File(dbDir, "tempimage.jpg");
        if (imagefile.exists()) {
            imagefile.delete();
        }
        createSampleImage(imagefile, R.raw.testimage);
        final String path = imagefile.getPath();
        mImageView.setImageURI(Uri.parse(path));
        assertTrue(mImageView.isLayoutRequested());
        assertNotNull(mImageView.getDrawable());

        Drawable imageViewDrawable = mImageView.getDrawable();
        BitmapDrawable imageViewBitmap = (BitmapDrawable) imageViewDrawable;
        Bitmap.Config viewConfig = imageViewBitmap.getBitmap().getConfig();
        Bitmap testimageBitmap = WidgetTestUtils.getUnscaledAndDitheredBitmap(
                mActivity.getResources(), R.raw.testimage, viewConfig);

        WidgetTestUtils.assertEquals(testimageBitmap, imageViewBitmap.getBitmap());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageDrawable(Drawable)}",
        method = "setImageDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    @UiThreadTest
    public void testSetImageDrawable() {
        mImageView = findImageViewById(R.id.imageview);

        mImageView.setImageDrawable(null);
        assertNull(mImageView.getDrawable());

        final Drawable drawable = mActivity.getResources().getDrawable(R.drawable.testimage);
        mImageView.setImageDrawable(drawable);
        assertTrue(mImageView.isLayoutRequested());
        assertNotNull(mImageView.getDrawable());
        BitmapDrawable testimageBitmap = (BitmapDrawable) drawable;
        Drawable imageViewDrawable = mImageView.getDrawable();
        BitmapDrawable imageViewBitmap = (BitmapDrawable) imageViewDrawable;
        WidgetTestUtils.assertEquals(testimageBitmap.getBitmap(), imageViewBitmap.getBitmap());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageBitmap(Bitmap)}",
        method = "setImageBitmap",
        args = {android.graphics.Bitmap.class}
    )
    @UiThreadTest
    public void testSetImageBitmap() {
        mImageView = findImageViewById(R.id.imageview);

        mImageView.setImageBitmap(null);
        // A BitmapDrawable is always created for the ImageView.
        assertNotNull(mImageView.getDrawable());

        final Bitmap bitmap =
            BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.testimage);
        mImageView.setImageBitmap(bitmap);
        assertTrue(mImageView.isLayoutRequested());
        assertNotNull(mImageView.getDrawable());
        Drawable imageViewDrawable = mImageView.getDrawable();
        BitmapDrawable imageViewBitmap = (BitmapDrawable) imageViewDrawable;
        WidgetTestUtils.assertEquals(bitmap, imageViewBitmap.getBitmap());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageState(int[], boolean)}",
        method = "setImageState",
        args = {int[].class, boolean.class}
    )
    public void testSetImageState() {
        mImageView = new ImageView(mActivity);
        int[] state = new int[8];
        mImageView.setImageState(state, false);
        assertSame(state, mImageView.onCreateDrawableState(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setSelected(boolean)}",
        method = "setSelected",
        args = {boolean.class}
    )
    public void testSetSelected() {
        mImageView = new ImageView(mActivity);
        assertFalse(mImageView.isSelected());

        mImageView.setSelected(true);
        assertTrue(mImageView.isSelected());

        mImageView.setSelected(false);
        assertFalse(mImageView.isSelected());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setImageLevel(int)}",
        method = "setImageLevel",
        args = {int.class}
    )
    public void testSetImageLevel() {
        PaintDrawable drawable = new PaintDrawable();
        drawable.setLevel(0);

        ImageView imageView = new ImageView(mActivity);
        imageView.setImageDrawable(drawable);
        imageView.setImageLevel(1);
        assertEquals(1, drawable.getLevel());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScaleType",
            args = {android.widget.ImageView.ScaleType.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScaleType",
            args = {}
        )
    })
    public void testAccessScaleType() {
        final ImageView imageView = new ImageView(mActivity);

        try {
            imageView.setScaleType(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        assertNotNull(imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.CENTER);
        assertEquals(ImageView.ScaleType.CENTER, imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        assertEquals(ImageView.ScaleType.MATRIX, imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        assertEquals(ImageView.ScaleType.FIT_START, imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.FIT_END);
        assertEquals(ImageView.ScaleType.FIT_END, imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.getScaleType());

        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        assertEquals(ImageView.ScaleType.CENTER_INSIDE, imageView.getScaleType());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setImageMatrix",
            args = {android.graphics.Matrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getImageMatrix",
            args = {}
        )
    })
    public void testAccessImageMatrix() {
        final ImageView imageView = new ImageView(mActivity);

        imageView.setImageMatrix(null);
        assertNotNull(imageView.getImageMatrix());

        final Matrix matrix = new Matrix();
        imageView.setImageMatrix(matrix);
        assertEquals(matrix, imageView.getImageMatrix());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#getBaseline()}",
        method = "getBaseline",
        args = {}
    )
    public void testGetBaseline() {
        final ImageView imageView = new ImageView(mActivity);
        assertEquals(-1, imageView.getBaseline());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setColorFilter(int, Mode)}",
        method = "setColorFilter",
        args = {int.class, android.graphics.PorterDuff.Mode.class}
    )
    public void testSetColorFilter1() {
        MockDrawable drawable = new MockDrawable();

        ImageView imageView = new ImageView(mActivity);
        imageView.setImageDrawable(drawable);
        imageView.setColorFilter(null);
        assertNull(drawable.getColorFilter());

        imageView.setColorFilter(0, PorterDuff.Mode.CLEAR);
        assertNotNull(drawable.getColorFilter());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#clearColorFilter()}",
        method = "clearColorFilter",
        args = {}
    )
    public void testClearColorFilter() {
        MockDrawable drawable = new MockDrawable();
        ColorFilter cf = new ColorFilter();

        ImageView imageView = new ImageView(mActivity);
        imageView.setImageDrawable(drawable);
        imageView.setColorFilter(cf);

        imageView.clearColorFilter();
        assertNull(drawable.getColorFilter());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setColorFilter(ColorFilter)}",
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    public void testSetColorFilter2() {
        MockDrawable drawable = new MockDrawable();

        ImageView imageView = new ImageView(mActivity);
        imageView.setImageDrawable(drawable);
        imageView.setColorFilter(null);
        assertNull(drawable.getColorFilter());

        ColorFilter cf = new ColorFilter();
        imageView.setColorFilter(cf);
        assertSame(cf, drawable.getColorFilter());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setAlpha(int)}",
        method = "setAlpha",
        args = {int.class}
    )
    public void testSetAlpha() {
        MockDrawable drawable = new MockDrawable();

        ImageView imageView = new ImageView(mActivity);
        imageView.setImageDrawable(drawable);
        imageView.setAlpha(0);
        assertEquals(0, drawable.getAlpha());

        imageView.setAlpha(255);
        assertEquals(255, drawable.getAlpha());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#drawableStateChanged()}",
        method = "drawableStateChanged",
        args = {}
    )
    public void testDrawableStateChanged() {
        MockImageView mockImageView = new MockImageView(mActivity);
        MockDrawable drawable = new MockDrawable();

        assertSame(StateSet.WILD_CARD, drawable.getState());
        mockImageView.setImageDrawable(drawable);
        mockImageView.drawableStateChanged();
        assertSame(mockImageView.getDrawableState(), drawable.getState());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#onCreateDrawableState(int)}",
        method = "onCreateDrawableState",
        args = {int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "IndexOutOfBoundsException is not expected.")
    public void testOnCreateDrawableState() {
        MockImageView mockImageView = new MockImageView(mActivity);

        assertEquals(MockImageView.getEnabledStateSet(), mockImageView.onCreateDrawableState(0));

        int[] expected = new int[]{1, 2, 3};
        mockImageView.setImageState(expected, false);
        assertSame(expected, mockImageView.onCreateDrawableState(1));

        mockImageView.setImageState(expected, true);
        try {
            mockImageView.onCreateDrawableState(-1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#onDraw(Canvas)}",
        method = "onDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testOnDraw() {
        MockImageView mockImageView = new MockImageView(mActivity);
        MockDrawable drawable = new MockDrawable();
        mockImageView.setImageDrawable(drawable);
        mockImageView.onDraw(new Canvas());
        assertTrue(drawable.hasDrawCalled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#onMeasure(int, int)}",
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
        mImageView = findImageViewById(R.id.imageview);
        mImageView.measure(200, 150);
        assertTrue(mImageView.getMeasuredWidth() <= 200);
        assertTrue(mImageView.getMeasuredHeight() <= 150);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#onSetAlpha(int)}",
        method = "onSetAlpha",
        args = {int.class}
    )
    public void testOnSetAlpha() {
        MockImageView mockImageView = new MockImageView(mActivity);
        assertTrue(mockImageView.onSetAlpha(0));
        MockDrawable drawable = new MockDrawable();
        mockImageView.setBackgroundDrawable(drawable);
        assertFalse(mockImageView.onSetAlpha(0));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#setFrame(int, int, int, int)}",
        method = "setFrame",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testSetFrame() {
        MockImageView mockImageView = new MockImageView(mActivity);
        assertFalse(mockImageView.hasOnSizeChangedCalled());
        assertTrue(mockImageView.setFrame(5, 10, 100, 200));
        assertEquals(5, mockImageView.getLeft());
        assertEquals(10, mockImageView.getTop());
        assertEquals(100, mockImageView.getRight());
        assertEquals(200, mockImageView.getBottom());
        assertTrue(mockImageView.hasOnSizeChangedCalled());

        mockImageView.reset();
        assertFalse(mockImageView.setFrame(5, 10, 100, 200));
        assertFalse(mockImageView.hasOnSizeChangedCalled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ImageView#verifyDrawable(Drawable)}",
        method = "verifyDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testVerifyDrawable() {
        MockImageView mockImageView = new MockImageView(mActivity);
        MockDrawable drawable = new MockDrawable();
        mockImageView.setImageDrawable(drawable);
        MockDrawable bgdrawable = new MockDrawable();
        mockImageView.setBackgroundDrawable(bgdrawable);
        assertFalse(mockImageView.verifyDrawable(null));
        assertFalse(mockImageView.verifyDrawable(new MockDrawable()));
        assertTrue(mockImageView.verifyDrawable(drawable));
        assertTrue(mockImageView.verifyDrawable(bgdrawable));
    }

    private static class MockImageView extends ImageView {
        private boolean mOnSizeChangedCalled = false;

        public boolean hasOnSizeChangedCalled() {
            return mOnSizeChangedCalled;
        }

        public void reset() {
            mOnSizeChangedCalled = false;
        }

        public MockImageView(Context context) {
            super(context);
        }

        public MockImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MockImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public static int[] getEnabledStateSet() {
            return ENABLED_STATE_SET;
        }

        public static int[] getPressedEnabledStateSet() {
            return PRESSED_ENABLED_STATE_SET;
        }
        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        @Override
        protected boolean onSetAlpha(int alpha) {
            return super.onSetAlpha(alpha);
        }
        @Override
        protected boolean setFrame(int l, int t, int r, int b) {
            return super.setFrame(l, t, r, b);
        }
        @Override
        protected boolean verifyDrawable(Drawable dr) {
            return super.verifyDrawable(dr);
        }

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mOnSizeChangedCalled = true;
        }
    }

    private class MockDrawable extends Drawable {
        private ColorFilter mColorFilter;
        private boolean mDrawCalled = false;
        private int mAlpha;

        public boolean hasDrawCalled() {
            return mDrawCalled;
        }

        public ColorFilter getColorFilter() {
            return mColorFilter;
        }

        @Override
        public void draw(Canvas canvas) {
            mDrawCalled = true;
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }

        public int getAlpha() {
            return mAlpha;
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mColorFilter = cf;
        }

        @Override
        public int getOpacity() {
            return 0;
        }

        public boolean isStateful() {
            return true;
        }
    }
}
