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
import android.media.audiofx.EnvironmentalReverb;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.util.Log;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(EnvironmentalReverb.class)
public class EnvReverbTest extends AndroidTestCase {

    private String TAG = "EnvReverbTest";
    private final static int MILLIBEL_TOLERANCE = 100;            // +/-1dB
    private final static float DELAY_TOLERANCE = 1.05f;           // 5%
    private final static float RATIO_TOLERANCE = 1.05f;           // 5%

    private EnvironmentalReverb mReverb = null;
    private EnvironmentalReverb mReverb2 = null;
    private int mSession = -1;
    private boolean mHasControl = false;
    private boolean mIsEnabled = false;
    private int mChangedParameter = -1;
    private boolean mInitialized = false;
    private Looper mLooper = null;
    private final Object mLock = new Object();


    //-----------------------------------------------------------------
    // ENVIRONMENTAL REVERB TESTS:
    //----------------------------------

    //-----------------------------------------------------------------
    // 0 - constructor
    //----------------------------------

    //Test case 0.0: test constructor and release
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "EnvironmentalReverb",
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
        EnvironmentalReverb envReverb = null;
         try {
            envReverb = new EnvironmentalReverb(0, 0);
            assertNotNull("could not create EnvironmentalReverb", envReverb);
            try {
                assertTrue("invalid effect ID", (envReverb.getId() != 0));
            } catch (IllegalStateException e) {
                fail("EnvironmentalReverb not initialized");
            }
        } catch (IllegalArgumentException e) {
            fail("EnvironmentalReverb not found");
        } catch (UnsupportedOperationException e) {
            fail("Effect library not loaded");
        } finally {
            if (envReverb != null) {
                envReverb.release();
            }
        }
    }


    //-----------------------------------------------------------------
    // 1 - get/set parameters
    //----------------------------------

    //Test case 1.0: test room level and room HF level
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRoomLevel",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRoomLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRoomHFLevel",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRoomHFLevel",
            args = {}
        )
    })
    public void test1_0Room() throws Exception {
        getReverb(0);
        try {
            short level = mReverb.getRoomLevel();
            level = (short)((level == 0) ? -1000 : 0);
            mReverb.setRoomLevel(level);
            short level2 = mReverb.getRoomLevel();
            assertTrue("got incorrect room level",
                    (level2 > (level - MILLIBEL_TOLERANCE)) &&
                    (level2 < (level + MILLIBEL_TOLERANCE)));

            level = mReverb.getRoomHFLevel();
            level = (short)((level == 0) ? -1000 : 0);
            mReverb.setRoomHFLevel(level);
            level2 = mReverb.getRoomHFLevel();
            assertTrue("got incorrect room HF level",
                    (level2 > (level - MILLIBEL_TOLERANCE)) &&
                    (level2 < (level + MILLIBEL_TOLERANCE)));

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

    //Test case 1.1: test decay time and ratio
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDecayTime",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDecayTime",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDecayHFRatio",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDecayHFRatio",
            args = {}
        )
    })
    public void test1_1Decay() throws Exception {
        getReverb(0);
        try {
            int time = mReverb.getDecayTime();
            time = (time == 500) ? 1000 : 500;
            mReverb.setDecayTime(time);
            int time2 = mReverb.getDecayTime();
            assertTrue("got incorrect decay time",
                    ((float)time2 > (float)(time / DELAY_TOLERANCE)) &&
                    ((float)time2 < (float)(time * DELAY_TOLERANCE)));
            short ratio = mReverb.getDecayHFRatio();
            ratio = (short)((ratio == 500) ? 1000 : 500);
            mReverb.setDecayHFRatio(ratio);
            short ratio2 = mReverb.getDecayHFRatio();
            assertTrue("got incorrect decay HF ratio",
                    ((float)ratio2 > (float)(ratio / RATIO_TOLERANCE)) &&
                    ((float)ratio2 < (float)(ratio * RATIO_TOLERANCE)));

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


    //Test case 1.2: test reverb level and delay
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReverbLevel",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReverbLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReverbDelay",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReverbDelay",
            args = {}
        )
    })
    public void test1_2Reverb() throws Exception {
        getReverb(0);
        try {
            short level = mReverb.getReverbLevel();
            level = (short)((level == 0) ? -1000 : 0);
            mReverb.setReverbLevel(level);
            short level2 = mReverb.getReverbLevel();
            assertTrue("got incorrect reverb level",
                    (level2 > (level - MILLIBEL_TOLERANCE)) &&
                    (level2 < (level + MILLIBEL_TOLERANCE)));

// FIXME:uncomment actual test when early reflections are implemented in the reverb
//            int time = mReverb.getReverbDelay();
//             mReverb.setReverbDelay(time);
//            int time2 = mReverb.getReverbDelay();
//            assertTrue("got incorrect reverb delay",
//                    ((float)time2 > (float)(time / DELAY_TOLERANCE)) &&
//                    ((float)time2 < (float)(time * DELAY_TOLERANCE)));
            mReverb.setReverbDelay(0);
            int time2 = mReverb.getReverbDelay();
            assertEquals("got incorrect reverb delay", mReverb.getReverbDelay(), 0);
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

    //Test case 1.3: test early reflections level and delay
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReflectionsLevel",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReflectionsLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReflectionsDelay",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReflectionsDelay",
            args = {}
        )
    })
    public void test1_3Reflections() throws Exception {
        getReverb(0);
        try {
// FIXME:uncomment actual test when early reflections are implemented in the reverb
//            short level = mReverb.getReflectionsLevel();
//            level = (short)((level == 0) ? -1000 : 0);
//            mReverb.setReflectionsLevel(level);
//            short level2 = mReverb.getReflectionsLevel();
//            assertTrue("got incorrect reflections level",
//                    (level2 > (level - MILLIBEL_TOLERANCE)) &&
//                    (level2 < (level + MILLIBEL_TOLERANCE)));
//
//            int time = mReverb.getReflectionsDelay();
//            time = (time == 20) ? 0 : 20;
//            mReverb.setReflectionsDelay(time);
//            int time2 = mReverb.getReflectionsDelay();
//            assertTrue("got incorrect reflections delay",
//                    ((float)time2 > (float)(time / DELAY_TOLERANCE)) &&
//                    ((float)time2 < (float)(time * DELAY_TOLERANCE)));
            mReverb.setReflectionsLevel((short) 0);
            assertEquals("got incorrect reverb delay",
                    mReverb.getReflectionsLevel(), (short) 0);
            mReverb.setReflectionsDelay(0);
            assertEquals("got incorrect reverb delay",
                    mReverb.getReflectionsDelay(), 0);

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

    //Test case 1.4: test diffusion and density
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDiffusion",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDiffusion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDensity",
            args = {short.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDensity",
            args = {}
        )
    })
    public void test1_4DiffusionAndDensity() throws Exception {
        getReverb(0);
        try {
            short ratio = mReverb.getDiffusion();
            ratio = (short)((ratio == 500) ? 1000 : 500);
            mReverb.setDiffusion(ratio);
            short ratio2 = mReverb.getDiffusion();
            assertTrue("got incorrect diffusion",
                    ((float)ratio2 > (float)(ratio / RATIO_TOLERANCE)) &&
                    ((float)ratio2 < (float)(ratio * RATIO_TOLERANCE)));

            ratio = mReverb.getDensity();
            ratio = (short)((ratio == 500) ? 1000 : 500);
            mReverb.setDensity(ratio);
            ratio2 = mReverb.getDensity();
            assertTrue("got incorrect density",
                    ((float)ratio2 > (float)(ratio / RATIO_TOLERANCE)) &&
                    ((float)ratio2 < (float)(ratio * RATIO_TOLERANCE)));

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

    //Test case 1.5: test properties
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProperties",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProperties",
            args = {EnvironmentalReverb.Settings.class}
        )
    })
    public void test1_5Properties() throws Exception {
        getReverb(0);
        try {
            EnvironmentalReverb.Settings settings = mReverb.getProperties();
            String str = settings.toString();
            settings = new EnvironmentalReverb.Settings(str);
            short level = (short)((settings.roomLevel == 0) ? -1000 : 0);
            settings.roomLevel = level;
            mReverb.setProperties(settings);
            settings = mReverb.getProperties();
            assertTrue("setProperties failed",
                    (settings.roomLevel >= (level - MILLIBEL_TOLERANCE)) &&
                    (settings.roomLevel <= (level + MILLIBEL_TOLERANCE)));
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
            fail("setEnabled() processed after release()");
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
            args = {EnvironmentalReverb.OnParameterChangeListener.class}
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
                mReverb.setRoomLevel((short)0);
                mLock.wait(1000);
            } catch(Exception e) {
                Log.e(TAG, "Create second effect: wait was interrupted.");
            } finally {
                releaseReverb();
                terminateListenerLooper();
            }
        }
        assertEquals("parameter change not received",
                EnvironmentalReverb.PARAM_ROOM_LEVEL, mChangedParameter);
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
                mReverb = new EnvironmentalReverb(0, session);
                mSession = session;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "getReverb() EnvironmentalReverb not found exception: "+e);
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

                mReverb2 = new EnvironmentalReverb(0, 0);
                assertNotNull("could not create reverb2", mReverb2);

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
                    mReverb2.setParameterListener(new EnvironmentalReverb.OnParameterChangeListener() {
                        public void onParameterChange(EnvironmentalReverb effect,
                                int status, int param, int value)
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