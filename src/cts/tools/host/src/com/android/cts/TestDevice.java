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
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.SyncService.ISyncProgressMonitor;
import com.android.ddmlib.log.LogReceiver;
import com.android.ddmlib.log.LogReceiver.ILogListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manage the testing target device for<br>
 * <ul>
 *    <li> install/uninstall test package, and
 *    <li> execute command on device
 *    <li> get command feedback from standard output
 * </ul>
 */
public class TestDevice implements DeviceObserver {
    private static final String DEVICE_SETUP_APK = "TestDeviceSetup";
    private static final String DEVICE_SETUP_APP_PACKAGE_NAME = "android.tests.devicesetup";
    private static final String DEFAULT_TEST_RUNNER_NAME =
                                  "android.test.InstrumentationTestRunner";
    private static final String ACTION_INSTALL = "install";
    private static final String ACTION_UNINSTALL = "uninstall";
    private static final String ACTION_GET_DEV_INFO = "getDeviceInfo";
    private static final String sInstrumentResultExpr = "INSTRUMENTATION_RESULT: (\\S+)=(.+)";

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_BUSY = STATUS_IDLE + 1;
    public static final int STATUS_OFFLINE = STATUS_IDLE + 2;
    private static final String STATUS_STR_IDLE = "idle";
    private static final String STATUS_STR_IN_USE = "in use";
    private static final String STATUS_STR_OFFLINE = "offline";

    /** Interval [ms] for polling a device until boot is completed. */
    private static final int REBOOT_POLL_INTERVAL = 5000;
    /** Number of times a booting device should be polled before we give up. */
    private static final int REBOOT_POLL_COUNT = 10 * 60 * 1000 / REBOOT_POLL_INTERVAL;
    /** Max time [ms] to wait for <code>adb shell getprop</code> to return a result. */
    private static final int GETPROP_TIMEOUT = 5000;

    public static final Pattern INSTRUMENT_RESULT_PATTERN;

    private BatchModeResultParser mBatchModeResultParser;

    private DeviceObserver mDeviceObserver;
    private IDevice mDevice;
    private DeviceParameterCollector mDeviceInfo;

    private SyncService mSyncService;

    private PackageActionObserver mUninstallObserver;

    private int mStatus;
    private static HashMap<Integer, String> mStatusMap;
    private PackageActionTimer mPackageActionTimer;

    private ObjectSync mObjectSync;

    private MultiplexingLogListener logListener = new MultiplexingLogListener();
    private LogReceiver logReceiver = new LogReceiver(logListener);

    private class LogServiceThread extends Thread {
        @Override
        public void run() {
            try {
                mDevice.runLogService("main", logReceiver);
            } catch (IOException e) {
            } catch (TimeoutException e) {
            } catch (AdbCommandRejectedException e) {
            }
        }

        /**
         * Cancel logging and exit this thread.
         */
        public void cancelLogService() {
            // this will cause the loop in our run method to
            // exit, terminating this thread.
            logReceiver.cancel();
        }
    }

    private LogServiceThread logServiceThread;

    static {
        INSTRUMENT_RESULT_PATTERN = Pattern.compile(sInstrumentResultExpr);
        mStatusMap = new HashMap<Integer, String>();
        mStatusMap.put(STATUS_IDLE, STATUS_STR_IDLE);
        mStatusMap.put(STATUS_BUSY, STATUS_STR_IN_USE);
        mStatusMap.put(STATUS_OFFLINE, STATUS_STR_OFFLINE);
    }

    // This constructor just for unit test
    TestDevice(final String serialNumber) {
        mDeviceInfo = new DeviceParameterCollector();
        mDeviceInfo.setSerialNumber(serialNumber);
    }

    public TestDevice(IDevice device) {
        mDevice = device;
        try {
            mSyncService = mDevice.getSyncService();
        } catch (IOException e) {
            // FIXME: handle failed connection.
        } catch (TimeoutException e) {
            // FIXME: handle failed connection.
        } catch (AdbCommandRejectedException e) {
            // FIXME: handle failed connection.
        }
        mBatchModeResultParser = null;
        mUninstallObserver = new PackageActionObserver(ACTION_UNINSTALL);
        mStatus = STATUS_IDLE;
        mDeviceInfo = new DeviceParameterCollector();
        mPackageActionTimer = new PackageActionTimer();
        mObjectSync = new ObjectSync();
    }

    /**
     * Gets this device's information.
     *
     * Assumes that the test device setup apk is already installed.
     * See {@link #installDeviceSetupApp()}.
     *
     * @return information of this device.
     */
    public DeviceParameterCollector getDeviceInfo()
                throws DeviceDisconnectedException, InvalidNameSpaceException,
                InvalidApkPathException {
        if (mDeviceInfo.size() == 0) {
            logServiceThread = new LogServiceThread();
            logServiceThread.start();
            genDeviceInfo();
        }
        return mDeviceInfo;
    }

    /**
     * Attempt to disable the screen guard on device.
     *
     * Assumes the test device setup apk is already installed.
     * See {@link #installDeviceSetupApp()}.
     *
     * Note: uninstalling the device setup app {@link #uninstallDeviceSetupApp()} will re-enable
     * keyguard.
     *
     * @throws DeviceDisconnectedException
     */
    public void disableKeyguard () throws DeviceDisconnectedException {
        final String commandStr = "am broadcast -a android.tests.util.disablekeyguard";
        Log.d(commandStr);

        executeShellCommand(commandStr, new NullOutputReceiver());
    }

    /**
     * Return the Device instance associated with this TestDevice.
     */
    public IDevice getDevice() {
        return mDevice;
    }

