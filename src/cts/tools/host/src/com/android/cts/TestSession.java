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
import java.util.Collection;

/**
 * Represents a runtime session for a test plan, takes charge in running a
 * plan and the setup&tear-downs.
 */
public class TestSession {
    private SessionObserver mSessionObserver;
    private TestSessionLog mSessionLog;
    private TestDevice mDevice;

    private int mId;
    private STATUS mStatus;

    private static int sIdCounter = 0;

    enum STATUS {
        INIT, STARTED, INSTALLING, RUNNING, PAUSED, RESUMED, STOPPED, FINISHED
    }

    private int mRequiredDeviceNumber;
    private boolean mTestStop;
    private TestSessionThread mTestThread;
    private boolean mNeedRestartAdbServer;
    private static boolean mADBServerRestartedMode;

    /** Running count of tests executed since last reboot. */
    private static long mTestCount;

    public TestSession(final TestSessionLog sessionLog,
            final int requiredDeviceNum) {
        mStatus = STATUS.INIT;

        mNeedRestartAdbServer = false;
        mADBServerRestartedMode = false;
        mTestCount = 0;
        mSessionLog = sessionLog;
        mDevice = null;
        mRequiredDeviceNumber = requiredDeviceNum;
        mTestStop = false;
        mId = sIdCounter++;
    }

    /**
     * Get the last session ID.
     *
     * @return The last session ID.
     */
    public static int getLastSessionId() {
        return sIdCounter-1;
    }

    /**
     * Set ADB server restarted mode.
     */
    public static void setADBServerRestartedMode() {
        mADBServerRestartedMode = true;
    }

    /**
     * Reset ADB server restarted mode.
     */
    public static void resetADBServerRestartedMode() {
        mADBServerRestartedMode = false;
    }

    /**
     * Check if it's in ADB server restarted mode.
     *
     * @return If in ADB server restarted mode, return true; else, return false.
     */
    public static boolean isADBServerRestartedMode() {
        return mADBServerRestartedMode;
    }

    /**
     * Increase the test count.
     */
    public static void incTestCount() {
        mTestCount++;
    }

    /**
     * Reset the test count.
     */
    public static void resetTestCount() {
        mTestCount = 0;
    }

    /**
     * Get the test count recently has been run.
     *
     * @return The test count recently has been run.
     */
    public static long getTestCount() {
        return mTestCount;
    }

    /**
     * Check if the test count exceeds the max test count. If the max test count is disabled
     * (HostConfig.getMaxTestCount() <= 0), this method always returns false.
     *
     * @return true, if the max count is enabled and exceeded.
     */
    public static boolean exceedsMaxCount() {
        final long maxTestCount = HostConfig.getMaxTestCount();
        return (maxTestCount > 0) && (mTestCount >= maxTestCount);
    }

    /**
     * Get status.
     *
     * @return The status.
     */
    public STATUS getStatus() {
        return mStatus;
    }

    /**
     * Get device ID.
     *
     * @return device ID.
     */
    public String getDeviceId() {
        if (mDevice == null) {
            return null;
        }
        return mDevice.getSerialNumber();
    }

    /**
     * Get the test device.
     *
     * @return the test device.
     */
    public TestDevice getDevice() {
        return mDevice;
    }

    /**
     * Get the number of required devices.
     *
     * @return The number of required devices.
     */
    public int getNumOfRequiredDevices() {
        return mRequiredDeviceNumber;
    }

    /**
     * Get ID.
     *
     * @return ID.
     */
    public int getId() {
        return mId;
    }

    /**
     * Start the single test with full name.
     *
     * @param testFullName The test full name.
     */
    public void start(final String testFullName) throws TestNotFoundException,
            IllegalTestNameException, ADBServerNeedRestartException {

        if ((testFullName == null) || (testFullName.length() == 0)) {
            throw new IllegalArgumentException();
        }

        // The test full name follows the following rule:
        //     java_package_name.class_name#method_name.
        // Other forms will be treated as illegal.
        if (!testFullName.matches("(\\w+.)+\\w+")) {
            throw new IllegalTestNameException(testFullName);
        }

        Test test = null;
        TestPackage pkg = null;
        if (-1 != testFullName.indexOf(Test.METHOD_SEPARATOR)) {
            test = searchTest(testFullName);
            if (test == null) {
                throw new TestNotFoundException(
                        "The specific test does not exist: " + testFullName);
            }

            mTestThread = new TestSessionThread(this, test);
            CUIOutputStream.println("start test " + testFullName);
        } else {
            pkg = searchTestPackage(testFullName);
            if (pkg == null) {
                throw new TestNotFoundException(
                        "The specific test package does not exist: " + testFullName);
            }

            mTestThread = new TestSessionThread(this, pkg, testFullName);
            CUIOutputStream.println("start java package " + testFullName);
        }

        mStatus = STATUS.STARTED;
        startImpl();
    }

