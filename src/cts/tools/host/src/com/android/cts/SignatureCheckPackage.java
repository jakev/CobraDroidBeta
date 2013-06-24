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

import com.android.ddmlib.MultiLineReceiver;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

/**
 * Class which holds signature check package content.
 */
public class SignatureCheckPackage extends TestPackage {

    private static final String CHECK_API_APP_PACKAGE_NAME = "android.tests.sigtest";
    private static final String MISMATCH_CLASS_SIGNATURE = "mismatch_class_signature";
    private static final String MISSING_INTERFACE = "missing_interface";
    private static final String MISSING_FIELD = "missing_field";
    private static final String MISMATCH_INTERFACE_SIGNATURE = "mismatch_interface_signature";
    private static final String MISMATCH_FIELD_SIGNATURE = "mismatch_field_signature";
    private static final String MISSING_METHOD = "missing_method";
    private static final String MISMATCH_METHOD_SIGNATURE = "mismatch_method_signature";
    private static final String MISSING_CLASS = "missing_class";
    private static final String CAUGHT_EXCEPTION = "caught_exception";
    private static final String ACTION_CHECKAPI = "checkapi";

    private ArrayList<String> mAPICheckResult;

    /**
     * Construct a Signature check package with given necessary information.
     *
     * @param instrumentationRunner The instrumentation runner.
     * @param testPkgBinaryName The binary name of the TestPackage.
     * @param targetNameSpace The package name space of the dependent package, if available.
     * @param targetBinaryName The binary name of the dependent package, if available.
     * @param version The version of the CTS Host allowed.
     * @param androidVersion The version of the Android platform allowed.
     * @param jarPath The host controller's jar path and file.
     * @param appNameSpace The package name space used to uninstall the TestPackage.
     * @param appPackageName The Java package name of the test package.
     */
    public SignatureCheckPackage(final String instrumentationRunner,
            final String testPkgBinaryName, final String targetNameSpace,
            final String targetBinaryName, final String version,
            final String androidVersion, final String jarPath, final String appNameSpace,
            final String appPackageName) throws NoSuchAlgorithmException {
        super(instrumentationRunner, testPkgBinaryName, targetNameSpace, targetBinaryName, version,
                androidVersion, jarPath, appNameSpace, appPackageName);
    }

    /**
     * Run the package over the device.
     *
     * @param device The device to run the package.
     * @param javaPkgName The java package name.
     * @param testSesssionLog the TestSessionLog for this TestSession.
     */
    @Override
    public void run(final TestDevice device, final String javaPkgName,
            TestSessionLog testSesssionLog) throws DeviceDisconnectedException,
            InvalidNameSpaceException, InvalidApkPathException {
        Test test = getTests().iterator().next();
        if ((test != null) && (test.getResult().isNotExecuted())) {

            ArrayList<String> result = startSignatureTest(device);
            if (result != null) {
                StringBuffer formattedResult = new StringBuffer();
                int resultCode = processSignatureResult(result, formattedResult);

                String resultStr = formattedResult.toString();
                if (resultCode == CtsTestResult.CODE_PASS) {
                    resultStr = null;
                }

                test.setResult(new CtsTestResult(resultCode, resultStr, null));
            }
        }
    }


    /**
     * Start signature test.
     *
     * @param device the device under test.
     * @return test result
     */
    public ArrayList<String> startSignatureTest(TestDevice device)
                throws DeviceDisconnectedException, InvalidNameSpaceException,
                InvalidApkPathException {
        Log.d("installing APICheck apk");
        mAPICheckResult = new ArrayList<String>();
        // TODO: This is non-obvious and should be cleaned up
        device.setRuntimeListener(device);

        String apkPath =
            HostConfig.getInstance().getCaseRepository().getApkPath(getAppBinaryName());
        if (!HostUtils.isFileExist(apkPath)) {
            Log.e("File doesn't exist: " + apkPath, null);
            return null;
        }

        device.installAPK(apkPath);
        device.waitForCommandFinish();

        runSignatureTestCommand(device);
        device.waitForCommandFinish();

        device.uninstallAPK(CHECK_API_APP_PACKAGE_NAME);
        device.waitForCommandFinish();

        device.removeRuntimeListener();

        return mAPICheckResult;
    }

