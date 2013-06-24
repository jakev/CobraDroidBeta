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

package com.android.cts;

import java.io.IOException;

/**
 * The host side only package.
 */
public class HostSideOnlyPackage extends TestPackage {

    /**
     * Construct a host side only package with given necessary information.
     *
     * @param testPkgBinaryName The binary name of the TestPackage.
     * @param version The version of the CTS Host allowed.
     * @param androidVersion The version of the Android platform allowed.
     * @param jarPath The host controller's jar path and file.
     * @param appPackageName The Java package name of the test package.
     */
    public HostSideOnlyPackage(final String testPkgBinaryName, final String version,
            final String androidVersion, final String jarPath,
            final String appPackageName) {
        super(null, testPkgBinaryName, null, null, version,
                androidVersion, jarPath, null, appPackageName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHostSideOnly() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void runImpl(final String javaPkgName)
            throws IOException, DeviceDisconnectedException, ADBServerNeedRestartException {
        try {
            if (!mTestStop) {
                Log.d("run in individual mode...");
                runInIndividualMode(javaPkgName);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void runTestImpl(final Test test) throws DeviceDisconnectedException,
                ADBServerNeedRestartException {
        try {
            if (!mTestStop) {
                mCurrentTestSuite = test.getTestSuite();
                mCurrentTestSuite.run(mDevice, test);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
}
