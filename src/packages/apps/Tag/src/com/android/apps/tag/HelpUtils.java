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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import java.util.Locale;

/**
 * Utilities for building help content.
 */
public class HelpUtils {

    // Non-instantiable.
    private HelpUtils() {}

    private static String replaceLocale(String str) {
        // Substitute locale if present in string
        if (str.contains("%locale%")) {
            Locale locale = Locale.getDefault();
            str = str.replace("%locale%", locale.getLanguage());
        }
        return str;
    }

    /**
     * Opens up the help page in a browser.
     */
    public static void openHelp(final Context context) {
        Uri uri = Uri.parse(replaceLocale(context.getString(R.string.more_info_url)));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
    }
}
