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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple holder for information required for editing a {@code ParsedNdefRecord}.
 */
public abstract class RecordEditInfo implements Parcelable, View.OnClickListener {

    public interface EditCallbacks {
        void startPickForRecord(RecordEditInfo info, Intent intent);
        void deleteRecord(RecordEditInfo info);
    }

    /**
     * The record type being edited.
     */
    private final String mType;

    protected EditCallbacks mCallbacks;

    public RecordEditInfo(String type) {
        mType = type;
    }

    protected RecordEditInfo(Parcel parcel) {
        mType = parcel.readString();
    }

    public String getType() {
        return mType;
    }

    /**
     * Returns the current value of the record in edit. Can be {@code null} if not fully inputted
     * by user, or the value is invalid for any reason.
     */
    public abstract NdefRecord getValue();

    /**
     * An {@link Intent} which can be fired to retrieve content for the {@code ParsedNdefRecord}.
     * Can be null, if no external {@link Activity} is required.
     */
    public abstract Intent getPickIntent();

    /**
     * Handles a pick {@link Intent}. Must be fully implemented if {@link #getPickIntent} returns
     * a non-null value.
     */
    public abstract void handlePickResult(Context context, Intent data);

    /**
     * Builds a {@link View} that can edit an underlying record, or launch a picker to change
     * the value of the record.
     * This {@code RecordEditInfo} will be set as the {@link View}'s tag.
     */
    public abstract View getEditView(
            Activity activity, LayoutInflater inflater, ViewGroup parent, EditCallbacks callbacks);

    /**
     * Does the work of building a {@link View} and binding common controls.
     */
    protected View buildEditView(
            Activity activity, LayoutInflater inflater, int resourceId,
            ViewGroup parent, EditCallbacks callbacks) {
        View result = inflater.inflate(resourceId, parent, false);
        result.setTag(this);

        View deleteButton = result.findViewById(R.id.delete);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(this);
        }
        mCallbacks = callbacks;
        return result;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mType);
    }

    @Override
    public void onClick(View target) {
        if (target.getId() == R.id.delete) {
            mCallbacks.deleteRecord(this);
        }
    }
}
