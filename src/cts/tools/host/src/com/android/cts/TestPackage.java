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

import com.android.cts.TestSession.TestSessionThread;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

/**
 * Correspond to an APK, provide functions on
 * representing and executing an APK from CTS test harness.
 */
public class TestPackage implements DeviceObserver {
    protected static final String PKG_LOG_SEPARATOR =
              "==============================================================";

    /**
     * For batch mode, there is just one command sent to the device
     * for the whole package, and the device will feed back the result
     * test by test. To guard the command, a timeout timer is started
     * to prevent it from running forever. And to make the timeout time
     * not too long, it's better choice to restart the timer each time
     * received the feedback from device. The following two variables
     * are used to restart/stop the timer, START for restarting and
     * FINISH for stopping.
     */
    public static final String FINISH = "finish";
    public static final String START = "start";

    private String mName, mVersion, mAndroidVersion;
    private String mTargetNameSpace, mTargetBinaryName, mInstrumentationRunner;
    private Collection<TestSuite> mSuites;
    private String mDigest;
    private String mJarPath;
    private String mAppNameSpace;
    private String mAppPackageName;

    protected TestSuite mCurrentTestSuite;

    protected TestDevice mDevice;

    protected boolean mTestStop;
    private TestSessionThread mTestThread;

    private HostTimer mTimeOutTimer;
    private ProgressObserver mProgressObserver;
    private boolean mIsInBatchMode;
    private Test mCurrentTest;

    /**
     * Construct a test package with given necessary information.
     *
     * @param instrumentationRunner The instrumentation runner.
     * @param testPkgBinaryName The binary name of the TestPackage.
     * @param targetNameSpace The target name space of the dependent package, if available.
     * @param version The version of the CTS Host allowed.
     * @param androidVersion The version of the Anroid platform allowed.
     * @param jarPath The host controller's jar path and file.
     * @param appNameSpace The package name space.
     * @param appPackageName The Java package name of the test package.
     */
    public TestPackage(final String instrumentationRunner,
            final String testPkgBinaryName, final String targetNameSpace,
            final String targetBinaryName, final String version,
            final String androidVersion, final String jarPath, final String appNameSpace,
            final String appPackageName) {
        mInstrumentationRunner = instrumentationRunner;
        mName = testPkgBinaryName;
        mTargetNameSpace = targetNameSpace;
        mTargetBinaryName = targetBinaryName;
        mVersion = version;
        mAndroidVersion = androidVersion;
        mSuites = new ArrayList<TestSuite>();
        mJarPath = jarPath;
        mAppNameSpace = appNameSpace;
        mAppPackageName = appPackageName;

        mDevice = null;
        mTestStop = false;
        mTestThread = null;
        mIsInBatchMode = false;
        mCurrentTest = null;
    }

    /**
     * Get the app package name space.
     *
     * @return The app package name space.
     */
    public String getAppNameSpace() {
        return mAppNameSpace;
    }

    /**
     * Get the app JAVA package name.
     *
     * @return The app JAVA package name.
     */
    public String getAppPackageName() {
        return mAppPackageName;
    }

    /**
     * Returns whether this is a host side test package.
     */
    public boolean isHostSideOnly() {
        return false;
    }

    /**
     * Add a TestSuite.
     *
     * @param suite The TestSuite to be added.
     */
    public void addTestSuite(final TestSuite suite) {
        mSuites.add(suite);
    }

    /**
     * Get test suites under this package.
     *
     * @return The test suites under this package.
     */
    public Collection<TestSuite> getTestSuites() {
        return mSuites;
    }

    /**
     * Get the specific test suite by the full suite name.
     *
     * @param suiteFullName The full suite name.
     * @return The test suite.
     */
    public TestSuite getTestSuiteByName(final String suiteFullName) {
        for (TestSuite suite : getAllTestSuites()) {
            if (suite.getFullName().equals(suiteFullName)) {
                return suite;
            }
        }
        return null;
    }

