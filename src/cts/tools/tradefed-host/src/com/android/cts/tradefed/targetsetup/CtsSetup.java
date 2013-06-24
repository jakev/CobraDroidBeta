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

import com.android.cts.tradefed.testtype.PlanTest;
import com.android.tradefed.config.ConfigurationException;
import com.android.tradefed.config.IConfiguration;
import com.android.tradefed.config.IConfigurationReceiver;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.targetsetup.BuildError;
import com.android.tradefed.targetsetup.IBuildInfo;
import com.android.tradefed.targetsetup.IFolderBuildInfo;
import com.android.tradefed.targetsetup.ITargetPreparer;
import com.android.tradefed.targetsetup.TargetSetupError;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * A {@link ITargetPreparer} that sets up a device for CTS testing.
 * <p/>
 * All the actions performed in this class must work on a production device.
 */
public class CtsSetup implements ITargetPreparer, IConfigurationReceiver {

    private static final String RUNNER_APK_NAME = "android.core.tests.runner.apk";
    // TODO: read this from configuration file rather than hardcoding
    private static final String TEST_STUBS_APK = "CtsTestStubs.apk";

    private IConfiguration mConfiguration = null;

    /**
     * Factory method to create a {@link CtsBuildHelper}.
     * <p/>
     * Exposed for unit testing.
     */
    CtsBuildHelper createBuildHelper(File rootDir) throws FileNotFoundException {
        return new CtsBuildHelper(rootDir);
    }

    /**
     * {@inheritDoc}
     */
    public void setConfiguration(IConfiguration configuration) {
        mConfiguration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp(ITestDevice device, IBuildInfo buildInfo) throws TargetSetupError,
            BuildError, DeviceNotAvailableException {
        if (!(buildInfo instanceof IFolderBuildInfo)) {
            throw new IllegalArgumentException("Provided buildInfo is not a IFolderBuildInfo");
        }
        if (mConfiguration == null) {
            throw new IllegalStateException("setConfiguration() was not called before setUp");
        }
        IFolderBuildInfo ctsBuildInfo = (IFolderBuildInfo)buildInfo;
        try {
            CtsBuildHelper buildHelper = createBuildHelper(ctsBuildInfo.getRootDir());
            // pass necessary build information to the other config objects
            mConfiguration.injectOptionValue(PlanTest.TEST_CASES_DIR_OPTION,
                    buildHelper.getTestCasesDir().getAbsolutePath());
            mConfiguration.injectOptionValue(PlanTest.TEST_PLANS_DIR_OPTION,
                    buildHelper.getTestPlansDir().getAbsolutePath());
            installCtsPrereqs(device, buildHelper);
        } catch (FileNotFoundException e) {
            throw new TargetSetupError("Invalid CTS installation", e);
        } catch (ConfigurationException e) {
            throw new TargetSetupError("Failed to set repository directory options", e);
        }
    }

    /**
     * Installs an apkFile on device.
     *
     * @param device the {@link ITestDevice}
     * @param apkFile the apk {@link File}
     * @throws DeviceNotAvailableException
     * @throws TargetSetupError if apk cannot be installed successfully
     */
    void installApk(ITestDevice device, File apkFile)
            throws DeviceNotAvailableException, TargetSetupError {
        String errorCode = device.installPackage(apkFile, true);
        if (errorCode != null) {
            // TODO: retry ?
            throw new TargetSetupError(String.format(
                    "Failed to install %s on device %s. Reason: %s", apkFile.getName(),
                    device.getSerialNumber(), errorCode));
        }
    }

    /**
     * Install pre-requisite apks for running tests
     *
     * @throws TargetSetupError if the pre-requisite apks fail to install
     * @throws DeviceNotAvailableException
     * @throws FileNotFoundException
     */
    private void installCtsPrereqs(ITestDevice device, CtsBuildHelper ctsBuild)
            throws DeviceNotAvailableException, TargetSetupError, FileNotFoundException {
        installApk(device, ctsBuild.getTestApp(TEST_STUBS_APK));
        installApk(device, ctsBuild.getTestApp(RUNNER_APK_NAME));
    }
}
