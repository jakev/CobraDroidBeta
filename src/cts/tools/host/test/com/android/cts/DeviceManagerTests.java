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

/**
 * Test the device manager.
 */
public class DeviceManagerTests extends CtsTestBase {

    private TestDevice d1, d2, d3;
    private static String d1SerialNumber = "mock_device1";
    private static String d2SerialNumber = "mock_device2";
    private static String d3SerialNumber = "mock_device3";

    /** {@inheritDoc} */
    @Override
    public void setUp() {
        d1 = new TestDevice(d1SerialNumber);
        d2 = new TestDevice(d2SerialNumber);
        d3 = new TestDevice(d3SerialNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void tearDown() {
        d1 = d2 = d3 = null;
    }

    /**
     * Test allocating devices.
     */
    public void testAllocateDevices() throws DeviceNotAvailableException {
        DeviceManager dm = new DeviceManager();
        TestDevice[] devices;

        try {
            devices = dm.allocateDevices(1);
            fail();
        } catch (DeviceNotAvailableException e) {
            // pass
        }
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);

        try {
            devices = dm.allocateDevices(-1);
            fail();
        } catch (IllegalArgumentException e) {
            // pass
        }

        devices = dm.allocateDevices(0);
        assertEquals(0, devices.length);

        try {
            devices = dm.allocateDevices(4);
            fail();
        } catch (DeviceNotAvailableException e) {
            // pass
        }

        devices = dm.allocateDevices(2);
        assertEquals(2, devices.length);
        assertEquals(d1SerialNumber, devices[0].getSerialNumber());
        assertEquals(d2SerialNumber, devices[1].getSerialNumber());

        d1.setStatus(TestDevice.STATUS_BUSY);
        d3.setStatus(TestDevice.STATUS_OFFLINE);
        try {
            devices = dm.allocateDevices(2);
            fail();
        } catch (DeviceNotAvailableException e) {
            // pass
        }
        devices = dm.allocateDevices(1);
        assertEquals(1, devices.length);
        assertEquals(d2SerialNumber, devices[0].getSerialNumber());
    }

    /**
     * Test getting device list.
     */
    public void testGetDeviceList() {
        DeviceManager dm = new DeviceManager();
        TestDevice[] devices;

        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        devices = dm.getDeviceList();
        assertEquals(3, devices.length);
        assertEquals(d1SerialNumber, devices[0].getSerialNumber());
        assertEquals(d2SerialNumber, devices[1].getSerialNumber());
        assertEquals(d3SerialNumber, devices[2].getSerialNumber());
    }

    /**
     * Test getting the number of all available devices.
     */
    public void testGetNumOfAllAvailableDevices() {
        DeviceManager dm = new DeviceManager();
        assertEquals(0, dm.getCountOfFreeDevices());

        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);
        assertEquals(1, dm.getCountOfFreeDevices());
    }

    /**
     * Test the default status of test device.
     */
    public void testDeviceDefaultStatus() {
        assertEquals(TestDevice.STATUS_IDLE, d1.getStatus());
        assertEquals(TestDevice.STATUS_IDLE, d2.getStatus());
        assertEquals(TestDevice.STATUS_IDLE, d3.getStatus());
    }

    /**
     * Test allocating device by the specified ID.
     */
    public void testAllocateDeviceById() throws DeviceNotAvailableException {
        DeviceManager dm = new DeviceManager();
        TestDevice device;

        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.mDevices.add(d1);
        dm.mDevices.add(d2);
        dm.mDevices.add(d3);

        try {
            device = dm.allocateFreeDeviceById("fake device");
            fail();
        } catch (DeviceNotAvailableException e) {
            // pass
        }

        device = dm.allocateFreeDeviceById(d3SerialNumber);
        assertEquals(d3SerialNumber, device.getSerialNumber());
    }

    /**
     * Test resetting test devices.
     */
    public void testResetTestDevices() {
        DeviceManager dm = new DeviceManager();

        d1.setStatus(TestDevice.STATUS_BUSY);
        d2.setStatus(TestDevice.STATUS_OFFLINE);
        dm.resetTestDevice(d1);
        dm.resetTestDevice(d2);
        dm.resetTestDevice(d3);
        assertEquals(d1.getStatus(), TestDevice.STATUS_IDLE);
        assertEquals(d2.getStatus(), TestDevice.STATUS_OFFLINE);
        assertEquals(d3.getStatus(), TestDevice.STATUS_IDLE);
    }
}
