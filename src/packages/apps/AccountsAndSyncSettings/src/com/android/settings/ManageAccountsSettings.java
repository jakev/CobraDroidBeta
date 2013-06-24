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

package com.android.settings;

import com.android.providers.subscribedfeeds.R;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SyncInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncStatusInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

public class ManageAccountsSettings extends AccountPreferenceBase implements View.OnClickListener {
    private static final String AUTO_SYNC_CHECKBOX_KEY = "syncAutomaticallyCheckBox";
    private static final String MANAGE_ACCOUNTS_CATEGORY_KEY = "manageAccountsCategory";
    private static final String BACKGROUND_DATA_CHECKBOX_KEY = "backgroundDataCheckBox";
    private static final int DIALOG_DISABLE_BACKGROUND_DATA = 1;

    private CheckBoxPreference mBackgroundDataCheckBox;
    private PreferenceCategory mManageAccountsCategory;
    private String[] mAuthorities;
    private TextView mErrorInfoView;
    private Button mAddAccountButton;
    private CheckBoxPreference mAutoSyncCheckbox;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.manage_accounts_screen);
        addPreferencesFromResource(R.xml.manage_accounts_settings);

        mErrorInfoView = (TextView)findViewById(R.id.sync_settings_error_info);
        mErrorInfoView.setVisibility(View.GONE);
        mErrorInfoView.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.ic_list_syncerror), null, null, null);

        mBackgroundDataCheckBox = (CheckBoxPreference) findPreference(BACKGROUND_DATA_CHECKBOX_KEY);
        mAutoSyncCheckbox = (CheckBoxPreference) findPreference(AUTO_SYNC_CHECKBOX_KEY);

        mManageAccountsCategory =
                (PreferenceCategory) findPreference(MANAGE_ACCOUNTS_CATEGORY_KEY);
        mAuthorities = getIntent().getStringArrayExtra(AUTHORITIES_FILTER_KEY);
        mAddAccountButton = (Button) findViewById(R.id.add_account_button);
        mAddAccountButton.setOnClickListener(this);

        AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
        updateAuthDescriptions();
    }

    @Override
    public void onDestroy() {
        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference == mBackgroundDataCheckBox) {
            ConnectivityManager connManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean oldBackgroundDataSetting = connManager.getBackgroundDataSetting();
            boolean backgroundDataSetting = mBackgroundDataCheckBox.isChecked();
            if (oldBackgroundDataSetting != backgroundDataSetting) {
                if (backgroundDataSetting) {
                    setBackgroundDataInt(true);
                    onSyncStateUpdated();
                } else {
                    // This will get unchecked only if the user hits "Ok"
                    mBackgroundDataCheckBox.setChecked(true);
                    showDialog(DIALOG_DISABLE_BACKGROUND_DATA);
                }
            }
        } else if (preference == mAutoSyncCheckbox) {
            ContentResolver.setMasterSyncAutomatically(mAutoSyncCheckbox.isChecked());
            onSyncStateUpdated();
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DISABLE_BACKGROUND_DATA:
                final CheckBoxPreference pref =
                    (CheckBoxPreference) findPreference(BACKGROUND_DATA_CHECKBOX_KEY);
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.background_data_dialog_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.background_data_dialog_message)
                        .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setBackgroundDataInt(false);
                                    pref.setChecked(false);
                                    onSyncStateUpdated();
                                }
                            })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        }

        return null;
    }

    private void setBackgroundDataInt(boolean enabled) {
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connManager.setBackgroundDataSetting(enabled);
    }

    @Override
    protected void onSyncStateUpdated() {
        // Set background connection state
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean backgroundDataSetting = connManager.getBackgroundDataSetting();
        mBackgroundDataCheckBox.setChecked(backgroundDataSetting);
        boolean masterSyncAutomatically = ContentResolver.getMasterSyncAutomatically();
        mAutoSyncCheckbox.setChecked(masterSyncAutomatically);

        // iterate over all the preferences, setting the state properly for each
        SyncInfo currentSync = ContentResolver.getCurrentSync();

        boolean anySyncFailed = false; // true if sync on any account failed

        // only track userfacing sync adapters when deciding if account is synced or not
        final SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
        HashSet<String> userFacing = new HashSet<String>();
        for (int k = 0, n = syncAdapters.length; k < n; k++) {
            final SyncAdapterType sa = syncAdapters[k];
            if (sa.isUserVisible()) {
                userFacing.add(sa.authority);
            }
        }
        for (int i = 0, count = mManageAccountsCategory.getPreferenceCount(); i < count; i++) {
            Preference pref = mManageAccountsCategory.getPreference(i);
            if (! (pref instanceof AccountPreference)) {
                continue;
            }

            AccountPreference accountPref = (AccountPreference) pref;
            Account account = accountPref.getAccount();
            int syncCount = 0;
            boolean syncIsFailing = false;
            final ArrayList<String> authorities = accountPref.getAuthorities();
            if (authorities != null) {
                for (String authority : authorities) {
                    SyncStatusInfo status = ContentResolver.getSyncStatus(account, authority);
                    boolean syncEnabled = ContentResolver.getSyncAutomatically(account, authority)
                            && masterSyncAutomatically
                            && backgroundDataSetting
                            && (ContentResolver.getIsSyncable(account, authority) > 0);
                    boolean authorityIsPending = ContentResolver.isSyncPending(account, authority);
                    boolean activelySyncing = currentSync != null
                            && currentSync.authority.equals(authority)
                            && new Account(currentSync.account.name, currentSync.account.type).equals(account);
                    boolean lastSyncFailed = status != null
                            && syncEnabled
                            && status.lastFailureTime != 0
                            && status.getLastFailureMesgAsInt(0)
                               != ContentResolver.SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS;
                    if (lastSyncFailed && !activelySyncing && !authorityIsPending) {
                        syncIsFailing = true;
                        anySyncFailed = true;
                    }
                    syncCount += syncEnabled && userFacing.contains(authority) ? 1 : 0;
                }
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "no syncadapters found for " + account);
                }
            }
            int syncStatus = AccountPreference.SYNC_DISABLED;
            if (syncIsFailing) {
                syncStatus = AccountPreference.SYNC_ERROR;
            } else if (syncCount == 0) {
                syncStatus = AccountPreference.SYNC_DISABLED;
            } else if (syncCount > 0) {
                syncStatus = AccountPreference.SYNC_ENABLED;
            }
            accountPref.setSyncStatus(syncStatus);
        }

        mErrorInfoView.setVisibility(anySyncFailed ? View.VISIBLE : View.GONE);
    }

    public void onAccountsUpdated(Account[] accounts) {
        mManageAccountsCategory.removeAll();
        for (int i = 0, n = accounts.length; i < n; i++) {
            final Account account = accounts[i];
            final ArrayList<String> auths = getAuthoritiesForAccountType(account.type);

            boolean showAccount = true;
            if (mAuthorities != null && auths != null) {
                showAccount = false;
                for (String requestedAuthority : mAuthorities) {
                    if (auths.contains(requestedAuthority)) {
                        showAccount = true;
                        break;
                    }
                }
            }

            if (showAccount) {
                Drawable icon = getDrawableForType(account.type);
                AccountPreference preference = new AccountPreference(this, account, icon, auths);
                mManageAccountsCategory.addPreference(preference);
            }
        }
        onSyncStateUpdated();
    }

    @Override
    protected void onAuthDescriptionsUpdated() {
        // Update account icons for all account preference items
        for (int i = 0; i < mManageAccountsCategory.getPreferenceCount(); i++) {
            AccountPreference pref = (AccountPreference) mManageAccountsCategory.getPreference(i);
            pref.setProviderIcon(getDrawableForType(pref.getAccount().type));
            pref.setSummary(getLabelForType(pref.getAccount().type));
        }
    }

    public void onClick(View v) {
        if (v == mAddAccountButton) {
            Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
            intent.putExtra(AUTHORITIES_FILTER_KEY, mAuthorities);
            startActivity(intent);
        }
    }
}
