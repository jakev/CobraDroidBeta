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

package android.graphics.cts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.cts.MockActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.cts.WidgetTestUtils;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Movie.class)
public class MovieTest extends ActivityInstrumentationTestCase2<MockActivity> {
    private Movie mMovie;
    private final int MOVIE = com.android.cts.stub.R.drawable.animated;

    public MovieTest() {
        super("com.android.cts.stub", MockActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMovie = getActivity().getResources().getMovie(MOVIE);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDraw1() {
        Canvas c = new Canvas();
        Paint p = new Paint();
        mMovie.draw(c, 100, 200, p);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, float.class, float.class}
    )
    public void testDraw2() {
        Canvas c = new Canvas();
        mMovie.draw(c, 100, 200);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeFile",
        args = {java.lang.String.class}
    )
    public void testDecodeFile() throws Exception {
        mMovie = null;
        File dbDir = getInstrumentation().getTargetContext().getDir("tests",
                Context.MODE_PRIVATE);
        File imagefile = new File(dbDir, "animated.gif");
        if (imagefile.exists()) {
            imagefile.delete();
        }
        writeSampleImage(imagefile);
        mMovie = Movie.decodeFile(imagefile.getPath());
        assertNotNull(mMovie);

        mMovie = null;
        mMovie = Movie.decodeFile("/no file path");
        assertNull(mMovie);
    }

    private void writeSampleImage(File imagefile) throws Exception {
        InputStream source = null;
        OutputStream target = null;

        try {
            source = getActivity().getResources().openRawResource(MOVIE);
            target = new FileOutputStream(imagefile);

            byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source
                    .read(buffer)) {
                target.write(buffer, 0, len);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
    }

    private byte[] inputStreamToBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
        return out.toByteArray();

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test decodeByteArray",
        method = "decodeByteArray",
        args = {byte[].class, int.class, int.class}
    )
    public void testDecodeByteArray() throws Exception {
        mMovie = null;
        InputStream is = getActivity().getResources().openRawResource(MOVIE);
        byte[] bytes = inputStreamToBytes(is);
        mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
        is.close();
        assertNotNull(mMovie);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test decodeStream method",
        method = "decodeStream",
        args = {java.io.InputStream.class}
    )
    public void testDecodeStream() {
        assertFalse(mMovie.isOpaque());
        mMovie = null;
        try {
            InputStream is = getActivity().getResources()
                    .openRawResource(MOVIE);
            mMovie = Movie.decodeStream(is);
            is.close();
        } catch (Exception e) {
            fail("shouldn't throw exception");
        }
        assertNotNull(mMovie);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test setTime method",
        method = "setTime",
        args = {int.class}
    )
    public void testSetTime() {
        assertTrue(mMovie.setTime(1000));
        assertFalse(mMovie.setTime(Integer.MAX_VALUE));
        assertFalse(mMovie.setTime(Integer.MIN_VALUE));
        assertFalse(mMovie.setTime(-1));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "duration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOpaque",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "height",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "width",
            args = {}
        )
    })
    public void testGetMovieProperties() {
        assertEquals(1000, mMovie.duration());
        assertFalse(mMovie.isOpaque());

        int expectedHeight = getActivity().getResources().getDrawable(MOVIE).getIntrinsicHeight();
        int scaledHeight = WidgetTestUtils.convertDipToPixels(getActivity(), mMovie.height());
        assertEquals(expectedHeight, scaledHeight);

        int expectedWidth = getActivity().getResources().getDrawable(MOVIE).getIntrinsicWidth();
        int scaledWidth = WidgetTestUtils.convertDipToPixels(getActivity(), mMovie.width());
        assertEquals(expectedWidth, scaledWidth);

    }
}