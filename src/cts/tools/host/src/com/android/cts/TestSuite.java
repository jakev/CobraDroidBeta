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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Hold information for a suite of test case, provide functions
 * on storing and executing a test suite from CTS test harness.
 *
 */
public class TestSuite implements DeviceObserver {
    private TestPackage mParentPackage;
    private Collection<TestCase> mTestCases;
    private String mName;
    private String mFullName;
    private Collection<TestSuite> mSubSuites;

    private TestCase mCurrentTestCase;
    private TestSuite mCurrentSubSuite;
    private boolean mTestStop;

    /**
     * Construct a test suite.
     *
     * @param pkg TestPakcage as the reference to the parent package.
     * @param suiteName The current suite name, not the full name.
     * @param fullName The full suite name along the nested suite path.
     */
    public TestSuite(final TestPackage pkg, final String suiteName, final String fullName) {
        mParentPackage = pkg;
        mName = suiteName;
        mFullName = fullName;
        mTestCases = new ArrayList<TestCase>();
        mSubSuites = new ArrayList<TestSuite>();

        mTestStop = false;
        mCurrentTestCase = null;
        mCurrentSubSuite = null;
    }

    /**
     * Get parent package.
     *
     * @return Parent package.
     */
    public TestPackage getParent() {
        return mParentPackage;
    }

    /**
     * Add a specific test case.
     *
     * @param tc The test case to be added.
     */
    public void addTestCase(final TestCase tc) {
        mTestCases.add(tc);
    }

    /**
     * Add a specific test suite.
     *
     * @param suite The test suite to be added.
     */
    public void addSubSuite(final TestSuite suite) {
        mSubSuites.add(suite);
    }

    /**
     * Get TestCases.
     *
     * @return TestCases
     */
    public Collection<TestCase> getTestCases() {
        return mTestCases;
    }

    /**
     * Get the suite name of this TestSuite.
     *
     * @return The suite name of this TestCase.
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the full suite name of this TestSuite.
     *
     * @return The full suite name of this TestCase.
     */
    public String getFullName() {
        return mFullName;
    }

    /**
     * Get the nested test suites of this test suite.
     *
     * @return The nested test suites.
     */
    public Collection<TestSuite> getSubSuites() {
        return mSubSuites;
    }

    /**
     * Get all of the test suites contained in this test suite.
     *
     * @return All of the test suites.
     */
    public Collection<TestSuite> getAllSuites() {
        Collection<TestSuite> testSuites = new ArrayList<TestSuite>();
        testSuites.add(this);
        for (TestSuite suite : mSubSuites) {
            testSuites.addAll(suite.getAllSuites());
        }
        return testSuites;
    }

    /**
     * Search test in this test suite.
     *
     * @param testName The test name to be searched against.
     * @return null if not found, or return founded test
     */
    public Test searchTest(final String testName) {
        Test test = null;
        for (TestCase testCase : mTestCases) {
            test = testCase.searchTest(testName);
            if (test != null) {
                return test;
            }
        }

        if (mSubSuites.size() != 0) {
            for (TestSuite subSuite : mSubSuites) {
                test = subSuite.searchTest(testName);
                if (test != null) {
                    return test;
                }
            }
        }

        return null;
    }

    /**
     * Get the excluded list according to the execution status of each test.
     *
     * @param resultType The result type to filter the tests.
     * @return All excluded list.
     */
    public ArrayList<String> getExcludedList(final String resultType) {
        ArrayList<String> excludedList = new ArrayList<String>();
        ArrayList<String> fullNameList = new ArrayList<String>();
        for (TestSuite suite : mSubSuites) {
            fullNameList.add(suite.getFullName());
            ArrayList<String> list = suite.getExcludedList(resultType);
            if ((list != null) && (list.size() > 0)) {
                excludedList.addAll(list);
            }
        }

        for (TestCase tc : mTestCases) {
            fullNameList.add(tc.getFullName());
            ArrayList<String> list = tc.getExcludedList(resultType);
            if ((list != null) && (list.size() > 0)) {
                excludedList.addAll(list);
            }
        }

        int count = 0;
        for (String fullName : fullNameList) {
            if (excludedList.contains(fullName)) {
                count ++;
            }
        }
        if (count == fullNameList.size()) {
            //the whole suite is excluded, just need to add the full suite name
            excludedList.removeAll(excludedList);
            excludedList.add(getFullName());
        }
        return excludedList;
    }

