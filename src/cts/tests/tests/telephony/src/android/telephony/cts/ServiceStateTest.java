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
package android.telephony.cts;

import android.os.Parcel;
import android.telephony.ServiceState;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(ServiceState.class)
public class ServiceStateTest extends AndroidTestCase {
    private static final String OPERATOR_ALPHA_LONG = "CtsOperatorLong";
    private static final String OPERATOR_ALPHA_SHORT = "CtsOp";
    private static final String OPERATOR_NUMERIC = "02871";

    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {Object.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIsManualSelection",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOperatorAlphaLong",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOperatorAlphaShort",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOperatorNumeric",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getRoaming",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getState",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ServiceState",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ServiceState",
        args = {Parcel.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ServiceState",
        args = {ServiceState.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "copyFrom",
        args = {ServiceState.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIsManualSelection",
        args = {boolean.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOperatorName",
        args = {String.class, String.class, String.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setRoaming",
        args = {boolean.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setState",
        args = {int.class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setStateOff",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setStateOutOfService",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
      )
    })
    public void testServiceState() {
        ServiceState serviceState = new ServiceState();

        assertEquals(0, serviceState.describeContents());

        serviceState.setStateOff();
        assertEquals(ServiceState.STATE_POWER_OFF, serviceState.getState());
        checkOffStatus(serviceState);

        serviceState.setStateOutOfService();
        assertEquals(ServiceState.STATE_OUT_OF_SERVICE, serviceState.getState());
        checkOffStatus(serviceState);

        serviceState.setState(ServiceState.STATE_IN_SERVICE);
        assertEquals(ServiceState.STATE_IN_SERVICE, serviceState.getState());

        assertFalse(serviceState.getRoaming());
        serviceState.setRoaming(true);
        assertTrue(serviceState.getRoaming());

        assertFalse(serviceState.getIsManualSelection());
        serviceState.setIsManualSelection(true);
        assertTrue(serviceState.getIsManualSelection());

        serviceState.setOperatorName(OPERATOR_ALPHA_LONG, OPERATOR_ALPHA_SHORT, OPERATOR_NUMERIC);
        assertEquals(OPERATOR_ALPHA_LONG, serviceState.getOperatorAlphaLong());
        assertEquals(OPERATOR_ALPHA_SHORT, serviceState.getOperatorAlphaShort());
        assertEquals(OPERATOR_NUMERIC, serviceState.getOperatorNumeric());

        assertTrue(serviceState.hashCode() > 0);
        assertNotNull(serviceState.toString());

        ServiceState tempServiceState = new ServiceState(serviceState);
        assertTrue(tempServiceState.equals(serviceState));

        Parcel stateParcel = Parcel.obtain();
        serviceState.writeToParcel(stateParcel, 0);
        stateParcel.setDataPosition(0);
        tempServiceState = new ServiceState(stateParcel);
        assertTrue(tempServiceState.equals(serviceState));

        MockServiceState mockServiceState = new MockServiceState();
        mockServiceState.copyFrom(serviceState);
        assertTrue(mockServiceState.equals(serviceState));
    }

    /**
     * Check the ServiceState fields in STATE_OUT_OF_SERVICE or STATE_POWER_OFF
     */
    private void checkOffStatus(ServiceState s) {
        assertFalse(s.getRoaming());
        assertNull(s.getOperatorAlphaLong());
        assertNull(s.getOperatorAlphaShort());
        assertNull(s.getOperatorNumeric());
        assertFalse(s.getIsManualSelection());
    }

    private class MockServiceState extends ServiceState {
        @Override
        protected void copyFrom(ServiceState s) {
            super.copyFrom(s);
        }
    }
}
