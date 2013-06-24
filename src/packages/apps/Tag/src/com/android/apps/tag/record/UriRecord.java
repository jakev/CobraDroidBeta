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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.Bytes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A parsed record containing a Uri.
 */
public class UriRecord extends ParsedNdefRecord implements OnClickListener {
    private static final String TAG = "UriRecord";

    public static final String RECORD_TYPE = "UriRecord";

    /**
     * NFC Forum "URI Record Type Definition"
     *
     * This is a mapping of "URI Identifier Codes" to URI string prefixes,
     * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static final BiMap<Byte, String> URI_PREFIX_MAP = ImmutableBiMap.<Byte, String>builder()
            .put((byte) 0x00, "")
            .put((byte) 0x01, "http://www.")
            .put((byte) 0x02, "https://www.")
            .put((byte) 0x03, "http://")
            .put((byte) 0x04, "https://")
            .put((byte) 0x05, "tel:")
            .put((byte) 0x06, "mailto:")
            .put((byte) 0x07, "ftp://anonymous:anonymous@")
            .put((byte) 0x08, "ftp://ftp.")
            .put((byte) 0x09, "ftps://")
            .put((byte) 0x0A, "sftp://")
            .put((byte) 0x0B, "smb://")
            .put((byte) 0x0C, "nfs://")
            .put((byte) 0x0D, "ftp://")
            .put((byte) 0x0E, "dav://")
            .put((byte) 0x0F, "news:")
            .put((byte) 0x10, "telnet://")
            .put((byte) 0x11, "imap:")
            .put((byte) 0x12, "rtsp://")
            .put((byte) 0x13, "urn:")
            .put((byte) 0x14, "pop:")
            .put((byte) 0x15, "sip:")
            .put((byte) 0x16, "sips:")
            .put((byte) 0x17, "tftp:")
            .put((byte) 0x18, "btspp://")
            .put((byte) 0x19, "btl2cap://")
            .put((byte) 0x1A, "btgoep://")
            .put((byte) 0x1B, "tcpobex://")
            .put((byte) 0x1C, "irdaobex://")
            .put((byte) 0x1D, "file://")
            .put((byte) 0x1E, "urn:epc:id:")
            .put((byte) 0x1F, "urn:epc:tag:")
            .put((byte) 0x20, "urn:epc:pat:")
            .put((byte) 0x21, "urn:epc:raw:")
            .put((byte) 0x22, "urn:epc:")
            .put((byte) 0x23, "urn:nfc:")
            .build();

    private final Uri mUri;

    private UriRecord(Uri uri) {
        this.mUri = Preconditions.checkNotNull(uri);
    }

    public Intent getIntentForUri() {
        String scheme = mUri.getScheme();
        if ("tel".equals(scheme)) {
            return new Intent(Intent.ACTION_CALL, mUri);
        } else if ("sms".equals(scheme) || "smsto".equals(scheme)) {
            return new Intent(Intent.ACTION_SENDTO, mUri);
        } else {
            return new Intent(Intent.ACTION_VIEW, mUri);
        }
    }

    public String getPrettyUriString(Context context) {
        String scheme = mUri.getScheme();
        boolean tel = "tel".equals(scheme);
        boolean sms = "sms".equals(scheme) || "smsto".equals(scheme);
        if (tel || sms) {
            String ssp = mUri.getSchemeSpecificPart();
            int offset = ssp.indexOf('?');
            if (offset >= 0) {
                ssp = ssp.substring(0, offset);
            }
            if (tel) {
                return context.getString(R.string.action_call, PhoneNumberUtils.formatNumber(ssp));
            } else {
                return context.getString(R.string.action_text, PhoneNumberUtils.formatNumber(ssp));
            }
        } else {
            return mUri.toString();
        }
    }

    @Override
    public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset) {
        return RecordUtils.getViewsForIntent(activity, inflater, parent, this, getIntentForUri(),
                getPrettyUriString(activity));
    }

    @Override
    public RecordEditInfo getEditInfo(Activity host) {
        return new UriRecordEditInfo(mUri.toString());
    }

    @Override
    public String getSnippet(Context context, Locale locale) {
        return getPrettyUriString(context);
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

    /**
     * Returns a view in a list of record types for adding new records to a message.
     */
    public static View getAddView(Context context, LayoutInflater inflater, ViewGroup parent) {
        ViewGroup root = (ViewGroup) inflater.inflate(
                R.layout.tag_add_record_list_item, parent, false);

        // Determine which Activity can open up normal URL's.
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.isEmpty()) {
            return null;
        }

