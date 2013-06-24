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

import com.android.tradefed.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Stub implementation of CtsBuildHelper that returns empty files for all methods
 */
public class StubCtsBuildHelper extends CtsBuildHelper {

    public static StubCtsBuildHelper createStubHelper() throws IOException {
        File tmpFolder= FileUtil.createTempDir("ctstmp");
        File ctsinstall = new File(tmpFolder, CtsBuildHelper.CTS_DIR_NAME);
        ctsinstall.mkdirs();
        return new StubCtsBuildHelper(tmpFolder);
    }

    private StubCtsBuildHelper(File rootDir) throws FileNotFoundException {
        super(rootDir);
    }

    @Override
    public File getTestApp(String appFileName) throws FileNotFoundException {
        return new File("tmp");
    }

    @Override
    public File getTestPlansDir() throws FileNotFoundException {
        return new File("tmp");
    }

    @Override
    public File getTestCasesDir() {
        return new File("tmp");
    }
}
