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

import com.google.common.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.List;

/**
 * Dummy {@link Logger} implementation.
 */
@VisibleForTesting
public class NoLogger implements Logger {

    public NoLogger() {
    }

    public void logStart(int latency, String intentSource, Corpus corpus,
            List<Corpus> orderedCorpora) {
    }

    public void logSuggestionClick(int position,
            SuggestionCursor suggestionCursor, Collection<Corpus> queriedCorpora,
            int clickType) {
    }

    public void logSearch(Corpus corpus, int startMethod, int numChars) {
    }

    public void logVoiceSearch(Corpus corpus) {
    }

    public void logExit(SuggestionCursor suggestionCursor, int numChars) {
    }

    public void logLatency(CorpusResult result) {
    }

}