    /**
     * Get the specific test case by the full test case name.
     *
     * @param testCaseFullName The full test case name.
     * @return The test case.
     */
    public TestCase getTestCaseByName(final String testCaseFullName) {
        for (TestCase testCase : getAllTestCases()) {
            if (testCase.getFullName().equals(testCaseFullName)) {
                return testCase;
            }
        }
        return null;
    }

    /**
     * Get all of test suites under this package.
     *
     * @return All of the test suites under this package.
     */
    public Collection<TestSuite> getAllTestSuites() {
        Collection<TestSuite> suites = new ArrayList<TestSuite>();
        for (TestSuite suite : mSuites) {
            suites.addAll(suite.getAllSuites());
        }
        return suites;
    }

    /**
     * Get suite/case names contained in this test package, searched against the expected name.
     *
     * @param expectName The expected name.
     * @param suiteNameList The suite names list.
     * @param caseNameList The case names list.
     */
    public void getTestSuiteNames(final String expectName,
            List<String> suiteNameList, List<String> caseNameList) {

        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            if (testCaseName.startsWith(expectName)) {
                String suiteName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
                if (suiteName.equals(expectName)) {
                    if (!caseNameList.contains(testCaseName)) {
                        caseNameList.add(testCaseName);
                    }
                } else {
                    if (!suiteNameList.contains(suiteName)) {
                        suiteNameList.add(suiteName);
                    }
                }
            }
        }
    }

    /**
     * Get all test suite names contained in this test package.
     *
     * @return The test suite name list.
     */
    public List<String> getAllTestSuiteNames() {
        List<String> suiteNameList = new ArrayList<String>();
        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            String suiteName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
            if (!suiteNameList.contains(suiteName)) {
                suiteNameList.add(suiteName);
            }
        }
        return suiteNameList;
    }

    /**
     * Get all test case names contained in the suite in this test package.
     *
     * @param suiteFullName The full suite name.
     * @return All test case names.
     */
    public List<String> getAllTestCaseNames(final String suiteFullName) {
        List<String> caseNameList = new ArrayList<String>();
        TestSuite suite = getTestSuiteByName(suiteFullName);
        if (suite != null) {
            caseNameList.addAll(suite.getAllTestCaseNames());
        }
        return caseNameList;
    }

    /**
     * Get all test names contained in the test case in this test package.
     *
     * @param testCaseFullName The full test case name.
     * @return All test names.
     */
    public List<String> getAllTestNames(final String testCaseFullName) {
        List<String> testNameList = new ArrayList<String>();
        TestCase testCase = getTestCaseByName(testCaseFullName);
        if (testCase != null) {
            testNameList.addAll(testCase.getAllTestNames());
        }
        return testNameList;
    }

    /**
     * Get test case names list.
     *
     * @param expectPackage The expected package name.
     * @param caseList The searched test case result.
     * @param testList The searched test result.
     */
    public void getTestCaseNames(final String expectPackage, List<String> caseList,
            List<String> testList) {

        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            if (testCaseName.equals(expectPackage)) {
                for (Test test : testCase.getTests()) {
                    testList.add(test.getFullName());
                }
                return;
            } else if (testCaseName.startsWith(expectPackage)) {
                caseList.add(testCaseName);
            }
        }
    }

    /**
     * Get test names list.
     *
     * @param expectPackage The expected package name.
     * @param testList The searched test result.
     */
    public void getTestNames(final String expectPackage, List<String> testList) {

        for (Test test : getTests()) {
            String testName = test.getFullName();
            if (testName.startsWith(expectPackage)) {
                testList.add(testName);
            }
        }
    }

    /**
     * Get the binary name of this package.
     *
     * @return The binary name of this package.
     */
    public String getAppBinaryName() {
        return mName;
    }

    /**
     * Get the version string of this package.
     *
     * @return The version string of this package.
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * Get the version information of Android.
     *
     * @return The version information of Android.
     */
    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    /**
     * Get the target name space of this package.
     *
     * @return The target name space of the package.
     */
    public String getTargetNameSpace() {
        return mTargetNameSpace;
    }

    /**
     * Get the target binary name.
     *
     * @return The target binary name.
     */
    public String getTargetBinaryName() {
        return mTargetBinaryName;
    }

    /**
     * Get the instrumentation runner.
     *
     * @return The instrumentation runner.
     */
    public String getInstrumentationRunner() {
        return mInstrumentationRunner;
    }

    /**
     * Search a specific Test within this package.
     *
     * @param testName The test name to be searched against.
     * @return The Test matches the given name.
     */
    public Test searchTest(final String testName) {
        Test test = null;
        for (TestSuite suite : mSuites) {
            test = suite.searchTest(testName);
            if (test != null) {
                break;
            }
        }

        return test;
    }

    /**
     * Get all tests of this test package.
     *
     * @return The tests of this test package.
     */
    public Collection<Test> getTests() {
        List<Test> tests = new ArrayList<Test>();
        for (TestSuite s : mSuites) {
            tests.addAll(s.getTests());
        }

        return tests;
    }

    /**
     * Get all test cases of this test package.
     *
     * @return The test cases of this test package.
     */
    public Collection<TestCase> getAllTestCases() {
        List<TestCase> testCases = new ArrayList<TestCase>();
        for (TestSuite s : mSuites) {
            testCases.addAll(s.getAllTestCases());
        }

        return testCases;
    }

    /**
     * Set the message digest of the test package.
     *
     * @param digest the string of the package's message digest.
     */
    private void setMessageDigest(final String digest) {
        mDigest = digest;
    }

    /**
     * Get the string of package's message digest.
     *
     * @return message digest string.
     */
    public String getMessageDigest() {
        return mDigest;
    }

    /**
     * Get the the path of the controller jar file.
     *
     * @return message digest string.
     */
    public String getJarPath() {
        return mJarPath;
    }

    /**
     * Get the excluded list according to the execution status of each test.
     *
     * @param resultType The result type to filter the tests.
     * @return All excluded list. There are three scenarios to interpret the return value:
     *      <ul>
     *          <li> null: nothing should be added to plan;
     *          <li> list size equals 0: the whole package should be added to plan;
     *          <li> list size greater than 0: the given excluded list should be added to plan.
     *      </ul>
     */
    public ArrayList<String> getExcludedList(final String resultType) {
        ArrayList<String> excludedList = new ArrayList<String>();
        ArrayList<String> fullNameList = new ArrayList<String>();
        for (TestSuite suite : getTestSuites()) {
            fullNameList.add(suite.getFullName());
            ArrayList<String> list = suite.getExcludedList(resultType);
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
            //all suites contained have been excluded,
            //return null to tell the caller nothing to add to the plan
            return null;
        }
        return excludedList;
    }

    /**
     * Print the message by appending the new line mark.
     *
     * @param msg The message to be print.
     */
    protected void println(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.println(msg);
        }
    }

    /**
     * Print the message without appending the new line mark.
     *
     * @param msg The message to be print.
     */
    protected void print(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.print(msg);
        }
    }

    /**
     * Notify that the batch mode finished.
     */
    public void notifyBatchModeFinish() {
        Log.d("TestPackage.notifyBatchModeFinish() is called, mTestStop=" + mTestStop);
        if (mTestStop) {
            return;
        }

        if (mIsInBatchMode) {
            if (mCurrentTest != null) {
                handleMissingFinishEvent();
            }
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.sendNotify();
            }
        }
    }

    /**
     * Handle the missing FINISH event.
     */
    private void handleMissingFinishEvent() {
        mProgressObserver.stop();
        synchronized (mTimeOutTimer) {
            mTimeOutTimer.cancel(false);
        }
        // The currently running test did not report a result. Mark it as not executed, so that it
        // will be run again in individual mode.
        mCurrentTest.setResult(new CtsTestResult(CtsTestResult.CODE_NOT_EXECUTED, null, null));
        mCurrentTest = null;
    }

    /**
     * Update Test running status when running in batch mode.
     *
     * @param test The Test to update. May be null if a status gets reported on a test that is not
     * in the test plan.
     * @param status The status to be updated.
     */
    public void notifyTestStatus(final Test test, final String status) {
        if (mTestStop) {
            return;
        }

        if (mIsInBatchMode) {
            if (status.equals(START)) {
                if ((mCurrentTest != null) && (mCurrentTest.getResult().isNotExecuted())) {
                    Log.d("Err: Missing FINISH msg for test " + mCurrentTest.getFullName());
                    handleMissingFinishEvent();
                }
                mCurrentTest = test;
                if (test != null) {
                    print(mCurrentTest.getFullName() + "...");
                    mProgressObserver.start();
                }
            } else {
                mProgressObserver.stop();
                mCurrentTest = null;
            }
            // restart the timer even for unexpected tests
            mTimeOutTimer.restart(new TimeOutTask(this),
                    HostConfig.Ints.testStatusTimeoutMs.value());
        }
    }

    /** {@inheritDoc} */
    public void notifyInstallingComplete(final int resultCode) {
        Log.d("notifyInstallingComplete() is called with resultCode=" + resultCode);
        sendNotify();

        if (resultCode == FAIL) {
            Log.d("install failed");
        }
    }

    /** {@inheritDoc} */
    public void notifyUninstallingComplete(final int resultCode) {
        Log.d("notifyUninstallingComplete() is called with resultCode=" + resultCode);
        sendNotify();

        if (resultCode == FAIL) {
            Log.d("uninstall failed");
        }
    }

    /**
     * Send notify to wake up the thread waiting on the object.
     */
    private void sendNotify() {
        synchronized (this) {
            notify();
        }
    }

    /** {@inheritDoc} */
    public void notifyInstallingTimeout(final TestDevice testDevice) {
        Log.d("TestPackage.notifyInstallingTimeout() is called");
        mTestStop = true;
        synchronized (this) {
            notify();
        }

        genPackageActionTimeoutCause(testDevice, "Installing");
    }

    /** {@inheritDoc} */
    public void notifyUninstallingTimeout(final TestDevice testDevice) {
        Log.d("TestPackage.notifyUninstallingTimeout() is called");
        mTestStop = true;
        synchronized (this) {
            notify();
        }

        genPackageActionTimeoutCause(testDevice, "Uninstalling");
    }

    /**
     * Generate the cause of package action timeout.
     *
     * @param testDevice The {@link TestDevice} which got timeout.
     * @param type Install or Uninstall.
     */
    private void genPackageActionTimeoutCause(final TestDevice testDevice, String type) {
        String cause;
        if (testDevice.getStatus() == TestDevice.STATUS_OFFLINE) {
            cause = testDevice.getSerialNumber() + " is offline.";
        } else {
            cause = "Unknown reason.";
        }

        if (type == null) {
            type = "Unknown timer";
        }
        Log.e(type + " met timeout due to " + cause, null);
    }

    /** {@inheritDoc} */
    public void notifyTestingDeviceDisconnected() {
        Log.d("busyDeviceDisconnected invoked");
        mTestStop = true;
        synchronized (this) {
            notify();
        }

        cleanUp();

        try {
            CUIOutputStream.println("Test stopped.");
            mTestThread.join();
        } catch (InterruptedException e) {
            Log.d("test thread interrupted");
        }
    }

    /**
     * Set the {@link TestDevice} which will run the test.
     *
     * @param device The {@link TestDevice} will run the test.
     */
    public void setTestDevice(final TestDevice device) {
        mDevice = device;
        device.setRuntimeListener(this);
        device.setStatus(TestDevice.STATUS_BUSY);
    }

    /**
     * Get the full path information.
     *
     * @param binaryFileName The binary file name.
     * @return The full path information.
     */
    private String getFullPath(String binaryFileName) {
        String packagePath = null;
        if ((binaryFileName != null) && (binaryFileName.length() != 0)) {
            packagePath = HostConfig.getInstance().getCaseRepository()
                .getApkPath(binaryFileName);
        }
        return packagePath;
    }
    /**
     * Load(install) test package and target package(if it exists).
     *
     * @return If succeed in installing, return true; else, return false.
     */
    private boolean install() throws DeviceDisconnectedException, InvalidApkPathException {
        String packageBinaryName = getAppBinaryName();
        String targetBinaryName = getTargetBinaryName();
        String packagePath = getFullPath(packageBinaryName);
        String targetApkPath = getFullPath(targetBinaryName);

        boolean success = true;
        if (packagePath != null) {
            installAPK(packagePath);
            if ((!mTestStop) && (targetApkPath != null)) {
                installAPK(targetApkPath);
            }
        } else {
            success = false;
            Log.e("The package binary name contains nothing!", null);
        }

        if (mTestStop) {
            success = false;
            println("Install package " + packageBinaryName + "failed");
        }

        return success;
    }

    /**
     * Uninstall test package and target package(if it exists)
     */
    private void uninstall() throws DeviceDisconnectedException, InvalidNameSpaceException {

        String testPkgBinaryName = getAppBinaryName();
        String appNameSpace = getAppNameSpace();
        String targetNameSpace = getTargetNameSpace();
        String packagePath = getFullPath(testPkgBinaryName);
        String targetApkPath = getFullPath(targetNameSpace);

        if ((packagePath != null) && HostUtils.isFileExist(packagePath)) {
            uninstallAPK(appNameSpace);
            if ((!mTestStop) && (targetNameSpace != null)
                    && ((targetApkPath != null) && (HostUtils.isFileExist(targetApkPath)))) {
                uninstallAPK(targetNameSpace);
            }
        }
    }

    /**
     * Uninstall the specified package(.apk)
     */
    private void uninstallAPK(final String packageName) throws DeviceDisconnectedException,
                InvalidNameSpaceException {
        Log.d("Uninstall: " + packageName);
        mDevice.uninstallAPK(packageName);
        waitPackageActionComplete();
    }

    /**
     * Install the test package on the devices attached to this session.
     *
     * @param apkPath The test package to be installed.
     */
    private void installAPK(final String apkPath) throws DeviceDisconnectedException,
            InvalidApkPathException {
        Log.d("installAPK " + apkPath + " ...");
        mDevice.installAPK(apkPath);
        waitPackageActionComplete();
        Log.d("installAPK " + apkPath + " finish");
    }

    /**
     * Wait for package action to complete.
     */
    private void waitPackageActionComplete() {
        Log.d("Enter waitPackageActionComplete()");
        synchronized (this) {
            if (!mTestStop) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.d("interrupted while waiting for package action complete");
                }
            }
        }
        try {
            Thread.sleep(HostConfig.Ints.postInstallWaitMs.value());
        } catch (InterruptedException e) {
            Log.d("sleeping after package action complete interrupted");
        }
        Log.d("Leave waitPackageActionComplete()");
    }

    /**
     * Generate the message digest of the specified package
     *
     * @param packagePath path to the package.
     * @return message digest string(base64 encoded).
     */
    private String genMessageDigest(final String packagePath) throws IOException {
        final String algorithm = "SHA-1";
        FileInputStream fin = new FileInputStream(packagePath);
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fin.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fin.close();
            return HostUtils.toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return algorithm + " not found";
        }
    }

    /**
     * Set the test session thread.
     *
     * @param thread
     */
    public void setSessionThread(TestSessionThread thread) {
        mTestThread = thread;
    }

    /**
     * Check if it's valid to use batch mode.
     *
     * @return If each test under this package doesn't depend on any host controller, return true;
     *         else, return false;
     */
    private boolean supportsBatchMode() {
        Collection<Test> tests = getTests();

        // check whether the package is small enough for batch mode
        if (tests.size() > HostConfig.Ints.maxTestsInBatchMode.value()) {
            return false;
        }

        for (Test test : tests) {
            if (!test.getResult().isNotExecuted()) {
                // if any test has been run, use individual mode
                return false;
            }

            if ((test.getTestController() != null)
                && (test.getTestController().getFullName() != null)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the first segment list of all of the test packages.
     *
     * @return the first segment list of all of the test packages contained in this test package;
     */
     List<String> getPackageNames() {
        List<String> pkgNames = new ArrayList<String>();
        List<String> suiteNames = getAllTestSuiteNames();
        for (String suiteName : suiteNames) {
            String pkgSeg = suiteName;
            if (suiteName.contains(".")) {
                pkgSeg = suiteName.split("\\.")[0];
            }
            if (!pkgNames.contains(pkgSeg)) {
                pkgNames.add(pkgSeg);
            }
        }

        return pkgNames;
    }

    /**
     * Run this package or the java package contained in this package in batch mode.
     *
     * @param javaPkgName The java package name. If null, run the whole package;
     *              else, run the specified java package contained in this package
     */
    private void runInBatchMode(final String javaPkgName)
            throws DeviceDisconnectedException {
        mTimeOutTimer = new HostTimer(new TimeOutTask(this),
                HostConfig.Ints.batchStartTimeoutMs.value());
        mTimeOutTimer.start();
        mProgressObserver = new ProgressObserver();

        if ((javaPkgName != null) && (javaPkgName.length() > 0)) {
            runInBatchModeImpl(javaPkgName);
        } else {
            for (String pkgName : getPackageNames()) {
                runInBatchModeImpl(pkgName);
            }
        }
    }

    /**
     * Implementation of running in batch mode.
     *
     * @param javaPkgName The java package name.
     */
    private void runInBatchModeImpl(String javaPkgName)
            throws DeviceDisconnectedException {
        mDevice.runInBatchMode(this, javaPkgName);

        synchronized (mTimeOutTimer) {
            if (!mTestStop) {
                try {
                    mTimeOutTimer.waitOn();
                } catch (InterruptedException e) {
                    Log.d("time out object interrupted");
                }
            }

            mProgressObserver.stop();
            if (mTimeOutTimer.isTimeOut()) {
                return;
            } else {
                // not caused by watch dog timer timing out,
                // need to cancel timer
                mTimeOutTimer.cancel(false);
            }
        }
    }

    /**
     * Run this package in individual mode.
     *
     * @param javaPkgName The java package name.
     * @param profile The profile of the device being tested.
     */
    protected void runInIndividualMode(final String javaPkgName) throws IOException,
                    DeviceDisconnectedException, ADBServerNeedRestartException {
        Iterator<TestSuite> suites = getTestSuites().iterator();
        while (suites.hasNext() && (!mTestStop)) {
            mCurrentTestSuite = suites.next();
            mCurrentTestSuite.run(mDevice, javaPkgName);
        }
    }

    /**
     * The timer task which aids in guarding the running package with the
     * guarding timer. If the executing of the package is not finished, and the
     * guarding timer is expired, this task will be executed to force the finish
     * of the running package.
     */
    class TimeOutTask extends TimerTask {
        private TestPackage mTestPackage;

        public TimeOutTask(final TestPackage testPackage) {
            mTestPackage = testPackage;
        }

        @Override
        public void run() {
            mProgressObserver.stop();
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.cancel(true);
                mTimeOutTimer.sendNotify();
            }

            if ((mIsInBatchMode) && (mCurrentTest != null)) {
                mCurrentTest.setResult(
                        new CtsTestResult(CtsTestResult.CODE_TIMEOUT, null, null));
                mCurrentTest = null;
            }

            Log.d("mTimeOutTimer timed out");
            killDeviceProcess(mTestPackage.getAppPackageName());
        }
    }

    /**
     * Kill the device process.
     *
     * @param packageName
     */
    private void killDeviceProcess(final String packageName) {
        mDevice.killProcess(packageName);
    }

    /**
     * Check if all of the tests contained in this package have been run.
     *
     * @return If all tests have been run, return true; else, return false.
     */
    protected boolean isAllTestsRun(){
        for (Test test : getTests()) {
            if (test.getResult().isNotExecuted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if any of the tests contained in this package have been executed.
     *
     * @return If no tests have been executed, return true, otherwise return false.
     */
    protected boolean noTestsExecuted() {
        for (Test test : getTests()) {
            if (!test.getResult().isNotExecuted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Run the java package contained within this package over device.
     *
     * @param device The device to run this package.getName
     * @param sessionLog the TestSession log for this TestSession.
     */
    public void run(final TestDevice device, final String javaPkgName,
                    TestSessionLog sessionLog)
            throws IOException, DeviceDisconnectedException,
            ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        if (isAllTestsRun()) {
            return;
        }

        setup(device, javaPkgName);
        runImpl(javaPkgName);
    }

    /**
     * Implementation of running the test package.
     *
     * @param javaPkgName The JAVA package name.
     * @param profile The profile of the device being tested.
     */
    protected void runImpl(final String javaPkgName) throws IOException,
            DeviceDisconnectedException, ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        try {
            if (!install()) {
                return;
            }

            if (!mTestStop) {
                Log.d("install " + getAppBinaryName() + " succeed!");

                setMessageDigest(genMessageDigest(HostConfig.getInstance()
                        .getCaseRepository().getApkPath(getAppBinaryName())));

                if (supportsBatchMode()) {
                    mIsInBatchMode = true;
                    Log.d("run in batch mode...");
                    runInBatchMode(javaPkgName);
                    if (!isAllTestsRun()) {
                        mIsInBatchMode = false;
                        Log.d("run in individual mode");
                        runInIndividualMode(javaPkgName);
                    }
                } else {
                    Log.d("run in individual mode...");
                    runInIndividualMode(javaPkgName);
                }
            }

            if (!mTestStop) {
                uninstall();
                if (!TestSession.isADBServerRestartedMode()) {
                    println(PKG_LOG_SEPARATOR);
                }
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }

    /**
     * Set up before running.
     *
     * @param device The device to run this package.getName
     * @param javaPkgName The JAVA package name.
     */
    protected void setup(final TestDevice device, final String javaPkgName) {
        if (!TestSession.isADBServerRestartedMode() || noTestsExecuted()) {
            println(PKG_LOG_SEPARATOR);
            if ((javaPkgName == null) || (javaPkgName.length() == 0)) {
                println("Test package: " + getAppPackageName());
            } else {
                println("Test java package contained in test package "
                        + getAppPackageName() + ": " + javaPkgName);
            }
        }

        mTestStop = false;
        mIsInBatchMode = false;
        mCurrentTest = null;
        mCurrentTestSuite = null;

        setTestDevice(device);
    }

    /**
     * Clean up.
     */
    public void cleanUp() {
        if (mCurrentTestSuite != null) {
            mCurrentTestSuite.setTestStopped(mTestStop);
            mCurrentTestSuite.notifyTestingDeviceDisconnected();
        }

        if (mProgressObserver != null) {
            mProgressObserver.stop();
        }

        if (mTimeOutTimer != null) {
            mTimeOutTimer.cancel(false);
        }
    }

    /**
     * Run the specific test contained in the package over device.
     *
     * @param device The device to run the specific test.
     * @param test The specific test to be run.
     * @param profile The profile of the device being tested.
     */
    public void runTest(final TestDevice device, final Test test)
            throws DeviceDisconnectedException, ADBServerNeedRestartException,
            InvalidApkPathException, InvalidNameSpaceException {

        if (test == null) {
            return;
        }

        mTestStop = false;
        mIsInBatchMode = false;

        println(PKG_LOG_SEPARATOR);
        println("Test package: " + getAppPackageName());
        setTestDevice(device);

        runTestImpl(test);
    }

    /**
     * Implementation of running test.
     *
     * @param test The test to be run.
     * @param profile The profile of the device being tested.
     */
    protected void runTestImpl(final Test test) throws DeviceDisconnectedException,
            ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        try {
            if (!install()) {
                return;
            }

            if (!mTestStop) {
                Log.d("install " + getAppPackageName() + " succeed!");
                mCurrentTestSuite = test.getTestSuite();
                mCurrentTestSuite.run(mDevice, test);
            }

            if (!mTestStop) {
                uninstall();
                println(PKG_LOG_SEPARATOR);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
}
