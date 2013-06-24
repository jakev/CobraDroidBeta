/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.WeakHashMap;

/**
 * Icon loader that caches the results of another icon loader.
 *
 */
public class CachingIconLoader implements IconLoader {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.CachingIconLoader";

    private final IconLoader mWrapped;

    private final WeakHashMap<String, Drawable.ConstantState> mIconCache;

    /**
     * Creates a new caching icon loader.
     *
     * @param wrapped IconLoader whose results will be cached.
     */
    public CachingIconLoader(IconLoader wrapped) {
        mWrapped = wrapped;
        mIconCache = new WeakHashMap<String, Drawable.ConstantState>();
    }

    public Drawable getIcon(String drawableId) {
        if (DBG) Log.d(TAG, "getIcon(" + drawableId + ")");
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        Drawable drawable = checkIconCache(drawableId);
        if (drawable != null) {
            return drawable;
        }
        drawable = mWrapped.getIcon(drawableId);
        storeInIconCache(drawableId, drawable);
        return drawable;
    }

    public Uri getIconUri(String drawableId) {
        return mWrapped.getIconUri(drawableId);
    }

    private Drawable checkIconCache(String drawableId) {
        Drawable.ConstantState cached = mIconCache.get(drawableId);
        if (cached == null) {
            return null;
        }
        if (DBG) Log.d(TAG, "Found icon in cache: " + drawableId);
        return cached.newDrawable();
    }

    private void storeInIconCache(String resourceUri, Drawable drawable) {
        if (drawable != null) {
            mIconCache.put(resourceUri, drawable.getConstantState());
        }
    }
}