    /**
     * Implement starting/resuming session.
     */
    private void startImpl() throws ADBServerNeedRestartException {
        String resultPath = mSessionLog.getResultPath();
        if ((resultPath == null) || (resultPath.length() == 0)) {
            mSessionLog.setStartTime(System.currentTimeMillis());
        }
        resetTestCount();
        mTestThread.start();
        try {
            mTestThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mNeedRestartAdbServer && HostConfig.getMaxTestCount() > 0) {
            throw new ADBServerNeedRestartException("Need restart ADB server");
        }
    }

    /**
     * Resume the test session.
     */
    public void resume() throws ADBServerNeedRestartException {
        mStatus = STATUS.RESUMED;
        mTestThread = new TestSessionThread(this);
        if (!isADBServerRestartedMode()) {
            CUIOutputStream.println("resume test plan " + getSessionLog().getTestPlanName()
                + " (session id = " + mId + ")");
        }
        startImpl();
    }

    /**
     * Search the test with the test full name given among the test
     * packages contained within this session.
     *
     * @param testFullName The full name of the test.
     * @return The test with the full name given.
     */
    private Test searchTest(final String testFullName) {
        Test test = null;
        for (TestPackage pkg : mSessionLog.getTestPackages()) {
            test = pkg.searchTest(testFullName);
            if (test != null) {
                break;
            }
        }

        return test;
    }

    /**
     * Search the test package with the specified java package name.
     *
     * @param javaPkgName The java package name.
     * @return The test package with the specified java package name.
     */
    private TestPackage searchTestPackage(String javaPkgName) {
        for (TestPackage pkg : mSessionLog.getTestPackages()) {
            Collection<Test> tests = pkg.getTests();
            for (Test test : tests) {
                String testFullName = test.getFullName();
                if (testFullName.startsWith(javaPkgName)) {
                    //adjust the java package name to make it equal to some java package name
                    if (testFullName.charAt(javaPkgName.length()) != '.') {
                        javaPkgName = javaPkgName.substring(0, javaPkgName.lastIndexOf("."));
                    }
                    return pkg;
                }
            }
        }

        return null;
    }
    /**
     * Start a new test session thread to execute the specific test plan.
     */
    public void start() throws ADBServerNeedRestartException {
        mStatus = STATUS.STARTED;
        mSessionLog.setStartTime(System.currentTimeMillis());
        mTestThread = new TestSessionThread(this);

        CUIOutputStream.println("start test plan " + getSessionLog().getTestPlanName());
        startImpl();
    }

    /**
     * Set observer.
     *
     * @param so Session observer.
     */
    public void setObserver(final SessionObserver so) {
        mSessionObserver = so;
    }

