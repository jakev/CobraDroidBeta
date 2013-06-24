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

package android.text.format.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.test.AndroidTestCase;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

@TestTargetClass(DateFormat.class)
public class DateFormatTest extends AndroidTestCase {

    private Context mContext;
    private ContentResolver mContentResolver;
    // Date: 12-18-2008 5:30AM
    private static final int YEAR_FROM_1900 = 108;
    private static final int YEAR = 2008;
    private static final int MONTH = 11;
    private static final int DAY = 18;
    private static final int HOUR = 5;
    private static final int MINUTE = 30;

    private boolean mIs24HourFormat;
    private Locale mDefaultLocale;
    private String mDefaultFormat;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mContentResolver = mContext.getContentResolver();
        mIs24HourFormat = DateFormat.is24HourFormat(mContext);
        mDefaultLocale = Locale.getDefault();
        mDefaultFormat = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.DATE_FORMAT);
    }

    @Override
    protected void tearDown() throws Exception {
        if (!mIs24HourFormat) {
            Settings.System.putString(mContentResolver, Settings.System.TIME_12_24, "12");
        }
        if (!Locale.getDefault().equals(mDefaultLocale)) {
            Locale.setDefault(mDefaultLocale);
        }
        Settings.System.putString(mContentResolver, Settings.System.DATE_FORMAT, mDefaultFormat);
        super.tearDown();
    }


    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "is24HourFormat",
        args = {Context.class}
    )
    public void testDateFormat() {
        Settings.System.putString(mContentResolver, Settings.System.TIME_12_24, "24");
        assertTrue(DateFormat.is24HourFormat(mContext));
        Settings.System.putString(mContentResolver, Settings.System.TIME_12_24, "12");
        assertFalse(DateFormat.is24HourFormat(mContext));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTimeFormat",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDateFormat",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLongDateFormat",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMediumDateFormat",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDateFormatOrder",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "format",
            args = {CharSequence.class, Calendar.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "format",
            args = {CharSequence.class, Date.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "format",
            args = {CharSequence.class, long.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testFormatMethods() throws ParseException {
        if (!mDefaultLocale.equals(Locale.US)) {
            Locale.setDefault(Locale.US);
        }

        java.text.DateFormat dateFormat = DateFormat.getDateFormat(mContext);
        assertNotNull(dateFormat);
        Date date = new Date(YEAR_FROM_1900, MONTH, DAY, HOUR, MINUTE);
        String source = dateFormat.format(date);
        Date parseDate = dateFormat.parse(source);
        assertEquals(date.getYear(), parseDate.getYear());
        assertEquals(date.getMonth(), parseDate.getMonth());
        assertEquals(date.getDay(), date.getDay());

        dateFormat = DateFormat.getLongDateFormat(mContext);
        assertNotNull(dateFormat);
        source = dateFormat.format(date);
        assertTrue(source.indexOf("December") >= 0);
        dateFormat = DateFormat.getMediumDateFormat(mContext);
        assertNotNull(dateFormat);
        source = dateFormat.format(date);
        assertTrue(source.indexOf("Dec") >= 0);
        assertTrue(source.indexOf("December") < 0);
        dateFormat = DateFormat.getTimeFormat(mContext);
        source = dateFormat.format(date);
        assertTrue(source.indexOf("5") >= 0);
        assertTrue(source.indexOf("30") >= 0);

        String testFormat = "yyyy-MM-dd";
        String testOrder = "yMd";
        Settings.System.putString(mContentResolver, Settings.System.DATE_FORMAT, testFormat);
        String actualOrder = String.valueOf(DateFormat.getDateFormatOrder(mContext));
        assertEquals(testOrder, actualOrder);

        String format = "MM/dd/yy";
        String expectedString = "12/18/08";
        Calendar calendar = new GregorianCalendar(YEAR, MONTH, DAY);
        CharSequence actual = DateFormat.format(format, calendar);
        assertEquals(expectedString, actual.toString());
        Date formatDate = new Date(YEAR_FROM_1900, MONTH, DAY);
        actual = DateFormat.format(format, formatDate);
        assertEquals(expectedString, actual.toString());
        actual = DateFormat.format(format, formatDate.getTime());
        assertEquals(expectedString, actual.toString());
    }
}
