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

import android.test.AndroidTestCase;

/**
 * Base class for tests for {@link IconLoader} subclasses.
 *
 */
public abstract class IconLoaderTest extends AndroidTestCase {

    protected IconLoader mLoader;

    @Override
    protected void setUp() throws Exception {
        mLoader = create();
    }

    protected abstract IconLoader create() throws Exception;

    public void testGetIcon() {
        assertNull(mLoader.getIcon(null));
        assertNull(mLoader.getIcon(""));
        assertNull(mLoader.getIcon("0"));
        assertNotNull(mLoader.getIcon(String.valueOf(android.R.drawable.star_on)));
    }

}
