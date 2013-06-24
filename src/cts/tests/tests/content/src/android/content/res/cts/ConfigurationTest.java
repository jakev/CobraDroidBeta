/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.content.res.cts;

import java.util.Locale;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Parcel;
import android.test.AndroidTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(Configuration.class)
public class ConfigurationTest extends AndroidTestCase {

    private Configuration mConfigDefault;
    private Configuration mConfig;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mConfigDefault = new Configuration();
        makeConfiguration();
    }

    private void makeConfiguration() {
        mConfig = new Configuration();
        mConfig.fontScale = 2;
        mConfig.mcc = mConfig.mnc = 1;
        mConfig.locale = Locale.getDefault();
        mConfig.touchscreen = Configuration.TOUCHSCREEN_NOTOUCH;
        mConfig.keyboard = Configuration.KEYBOARD_NOKEYS;
        mConfig.keyboardHidden = Configuration.KEYBOARDHIDDEN_NO;
        mConfig.navigation = Configuration.NAVIGATION_NONAV;
        mConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Configuration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Configuration",
            args = {android.content.res.Configuration.class}
        )
    })
    public void testConstructor() {
        new Configuration();
        new Configuration(mConfigDefault);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "compareTo",
        args = {android.content.res.Configuration.class}
    )
    public void testCompareTo() {
        final Configuration cfg1 = new Configuration();
        final Configuration cfg2 = new Configuration();
        assertEquals(0, cfg1.compareTo(cfg2));

        cfg1.orientation = 2;
        cfg2.orientation = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.orientation = 3;
        cfg2.orientation = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.navigation = 2;
        cfg2.navigation = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.navigation = 3;
        cfg2.navigation = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.keyboardHidden = 2;
        cfg2.keyboardHidden = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.keyboardHidden = 3;
        cfg2.keyboardHidden = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.keyboard = 2;
        cfg2.keyboard = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.keyboard = 3;
        cfg2.keyboard = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.touchscreen = 2;
        cfg2.touchscreen = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.touchscreen = 3;
        cfg2.touchscreen = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.locale = new Locale("", "", "2");
        cfg2.locale = new Locale("", "", "3");
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.locale = new Locale("", "", "3");
        cfg2.locale = new Locale("", "", "2");
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.locale = new Locale("", "2", "");
        cfg2.locale = new Locale("", "3", "");
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.locale = new Locale("", "3", "");
        cfg2.locale = new Locale("", "2", "");
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.locale = new Locale("2", "", "");
        cfg2.locale = new Locale("3", "", "");
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.locale = new Locale("3", "", "");
        cfg2.locale = new Locale("2", "", "");
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.mnc = 2;
        cfg2.mnc = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.mnc = 3;
        cfg2.mnc = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.mcc = 2;
        cfg2.mcc = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.mcc = 3;
        cfg2.mcc = 2;
        assertEquals(1, cfg1.compareTo(cfg2));

        cfg1.fontScale = 2;
        cfg2.fontScale = 3;
        assertEquals(-1, cfg1.compareTo(cfg2));
        cfg1.fontScale = 3;
        cfg2.fontScale = 2;
        assertEquals(1, cfg1.compareTo(cfg2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mConfigDefault.describeContents());
    }

    void doConfigCompare(int expectedFlags, Configuration c1, Configuration c2) {
        assertEquals(expectedFlags, c1.diff(c2));
        Configuration tmpc1 = new Configuration(c1);
        assertEquals(expectedFlags, tmpc1.updateFrom(c2));
        assertEquals(0, tmpc1.diff(c2));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "diff",
        args = {android.content.res.Configuration.class}
    )
    public void testDiff() {
        Configuration config = new Configuration();
        config.mcc = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC, mConfigDefault, config);
        config.mnc = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC, mConfigDefault, config);
        config.locale = Locale.getDefault();
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE, mConfigDefault, config);
        config.screenLayout = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT, mConfigDefault, config);
        config.touchscreen = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN, mConfigDefault, config);
        config.keyboard = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD, mConfigDefault, config);
        config.keyboardHidden = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN, mConfigDefault, config);
        config.keyboardHidden = 0;
        config.hardKeyboardHidden = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN, mConfigDefault, config);
        config.hardKeyboardHidden = 0;
        config.navigationHidden = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN, mConfigDefault, config);
        config.navigation = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
                | ActivityInfo.CONFIG_NAVIGATION, mConfigDefault, config);
        config.orientation = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
                | ActivityInfo.CONFIG_NAVIGATION
                | ActivityInfo.CONFIG_ORIENTATION, mConfigDefault, config);
        config.uiMode = 1;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
                | ActivityInfo.CONFIG_NAVIGATION
                | ActivityInfo.CONFIG_ORIENTATION
                | ActivityInfo.CONFIG_UI_MODE, mConfigDefault, config);
        config.fontScale = 2;
        doConfigCompare(ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
                | ActivityInfo.CONFIG_NAVIGATION
                | ActivityInfo.CONFIG_ORIENTATION
                | ActivityInfo.CONFIG_UI_MODE
                | ActivityInfo.CONFIG_FONT_SCALE, mConfigDefault, config);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "equals",
            args = {android.content.res.Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "equals",
            args = {java.lang.Object.class}
        )
    })
    public void testEquals() {
        assertFalse(mConfigDefault.equals(mConfig));
        assertFalse(mConfigDefault.equals(new Object()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        assertFalse(mConfigDefault.hashCode() == mConfig.hashCode());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "needNewResources",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateFrom",
            args = {android.content.res.Configuration.class}
        )
    })
    public void testNeedNewResources() {
        assertTrue(Configuration.needNewResources(ActivityInfo.CONFIG_MCC,
                ActivityInfo.CONFIG_MCC));
        assertFalse(Configuration.needNewResources(ActivityInfo.CONFIG_MNC,
                ActivityInfo.CONFIG_MCC));
        assertTrue(Configuration.needNewResources(
                ActivityInfo.CONFIG_MNC|ActivityInfo.CONFIG_FONT_SCALE,
                ActivityInfo.CONFIG_MCC));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setToDefaults",
        args = {}
    )
    public void testSetToDefaults() {
        final Configuration temp = new Configuration(mConfig);
        assertFalse(temp.equals(mConfigDefault));
        temp.setToDefaults();
        assertTrue(temp.equals(mConfigDefault));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(mConfigDefault.toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        assertWriteToParcel(createConfig(null), Parcel.obtain());
        assertWriteToParcel(createConfig(Locale.JAPAN), Parcel.obtain());
    }

    private Configuration createConfig(Locale locale) {
        Configuration config = new Configuration();
        config.fontScale = 13.37f;
        config.mcc = 0;
        config.mnc = 1;
        config.locale = locale;
        config.touchscreen = Configuration.TOUCHSCREEN_STYLUS;
        config.keyboard = Configuration.KEYBOARD_UNDEFINED;
        config.keyboardHidden = Configuration.KEYBOARDHIDDEN_YES;
        config.hardKeyboardHidden = Configuration.KEYBOARDHIDDEN_UNDEFINED;
        config.navigation = Configuration.NAVIGATION_DPAD;
        config.navigationHidden = Configuration.NAVIGATIONHIDDEN_UNDEFINED;
        config.orientation = Configuration.ORIENTATION_PORTRAIT;
        config.screenLayout = Configuration.SCREENLAYOUT_LONG_UNDEFINED;
        return config;
    }

    private void assertWriteToParcel(Configuration config, Parcel parcel) {
        config.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        assertEquals(config.fontScale, parcel.readFloat());
        assertEquals(config.mcc, parcel.readInt());
        assertEquals(config.mnc, parcel.readInt());
        if (config.locale == null) {
            assertEquals(0, parcel.readInt());
        } else {
            assertEquals(1, parcel.readInt());
            assertEquals(config.locale.getLanguage(),
                    parcel.readString());
            assertEquals(config.locale.getCountry(),
                    parcel.readString());
            assertEquals(config.locale.getVariant(),
                    parcel.readString());
        }
        parcel.readInt();
        assertEquals(config.touchscreen, parcel.readInt());
        assertEquals(config.keyboard, parcel.readInt());
        assertEquals(config.keyboardHidden, parcel.readInt());
        assertEquals(config.hardKeyboardHidden, parcel.readInt());
        assertEquals(config.navigation, parcel.readInt());
        assertEquals(config.navigationHidden, parcel.readInt());
        assertEquals(config.orientation, parcel.readInt());
        assertEquals(config.screenLayout, parcel.readInt());
    }

}
