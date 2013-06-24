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

package android.view.cts;

import android.os.Message;
import android.os.Parcel;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Class for testing {@link AccessibilityEvent}.
 */
public class AccessibilityEventTest extends TestCase {

    /** The number of properties of the {@link AccessibilityEvent} class. */
    private static final int NON_STATIC_FIELD_COUNT = 16;

    @MediumTest
    public void testMarshalling() throws Exception {
        // no new fields, so we are testing marshaling of all such
        assertNoNewNonStaticFieldsAdded();

        // fully populate the event to marshal
        AccessibilityEvent sentEvent = AccessibilityEvent.obtain();
        fullyPopulateSentAccessibilityEvent(sentEvent);

        // marshal and unmarshal the event
        Parcel parcel = Parcel.obtain();
        sentEvent.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        AccessibilityEvent receivedEvent = AccessibilityEvent.CREATOR.createFromParcel(parcel);

        // make sure all fields properly marshaled
        assertEqualsAccessiblityEvent(sentEvent, receivedEvent);
    }

    /**
     * Tests if {@link AccessibilityEvent} are properly reused.
     */
    @MediumTest
    public void testReuse() {
        AccessibilityEvent firstEvent = AccessibilityEvent.obtain();
        firstEvent.recycle();
        AccessibilityEvent secondEvent = AccessibilityEvent.obtain();
        assertSame("AccessibilityEvent not properly reused", firstEvent, secondEvent);
    }

    /**
     * Tests if {@link AccessibilityEvent} are properly recycled.
     */
    @MediumTest
    public void testRecycle() {
        // obtain and populate an event
        AccessibilityEvent populatedEvent = AccessibilityEvent.obtain();
        fullyPopulateSentAccessibilityEvent(populatedEvent);

        // recycle and obtain the same recycled instance
        populatedEvent.recycle();
        AccessibilityEvent recycledEvent = AccessibilityEvent.obtain();

        // check expectations
        assertAccessibilityEventCleared(recycledEvent);
    }

    /**
     * Asserts that no new fields have been added, so we are testing marshaling
     * of all such.
     */
    private void assertNoNewNonStaticFieldsAdded() {
        int nonStaticFieldCount = 0;

        for (Field field : AccessibilityEvent.class.getDeclaredFields()) {
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                nonStaticFieldCount++;
            }
        }

