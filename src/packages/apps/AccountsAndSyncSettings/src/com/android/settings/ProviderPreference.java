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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;

/**
 * ProviderPreference is used to display an image to the left of a provider name.
 * The preference ultimately calls AccountManager.addAccount() for the account type.
 */
public class ProviderPreference extends Preference {
    private Drawable mProviderIcon;
    private ImageView mProviderIconView;
    private CharSequence mProviderName;
    private String mAccountType;

    public ProviderPreference(Context context, String accountType, Drawable icon, CharSequence providerName) {
        super(context);
        mAccountType = accountType;
        mProviderIcon = icon;
        mProviderName = providerName;
        setLayoutResource(R.layout.provider_preference);
        setPersistent(false);
        setTitle(mProviderName);
        //setSummary(mProviderName);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mProviderIconView = (ImageView) view.findViewById(R.id.providerIcon);
        mProviderIconView.setImageDrawable(mProviderIcon);
        setTitle(mProviderName);
        //setSummary(mProviderName);
    }

    public String getAccountType() {
        return mAccountType;
    }
}
