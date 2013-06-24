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

package com.android.quicksearchbox.ui;

import com.android.quicksearchbox.Suggestion;

import android.view.View;
import android.view.ViewGroup;

/**
 * Creates suggestion views.
 */
public interface SuggestionViewFactory {

    /**
     * Gets the number of distinct suggestion view types created by this factory.
     */
    int getSuggestionViewTypeCount();

    /**
     * Gets the view type associated with a given suggestion.
     */
    int getSuggestionViewType(Suggestion suggestion);

    /**
     * Gets a suggestion view, possibly recycling convertView.
     *
     * @param viewType The type of view to return.
     * @param convertView A view which may be re-used, or {@code null}.
     * @param parentViewType Used to create LayoutParams of the right type.
     */
    SuggestionView getSuggestionView(int viewType, View convertView, ViewGroup parentViewType);

}
