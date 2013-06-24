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

package android.media.cts;

import android.media.audiofx.AudioEffect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.util.Log;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(BassBoost.class)
public class BassBoostTest extends AndroidTestCase {

    private String TAG = "BassBoostTest";
    private final static short TEST_STRENGTH = 500;
    private final static short TEST_STRENGTH2 = 1000;
    private final static float STRENGTH_TOLERANCE = 1.1f;  // 10%

    private BassBoost mBassBoost = null;
    private BassBoost mBassBoost2 = null;
    private int mSession = -1;
    private boolean mHasControl = false;
    private boolean mIsEnabled = false;
    private int mChangedParameter = -1;
    private boolean mInitialized = false;
    private Looper mLooper = null;
    private final Object mLock = new Object();

    //-----------------------------------------------------------------
    // BASS BOOST TESTS:
    //----------------------------------

    //-----------------------------------------------------------------
    // 0 - constructor
    //----------------------------------

    //Test case 0.0: test constructor and release
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BassBoost",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        )
    })
    public void test0_0ConstructorAndRelease() throws Exception {
        BassBoost eq = null;
        try {
            eq = new BassBoost(0, 0);
            assertNotNull("could not create BassBoost", eq);
            try {
                assertTrue("invalid effect ID", (eq.getId() != 0));
            } catch (IllegalStateException e) {
                fail("BassBoost not initialized");
            }
            // test passed
        } catch (IllegalArgumentException e) {
            fail("BassBoost not found");
        } catch (UnsupportedOperationException e) {
            fail("Effect library not loaded");
        } finally {
            if (eq != null) {
                eq.release();
            }
        }
    }


    //-----------------------------------------------------------------
    // 1 - get/set parameters
    //----------------------------------

    //Test case 1.0: test strength
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrengthSupported",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrength",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRoundedStrength",
            args = {}
        )
    })
    public void test1_0Strength() throws Exception {
        getBassBoost(0);
        try {
            if (mBassBoost.getStrengthSupported()) {
                short strength = mBassBoost.getRoundedStrength();
                strength = (strength == TEST_STRENGTH) ? TEST_STRENGTH2 : TEST_STRENGTH;
                mBassBoost.setStrength((short)strength);
                short strength2 = mBassBoost.getRoundedStrength();
                // allow STRENGTH_TOLERANCE difference between set strength and rounded strength
                assertTrue("got incorrect strength",
                        ((float)strength2 > (float)strength / STRENGTH_TOLERANCE) &&
                        ((float)strength2 < (float)strength * STRENGTH_TOLERANCE));
            } else {
                short strength = mBassBoost.getRoundedStrength();
                assertTrue("got incorrect strength", strength >= 0 && strength <= 1000);
            }
            // test passed
        } catch (IllegalArgumentException e) {
            fail("Bad parameter value");
        } catch (UnsupportedOperationException e) {
            fail("get parameter() rejected");
        } catch (IllegalStateException e) {
            fail("get parameter() called in wrong state");
        } finally {
            releaseBassBoost();
        }
    }

    //Test case 1.1: test properties
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProperties",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProperties",
            args = {BassBoost.Settings.class}
        )
    })
    public void test1_1Properties() throws Exception {
        getBassBoost(0);
        try {
            BassBoost.Settings settings = mBassBoost.getProperties();
            String str = settings.toString();
            settings = new BassBoost.Settings(str);

            short strength = settings.strength;
            if (mBassBoost.getStrengthSupported()) {
                strength = (strength == TEST_STRENGTH) ? TEST_STRENGTH2 : TEST_STRENGTH;
            }
            settings.strength = strength;
            mBassBoost.setProperties(settings);
            settings = mBassBoost.getProperties();

            if (mBassBoost.getStrengthSupported()) {
                // allow STRENGTH_TOLERANCE difference between set strength and rounded strength
                assertTrue("got incorrect strength",
                        ((float)settings.strength > (float)strength / STRENGTH_TOLERANCE) &&
                        ((float)settings.strength < (float)strength * STRENGTH_TOLERANCE));
            }
            // test passed
        } catch (IllegalArgumentException e) {
            fail("Bad parameter value");
        } catch (UnsupportedOperationException e) {
            fail("get parameter() rejected");
        } catch (IllegalStateException e) {
            fail("get parameter() called in wrong state");
        } finally {
            releaseBassBoost();
        }
    }

    //Test case 1.2: test setStrength() throws exception after release
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrength",
            args = {short.class}
        )
    })
    public void test1_2SetStrengthAfterRelease() throws Exception {
        getBassBoost(0);
        mBassBoost.release();
        try {
            mBassBoost.setStrength(TEST_STRENGTH);
            fail("setStrength() processed after release()");
        } catch (IllegalStateException e) {
            // test passed
        } finally {
            releaseBassBoost();
        }
    }

    //-----------------------------------------------------------------
    // 2 - Effect enable/disable
    //----------------------------------

    //Test case 2.0: test setEnabled() and getEnabled() in valid state
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEnabled",
            args = {}
        )
    })
    public void test2_0SetEnabledGetEnabled() throws Exception {
        getBassBoost(0);
        try {
            mBassBoost.setEnabled(true);
            assertTrue("invalid state from getEnabled", mBassBoost.getEnabled());
            mBassBoost.setEnabled(false);
            assertFalse("invalid state to getEnabled", mBassBoost.getEnabled());
            // test passed
        } catch (IllegalStateException e) {
            fail("setEnabled() in wrong state");
        } finally {
            releaseBassBoost();
        }
    }

    //Test case 2.1: test setEnabled() throws exception after release
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEnabled",
            args = {boolean.class}
        )
    })
    public void test2_1SetEnabledAfterRelease() throws Exception {
        getBassBoost(0);
        mBassBoost.release();
        try {
            mBassBoost.setEnabled(true);
            fail("setEnabled() processed after release()");
        } catch (IllegalStateException e) {
            // test passed
        } finally {
            releaseBassBoost();
        }
    }

    //-----------------------------------------------------------------
    // 3 priority and listeners
    //----------------------------------

    //Test case 3.0: test control status listener
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setControlStatusListener",
            args = {AudioEffect.OnControlStatusChangeListener.class}
        )
    })
    public void test3_0ControlStatusListener() throws Exception {
        mHasControl = true;
        createListenerLooper(true, false, false);
        synchronized(mLock) {
            try {
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Looper creation: wait was interrupted.");
            }
        }
        assertTrue(mInitialized);
        synchronized(mLock) {
            try {
                getBassBoost(0);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseBassBoost();
                terminateListenerLooper();
            }
        }
        assertFalse("effect control not lost by effect1", mHasControl);
    }

    //Test case 3.1: test enable status listener
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEnableStatusListener",
            args = {AudioEffect.OnEnableStatusChangeListener.class}
        )
    })
    public void test3_1EnableStatusListener() throws Exception {
        createListenerLooper(false, true, false);
        synchronized(mLock) {
            try {
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Looper creation: wait was interrupted.");
            }
        }
        assertTrue(mInitialized);
        mBassBoost2.setEnabled(true);
        mIsEnabled = true;
        getBassBoost(0);
        synchronized(mLock) {
            try {
                mBassBoost.setEnabled(false);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseBassBoost();
                terminateListenerLooper();
            }
        }
        assertFalse("enable status not updated", mIsEnabled);
    }

    //Test case 3.2: test parameter changed listener
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setParameterListener",
            args = {BassBoost.OnParameterChangeListener.class}
        )
    })
    public void test3_2ParameterChangedListener() throws Exception {
        createListenerLooper(false, false, true);
        synchronized(mLock) {
            try {
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Looper creation: wait was interrupted.");
            }
        }
        assertTrue(mInitialized);
        getBassBoost(0);
        synchronized(mLock) {
            try {
                mChangedParameter = -1;
                mBassBoost.setStrength(TEST_STRENGTH);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseBassBoost();
                terminateListenerLooper();
            }
        }
        assertEquals("parameter change not received",
                BassBoost.PARAM_STRENGTH, mChangedParameter);
    }

    //-----------------------------------------------------------------
    // private methods
    //----------------------------------

    private void getBassBoost(int session) {
         if (mBassBoost == null || session != mSession) {
             if (session != mSession && mBassBoost != null) {
                 mBassBoost.release();
                 mBassBoost = null;
             }
             try {
                mBassBoost = new BassBoost(0, session);
                mSession = session;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "getBassBoost() BassBoost not found exception: "+e);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "getBassBoost() Effect library not loaded exception: "+e);
            }
         }
         assertNotNull("could not create mBassBoost", mBassBoost);
    }

    private void releaseBassBoost() {
        if (mBassBoost != null) {
            mBassBoost.release();
            mBassBoost = null;
        }
    }

    // Initializes the bassboot listener looper
    class ListenerThread extends Thread {
        boolean mControl;
        boolean mEnable;
        boolean mParameter;

        public ListenerThread(boolean control, boolean enable, boolean parameter) {
            super();
            mControl = control;
            mEnable = enable;
            mParameter = parameter;
        }
    }

    private void createListenerLooper(boolean control, boolean enable, boolean parameter) {
        mInitialized = false;
        new ListenerThread(control, enable, parameter) {
            @Override
            public void run() {
                // Set up a looper
                Looper.prepare();

                // Save the looper so that we can terminate this thread
                // after we are done with it.
                mLooper = Looper.myLooper();

                mBassBoost2 = new BassBoost(0, 0);
                assertNotNull("could not create bassboot2", mBassBoost2);

                if (mControl) {
                    mBassBoost2.setControlStatusListener(
                            new AudioEffect.OnControlStatusChangeListener() {
                        public void onControlStatusChange(
                                AudioEffect effect, boolean controlGranted) {
                            synchronized(mLock) {
                                if (effect == mBassBoost2) {
                                    mHasControl = controlGranted;
                                    mLock.notify();
                                }
                            }
                        }
                    });
                }
                if (mEnable) {
                    mBassBoost2.setEnableStatusListener(
                            new AudioEffect.OnEnableStatusChangeListener() {
                        public void onEnableStatusChange(AudioEffect effect, boolean enabled) {
                            synchronized(mLock) {
                                if (effect == mBassBoost2) {
                                    mIsEnabled = enabled;
                                    mLock.notify();
                                }
                            }
                        }
                    });
                }
                if (mParameter) {
                    mBassBoost2.setParameterListener(new BassBoost.OnParameterChangeListener() {
                        public void onParameterChange(BassBoost effect, int status,
                                int param, short value)
                        {
                            synchronized(mLock) {
                                if (effect == mBassBoost2) {
                                    mChangedParameter = param;
                                    mLock.notify();
                                }
                            }
                        }
                    });
                }

                synchronized(mLock) {
                    mInitialized = true;
                    mLock.notify();
                }
                Looper.loop();  // Blocks forever until Looper.quit() is called.
            }
        }.start();
    }

    // Terminates the listener looper thread.
    private void terminateListenerLooper() {
        if (mBassBoost2 != null) {
            mBassBoost2.release();
            mBassBoost2 = null;
        }
        if (mLooper != null) {
            mLooper.quit();
            mLooper = null;
        }
    }

}