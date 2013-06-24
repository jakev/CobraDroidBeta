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

package android.accessibilityservice.cts;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.IAccessibilityServiceDelegate;
import android.accessibilityservice.IAccessibilityServiceDelegateConnection;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.cts.accessibilityservice.R;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class performs end-to-end testing of the accessibility feature by
 * creating an {@link Activity} and poking around so {@link AccessibilityEvent}s
 * are generated and their correct dispatch verified.
 * </p>
 * Note: The end-to-end test is composed of two APKs, one with a delegating accessibility
 * service, another with the instrumented activity and test cases. The motivation for
 * two APKs design is that CTS tests cannot access the secure settings which is
 * required for enabling accessibility and accessibility services. Therefore, manual
 * installation of the <strong>CtsDelegatingAccessibilityService.apk</strong>
 * whose source is located at <strong>cts/tests/accessibilityservice</strong> is required.
 * Once the former package has been installed accessibility must be enabled (Settings ->
 * Accessibility), the delegating service must be enabled (Settings -> Accessibility
 * -> Delegating Accessibility Service), and then the CTS tests in this package can be
 * successfully run. Further, the delegate and tests run in separate processes since
 * the instrumentation restarts the process in which it is running and this
 * breaks the binding between the delegating accessibility service and the system.
 */
