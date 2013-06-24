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

package com.android.cts.appsecurity;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.Log;
import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.hosttest.DeviceTestCase;
import com.android.hosttest.DeviceTestSuite;

/**
 * Set of tests that verify various security checks involving multiple apps are properly enforced.
 */
public class AppSecurityTests extends DeviceTestCase {

    // testSharedUidDifferentCerts constants
    private static final String SHARED_UI_APK = "CtsSharedUidInstall.apk";
    private static final String SHARED_UI_PKG = "com.android.cts.shareuidinstall";
    private static final String SHARED_UI_DIFF_CERT_APK = "CtsSharedUidInstallDiffCert.apk";
    private static final String SHARED_UI_DIFF_CERT_PKG =
        "com.android.cts.shareuidinstalldiffcert";

    // testAppUpgradeDifferentCerts constants
    private static final String SIMPLE_APP_APK = "CtsSimpleAppInstall.apk";
    private static final String SIMPLE_APP_PKG = "com.android.cts.simpleappinstall";
    private static final String SIMPLE_APP_DIFF_CERT_APK = "CtsSimpleAppInstallDiffCert.apk";

    // testAppFailAccessPrivateData constants
    private static final String APP_WITH_DATA_APK = "CtsAppWithData.apk";
    private static final String APP_WITH_DATA_PKG = "com.android.cts.appwithdata";
    private static final String APP_WITH_DATA_CLASS =
            "com.android.cts.appwithdata.CreatePrivateDataTest";
    private static final String APP_WITH_DATA_CREATE_METHOD =
            "testCreatePrivateData";
    private static final String APP_WITH_DATA_CHECK_NOEXIST_METHOD =
            "testEnsurePrivateDataNotExist";
    private static final String APP_ACCESS_DATA_APK = "CtsAppAccessData.apk";
    private static final String APP_ACCESS_DATA_PKG = "com.android.cts.appaccessdata";

    // testInstrumentationDiffCert constants
    private static final String TARGET_INSTRUMENT_APK = "CtsTargetInstrumentationApp.apk";
    private static final String TARGET_INSTRUMENT_PKG = "com.android.cts.targetinstrumentationapp";
    private static final String INSTRUMENT_DIFF_CERT_APK = "CtsInstrumentationAppDiffCert.apk";
    private static final String INSTRUMENT_DIFF_CERT_PKG =
        "com.android.cts.instrumentationdiffcertapp";

    // testPermissionDiffCert constants
    private static final String DECLARE_PERMISSION_APK = "CtsPermissionDeclareApp.apk";
    private static final String DECLARE_PERMISSION_PKG = "com.android.cts.permissiondeclareapp";
    private static final String PERMISSION_DIFF_CERT_APK = "CtsUsePermissionDiffCert.apk";
    private static final String PERMISSION_DIFF_CERT_PKG =
        "com.android.cts.usespermissiondiffcertapp";

