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

package android.location.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.test.InstrumentationTestCase;

import java.util.List;

/**
 * Requires the permissions
 * android.permission.ACCESS_MOCK_LOCATION to mock provider
 * android.permission.ACCESS_COARSE_LOCATION to access network provider
 * android.permission.ACCESS_FINE_LOCATION to access GPS provider
 * android.permission.ACCESS_LOCATION_EXTRA_COMMANDS to send extra commands to GPS provider
 */
@TestTargetClass(LocationManager.class)
public class LocationManagerTest extends InstrumentationTestCase {
    private static final long TEST_TIME_OUT = 5000;

    private static final String TEST_MOCK_PROVIDER_NAME = "test_provider";

    private static final String UNKNOWN_PROVIDER_NAME = "unknown_provider";

    private LocationManager mManager;

    private Context mContext;

    private PendingIntent mPendingIntent;

    private TestIntentReceiver mIntentReceiver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();

        mManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // test that mock locations are allowed so a more descriptive error message can be logged
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {
            fail("Mock locations are currently disabled in Settings - this test requires "
                    + "mock locations");
        }

        // remove test provider if left over from an aborted run
        LocationProvider lp = mManager.getProvider(TEST_MOCK_PROVIDER_NAME);
        if (lp != null) {
            mManager.removeTestProvider(TEST_MOCK_PROVIDER_NAME);
        }

