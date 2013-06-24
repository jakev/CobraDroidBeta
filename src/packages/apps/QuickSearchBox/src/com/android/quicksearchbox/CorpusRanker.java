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

package com.android.quicksearchbox;

import android.database.DataSetObserver;

import java.util.List;

/**
 * Orders corpora by importance.
 */
public interface CorpusRanker {

    /**
     * Gets a an ordered list of corpora.
     *
     * @return The most important corpora come first in the list. Callers should not mofify the
     *         returned list.
     */
    List<Corpus> getRankedCorpora();

    /**
     * Registers an observer that is called when the corpus list changes.
     *
     * @param observer gets notified when the corpus list changes.
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * Unregisters an observer that has previously been registered with
     * {@link #registerDataSetObserver(DataSetObserver)}
     *
     * @param observer the observer to unregister.
     */
    void unregisterDataSetObserver(DataSetObserver observer);

}
