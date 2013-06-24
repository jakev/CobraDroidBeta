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

package com.android.cts.tradefed.testtype;

import com.android.cts.tradefed.device.DeviceInfoCollector;
import com.android.ddmlib.Log;
import com.android.tradefed.config.Option;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.result.ITestInvocationListener;
import com.android.tradefed.testtype.AbstractRemoteTest;
import com.android.tradefed.testtype.IDeviceTest;
import com.android.tradefed.testtype.IRemoteTest;
import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

/**
 * A {@link Test} that runs all the tests in the CTS test plan with given name
 */
public class PlanTest extends AbstractRemoteTest implements IDeviceTest, IRemoteTest {

    private static final String LOG_TAG = "PlanTest";

    public static final String TEST_CASES_DIR_OPTION = "test-cases-path";
    public static final String TEST_PLANS_DIR_OPTION = "test-plans-path";

    private ITestDevice mDevice;

    @Option(name = "plan", description = "the test plan to run")
    private String mPlanName = null;

    @Option(name = TEST_CASES_DIR_OPTION, description =
        "file path to directory containing CTS test cases")
    private File mTestCaseDir = null;

    @Option(name = TEST_PLANS_DIR_OPTION, description =
        "file path to directory containing CTS test plans")
    private File mTestPlanDir = null;

    /**
     * {@inheritDoc}
     */
    public ITestDevice getDevice() {
        return mDevice;
    }

    /**
     * {@inheritDoc}
     */
    public void setDevice(ITestDevice device) {
        mDevice = device;
    }

    /**
     * Set the test plan directory.
     * <p/>
     * Exposed for unit testing
     */
    void setTestPlanDir(File planDir) {
        mTestPlanDir = planDir;
    }

    /**
     * Set the test case directory.
     * <p/>
     * Exposed for unit testing
     */
    void setTestCaseDir(File testCaseDir) {
        mTestCaseDir = testCaseDir;
    }

    /**
     * Set the plan name to run.
     * <p/>
     * Exposed for unit testing
     */
    void setPlanName(String planName) {
        mPlanName = planName;
    }

    /**
     * {@inheritDoc}
     */
    public void run(List<ITestInvocationListener> listeners) throws DeviceNotAvailableException {
        if (mPlanName == null) {
            throw new IllegalArgumentException("missing --plan option");
        }
        if (getDevice() == null) {
            throw new IllegalArgumentException("missing device");
        }
        if (mTestCaseDir == null) {
            throw new IllegalArgumentException(String.format("missing %s option",
                    TEST_CASES_DIR_OPTION));
        }
        if (mTestPlanDir == null) {
            throw new IllegalArgumentException(String.format("missing %s", TEST_PLANS_DIR_OPTION));
        }

        Log.i(LOG_TAG, String.format("Executing CTS test plan %s", mPlanName));

        try {
            String ctsPlanRelativePath = String.format("%s.xml", mPlanName);
            File ctsPlanFile = new File(mTestPlanDir, ctsPlanRelativePath);
            IPlanXmlParser parser = createXmlParser();
            parser.parse(createXmlStream(ctsPlanFile));
            Collection<String> testUris = parser.getTestUris();
            ITestCaseRepo testRepo = createTestCaseRepo();
            Collection<IRemoteTest> tests = testRepo.getTests(testUris);
            collectDeviceInfo(getDevice(), mTestCaseDir, listeners);
            for (IRemoteTest test : tests) {
                if (test instanceof IDeviceTest) {
                    ((IDeviceTest)test).setDevice(getDevice());
                }
                test.run(listeners);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("failed to find CTS plan file", e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("failed to parse CTS plan file", e);
        }
    }

    /**
     * Runs the device info collector instrumentation on device, and forwards it to test listeners
     * as run metrics.
     *
     * @param listeners
     * @throws DeviceNotAvailableException
     */
    private void collectDeviceInfo(ITestDevice device, File testApkDir,
            List<ITestInvocationListener> listeners) throws DeviceNotAvailableException {
        DeviceInfoCollector.collectDeviceInfo(device, testApkDir, listeners);
    }

    /**
     * Factory method for creating a {@link ITestCaseRepo}.
     * <p/>
     * Exposed for unit testing
     */
    ITestCaseRepo createTestCaseRepo() {
        return new TestCaseRepo(mTestCaseDir);
    }

    /**
     * Factory method for creating a {@link PlanXmlParser}.
     * <p/>
     * Exposed for unit testing
     */
    IPlanXmlParser createXmlParser() {
        return new PlanXmlParser();
    }

    /**
     * Factory method for creating a {@link InputStream} from a plan xml file.
     * <p/>
     * Exposed for unit testing
     */
    InputStream createXmlStream(File xmlFile) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(xmlFile));
    }
}
