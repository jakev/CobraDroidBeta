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

package com.android.cts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * Host lock to make sure just one CTS host is running.
 */
public class HostLock {
    private static FileOutputStream mFileOs;
    private static FileLock mLock;
    private static File mFile;

    /**
     * Lock the host.
     *
     * @return If succeed in locking the host, return true; else , return false.
     */
    public static boolean lock() {
        try {
            String tmpdir = System.getProperty("java.io.tmpdir");
            mFile = new File(tmpdir + File.separator + "ctsLockFile.txt");
            mFileOs = new FileOutputStream(mFile);
            mLock = mFileOs.getChannel().tryLock();
            if (mLock != null) {
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e1) {
            return false;
        }catch (IOException e1) {
            return false;
        }
    }

    /**
     * Release the host lock.
     */
    public static void release() {
        try {
            if (mLock != null) {
                mLock.release();
            }

            if (mFileOs != null) {
                mFileOs.close();
            }
            // On systems with permissions, it's possible for this lock file
            // (depending on the default permissions set) to lock other users 
            // out from using CTS on the same host.  So remove the file and 
            // play nice with others.
            mFile.delete();
        } catch (IOException e) {
        }
    }
}