    /**
     * Run signature test command.
     *
     * @param device the device to run against.
     */
    private void runSignatureTestCommand(TestDevice device)
                throws DeviceDisconnectedException {
        Log.i("Checking API... ");
        Log.i("This might take several minutes, please be patient...");

        final String commandStr = "am instrument -w -e bundle true "
            + getAppPackageName() + "/" + getInstrumentationRunner();
        Log.d(commandStr);

        device.startActionTimer(ACTION_CHECKAPI,
                HostConfig.Ints.signatureTestTimeoutMs.value());
        device.executeShellCommand(commandStr, new SignatureTestResultObserver(device));
    }

    /**
     * Signature result observer.
     */
    class SignatureTestResultObserver extends MultiLineReceiver {

        private final TestDevice device;

        public SignatureTestResultObserver(TestDevice td) {
            this.device = td;
        }

        /** {@inheritDoc} */
        @Override
        public void processNewLines(String[] lines) {
            for (int i = 0; i < lines.length; i++) {
                mAPICheckResult.add(lines[i]);
            }
        }

        /** {@inheritDoc} */
        public boolean isCancelled() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public void done() {
            device.stopActionTimer();
            device.notifyExternalTestComplete();
        }
    }

    /**
     * Process signature result.
     *
     * @param apiCheckResult The result of API checking.
     * @param formattedResult The formated result of API checking.
     * @return  PASS or FAIL according to the check result.
     */
    public int processSignatureResult(ArrayList<String> apiCheckResult,
                StringBuffer formattedResult) {

        HashMap<String, String> resMap = new HashMap<String, String>();
        for (String res : apiCheckResult) {
            Matcher matcher = TestDevice.INSTRUMENT_RESULT_PATTERN.matcher(res);
            if (matcher.matches()) {
                resMap.put(matcher.group(1), matcher.group(2));
            }
        }

        String result = resMap.get("result");
        if (result == null) {
            CUIOutputStream.println("API Check TIMEOUT.");
            return CtsTestResult.CODE_TIMEOUT;
        }

        if (result.equals("true")) {
            CUIOutputStream.println("API Check PASS.");
            return CtsTestResult.CODE_PASS;
        }

        CUIOutputStream.println("API Check FAIL!");
        final String leadingSpace = "      ";
        for (String key : resMap.keySet()) {
            if (key.equals("result")) {
                // ignore the result string
                continue;
            }

            String resStr = resMap.get(key);
            if ((resStr != null) && (resStr.length() > 2)) {
                formattedResult.append(key +":\n");
                if (MISMATCH_CLASS_SIGNATURE.equals(key)
                        || MISMATCH_INTERFACE_SIGNATURE.equals(key)
                        || MISMATCH_FIELD_SIGNATURE.equals(key)
                        || MISSING_FIELD.equals(key)
                        || MISSING_METHOD.equals(key)
                        || MISMATCH_METHOD_SIGNATURE.equals(key)) {
                    resStr = resStr.substring(1, resStr.length() - 1);
                    String[] details = resStr.split("\\), ");
                    for (String detail : details) {
                        formattedResult.append(leadingSpace + detail + ")\n");
                    }
                    formattedResult.append("\n");
                } else if (MISSING_INTERFACE.equals(key)
                        || MISSING_CLASS.equals(key)) {
                    resStr = resStr.substring(1, resStr.length() - 1);
                    String[] details = resStr.split(", ");
                    for (String detail : details) {
                        formattedResult.append(leadingSpace + detail + "\n");
                    }
                    formattedResult.append("\n");
                } else if (CAUGHT_EXCEPTION.equals(key)) {
                    resStr = resStr.substring(1, resStr.length() - 1);
                    formattedResult.append(resStr);
                    formattedResult.append("\n");
                }
            }
        }

        return CtsTestResult.CODE_FAIL;
    }
}

