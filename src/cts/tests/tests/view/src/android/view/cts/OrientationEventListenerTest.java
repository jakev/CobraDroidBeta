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

package android.view.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.test.AndroidTestCase;
import android.view.OrientationEventListener;

/**
 * Test {@link OrientationEventListener}.
 */
@TestTargetClass(OrientationEventListener.class)
public class OrientationEventListenerTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "OrientationEventListener",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "OrientationEventListener",
            args = {Context.class, int.class}
        )
    })
    public void testConstructor() {
        new MockOrientationEventListener(mContext);

        new MockOrientationEventListener(mContext, SensorManager.SENSOR_DELAY_UI);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test {@link OrientationEventListener#enable()}. "
                    + "This method is simply called to make sure that no exception is thrown. "
                    + "The registeration of the listener can not be tested becuase there is "
                    + "no way to simulate sensor events",
            method = "enable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test {@link OrientationEventListener#disable()}. "
                    + "This method is simply called to make sure that no exception is thrown. "
                    + "The registeration of the listener can not be tested becuase there is "
                    + "no way to simulate sensor events",
            method = "disable",
            args = {}
        )
    })
    @ToBeFixed(explanation = "Can not simulate sensor events on the emulator.")
    public void testEnableAndDisable() {
        MockOrientationEventListener listener = new MockOrientationEventListener(mContext);
        listener.enable();
        listener.disable();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "canDetectOrientation",
        args = {}
    )
    public void testCanDetectOrientation() {
        SensorManager sm = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        // Orientation can only be detected if there is an accelerometer
        boolean hasSensor = (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
        
        MockOrientationEventListener listener = new MockOrientationEventListener(mContext);
        assertEquals(hasSensor, listener.canDetectOrientation());
    }

    private static class MockOrientationEventListener extends OrientationEventListener {
        public MockOrientationEventListener(Context context) {
            super(context);
        }

        public MockOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
        }
    }
}
