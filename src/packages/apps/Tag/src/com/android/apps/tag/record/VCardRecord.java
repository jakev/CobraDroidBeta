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
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardVersionException;
import com.google.android.collect.Lists;
import com.google.common.base.Preconditions;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * VCard Ndef Record object
 */
public class VCardRecord extends ParsedNdefRecord implements OnClickListener {
    private static final String TAG = VCardRecord.class.getSimpleName();

    public static final String RECORD_TYPE = "vcard";

    private final byte[] mVCard;

    private VCardRecord(byte[] content) {
        mVCard = content;
    }

    @Override
    public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset) {

        Uri uri = activity.getIntent().getData();
        uri = Uri.withAppendedPath(uri, Integer.toString(offset));
        uri = Uri.withAppendedPath(uri, "mime");

        // TODO: parse content and display something nicer.
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        CharSequence template = activity.getResources().getText(R.string.import_vcard);
        String description = TextUtils.expandTemplate(template, getDisplayName()).toString();

        return RecordUtils.getViewsForIntent(activity, inflater, parent, this, intent, description);
    }

    @Override
    public String getSnippet(Context context, Locale locale) {
        CharSequence template = context.getResources().getText(R.string.vcard_title);
        return TextUtils.expandTemplate(template, getDisplayName()).toString();
    }

    public String getDisplayName() {
        try {
            ArrayList<VCardEntry> entries = getVCardEntries();
            if (!entries.isEmpty()) {
                return entries.get(0).getDisplayName();
            }
        } catch (Exception e) {
        }

        return "vCard";
    }

    private ArrayList<VCardEntry> getVCardEntries() throws IOException, VCardException {
        final ArrayList<VCardEntry> entries = Lists.newArrayList();

        final int type = VCardConfig.VCARD_TYPE_UNKNOWN;
        final VCardEntryConstructor constructor = new VCardEntryConstructor(type);
        constructor.addEntryHandler(new VCardEntryHandler() {
            @Override public void onStart() {}
            @Override public void onEnd() {}

            @Override
            public void onEntryCreated(VCardEntry entry) {
                entries.add(entry);
            }
        });

        VCardParser parser = new VCardParser_V21(type);
        try {
            parser.parse(new ByteArrayInputStream(mVCard), constructor);
        } catch (VCardVersionException e) {
            try {
                parser = new VCardParser_V30(type);
                parser.parse(new ByteArrayInputStream(mVCard), constructor);
            } finally {
            }
        }

        return entries;
    }

    private static Intent getPickContactIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        return intent;
    }

    /**
     * Returns a view in a list of record types for adding new records to a message.
     */
    public static View getAddView(Context context, LayoutInflater inflater, ViewGroup parent) {
        ViewGroup root = (ViewGroup) inflater.inflate(
                R.layout.tag_add_record_list_item, parent, false);

        Intent intent = getPickContactIntent();
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.isEmpty()) {
            return null;
        }

        ResolveInfo info = activities.get(0);
        ((ImageView) root.findViewById(R.id.image)).setImageDrawable(info.loadIcon(pm));
        ((TextView) root.findViewById(R.id.text)).setText(context.getString(R.string.contact));

        root.setTag(new VCardRecordEditInfo());
        return root;
    }

    @Override
    public RecordEditInfo getEditInfo(Activity host) {
        return new VCardRecordEditInfo(mVCard);
    }

    public static VCardRecord parse(NdefRecord record) {
        MimeRecord underlyingRecord = MimeRecord.parse(record);

        // TODO: Add support for other vcard mime types.
        Preconditions.checkArgument("text/x-vCard".equals(underlyingRecord.getMimeType()));
        return new VCardRecord(underlyingRecord.getContent());
    }

    public static NdefRecord newVCardRecord(byte[] data) {
        return MimeRecord.newMimeRecord("text/x-vCard", data);
    }

    @Override
    public void onClick(View view) {
        RecordUtils.ClickInfo info = (RecordUtils.ClickInfo) view.getTag();
        try {
            info.activity.startActivity(info.intent);
            info.activity.finish();
        } catch (ActivityNotFoundException e) {
            // The activity wansn't found for some reason. Don't crash, but don't do anything.
            Log.e(TAG, "Failed to launch activity for intent " + info.intent, e);
        }
    }

    public static boolean isVCard(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static VCardRecordEditInfo editInfoForUri(Uri vcardUri) {
        if (vcardUri.toString().startsWith(
                ContactsContract.Contacts.CONTENT_VCARD_URI.toString())) {
            String lookupKey = vcardUri.getLastPathSegment();
            Uri lookupUri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            return new VCardRecord.VCardRecordEditInfo(lookupUri);
        }
        return null;
    }

    private static class VCardRecordEditInfo extends RecordEditInfo {
        /**
         * The lookup {@link Uri} if the data is to be pulled from the contact provider.
         * Can be null if {@link #mValue} is filled in by other means externally.
         * @see ContactsContract
         */
        private Uri mLookupUri;

        /**
         * The raw VCard bytes.
         */
        private byte[] mValue;

        private WeakReference<View> mActiveView = null;

        private String mCachedName = null;
        private Drawable mCachedPhoto = null;

        /**
         * Data
         */
        private static final class CacheData {
            public String mName;
            public Drawable mPhoto;
            public byte[] mVcard;
        }

        public VCardRecordEditInfo() {
            super(RECORD_TYPE);
        }

        public VCardRecordEditInfo(Uri lookupUri) {
            super(RECORD_TYPE);
            mLookupUri = lookupUri;
            mValue = null;
        }

        public VCardRecordEditInfo(byte[] value) {
            super(RECORD_TYPE);
            mLookupUri = null;
            mValue = Preconditions.checkNotNull(value);
        }

        protected VCardRecordEditInfo(Parcel parcel) {
            super(parcel);
            mLookupUri = parcel.readParcelable(null);
            int valueLength = parcel.readInt();
            if (valueLength > 0) {
                mValue = new byte[valueLength];
                parcel.readByteArray(mValue);
            }
        }

        @Override
        public Intent getPickIntent() {
            return getPickContactIntent();
        }

        private void extractValuesFromBytes(final Context context) {
            byte[] bytes = Preconditions.checkNotNull(mValue);

            final int type = VCardConfig.VCARD_TYPE_UNKNOWN;
            final VCardEntryConstructor constructor = new VCardEntryConstructor(type);
            constructor.addEntryHandler(new VCardEntryHandler() {
                @Override public void onStart() {}
                @Override public void onEnd() {}

                @Override
                public void onEntryCreated(VCardEntry entry) {
                    mCachedName = entry.getDisplayName();
                    List<VCardEntry.PhotoData> photoList = entry.getPhotoList();
                    if (photoList != null && !photoList.isEmpty()) {
                        byte[] rawData = photoList.get(0).photoBytes;
                        mCachedPhoto = new BitmapDrawable(
                                context.getResources(),
                                BitmapFactory.decodeByteArray(rawData, 0, rawData.length));
                    }
                    bindView();
                }
            });

            VCardParser parser = new VCardParser_V21(type);
            try {
                parser.parse(new ByteArrayInputStream(bytes), constructor);
            } catch (IOException e) {
            } catch (VCardException e) {
            }
        }

        private void fetchValuesFromProvider(final Context context) {
            if (mValue != null) {
                bindView();
                return;
            }

            new AsyncTask<Uri, Void, CacheData>() {
                @Override
                protected CacheData doInBackground(Uri... params) {
                    Cursor cursor = null;
                    long id;
                    String lookupKey = null;
                    Uri lookupUri = params[0];
                    CacheData result = new CacheData();
                    try {
                        String[] projection = {
                                ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.LOOKUP_KEY,
                                ContactsContract.Contacts.DISPLAY_NAME
                        };
                        cursor = context.getContentResolver().query(
                                lookupUri, projection, null, null, null);
                        cursor.moveToFirst();
                        id = cursor.getLong(0);
                        lookupKey = cursor.getString(1);
                        result.mName = cursor.getString(2);

                    } finally {
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                    }

                    if (lookupKey == null) {
                        // TODO: handle errors.
                        return null;
                    }

                    // Note: the lookup key should already encoded.
                    Uri vcardUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_VCARD_URI,
                            lookupKey);

                    AssetFileDescriptor descriptor;
                    FileInputStream in = null;
                    try {
                        descriptor =  context.getContentResolver().openAssetFileDescriptor(
                                vcardUri, "r");
                        result.mVcard = new byte[(int) descriptor.getLength()];

                        in = descriptor.createInputStream();
                        in.read(result.mVcard);
                        in.close();
                    } catch (FileNotFoundException e) {
                        return null;
                    } catch (IOException e) {
                        return null;
                    }

                    Uri contactUri = ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, id);
                    InputStream photoIn = ContactsContract.Contacts.openContactPhotoInputStream(
                            context.getContentResolver(), contactUri);
                    if (photoIn != null) {
                        result.mPhoto = Drawable.createFromStream(photoIn, contactUri.toString());
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(CacheData data) {
                    if (data == null) {
                        return;
                    }

                    mCachedName = data.mName;
                    mValue = data.mVcard;
                    mCachedPhoto = data.mPhoto;
                    bindView();
                }
            }.execute(mLookupUri);
        }

        @Override
        public NdefRecord getValue() {
            return (mValue == null) ? null : VCardRecord.newVCardRecord(mValue);
        }

        @Override
        public void handlePickResult(Context context, Intent data) {
            mLookupUri = data.getData();
            mValue = null;
            mCachedName = null;
            mCachedPhoto = null;
        }

        private void bindView() {
            View view = (mActiveView == null) ? null : mActiveView.get();
            if (view == null) {
                return;
            }

            if (mCachedPhoto != null) {
                ((ImageView) view.findViewById(R.id.photo)).setImageDrawable(mCachedPhoto);
            }

            if (mCachedName != null) {
                ((TextView) view.findViewById(R.id.display_name)).setText(mCachedName);
            }
        }

        @Override
        public View getEditView(
                Activity activity, LayoutInflater inflater,
                ViewGroup parent, EditCallbacks callbacks) {
            View result = buildEditView(
                    activity, inflater, R.layout.tag_edit_vcard, parent, callbacks);

            mActiveView = new WeakReference<View>(result);
            result.setOnClickListener(this);

            // Show default contact photo until the data loads.
            ((ImageView) result.findViewById(R.id.photo)).setImageDrawable(
                    activity.getResources().getDrawable(R.drawable.default_contact_photo));

            if (mLookupUri != null) {
                fetchValuesFromProvider(activity);
            } else if (mValue != null) {
                extractValuesFromBytes(activity);
            }
            return result;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(mLookupUri, flags);
            out.writeInt(mValue == null ? 0 : mValue.length);
            out.writeByteArray(mValue);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<VCardRecordEditInfo> CREATOR =
                new Parcelable.Creator<VCardRecordEditInfo>() {
            @Override
            public VCardRecordEditInfo createFromParcel(Parcel in) {
                return new VCardRecordEditInfo(in);
            }

            @Override
            public VCardRecordEditInfo[] newArray(int size) {
                return new VCardRecordEditInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void onClick(View target) {
            if (this == target.getTag()) {
                mCallbacks.startPickForRecord(this, getPickIntent());
            } else {
                super.onClick(target);
            }
        }
    }
}
