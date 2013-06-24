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

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.test.AndroidTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Camera.class)
public class CameraTest extends AndroidTestCase {
    private Camera mCamera;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCamera = new Camera();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Camera",
        args = {}
    )
    public void testCamera(){
        new Camera();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "save",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testRestore() {
        // we cannot get the state changed because it was a native method
        mCamera.save();
        mCamera.restore();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "translate",
            args = {float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testTranslate() {
        Matrix m1 = new Matrix();
        preCompare(m1);

        mCamera.translate(10.0f, 28.0f, 2008.0f);
        Matrix m2 = new Matrix();
        mCamera.getMatrix(m2);
        assertFalse(m1.equals(m2));

        float[] f = new float[9];
        m2.getValues(f);
        assertEquals(0.22291021f, f[0]);
        assertEquals(0.0f, f[1]);
        assertEquals(2.2291021f, f[2]);
        assertEquals(0.0f, f[3]);
        assertEquals(0.22291021f, f[4]);
        assertEquals(-6.241486f, f[5]);
        assertEquals(0.0f, f[6]);
        assertEquals(0.0f, f[7]);
        assertEquals(1.0f, f[8]);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rotateX",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testRotateX() {
        Matrix m1 = new Matrix();
        preCompare(m1);

        mCamera.rotateX(90.0f);
        Matrix m2 = new Matrix();
        mCamera.getMatrix(m2);
        assertFalse(m1.equals(m2));

        float[] f = new float[9];
        m2.getValues(f);
        assertEquals(1.0f, f[0]);
        assertEquals(0.0f, f[1]);
        assertEquals(0.0f, f[2]);
        assertEquals(0.0f, f[3]);
        assertEquals(0.0f, f[4]);
        assertEquals(0.0f, f[5]);
        assertEquals(0.0f, f[6]);
        assertEquals(-0.0017361111f, f[7]);
        assertEquals(1.0f, f[8]);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rotateY",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testRotateY() {
        Matrix m1 = new Matrix();
        preCompare(m1);

        mCamera.rotateY(90.0f);
        Matrix m2 = new Matrix();
        mCamera.getMatrix(m2);
        assertFalse(m1.equals(m2));

        float[] f = new float[9];
        m2.getValues(f);
        assertEquals(0.0f, f[0]);
        assertEquals(0.0f, f[1]);
        assertEquals(0.0f, f[2]);
        assertEquals(0.0f, f[3]);
        assertEquals(1.0f, f[4]);
        assertEquals(0.0f, f[5]);
        assertEquals(0.0017361111f, f[6]);
        assertEquals(0.0f, f[7]);
        assertEquals(1.0f, f[8]);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rotateZ",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testRotateZ() {
        Matrix m1 = new Matrix();
        preCompare(m1);

        mCamera.rotateZ(90.0f);
        Matrix m2 = new Matrix();
        mCamera.getMatrix(m2);
        assertFalse(m1.equals(m2));

        float[] f = new float[9];
        m2.getValues(f);
        assertEquals(0.0f, f[0]);
        assertEquals(1.0f, f[1]);
        assertEquals(0.0f, f[2]);
        assertEquals(-1.0f, f[3]);
        assertEquals(0.0f, f[4]);
        assertEquals(0.0f, f[5]);
        assertEquals(0.0f, f[6]);
        assertEquals(0.0f, f[7]);
        assertEquals(1.0f, f[8]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "applyToCanvas",
        args = {android.graphics.Canvas.class}
    )
    public void testApplyToCanvas() {
        Canvas c1 = new Canvas();
        mCamera.applyToCanvas(c1);

        Canvas c2 = new Canvas();
        Matrix m = new Matrix();
        mCamera.getMatrix(m);
        c2.concat(m);

        assertTrue(c1.getMatrix().equals(c2.getMatrix()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dotWithNormal",
        args = {float.class, float.class, float.class}
    )
    public void testDotWithNormal() {
        assertEquals(0.0792f, mCamera.dotWithNormal(0.1f, 0.28f, 0.2008f));
    }

    private void preCompare(Matrix m) {
        mCamera.getMatrix(m);
        float[] f = new float[9];
        m.getValues(f);
        assertEquals(1.0f, f[0]);
        assertEquals(0.0f, f[1]);
        assertEquals(0.0f, f[2]);
        assertEquals(0.0f, f[3]);
        assertEquals(1.0f, f[4]);
        assertEquals(0.0f, f[5]);
        assertEquals(0.0f, f[6]);
        assertEquals(0.0f, f[7]);
        assertEquals(1.0f, f[8]);
    }

}
