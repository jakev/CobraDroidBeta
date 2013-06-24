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

package android.net.wifi.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.test.AndroidTestCase;

@TestTargetClass(WifiInfo.class)
public class WifiInfoTest extends AndroidTestCase {
    private static class MySync {
        int expectedState = STATE_NULL;
    }

    private WifiManager mWifiManager;
    private WifiLock mWifiLock;
    private static MySync mMySync;

    private static final int STATE_NULL = 0;
    private static final int STATE_WIFI_CHANGING = 1;
    private static final int STATE_WIFI_CHANGED = 2;

    private static final String TAG = "WifiInfoTest";
    private static final int TIMEOUT_MSEC = 6000;
    private static final int WAIT_MSEC = 60;
    private static final int DURATION = 10000;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                synchronized (mMySync) {
                    mMySync.expectedState = STATE_WIFI_CHANGED;
                    mMySync.notify();
                }
            }
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMySync = new MySync();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mWifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        assertNotNull(mWifiManager);
        mWifiLock = mWifiManager.createWifiLock(TAG);
        mWifiLock.acquire();
        if (!mWifiManager.isWifiEnabled())
            setWifiEnabled(true);
        Thread.sleep(DURATION);
        assertTrue(mWifiManager.isWifiEnabled());
        mMySync.expectedState = STATE_NULL;
    }

    @Override
    protected void tearDown() throws Exception {
        mWifiLock.release();
        mContext.unregisterReceiver(mReceiver);
        if (!mWifiManager.isWifiEnabled())
            setWifiEnabled(true);
        Thread.sleep(DURATION);
        super.tearDown();
    }

    private void setWifiEnabled(boolean enable) throws Exception {
        synchronized (mMySync) {
            mMySync.expectedState = STATE_WIFI_CHANGING;
            assertTrue(mWifiManager.setWifiEnabled(enable));
            long timeout = System.currentTimeMillis() + TIMEOUT_MSEC;
            while (System.currentTimeMillis() < timeout
                    && mMySync.expectedState == STATE_WIFI_CHANGING)
                mMySync.wait(WAIT_MSEC);
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getMacAddress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getIpAddress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getDetailedStateOf",
            args = {android.net.wifi.SupplicantState.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getNetworkId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getSSID",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getBSSID",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getSupplicantState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getLinkSpeed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getRssi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getHiddenSSID",
            args = {}
        )
    })
    public void testWifiInfoProperties() throws Exception {
        // this test case should in Wifi environment
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        assertNotNull(wifiInfo);
        assertNotNull(wifiInfo.toString());
        SupplicantState.isValidState(wifiInfo.getSupplicantState());
        WifiInfo.getDetailedStateOf(SupplicantState.DISCONNECTED);
        wifiInfo.getSSID();
        wifiInfo.getBSSID();
        wifiInfo.getIpAddress();
        wifiInfo.getLinkSpeed();
        wifiInfo.getRssi();
        wifiInfo.getHiddenSSID();
        wifiInfo.getMacAddress();
        setWifiEnabled(false);
        Thread.sleep(DURATION);
        wifiInfo = mWifiManager.getConnectionInfo();
        assertEquals(-1, wifiInfo.getNetworkId());
        assertEquals(WifiManager.WIFI_STATE_DISABLED, mWifiManager.getWifiState());
    }

}
