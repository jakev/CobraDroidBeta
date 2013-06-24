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

package android.graphics.drawable.shapes.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;

import junit.framework.TestCase;

@TestTargetClass(android.graphics.drawable.shapes.Shape.class)
public class ShapeTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resize",
            args = {float.class, float.class}
        )
    })
    public void testSize() {
        MockShape mockShape = new MockShape();
        assertFalse(mockShape.hasCalledOnResize());

        mockShape.resize(200f, 300f);
        assertEquals(200f, mockShape.getWidth());
        assertEquals(300f, mockShape.getHeight());
        assertTrue(mockShape.hasCalledOnResize());

        mockShape.resize(0f, 0f);
        assertEquals(0f, mockShape.getWidth());
        assertEquals(0f, mockShape.getHeight());

        mockShape.resize(Float.MAX_VALUE, Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, mockShape.getWidth());
        assertEquals(Float.MAX_VALUE, mockShape.getHeight());

        mockShape.resize(-1, -1);
        assertEquals(0f, mockShape.getWidth());
        assertEquals(0f, mockShape.getHeight());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onResize",
        args = {float.class, float.class}
    )
    public void testOnResize() {
        MockShape mockShape = new MockShape();
        assertFalse(mockShape.hasCalledOnResize());

        mockShape.resize(200f, 300f);
        assertTrue(mockShape.hasCalledOnResize());

        // size does not change
        mockShape.reset();
        mockShape.resize(200f, 300f);
        assertFalse(mockShape.hasCalledOnResize());

        // size changes
        mockShape.reset();
        mockShape.resize(100f, 200f);
        assertTrue(mockShape.hasCalledOnResize());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        Shape shape = new MockShape();
        shape.resize(100f, 200f);
        Shape clonedShape = shape.clone();
        assertEquals(100f, shape.getWidth());
        assertEquals(200f, shape.getHeight());

        assertNotSame(shape, clonedShape);
        assertEquals(shape.getWidth(), clonedShape.getWidth());
        assertEquals(shape.getHeight(), clonedShape.getHeight());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasAlpha",
        args = {}
    )
    public void testHasAlpha() {
        assertTrue(new MockShape().hasAlpha());
    }

    private static class MockShape extends Shape {
        private boolean mCalledOnResize = false;

        @Override
        public void draw(Canvas canvas, Paint paint) {
        }

        @Override
        protected void onResize(float width, float height) {
            super.onResize(width, height);
            mCalledOnResize = true;
        }

        public boolean hasCalledOnResize() {
            return mCalledOnResize;
        }

        public void reset() {
            mCalledOnResize = false;
        }
    }
}