        String message = "New fields have been added, so add code to test marchalling them.";
        assertEquals(message, NON_STATIC_FIELD_COUNT, nonStaticFieldCount);
    }

    /**
     * Fully populates the {@link AccessibilityEvent} to marshal.
     *
     * @param sentEvent The event to populate.
     */
    private void fullyPopulateSentAccessibilityEvent(AccessibilityEvent sentEvent) {
        sentEvent.setAddedCount(1);
        sentEvent.setBeforeText("BeforeText");
        sentEvent.setChecked(true);
        sentEvent.setClassName("foo.bar.baz.Class");
        sentEvent.setContentDescription("ContentDescription");
        sentEvent.setCurrentItemIndex(1);
        sentEvent.setEnabled(true);
        sentEvent.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        sentEvent.setEventTime(1000);
        sentEvent.setFromIndex(1);
        sentEvent.setFullScreen(true);
        sentEvent.setItemCount(1);
        sentEvent.setPackageName("foo.bar.baz");
        sentEvent.setParcelableData(Message.obtain(null, 1, 2, 3));
        sentEvent.setPassword(true);
        sentEvent.setRemovedCount(1);
        sentEvent.getText().add("Foo");
    }

    /**
     * Compares all properties of the <code>expectedEvent</code> and the
     * <code>receviedEvent</code> to verify that the received event is the one
     * that is expected.
     */
    public static void assertEqualsAccessiblityEvent(AccessibilityEvent expectedEvent,
            AccessibilityEvent receivedEvent) {
        assertEquals("addedCount has incorrect value", expectedEvent.getAddedCount(), receivedEvent
                .getAddedCount());
        assertEquals("beforeText has incorrect value", expectedEvent.getBeforeText(), receivedEvent
                .getBeforeText());
        assertEquals("checked has incorrect value", expectedEvent.isChecked(), receivedEvent
                .isChecked());
        assertEquals("className has incorrect value", expectedEvent.getClassName(), receivedEvent
                .getClassName());
        assertEquals("contentDescription has incorrect value", expectedEvent
                .getContentDescription(), receivedEvent.getContentDescription());
        assertEquals("currentItemIndex has incorrect value", expectedEvent.getCurrentItemIndex(),
                receivedEvent.getCurrentItemIndex());
        assertEquals("enabled has incorrect value", expectedEvent.isEnabled(), receivedEvent
                .isEnabled());
        assertEquals("eventType has incorrect value", expectedEvent.getEventType(), receivedEvent
                .getEventType());
        assertEquals("fromIndex has incorrect value", expectedEvent.getFromIndex(), receivedEvent
                .getFromIndex());
        assertEquals("fullScreen has incorrect value", expectedEvent.isFullScreen(), receivedEvent
                .isFullScreen());
        assertEquals("itemCount has incorrect value", expectedEvent.getItemCount(), receivedEvent
                .getItemCount());
        assertEquals("password has incorrect value", expectedEvent.isPassword(), receivedEvent
                .isPassword());
        assertEquals("removedCount has incorrect value", expectedEvent.getRemovedCount(),
                receivedEvent.getRemovedCount());
        assertEqualsText(expectedEvent, receivedEvent);
    }

    /**
     * Compares the text of the <code>expectedEvent</code> and
     * <code>receivedEvent</code> by comparing the string representation of the
     * corresponding {@link CharSequence}s.
     */
    public static void assertEqualsText(AccessibilityEvent expectedEvent,
            AccessibilityEvent receivedEvent) {
        String message = "text has incorrect value";
        List<CharSequence> expectedText = expectedEvent.getText();
        List<CharSequence> receivedText = receivedEvent.getText();

        TestCase.assertEquals(message, expectedText.size(), receivedText.size());

        Iterator<CharSequence> expectedTextIterator = expectedText.iterator();
        Iterator<CharSequence> receivedTextIterator = receivedText.iterator();

        for (int i = 0; i < expectedText.size(); i++) {
            // compare the string representation
            TestCase.assertEquals(message, expectedTextIterator.next().toString(),
                    receivedTextIterator.next().toString());
        }
    }

    /**
     * Asserts that an {@link AccessibilityEvent} is cleared.
     *
     * @param event The event to check.
     */
    public static void assertAccessibilityEventCleared(AccessibilityEvent event) {
        TestCase.assertEquals("addedCount not properly recycled", 0, event.getAddedCount());
        TestCase.assertNull("beforeText not properly recycled", event.getBeforeText());
        TestCase.assertNull("className not properly recycled", event.getClassName());
        TestCase.assertNull("contentDescription not properly recycled", event
                .getContentDescription());
        TestCase.assertEquals("currentItemIndex not properly recycled", -1, event
                .getCurrentItemIndex());
        TestCase.assertEquals("eventTime not properly recycled", 0, event.getEventTime());
        TestCase.assertEquals("eventType not properly recycled", 0, event.getEventType());
        TestCase.assertEquals("fromIndex not properly recycled", 0, event.getFromIndex());
        TestCase.assertEquals("itemCount not properly recycled", 0, event.getItemCount());
        TestCase.assertNull("packageName not properly recycled", event.getPackageName());
        // This will fail and is fixed in Gingerbread Bug: 2593810
        // TestCase.assertNull("parcelableData not properly recycled", event.getParcelableData());
        TestCase.assertEquals("removedCount not properly recycled", 0, event.getRemovedCount());
        TestCase.assertTrue("text not properly recycled", event.getText().isEmpty());
    }
}
