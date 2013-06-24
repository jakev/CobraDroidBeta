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
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Initializing and managing devices.
 */
public class DeviceManager implements IDeviceChangeListener {

    private static final int SHORT_DELAY = 1000 * 15; // 15 seconds
    private static final int LONG_DELAY = 1000 * 60 * 10; // 10 minutes
    /** Time to wait after issuing reboot command  */
    private static final int REBOOT_DELAY = 5 * 1000; // 5 seconds
    /** Time to wait after device reports that boot is complete. */
    private static final int POST_BOOT_DELAY = 1000 * 60; // 1 minute
    /** Maximal number of attempts to restart ADB connection. */
    private static final int MAX_ADB_RESTART_ATTEMPTS = 10;
    ArrayList<TestDevice> mDevices;
    /** This is used during device restart for blocking until the device has been reconnected. */
    private Semaphore mSemaphore = new Semaphore(0);

    public DeviceManager() {
        mDevices = new ArrayList<TestDevice>();
    }

    /**
     * Initialize Android debug bridge. This function should be called after
     * {@link DeviceManager} initialized.
     */
    public void initAdb() {
        String adbLocation = getAdbLocation();

        Log.d("init adb...");
        AndroidDebugBridge.init(true);
        AndroidDebugBridge.addDeviceChangeListener(this);
        AndroidDebugBridge.createBridge(adbLocation, true);
    }

    /**
     * Get the location of the adb command.
     *
     * @return The location of the adb location.
     */
    public static String getAdbLocation() {
        return "adb";
    }

