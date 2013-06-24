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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.log.LogReceiver.ILogListener;
import com.android.ddmlib.log.LogReceiver.LogEntry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * TestPackage for Reference Application Testing.
 */
public class ReferenceAppTestPackage extends TestPackage {

    private static final String ACTION_REFERENCE_APP_TEST = "ReferenceAppTest";
    private final String apkToTestName;
    private final String packageUnderTest;
    private ArrayList<String> testOutputLines = new ArrayList<String>();

    /**
     * Construct a ReferenceAppTest package with given necessary information.
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
     * @param apkToTestName the apk package that contains the ReferenceApp to be tested.
     * @param packageUnderTest the Java package name of the ReferenceApp to be tested.
     * @throws NoSuchAlgorithmException
     */
    public ReferenceAppTestPackage(String instrumentationRunner,
            String testPkgBinaryName, String targetNameSpace,
            String targetBinaryName, String version,
            String androidVersion, String jarPath,
            String appNameSpace, String appPackageName,
            String apkToTestName, String packageUnderTest) throws NoSuchAlgorithmException {
        super(instrumentationRunner, testPkgBinaryName, targetNameSpace, targetBinaryName, version,
                androidVersion, jarPath, appNameSpace, appPackageName);
        this.apkToTestName = apkToTestName;
        this.packageUnderTest = packageUnderTest;
    }

    /**
     * Run the package over the device.
     *
     * @param device The device to run the package.
     * @param javaPkgName The java package name.
     * @param testSessionLog The TestSessionLog for this TestSession.
     * @throws DeviceDisconnectedException if the device disconnects during the test
     */
    @Override
    public void run(final TestDevice device, final String javaPkgName,
            TestSessionLog testSessionLog) throws DeviceDisconnectedException,
            InvalidApkPathException, InvalidNameSpaceException {
        Test test = getTests().iterator().next();
        if ((test != null) && (test.getResult().isNotExecuted())) {
            String appToTestApkPath =
                HostConfig.getInstance().getCaseRepository().getApkPath(apkToTestName);

            // TODO: This is non-obvious and should be cleaned up
            device.setRuntimeListener(device);

            // Install the Reference App
            device.installAPK(appToTestApkPath);
            device.waitForCommandFinish();

            // Install the Reference App Tests
            String testApkPath = HostConfig.getInstance().getCaseRepository()
                    .getApkPath(getAppBinaryName());
            device.installAPK(testApkPath);
            device.waitForCommandFinish();

            runTests(device, testSessionLog);

            // Uninstall the Reference App Tests
            device.uninstallAPK(getAppPackageName());
            device.waitForCommandFinish();

            // Uninstall the Reference App
            device.uninstallAPK(packageUnderTest);
            device.waitForCommandFinish();

            verifyTestResults(test);
        }
    }

    private void verifyTestResults(Test test) {
        // Now go through the results of the test and see if it ran OK
        boolean testRanOk = false;
        String numberOfTestsRan = "unknown";
        for (String line : testOutputLines) {
            if (line.startsWith("OK")) {
                testRanOk = true;
                int startIndex = 4; // OK (5 tests)
                int endIndex = line.indexOf(' ', 4);
                numberOfTestsRan = line.substring(4, endIndex);
                break;
            }
        }
        if (!testRanOk) {
            test.setResult(new CtsTestResult(CtsTestResult.CODE_FAIL, null, null));
        } else {
            test.setResult(new CtsTestResult(CtsTestResult.CODE_PASS,
                            numberOfTestsRan + " tests passed", null));
        }
    }

    private static final String REF_APP_COMMAND_COMPONENT = "ReferenceAppTestCase";
    private static final String TAKE_SNAPSHOT_CMD = "takeSnapshot";

    /**
     * Run the tests for this test package.
     *
     * @param device the device under test.
     * @param testSessionLog the TestSessionLog for this test
     * @throws DeviceDisconnectedException if the device disconnects.
     */
    private void runTests(final TestDevice device,
            final TestSessionLog testSessionLog) throws DeviceDisconnectedException {
        Log.i("Running reference tests for " + apkToTestName);

        device.addMainLogListener(new ILogListener() {
            public void newData(byte[] data, int offset, int length) {
                // use newEntry instead
            }

            public void newEntry(LogEntry entry) {
                // skip first bytes, its the log level
                String component = "";
                String msg = "";
                for (int i = 1; i < entry.len; i++) {
                    if (entry.data[i] == 0) {
                        component = new String(entry.data, 1, i - 1);
                        msg = new String(entry.data, i + 1, entry.len - i - 2);
                        // clean up any trailing newlines
                        if (msg.endsWith("\n")) {
                            msg = msg.substring(0, msg.length() - 1);
                        }
                        break;
                    }
                }
                if (REF_APP_COMMAND_COMPONENT.equals(component)) {
                    String[] parts = msg.split(":", 2);
                    if (parts == null ||
                        parts.length != 2) {
                        Log.e("Got reference app command component with invalid cmd: " + msg,
                                null);
                        return;
                    }

                    String cmd = parts[0];
                    String cmdArgs = parts[1];
                    if (TAKE_SNAPSHOT_CMD.equals(cmd)) {
                        takeSnapshot(device, testSessionLog, cmdArgs);
                    }
                }
            }

            private void takeSnapshot(TestDevice device,
                                      TestSessionLog testSessionLog,
                                      String cmdArgs) {
                try {
                    RawImage rawImage = device.getScreenshot();
                    if (rawImage != null) {
                    String outputFilename = testSessionLog.getResultDir() +
                        File.separator + cmdArgs + ".png";
                    File output = new File(outputFilename);
                    BufferedImage im = HostUtils.convertRawImageToBufferedImage(rawImage);
                    ImageIO.write(im, "png", output);
                    } else {
                        Log.e("getScreenshot returned a null image", null);
                    }
                } catch (IOException e) {
                    Log.e("Error taking snapshot! " + cmdArgs, e);
                } catch (TimeoutException e) {
                    Log.e("Error taking snapshot! " + cmdArgs, e);
                } catch (AdbCommandRejectedException e) {
                    Log.e("Error taking snapshot! " + cmdArgs, e);
                }
            }
        });

        final String commandStr = "am instrument -w -e package "+ getAppPackageName() + " "
        + getAppPackageName() + "/" + getInstrumentationRunner();
        Log.d(commandStr);

        device.startActionTimer(ACTION_REFERENCE_APP_TEST);
        device.executeShellCommand(commandStr, new ReferenceAppResultsObserver(device));
        device.waitForCommandFinish();
    }

    /**
     * Reference app result observer.
     */
    class ReferenceAppResultsObserver extends MultiLineReceiver {

        private final TestDevice device;

        public ReferenceAppResultsObserver(TestDevice td) {
            this.device = td;
        }

        /** {@inheritDoc} */
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                testOutputLines.add(line);
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
}
