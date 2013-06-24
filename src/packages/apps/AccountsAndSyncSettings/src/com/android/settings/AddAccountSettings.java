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
import android.accounts.AuthenticatorException;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.accounts.AccountManagerCallback;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class AddAccountSettings extends AccountPreferenceBase {
    private static final String TAG = "AccountSettings";
    private static final boolean LDEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private String[] mAuthorities;
    private PreferenceGroup mAddAccountGroup;
    private ArrayList<ProviderEntry> mProviderList = new ArrayList<ProviderEntry>();;

    private static class ProviderEntry {
        private final CharSequence name;
        private final String type;
        ProviderEntry(CharSequence providerName, String accountType) {
            name = providerName;
            type = accountType;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.add_account_screen);
        addPreferencesFromResource(R.xml.add_account_settings);
        mAuthorities = getIntent().getStringArrayExtra(AUTHORITIES_FILTER_KEY);
        mAddAccountGroup = getPreferenceScreen();
        updateAuthDescriptions();
    }

    @Override
    protected void onAuthDescriptionsUpdated() {
        // Create list of providers to show on preference screen
        for (int i = 0; i < mAuthDescs.length; i++) {
            String accountType = mAuthDescs[i].type;
            CharSequence providerName = getLabelForType(accountType);

            // Skip preferences for authorities not specified. If no authorities specified,
            // then include them all.
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType);
            boolean addAccountPref = true;
            if (mAuthorities != null && mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (int k = 0; k < mAuthorities.length; k++) {
                    if (accountAuths.contains(mAuthorities[k])) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
            if (addAccountPref) {
                mProviderList.add(new ProviderEntry(providerName, accountType));
            } else {
                if (LDEBUG) Log.v(TAG, "Skipped pref " + providerName + ": has no authority we need");
            }
        }

        if (mProviderList.size() == 1) {
            // If there's only one provider that matches, just run it.
            addAccount(mProviderList.get(0).type);
            finish();
        } else if (mProviderList.size() > 0) {
            mAddAccountGroup.removeAll();
            for (ProviderEntry pref : mProviderList) {
                Drawable drawable = getDrawableForType(pref.type);
                ProviderPreference p = new ProviderPreference(this, pref.type, drawable, pref.name);
                mAddAccountGroup.addPreference(p);
            }
        } else {
            String auths = new String();
            for (String a : mAuthorities) auths += a + " ";
            Log.w(TAG, "No providers found for authorities: " + auths);
        }
    }

    private AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        public void run(AccountManagerFuture<Bundle> future) {
            boolean accountAdded = false;
            try {
                Bundle bundle = future.getResult();
                bundle.keySet();
                accountAdded = true;
                if (LDEBUG) Log.d(TAG, "account added: " + bundle);
            } catch (OperationCanceledException e) {
                if (LDEBUG) Log.d(TAG, "addAccount was canceled");
            } catch (IOException e) {
                if (LDEBUG) Log.d(TAG, "addAccount failed: " + e);
            } catch (AuthenticatorException e) {
                if (LDEBUG) Log.d(TAG, "addAccount failed: " + e);
            } finally {
                finish();
            }
        }
    };

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference instanceof ProviderPreference) {
            ProviderPreference pref = (ProviderPreference) preference;
            if (LDEBUG) Log.v(TAG, "Attempting to add account of type " + pref.getAccountType());
            addAccount(pref.getAccountType());
        }
        return true;
    }

    private void addAccount(String accountType) {
        AccountManager.get(this).addAccount(
                accountType,
                null, /* authTokenType */
                null, /* requiredFeatures */
                null, /* addAccountOptions */
                this,
                mCallback,
                null /* handler */);
    }
}
