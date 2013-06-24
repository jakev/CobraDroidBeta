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

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Bitmap.CompressFormat.class)
public class Bitmap_CompressFormatTest extends AndroidTestCase{

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(CompressFormat.JPEG, CompressFormat.valueOf("JPEG"));
        assertEquals(CompressFormat.PNG, CompressFormat.valueOf("PNG"));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "compress",
            args = {android.graphics.Bitmap.CompressFormat.class, int.class,
                    java.io.OutputStream.class}
        )
    })
    public void testValues(){
        CompressFormat[] comFormat = CompressFormat.values();

        assertEquals(2, comFormat.length);
        assertEquals(CompressFormat.JPEG, comFormat[0]);
        assertEquals(CompressFormat.PNG, comFormat[1]);

        //CompressFormat is used as a argument here for all the methods that use it
        Bitmap b = Bitmap.createBitmap(10, 24, Config.ARGB_8888);
        assertTrue(b.compress(CompressFormat.JPEG, 24, new ByteArrayOutputStream()));
        assertTrue(b.compress(CompressFormat.PNG, 24, new ByteArrayOutputStream()));
    }
}
