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

package com.android.cts.appwithdata;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.test.AndroidTestCase;

/**
 * Test that will create private app data.
 *
 * This is not really a test per-say. Its just used as a hook so the test controller can trigger
 * the creation of private app data.
 */
public class CreatePrivateDataTest extends AndroidTestCase {

    /**
     * Name of private file to create.
     */
    private static final String PRIVATE_FILE_NAME = "private_file.txt";

    /**
     * Creates a file private to this app
     * @throws IOException if any error occurred when creating the file
     */
    public void testCreatePrivateData() throws IOException {
        FileOutputStream outputStream = getContext().openFileOutput(PRIVATE_FILE_NAME,
                Context.MODE_PRIVATE);
        outputStream.write("file contents".getBytes());
        outputStream.close();
        assertTrue(getContext().getFileStreamPath(PRIVATE_FILE_NAME).exists());
    }

    /**
     * Check to ensure the private file created in testCreatePrivateData does not exist.
     * Used to check that uninstall of an app deletes the app's data.
     */
    public void testEnsurePrivateDataNotExist() throws IOException {
        assertFalse(getContext().getFileStreamPath(PRIVATE_FILE_NAME).exists());
    }
}
