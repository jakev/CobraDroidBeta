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

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for corpus rankers.
 */
public abstract class AbstractCorpusRanker implements CorpusRanker {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    private final Corpora mCorpora;

    // Cached list of ranked corpora. Set to null when mCorpora changes.
    private List<Corpus> mRankedCorpora;

    public AbstractCorpusRanker(Corpora corpora) {
        mCorpora = corpora;
        mCorpora.registerDataSetObserver(new CorporaObserver());
    }

    /**
     * Creates a ranked list of corpora.
     */
    protected abstract List<Corpus> rankCorpora(Corpora corpora);

    public List<Corpus> getRankedCorpora() {
        if (mRankedCorpora == null) {
            mRankedCorpora = Collections.unmodifiableList(rankCorpora(mCorpora));
        }
        return mRankedCorpora;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    protected void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    private class CorporaObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mRankedCorpora = null;
            notifyDataSetChanged();
        }
    }
}
