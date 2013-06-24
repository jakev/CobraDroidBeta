/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.apps.tag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

/**
 * A browsing {@link Activity} that displays the saved tags in categories under tabs.
 */
public class TagBrowserActivity extends TabActivity implements DialogInterface.OnClickListener {

    private static final int DIALOG_NFC_OFF = 1;
    private static final String PREF_KEY_SHOW_INTRO = "showintro";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Resources res = getResources();
        TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tags")
                .setIndicator(getText(R.string.tab_tags),
                        res.getDrawable(R.drawable.ic_tab_all_tags))
                .setContent(new Intent().setClass(this, TagList.class)));

        tabHost.addTab(tabHost.newTabSpec("starred")
                .setIndicator(getText(R.string.tab_starred),
                        res.getDrawable(R.drawable.ic_tab_starred))
                .setContent(new Intent().setClass(this, TagList.class)
                        .putExtra(TagList.EXTRA_SHOW_STARRED_ONLY, true)));

        tabHost.addTab(tabHost.newTabSpec("mytag")
                .setIndicator(getText(R.string.tab_my_tag),
                        res.getDrawable(R.drawable.ic_tab_my_tag))
                .setContent(new Intent().setClass(this, MyTagList.class)));

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        if (!preferences.getBoolean(PREF_KEY_SHOW_INTRO, false)) {
            preferences.edit().putBoolean(PREF_KEY_SHOW_INTRO, true).apply();
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Restore the last active tab
        SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        getTabHost().setCurrentTabByTag(prefs.getString("tab", "tags"));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see if NFC is on
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc == null || !nfc.isEnabled()) {
            showDialog(DIALOG_NFC_OFF);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Save the active tab
        SharedPreferences.Editor edit = getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();
        edit.putString("tab", getTabHost().getCurrentTabTag());
        edit.apply();
    }

    @Override
    public Dialog onCreateDialog(int dialogId, Bundle args) {
        if (dialogId == DIALOG_NFC_OFF) {
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_nfc_off)
                    .setMessage(R.string.dialog_text_nfc_off)
                    .setPositiveButton(R.string.button_settings, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setCancelable(true)
                    .create();
        }

        throw new IllegalArgumentException("Unknown dialog id " + dialogId);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Thake the user to the wireless settings panel, where they can enable NFC
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        }
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                HelpUtils.openHelp(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
