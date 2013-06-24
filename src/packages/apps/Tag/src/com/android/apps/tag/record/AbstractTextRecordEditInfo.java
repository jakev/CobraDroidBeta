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

package com.android.apps.tag.record;

import com.android.apps.tag.R;
import com.google.common.base.Preconditions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public abstract class AbstractTextRecordEditInfo extends RecordEditInfo implements TextWatcher {
    private String mCurrentValue;
    private EditText mEditText;

    public AbstractTextRecordEditInfo(String initialValue) {
        super(UriRecord.RECORD_TYPE);
        mCurrentValue = Preconditions.checkNotNull(initialValue);
    }

    public AbstractTextRecordEditInfo(Parcel parcel) {
        super(parcel);
        mCurrentValue = parcel.readString();
    }

    public String getCurrentText() {
        return mCurrentValue;
    }

    @Override
    public Intent getPickIntent() {
        return null;
    }

    @Override
    public void handlePickResult(Context context, Intent data) {
        // Not supported.
    }

    public abstract int getLayoutId();
    
    @Override
    public View getEditView(
            Activity activity, LayoutInflater inflater,
            ViewGroup parent, EditCallbacks callbacks) {
        View view = buildEditView(activity, inflater, getLayoutId(), parent, callbacks);
        mEditText = (EditText) view.findViewById(R.id.value);
        mEditText.setText(mCurrentValue);
        mEditText.addTextChangedListener(this);
        return view;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mCurrentValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void afterTextChanged(Editable s) {
        mCurrentValue = s.toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}