    /**
     * Get all tests of this test suite.
     *
     * @return The tests of this suite.
     */
    public Collection<Test> getTests() {
        ArrayList<Test> tests = new ArrayList<Test>();
        for (TestSuite subSuite : mSubSuites) {
            tests.addAll(subSuite.getTests());
        }

        for (TestCase testCase : mTestCases) {
            tests.addAll(testCase.getTests());
        }

        return tests;
    }

    /**
     * Get all test cases of this test suite.
     *
     * @return The test cases of this suite.
     */
    public Collection<TestCase> getAllTestCases() {
        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.addAll(mTestCases);
        for (TestSuite subSuite : mSubSuites) {
            testCases.addAll(subSuite.getAllTestCases());
        }

        return testCases;
    }

    /**
     * Get all test case names contained in the suite.
     *
     * @return All test case names.
     */
    public ArrayList<String> getAllTestCaseNames() {
        ArrayList<String> caseNameList = new ArrayList<String>();
        for (TestCase testCase : getAllTestCases()) {
            caseNameList.add(testCase.getFullName());
        }
        return caseNameList;
    }

    /**
     * Set test stopped;
     *
     * @param testStopped If true, it's stopped. Else, still running.
     */
    public void setTestStopped(final boolean testStopped) {
        mTestStop = testStopped;
        if (mCurrentTestCase != null) {
            mCurrentTestCase.setTestStopped(mTestStop);
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.setTestStopped(mTestStop);
        }
    }

    /**
     * Run the this test suite or the specific java package contained
     * in the test suite over device given.
     *
     * @param device The device to run the test over.
     * @param javaPkgName The java package name.
     */
    public void run(final TestDevice device, final String javaPkgName)
            throws IOException, DeviceDisconnectedException, ADBServerNeedRestartException {
        Iterator<TestSuite> subSuites = getSubSuites().iterator();
        Iterator<TestCase> testCases = getTestCases().iterator();

        mTestStop = false;
        mCurrentTestCase = null;
        mCurrentSubSuite = null;

        while (subSuites.hasNext() && (!mTestStop)) {
            mCurrentSubSuite = subSuites.next();
            mCurrentSubSuite.run(device, javaPkgName);
        }

        while (testCases.hasNext() && (!mTestStop)) {
            mCurrentTestCase = testCases.next();
            String fullName = mFullName + "." + mCurrentTestCase.getName();
            if ((javaPkgName == null) || (javaPkgName.length() == 0)
                    || fullName.startsWith(javaPkgName)) {
                mCurrentTestCase.run(device);
            }
        }
    }

    /**
     * Run the specific test contained in the test suite over device given.
     *
     * @param device The device to run the test over.
     * @param test The specific test to be run.
     */
    public void run(final TestDevice device, final Test test)
            throws DeviceDisconnectedException, ADBServerNeedRestartException {
        mTestStop = false;
        mCurrentTestCase = null;
        mCurrentSubSuite = null;

        mCurrentTestCase = test.getTestCase();
        mCurrentTestCase.run(device, test);
    }

    /** {@inheritDoc} */
    public void notifyInstallingComplete(final int resultCode) {
        if (mCurrentTestCase != null) {
            mCurrentTestCase.notifyInstallingComplete(resultCode);
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.notifyInstallingComplete(resultCode);
        }
    }

    /** {@inheritDoc} */
    public void notifyUninstallingComplete(final int resultCode) {
        if (mCurrentTestCase != null) {
            mCurrentTestCase.notifyUninstallingComplete(resultCode);
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.notifyUninstallingComplete(resultCode);
        }
    }

    /** {@inheritDoc} */
    public void notifyInstallingTimeout(final TestDevice testDevice) {
        if (mCurrentTestCase != null) {
            mCurrentTestCase.notifyInstallingTimeout(testDevice);
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.notifyInstallingTimeout(testDevice);
        }
    }

    /** {@inheritDoc} */
    public void notifyUninstallingTimeout(final TestDevice testDevice) {
        if (mCurrentTestCase != null) {
            mCurrentTestCase.notifyUninstallingTimeout(testDevice);
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.notifyUninstallingTimeout(testDevice);
        }
    }

    /** {@inheritDoc} */
    public void notifyTestingDeviceDisconnected() {
        if (mCurrentTestCase != null) {
            mCurrentTestCase.notifyTestingDeviceDisconnected();
        }

        if (mCurrentSubSuite != null) {
            mCurrentSubSuite.notifyTestingDeviceDisconnected();
        }
    }
}
