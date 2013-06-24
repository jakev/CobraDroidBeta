/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.cellbroadcastreceiver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsCbConstants;
import android.telephony.SmsCbMessage;
import android.text.format.DateUtils;

import com.android.internal.telephony.gsm.SmsCbHeader;

/**
 * Application wrapper for {@link SmsCbMessage}. This is Parcelable so that
 * decoded broadcast message objects can be passed between running Services.
 * New broadcasts are received by {@link CellBroadcastReceiver},
 * displayed by {@link CellBroadcastAlertService}, and saved to SQLite by
 * {@link CellBroadcastDatabaseService}.
 */
public class CellBroadcastMessage implements Parcelable {

    /** Identifier for getExtra() when adding this object to an Intent. */
    public static final String SMS_CB_MESSAGE_EXTRA =
            "com.android.cellbroadcastreceiver.SMS_CB_MESSAGE";

    private final int mGeographicalScope;
    private final int mSerialNumber;
    private final int mMessageCode;
    private final int mMessageIdentifier;
    private final String mLanguageCode;
    private final String mMessageBody;
    private final long mDeliveryTime;
    private boolean mIsRead;

    public CellBroadcastMessage(SmsCbMessage message) {
        mGeographicalScope = message.getGeographicalScope();
        mSerialNumber = message.getUpdateNumber();
        mMessageCode = message.getMessageCode();
        mMessageIdentifier = message.getMessageIdentifier();
        mLanguageCode = message.getLanguageCode();
        mMessageBody = message.getMessageBody();
        mDeliveryTime = System.currentTimeMillis();
        mIsRead = false;
    }

    private CellBroadcastMessage(int geoScope, int serialNumber,
            int messageCode, int messageId, String languageCode,
            String messageBody, long deliveryTime, boolean isRead) {
        mGeographicalScope = geoScope;
        mSerialNumber = serialNumber;
        mMessageCode = messageCode;
        mMessageIdentifier = messageId;
        mLanguageCode = languageCode;
        mMessageBody = messageBody;
        mDeliveryTime = deliveryTime;
        mIsRead = isRead;
    }