        addTestProvider(TEST_MOCK_PROVIDER_NAME);
    }

    /**
     * Helper method to add a test provider with given name.
     */
    private void addTestProvider(final String providerName) {
        mManager.addTestProvider(providerName, true, //requiresNetwork,
                false, // requiresSatellite,
                true,  // requiresCell,
                false, // hasMonetaryCost,
                false, // supportsAltitude,
                false, // supportsSpeed,
                false, // supportsBearing,
                Criteria.POWER_MEDIUM, // powerRequirement
                Criteria.ACCURACY_FINE); // accuracy
        mManager.setTestProviderEnabled(providerName, true);
    }

    @Override
    protected void tearDown() throws Exception {
        LocationProvider provider = mManager.getProvider(TEST_MOCK_PROVIDER_NAME);
        if (provider != null) {
            mManager.removeTestProvider(TEST_MOCK_PROVIDER_NAME);
        }
        if (mPendingIntent != null) {
            mManager.removeProximityAlert(mPendingIntent);
        }
        if (mIntentReceiver != null) {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeTestProvider",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addTestProvider",
            args = {String.class, boolean.class, boolean.class, boolean.class, boolean.class,
                    boolean.class, boolean.class, boolean.class, int.class, int.class}
        )
    })
    public void testRemoveTestProvider() {
        // this test assumes TEST_MOCK_PROVIDER_NAME was created in setUp.
        LocationProvider provider = mManager.getProvider(TEST_MOCK_PROVIDER_NAME);
        assertNotNull(provider);

        try {
            mManager.addTestProvider(TEST_MOCK_PROVIDER_NAME, true, //requiresNetwork,
                    false, // requiresSatellite,
                    true,  // requiresCell,
                    false, // hasMonetaryCost,
                    false, // supportsAltitude,
                    false, // supportsSpeed,
                    false, // supportsBearing,
                    Criteria.POWER_MEDIUM, // powerRequirement
                    Criteria.ACCURACY_FINE); // accuracy
            fail("Should throw IllegalArgumentException when provider already exists!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        mManager.removeTestProvider(TEST_MOCK_PROVIDER_NAME);
        provider = mManager.getProvider(TEST_MOCK_PROVIDER_NAME);
        assertNull(provider);

        try {
            mManager.removeTestProvider(UNKNOWN_PROVIDER_NAME);
            fail("Should throw IllegalArgumentException when no provider exists!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAllProviders",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProviders",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderEnabled",
            args = {String.class, boolean.class}
        )
    })
    public void testGetProviders() {
        List<String> providers = mManager.getAllProviders();
        assertTrue(providers.size() >= 2);
        assertTrue(hasTestProvider(providers));

        assertEquals(hasGpsFeature(), hasGpsProvider(providers));

        int oldSizeAllProviders = providers.size();

        providers = mManager.getProviders(false);
        assertEquals(oldSizeAllProviders, providers.size());
        assertTrue(hasTestProvider(providers));

        providers = mManager.getProviders(true);
        assertTrue(providers.size() >= 1);
        assertTrue(hasTestProvider(providers));
        int oldSizeTrueProviders = providers.size();

        mManager.setTestProviderEnabled(TEST_MOCK_PROVIDER_NAME, false);
        providers = mManager.getProviders(true);
        assertEquals(oldSizeTrueProviders - 1, providers.size());
        assertFalse(hasTestProvider(providers));

        providers = mManager.getProviders(false);
        assertEquals(oldSizeAllProviders, providers.size());
        assertTrue(hasTestProvider(providers));

        mManager.removeTestProvider(TEST_MOCK_PROVIDER_NAME);
        providers = mManager.getAllProviders();
        assertEquals(oldSizeAllProviders - 1, providers.size());
        assertFalse(hasTestProvider(providers));
    }

    private boolean hasTestProvider(List<String> providers) {
        return hasProvider(providers, TEST_MOCK_PROVIDER_NAME);
    }

    private boolean hasGpsProvider(List<String> providers) {
        return hasProvider(providers, LocationManager.GPS_PROVIDER);
    }

    private boolean hasGpsFeature() {
        return mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_LOCATION_GPS);
    }

    private boolean hasProvider(List<String> providers, String providerName) {
        for (String provider : providers) {
            if (provider != null && provider.equals(providerName)) {
                return true;
            }
        }
        return false;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getProvider",
        args = {String.class}
    )
    public void testGetProvider() {
        LocationProvider p = mManager.getProvider(TEST_MOCK_PROVIDER_NAME);
        assertNotNull(p);
        assertEquals(TEST_MOCK_PROVIDER_NAME, p.getName());

        p = mManager.getProvider(LocationManager.GPS_PROVIDER);
        if (hasGpsFeature()) {
            assertNotNull(p);
            assertEquals(LocationManager.GPS_PROVIDER, p.getName());
        } else {
            assertNull(p);
        }

        p = mManager.getProvider(UNKNOWN_PROVIDER_NAME);
        assertNull(p);

        try {
            mManager.getProvider(null);
            fail("Should throw IllegalArgumentException when provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProviders",
            args = {Criteria.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBestProvider",
            args = {Criteria.class, boolean.class}
        )
    })
    public void testGetProvidersWithCriteria() {
        Criteria criteria = new Criteria();
        List<String> providers = mManager.getProviders(criteria, true);
        assertTrue(providers.size() >= 1);
        assertTrue(hasTestProvider(providers));

        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String p = mManager.getBestProvider(criteria, true);
        if (p != null) { // we may not have any enabled providers
            assertTrue(mManager.isProviderEnabled(p));
        }

        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        p = mManager.getBestProvider(criteria, false);
        assertNotNull(p);

        criteria.setPowerRequirement(Criteria.POWER_LOW);
        p = mManager.getBestProvider(criteria, true);
        if (p != null) { // we may not have any enabled providers
            assertTrue(mManager.isProviderEnabled(p));
        }

        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        p = mManager.getBestProvider(criteria, false);
        assertNotNull(p);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestLocationUpdates",
            args = {String.class, long.class, float.class, LocationListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeUpdates",
            args = {LocationListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderLocation",
            args = {String.class, Location.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Cannot determine whether location has been cleared",
            method = "clearTestProviderLocation",
            args = {String.class}
        )
    })
    public void testLocationUpdatesWithLocationListener() throws InterruptedException {
        doLocationUpdatesWithLocationListener(TEST_MOCK_PROVIDER_NAME);

        try {
            mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    (LocationListener) null);
            fail("Should throw IllegalArgumentException if param listener is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.requestLocationUpdates(null, 0, 0, new MockLocationListener());
            fail("Should throw IllegalArgumentException if param provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.removeUpdates( (LocationListener) null );
            fail("Should throw IllegalArgumentException if listener is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.clearTestProviderLocation(UNKNOWN_PROVIDER_NAME);
            fail("Should throw IllegalArgumentException if provider is unknown!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Helper method to test a location update with given provider
     *
     * @param providerName name of provider to test. Must already exist.
     * @throws InterruptedException
     */
    private void doLocationUpdatesWithLocationListener(final String providerName)
            throws InterruptedException {
        final double latitude1 = 10;
        final double longitude1 = 40;
        final double latitude2 = 35;
        final double longitude2 = 80;
        final MockLocationListener listener = new MockLocationListener();

        // update location and notify listener
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                mManager.requestLocationUpdates(providerName, 0, 0, listener);
                listener.setLocationRequested();
                Looper.loop();
            }
        }).start();
        // wait for location requested to be called first, otherwise setLocation can be called
        // before there is a listener attached
        assertTrue(listener.hasCalledLocationRequested(TEST_TIME_OUT));
        updateLocation(providerName, latitude1, longitude1);
        assertTrue(listener.hasCalledOnLocationChanged(TEST_TIME_OUT));
        Location location = listener.getLocation();
        assertEquals(providerName, location.getProvider());
        assertEquals(latitude1, location.getLatitude());
        assertEquals(longitude1, location.getLongitude());

        // update location without notifying listener
        listener.reset();
        assertFalse(listener.hasCalledOnLocationChanged(0));
        mManager.removeUpdates(listener);
        updateLocation(providerName, latitude2, longitude2);
        assertFalse(listener.hasCalledOnLocationChanged(TEST_TIME_OUT));
    }

    /**
     * Verifies that all real location providers can be replaced by a mock provider.
     * <p/>
     * This feature is quite useful for developer automated testing.
     * This test may fail if another unknown test provider already exists, because there is no
     * known way to determine if a given provider is a test provider.
     * @throws InterruptedException
     */
    public void testReplaceRealProvidersWithMocks() throws InterruptedException {
        for (String providerName : mManager.getAllProviders()) {
            if (!providerName.equals(TEST_MOCK_PROVIDER_NAME) &&
                !providerName.equals(LocationManager.PASSIVE_PROVIDER)) {
                addTestProvider(providerName);
                try {
                    // run the update location test logic to ensure location updates can be injected
                    doLocationUpdatesWithLocationListener(providerName);
                } finally {
                    mManager.removeTestProvider(providerName);
                }
            }
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestLocationUpdates",
            args = {String.class, long.class, float.class, LocationListener.class, Looper.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeUpdates",
            args = {LocationListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderLocation",
            args = {String.class, Location.class}
        )
    })
    public void testLocationUpdatesWithLocationListenerAndLooper() throws InterruptedException {
        double latitude1 = 60;
        double longitude1 = 20;
        double latitude2 = 40;
        double longitude2 = 30;
        final MockLocationListener listener = new MockLocationListener();

        // update location and notify listener
        HandlerThread handlerThread = new HandlerThread("testLocationUpdates");
        handlerThread.start();
        mManager.requestLocationUpdates(TEST_MOCK_PROVIDER_NAME, 0, 0, listener,
                handlerThread.getLooper());

        updateLocation(latitude1, longitude1);
        assertTrue(listener.hasCalledOnLocationChanged(TEST_TIME_OUT));
        Location location = listener.getLocation();
        assertEquals(TEST_MOCK_PROVIDER_NAME, location.getProvider());
        assertEquals(latitude1, location.getLatitude());
        assertEquals(longitude1, location.getLongitude());

        // update location without notifying listener
        mManager.removeUpdates(listener);
        listener.reset();
        updateLocation(latitude2, longitude2);
        assertFalse(listener.hasCalledOnLocationChanged(TEST_TIME_OUT));

        try {
            mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    (LocationListener) null, Looper.myLooper());
            fail("Should throw IllegalArgumentException if param listener is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.requestLocationUpdates(null, 0, 0, listener, Looper.myLooper());
            fail("Should throw IllegalArgumentException if param provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.removeUpdates( (LocationListener) null );
            fail("Should throw IllegalArgumentException if listener is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestLocationUpdates",
            args = {String.class, long.class, float.class, PendingIntent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeUpdates",
            args = {PendingIntent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderLocation",
            args = {String.class, Location.class}
        )
    })
    public void testLocationUpdatesWithPendingIntent() throws InterruptedException {
        double latitude1 = 20;
        double longitude1 = 40;
        double latitude2 = 30;
        double longitude2 = 50;

        // update location and receive broadcast.
        registerIntentReceiver();
        mManager.requestLocationUpdates(TEST_MOCK_PROVIDER_NAME, 0, 0, mPendingIntent);
        updateLocation(latitude1, longitude1);
        waitForReceiveBroadcast();

        assertNotNull(mIntentReceiver.getLastReceivedIntent());
        Location location = mManager.getLastKnownLocation(TEST_MOCK_PROVIDER_NAME);
        assertEquals(TEST_MOCK_PROVIDER_NAME, location.getProvider());
        assertEquals(latitude1, location.getLatitude());
        assertEquals(longitude1, location.getLongitude());

        // update location without receiving broadcast.
        mManager.removeUpdates(mPendingIntent);
        mIntentReceiver.clearReceivedIntents();
        updateLocation(latitude2, longitude2);
        waitForReceiveBroadcast();
        assertNull(mIntentReceiver.getLastReceivedIntent());

        try {
            mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    (PendingIntent) null);
            fail("Should throw IllegalArgumentException if param intent is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.requestLocationUpdates(null, 0, 0, mPendingIntent);
            fail("Should throw IllegalArgumentException if param provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.removeUpdates( (PendingIntent) null );
            fail("Should throw IllegalArgumentException if intent is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addProximityAlert",
            args = {double.class, double.class, float.class, long.class, PendingIntent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeProximityAlert",
            args = {PendingIntent.class}
        )
    })
    public void testAddProximityAlert() {
        Intent i = new Intent();
        i.setAction("android.location.cts.TEST_GET_GPS_STATUS_ACTION");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);

        mManager.addProximityAlert(0, 0, 0, 5000, pi);
        mManager.removeProximityAlert(pi);

        mManager.addProximityAlert(0, 0, 0, 5000, null);
        mManager.removeProximityAlert(null);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isProviderEnabled",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderEnabled",
            args = {String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearTestProviderEnabled",
            args = {String.class}
        )
    })
    public void testIsProviderEnabled() {
        // this test assumes enabled TEST_MOCK_PROVIDER_NAME was created in setUp.
        assertNotNull(mManager.getProvider(TEST_MOCK_PROVIDER_NAME));
        assertTrue(mManager.isProviderEnabled(TEST_MOCK_PROVIDER_NAME));

        mManager.clearTestProviderEnabled(TEST_MOCK_PROVIDER_NAME);
        assertFalse(mManager.isProviderEnabled(TEST_MOCK_PROVIDER_NAME));

        mManager.setTestProviderEnabled(TEST_MOCK_PROVIDER_NAME, true);
        assertTrue(mManager.isProviderEnabled(TEST_MOCK_PROVIDER_NAME));

        try {
            mManager.isProviderEnabled(null);
            fail("Should throw IllegalArgumentException if provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.clearTestProviderEnabled(UNKNOWN_PROVIDER_NAME);
            fail("Should throw IllegalArgumentException if provider is unknown!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.setTestProviderEnabled(UNKNOWN_PROVIDER_NAME, false);
            fail("Should throw IllegalArgumentException if provider is unknown!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLastKnownLocation",
        args = {String.class}
    )
    public void testGetLastKnownLocation() throws InterruptedException {
        double latitude1 = 20;
        double longitude1 = 40;
        double latitude2 = 10;
        double longitude2 = 70;

        registerIntentReceiver();
        mManager.requestLocationUpdates(TEST_MOCK_PROVIDER_NAME, 0, 0, mPendingIntent);
        updateLocation(latitude1, longitude1);
        waitForReceiveBroadcast();

        assertNotNull(mIntentReceiver.getLastReceivedIntent());
        Location location = mManager.getLastKnownLocation(TEST_MOCK_PROVIDER_NAME);
        assertEquals(TEST_MOCK_PROVIDER_NAME, location.getProvider());
        assertEquals(latitude1, location.getLatitude());
        assertEquals(longitude1, location.getLongitude());

        mIntentReceiver.clearReceivedIntents();
        updateLocation(latitude2, longitude2);
        waitForReceiveBroadcast();

        assertNotNull(mIntentReceiver.getLastReceivedIntent());
        location = mManager.getLastKnownLocation(TEST_MOCK_PROVIDER_NAME);
        assertEquals(TEST_MOCK_PROVIDER_NAME, location.getProvider());
        assertEquals(latitude2, location.getLatitude());
        assertEquals(longitude2, location.getLongitude());

        try {
            mManager.getLastKnownLocation(null);
            fail("Should throw IllegalArgumentException if provider is null!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addGpsStatusListener",
            args = {GpsStatus.Listener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeGpsStatusListener",
            args = {GpsStatus.Listener.class}
        )
    })
    @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
            + "because there is no simulation of GPS events on the emulator")
    public void testGpsStatusListener() {
        MockGpsStatusListener listener = new MockGpsStatusListener();
        mManager.addGpsStatusListener(listener);
        mManager.removeGpsStatusListener(listener);

        mManager.addGpsStatusListener(null);
        mManager.removeGpsStatusListener(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getGpsStatus",
        args = {GpsStatus.class}
    )
    @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
            + "because there is no simulation of GPS events on the emulator")
    public void testGetGpsStatus() {
        GpsStatus status = mManager.getGpsStatus(null);
        assertNotNull(status);
        assertSame(status, mManager.getGpsStatus(status));
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Cannot rely on any specific extra command to be implemented.",
        method = "sendExtraCommand",
        args = {String.class, String.class, Bundle.class}
    )
    public void testSendExtraCommand() {
        // this test assumes TEST_MOCK_PROVIDER_NAME was created in setUp.
        assertNotNull(mManager.getProvider(TEST_MOCK_PROVIDER_NAME));
        // Unknown command
        assertFalse(mManager.sendExtraCommand(TEST_MOCK_PROVIDER_NAME, "unknown", new Bundle()));

        assertNull(mManager.getProvider(UNKNOWN_PROVIDER_NAME));
        assertFalse(mManager.sendExtraCommand(UNKNOWN_PROVIDER_NAME, "unknown", new Bundle()));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestLocationUpdates",
            args = {String.class, long.class, float.class, LocationListener.class, Looper.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTestProviderStatus",
            args = {String.class, int.class, Bundle.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Cannot determine whether status has been cleared",
            method = "clearTestProviderStatus",
            args = {String.class}
        )
    })
    public void testSetTestProviderStatus() throws InterruptedException {
        final int status = LocationProvider.TEMPORARILY_UNAVAILABLE;
        final long updateTime = 1000;
        final MockLocationListener listener = new MockLocationListener();

        HandlerThread handlerThread = new HandlerThread("testStatusUpdates");
        handlerThread.start();

        // set status successfully
        mManager.requestLocationUpdates(TEST_MOCK_PROVIDER_NAME, 0, 0, listener,
                handlerThread.getLooper());
        mManager.setTestProviderStatus(TEST_MOCK_PROVIDER_NAME, status, null, updateTime);
        // setting the status alone is not sufficient to trigger a status update
        updateLocation(10, 30);
        assertTrue(listener.hasCalledOnStatusChanged(TEST_TIME_OUT));
        assertEquals(TEST_MOCK_PROVIDER_NAME, listener.getProvider());
        assertEquals(status, listener.getStatus());

        try {
            mManager.setTestProviderStatus(UNKNOWN_PROVIDER_NAME, 0, null,
                    System.currentTimeMillis());
            fail("Should throw IllegalArgumentException if provider is unknown!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mManager.clearTestProviderStatus(UNKNOWN_PROVIDER_NAME);
            fail("Should throw IllegalArgumentException if provider is unknown!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests basic proximity alert when entering proximity
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addProximityAlert",
        args = {double.class, double.class, float.class, long.class, PendingIntent.class}
    )
    public void testEnterProximity() throws Exception {
        doTestEnterProximity(10000);
    }

    /**
     * Tests proximity alert when entering proximity, with no expiration
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addProximityAlert",
        args = {double.class, double.class, float.class, long.class, PendingIntent.class}
    )
    public void testEnterProximity_noexpire() throws Exception {
        doTestEnterProximity(-1);
    }

    /**
     * Tests basic proximity alert when exiting proximity
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addProximityAlert",
        args = {double.class, double.class, float.class, long.class, PendingIntent.class}
    )
    public void testExitProximity() throws Exception {
        // first do enter proximity scenario
        doTestEnterProximity(-1);

        // now update to trigger exit proximity proximity
        mIntentReceiver.clearReceivedIntents();
        updateLocation(20, 20);
        waitForReceiveBroadcast();
        assertProximityType(false);
    }

    /**
     * Helper variant for testing enter proximity scenario
     * TODO: add additional parameters as more scenarios are added
     *
     * @param expiration - expiration of proximity alert
     */
    private void doTestEnterProximity(long expiration) throws Exception {
        // update location to outside proximity range
        updateLocation(30, 30);
        registerProximityListener(0, 0, 1000, expiration);
        updateLocation(0, 0);
        waitForReceiveBroadcast();
        assertProximityType(true);
    }

    private void registerIntentReceiver() {
        String intentKey = "LocationManagerTest";
        Intent proximityIntent = new Intent(intentKey);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, proximityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mIntentReceiver = new TestIntentReceiver(intentKey);
        mContext.registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
    }

    /**
     * Registers the proximity intent receiver
     */
    private void registerProximityListener(double latitude, double longitude, float radius,
            long expiration) {
        registerIntentReceiver();
        mManager.addProximityAlert(latitude, longitude, radius, expiration, mPendingIntent);
    }

    /**
     * Blocks until receive intent notification or time out.
     *
     * @throws InterruptedException
     */
    private void waitForReceiveBroadcast() throws InterruptedException {
        synchronized (mIntentReceiver) {
            mIntentReceiver.wait(TEST_TIME_OUT);
        }
    }

    /**
     * Asserts that the received intent had the enter proximity property set as
     * expected
     *
     * @param expectedEnterProximity - true if enter proximity expected, false
     *            if exit expected
     */
    private void assertProximityType(boolean expectedEnterProximity) throws Exception {
        boolean proximityTest = mIntentReceiver.getLastReceivedIntent().getBooleanExtra(
                LocationManager.KEY_PROXIMITY_ENTERING, !expectedEnterProximity);
        assertEquals("proximity alert not set to expected enter proximity value",
                expectedEnterProximity, proximityTest);
    }

    private void updateLocation(final String providerName, final double latitude,
            final double longitude) {
        Location location = new Location(providerName);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        location.setTime(java.lang.System.currentTimeMillis());
        mManager.setTestProviderLocation(providerName, location);
    }

    private void updateLocation(final double latitude, final double longitude) {
        updateLocation(TEST_MOCK_PROVIDER_NAME, latitude, longitude);
    }

    /**
     * Helper class that receives a proximity intent and notifies the main class
     * when received
     */
    private static class TestIntentReceiver extends BroadcastReceiver {
        private String mExpectedAction;

        private Intent mLastReceivedIntent;

        public TestIntentReceiver(String expectedAction) {
            mExpectedAction = expectedAction;
            mLastReceivedIntent = null;
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter(mExpectedAction);
            return filter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && mExpectedAction.equals(intent.getAction())) {
                synchronized (this) {
                    mLastReceivedIntent = intent;
                    notify();
                }
            }
        }

        public Intent getLastReceivedIntent() {
            return mLastReceivedIntent;
        }

        public void clearReceivedIntents() {
            mLastReceivedIntent = null;
        }
    }

    private static class MockLocationListener implements LocationListener {
        private String mProvider;
        private int mStatus;
        private Location mLocation;
        private Object mStatusLock = new Object();
        private Object mLocationLock = new Object();
        private Object mLocationRequestLock = new Object();

        private boolean mHasCalledOnLocationChanged;

        private boolean mHasCalledOnProviderDisabled;

        private boolean mHasCalledOnProviderEnabled;

        private boolean mHasCalledOnStatusChanged;

        private boolean mHasCalledRequestLocation;

        public void reset(){
            mHasCalledOnLocationChanged = false;
            mHasCalledOnProviderDisabled = false;
            mHasCalledOnProviderEnabled = false;
            mHasCalledOnStatusChanged = false;
            mHasCalledRequestLocation = false;
            mProvider = null;
            mStatus = 0;
        }

        /**
         * Call to inform listener that location has been updates have been requested
         */
        public void setLocationRequested() {
            synchronized (mLocationRequestLock) {
                mHasCalledRequestLocation = true;
                mLocationRequestLock.notify();
            }
        }

        public boolean hasCalledLocationRequested(long timeout) throws InterruptedException {
            synchronized (mLocationRequestLock) {
                if (timeout > 0 && !mHasCalledRequestLocation) {
                    mLocationRequestLock.wait(timeout);
                }
            }
            return mHasCalledRequestLocation;
        }

        /**
         * Check whether onLocationChanged() has been called. Wait up to timeout milliseconds
         * for the callback.
         * @param timeout Maximum time to wait for the callback, 0 to return immediately.
         */
        public boolean hasCalledOnLocationChanged(long timeout) throws InterruptedException {
            synchronized (mLocationLock) {
                if (timeout > 0 && !mHasCalledOnLocationChanged) {
                    mLocationLock.wait(timeout);
                }
            }
            return mHasCalledOnLocationChanged;
        }

        public boolean hasCalledOnProviderDisabled() {
            return mHasCalledOnProviderDisabled;
        }

        public boolean hasCalledOnProviderEnabled() {
            return mHasCalledOnProviderEnabled;
        }

        public boolean hasCalledOnStatusChanged(long timeout) throws InterruptedException {
            synchronized(mStatusLock) {
                // wait(0) would wait forever
                if (timeout > 0 && !mHasCalledOnStatusChanged) {
                    mStatusLock.wait(timeout);
                }
            }
            return mHasCalledOnStatusChanged;
        }

        public void onLocationChanged(Location location) {
            mLocation = location;
            synchronized (mLocationLock) {
                mHasCalledOnLocationChanged = true;
                mLocationLock.notify();
            }
        }

        public void onProviderDisabled(String provider) {
            mHasCalledOnProviderDisabled = true;
        }

        public void onProviderEnabled(String provider) {
            mHasCalledOnProviderEnabled = true;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            mProvider = provider;
            mStatus = status;
            synchronized (mStatusLock) {
                mHasCalledOnStatusChanged = true;
                mStatusLock.notify();
            }
        }

        public String getProvider() {
            return mProvider;
        }

        public int getStatus() {
            return mStatus;
        }

        public Location getLocation() {
            return mLocation;
        }
    }

    private static class MockGpsStatusListener implements Listener {
        private boolean mHasCallOnGpsStatusChanged;

        public boolean hasCallOnGpsStatusChanged() {
            return mHasCallOnGpsStatusChanged;
        }

        public void reset(){
            mHasCallOnGpsStatusChanged = false;
        }

        public void onGpsStatusChanged(int event) {
            mHasCallOnGpsStatusChanged = true;
        }
    }
}
