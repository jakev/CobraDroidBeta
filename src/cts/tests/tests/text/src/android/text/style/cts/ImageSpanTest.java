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

package android.text.style.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.widget.cts.WidgetTestUtils;

@TestTargetClass(ImageSpan.class)
public class ImageSpanTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.Bitmap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.Bitmap.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, android.net.Uri.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, int.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "some constructors miss javadoc")
    public void testConstructor() {
        int width = 80;
        int height = 120;
        int[] color = new int[width * height];
        Bitmap b = Bitmap.createBitmap(color, width, height, Bitmap.Config.RGB_565);

        new ImageSpan(b);
        new ImageSpan(b, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(b, DynamicDrawableSpan.ALIGN_BASELINE);

        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        new ImageSpan(d);
        new ImageSpan(d, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(d, DynamicDrawableSpan.ALIGN_BASELINE);

        new ImageSpan(d, "cts test.");
        new ImageSpan(d, "cts test.", DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(d, "cts test.", DynamicDrawableSpan.ALIGN_BASELINE);

        new ImageSpan(mContext, Uri.parse("content://user/a/b"));
        new ImageSpan(mContext, Uri.parse("content://user/a/b"),
                DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(mContext, Uri.parse("content://user/a/b"),
                DynamicDrawableSpan.ALIGN_BASELINE);

        new ImageSpan(mContext, R.drawable.pass);
        new ImageSpan(mContext, R.drawable.pass, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(mContext, R.drawable.pass, DynamicDrawableSpan.ALIGN_BASELINE);

        new ImageSpan((Bitmap) null);
        new ImageSpan((Drawable) null);
        new ImageSpan((Drawable) null, (String) null);
        new ImageSpan((Context) null, -1);
        new ImageSpan((Bitmap) null, -1);
        new ImageSpan((Drawable) null, -1);
        new ImageSpan((Drawable) null, (String) null, -1);
        new ImageSpan((Context) null, -1, -1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSource().",
        method = "getSource",
        args = {}
    )
    public void testGetSource() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);

        ImageSpan imageSpan = new ImageSpan(d);
        assertNull(imageSpan.getSource());

        String source = "cts test.";
        imageSpan = new ImageSpan(d, source);
        assertEquals(source, imageSpan.getSource());

        source = "content://user/a/b";
        imageSpan = new ImageSpan(mContext, Uri.parse(source));
        assertEquals(source, imageSpan.getSource());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDrawable().",
        method = "getDrawable",
        args = {}
    )
    public void testGetDrawable() {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.pass);

        ImageSpan imageSpan = new ImageSpan(drawable);
        assertSame(drawable, imageSpan.getDrawable());

        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        imageSpan = new ImageSpan(mContext, R.drawable.pass);
        BitmapDrawable resultDrawable = (BitmapDrawable) imageSpan.getDrawable();
        WidgetTestUtils.assertEquals(bitmapDrawable.getBitmap(), resultDrawable.getBitmap());

        imageSpan = new ImageSpan(mContext, Uri.parse("unknown uri."));
        assertNull(imageSpan.getDrawable());

        imageSpan = new ImageSpan((Context) null, -1);
        assertNull(imageSpan.getDrawable());
    }
}