    /** Parcelable: no special flags. */
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mGeographicalScope);
        out.writeInt(mSerialNumber);
        out.writeInt(mMessageCode);
        out.writeInt(mMessageIdentifier);
        out.writeString(mLanguageCode);
        out.writeString(mMessageBody);
        out.writeLong(mDeliveryTime);
        out.writeInt(mIsRead ? 1 : 0);
    }

    public static final Parcelable.Creator<CellBroadcastMessage> CREATOR
            = new Parcelable.Creator<CellBroadcastMessage>() {
        public CellBroadcastMessage createFromParcel(Parcel in) {
            return new CellBroadcastMessage(
                    in.readInt(), in.readInt(),
                    in.readInt(), in.readInt(), in.readString(),
                    in.readString(), in.readLong(), (in.readInt() != 0));
        }

        public CellBroadcastMessage[] newArray(int size) {
            return new CellBroadcastMessage[size];
        }
    };

    /**
     * Create a CellBroadcastMessage from a row in the database.
     * @param cursor an open SQLite cursor pointing to the row to read
     * @return the new CellBroadcastMessage
     */
    public static CellBroadcastMessage createFromCursor(Cursor cursor) {
        int geoScope = cursor.getInt(CellBroadcastDatabase.COLUMN_GEOGRAPHICAL_SCOPE);
        int serialNum = cursor.getInt(CellBroadcastDatabase.COLUMN_SERIAL_NUMBER);
        int messageCode = cursor.getInt(CellBroadcastDatabase.COLUMN_MESSAGE_CODE);
        int messageId = cursor.getInt(CellBroadcastDatabase.COLUMN_MESSAGE_IDENTIFIER);
        String language = cursor.getString(CellBroadcastDatabase.COLUMN_LANGUAGE_CODE);
        String body = cursor.getString(CellBroadcastDatabase.COLUMN_MESSAGE_BODY);
        long deliveryTime = cursor.getLong(CellBroadcastDatabase.COLUMN_DELIVERY_TIME);
        boolean isRead = (cursor.getInt(CellBroadcastDatabase.COLUMN_MESSAGE_READ) != 0);
        return new CellBroadcastMessage(geoScope, serialNum, messageCode, messageId,
                language, body, deliveryTime, isRead);
    }

    /**
     * Return a ContentValues object for insertion into the database.
     * @return a new ContentValues object containing this object's data
     */
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(8);
        cv.put(CellBroadcastDatabase.Columns.GEOGRAPHICAL_SCOPE, mGeographicalScope);
        cv.put(CellBroadcastDatabase.Columns.SERIAL_NUMBER, mSerialNumber);
        cv.put(CellBroadcastDatabase.Columns.MESSAGE_CODE, mMessageCode);
        cv.put(CellBroadcastDatabase.Columns.MESSAGE_IDENTIFIER, mMessageIdentifier);
        cv.put(CellBroadcastDatabase.Columns.LANGUAGE_CODE, mLanguageCode);
        cv.put(CellBroadcastDatabase.Columns.MESSAGE_BODY, mMessageBody);
        cv.put(CellBroadcastDatabase.Columns.DELIVERY_TIME, mDeliveryTime);
        cv.put(CellBroadcastDatabase.Columns.MESSAGE_READ, mIsRead);
        return cv;
    }

    /**
     * Set or clear the "read message" flag.
     * @param isRead true if the message has been read; false if not
     */
    public void setIsRead(boolean isRead) {
        mIsRead = isRead;
    }

    public int getGeographicalScope() {
        return mGeographicalScope;
    }

    public int getSerialNumber() {
        return mSerialNumber;
    }

    public int getMessageCode() {
        return mMessageCode;
    }

    public int getMessageIdentifier() {
        return mMessageIdentifier;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public long getDeliveryTime() {
        return mDeliveryTime;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public boolean isRead() {
        return mIsRead;
    }

    /**
     * Return whether the broadcast is an emergency (PWS) message type.
     * This includes lower priority test messages and Amber alerts.
     *
     * All public alerts show the flashing warning icon in the dialog,
     * but only emergency alerts play the alert sound and speak the message.
     *
     * @return true if the message is PWS type; false otherwise
     */
    public boolean isPublicAlertMessage() {
        return SmsCbHeader.isEmergencyMessage(mMessageIdentifier);
    }

    /**
     * Returns whether the broadcast is an emergency (PWS) message type,
     * including test messages, but excluding lower priority Amber alert broadcasts.
     *
     * @return true if the message is PWS type, excluding Amber alerts
     */
    public boolean isEmergencyAlertMessage() {
        int id = mMessageIdentifier;
        return SmsCbHeader.isEmergencyMessage(id) &&
                id != SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY;
    }

    /**
     * Return whether the broadcast is an ETWS emergency message type.
     * @return true if the message is ETWS emergency type; false otherwise
     */
    public boolean isEtwsMessage() {
        return SmsCbHeader.isEtwsMessage(mMessageIdentifier);
    }

    /**
     * Return whether the broadcast is a CMAS emergency message type.
     * @return true if the message is CMAS emergency type; false otherwise
     */
    public boolean isCmasMessage() {
        return SmsCbHeader.isCmasMessage(mMessageIdentifier);
    }

    /**
     * Return whether the broadcast is an ETWS popup alert.
     * This method checks the message ID and the message code.
     * @return true if the message indicates an ETWS popup alert
     */
    public boolean isEtwsPopupAlert() {
        return SmsCbHeader.isEtwsMessage(mMessageIdentifier) &&
                SmsCbHeader.isEtwsPopupAlert(mMessageCode);
    }

    /**
     * Return whether the broadcast is an ETWS emergency user alert.
     * This method checks the message ID and the message code.
     * @return true if the message indicates an ETWS emergency user alert
     */
    public boolean isEtwsEmergencyUserAlert() {
        return SmsCbHeader.isEtwsMessage(mMessageIdentifier) &&
                SmsCbHeader.isEtwsEmergencyUserAlert(mMessageCode);
    }

    public int getDialogTitleResource() {
        switch (mMessageIdentifier) {
            case SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING:
                return R.string.etws_earthquake_warning;

            case SmsCbConstants.MESSAGE_ID_ETWS_TSUNAMI_WARNING:
                return R.string.etws_tsunami_warning;

            case SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING:
                return R.string.etws_earthquake_and_tsunami_warning;

            case SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE:
                return R.string.etws_test_message;

            case SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE:
                return R.string.etws_other_emergency_type;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL:
                return R.string.cmas_presidential_level_alert;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY:
                return R.string.cmas_extreme_alert;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY:
                return R.string.cmas_severe_alert;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY:
                return R.string.cmas_amber_alert;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST:
                return R.string.cmas_required_monthly_test;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE:
                return R.string.cmas_exercise_alert;

            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE:
                return R.string.cmas_operator_defined_alert;

            default:
                if (SmsCbHeader.isEmergencyMessage(mMessageIdentifier)) {
                    return R.string.pws_other_message_identifiers;
                } else {
                    return R.string.cb_other_message_identifiers;
                }
        }
    }

    /**
     * Return the abbreviated date string for the message delivery time.
     * @param context the context object
     * @return a String to use in the broadcast list UI
     */
    String getDateString(Context context) {
        int flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_SHOW_TIME |
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_CAP_AMPM;
        return DateUtils.formatDateTime(context, mDeliveryTime, flags);
    }

    /**
     * Return the date string for the message delivery time, suitable for text-to-speech.
     * @param context the context object
     * @return a String for populating the list item AccessibilityEvent for TTS
     */
    String getSpokenDateString(Context context) {
        int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE;
        return DateUtils.formatDateTime(context, mDeliveryTime, flags);
    }
}
