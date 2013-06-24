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

import com.android.cts.TestSession.ResultObserver;

import java.util.TimerTask;

/**
 * Correspond to junit's test method, provide functions on storing
 * and executing a test from CTS test harness.
 */
public class Test implements DeviceObserver {
    public static final String METHOD_SEPARATOR = "#";

    private TestController mTestController;
    private TestCase mParentCase;
    private String mName;
    private String mType;
    private String mKnownFailure;
    private long mStartTime;
    private long mEndTime;

    protected boolean mTestStop;
    protected TestDevice mDevice;
    protected HostTimer mTimeOutTimer;
    protected ProgressObserver mProgressObserver;
    protected CtsTestResult mResult;

    public Test(final TestCase parentCase, final String name,
            final String type, final String knownFailure, final int resCode) {
        mParentCase = parentCase;
        mName = name;
        mType = type;
        mKnownFailure = knownFailure;
        mResult = new CtsTestResult(resCode);

        mTestController = null;
        mProgressObserver = null;
        mTestStop = false;
    }

    /**
     * Check if it's known failure test.
     *
     * @return If known failure test, return true; else, return false.
     */
    public boolean isKnownFailure() {
        return (mKnownFailure != null);
    }

    /**
     * Get the known failure description.
     *
     * @return The known failure description.
     */
    public String getKnownFailure() {
        return mKnownFailure;
    }

    /**
     * Set the test controller.
     *
     * @param testController The test controller.
     */
    public void setTestController(final TestController testController) {
        mTestController = testController;
    }

    /**
     * Get the test controller.
     *
     * @return The test controller.
     */
    public TestController getTestController() {
        return mTestController;
    }

    /**
     * Get the instrumentation runner.
     *
     * @return The instrumentation runner.
     */
    public String getInstrumentationRunner() {
        TestPackage pkg = mParentCase.getParent().getParent();
        return pkg.getInstrumentationRunner();
    }

    /**
     * Get the test name of this test.
     *
     * @return The test name of this test.
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the test type of this test.
     *
     * @return The test type of this test.
     */
    public String getType() {
        return mType;
    }

    /**
     * Get the parent TestCase containing the test.
     *
     * @return The parent TestCase.
     */
    public TestCase getTestCase() {
        return mParentCase;
    }

    /**
     * Get the parent TestSuite containing the test.
     *
     * @return The parent TestSuite.
     */
    public TestSuite getTestSuite() {
        return mParentCase.getParent();
    }

    /**
     * Get the parent TestPackage containing the test.
     *
     * @return The parent TestPackage.
     */
    public TestPackage getTestPackage() {
        return mParentCase.getParent().getParent();
    }

    /**
     * Get the app package name space of this test.
     *
     * @return The app package name space of this test.
     */
    public String getAppNameSpace() {
        TestPackage pkg = mParentCase.getParent().getParent();
        return pkg.getAppNameSpace();
    }

    /**
     * Get the full name of this test.
     *
     * @return The full name of this test.
     */
    public String getFullName() {
        TestSuite suite = mParentCase.getParent();
        return suite.getFullName() + "." + mParentCase.getName()
                + METHOD_SEPARATOR + mName;
    }

    /**
     * Set test result.
     *
     * @param result The result.
     */
    public void setResult(CtsTestResult result) {
        if (isKnownFailure()) {
            result.reverse();
        }
        mResult = result;
        CUIOutputStream.println("(" + mResult.getResultString() + ")");
        if (!mResult.isPass()) {
            String failedMessage = result.getFailedMessage();
            String stackTrace = result.getStackTrace();
            if (failedMessage != null) {
                CUIOutputStream.println(failedMessage);
            }
            if (stackTrace != null) {
                CUIOutputStream.println(stackTrace);
            }
        }
        setEndTime(System.currentTimeMillis());

        ResultObserver.getInstance().notifyUpdate();
    }

    /**
     * Add test result.
     *
     * @param result The result.
     */
    public void addResult(CtsTestResult result) {
        if (isKnownFailure()) {
            result.reverse();
        }
        mResult = result;
    }

    /**
     * Get the result.
     *
     * @return the result.
     */
    public CtsTestResult getResult() {
        return mResult;
    }

