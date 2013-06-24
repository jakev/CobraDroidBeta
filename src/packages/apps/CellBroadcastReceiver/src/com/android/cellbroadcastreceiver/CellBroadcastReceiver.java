/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.cellbroadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class CellBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "CellBroadcastReceiver";
    static final boolean DBG = true;    // TODO: change to false before ship

    @Override
    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        if (DBG) Log.d(TAG, "onReceive " + intent);

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            startConfigService(context);
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean airplaneModeOn = intent.getBooleanExtra("state", false);
            if (!airplaneModeOn) {
                startConfigService(context);
            }
        } else if (Telephony.Sms.Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION.equals(action) ||
                Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION.equals(action)) {
            // If 'privileged' is false, it means that the intent was delivered to the base
            // no-permissions receiver class.  If we get an SMS_CB_RECEIVED message that way, it
            // means someone has tried to spoof the message by delivering it outside the normal
            // permission-checked route, so we just ignore it.
            if (privileged) {
                intent.setClass(context, CellBroadcastAlertService.class);
                context.startService(intent);
            } else {
                Log.e(TAG, "ignoring unprivileged action received " + action);
            }
        } else {
            Log.w(TAG, "onReceive() unexpected action " + action);
        }
    }

    /**
     * Tell {@link CellBroadcastConfigService} to enable the CB channels.
     * @param context the broadcast receiver context
     */
    static void startConfigService(Context context) {
        if (phoneIsCdma()) {
            Log.d(TAG, "CDMA phone detected; doing nothing");
        } else {
            Intent serviceIntent = new Intent(CellBroadcastConfigService.ACTION_ENABLE_CHANNELS,
                    null, context, CellBroadcastConfigService.class);
            context.startService(serviceIntent);
        }
    }

    /**
     * @return true if the phone is a CDMA phone type
     */
    private static boolean phoneIsCdma() {
        boolean isCdma = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.getActivePhoneType() failed", e);
        }
        return isCdma;
    }
}
