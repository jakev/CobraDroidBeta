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

package android.content.res.cts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(AssetFileDescriptor.AutoCloseInputStream.class)
public class AssetFileDescriptor_AutoCloseInputStreamTest extends AndroidTestCase {
    private static final int FILE_END = -1;
    private static final int FILE_LENGTH = 10;
    private static final String FILE_NAME = "testAssertFileDescriptorAutoCloseInputStream";
    private static final byte[] FILE_DATA = new byte[] {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
            };
    private static final int OFFSET = 0;
    private static final int READ_LENGTH = 1;

    private File mFile;
    private AssetFileDescriptor mAssetFileDes;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFile = new File(getContext().getFilesDir(), FILE_NAME);
        mFile.createNewFile();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // As {@link AssetFileDescripter#createOutputStream()}
        // and {@link AssetFileDescripter#createInputStream()} doc,
        // the input and output stream will be auto closed when the AssetFileDescriptor closed.
        // Here is no need to close AutoCloseInputStream, as AssetFileDescriptor will do it for us.
        if (mAssetFileDes != null) {
            mAssetFileDes.close();
        }
        getContext().deleteFile(FILE_NAME);
    }

    /*
     * Test AutoInputStream life circle.
     * Test point:
     * 1. Test AutoInputStream read what we write into file.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AssetFileDescriptor.AutoCloseInputStream",
            args = {AssetFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "read",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "read",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "read",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "mark",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "markSupported",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "skip",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "available",
            args = {}
        )
    })
    public void testInputStream() throws IOException {
        initAssetFileDescriptor();
        FileOutputStream outputStream = null;
        outputStream = mAssetFileDes.createOutputStream();
        outputStream.write(FILE_DATA);
        mAssetFileDes.close();

        initAssetFileDescriptor();
        AssetFileDescriptor.AutoCloseInputStream inputStream = null;
        inputStream = new AssetFileDescriptor.AutoCloseInputStream(mAssetFileDes);
        assertEquals(FILE_LENGTH, inputStream.available());
        assertEquals(FILE_DATA[0], inputStream.read());
        byte[] readFromFile = new byte[2];
        int readBytes = inputStream.read(readFromFile);
        assertTrue(FILE_END != readBytes);
        while (readBytes < readFromFile.length) {
            readFromFile[readBytes] = (byte) inputStream.read();
            readBytes++;
        }
        assertEquals(FILE_DATA[1], readFromFile[0]);
        assertEquals(FILE_DATA[2], readFromFile[1]);
        inputStream.mark(FILE_LENGTH);
        assertEquals(5, inputStream.skip(5));
        readBytes = inputStream.read(readFromFile, OFFSET, READ_LENGTH);
        while (readBytes < READ_LENGTH) {
            readFromFile[readBytes] = (byte) inputStream.read();
            readBytes++;
        }
        assertEquals(FILE_DATA[8], readFromFile[0]);
        assertEquals(FILE_END, inputStream.read());
        inputStream.reset();
        assertEquals(FILE_END, inputStream.read(readFromFile));
        assertEquals(FILE_END, inputStream.read(readFromFile, 0, 1));
        assertFalse(inputStream.markSupported());
    }

    private void initAssetFileDescriptor() throws FileNotFoundException {
        ParcelFileDescriptor fd =
            ParcelFileDescriptor.open(mFile, ParcelFileDescriptor.MODE_READ_WRITE);
        mAssetFileDes = new AssetFileDescriptor(fd, 0, FILE_LENGTH);
    }
}