    /**
     * Allocate devices by specified number for testing.
     * @param num the number of required device
     * @return the specified number of devices.
     */
    public TestDevice[] allocateDevices(final int num) throws DeviceNotAvailableException {

        ArrayList<TestDevice> deviceList;
        TestDevice td;
        int index = 0;

        if (num < 0) {
            throw new IllegalArgumentException();
        }
        if (num > mDevices.size()) {
            throw new DeviceNotAvailableException("The number of connected device("
                    + mDevices.size() + " is less than the specified number("
                    + num + "). Please plug in enough devices");
        }
        deviceList = new ArrayList<TestDevice>();

        while (index < mDevices.size() && deviceList.size() != num) {
            td = mDevices.get(index);
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                deviceList.add(td);
            }
            index++;
        }
        if (deviceList.size() != num) {
            throw new DeviceNotAvailableException("Can't get the specified number("
                    + num + ") of idle device(s).");
        }
        return deviceList.toArray(new TestDevice[num]);
    }

    /**
     * Get TestDevice list that available for executing tests.
     *
     * @return The device list.
     */
    public final TestDevice[] getDeviceList() {
        return mDevices.toArray(new TestDevice[mDevices.size()]);
    }

    /**
     * Get the number of all free devices.
     *
     * @return the number of all free devices
     */
    public int getCountOfFreeDevices() {
        int count = 0;
        for (TestDevice td : mDevices) {
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Append the device to the device list.
     *
     * @param device The device to be appended to the device list.
     */
    private void appendDevice(final IDevice device) {
        if (-1 == getDeviceIndex(device)) {
            TestDevice td = new TestDevice(device);
            mDevices.add(td);
        }
    }

    /**
     * Remove specified TestDevice from managed list.
     *
     * @param device The device to be removed from the device list.
     */
    private void removeDevice(final IDevice device) {
        int index = getDeviceIndex(device);
        if (index == -1) {
            Log.d("Can't find " + device + " in device list of DeviceManager");
            return;
        }
        mDevices.get(index).disconnected();
        mDevices.remove(index);
    }

    /**
     * Get the index of the specified device in the device array.
     *
     * @param device The device to be found.
     * @return The index of the device if it exists; else -1.
     */
    private int getDeviceIndex(final IDevice device) {
        TestDevice td;

        for (int index = 0; index < mDevices.size(); index++) {
            td = mDevices.get(index);
            if (td.getSerialNumber().equals(device.getSerialNumber())) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Search a <code>TestDevice</code> by serial number.
     *
     * @param deviceSerialNumber The serial number of the device to be found.
     * @return The test device, if it exists, otherwise null.
     */
    private TestDevice searchTestDevice(final String deviceSerialNumber) {
        for (TestDevice td : mDevices) {
            if (td.getSerialNumber().equals(deviceSerialNumber)) {
                return td;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public void deviceChanged(IDevice device, int changeMask) {
        Log.d("device " + device.getSerialNumber() + " changed with changeMask=" + changeMask);
        Log.d("Device state:" + device.getState());
    }

    /** {@inheritDoc} */
    public void deviceConnected(IDevice device) {
        new DeviceServiceMonitor(device).start();
    }

    /**
     * To make sure that connection between {@link AndroidDebugBridge}
     * and {@link IDevice} is initialized properly. In fact, it just make sure
     * the sync service isn't null and device's build values are collected
     * before appending device.
     */
    private class DeviceServiceMonitor extends Thread {
        private IDevice mDevice;

        public DeviceServiceMonitor(IDevice device) {
            mDevice = device;
        }

        @Override
        public void run() {
            try {
               while (mDevice.getSyncService() == null || mDevice.getPropertyCount() == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d("polling for device sync service interrupted");
                    }
                }
                CUIOutputStream.println("Device(" + mDevice + ") connected");
                if (!TestSession.isADBServerRestartedMode()) {
                    CUIOutputStream.printPrompt();
                }
                appendDevice(mDevice);
                // increment the counter semaphore to unblock threads waiting for devices
                mSemaphore.release();
            } catch (IOException e) {
                // FIXME: handle failed connection to device.
            } catch (TimeoutException e) {
                // FIXME: handle failed connection to device.
            } catch (AdbCommandRejectedException e) {
                // FIXME: handle failed connection to device.
            }
        }
    }

    /** {@inheritDoc} */
    public void deviceDisconnected(IDevice device) {
        removeDevice(device);
    }

    /**
     * Allocate device by specified Id for testing.
     * @param deviceId the ID of the test device.
     * @return a {@link TestDevice} if the specified device is free.
     */
    public TestDevice allocateFreeDeviceById(String deviceId) throws DeviceNotAvailableException {
        for (TestDevice td : mDevices) {
            if (td.getSerialNumber().equals(deviceId)) {
                if (td.getStatus() != TestDevice.STATUS_IDLE) {
                    String msg = "The specifed device(" + deviceId + ") is " +
                    td.getStatusAsString();
                    throw new DeviceNotAvailableException(msg);
                }
                return td;
            }
        }
        throw new DeviceNotAvailableException("The specified device(" +
                deviceId + "cannot be found");
    }

    /**
     * Reset the online {@link TestDevice} to STATUS_IDLE
     *
     * @param device of the specified {@link TestDevice}
     */
    public void resetTestDevice(final TestDevice device) {
        if (device.getStatus() != TestDevice.STATUS_OFFLINE) {
            device.setStatus(TestDevice.STATUS_IDLE);
        }
    }

    /**
     * Restart ADB server.
     *
     * @param ts The test session.
     */
    public void restartADBServer(TestSession ts) throws DeviceDisconnectedException {
        try {
            Thread.sleep(SHORT_DELAY); // time to collect outstanding logs
            Log.i("Restarting device ...");
            rebootDevice(ts);
            Log.i("Restart complete.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reboot the device.
     *
     * @param ts The test session.
     */
    private void rebootDevice(TestSession ts) throws InterruptedException,
                DeviceDisconnectedException {

        String deviceSerialNumber = ts.getDeviceId();
        if (!deviceSerialNumber.toLowerCase().startsWith("emulator")) {
            // try to reboot the device using the command line adb
            // TODO: do we need logic to retry this
            executeCommand("adb -s " + deviceSerialNumber + " reboot");
            // wait to make sure the reboot gets through before we tear down the connection

            // TODO: this is flaky, no guarantee device has actually rebooted, host should wait till
            // device goes offline
            Thread.sleep(REBOOT_DELAY);

            int attempts = 0;
            boolean deviceConnected = false;
            while (!deviceConnected && (attempts < MAX_ADB_RESTART_ATTEMPTS)) {
                AndroidDebugBridge.disconnectBridge();

                // kill the server while the device is rebooting
                executeCommand("adb kill-server");

                // Reset the device counter semaphore. We will wait below until at least one device
                // has come online. This can happen any time during or after the call to
                // createBridge(). The counter gets increased by the DeviceServiceMonitor when a
                // device is added.
                mSemaphore.drainPermits();
                AndroidDebugBridge.createBridge(getAdbLocation(), true);

                boolean deviceFound = false;
                while (!deviceFound) {
                    // wait until at least one device has been added
                    mSemaphore.tryAcquire(LONG_DELAY, TimeUnit.MILLISECONDS);
                    TestDevice device = searchTestDevice(deviceSerialNumber);
                    if (device != null) {
                        ts.setTestDevice(device);
                        deviceFound = true;
                        deviceConnected = device.waitForBootComplete();
                        // After boot is complete, the ADB connection sometimes drops
                        // for a short time. Wait for things to stabilize.
                        try {
                            Thread.sleep(POST_BOOT_DELAY);
                        } catch (InterruptedException ignored) {
                            // ignore
                        }
                        // If the connection dropped during the sleep above, the TestDevice
                        // instance is no longer valid.
                        TestDevice newDevice = searchTestDevice(deviceSerialNumber);
                        if (newDevice != null) {
                            ts.setTestDevice(newDevice);
                            if (newDevice != device) {
                                // the connection was dropped or a second reboot occurred
                                // TODO: replace the hardcoded /sdcard
                                String cmd = String.format("adb -s %s shell bugreport -o " +
                                            "/sdcard/bugreports/doubleReboot", deviceSerialNumber);
                                executeCommand(cmd);
                            }
                        } else {
                            // connection dropped and has not come back up
                            deviceFound = false; // go wait for next semaphore permit
                        }
                    }
                }
                attempts += 1;
            }
        }
    }

    /**
     * Execute the given command and wait for its completion.
     *
     * @param command The command to be executed.
     * @return True if the command was executed successfully, otherwise false.
     */
    private boolean executeCommand(String command) {
        Log.d("executeCommand(): cmd=" + command);
        try {
            Process proc = Runtime.getRuntime().exec(command);
            TimeoutThread tt = new TimeoutThread(proc, SHORT_DELAY);
            tt.start();
            proc.waitFor(); // ignore exit value
            tt.interrupt(); // wake timeout thread
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    class TimeoutThread extends Thread {
        Process mProcess;
        long mTimeout;

        TimeoutThread(Process process, long timeout) {
            mProcess = process;
            mTimeout = timeout;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(mTimeout);
            } catch (InterruptedException e) {
                // process has already completed
                return;
            }
            // destroy process and wake up thread waiting for its completion
            mProcess.destroy();
        }
    }
}
