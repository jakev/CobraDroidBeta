/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.cellbroadcastreceiver;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Settings activity for the cell broadcast receiver.
 */
public class CellBroadcastSettings extends PreferenceActivity {
    
    // Preference key for whether to enable emergency notifications (default enabled).
    public static final String KEY_ENABLE_EMERGENCY_ALERTS = "enable_emergency_alerts";

    // Duration of alert sound (in seconds).
    public static final String KEY_ALERT_SOUND_DURATION = "alert_sound_duration";

    // Default alert duration (in seconds).
    public static final String ALERT_SOUND_DEFAULT_DURATION = "4";

    // Speak contents of alert after playing the alert sound.
    public static final String KEY_ENABLE_ALERT_SPEECH = "enable_alert_speech";

    // Preference category for ETWS related settings.
    public static final String KEY_CATEGORY_ETWS_SETTINGS = "category_etws_settings";

    // Whether to display ETWS test messages (default is disabled).
    public static final String KEY_ENABLE_ETWS_TEST_ALERTS = "enable_etws_test_alerts";
    
    // Preference category for CMAS related settings.
    public static final String KEY_CATEGORY_CMAS_SETTINGS = "category_cmas_settings";

    // Whether to display CMAS imminent threat notifications (default is enabled).
    public static final String KEY_ENABLE_CMAS_IMMINENT_THREAT_ALERTS =
            "enable_cmas_imminent_threat_alerts";

    // Whether to display CMAS amber alert messages (default is disabled).
    public static final String KEY_ENABLE_CMAS_AMBER_ALERTS = "enable_cmas_amber_alerts";

    // Whether to display CMAS monthly test messages (default is disabled).
    public static final String KEY_ENABLE_CMAS_TEST_ALERTS = "enable_cmas_test_alerts";

    // Preference category for Brazil specific settings.
    public static final String KEY_CATEGORY_BRAZIL_SETTINGS = "category_brazil_settings";

    // Preference key for whether to enable channel 50 notifications
    // Enabled by default for phones sold in Brazil, otherwise this setting may be hidden.
    public static final String KEY_ENABLE_CHANNEL_50_ALERTS = "enable_channel_50_alerts";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Resources res = getResources();
        if (!res.getBoolean(R.bool.show_etws_settings)) {
            getPreferenceScreen().removePreference(findPreference(KEY_CATEGORY_ETWS_SETTINGS));
        }
        if (!res.getBoolean(R.bool.show_cmas_settings)) {
            getPreferenceScreen().removePreference(findPreference(KEY_CATEGORY_CMAS_SETTINGS));
        }
        if (!res.getBoolean(R.bool.show_brazil_settings)) {
            getPreferenceScreen().removePreference(findPreference(KEY_CATEGORY_BRAZIL_SETTINGS));
        }

        ListPreference duration = (ListPreference) findPreference(KEY_ALERT_SOUND_DURATION);
        duration.setSummary(duration.getEntry());
        duration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                final ListPreference listPref = (ListPreference) pref;
                final int idx = listPref.findIndexOfValue((String) newValue);
                listPref.setSummary(listPref.getEntries()[idx]);
                return true;
            }
        });

        Preference.OnPreferenceChangeListener startConfigServiceListener =
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference pref, Object newValue) {
                        CellBroadcastReceiver.startConfigService(pref.getContext());
                        return true;
                    }
                };
        Preference enablePwsAlerts = findPreference(KEY_ENABLE_EMERGENCY_ALERTS);
        if (enablePwsAlerts != null) {
            enablePwsAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
        }
        Preference enableChannel50Alerts = findPreference(KEY_ENABLE_CHANNEL_50_ALERTS);
        if (enableChannel50Alerts != null) {
            enableChannel50Alerts.setOnPreferenceChangeListener(startConfigServiceListener);
        }
    }
}