    class RestartPropReceiver extends MultiLineReceiver {
        private boolean mRestarted;
        private boolean mCancelled;
        private boolean mDone;

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.trim().equals("1")) {
                    mRestarted = true;
                }
            }
        }
        @Override
        public void done() {
            synchronized(this) {
                mDone = true;
                this.notifyAll();
            }
        }

        public boolean isCancelled() {
            return mCancelled;
        }

        boolean hasRestarted(long timeout) {
            try {
                synchronized (this) {
                    if (!mDone) {
                        this.wait(timeout);
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            }
            mCancelled = true;
            return mRestarted;
        }
    }

    /**
     * Wait until device indicates that boot is complete.
     *
     * @return true if the device has completed the boot process, false if it does not, or the
     * device does not respond.
     */
    public boolean waitForBootComplete() throws DeviceDisconnectedException {
        Log.d("probe device status...");

        mDeviceInfo.set(DeviceParameterCollector.SERIAL_NUMBER, getSerialNumber());
        mObjectSync = new ObjectSync();

        // reset device observer
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;

        int retries = 0;
        boolean success = false;
        while (!success && (retries < REBOOT_POLL_COUNT)) {
            Log.d("Waiting for device to complete boot");
            RestartPropReceiver rpr = new RestartPropReceiver();
            this.executeShellCommand("getprop dev.bootcomplete", rpr);
            success = rpr.hasRestarted(GETPROP_TIMEOUT);
            if (!success) {
                try {
                    Thread.sleep(REBOOT_POLL_INTERVAL);
                } catch (InterruptedException e) {
                    // ignore and retry
                }
                retries += 1;
            }
        }
        mDeviceObserver = tmpDeviceObserver;
        if (success) {
            Log.d("Device boot complete");
        }
        return success;
    }

    /**
     * Run device information collector command to got the device info.
     */
    private void genDeviceInfo() throws DeviceDisconnectedException,
                InvalidNameSpaceException, InvalidApkPathException {
        mDeviceInfo.set(DeviceParameterCollector.SERIAL_NUMBER, getSerialNumber());
        // run shell command to run device information collector
        Log.d("run device information collector");
        runDeviceInfoCollectorCommand();
        waitForCommandFinish();
    }

    /**
     * Uninstall the device setup apk from device.
     *
     * See {@link #installDeviceSetupApp}
     *
     * @throws DeviceDisconnectedException
     * @throws InvalidNameSpaceException
     */
    public void uninstallDeviceSetupApp() throws DeviceDisconnectedException,
            InvalidNameSpaceException {
        // reset device observer
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;
        Log.d("uninstall get info ...");
        uninstallAPK(DEVICE_SETUP_APP_PACKAGE_NAME);
        waitForCommandFinish();
        Log.d("uninstall device information collector successfully");
        mDeviceObserver = tmpDeviceObserver;
    }

    /**
     * Install the device setup apk on the device.
     *
     * @throws DeviceDisconnectedException
     * @throws InvalidApkPathException
     */
    public void installDeviceSetupApp() throws DeviceDisconnectedException, InvalidApkPathException {
        String apkPath = HostConfig.getInstance().getCaseRepository().getApkPath(DEVICE_SETUP_APK);
        if (!HostUtils.isFileExist(apkPath)) {
            Log.e("File doesn't exist: " + apkPath, null);
            return;
        }

        Log.d("installing " + DEVICE_SETUP_APK + " apk");
        mObjectSync = new ObjectSync();

        // reset device observer
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;

        Log.d("install get info ...");
        installAPK(apkPath);
        waitForCommandFinish();
        mDeviceObserver = tmpDeviceObserver;
    }

    /**
     * Run command to collect device info.
     */
    private void runDeviceInfoCollectorCommand() throws DeviceDisconnectedException {
        final String commandStr = "am instrument -w -e bundle true "
            + String.format("%s/android.tests.getinfo.DeviceInfoInstrument",
                    DEVICE_SETUP_APP_PACKAGE_NAME);
        Log.d(commandStr);

        mPackageActionTimer.start(ACTION_GET_DEV_INFO, this);
        executeShellCommand(commandStr, new DeviceInfoReceiver(mDeviceInfo));
    }

    /**
     * Receiver which receives and parses the device information.
     */
    final class DeviceInfoReceiver extends MultiLineReceiver {

        private ArrayList<String> mResultLines = new ArrayList<String>();
        private DeviceParameterCollector mDeviceParamCollector;

        public DeviceInfoReceiver(DeviceParameterCollector paramCollector) {
            super();
            mDeviceParamCollector = paramCollector;
            setTrimLine(false);
        }

        /** {@inheritDoc} */
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                mResultLines.add(line);
            }
        }

        /** {@inheritDoc} */
        public boolean isCancelled() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public void done() {
            super.done();
            String key, value;
            for (String line : mResultLines) {
                Matcher matcher = INSTRUMENT_RESULT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    key = matcher.group(1);
                    value = matcher.group(2);
                    mDeviceParamCollector.set(key, value);
                }
            }

            synchronized(mObjectSync) {
                mObjectSync.sendNotify();
            }
        }

    }

    /**
     * Store the build information of a device
     */
    public static final class DeviceParameterCollector{
        // the device info keys expected to be sent from device info instrumentation
        // these constants should match exactly with those defined in DeviceInfoInstrument.jaa
        public static final String PRODUCT_NAME = "buildName";
        public static final String BUILD_VERSION = "buildVersion";
        public static final String BUILD_ID = "buildID";
        public static final String BUILD_FINGERPRINT = "build_fingerprint";
        public static final String BUILD_TAGS = "build_tags";
        public static final String BUILD_TYPE = "build_type";
        public static final String BUILD_MANUFACTURER = "build_manufacturer";
        public static final String BUILD_MODEL = "build_model";
        public static final String BUILD_BRAND = "build_brand";
        public static final String BUILD_BOARD = "build_board";
        public static final String BUILD_DEVICE = "build_device";
        public static final String BUILD_ABI = "build_abi";
        public static final String BUILD_ABI2 = "build_abi2";
        public static final String SCREEN_SIZE = "screen_size";
        public static final String SCREEN_HEIGHT = "screen_height";
        public static final String SCREEN_WIDTH = "screen_width";
        public static final String SCREEN_DENSITY = "screen_density";
        public static final String SCREEN_DENSITY_BUCKET = "screen_density_bucket";
        public static final String SERIAL_NUMBER = "serialNumber";
        public static final String VERSION_SDK = "androidPlatformVersion";
        public static final String LOCALES = "locales";
        public static final String SCREEN_Y_DENSITY = "Ydpi";
        public static final String SCREEN_X_DENSITY = "Xdpi";
        public static final String TOUCH_SCREEN = "touch";
        public static final String NAVIGATION = "navigation";
        public static final String KEYPAD = "keypad";
        public static final String NETWORK = "network";
        public static final String IMEI = "imei";
        public static final String IMSI = "imsi";
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String FEATURES = "features";
        public static final String PROCESSES = "processes";
        public static final String OPEN_GL_ES_VERSION = "openGlEsVersion";
        public static final String PARTITIONS = "partitions";

        private HashMap<String, String> mInfoMap;

        public DeviceParameterCollector() {
            mInfoMap = new HashMap<String, String>();
        }

        /**
         * Set the pair of key and value of device information.
         *
         * @param key The key of the pair.
         * @param value The value of the pair.
         */
        public void set(final String key, final String value) {
            mInfoMap.put(key, value);
        }

        /**
         * Return the number of device info items which stored in.
         *
         * @return the number of device info items which stored in.
         */
        public int size() {
            return mInfoMap.size();
        }

        /**
         * Set the build finger print.
         *
         * @param buildFingerPrint The build finger print.
         */
        public void setBuildFingerPrint(final String buildFingerPrint) {
            mInfoMap.put(BUILD_FINGERPRINT, buildFingerPrint);
        }

        /**
         * Set the build tags.
         *
         * @param buildTags The build tags.
         */
        public void setBuildTags(final String buildTags) {
            mInfoMap.put(BUILD_TAGS, buildTags);
        }

        /**
         * Set build type.
         *
         * @param buildType The build type.
         */
        public void setBuildType(final String buildType) {
            mInfoMap.put(BUILD_TYPE, buildType);
        }

        /**
         * Set the build model.
         *
         * @param buildModel The build model.
         */
        public void setBuildModel(final String buildModel) {
            mInfoMap.put(BUILD_MODEL, buildModel);
        }

        /**
         * Set the build brand.
         *
         * @param buildBrand The build brand.
         */
        public void setBuildBrand(final String buildBrand) {
            mInfoMap.put(BUILD_BRAND, buildBrand);
        }

        /**
         * Set the build board.
         *
         * @param buildBoard The build board.
         */
        public void setBuildBoard(final String buildBoard) {
            mInfoMap.put(BUILD_BOARD, buildBoard);
        }

        /**
         * Set the build device.
         *
         * @param buildDevice The build device.
         */
        public void setBuildDevice(final String buildDevice) {
            mInfoMap.put(BUILD_DEVICE, buildDevice);
        }

        /**
         * Set the build abi
         *
         * @param buildAbi The build ABI
         */
        public void setBuildAbi(final String buildAbi) {
            mInfoMap.put(BUILD_ABI, buildAbi);
        }

        /**
         * Set the build abi2
         *
         * @param buildAbi The build ABI2
         */
        public void setBuildAbi2(final String buildAbi2) {
            mInfoMap.put(BUILD_ABI2, buildAbi2);
        }

        /**
         * set the serialNumber of this device
         *
         * @param serialNumber The serial number.
         */
        public void setSerialNumber(final String serialNumber) {
            mInfoMap.put(SERIAL_NUMBER, serialNumber);
        }

        /**
         * set the build id
         *
         * @param bldId The build ID.
         */
        public void setBuildId(final String bldId) {
            mInfoMap.put(BUILD_ID, bldId);
        }

        /**
         * set the build version
         *
         * @param bldVer The build version.
         */
        public void setBuildVersion(final String bldVer) {
            mInfoMap.put(BUILD_VERSION, bldVer);
        }

        /**
         * set the product name
         **
         * @param productName The product name.
         */
        public void setProductName(final String productName) {
            mInfoMap.put(PRODUCT_NAME, productName);
        }

        /**
         * Get the build finger print.
         *
         * @return The build finger print.
         */
        public String getBuildFingerPrint() {
            return mInfoMap.get(BUILD_FINGERPRINT);
        }

        /**
         * Get the build tags.
         *
         * @return The build tags.
         */
        public String getBuildTags() {
            return mInfoMap.get(BUILD_TAGS);
        }

        /**
         * Get build type.
         *
         * @return The build type.
         */
        public String getBuildType() {
            return mInfoMap.get(BUILD_TYPE);
        }

        /**
         * Get the build model.
         *
         * @return The build model.
         */
        public String getBuildModel() {
            return mInfoMap.get(BUILD_MODEL);
        }

        /**
         * Get the build manufacturer.
         *
         * @return The build manufacturer.
         */
        public String getBuildManufacturer() {
            return mInfoMap.get(BUILD_MANUFACTURER);
        }

        /**
         * Get the build brand.
         *
         * @return The build brand.
         */
        public String getBuildBrand() {
            return mInfoMap.get(BUILD_BRAND);
        }

        /**
         * Get the build board.
         *
         * @return The build board.
         */
        public String getBuildBoard() {
            return mInfoMap.get(BUILD_BOARD);
        }

        /**
         * Get the build device.
         *
         * @return The build device.
         */
        public String getBuildDevice() {
            return mInfoMap.get(BUILD_DEVICE);
        }

        /**
         * Get the build ABI.
         *
         * @return The build ABI.
         */
        public String getBuildAbi() {
            return mInfoMap.get(BUILD_ABI);
        }

        /**
         * Get the build ABI2.
         *
         * @return The build ABI2.
         */
        public String getBuildAbi2() {
            return mInfoMap.get(BUILD_ABI2);
        }

        /**
         * get the build id
         **
         * @return The build ID.
         */
        public String getBuildId() {
            return mInfoMap.get(BUILD_ID);
        }

        /**
         * get the build version
         **
         * @return The build version.
         */
        public String getBuildVersion() {
            return mInfoMap.get(BUILD_VERSION);
        }

        /**
         * get the product name
         *
         * @return The product name.
         */
        public String getProductName() {
            return mInfoMap.get(PRODUCT_NAME);
        }

        /**
         * get the serial number
         *
         * @return The serial number.
         */
        public String getSerialNumber() {
            return mInfoMap.get(SERIAL_NUMBER);
        }

        public String getScreenSize() {
            return mInfoMap.get(SCREEN_SIZE);
        }

        /**
         * Return screen resolution(width x height)
         *
         * @return The screen resolution.
         */
        public String getScreenResolution() {
            return mInfoMap.get(SCREEN_WIDTH) + "x" + mInfoMap.get(SCREEN_HEIGHT);
        }

        /**
         * Return logical screen density
         *
         * @return The logical screen density.
         */
        public String getScreenDensity() {
            return mInfoMap.get(SCREEN_DENSITY);
        }

        /**
         * Return the probable screen density bucket
         *
         * @return The probable screen density bucket.
         */
        public String getScreenDensityBucket() {
            return mInfoMap.get(SCREEN_DENSITY_BUCKET);
        }

        /**
         * Get Android platform version.
         *
         * @return The Android platform version.
         */
        public String getAndroidPlatformVersion() {
            return mInfoMap.get(VERSION_SDK);
        }

        /**
         * Get supported locales.
         *
         * @return The supported locales.
         */
        public String getLocales() {
            return mInfoMap.get(LOCALES);
        }

        /**
         * Get x dip
         *
         * @return The X dip.
         */
        public String getXdpi() {
            return mInfoMap.get(SCREEN_X_DENSITY);
        }

        /**
         * Get y dip
         *
         * @return The y dip.
         */
        public String getYdpi() {
            return mInfoMap.get(SCREEN_Y_DENSITY);
        }

        /**
         * Get touch information
         *
         * @return The touch screen information.
         */
        public String getTouchInfo() {
            return mInfoMap.get(TOUCH_SCREEN);
        }

        /**
         * Get navigation information
         *
         * @return The navigation information.
         */
        public String getNavigation() {
            return mInfoMap.get(NAVIGATION);
        }

        /**
         * Get keypad information
         *
         * @return The keypad information.
         */
        public String getKeypad() {
            return mInfoMap.get(KEYPAD);
        }

        /**
         * Get network information
         *
         * @return The network information.
         */
        public String getNetwork() {
            return mInfoMap.get(NETWORK);
        }

        /**
         * Get IMEI
         *
         * @return IMEI.
         */
        public String getIMEI() {
            return mInfoMap.get(IMEI);
        }

        /**
         * Get IMSI
         *
         * @return IMSI.
         */
        public String getIMSI() {
            return mInfoMap.get(IMSI);
        }

        /**
         * Get phone number
         *
         * @return Phone number.
         */
        public String getPhoneNumber() {
            return mInfoMap.get(PHONE_NUMBER);
        }

        /**
         * Get features.
         *
         * @return Features.
         */
        public String getFeatures() {
            return mInfoMap.get(FEATURES);
        }

        /**
         * Get processes.
         *
         * @return Processes.
         */
        public String getProcesses() {
            return mInfoMap.get(PROCESSES);
        }

        /**
         * Get Open GL ES version.
         *
         * @return version or error message.
         */
        public String getOpenGlEsVersion() {
            return mInfoMap.get(OPEN_GL_ES_VERSION);
        }

        /**
         * Get partitions.
         *
         * @return partitions or error message.
         */
        public String getPartitions() {
            return mInfoMap.get(PARTITIONS);
        }
    }

    /**
     * Get the serial number of the  {@link TestDevice}.
     *
     * @return the serial number of the {@link TestDevice}
     */
    public String getSerialNumber() {
        if (mDevice == null) {
            return mDeviceInfo.getSerialNumber();
        }
        return mDevice.getSerialNumber();
    }

    /**
     * Run a specified test.
     *
     * @param test The test to be run.
     */
    public void runTest(Test test) throws DeviceDisconnectedException {

        final String appNameSpace = test.getAppNameSpace();
        String runner = test.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }

        // need to doubly escape any '$' chars in the name since this string is
        // passed through two shells \\\$ -> \$ -> $
        final String testName = test.getFullName().replaceAll("\\$", "\\\\\\$");

        final String commandStr = "am instrument -w -r -e class " + testName
                + " " + appNameSpace + "/" + runner;
        Log.d(commandStr);
        executeShellCommand(commandStr, new IndividualModeResultParser(test));
    }

    /**
     * Run a test package in batch mode.
     *
     * @param testPackage The testPackage to be run.
     * @param javaPkgName The java package name. If null, run the whole test package;
     *              else, run the specified java package contained in the test package
     */
    public void runInBatchMode(TestPackage testPackage, final String javaPkgName)
                throws DeviceDisconnectedException {
        String appNameSpace = testPackage.getAppNameSpace();
        String runner = testPackage.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }

        String name = testPackage.getAppPackageName();
        if ((javaPkgName != null) && (javaPkgName.length() != 0)) {
            name = javaPkgName;
        }

        String cmdHeader = "am instrument -w -r -e package " + name + " ";
        final String commandStr = cmdHeader + appNameSpace + "/" + runner;
        Log.d(commandStr);

        mBatchModeResultParser = new BatchModeResultParser(testPackage);
        executeShellCommand(commandStr, mBatchModeResultParser);
    }

    /**
     * Run a in batch mode of a TestPackage.
     *
     * @param testPackage The testPackage to be run.
     * @param javaClassName The java class name.
     */
    public void runTestCaseInBatchMode(TestPackage testPackage, final String javaClassName,
            String profile) throws DeviceDisconnectedException {
        if (javaClassName == null) {
            return;
        }

        String appNameSpace = testPackage.getAppNameSpace();
        String runner = testPackage.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }

        String cmdHeader = "am instrument -w -r -e class " + javaClassName
                + " -e profile " + profile + " ";
        final String commandStr = cmdHeader + appNameSpace + "/" + runner;
        Log.d(commandStr);

        mBatchModeResultParser = new BatchModeResultParser(testPackage);
        executeShellCommand(commandStr, mBatchModeResultParser);
    }

    /**
     * Get clients.
     *
     * @return The clients.
     */
    public Client[] getClients() {
        return mDevice.getClients();
    }

    /**
     * Push a file to a given path.
     *
     * @param localPath The local path.
     * @param remotePath The remote path.
     */
    public void pushFile(String localPath, String remotePath) {
        try {
            mSyncService.pushFile(localPath, remotePath, new PushMonitor());
        } catch (TimeoutException e) {
            Log.e("Uploading file failed: timeout", null);
        } catch (SyncException e) {
            Log.e("Uploading file failed: " + e.getMessage(), null);
        } catch (FileNotFoundException e) {
            Log.e("Uploading file failed: " + e.getMessage(), null);
        } catch (IOException e) {
            Log.e("Uploading file failed: " + e.getMessage(), null);
        }
    }

    /**
     * Install a specified APK using adb command install.
     *
     * @param apkPath Name of the package to be installed.
     */
    public void installAPK(final String apkPath) throws DeviceDisconnectedException,
                InvalidApkPathException {
        if ((apkPath == null) || (apkPath.length() == 0) || (!HostUtils.isFileExist(apkPath))) {
            throw new InvalidApkPathException(apkPath);
        }

        // Use re-install directly
        final String cmd = DeviceManager.getAdbLocation() + " -s "
                + getSerialNumber() + " install -r " + apkPath;
        Log.d(cmd);

        mPackageActionTimer.start(ACTION_INSTALL, this);
        executeCommand(cmd, new PackageActionObserver(ACTION_INSTALL));
    }

    /**
     * Execute the given command.
     *
     * @param command The command to be executed.
     * @param stdOutReceiver The receiver for handling the output from the device.
     */
    private void executeCommand(String command, StdOutObserver stdOutReceiver)
                    throws DeviceDisconnectedException {
        if (mStatus != STATUS_OFFLINE) {
            try {
                Process proc = Runtime.getRuntime().exec(command);

                if (stdOutReceiver != null) {
                    stdOutReceiver.setInputStream(proc.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new DeviceDisconnectedException(getSerialNumber());
        }
    }

    /**
     * Standard output observer.
     *
     */
    interface StdOutObserver {
        /**
         * set the input Stream.
         */
        public void setInputStream(InputStream is);

        /**
         * Process lines.
         */
        public void processLines() throws IOException;
    }

    /**
     * Un-install APK.
     *
     * @param packageName The package to be un-installed.
     */
    public void uninstallAPK(String packageName) throws DeviceDisconnectedException,
                InvalidNameSpaceException {
        if ((packageName == null) || (packageName.length() == 0)) {
            throw new InvalidNameSpaceException(packageName);
        }

        uninstallAPKImpl(packageName, mUninstallObserver);
    }

    /**
     * The implementation of uninstalling APK.
     *
     * @param packageName The package to be uninstalled.
     * @param observer The uninstall observer
     */
    private void uninstallAPKImpl(final String packageName, final PackageActionObserver observer)
                throws DeviceDisconnectedException {
        final String cmdStr = DeviceManager.getAdbLocation() + " -s "
                      + getSerialNumber() + " uninstall " + packageName;
        Log.d(cmdStr);
        mPackageActionTimer.start(ACTION_UNINSTALL, this);
        executeCommand(cmdStr, observer);
    }

    /**
     * Package action(install/uninstall) timeout task
     */
    class PackageActionTimeoutTask extends TimerTask {

        private String mAction;
        private TestDevice mTargetDevice;

        /**
         * Task of package action timeout.
         *
         * @param action string of action
         * @param testDevice the {@TestDevice} which got the timeout.
         */
        public PackageActionTimeoutTask(final String action,
                TestDevice testDevice) {
            mAction = action;
            mTargetDevice = testDevice;
        }

        /** {@inheritDoc}*/
        @Override
        public void run() {
            Log.d("PackageActionTimeoutTask.run(): mAction=" + mAction);
            synchronized (mObjectSync) {
                mObjectSync.sendNotify();
            }

            if (mAction.toLowerCase().equals(ACTION_INSTALL)) {
                mDeviceObserver.notifyInstallingTimeout(mTargetDevice);
            } else if (mAction.toLowerCase().equals(ACTION_UNINSTALL)) {
                mDeviceObserver.notifyUninstallingTimeout(mTargetDevice);
            } else if (mAction.toLowerCase().equals(ACTION_GET_DEV_INFO)) {
                Log.e("Get device information timeout", null);
            } else {
                Log.e("Timeout: " + mAction, null);
            }
        }
    }

    /**
     * Package action timer monitors the package action.
     *
     */
    class PackageActionTimer {
        private Timer mTimer;

        /**
         * Start the timer while package install/uninstall/getDeviceInfo/checkAPI.
         *
         * @param action The action of package.
         * @param device The TestDevice the action is taken over.
         */
        private void start(final String action, final TestDevice device) {
            start(action, HostConfig.Ints.packageInstallTimeoutMs.value(), device);
        }

        /**
         * Start the timer while package install/uninstall/getDeviceInfo/checkAPI with specific
         * timeout.
         *
         * @param action The action of package
         * @param timeout The specific timeout
         * @param device The TestDevice under operation
         */
        private void start(final String action, final int timeout, final TestDevice device) {
            Log.d("start(), action=" + action + ",mTimer=" + mTimer + ",timeout=" + timeout);
            synchronized (this) {
                if (mTimer != null) {
                    mTimer.cancel();
                }

                mTimer = new Timer();
                mTimer.schedule(new PackageActionTimeoutTask(action, device), timeout);
            }
        }

        /**
         * Stop the action timer.
         */
        private void stop() {
            synchronized (this) {
                Log.d("stop() , mTimer=" + mTimer);
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        }
    }

    /**
     * The observer of package action, currently including installing and uninstalling.
     */
    final class PackageActionObserver implements StdOutObserver, Runnable {

        private BufferedReader mReader;
        private String mAction;

        public PackageActionObserver(final String action) {
            mAction = action;
        }

        /** {@inheritDoc} */
        public void run() {
            try {
                processLines();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    mReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Parse the standard out to judge where the installation is complete.
         */
        public void processLines() throws IOException {
            String line = mReader.readLine();
            int statusCode = DeviceObserver.FAIL;
            boolean gotResult = false;

            while (line != null) {
                line = line.toLowerCase();
                if (line.indexOf("success") != -1) {
                    statusCode = DeviceObserver.SUCCESS;
                    gotResult = true;
                } else if (line.indexOf("failure") != -1) {
                    statusCode = DeviceObserver.FAIL;
                    CUIOutputStream.println(mAction.toLowerCase() + " met " + line);
                    gotResult = true;
                } else if (line.indexOf("error") != -1) {
                    CUIOutputStream.println(mAction.toLowerCase() + " met " + line);
                    statusCode = DeviceObserver.FAIL;
                    gotResult = true;
                }

                if (gotResult) {
                    Log.d(mAction + " calls stopPackageActionTimer()");
                    mPackageActionTimer.stop();

                    if (mDeviceObserver != null) {
                        mDeviceObserver.notifyInstallingComplete(statusCode);
                    }
                    break;
                }
                line = mReader.readLine();
            }
        }

        /** {@inheritDoc} */
        public void setInputStream(InputStream is) {
            mReader = new BufferedReader(new InputStreamReader(is));
            new Thread(this).start();
        }
    }

    /**
     * Raw mode result parser.
     */
    abstract class RawModeResultParser extends MultiLineReceiver {
        public final static String EQ_MARK = "=";
        public final static String COMMA_MARK = ":";
        public final static String AT_MARK = "at ";

        public final static String STATUS_STREAM = "INSTRUMENTATION_STATUS: stream=";
        public final static String STATUS_TEST = "INSTRUMENTATION_STATUS: test=";
        public final static String STATUS_CLASS = "INSTRUMENTATION_STATUS: class=";
        public final static String STATUS_CODE = "INSTRUMENTATION_STATUS_CODE:";
        public final static String STATUS_STACK = "INSTRUMENTATION_STATUS: stack=";
        public final static String STATUS_CURRENT = "INSTRUMENTATION_STATUS: current=";
        public final static String STATUS_NUM = "INSTRUMENTATION_STATUS: numtests=";
        public final static String STATUS_ERROR_STR = "INSTRUMENTATION_STATUS: Error=";

        public final static String FAILURE = "Failure in ";
        public final static String ASSERTION = "junit.framework.Assertion";

        public final static String RESULT_STREAM = "INSTRUMENTATION_RESULT: stream=";
        public final static String RESULT_CODE = "INSTRUMENTATION_CODE:";
        public final static String RESULT = "Test results";
        public final static String RESULT_TIME = "Time:";
        public final static String RESULT_SUMMARY = "Tests run:";

        public final static int STATUS_STARTING = 1;
        public final static int STATUS_PASS = 0;
        public final static int STATUS_FAIL = -1;
        public final static int STATUS_ERROR = -2;
        public final static int STATUS_OMITTED = -3;

        private ArrayList<String> mResultLines;

        public String mStackTrace;
        public String mFailedMsg;
        public int mResultCode;

        public Test mTest;

        public RawModeResultParser(Test test) {
            super();

            setTrimLine(false);

            mTest = test;
            mResultLines = new ArrayList<String>();
            mStackTrace = null;
            mFailedMsg = null;
            mResultCode = CtsTestResult.CODE_FAIL;
        }

        /** {@inheritDoc} */
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                processNewLine(line.trim());
            }
        }

        /**
         * Process the new line.
         *
         * @param line The new line.
         */
        abstract public void processNewLine(final String line);

        /**
         * Get the result lines.
         *
         * @return The result lines.
         */
        public ArrayList<String> getResultLines() {
            return mResultLines;
        }

        /**
         * Get the named string from the string containing the mark.
         *
         * @param mark The mark to search against the results.
         * @return The test name.
         */
        public String getNamedString(String mark) {
            for (String line : mResultLines) {
                if (line.startsWith(mark)) {
                    String name = line.substring(line.indexOf(EQ_MARK) + 1);
                    return name.trim();
                }
            }

            return null;
        }

        /**
         * Parse the int from the string containing the mark.
         *
         * @param mark The mark to search against the results.
         * @return The number.
         */
        public int parseIntWithMark(String mark) {
            for (String line : mResultLines) {
                if (line.startsWith(mark)) {
                    String code = line.substring(line.indexOf(EQ_MARK) + 1);
                    return Integer.parseInt(code.trim());
                }
            }

            return 0;
        }

        /**
         * Get failed message.
         *
         * @return The failed message.
         */
        public String getFailedMessage() {
            Iterator<String> iterator = mResultLines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (line.startsWith(STATUS_STACK)) {
                    String failedMsg = line.substring(STATUS_STACK.length());
                    if (iterator.hasNext()) {
                        failedMsg += " " + iterator.next();
                    }
                    return failedMsg;
                }
            }
            return null;
        }

        /**
         * Get stack trace from output result.
         *
         * @return The stack trace message.
         */
        public String getStackTrace() {
            StringBuilder sb = new StringBuilder();
            for (String line : mResultLines) {
                line = line.trim();
                if (line.startsWith(AT_MARK) && line.endsWith(")")) {
                    sb.append(line + "\n");
                }
            }
            return sb.toString();
        }

        /**
         * Get the status code of the test result.
         *
         * @param line The string contains the status code of the test result.
         * @return The status code of the test result.
         */
        public int getStatusCode(String line) {
            String codeStr = line.substring(line.indexOf(COMMA_MARK) + 1);
            return Integer.parseInt(codeStr.trim());
        }

        /** {@inheritDoc} */
        public boolean isCancelled() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public void done() {
            super.done();
        }
    }

    /**
     * Individual mode result parser. <br>
     * Individual mode means that the host sends request
     * to the device method by method. And the device
     * reactions and outputs the result to each request.
     */
    final class IndividualModeResultParser extends RawModeResultParser {

        public IndividualModeResultParser(Test test) {
            super(test);
        }

        /**
         * Process a new line.
         *
         * @param line The new line.
         */
        @Override
        public void processNewLine(final String line) {
            if ((line == null) || (line.trim().length() == 0)) {
                return;
            }

            ArrayList<String> resultLines = getResultLines();
            resultLines.add(line);

            if (line.startsWith(STATUS_CODE)) {
                int statusCode = getStatusCode(line);
                processTestResult(statusCode);
                resultLines.removeAll(resultLines);
            }
        }

        /**
         * Process the test result of a single test.
         *
         * @param statusCode The status code of a single test's test result.
         */
        public void processTestResult(int statusCode) {
            String testName = getNamedString(STATUS_TEST);
            String className = getNamedString(STATUS_CLASS);
            String testFullName = className + Test.METHOD_SEPARATOR + testName;
            String errorMessage = getNamedString(STATUS_ERROR_STR);

            mFailedMsg = null;
            mStackTrace = null;
            if ((statusCode == STATUS_FAIL) || (statusCode == STATUS_ERROR)) {
                mFailedMsg = getFailedMessage();
                mStackTrace = getStackTrace();
            }

            if ((errorMessage != null) && (errorMessage.length() != 0)) {
                if (mFailedMsg == null) {
                    mFailedMsg = errorMessage;
                } else {
                    mFailedMsg += " : " + errorMessage;
                }
            }

            Log.d(testFullName + "...(" + statusCode + ")");
            Log.d("errorMessage= " + errorMessage);
            Log.d("mFailedMsg=" + mFailedMsg);
            Log.d("mStackTrace=" + mStackTrace);

            switch (statusCode) {
            case STATUS_STARTING:
                break;

            case STATUS_PASS:
                mResultCode = CtsTestResult.CODE_PASS;
                break;

            case STATUS_FAIL:
            case STATUS_ERROR:
                mResultCode = CtsTestResult.CODE_FAIL;
                break;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void done() {
            mTest.notifyResult(new CtsTestResult(mResultCode, mFailedMsg, mStackTrace));
            super.done();
        }
    }

    /**
     * Batch mode result parser.
     * Batch mode means that the host sends only one request
     * for all of the methods contained in the package to the
     * device. And then, the device runs the method one by one
     * and outputs the result method by method.
     */
    final class BatchModeResultParser extends RawModeResultParser {
        private TestPackage mTestPackage;
        private Collection<Test> mTests;
        public int mCurrentTestNum;
        public int mTotalNum;

        public BatchModeResultParser(TestPackage testPackage) {
            super(null);

            mTestPackage = testPackage;
            if (mTestPackage != null) {
                mTests = mTestPackage.getTests();
            }
        }

        /**
         * Process a new line.
         *
         * @param line The new line.
         */
        @Override
        public void processNewLine(final String line) {
            if ((line == null) || (line.trim().length() == 0)) {
                return;
            }

            ArrayList<String> resultLines = getResultLines();
            resultLines.add(line);

            if (line.startsWith(STATUS_CODE)) {
                int statusCode = getStatusCode(line);
                processTestResult(statusCode);
                resultLines.removeAll(resultLines);
            } else if (line.startsWith(RESULT_CODE)) {
                int resultCode = getStatusCode(line);
                switch(resultCode) {
                case STATUS_STARTING:
                    break;

                case STATUS_FAIL:
                case STATUS_ERROR:
                    mResultCode = CtsTestResult.CODE_FAIL;
                    break;

                case STATUS_PASS:
                    mResultCode = CtsTestResult.CODE_PASS;
                    break;
                }
                resultLines.removeAll(resultLines);
            }
        }

        /**
         * Process the test result of a single test.
         *
         * @param statusCode The status code of a single test's test result.
         */
        public void processTestResult(int statusCode) {
            String testName = getNamedString(STATUS_TEST);
            String className = getNamedString(STATUS_CLASS);
            String testFullName = className + Test.METHOD_SEPARATOR + testName;
            mCurrentTestNum = parseIntWithMark(STATUS_CURRENT);
            mTotalNum = parseIntWithMark(STATUS_NUM);

            mFailedMsg = null;
            mStackTrace = null;
            if ((statusCode == STATUS_FAIL) || ((statusCode == STATUS_ERROR))) {
                mFailedMsg = getFailedMessage();
                mStackTrace = getStackTrace();
            }

            Log.d(testFullName + "...(" + statusCode + ")");
            Log.d("mFailedMsg=" + mFailedMsg);
            Log.d("mStackTrace=" + mStackTrace);

            String status = TestPackage.FINISH;

            if (statusCode == STATUS_STARTING) {
                status = TestPackage.START;
            }

            mTest = searchTest(testFullName);
            if (mTest != null) {
                switch(statusCode) {
                case STATUS_STARTING:
                    status = TestPackage.START;
                    break;

                case STATUS_PASS:
                    mTest.setResult(new CtsTestResult(
                            CtsTestResult.CODE_PASS, null, null));
                    break;

                case STATUS_ERROR:
                case STATUS_FAIL:
                    mTest.setResult(new CtsTestResult(
                            CtsTestResult.CODE_FAIL, mFailedMsg, mStackTrace));
                    break;
                }
            }
            // report status even if no matching test was found
            mTestPackage.notifyTestStatus(mTest, status);
        }

        /**
         * Search Test with given test full name.
         *
         * @param testFullName The test full name.
         * @return The Test matches the test full name given.
         */
        private Test searchTest(String testFullName) {
            for (Test test : mTests) {
                if (testFullName.equals(test.getFullName())) {
                    return test;
                }
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void done() {
            mTestPackage.notifyBatchModeFinish();
            super.done();
        }
    }

    /**
     * Remove the run time listener.
     */
    public void removeRuntimeListener() {
        mDeviceObserver = null;
    }

    /**
     * Set the run time listener.
     *
     * @param listener The run time listener.
     */
    public void setRuntimeListener(DeviceObserver listener) {
        mDeviceObserver = listener;
    }

    /**
     * Push monitor monitoring the status of pushing a file.
     */
    class PushMonitor implements ISyncProgressMonitor {

        public PushMonitor() {
        }

        /** {@inheritDoc} */
        public void advance(int arg0) {
        }

        /** {@inheritDoc} */
        public boolean isCanceled() {
            return false;
        }

        /** {@inheritDoc} */
        public void start(int arg0) {
        }

        /** {@inheritDoc} */
        public void startSubTask(String arg0) {
        }

        /** {@inheritDoc} */
        public void stop() {
        }
    }

    /**
     * Add a new log listener.
     *
     * @param listener the listener
     */
    public void addMainLogListener(ILogListener listener) {
        logListener.addListener(listener);
    }

    /**
     * Remove an existing log listener.
     *
     * @param listener the listener to remove.
     */
    public void removeMainLogListener(ILogListener listener) {
        logListener.removeListener(listener);
    }

    /**
     * Execute Adb shell command on {@link IDevice}
     *
     * @param cmd the string of command.
     * @param receiver {@link IShellOutputReceiver}
     * @throws DeviceDisconnectedException if the device disconnects during the command
     */
    public void executeShellCommand(final String cmd,
            final IShellOutputReceiver receiver) throws DeviceDisconnectedException {
        executeShellCommand(cmd, receiver, null);
    }

    /**
     * Execute Adb shell command on {@link IDevice}
     *
     * Note that the receivers run in a different thread than the caller.
     *
     * @param cmd the string of command.
     * @param receiver {@link IShellOutputReceiver}
     * @param logReceiver {@link LogReceiver}
     * @throws DeviceDisconnectedException if the device disconnects during the command
     */
    public void executeShellCommand(final String cmd,
            final IShellOutputReceiver receiver,
            final LogReceiver logReceiver)
            throws DeviceDisconnectedException {
        if (mStatus == STATUS_OFFLINE) {
            Log.d(String.format("device %s is offline when attempting to execute %s",
                    getSerialNumber(), cmd));
            throw new DeviceDisconnectedException(getSerialNumber());
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    mDevice.executeShellCommand(cmd, receiver, 0);
                } catch (IOException e) {
                    Log.e(String.format("Failed to execute shell command %s on device %s", cmd,
                            mDevice.getSerialNumber()), e);
                } catch (TimeoutException e) {
                    Log.e(String.format("Failed to execute shell command %s on device %s", cmd,
                            mDevice.getSerialNumber()), e);
                } catch (AdbCommandRejectedException e) {
                    Log.e(String.format("Failed to execute shell command %s on device %s", cmd,
                            mDevice.getSerialNumber()), e);
                } catch (ShellCommandUnresponsiveException e) {
                    Log.e(String.format("Failed to execute shell command %s on device %s", cmd,
                            mDevice.getSerialNumber()), e);
                }
            }
        }.start();
    }

    /**
     * Kill {@link Client} which running the test on the {@link IDevice}
     *
     * @param packageName the test package name
     */
    public void killProcess(String packageName) {
        if (mStatus == STATUS_OFFLINE) {
            return;
        }
        Client[] clients = mDevice.getClients();

        for (Client c : clients) {
            ClientData cd = c.getClientData();
            if (cd.getClientDescription() == null) {
                continue;
            }
            if (cd.getClientDescription().equals(packageName)) {
                c.kill();
                break;
            }
        }
    }

    /**
     * Called when the {@link TestDevice} disconnected.
     */
    public void disconnected() {
        CUIOutputStream.println("Device(" + getSerialNumber() + ") disconnected");
        mDevice = null;
        mSyncService = null;

        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }

        if (mStatus == STATUS_BUSY) {
            Log.d("TestDevice.disconnected calls notifyTestingDeviceDisconnected");
            mDeviceObserver.notifyTestingDeviceDisconnected();
        } else {
            if (!TestSession.isADBServerRestartedMode()) {
                CUIOutputStream.printPrompt();
            }
        }
        setStatus(STATUS_OFFLINE);
        if (logServiceThread != null) {
            logServiceThread.cancelLogService();
        }
    }

    /**
     * Set the status of the {@link TestDevice}
     *
     * @param statusCode the status code of {@link TestDevice}
     */
    public void setStatus(final int statusCode) {
        if (statusCode != STATUS_IDLE && statusCode != STATUS_BUSY
                && statusCode != STATUS_OFFLINE) {
            throw new IllegalArgumentException("Invalid status code");
        }
        mStatus = statusCode;
    }

    /**
     * Get the status code of the {@link TestDevice}.
     *
     * @return get the status code of the {@link TestDevice}
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * Get the status of the {@link TestDevice} as string.
     *
     * @return the status of the {@link TestDevice} as string.
     */
    public String getStatusAsString() {
        return mStatusMap.get(mStatus);
    }

    /**
     * Wait for command finish.
     */
    public void waitForCommandFinish() {
        synchronized (mObjectSync) {
            try {
                mObjectSync.waitOn();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Start the action timer with specific timeout
     *
     * @param action the action to start the timer
     * @param timeout the specific timeout
     */
    void startActionTimer(final String action, final int timeout) {
        mPackageActionTimer.start(action, timeout, this);
    }

    /**
     * Start the action timer.
     *
     * @param action the action to start the timer.
     */
    void startActionTimer(String action) {
       mPackageActionTimer.start(action, this);
    }

    /**
     * Stop the action timer.
     */
    void stopActionTimer() {
        mPackageActionTimer.stop();
    }

    /**
     * Allows an external test to signal that it's command is complete.
     */
    void notifyExternalTestComplete() {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }

    /**
     * Notify install complete.
     */
    public void notifyInstallingComplete(int resultCode) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }
    }

    /** {@inheritDoc} */
    public void notifyInstallingTimeout(TestDevice testDevice) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }

    /** {@inheritDoc} */
    public void notifyTestingDeviceDisconnected() {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            if (mPackageActionTimer != null) {
                mPackageActionTimer.stop();
            }
        }
    }

    /** {@inheritDoc} */
    public void notifyUninstallingComplete(int resultCode) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }
    }

    /** {@inheritDoc} */
    public void notifyUninstallingTimeout(TestDevice testDevice) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }

    /**
     * Synchronization object for communication between threads.
     */
    class ObjectSync {
        private boolean mNotifySent = false;

        /**
         * Send notify to the waiting thread.
         */
        public void sendNotify() {
            Log.d("ObjectSync.sendNotify() is called, mNotifySent=" + mNotifySent);
            mNotifySent = true;
            notify();
        }

        /**
         * Wait on.
         */
        public void waitOn() throws InterruptedException {
            Log.d("ObjectSync.waitOn() is called, mNotifySent=" + mNotifySent);
            if (!mNotifySent) {
                wait();
            }

            mNotifySent = false;
        }

        /**
         * Check if notify has been sent to the waiting thread.
         *
         * @return If sent, return true; else, return false.
         */
        public boolean isNotified() {
            return mNotifySent;
        }
    }

    /**
     * Take a screenshot of the device under test.
     *
     * @return the screenshot
     * @throws IOException
     * @throws AdbCommandRejectedException
     * @throws TimeoutException
     */
    public RawImage getScreenshot() throws IOException, TimeoutException,
            AdbCommandRejectedException {
        return mDevice.getScreenshot();
    }
}
