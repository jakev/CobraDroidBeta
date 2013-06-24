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
import android.media.audiofx.PresetReverb;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.util.Log;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(PresetReverb.class)
public class PresetReverbTest extends AndroidTestCase {

    private String TAG = "PresetReverbTest";
    private final static short FIRST_PRESET = PresetReverb.PRESET_NONE;
    private final static short LAST_PRESET = PresetReverb.PRESET_PLATE;
    private PresetReverb mReverb = null;
    private PresetReverb mReverb2 = null;
    private int mSession = -1;
    private boolean mHasControl = false;
    private boolean mIsEnabled = false;
    private int mChangedParameter = -1;
    private boolean mInitialized = false;
    private Looper mLooper = null;
    private final Object mLock = new Object();


    //-----------------------------------------------------------------
    // PRESET REVERB TESTS:
    //----------------------------------

    //-----------------------------------------------------------------
    // 0 - constructor
    //----------------------------------

    //Test case 0.0: test constructor and release
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PresetReverb",
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
        PresetReverb reverb = null;
        try {
            reverb = new PresetReverb(0, 0);
            assertNotNull("could not create PresetReverb", reverb);
            try {
                assertTrue("invalid effect ID", (reverb.getId() != 0));
            } catch (IllegalStateException e) {
                fail("PresetReverb not initialized");
            }
        } catch (IllegalArgumentException e) {
            fail("PresetReverb not found");
        } catch (UnsupportedOperationException e) {
            fail("Effect library not loaded");
        } finally {
            if (reverb != null) {
                reverb.release();
            }
        }
    }

    //-----------------------------------------------------------------
    // 1 - get/set parameters
    //----------------------------------

    //Test case 1.0: test presets
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPreset",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreset",
            args = {}
        )
    })
    public void test1_0Presets() throws Exception {
        getReverb(0);
        try {
            for (short preset = FIRST_PRESET;
                 preset <= LAST_PRESET;
                 preset++) {
                mReverb.setPreset(preset);
                assertEquals("got incorrect preset", preset, mReverb.getPreset());
            }
        } catch (IllegalArgumentException e) {
            fail("Bad parameter value");
        } catch (UnsupportedOperationException e) {
            fail("get parameter() rejected");
        } catch (IllegalStateException e) {
            fail("get parameter() called in wrong state");
        } finally {
            releaseReverb();
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
            args = {PresetReverb.Settings.class}
        )
    })
    public void test1_1Properties() throws Exception {
        getReverb(0);
        try {
            PresetReverb.Settings settings = mReverb.getProperties();
            String str = settings.toString();
            settings = new PresetReverb.Settings(str);
            short preset = (settings.preset == PresetReverb.PRESET_SMALLROOM) ?
                            PresetReverb.PRESET_MEDIUMROOM : PresetReverb.PRESET_SMALLROOM;
            settings.preset = preset;
            mReverb.setProperties(settings);
            settings = mReverb.getProperties();
            assertEquals("setProperties failed", settings.preset, preset);
        } catch (IllegalArgumentException e) {
            fail("Bad parameter value");
        } catch (UnsupportedOperationException e) {
            fail("get parameter() rejected");
        } catch (IllegalStateException e) {
            fail("get parameter() called in wrong state");
        } finally {
            releaseReverb();
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
        getReverb(0);
        try {
            mReverb.setEnabled(true);
            assertTrue("invalid state from getEnabled", mReverb.getEnabled());
            mReverb.setEnabled(false);
            assertFalse("invalid state to getEnabled", mReverb.getEnabled());
        } catch (IllegalStateException e) {
            fail("setEnabled() in wrong state");
        } finally {
            releaseReverb();
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
        getReverb(0);
        mReverb.release();
        try {
            mReverb.setEnabled(true);
        } catch (IllegalStateException e) {
            // test passed
        } finally {
            releaseReverb();
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
                getReverb(0);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseReverb();
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
        mReverb2.setEnabled(true);
        mIsEnabled = true;
        getReverb(0);
        synchronized(mLock) {
            try {
                mReverb.setEnabled(false);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseReverb();
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
            args = {PresetReverb.OnParameterChangeListener.class}
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
        getReverb(0);
        synchronized(mLock) {
            try {
                mChangedParameter = -1;
                mReverb.setPreset(PresetReverb.PRESET_SMALLROOM);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseReverb();
                terminateListenerLooper();
            }
        }
        assertEquals("parameter change not received",
                PresetReverb.PARAM_PRESET, mChangedParameter);
    }

    //-----------------------------------------------------------------
    // private methods
    //----------------------------------

    private void getReverb(int session) {
         if (mReverb == null || session != mSession) {
             if (session != mSession && mReverb != null) {
                 mReverb.release();
                 mReverb = null;
             }
             try {
                mReverb = new PresetReverb(0, session);
                mSession = session;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "getReverb() PresetReverb not found exception: "+e);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "getReverb() Effect library not loaded exception: "+e);
            }
         }
         assertNotNull("could not create mReverb", mReverb);
    }

    private void releaseReverb() {
        if (mReverb != null) {
            mReverb.release();
            mReverb = null;
        }
    }

    // Initializes the reverb listener looper
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

                mReverb2 = new PresetReverb(0, 0);
                assertNotNull("could not create Reverb2", mReverb2);

                if (mControl) {
                    mReverb2.setControlStatusListener(
                            new AudioEffect.OnControlStatusChangeListener() {
                        public void onControlStatusChange(
                                AudioEffect effect, boolean controlGranted) {
                            synchronized(mLock) {
                                if (effect == mReverb2) {
                                    mHasControl = controlGranted;
                                    mLock.notify();
                                }
                            }
                        }
                    });
                }
                if (mEnable) {
                    mReverb2.setEnableStatusListener(
                            new AudioEffect.OnEnableStatusChangeListener() {
                        public void onEnableStatusChange(AudioEffect effect, boolean enabled) {
                            synchronized(mLock) {
                                if (effect == mReverb2) {
                                    mIsEnabled = enabled;
                                    mLock.notify();
                                }
                            }
                        }
                    });
                }
                if (mParameter) {
                    mReverb2.setParameterListener(new PresetReverb.OnParameterChangeListener() {
                        public void onParameterChange(PresetReverb effect,
                                int status, int param, short value)
                        {
                            synchronized(mLock) {
                                if (effect == mReverb2) {
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
        if (mReverb2 != null) {
            mReverb2.release();
            mReverb2 = null;
        }
        if (mLooper != null) {
            mLooper.quit();
            mLooper = null;
        }
    }

}