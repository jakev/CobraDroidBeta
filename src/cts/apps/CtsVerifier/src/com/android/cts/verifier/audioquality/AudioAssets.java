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

package com.android.cts.verifier.audioquality;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class AudioAssets {

    private static final String TAG = "AudioQualityVerifier";

    private static final String STIM_BASENAME = "audioquality/stim_dt_";

    public static byte[] getStim(Context context, int which) {
        return readAsset(context, STIM_BASENAME + which);
    }

    public static byte[] getPinkNoise(Context context, int ampl, int duration) {
        return readAsset(context, "audioquality/pink_" + ampl + "_" + duration + "s");
    }

    private static byte[] readAsset(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        InputStream ais;
        try {
            ais = assetManager.open(filename);
        } catch (IOException e) {
            Log.e(TAG, "Cannot load asset " + filename, e);
            return null;
        }
        byte[] buffer = Utils.readFile(ais);
        return buffer;
    }
}
