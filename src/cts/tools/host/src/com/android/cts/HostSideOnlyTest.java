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

import junit.framework.TestResult;

/**
 * Host side only test.
 */
public class HostSideOnlyTest extends Test {
    private HostSideTestRunner mHostSideTestRunner;

    public HostSideOnlyTest(final TestCase parentCase, final String name,
            final String type, final String knownFailure, final int resCode) {

        super(parentCase, name, type, knownFailure, resCode);
        mHostSideTestRunner = null;
    }

    /**
     * The Thread to be run host side unit test.
     */
    class HostSideTestRunner extends Thread {

        private HostSideOnlyTest mTest;

        public HostSideTestRunner(final HostSideOnlyTest test) {
            mTest = test;
        }

        @Override
        public void run() {
            HostUnitTestRunner runner = new HostUnitTestRunner(mTest);
            TestController controller = mTest.getTestController();
            TestResult testResult = null;
            try {
                testResult = runner.runTest(controller.getJarPath(),
                        controller.getPackageName(), controller.getClassName(),
                        controller.getMethodName());
            } catch (IOException e) {
                Log.e("IOException while running test from " +
                      controller.getJarPath(), e);
            } catch (ClassNotFoundException e) {
                Log.e("The host controller JAR (" + controller.getJarPath() +
                        ") file doesn't contain class: "
                        + controller.getPackageName() + "."
                        + controller.getClassName(), e);
            }

            synchronized (mTimeOutTimer) {
                mResult.setResult(testResult);

                if (!mTimeOutTimer.isTimeOut()) {
                    Log.d("HostSideTestRunnerThread() detects that it needs to "
                            + "cancel mTimeOutTimer");
                    mTimeOutTimer.sendNotify();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void runImpl() {
        mHostSideTestRunner = new HostSideTestRunner(this);
        mHostSideTestRunner.start();
    }
}
