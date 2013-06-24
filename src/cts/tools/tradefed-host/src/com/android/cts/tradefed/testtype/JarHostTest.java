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

import com.android.ddmlib.Log;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.result.ITestInvocationListener;
import com.android.tradefed.result.JUnitToInvocationResultForwarder;
import com.android.tradefed.testtype.AbstractRemoteTest;
import com.android.tradefed.testtype.IDeviceTest;
import com.android.tradefed.testtype.IRemoteTest;
import com.android.tradefed.util.CommandStatus;
import com.android.tradefed.util.RunUtil;
import com.android.tradefed.util.IRunUtil.IRunnableResult;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * A {@link IRemoteTest} that can run a set of JUnit tests from a jar.
 */
public class JarHostTest extends AbstractRemoteTest implements IDeviceTest {

    private static final String LOG_TAG = "JarHostTest";

    private ITestDevice mDevice;
    private File mJarFile;
    private Collection<TestIdentifier> mTests;
    private long mTimeoutMs = 10 * 60 * 1000;
    private String mRunName;
    private String mTestAppPath;

    /**
     * Set the jar file to load tests from.
     * @param jarFile
     */
    void setJarFile(File jarFile) {
        mJarFile = jarFile;
    }

    /**
     * Sets the collection of tests to run
     * @param tests
     */
    void setTests(Collection<TestIdentifier> tests) {
        mTests = tests;
    }

    /**
     * Set the maximum time in ms each test should run.
     * <p/>
     * Tests that take longer than this amount will be failed with a {@link TestTimeoutException}
     * as the cause.
     *
     * @param testTimeout
     */
    void setTimeout(long testTimeoutMs) {
        mTimeoutMs = testTimeoutMs;
    }

    /**
     * Set the run name to report to {@link ITestInvocationListener#testRunStarted(String, int)}
     *
     * @param runName
     */
    void setRunName(String runName) {
        mRunName = runName;
    }

    /**
     * Set the filesystem path to test app artifacts needed to run tests.
     *
     * @see {@link com.android.hosttest.DeviceTest#setTestAppPath(String)}
     *
     * @param testAppPath
     */
    void setTestAppPath(String testAppPath) {
        mTestAppPath = testAppPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITestDevice getDevice() {
        return mDevice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDevice(ITestDevice device) {
        mDevice = device;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countTestCases() {
        if (mTests == null) {
            throw new IllegalStateException();
        }
        return mTests.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(List<ITestInvocationListener> listeners) throws DeviceNotAvailableException {
        checkFields();
        Log.i(LOG_TAG, String.format("Running %s test package from jar, contains %d tests.",
                mRunName, countTestCases()));
        // create a junit listener to forward the JUnit test results to the
        // {@link ITestInvocationListener}s
        JUnitToInvocationResultForwarder resultForwarder =
                new JUnitToInvocationResultForwarder(listeners);
        TestResult junitResult = new TestResult();
        junitResult.addListener(resultForwarder);
        long startTime = System.currentTimeMillis();
        reportRunStarted(listeners);
        for (TestIdentifier testId : mTests) {
            Test junitTest = loadTest(testId.getClassName(), testId.getTestName());
            if (junitTest != null) {
                runTest(testId, junitTest, junitResult);
            }
        }
        reportRunEnded(System.currentTimeMillis() - startTime, listeners);
    }

    /**
     * Report the start of the test run.
     *
     * @param listeners
     */
    private void reportRunStarted(List<ITestInvocationListener> listeners) {
        for (ITestInvocationListener listener : listeners) {
            listener.testRunStarted(mRunName, countTestCases());
        }
    }

    /**
     * Run test with timeout support.
     */
    private void runTest(TestIdentifier testId, final Test junitTest, final TestResult junitResult) {
        if (junitTest instanceof IDeviceTest) {
            ((IDeviceTest)junitTest).setDevice(getDevice());
        } else if (junitTest instanceof com.android.hosttest.DeviceTest) {
            // legacy check - see if test uses hosttestlib. This check should go away once
            // all host tests are converted to use tradefed
            com.android.hosttest.DeviceTest deviceTest = (com.android.hosttest.DeviceTest)junitTest;
            deviceTest.setDevice(getDevice().getIDevice());
            deviceTest.setTestAppPath(mTestAppPath);
        }
        CommandStatus status = RunUtil.getInstance().runTimed(mTimeoutMs, new IRunnableResult() {

            @Override
            public boolean run() throws Exception {
                junitTest.run(junitResult);
                return true;
            }

            @Override
            public void cancel() {
                // ignore
            }
        });
        if (status.equals(CommandStatus.TIMED_OUT)) {
            junitResult.addError(junitTest, new TestTimeoutException());
            junitResult.endTest(junitTest);
        }
    }

    /**
     * Report the end of the test run.
     *
     * @param elapsedTime
     * @param listeners
     */
    @SuppressWarnings("unchecked")
    private void reportRunEnded(long elapsedTime, List<ITestInvocationListener> listeners) {
        for (ITestInvocationListener listener : listeners) {
            listener.testRunEnded(elapsedTime, Collections.EMPTY_MAP);
        }
    }

    /**
     * Load the test with given names from the jar.
     *
     * @param className
     * @param testName
     * @return the loaded {@link Test} or <code>null</code> if test could not be loaded.
     */
    private Test loadTest(String className, String testName) {
        try {
            URL urls[] = {mJarFile.getCanonicalFile().toURI().toURL()};
            Class<?> testClass = loadClass(className, urls);

            if (TestCase.class.isAssignableFrom(testClass)) {
                TestCase testCase = (TestCase)testClass.newInstance();
                testCase.setName(testName);
                return testCase;
            } else if (Test.class.isAssignableFrom(testClass)) {
                Test test = (Test)testClass.newInstance();
                return test;
            } else {
                Log.e(LOG_TAG, String.format("Class '%s' from jar '%s' is not a Test",
                        className, mJarFile.getAbsolutePath()));
            }
        } catch (ClassNotFoundException e) {
            reportLoadError(mJarFile, className, e);
        } catch (IllegalAccessException e) {
            reportLoadError(mJarFile, className, e);
        } catch (IOException e) {
            reportLoadError(mJarFile, className, e);
        } catch (InstantiationException e) {
            reportLoadError(mJarFile, className, e);
        }
        return null;
    }

    /**
     * Loads a class from given URLs.
     * <p/>
     * Exposed so unit tests can mock
     *
     * @param className
     * @param urls
     * @return
     * @throws ClassNotFoundException
     */
    Class<?> loadClass(String className, URL[] urls) throws ClassNotFoundException {
        URLClassLoader cl = new URLClassLoader(urls);
        Class<?> testClass = cl.loadClass(className);
        return testClass;
    }

    private void reportLoadError(File jarFile, String className, Exception e) {
        Log.e(LOG_TAG, String.format("Failed to load test class '%s' from jar '%s'",
                className, jarFile.getAbsolutePath()));
        Log.e(LOG_TAG, e);
    }

    /**
     * Checks that all mandatory member fields has been set.
     */
    private void checkFields() {
        if (mRunName == null) {
            throw new IllegalArgumentException("run name has not been set");
        }
        if (mDevice == null) {
            throw new IllegalArgumentException("Device has not been set");
        }
        if (mJarFile == null) {
            throw new IllegalArgumentException("jar file has not been set");
        }
        if (mTests == null) {
            throw new IllegalArgumentException("tests has not been set");
        }
    }
}
