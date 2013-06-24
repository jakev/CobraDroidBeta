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

import com.android.ddmlib.Log;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.targetsetup.BuildError;
import com.android.tradefed.targetsetup.IBuildInfo;
import com.android.tradefed.targetsetup.IFolderBuildInfo;
import com.android.tradefed.targetsetup.TargetSetupError;

import org.easymock.EasyMock;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link CtsSetup}.
 */
public class CtsSetupTest extends TestCase {

    private static final String LOG_TAG = "CtsSetupTest";

    private CtsSetup mSetup;
    private ITestDevice mMockDevice;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSetup = new CtsSetup() {
            @Override
            CtsBuildHelper createBuildHelper(File rootDir) {
                try {
                    return StubCtsBuildHelper.createStubHelper();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e);
                    fail("failed to create stub helper");
                    return null;
                }
            }
        };
        mMockDevice = EasyMock.createMock(ITestDevice.class);
    }

    /**
     * Test {@link CtsSetup#setUp(ITestDevice, IBuildInfo)} when provided buildInfo is the incorrect
     * type
     */
    public void testSetUp_wrongBuildInfo() throws TargetSetupError, BuildError,
            DeviceNotAvailableException {
        try {
            mSetup.setUp(mMockDevice, EasyMock.createMock(IBuildInfo.class));
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test normal case for {@link CtsSetup#setUp(ITestDevice, IBuildInfo)}
     */
    public void testSetUp() throws TargetSetupError, BuildError, DeviceNotAvailableException {
        IFolderBuildInfo ctsBuild = EasyMock.createMock(IFolderBuildInfo.class);
        EasyMock.expect(ctsBuild.getRootDir()).andReturn(
                new File("tmp")).anyTimes();
        EasyMock.expect(ctsBuild.getBuildId()).andStubReturn(0);
        EasyMock.expect(
                mMockDevice.installPackage((File)EasyMock.anyObject(), EasyMock.anyBoolean()))
                .andReturn(null)
                .anyTimes();
        EasyMock.replay(ctsBuild, mMockDevice);
        mSetup.setUp(mMockDevice, ctsBuild);
    }
}