    /**
     * Print the message by appending the new line mark.
     *
     * @param msg The message to be print.
     */
    private void println(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.println(msg);
        }
    }

    /**
     * Set the {@link TestDevice} which will run the test.
     *
     * @param device The {@link TestDevice} will run the test.
     */
    public void setTestDevice(final TestDevice device) {
        mDevice = device;
    }

    /**
     * Get the session log of this session.
     *
     * @return The session log of this session.
     */
    public TestSessionLog getSessionLog() {
        return mSessionLog;
    }

    /**
     * Get the test packages contained within this session.
     *
     * @return The test packages contained within this session.
     */
    public Collection<TestPackage> getTestPackages() {
        return mSessionLog.getTestPackages();
    }

    /**
     * The Thread to be run the {@link TestSession}
     */
    class TestSessionThread extends Thread {
        private final int MSEC_PER_SECOND = 1000;

        private TestSession mTestSession;
        private Test mTest;
        private TestPackage mTestPackage;
        private String mJavaPackageName;
        private ResultObserver mResultObserver;

        public TestSessionThread(final TestSession ts) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
        }

        public TestSessionThread(final TestSession ts, final Test test) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
            mTest = test;
        }

        public TestSessionThread(final TestSession ts,
                final TestPackage pkg, final String javaPkgName) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
            mTestPackage = pkg;
            mJavaPackageName = javaPkgName;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            Log.d("Start a test session.");
            mNeedRestartAdbServer = false;
            mResultObserver.setTestSessionLog(getSessionLog());
            mResultObserver.start();

            try {
                if (mTest != null) {
                    TestPackage pkg = mTest.getTestPackage();
                    pkg.setSessionThread(this);
                    pkg.runTest(mDevice, mTest);
                } else if (mTestPackage != null) {
                    mTestPackage.setSessionThread(this);
                    mTestPackage.run(mDevice, mJavaPackageName, mSessionLog);
                } else {
                    for (TestPackage pkg : mSessionLog.getTestPackages()) {
                        if (!pkg.isAllTestsRun()) {
                            pkg.setSessionThread(this);
                            pkg.run(mDevice, null, mSessionLog);
                            if (!isAllTestsRun()) {
                                if (HostConfig.getMaxTestCount() > 0) {
                                    // ADB server restart enabled
                                    markNeedRestartADBServer();
                                    return;
                                }
                            } else {
                                Log.d("All tests have been run.");
                                break;
                            }
                        }
                    }
                    mNeedRestartAdbServer = false;
                    displayTestResultSummary();
                }
            } catch (IOException e) {
                Log.e("Got exception when running the package", e);
            } catch (DeviceDisconnectedException e) {
                Log.e("Device " + e.getMessage() + " disconnected ", null);
            } catch (ADBServerNeedRestartException e) {
                Log.d(e.getMessage());
                if (mTest == null) {
                    markNeedRestartADBServer();
                    return;
                }
            } catch (InvalidApkPathException e) {
                Log.e(e.getMessage(), null);
            } catch (InvalidNameSpaceException e) {
                Log.e(e.getMessage(), null);
            }

            long startTime = getSessionLog().getStartTime().getTime();
            displayTimeInfo(startTime, System.currentTimeMillis());

            mStatus = STATUS.FINISHED;
            mTestSession.getSessionLog().setEndTime(System.currentTimeMillis());
            mSessionObserver.notifyFinished(mTestSession);
            notifyResultObserver();
        }

        /**
         * Mark need restarting ADB server.
         */
        private void markNeedRestartADBServer() {
            Log.d("mark mNeedRestartAdbServer to true");
            mNeedRestartAdbServer = true;
            mStatus = STATUS.FINISHED;
            notifyResultObserver();
            return;
        }

        /**
         * Notify result observer.
         */
        private void notifyResultObserver() {
            mResultObserver.notifyUpdate();
            mResultObserver.finish();
        }

        /**
         * Check if all tests contained in all of the test packages has been run.
         *
         * @return If all tests have been run, return true; else, return false.
         */
        private boolean isAllTestsRun() {
            Collection<TestPackage> pkgs = getTestPackages();
            for (TestPackage pkg : pkgs) {
                if (!pkg.isAllTestsRun()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Display the summary of test result.
         */
        private void displayTestResultSummary() {
            int passNum = mSessionLog.getTestList(CtsTestResult.CODE_PASS).size();
            int failNum = mSessionLog.getTestList(CtsTestResult.CODE_FAIL).size();
            int notExecutedNum = mSessionLog.getTestList(CtsTestResult.CODE_NOT_EXECUTED).size();
            int timeOutNum = mSessionLog.getTestList(CtsTestResult.CODE_TIMEOUT).size();
            int total = passNum + failNum + notExecutedNum + timeOutNum;

            println("Test summary:   pass=" + passNum
                    + "   fail=" + failNum
                    + "   timeOut=" + timeOutNum
                    + "   notExecuted=" + notExecutedNum
                    + "   Total=" + total);
        }

        /**
         * Display the time information of running a test plan.
         *
         * @param startTime start time in milliseconds.
         * @param endTime end time in milliseconds.
         */
        private void displayTimeInfo(final long startTime, final long endTime) {
            long diff = endTime - startTime;
            long seconds = diff / MSEC_PER_SECOND;
            long millisec = diff % MSEC_PER_SECOND;
            println("Time: " + seconds + "." + millisec + "s\n");
        }
    }

    /**
     * Update test result after executing each test.
     * During running test, the process may be interrupted. To avoid
     * test result losing, it's needed to update the test result into
     * xml file after executing each test, which is done by this observer.
     * The possible reasons causing interruption to the process include:
     * <ul>
     *    <li> Device disconnected
     *    <li> Run time exception
     *    <li> System crash
     *    <li> User action to cause the system exit
     * </ul>
     *
     */
   static class ResultObserver {
        static private boolean mFinished = false;
        static private boolean mNotified = false; //used for avoiding race condition
        static private boolean mNeedUpdate = true;
        static private TestSessionLog mSessionLog;
        static final ResultObserver sInstance = new ResultObserver();

        private Observer mObserver;
        /**
         * Get the static instance.
         *
         * @return The static instance.
         */
        public static final ResultObserver getInstance() {
            return sInstance;
        }

        /**
         * Set TestSessionLog.
         *
         * @param log The TestSessionLog.
         */
        public void setTestSessionLog(TestSessionLog log) {
            mSessionLog = log;
        }

        /**
         * Notify this updating thread to update the test result to xml file.
         */
        public void notifyUpdate() {
            if (mObserver != null) {
                synchronized (mObserver) {
                    mNotified = true;
                    mObserver.notify();
                }
            }
        }

        /**
         * Start the observer.
         */
        public void start() {
            mFinished = false;
            mNeedUpdate = true;
            mObserver = new Observer();
            mObserver.start();
        }

        /**
         * Finish updating.
         */
        public void finish() {
            mFinished = true;
            mNeedUpdate = false;
            notifyUpdate();
            try {
                mObserver.join();
                mObserver = null;
            } catch (InterruptedException e) {
            }
        }

        /**
         * Observer which updates the test result to result XML file.
         *
         */
        class Observer extends Thread {

            /** {@inheritDoc} */
            @Override
            public void run() {
                while (!mFinished) {
                    try {
                        synchronized (this) {
                            if ((!mNotified) && (!mFinished)) {
                                wait();
                            }

                            mNotified = false;
                        }

                        if (mNeedUpdate && (mSessionLog != null)) {
                            mSessionLog.sessionComplete();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
