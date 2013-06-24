/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.cts.tradefed.targetsetup;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Helper class for retrieving files from the CTS install.
 * <p/>
 * Encapsulates the filesystem layout of the CTS installation.
 */
public class CtsBuildHelper {

    static final String CTS_DIR_NAME = "android-cts";
    /** The root location of the extracted CTS package */
    private final File mRootDir;
    /** the {@link CTS_DIR_NAME} directory */
    private final File mCtsDir;

    /**
     * Creates a {@link CtsBuildHelper}.
     *
     * @param rootDir the parent folder that contains the "android-cts" directory and all its
     *            contents.
     * @throws FileNotFoundException if file does not exist
     */
    public CtsBuildHelper(File rootDir) throws FileNotFoundException {
        mRootDir = rootDir;
        mCtsDir = new File(mRootDir, CTS_DIR_NAME);
        if (!mCtsDir.exists()) {
            throw new FileNotFoundException(String.format(
                    "CTS install folder %s does not exist", mCtsDir.getAbsolutePath()));
        }
    }

    /**
     * @return a {@link File} representing the parent folder of the CTS installation
     */
    public File getRootDir() {
        return mRootDir;
    }

    /**
     * @return a {@link File} representing the "android-cts" folder of the CTS installation
     */
    public File getCtsDir() {
        return mCtsDir;
    }

    /**
     * @return a {@link File} representing the test application file with given name
     * @throws FileNotFoundException if file does not exist
     */
    public File getTestApp(String appFileName) throws FileNotFoundException {
        File apkFile = new File(getTestCasesDir(), appFileName);
        if (!apkFile.exists()) {
            throw new FileNotFoundException(String.format("CTS test app file %s does not exist",
                    apkFile.getAbsolutePath()));
        }
        return apkFile;
    }

    private File getRepositoryDir() {
        return new File(getCtsDir(), "repository");
    }

    /**
     * @return a {@link File} representing the results directory.
     */
    public File getResultsDir() {
        return new File(getRepositoryDir(), "results");
    }

    /**
     * @return a {@link File} representing the test cases directory
     * @throws FileNotFoundException if dir does not exist
     */
    public File getTestCasesDir() throws FileNotFoundException {
        File dir = new File(getRepositoryDir(), "testcases");
        if (!dir.exists()) {
            throw new FileNotFoundException(String.format(
                    "CTS test cases directory %s does not exist", dir.getAbsolutePath()));
        }
        return dir;
    }

    /**
     * @return a {@link File} representing the test plan directory
     * @throws FileNotFoundException if dir does not exist
     */
    public File getTestPlansDir() throws FileNotFoundException {
        File dir = new File(getRepositoryDir(), "plans");
        if (!dir.exists()) {
            throw new FileNotFoundException(String.format(
                    "CTS test plans directory %s does not exist", dir.getAbsolutePath()));
        }
        return dir;
    }
}
