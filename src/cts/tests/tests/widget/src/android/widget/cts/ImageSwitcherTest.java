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

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@TestTargetClass(ImageSwitcher.class)
public class ImageSwitcherTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ImageSwitcher",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ImageSwitcher",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="ImageSwitcher#ImageSwitcher(Context, AttributeSet)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new ImageSwitcher(getContext());

        new ImageSwitcher(getContext(), null);

        XmlPullParser parser = getContext().getResources().getXml(R.layout.imageswitcher_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        assertNotNull(attrs);
        new ImageSwitcher(getContext(), attrs);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setImageResource(int)",
        method = "setImageResource",
        args = {int.class}
    )
    public void testSetImageResource() {
        // new the ImageSwitcher instance
        ImageSwitcher imageSwitcher = new ImageSwitcher(getContext());
        ImageView iv = new ImageView(getContext());
        imageSwitcher.addView(iv);
        ImageView iv1 = new ImageView(getContext());
        imageSwitcher.addView(iv1);

        assertSame(iv, imageSwitcher.getCurrentView());
        imageSwitcher.setImageResource(R.drawable.scenery);
        assertSame(iv1, imageSwitcher.getCurrentView());
        Resources resources = getContext().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.scenery);
        BitmapDrawable sceneryBitmap = (BitmapDrawable) drawable;
        BitmapDrawable currViewBitmap =
            (BitmapDrawable) ((ImageView) imageSwitcher.getCurrentView()).getDrawable();
        WidgetTestUtils.assertEquals(sceneryBitmap.getBitmap(), currViewBitmap.getBitmap());

        imageSwitcher.setImageResource(R.drawable.testimage);
        assertSame(iv, imageSwitcher.getCurrentView());
        drawable = resources.getDrawable(R.drawable.testimage);
        BitmapDrawable testimageBitmap = (BitmapDrawable) drawable;
        currViewBitmap =
            (BitmapDrawable) ((ImageView) imageSwitcher.getCurrentView()).getDrawable();
        WidgetTestUtils.assertEquals(testimageBitmap.getBitmap(), currViewBitmap.getBitmap());

        imageSwitcher.setImageResource(-1);
        assertNull(((ImageView) imageSwitcher.getCurrentView()).getDrawable());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setImageURI(Uri)",
        method = "setImageURI",
        args = {android.net.Uri.class}
    )
    public void testSetImageURI() {
        // new the ImageSwitcher instance
        ImageSwitcher imageSwitcher = new ImageSwitcher(getContext());
        ImageView iv = new ImageView(getContext());
        imageSwitcher.addView(iv);
        ImageView iv1 = new ImageView(getContext());
        imageSwitcher.addView(iv1);

        File dbDir = getContext().getDir("tests", Context.MODE_PRIVATE);
        File imagefile = new File(dbDir, "tempimage.jpg");
        if (imagefile.exists()) {
            imagefile.delete();
        }
        createSampleImage(imagefile, R.raw.testimage);

        assertSame(iv, imageSwitcher.getCurrentView());
        Uri uri = Uri.parse(imagefile.getPath());
        imageSwitcher.setImageURI(uri);
        assertSame(iv1, imageSwitcher.getCurrentView());

        BitmapDrawable currViewBitmap =
            (BitmapDrawable) ((ImageView) imageSwitcher.getCurrentView()).getDrawable();
        Bitmap testImageBitmap = WidgetTestUtils.getUnscaledAndDitheredBitmap(
                getContext().getResources(), R.raw.testimage,
                currViewBitmap.getBitmap().getConfig());
        WidgetTestUtils.assertEquals(testImageBitmap, currViewBitmap.getBitmap());

        createSampleImage(imagefile, R.raw.scenery);
        uri = Uri.parse(imagefile.getPath());
        imageSwitcher.setImageURI(uri);
        assertSame(iv, imageSwitcher.getCurrentView());
        Bitmap sceneryImageBitmap = WidgetTestUtils.getUnscaledAndDitheredBitmap(
                getContext().getResources(), R.raw.scenery,
                currViewBitmap.getBitmap().getConfig());
        currViewBitmap =
            (BitmapDrawable) ((ImageView) imageSwitcher.getCurrentView()).getDrawable();
        WidgetTestUtils.assertEquals(sceneryImageBitmap, currViewBitmap.getBitmap());

        imagefile.delete();

        imageSwitcher.setImageURI(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setImageDrawable(Drawable)",
        method = "setImageDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testSetImageDrawable() {
        ImageSwitcher imageSwitcher = new ImageSwitcher(getContext());
        ImageView iv = new ImageView(getContext());
        imageSwitcher.addView(iv);
        ImageView iv1 = new ImageView(getContext());
        imageSwitcher.addView(iv1);

        Resources resources = getContext().getResources();
        assertSame(iv, imageSwitcher.getCurrentView());
        Drawable drawable = resources.getDrawable(R.drawable.scenery);
        imageSwitcher.setImageDrawable(drawable);
        assertSame(iv1, imageSwitcher.getCurrentView());
        assertSame(drawable, ((ImageView) imageSwitcher.getCurrentView()).getDrawable());

        drawable = resources.getDrawable(R.drawable.testimage);
        imageSwitcher.setImageDrawable(drawable);
        assertSame(iv, imageSwitcher.getCurrentView());
        assertSame(drawable, ((ImageView) imageSwitcher.getCurrentView()).getDrawable());

        imageSwitcher.setImageDrawable(null);
    }

    private void createSampleImage(File imagefile, int resid) {
        InputStream source = null;
        OutputStream target = null;

        try {
            source = getContext().getResources().openRawResource(resid);
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
}
