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

package android.hardware.cts;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Sensor.class)
public class SensorTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getPower",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getResolution",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getVendor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getMaximumRange",
            args = {}
        )
    })
    public void testSensorOperations() {
        // Because we can't know every sensors unit details, so we can't assert
        // get values with specified values.
        final SensorManager mSensorManager =
            (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        assertNotNull(sensors);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean hasAccelerometer = getContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        // accelerometer sensor is optional
        if (hasAccelerometer) {
            assertEquals(Sensor.TYPE_ACCELEROMETER, sensor.getType());
            assertSensorValues(sensor);
        } else {
            assertNull(sensor);
        }

        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        boolean hasCompass = getContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_SENSOR_COMPASS);
        // compass sensor is optional
        if (hasCompass) {
            assertEquals(Sensor.TYPE_MAGNETIC_FIELD, sensor.getType());
            assertSensorValues(sensor);
        } else {
            assertNull(sensor);
        }

        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        // orientation sensor is required if the device can physically implement it
        if (hasCompass && hasAccelerometer) {
            assertEquals(Sensor.TYPE_ORIENTATION, sensor.getType());
            assertSensorValues(sensor);
        }

        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        // temperature sensor is optional
        if (sensor != null) {
            assertEquals(Sensor.TYPE_TEMPERATURE, sensor.getType());
            assertSensorValues(sensor);
        }
    }

    private void assertSensorValues(Sensor sensor) {
        assertTrue(sensor.getMaximumRange() >= 0);
        assertTrue(sensor.getPower() >= 0);
        assertTrue(sensor.getResolution() >= 0);
        assertNotNull(sensor.getVendor());
        assertTrue(sensor.getVersion() > 0);
    }
}