    /**
     * Set start Test time.
     *
     * @param time The start time.
     */
    public void setStartTime(final long time) {
        mStartTime = time;
    }

    /**
     * Set end Test time.
     *
     * @param time The end time.
     */
    public void setEndTime(final long time) {
        mEndTime = time;
    }

    /**
     * Get Test start time.
     *
     * @return The start time.
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Get Test end time.
     *
     * @return The end time.
     */
    public long getEndTime() {
        return mEndTime;
    }

    /**
     * Print the message without appending the new line mark.
     *
     * @param msg the message to be print.
     */
    protected void print(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.print(msg);
        }
    }

    /**
     * The timer task which aids in guarding the running test
     * with the guarding timer. If the executing of the test
     * is not finished, and the guarding timer is expired,
     * this task will be executed to force the finish of the
     * running test.
     */
    class TimeOutTask extends TimerTask {
        private Test mTest;

        public TimeOutTask(final Test testResult) {
            mTest = testResult;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            mProgressObserver.stop();
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.cancel(true);
                mTimeOutTimer.sendNotify();
            }

            Log.d("mTimeOutTimer timed out");

            if (!mTestStop) {
                mTest.setResult(
                        new CtsTestResult(CtsTestResult.CODE_TIMEOUT, null, null));
            }

            killDeviceProcess(mTest.getAppNameSpace());
        }
    }

    /**
     * Kill the device process.
     *
     * @param packageName The package name.
     */
    private void killDeviceProcess(final String packageName) {
        mDevice.killProcess(packageName);
    }

    /**
     * Set test stopped.
     *
     * @param testStopped If true, it's stopped. Else, still running.
     */
    public void setTestStopped(final boolean testStopped) {
        mTestStop = testStopped;
    }

    /**
     * Run the test over device given.
     *
     * @param device the device to run the test.
     */
    public void run(final TestDevice device) throws DeviceDisconnectedException,
            ADBServerNeedRestartException {

        if ((getName() == null) || (getName().length() == 0)) {
            return;
        }

        if (TestSession.exceedsMaxCount()) {
            throw new ADBServerNeedRestartException("Test count reached overflow point");
        } else {
            TestSession.incTestCount();
        }

        mTestStop = false;
        mDevice = device;
        mTimeOutTimer = new HostTimer(new TimeOutTask(this),
                HostConfig.Ints.individualStartTimeoutMs.value());
        mTimeOutTimer.start();
        mProgressObserver = new ProgressObserver();
        mProgressObserver.start();

        setStartTime(System.currentTimeMillis());
        String testFullName = getFullName();
        print(testFullName + "...");

        runImpl();

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
                //not caused by timer timing out
                //need to cancel timer
                mTimeOutTimer.cancel(false);
            }
        }

        setResult(mResult);
    }

    /**
     * Implementation of running test.
     */
    protected void runImpl() throws DeviceDisconnectedException {
        mDevice.runTest(this);
    }

    /**
     * Notify the result.
     *
     * @param result The result.
     */
    public void notifyResult(CtsTestResult result) {

        Log.d("Test.notifyResult() is called. (Test.getFullName()=" + getFullName());
        mResult = result;
        if (mTimeOutTimer != null) {
            synchronized (mTimeOutTimer) {
                // set result again in case timeout just happened
                mResult = result;
                Log.d("notifyUpdateResult() detects that it needs to cancel mTimeOutTimer");
                if (mTimeOutTimer != null) {
                    mTimeOutTimer.sendNotify();
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void notifyInstallingComplete(final int resultCode) {
    }

    /** {@inheritDoc} */
    public void notifyUninstallingComplete(final int resultCode) {
    }

    /** {@inheritDoc} */
    public void notifyInstallingTimeout(final TestDevice testDevice) {
    }

    /** {@inheritDoc} */
    public void notifyUninstallingTimeout(final TestDevice testDevice) {
    }

    /** {@inheritDoc} */
    public void notifyTestingDeviceDisconnected() {
        Log.d("Test.notifyTestingDeviceDisconnected() is called");
        if (mProgressObserver != null) {
            mProgressObserver.stop();
        }

        if (mTimeOutTimer != null) {
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.cancel(false);
                mTimeOutTimer.sendNotify();
            }
        }
    }
}

