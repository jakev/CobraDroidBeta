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

import com.android.tradefed.config.Option;
import com.android.tradefed.targetsetup.FolderBuildInfo;
import com.android.tradefed.targetsetup.IBuildInfo;
import com.android.tradefed.targetsetup.IBuildProvider;
import com.android.tradefed.targetsetup.IFolderBuildInfo;
import com.android.tradefed.targetsetup.TargetSetupError;

import java.io.File;

/**
 * A simple {@link IBuildProvider} that uses a pre-existing CTS install.
 */
public class CtsBuildProvider implements IBuildProvider {

    @Option(name="cts-install-path", description="the path to the cts installation to use")
    private File mCtsRootDir;

    /**
     * {@inheritDoc}
     */
    public IBuildInfo getBuild() throws TargetSetupError {
        if (mCtsRootDir == null) {
            throw new IllegalArgumentException("Missing --cts-install-path");
        }
        IFolderBuildInfo ctsBuild = new FolderBuildInfo(0, "cts", "cts");
        ctsBuild.setRootDir(mCtsRootDir);
        return ctsBuild;
    }

    /**
     * {@inheritDoc}
     */
    public void buildNotTested(IBuildInfo info) {
        // ignore
    }
}