public class AccessibilityEndToEndTest extends
        ActivityInstrumentationTestCase2<AccessibilityEndToEndTestActivity> {

    /**
     * Timeout required for pending Binder calls or event processing to
     * complete.
     */
    private static final long MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING = 1000;

    /**
     * The count of the polling attempts during {@link #MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING}
     */
    private static final long COUNT_POLLING_ATTEMPTS = 10;

    /**
     * The package of the accessibility service mock interface.
     */
    private static final String DELEGATING_SERVICE_PACKAGE =
        "android.accessibilityservice.delegate";

    /**
     * The package of the delegating accessibility service interface.
     */
    private static final String DELEGATING_SERVICE_CLASS_NAME =
        "android.accessibilityservice.delegate.DelegatingAccessibilityService";

    /**
     * The package of the delegating accessibility service connection interface.
     */
    private static final String DELEGATING_SERVICE_CONNECTION_CLASS_NAME =
        "android.accessibilityservice.delegate."
            + "DelegatingAccessibilityService$DelegatingConnectionService";

    /**
     * Creates a new instance for testing
     * {@link AccessibilityEndToEndTestActivity}.
     *
     * @throws Exception If any error occurs.
     */
    public AccessibilityEndToEndTest() throws Exception {
        super("com.android.cts.accessibilityservice", AccessibilityEndToEndTestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // wait for the activity to settle down so we do not receive
        // the event for its start, thus breaking the tests
        getInstrumentation().waitForIdleSync();
    }

    @LargeTest
    public void testTypeViewSelectedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();

        // create and populate the expected event
        AccessibilityEvent selectedEvent = AccessibilityEvent.obtain();
        selectedEvent.setEventType(AccessibilityEvent.TYPE_VIEW_SELECTED);
        selectedEvent.setClassName(ListView.class.getName());
        selectedEvent.setPackageName(getActivity().getPackageName());
        selectedEvent.getText().add(activity.getString(R.string.second_list_item));
        selectedEvent.setItemCount(2);
        selectedEvent.setCurrentItemIndex(1);
        selectedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(selectedEvent);
        service.replay();

        // trigger the event
        final ListView listView = (ListView) activity.findViewById(R.id.listview);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                listView.setSelection(1);
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeViewClickedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();

        // create and populate the expected event
        AccessibilityEvent clickedEvent = AccessibilityEvent.obtain();
        clickedEvent.setEventType(AccessibilityEvent.TYPE_VIEW_CLICKED);
        clickedEvent.setClassName(Button.class.getName());
        clickedEvent.setPackageName(getActivity().getPackageName());
        clickedEvent.getText().add(activity.getString(R.string.button_title));
        clickedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(clickedEvent);
        service.replay();

        // trigger the event
        final Button button = (Button) activity.findViewById(R.id.button);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                button.performClick();
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeViewLongClickedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();

        // create and populate the expected event
        AccessibilityEvent longClickedEvent = AccessibilityEvent.obtain();
        longClickedEvent.setEventType(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
        longClickedEvent.setClassName(Button.class.getName());
        longClickedEvent.setPackageName(getActivity().getPackageName());
        longClickedEvent.getText().add(activity.getString(R.string.button_title));
        longClickedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(longClickedEvent);
        service.replay();

        // trigger the event
        final Button button = (Button) activity.findViewById(R.id.button);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                button.performLongClick();
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeViewFocusedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();

        // create and populate the expected event
        AccessibilityEvent focusedEvent = AccessibilityEvent.obtain();
        focusedEvent.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        focusedEvent.setClassName(Button.class.getName());
        focusedEvent.setPackageName(getActivity().getPackageName());
        focusedEvent.getText().add(activity.getString(R.string.button_title));
        focusedEvent.setItemCount(3);
        focusedEvent.setCurrentItemIndex(2);
        focusedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(focusedEvent);
        service.replay();

        // trigger the event
        final Button button = (Button) activity.findViewById(R.id.button);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                button.requestFocus();
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeViewTextChangedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();

        // focus the edit text
        final EditText editText = (EditText) activity.findViewById(R.id.edittext);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                editText.requestFocus();
            }
        });

        // wait for the generated focus event to be dispatched
        Thread.sleep(MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING);

        final String beforeText = activity.getString(R.string.text_input_blah);
        final String newText = activity.getString(R.string.text_input_blah_blah);
        final String afterText = beforeText.substring(0, 3) + newText;

        // create and populate the expected event
        AccessibilityEvent textChangedEvent = AccessibilityEvent.obtain();
        textChangedEvent.setEventType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        textChangedEvent.setClassName(EditText.class.getName());
        textChangedEvent.setPackageName(getActivity().getPackageName());
        textChangedEvent.getText().add(afterText);
        textChangedEvent.setBeforeText(beforeText);
        textChangedEvent.setFromIndex(3);
        textChangedEvent.setAddedCount(9);
        textChangedEvent.setRemovedCount(1);
        textChangedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(textChangedEvent);
        service.replay();

        // trigger the event
        activity.runOnUiThread(new Runnable() {
            public void run() {
                editText.getEditableText().replace(3, 4, newText);
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeWindowStateChangedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();
        String title = activity.getString(R.string.alert_title);
        String message = activity.getString(R.string.alert_message);

        // create and populate the expected event
        AccessibilityEvent windowStateChangedEvent = AccessibilityEvent.obtain();
        windowStateChangedEvent.setEventType(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        windowStateChangedEvent.setClassName(AlertDialog.class.getName());
        windowStateChangedEvent.setPackageName(getActivity().getPackageName());
        windowStateChangedEvent.getText().add(title);
        windowStateChangedEvent.getText().add(message);
        windowStateChangedEvent.setEnabled(true);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(windowStateChangedEvent);
        service.replay();

        // trigger the event
        final EditText editText = (EditText) activity.findViewById(R.id.edittext);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog dialog = (new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_title).setMessage(R.string.alert_message))
                        .create();
                dialog.show();
            }
        });

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);
    }

    @LargeTest
    public void testTypeNotificationStateChangedAccessibilityEvent() throws Throwable {
        Activity activity = getActivity();
        String message = activity.getString(R.string.notification_message);

        // create the notification to send
        int notificationId = 1;
        Notification notification = new Notification();
        notification.icon = android.R.drawable.stat_notify_call_mute;
        notification.contentIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(),
                PendingIntent.FLAG_CANCEL_CURRENT);
        notification.tickerText = message;
        notification.setLatestEventInfo(getActivity(), "", "", notification.contentIntent);

        // create and populate the expected event
        AccessibilityEvent notificationChangedEvent = AccessibilityEvent.obtain();
        notificationChangedEvent.setEventType(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        notificationChangedEvent.setClassName(Notification.class.getName());
        notificationChangedEvent.setPackageName(getActivity().getPackageName());
        notificationChangedEvent.getText().add(message);
        notificationChangedEvent.setParcelableData(notification);

        // set expectations
        MockAccessibilityService service = MockAccessibilityService.getInstance(activity);
        service.expectEvent(notificationChangedEvent);
        service.replay();

        // trigger the event
        NotificationManager notificationManager = (NotificationManager) activity
                .getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);

        // verify if all expected methods have been called
        assertMockServiceVerifiedWithinTimeout(service);

        // remove the notification
        notificationManager.cancel(notificationId);
    }

    /**
     * Asserts the the mock accessibility service has been successfully verified
     * (which is it has received the expected method calls with expected
     * arguments) within the {@link #MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING}. The
     * verified state is checked by polling upon small intervals.
     *
     * @param service The service to verify.
     * @throws Exception If the verification has failed with exception after the
     *             {@link #MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING}.
     */
    private void assertMockServiceVerifiedWithinTimeout(MockAccessibilityService service)
            throws Throwable {
        Throwable lastVerifyThrowable = null;
        long beginTime = SystemClock.uptimeMillis();
        long pollTmeout = MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING / COUNT_POLLING_ATTEMPTS;

        // poll until the timeout has elapsed
        while (SystemClock.uptimeMillis() - beginTime < MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING) {
            // sleep first since immediate call will always fail
            try {
                Thread.sleep(pollTmeout);
            } catch (InterruptedException ie) {
                /* ignore */
            }

            try {
                service.verify();
                // success - reset so it is not accept more events
                service.reset();
                return;
            } catch (IllegalStateException ise) {
                // this exception is thrown if the expected event is not
                // received yet, so we will keep trying within the timeout
                lastVerifyThrowable = ise;
                continue;
            } catch (Throwable t) {
                // we have just failed
                lastVerifyThrowable = t;
                break;
            }
        }

        // failure - reset so it is not accept more events
        service.reset();
        throw lastVerifyThrowable;
    }

    static class MockAccessibilityService extends AccessibilityService implements
            ServiceConnection {

        /**
         * The singleton instance.
         */
        private static MockAccessibilityService sInstance;

        /**
         * The events this service expects to receive.
         */
        private final Queue<AccessibilityEvent> mExpectedEvents =
            new LinkedList<AccessibilityEvent>();

        /**
         * Interruption call this service expects to receive.
         */
        private boolean mExpectedInterrupt;

        /**
         * Flag if the mock is currently replaying.
         */
        private boolean mReplaying;

        /**
         * Flag indicating if this mock is initialized.
         */
        private boolean mInitialized;

        /**
         * The {@link Context} whose services to utilize.
         */
        private Context mContext;

        /**
         * Gets the {@link MockAccessibilityService} singleton.
         *
         * @param context A context handle.
         * @return The mock service.
         */
        public static MockAccessibilityService getInstance(Context context) {
            if (sInstance == null) {
                // since we do bind once and do not unbind from the delegating
                // service and JUnit3 does not support @BeforeTest and @AfterTest,
                // we will leak a service connection after the test but this
                // does not affect the test results and the test is twice as fast
                sInstance = new MockAccessibilityService(context);
            }
            return sInstance;
        }

        /**
         * Creates a new instance.
         */
        private MockAccessibilityService(Context context) {
            mContext = context;
            ensureSetupAndBoundToDelegatingAccessibilityService();
        }

        /**
         * Ensures the required setup for the test performed and that it is bound to the
         * DelegatingAccessibilityService which runs in another process. The setup is
         * enabling accessibility and installing and enabling the delegating accessibility
         * service this test binds to.
         * </p>
         * Note: Please look at the class description for information why such an
         *       approach is taken.
         */
        public void ensureSetupAndBoundToDelegatingAccessibilityService() {
            // check if accessibility is enabled
            AccessibilityManager accessibilityManager = (AccessibilityManager) mContext
                    .getSystemService(Service.ACCESSIBILITY_SERVICE);

            if (!accessibilityManager.isEnabled()) {
                throw new IllegalStateException("Accessibility not enabled. "
                        + "(Settings -> Accessibility)");
            }

            // check if the delegating service is running
            ComponentName delegatingServiceName = new ComponentName(
                    DELEGATING_SERVICE_PACKAGE, DELEGATING_SERVICE_CLASS_NAME);
            ActivityManager activityManager = (ActivityManager) mContext
                    .getSystemService(Service.ACTIVITY_SERVICE);
            boolean delegatingServiceRunning = false;

            for (RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(100)) {
                if (delegatingServiceName.equals(runningServiceInfo.service)) {
                    delegatingServiceRunning = true;
                    break;
                }
            }

            if (!delegatingServiceRunning) {
                // delegating service not running, so check if it is installed at all
                try {
                    PackageManager packageManager = mContext.getPackageManager();
                    packageManager.getServiceInfo(delegatingServiceName, 0);
                } catch (NameNotFoundException nnfe) {
                    throw new IllegalStateException("CtsDelegatingAccessibilityService.apk" +
                            " not installed.");
                }

                throw new IllegalStateException("Delegating Accessibility Service not running."
                         + "(Settings -> Accessibility -> Delegating Accessibility Service)");
            }

            Intent intent = new Intent().setClassName(DELEGATING_SERVICE_PACKAGE,
                    DELEGATING_SERVICE_CONNECTION_CLASS_NAME);
            mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);

            long beginTime = SystemClock.uptimeMillis();
            long pollTmeout = MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING / COUNT_POLLING_ATTEMPTS;

            // bind to the delegating service which runs in another process by
            // polling until the binder connection is established
            while (SystemClock.uptimeMillis() - beginTime < MAX_TIMEOUT_ASYNCHRONOUS_PROCESSING) {
                if (mInitialized) {
                    // success
                    return;
                }
                try {
                    Thread.sleep(pollTmeout);
                } catch (InterruptedException ie) {
                    /* ignore */
                }
            }
        }

        /**
         * Starts replaying the mock.
         */
        private void replay() {
            mReplaying = true;
        }

        /**
         * Verifies if all expected service methods have been called.
         */
        private void verify() {
            synchronized (this) {
                if (!mReplaying) {
                    throw new IllegalStateException("Did you forget to call replay()");
                }
                if (mExpectedInterrupt) {
                    throw new IllegalStateException("Expected call to #interrupt() not received");
                }
                if (!mExpectedEvents.isEmpty()) {
                    throw new IllegalStateException("Expected a call to onAccessibilityEvent() for "
                            + "events \"" + mExpectedEvents + "\" not received");
                }
            }
        }

        /**
         * Resets this instance so it can be reused.
         */
        private void reset() {
            synchronized (this) {
                mExpectedEvents.clear();
                mExpectedInterrupt = false;
                mReplaying = false;
            }
        }

        /**
         * Sets an expected call to
         * {@link #onAccessibilityEvent(AccessibilityEvent)} with given event as
         * argument.
         *
         * @param expectedEvent The expected event argument.
         */
        private void expectEvent(AccessibilityEvent expectedEvent) {
            mExpectedEvents.add(expectedEvent);
        }

        /**
         * Sets an expected call of {@link #onInterrupt()}.
         */
        public void expectInterrupt() {
            mExpectedInterrupt = true;
        }

        @Override
        public void onAccessibilityEvent(AccessibilityEvent receivedEvent) {
            synchronized (this) {
                if (!mReplaying) {
                    return;
                }
                if (mExpectedEvents.isEmpty()) {
                    throw new IllegalStateException("Unexpected event: " + receivedEvent);
                }
                AccessibilityEvent expectedEvent = mExpectedEvents.poll();
                assertEqualsAccessiblityEvent(expectedEvent, receivedEvent);

            }
        }

        @Override
        public void onInterrupt() {
            synchronized (this) {
                if (!mReplaying) {
                    return;
                }

                if (!mExpectedInterrupt) {
                    throw new IllegalStateException("Unexpected call to onInterrupt()");
                }

                mExpectedInterrupt = false;
            }
        }

        /**
         * {@inheritDoc ServiceConnection#onServiceConnected(ComponentName,IBinder)}
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            IAccessibilityServiceDelegateConnection connection =
                IAccessibilityServiceDelegateConnection.Stub
                    .asInterface(service);
            try {
                connection.setAccessibilityServiceDelegate(new AccessibilityServiceDelegate(this));
                mInitialized = true;
            } catch (RemoteException re) {
                fail("Could not set delegate to the delegating service.");
            }
        }

        /**
         * {@inheritDoc ServiceConnection#onServiceDisconnected(ComponentName)}
         */
        public void onServiceDisconnected(ComponentName name) {
            mInitialized = false;
            /* do nothing */
        }

        /**
         * Compares all properties of the <code>expectedEvent</code> and the
         * <code>receviedEvent</code> to verify that the received event is the
         * one that is expected.
         */
        private void assertEqualsAccessiblityEvent(AccessibilityEvent expectedEvent,
                AccessibilityEvent receivedEvent) {
            TestCase.assertEquals("addedCount has incorrect value", expectedEvent.getAddedCount(),
                    receivedEvent.getAddedCount());
            TestCase.assertEquals("beforeText has incorrect value", expectedEvent.getBeforeText(),
                    receivedEvent.getBeforeText());
            TestCase.assertEquals("checked has incorrect value", expectedEvent.isChecked(),
                    receivedEvent.isChecked());
            TestCase.assertEquals("className has incorrect value", expectedEvent.getClassName(),
                    receivedEvent.getClassName());
            TestCase.assertEquals("contentDescription has incorrect value", expectedEvent
                    .getContentDescription(), receivedEvent.getContentDescription());
            TestCase.assertEquals("currentItemIndex has incorrect value", expectedEvent
                    .getCurrentItemIndex(), receivedEvent.getCurrentItemIndex());
            TestCase.assertEquals("enabled has incorrect value", expectedEvent.isEnabled(),
                    receivedEvent.isEnabled());
            TestCase.assertEquals("eventType has incorrect value", expectedEvent.getEventType(),
                    receivedEvent.getEventType());
            TestCase.assertEquals("fromIndex has incorrect value", expectedEvent.getFromIndex(),
                    receivedEvent.getFromIndex());
            TestCase.assertEquals("fullScreen has incorrect value", expectedEvent.isFullScreen(),
                    receivedEvent.isFullScreen());
            TestCase.assertEquals("itemCount has incorrect value", expectedEvent.getItemCount(),
                    receivedEvent.getItemCount());
            // This will fail due to a bug fixed in Gingerbread. Bug 2593810 (removed the method).
            // assertEqualsNotificationAsParcelableData(expectedEvent, receivedEvent);
            TestCase.assertEquals("password has incorrect value", expectedEvent.isPassword(),
                    receivedEvent.isPassword());
            TestCase.assertEquals("removedCount has incorrect value", expectedEvent
                    .getRemovedCount(), receivedEvent.getRemovedCount());
            assertEqualsText(expectedEvent, receivedEvent);
        }

        /**
         * Compares the text of the <code>expectedEvent</code> and
         * <code>receivedEvent</code> by comparing the string representation of
         * the corresponding {@link CharSequence}s.
         */
        private void assertEqualsText(AccessibilityEvent expectedEvent,
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
         * This class is the delegate called by the DelegatingAccessibilityService.
         */
        private class AccessibilityServiceDelegate extends
                IAccessibilityServiceDelegate.Stub implements Handler.Callback {

            /**
             * Tag for logging.
             */
            private static final String LOG_TAG = "AccessibilityServiceDelegate";

            /**
             * Message type for calling {@link #onInterrupt()}
             */
            private static final int DO_ON_INTERRUPT = 10;

            /**
             * Message type for calling {@link #onAccessibilityEvent(AccessibilityEvent)}
             */
            private static final int DO_ON_ACCESSIBILITY_EVENT = 20;

            /**
             * Caller for handling {@link Message}s
             */
            private final Handler mHandler;

            /**
             * The {@link MockAccessibilityService} to which to delegate;
             */
            private MockAccessibilityService mMockAccessibilityService;

            /**
             * Creates a new instance.
             *
             * @param mockAccessibilityService The service to whcih to delegate.
             */
            public AccessibilityServiceDelegate(MockAccessibilityService mockAccessibilityService) {
                mMockAccessibilityService = mockAccessibilityService;
                mHandler = new Handler(this);
            }

            /**
             * {@inheritDoc IAccessibilityServiceDelegate#onAccessibilityEvent(AccessibilityEvent)}
             */
            public void onAccessibilityEvent(AccessibilityEvent event) {
                Message message = Message.obtain(mHandler, DO_ON_ACCESSIBILITY_EVENT, event);
                mHandler.sendMessage(message);
            }

            /**
             * {@inheritDoc IAccessibilityServiceDelegate#onInterrupt()}
             */
            public void onInterrupt() {
                Message message = mHandler.obtainMessage(DO_ON_INTERRUPT);
                mHandler.sendMessage(message);
            }

            /**
             * {@inheritDoc Handler.Callback#handleMessage(Message)}
             */
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case DO_ON_ACCESSIBILITY_EVENT:
                        AccessibilityEvent event = (AccessibilityEvent) message.obj;
                        if (event != null) {
                            mMockAccessibilityService.onAccessibilityEvent(event);
                            event.recycle();
                        }
                        return true;
                    case DO_ON_INTERRUPT:
                        mMockAccessibilityService.onInterrupt();
                        return true;
                    default:
                        Log.w(LOG_TAG, "Unknown message type " + message.what);
                        return false;
                }
            }
        }
    }
}