        ResolveInfo info = activities.get(0);
        ((ImageView) root.findViewById(R.id.image)).setImageDrawable(info.loadIcon(pm));
        ((TextView) root.findViewById(R.id.text)).setText(context.getString(R.string.url));

        root.setTag(new UriRecordEditInfo(""));
        return root;
    }

    @VisibleForTesting
    public Uri getUri() {
        return mUri;
    }

    /**
     * Convert {@link android.nfc.NdefRecord} into a {@link android.net.Uri}. This will handle
     * both TNF_WELL_KNOWN / RTD_URI and TNF_ABSOLUTE_URI.
     *
     * @throws IllegalArgumentException if the NdefRecord is not a
     *     record containing a URI.
     */
    public static UriRecord parse(NdefRecord record) {
        short tnf = record.getTnf();
        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            return parseWellKnown(record);
        } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
            return parseAbsolute(record);
        }
        throw new IllegalArgumentException("Unknown TNF " + tnf);
    }

    /** Parse and absolute URI record */
    private static UriRecord parseAbsolute(NdefRecord record) {
        byte[] payload = record.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        return new UriRecord(uri);
    }

    /** Parse an well known URI record */
    private static UriRecord parseWellKnown(NdefRecord record) {
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_URI));

        byte[] payload = record.getPayload();

        /*
         * payload[0] contains the URI Identifier Code, per the
         * NFC Forum "URI Record Type Definition" section 3.2.2.
         *
         * payload[1]...payload[payload.length - 1] contains the rest of
         * the URI.
         */

        String prefix = URI_PREFIX_MAP.get(payload[0]);
        byte[] fullUri = Bytes.concat(
                prefix.getBytes(Charset.forName("UTF-8")),
                Arrays.copyOfRange(payload, 1, payload.length));

        Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
        return new UriRecord(uri);
    }

    public static boolean isUri(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static final byte[] EMPTY = new byte[0];

    /**
     * Convert a {@link Uri} to an {@link NdefRecord}
     */
    public static NdefRecord newUriRecord(Uri uri) {
        byte[] uriBytes = uri.toString().getBytes(Charset.forName("UTF-8"));

        /*
         * We prepend 0x00 to the bytes of the URI to indicate that this
         * is the entire URI, and we are not taking advantage of the
         * URI shortening rules in the NFC Forum URI spec section 3.2.2.
         * This produces a NdefRecord which is slightly larger than
         * necessary.
         *
         * In the future, we should use the URI shortening rules in 3.2.2
         * to create a smaller NdefRecord.
         */
        byte[] payload = Bytes.concat(new byte[] { 0x00 }, uriBytes);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_URI, EMPTY, payload);
    }

    public static class UriRecordEditInfo extends AbstractTextRecordEditInfo {
        public UriRecordEditInfo(String uri) {
            super(uri);
        }

        public UriRecordEditInfo(Parcel in) {
            super(in);
        }

        @Override
        public int getLayoutId() {
            return R.layout.tag_edit_url;
        }

        @Override
        public NdefRecord getValue() {
            String text = getCurrentText();
            if (TextUtils.isEmpty(text)) text = "";
            return UriRecord.newUriRecord(Uri.parse(text));
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<UriRecordEditInfo> CREATOR =
                new Parcelable.Creator<UriRecordEditInfo>() {
            @Override
            public UriRecordEditInfo createFromParcel(Parcel in) {
                return new UriRecordEditInfo(in);
            }

            @Override
            public UriRecordEditInfo[] newArray(int size) {
                return new UriRecordEditInfo[size];
            }
        };
    }
}
