/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.widget.cts;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.ToggleButton;

import com.android.cts.stub.R;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.ToBeFixed;

/**
 * Test {@link ToggleButton}.
 */
@TestTargetClass(ToggleButton.class)
public class ToggleButtonTest extends AndroidTestCase {
    private static final String TEXT_OFF = "text off";
    private static final String TEXT_ON = "text on";
    ToggleButton mToggleButton;
    Context mContext;
    AttributeSet mAttrSet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContext = getContext();
        XmlPullParser parser = mContext.getResources().getXml(R.layout.togglebutton_layout);
        mAttrSet = Xml.asAttributeSet(parser);
        mToggleButton = new ToggleButton(mContext, mAttrSet);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test constructors",
            method = "ToggleButton",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test constructors",
            method = "ToggleButton",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test constructors",
            method = "ToggleButton",
            args = {android.content.Context.class}
        )
    })
    @ToBeFixed(bug = "1371108",
            explanation = "There should not be a NullPointerException thrown out.")
    public void testConstructor() {
        new ToggleButton(mContext, mAttrSet, 0);
        new ToggleButton(mContext, mAttrSet);
        new ToggleButton(mContext);

        try {
            new ToggleButton(null, null, -1);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }

        try {
            new ToggleButton(null, null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }

        try {
            new ToggleButton(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
            // expected, test success.
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setTextOff and getTextOff",
            method = "setTextOff",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setTextOff and getTextOff",
            method = "getTextOff",
            args = {}
        )
    })
    public void testAccessTextOff() {
        mToggleButton.setTextOff("android");
        assertEquals("android", mToggleButton.getTextOff());
        mToggleButton.setChecked(false);

        mToggleButton.setTextOff(null);
        assertNull(mToggleButton.getTextOff());

        mToggleButton.setTextOff("");
        assertEquals("", mToggleButton.getTextOff());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test drawableStateChanged",
        method = "drawableStateChanged",
        args = {}
    )
    public void testDrawableStateChanged() {
        MockToggleButton toggleButton = new MockToggleButton(mContext);

        // drawableStateChanged without any drawable.
        toggleButton.drawableStateChanged();

        // drawableStateChanged when CheckMarkDrawable is not null.
        Resources resources = mContext.getResources();
        Drawable drawable = resources.getDrawable(R.drawable.scenery);
        toggleButton.setButtonDrawable(drawable);
        drawable.setState(null);
        assertNull(drawable.getState());

        toggleButton.drawableStateChanged();
        assertNotNull(drawable.getState());
        assertEquals(toggleButton.getDrawableState(), drawable.getState());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test onFinishInflate",
        method = "onFinishInflate",
        args = {}
    )
    @ToBeFixed(bug = "1400085", explanation = "This method will effect the UI. "
            + "The best way to test this is looking at the actual view.")
    public void testOnFinishInflate() {
        MockToggleButton toggleButton = new MockToggleButton(mContext);
        toggleButton.onFinishInflate();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test setChecked",
        method = "setChecked",
        args = {boolean.class}
    )
    public void testSetChecked() {
        assertFalse(mToggleButton.isChecked());

        mToggleButton.setChecked(true);
        assertTrue(mToggleButton.isChecked());

        mToggleButton.setChecked(false);
        assertFalse(mToggleButton.isChecked());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Check the text of button when change button status",
            method = "setChecked",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Check the text of button when change button status",
            method = "setTextOff",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Check the text of button when change button status",
            method = "setTextOn",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testToggleText() {
        mToggleButton.setText("default text");
        mToggleButton.setTextOn(TEXT_ON);
        mToggleButton.setTextOff(TEXT_OFF);
        mToggleButton.setChecked(true);
        assertEquals(TEXT_ON, mToggleButton.getText().toString());
        mToggleButton.setChecked(false);
        assertFalse(mToggleButton.isChecked());
        assertEquals(TEXT_OFF, mToggleButton.getText().toString());

        // Set the current displaying text as TEXT_OFF.
        // Then set checked button, but textOn is null.
        mToggleButton.setTextOff(TEXT_OFF);
        mToggleButton.setChecked(false);
        mToggleButton.setTextOn(null);
        mToggleButton.setChecked(true);
        assertEquals(TEXT_OFF, mToggleButton.getText().toString());

        // Set the current displaying text as TEXT_ON. Then set unchecked button,
        // but textOff is null.
        mToggleButton.setTextOn(TEXT_ON);
        mToggleButton.setChecked(true);
        mToggleButton.setTextOff(null);
        mToggleButton.setChecked(false);
        assertEquals(TEXT_ON, mToggleButton.getText().toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test setBackgroundDrawable",
        method = "setBackgroundDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testSetBackgroundDrawable() {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.scenery);

        mToggleButton.setBackgroundDrawable(drawable);
        assertSame(drawable, mToggleButton.getBackground());

        // remove the background
        mToggleButton.setBackgroundDrawable(null);
        assertNull(mToggleButton.getBackground());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setTextOn and getTextOn",
            method = "setTextOn",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setTextOn and getTextOn",
            method = "getTextOn",
            args = {}
        )
    })
    public void testAccessTextOn() {
        mToggleButton.setTextOn("cts");
        assertEquals("cts", mToggleButton.getTextOn());

        mToggleButton.setTextOn(null);
        assertNull(mToggleButton.getTextOn());

        mToggleButton.setTextOn("");
        assertEquals("", mToggleButton.getTextOn());
    }

    /**
     * MockToggleButton class for testing.
     */
    private static final class MockToggleButton extends ToggleButton {
        public MockToggleButton(Context context) {
            super(context);
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
        }
    }
}
