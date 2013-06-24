/*
 * Copyright (C) 2009 The Android Open Source Project
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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;

import junit.framework.TestCase;

@TestTargetClass(RadialGradient.class)
public class RadialGradientTest extends TestCase {
    private static final int SIZE = 200;
    private static final int RADIUS = 80;
    private static final int CENTER = SIZE / 2;

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RadialGradient",
        args = {float.class, float.class, float.class, int[].class, float[].class,
                TileMode.class}
    )
    public void testRadialGradient() {
        final int[] colors = { Color.BLUE, Color.GREEN, Color.RED };
        final float[] positions = { 0f, 0.3f, 1f };
        int tolerance = (int)(0xFF / (0.3f * RADIUS) * 2);
        RadialGradient rg = new RadialGradient(CENTER, CENTER, RADIUS, colors, positions,
                Shader.TileMode.CLAMP);
        Bitmap b = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint p = new Paint();
        p.setShader(rg);
        canvas.drawRect(0, 0, SIZE, SIZE, p);
        checkPixels(b, colors, positions, tolerance);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RadialGradient",
        args = {float.class, float.class, float.class, int.class, int.class, TileMode.class}
    )
    public void testRadialGradientWithColor() {
        final int[] colors = { Color.BLUE, Color.GREEN };
        final float[] positions = { 0f, 1f };
        int tolerance = (int)(0xFF / RADIUS * 2);
        RadialGradient rg = new RadialGradient(CENTER, CENTER, RADIUS, colors[0], colors[1],
                Shader.TileMode.CLAMP);
        Bitmap b = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint p = new Paint();
        p.setShader(rg);
        canvas.drawRect(0, 0, SIZE, SIZE, p);
        checkPixels(b, colors, positions, tolerance);
    }

    private void checkPixels(Bitmap bitmap, int[] colors, float[] positions, int tolerance) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = dist(x, y, CENTER, CENTER) / RADIUS;
                int idx;
                int color;
                for (idx = 0; idx < positions.length; idx++) {
                    if (positions[idx] > dist) {
                        break;
                    }
                }
                if (idx == 0) {
                    // use start color
                    color = colors[0];
                } else if (idx == positions.length) {
                    // clamp to end color
                    color = colors[positions.length - 1];
                } else {
                    // linear interpolation
                    int i1 = idx - 1; // index of next lower color and position
                    int i2 = idx; // index of next higher color and position
                    double delta = (dist - positions[i1]) / (positions[i2] - positions[i1]);
                    int alpha = (int) ((1d - delta) * Color.alpha(colors[i1]) +
                            delta * Color.alpha(colors[i2]));
                    int red = (int) ((1d - delta) * Color.red(colors[i1]) +
                            delta * Color.red(colors[i2]));
                    int green = (int) ((1d - delta) * Color.green(colors[i1]) +
                            delta * Color.green(colors[i2]));
                    int blue = (int) ((1d - delta) * Color.blue(colors[i1]) +
                            delta * Color.blue(colors[i2]));
                    color = Color.argb(alpha, red, green, blue);
                }
                int pixel = bitmap.getPixel(x, y);

                assertEquals(Color.alpha(color), Color.alpha(pixel), tolerance);
                assertEquals(Color.red(color), Color.red(pixel), tolerance);
                assertEquals(Color.green(color), Color.green(pixel), tolerance);
                assertEquals(Color.blue(color), Color.blue(pixel), tolerance);
            }
        }
    }

    /**
     * Calculate distance between two points.
     */
    private double dist(int x1, int y1, int x2, int y2) {
        int distX = x1 - x2;
        int distY = y1 - y2;
        return Math.sqrt(distX * distX + distY * distY);
    }

}
