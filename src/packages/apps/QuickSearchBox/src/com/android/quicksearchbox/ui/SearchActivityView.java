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

package com.android.quicksearchbox.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

/**
 * Finishes the containing activity on BACK, even if input method is showing.
 */
public class SearchActivityView extends RelativeLayout {

    public SearchActivityView(Context context) {
        super(context);
    }

    public SearchActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Activity getActivity() {
        Context context = getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        } else {
            return null;
        }
    }

    /**
     * Hides the input method.
     */
    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    /**
     * Overrides the handling of the back key to dismiss the activity.
     */
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        Activity activity = getActivity();
        if (activity != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    state.startTracking(event, this);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP
                        && !event.isCanceled() && state.isTracking(event)) {
                    hideInputMethod();
                    activity.onBackPressed();
                    return true;
                }
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
