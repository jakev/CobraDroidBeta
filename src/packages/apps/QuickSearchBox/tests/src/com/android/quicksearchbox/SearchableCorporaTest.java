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
import android.test.suitebuilder.annotation.MediumTest;

import java.util.Collection;

/**
 * Tests for {@link SearchableCorpora}.
 *
 */
@MediumTest
public class SearchableCorporaTest extends AndroidTestCase {

    protected SearchableCorpora mCorpora;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockSources sources = new MockSources();
        sources.addSource(MockSource.SOURCE_1);
        sources.addSource(MockSource.SOURCE_2);
        mCorpora = new SearchableCorpora(mContext, sources, new MockCorpusFactory());
        mCorpora.update();
    }

    public void testGetAllCorpora() {
        assertNotEmpty(mCorpora.getAllCorpora());
    }

    public void testEnabledSuggestionSources() {
        assertNotNull(mCorpora.getEnabledCorpora());
    }

    public void testGetCorpusForSource() {
        assertNotNull(mCorpora.getCorpusForSource(MockSource.SOURCE_1));
        assertNotNull(mCorpora.getCorpusForSource(MockSource.SOURCE_2));
        assertNull(mCorpora.getCorpusForSource(MockSource.SOURCE_3));
    }

    static void assertEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    static void assertNotEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
    }

}