    private static final String LOG_TAG = "AppSecurityTests";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // ensure apk path has been set before test is run
        assertNotNull(getTestAppPath());
    }

    /**
     * Test that an app that declares the same shared uid as an existing app, cannot be installed
     * if it is signed with a different certificate.
     */
    public void testSharedUidDifferentCerts() throws InstallException {
        Log.i(LOG_TAG, "installing apks with shared uid, but different certs");
        try {
            // cleanup test apps that might be installed from previous partial test run
            getDevice().uninstallPackage(SHARED_UI_PKG);
            getDevice().uninstallPackage(SHARED_UI_DIFF_CERT_PKG);

            String installResult = getDevice().installPackage(getTestAppFilePath(SHARED_UI_APK),
                    false);
            assertNull("failed to install shared uid app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(SHARED_UI_DIFF_CERT_APK),
                    false);
            assertNotNull("shared uid app with different cert than existing app installed " +
                    "successfully", installResult);
            assertEquals("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE", installResult);
        }
        finally {
            getDevice().uninstallPackage(SHARED_UI_PKG);
            getDevice().uninstallPackage(SHARED_UI_DIFF_CERT_PKG);
        }
    }

    /**
     * Test that an app update cannot be installed over an existing app if it has a different
     * certificate.
     */
    public void testAppUpgradeDifferentCerts() throws InstallException {
        Log.i(LOG_TAG, "installing app upgrade with different certs");
        try {
            // cleanup test app that might be installed from previous partial test run
            getDevice().uninstallPackage(SIMPLE_APP_PKG);

            String installResult = getDevice().installPackage(getTestAppFilePath(SIMPLE_APP_APK),
                    false);
            assertNull("failed to install simple app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(SIMPLE_APP_DIFF_CERT_APK),
                    true /* reinstall */);
            assertNotNull("app upgrade with different cert than existing app installed " +
                    "successfully", installResult);
            assertEquals("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES", installResult);
        }
        finally {
            getDevice().uninstallPackage(SIMPLE_APP_PKG);
        }
    }

    /**
     * Test that an app cannot access another app's private data.
     */
    public void testAppFailAccessPrivateData() throws InstallException, TimeoutException,
            AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        Log.i(LOG_TAG, "installing app that attempts to access another app's private data");
        try {
            // cleanup test app that might be installed from previous partial test run
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);
            getDevice().uninstallPackage(APP_ACCESS_DATA_PKG);

            String installResult = getDevice().installPackage(getTestAppFilePath(APP_WITH_DATA_APK),
                    false);
            assertNull("failed to install app with data", installResult);
            // run appwithdata's tests to create private data
            assertTrue("failed to create app's private data", runDeviceTests(APP_WITH_DATA_PKG,
                    APP_WITH_DATA_CLASS, APP_WITH_DATA_CREATE_METHOD));

            installResult = getDevice().installPackage(getTestAppFilePath(APP_ACCESS_DATA_APK),
                    false);
            assertNull("failed to install app access data", installResult);
            // run appaccessdata's tests which attempt to access appwithdata's private data
            assertTrue("could access app's private data", runDeviceTests(APP_ACCESS_DATA_PKG));
        }
        finally {
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);
            getDevice().uninstallPackage(APP_ACCESS_DATA_PKG);
        }
    }

    /**
     * Test that uninstall of an app removes its private data.
     */
    public void testUninstallRemovesData() throws Exception {
        Log.i(LOG_TAG, "Uninstalling app, verifying data is removed.");
        try {
            // cleanup test app that might be installed from previous partial test run
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);

            String installResult = getDevice().installPackage(getTestAppFilePath(APP_WITH_DATA_APK),
                    false);
            assertNull("failed to install app with data", installResult);
            // run appwithdata's tests to create private data
            assertTrue("failed to create app's private data", runDeviceTests(APP_WITH_DATA_PKG,
                    APP_WITH_DATA_CLASS, APP_WITH_DATA_CREATE_METHOD));

            getDevice().uninstallPackage(APP_WITH_DATA_PKG);

            installResult = getDevice().installPackage(getTestAppFilePath(APP_WITH_DATA_APK),
                    false);
            assertNull("failed to install app with data second time", installResult);
            // run appwithdata's 'check if file exists' test
            assertTrue("app's private data still exists after install", runDeviceTests(
                    APP_WITH_DATA_PKG, APP_WITH_DATA_CLASS, APP_WITH_DATA_CHECK_NOEXIST_METHOD));

        }
        finally {
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);
        }
    }

    /**
     * Test that an app cannot instrument another app that is signed with different certificate.
     */
    public void testInstrumentationDiffCert() throws InstallException, TimeoutException,
            AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        Log.i(LOG_TAG, "installing app that attempts to instrument another app");
        try {
            // cleanup test app that might be installed from previous partial test run
            getDevice().uninstallPackage(TARGET_INSTRUMENT_PKG);
            getDevice().uninstallPackage(INSTRUMENT_DIFF_CERT_PKG);

            String installResult = getDevice().installPackage(
                    getTestAppFilePath(TARGET_INSTRUMENT_APK), false);
            assertNull("failed to install target instrumentation app", installResult);

            // the app will install, but will get error at runtime when starting instrumentation
            installResult = getDevice().installPackage(getTestAppFilePath(INSTRUMENT_DIFF_CERT_APK),
                    false);
            assertNull("failed to install instrumentation app with diff cert", installResult);
            // run INSTRUMENT_DIFF_CERT_PKG tests
            // this test will attempt to call startInstrumentation directly and verify
            // SecurityException is thrown 
            assertTrue("running instrumentation with diff cert unexpectedly succeeded",
                    runDeviceTests(INSTRUMENT_DIFF_CERT_PKG));
        }
        finally {
            getDevice().uninstallPackage(TARGET_INSTRUMENT_PKG);
            getDevice().uninstallPackage(INSTRUMENT_DIFF_CERT_PKG);
        }
    }

    /**
     * Test that an app cannot use a signature-enforced permission if it is signed with a different
     * certificate than the app that declared the permission.
     */
    public void testPermissionDiffCert() throws InstallException, TimeoutException,
            AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        Log.i(LOG_TAG, "installing app that attempts to use permission of another app");
        try {
            // cleanup test app that might be installed from previous partial test run
            getDevice().uninstallPackage(DECLARE_PERMISSION_PKG);
            getDevice().uninstallPackage(PERMISSION_DIFF_CERT_PKG);

            String installResult = getDevice().installPackage(
                    getTestAppFilePath(DECLARE_PERMISSION_APK), false);
            assertNull("failed to install declare permission app", installResult);

            // the app will install, but will get error at runtime
            installResult = getDevice().installPackage(getTestAppFilePath(PERMISSION_DIFF_CERT_APK),
                    false);
            assertNull("failed to install permission app with diff cert", installResult);
            // run PERMISSION_DIFF_CERT_PKG tests which try to access the permission
            assertTrue("unexpected result when running permission tests",
                    runDeviceTests(PERMISSION_DIFF_CERT_PKG));
        }
        finally {
            getDevice().uninstallPackage(DECLARE_PERMISSION_PKG);
            getDevice().uninstallPackage(PERMISSION_DIFF_CERT_PKG);
        }
    }

    /**
     * Get the absolute file system location of test app with given filename
     * @param fileName the file name of the test app apk
     * @return {@link String} of absolute file path
     */
    private String getTestAppFilePath(String fileName) {
        return String.format("%s%s%s", getTestAppPath(), File.separator, fileName);
    }

    /**
     * Helper method that will the specified packages tests on device.
     *
     * @param pkgName Android application package for tests
     * @return <code>true</code> if all tests passed.
     * @throws TimeoutException in case of a timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws ShellCommandUnresponsiveException if the device did not output any test result for
     * a period longer than the max time to output.
     * @throws IOException if connection to device was lost.
     */
    private boolean runDeviceTests(String pkgName) throws AdbCommandRejectedException,
            ShellCommandUnresponsiveException, IOException, TimeoutException {
    	return runDeviceTests(pkgName, null, null);
    }

    /**
     * Helper method that will the specified packages tests on device.
     *
     * @param pkgName Android application package for tests
     * @return <code>true</code> if all tests passed.
     */
    private boolean runDeviceTests(String pkgName, String testClassName, String testMethodName)
            throws AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException,
                   TimeoutException {
        CollectingTestRunListener listener = doRunTests(pkgName, testClassName, testMethodName);
        return listener.didAllTestsPass();
    }

    /**
     * Helper method to run tests and return the listener that collected the results.
     * @param pkgName Android application package for tests
     * @return the {@link CollectingTestRunListener}
     * @throws TimeoutException in case of a timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws ShellCommandUnresponsiveException if the device did not output any test result for
     * a period longer than the max time to output.
     * @throws IOException if connection to device was lost.
     */
    private CollectingTestRunListener doRunTests(String pkgName, String testClassName,
            String testMethodName) throws AdbCommandRejectedException, IOException,
                    ShellCommandUnresponsiveException, TimeoutException {
        RemoteAndroidTestRunner testRunner = new RemoteAndroidTestRunner(pkgName, getDevice());
        if (testClassName != null && testMethodName != null) {
            testRunner.setMethodName(testClassName, testMethodName);
        }
        CollectingTestRunListener listener = new CollectingTestRunListener();
        testRunner.run(listener);
        return listener;
    }

    private static class CollectingTestRunListener implements ITestRunListener {

        private boolean mAllTestsPassed = true;
        private String mTestRunErrorMessage = null;

        public void testEnded(TestIdentifier test,  Map<String, String> metrics) {
            // ignore
        }

        public void testFailed(TestFailure status, TestIdentifier test,
                String trace) {
            Log.logAndDisplay(LogLevel.WARN, LOG_TAG, String.format("%s#%s failed: %s",
                    test.getClassName(),
                    test.getTestName(), trace));
            mAllTestsPassed = false;
        }

        public void testRunEnded(long elapsedTime, Map<String, String> resultBundle) {
            // ignore
        }

        public void testRunFailed(String errorMessage) {
            Log.logAndDisplay(LogLevel.WARN, LOG_TAG, String.format("test run failed: %s",
                    errorMessage));
            mAllTestsPassed = false;
            mTestRunErrorMessage = errorMessage;
        }

        public void testRunStarted(String runName, int testCount) {
            // ignore
        }

        public void testRunStopped(long elapsedTime) {
            // ignore
        }

        public void testStarted(TestIdentifier test) {
            // ignore
        }

        boolean didAllTestsPass() {
            return mAllTestsPassed;
        }

        /**
         * Get the test run failure error message.
         * @return the test run failure error message or <code>null</code> if test run completed.
         */
        String getTestRunErrorMessage() {
            return mTestRunErrorMessage;
        }
    }

    public static Test suite() {
        return new DeviceTestSuite(AppSecurityTests.class);
    }
}
