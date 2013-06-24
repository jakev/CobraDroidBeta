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

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.telephony.SmsCbConstants;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.internal.telephony.gsm.SmsCbHeader;

import static com.android.cellbroadcastreceiver.CellBroadcastReceiver.DBG;

/**
 * This service manages enabling and disabling ranges of message identifiers
 * that the radio should listen for. It operates independently of the other
 * services and runs at boot time and after exiting airplane mode.
 *
 * Note that the entire range of emergency channels is enabled. Test messages
 * and lower priority broadcasts are filtered out in CellBroadcastAlertService
 * if the user has not enabled them in settings.
 *
 * TODO: add notification to re-enable channels after a radio reset.
 */
public class CellBroadcastConfigService extends IntentService {
    private static final String TAG = "CellBroadcastConfigService";

    static final String ACTION_ENABLE_CHANNELS = "ACTION_ENABLE_CHANNELS";

    public CellBroadcastConfigService() {
        super(TAG);          // use class name for worker thread name
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_ENABLE_CHANNELS.equals(intent.getAction())) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                Resources res = getResources();

                boolean enableEmergencyAlerts = prefs.getBoolean(
                        CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS, true);

                boolean enableChannel50Alerts = res.getBoolean(R.bool.show_brazil_settings) &&
                        prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_CHANNEL_50_ALERTS, true);

                SmsManager manager = SmsManager.getDefault();
                if (enableEmergencyAlerts) {
                    if (DBG) Log.d(TAG, "enabling emergency cell broadcast channels");
                    manager.enableCellBroadcastRange(
                            SmsCbConstants.MESSAGE_ID_PWS_FIRST_IDENTIFIER,
                            SmsCbConstants.MESSAGE_ID_PWS_LAST_IDENTIFIER);
                    if (DBG) Log.d(TAG, "enabled emergency cell broadcast channels");
                } else {
                    // we may have enabled these channels previously, so try to disable them
                    if (DBG) Log.d(TAG, "disabling emergency cell broadcast channels");
                    manager.disableCellBroadcastRange(
                            SmsCbConstants.MESSAGE_ID_PWS_FIRST_IDENTIFIER,
                            SmsCbConstants.MESSAGE_ID_PWS_LAST_IDENTIFIER);
                    if (DBG) Log.d(TAG, "disabled emergency cell broadcast channels");
                }

                if (enableChannel50Alerts) {
                    if (DBG) Log.d(TAG, "enabling cell broadcast channel 50");
                    manager.enableCellBroadcast(50);
                    if (DBG) Log.d(TAG, "enabled cell broadcast channel 50");
                } else {
                    if (DBG) Log.d(TAG, "disabling cell broadcast channel 50");
                    manager.disableCellBroadcast(50);
                    if (DBG) Log.d(TAG, "disabled cell broadcast channel 50");
                }
            } catch (Exception ex) {
                Log.e(TAG, "exception enabling cell broadcast channels", ex);
            }
        }
    }
}
