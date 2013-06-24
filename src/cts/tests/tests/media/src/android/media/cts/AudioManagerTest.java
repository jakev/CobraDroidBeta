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

package android.media.cts;

import static android.media.AudioManager.ADJUST_LOWER;
import static android.media.AudioManager.ADJUST_RAISE;
import static android.media.AudioManager.ADJUST_SAME;
import static android.media.AudioManager.FLAG_ALLOW_RINGER_MODES;
import static android.media.AudioManager.FLAG_SHOW_UI;
import static android.media.AudioManager.MODE_IN_CALL;
import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.USE_DEFAULT_STREAM_TYPE;
import static android.media.AudioManager.VIBRATE_SETTING_OFF;
import static android.media.AudioManager.VIBRATE_SETTING_ON;
import static android.media.AudioManager.VIBRATE_SETTING_ONLY_SILENT;
import static android.media.AudioManager.VIBRATE_TYPE_NOTIFICATION;
import static android.media.AudioManager.VIBRATE_TYPE_RINGER;
import static android.provider.Settings.System.SOUND_EFFECTS_ENABLED;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.cts.CTSResult;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.test.AndroidTestCase;
import android.view.SoundEffectConstants;

@TestTargetClass(AudioManager.class)
public class AudioManagerTest extends AndroidTestCase implements CTSResult {

    private final static int MP3_TO_PLAY = R.raw.testmp3;
    private final static long TIME_TO_PLAY = 2000;
    private AudioManager mAudioManager;
    private int mResultCode;
    private Sync mSync = new Sync();

    private static class Sync {
        private boolean notified;

        synchronized void notifyResult() {
            notified = true;
            notify();
        }

        synchronized void waitForResult() throws Exception {
            if (!notified) {
                wait();
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMicrophoneMute",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMicrophoneMute",
            args = {}
        )
    })
    public void testMicrophoneMute() throws Exception {
        mAudioManager.setMicrophoneMute(true);
        assertTrue(mAudioManager.isMicrophoneMute());
        mAudioManager.setMicrophoneMute(false);
        assertFalse(mAudioManager.isMicrophoneMute());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unloadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "playSoundEffect",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "playSoundEffect",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testSoundEffects() throws Exception {
        // set relative setting
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        Settings.System.putInt(mContext.getContentResolver(), SOUND_EFFECTS_ENABLED, 1);

        // should hear sound after loadSoundEffects() called.
        mAudioManager.loadSoundEffects();
        Thread.sleep(TIME_TO_PLAY);
        float volume = 13;
        mAudioManager.playSoundEffect(SoundEffectConstants.CLICK);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);

        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT, volume);

        // won't hear sound after unloadSoundEffects() called();
        mAudioManager.unloadSoundEffects();
        mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);

        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT, volume);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMusicActive",
            args = {}
        )
    })
    public void testMusicActive() throws Exception {
        MediaPlayer mp = MediaPlayer.create(mContext, MP3_TO_PLAY);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
        Thread.sleep(TIME_TO_PLAY);
        assertTrue(mAudioManager.isMusicActive());
        Thread.sleep(TIME_TO_PLAY);
        mp.stop();
        mp.release();
        Thread.sleep(TIME_TO_PLAY);
        assertFalse(mAudioManager.isMusicActive());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMode",
            args = {}
        )
    })
    public void testAccessMode() throws Exception {
        mAudioManager.setMode(MODE_RINGTONE);
        assertEquals(MODE_RINGTONE, mAudioManager.getMode());
        mAudioManager.setMode(MODE_IN_CALL);
        assertEquals(MODE_IN_CALL, mAudioManager.getMode());
        mAudioManager.setMode(MODE_NORMAL);
        assertEquals(MODE_NORMAL, mAudioManager.getMode());
    }

    @SuppressWarnings("deprecation")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBluetoothA2dpOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBluetoothScoOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRouting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBluetoothA2dpOn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBluetoothScoOn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "setRouting",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSpeakerphoneOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSpeakerphoneOn",
            args = {}
        )
    })
    @ToBeFixed(bug="1713090", explanation="setRouting() has not only been deprecated, but is no"
        + " longer having any effect.")
    public void testRouting() throws Exception {
        // setBluetoothA2dpOn is a no-op, and getRouting should always return -1
        // AudioManager.MODE_CURRENT
        boolean oldA2DP = mAudioManager.isBluetoothA2dpOn();
        mAudioManager.setBluetoothA2dpOn(true);
        assertEquals(oldA2DP , mAudioManager.isBluetoothA2dpOn());
        mAudioManager.setBluetoothA2dpOn(false);
        assertEquals(oldA2DP , mAudioManager.isBluetoothA2dpOn());

        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));

        mAudioManager.setBluetoothScoOn(true);
        assertTrue(mAudioManager.isBluetoothScoOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));

        mAudioManager.setBluetoothScoOn(false);
        assertFalse(mAudioManager.isBluetoothScoOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));

        mAudioManager.setSpeakerphoneOn(true);
        assertTrue(mAudioManager.isSpeakerphoneOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
        mAudioManager.setSpeakerphoneOn(false);
        assertFalse(mAudioManager.isSpeakerphoneOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "shouldVibrate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVibrateSetting",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVibrateSetting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testVibrateNotification() throws Exception {
        // VIBRATE_SETTING_ON
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        // VIBRATE_SETTING_OFF
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        // VIBRATE_SETTING_ONLY_SILENT
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));

        // VIBRATE_TYPE_NOTIFICATION
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "shouldVibrate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVibrateSetting",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVibrateSetting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testVibrateRinger() throws Exception {
        // VIBRATE_TYPE_RINGER
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        // VIBRATE_SETTING_OFF
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        // Note: as of Froyo, if VIBRATE_TYPE_RINGER is set to OFF, it will
        // not vibrate, even in RINGER_MODE_VIBRATE. This allows users to
        // disable the vibration for incoming calls only.
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        // VIBRATE_SETTING_ONLY_SILENT
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));

        // VIBRATE_TYPE_NOTIFICATION
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unloadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingerMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testAccessRingMode() throws Exception {
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());

        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertEquals(RINGER_MODE_SILENT, mAudioManager.getRingerMode());

        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamMaxVolume",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamVolume",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
             method = "adjustStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "adjustSuggestedStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "adjustVolume",
            args = {int.class, int.class}
        )
    })
    public void testVolume() throws Exception {
        int[] streams = { AudioManager.STREAM_ALARM,
                          AudioManager.STREAM_MUSIC,
                          AudioManager.STREAM_SYSTEM,
                          AudioManager.STREAM_VOICE_CALL,
                          AudioManager.STREAM_RING };

        mAudioManager.adjustVolume(ADJUST_RAISE, 100);
        mAudioManager.adjustSuggestedStreamVolume(
                ADJUST_LOWER, USE_DEFAULT_STREAM_TYPE, FLAG_SHOW_UI);

        for (int i = 0; i < streams.length; i++) {
            int maxVolume = mAudioManager.getStreamMaxVolume(streams[i]);

            mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
            assertEquals(1, mAudioManager.getStreamVolume(streams[i]));

            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_SHOW_UI);
            assertEquals(maxVolume, mAudioManager.getStreamVolume(streams[i]));

            mAudioManager.adjustSuggestedStreamVolume(ADJUST_LOWER, streams[i], FLAG_SHOW_UI);
            assertEquals(maxVolume - 1, mAudioManager.getStreamVolume(streams[i]));

            // volume lower
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            for (int k = maxVolume; k > 0; k--) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(k - 1, mAudioManager.getStreamVolume(streams[i]));
            }

            mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
            assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
            mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
            assertEquals(1, mAudioManager.getStreamVolume(streams[i]));
            if (streams[i] == AudioManager.STREAM_RING) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(0, mAudioManager.getStreamVolume(streams[i]));
                // adjusting the volume to zero should result in either silent or vibrate mode
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE ||
                        mAudioManager.getRingerMode() == RINGER_MODE_SILENT);
                mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
                assertEquals(1, mAudioManager.getStreamVolume(streams[i]));
            } else {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(0, mAudioManager.getStreamVolume(streams[i]));
                // lowering the volume should NOT have changed the ringer mode
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
                // API quirk: volume must be decremented from 1 to get ringer mode change
                mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_ALLOW_RINGER_MODES);
                // lowering the volume should have changed the ringer mode
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE ||
                        mAudioManager.getRingerMode() == RINGER_MODE_SILENT);
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_ALLOW_RINGER_MODES);
                // adjusting the volume to zero should result in either silent or vibrate mode
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE ||
                        mAudioManager.getRingerMode() == RINGER_MODE_SILENT);
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_ALLOW_RINGER_MODES);
                // There are two possible ways the device may work. It may have a silent/vibrate
                // mode or it may have distinct silent and vibrate modes.
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_NORMAL ||
                        mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE);
                // Increase the volume one more time to get out of the vibrate mode which may
                // be separate from silent mode.
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_ALLOW_RINGER_MODES);
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
            }

            // volume raise
            mAudioManager.setStreamVolume(streams[i], 0, FLAG_SHOW_UI);
            for (int k = 0; k < maxVolume; k++) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_SHOW_UI);
                assertEquals(1 + k, mAudioManager.getStreamVolume(streams[i]));
            }

            // volume same
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            for (int k = 0; k < maxVolume; k++) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_SAME, FLAG_SHOW_UI);
                assertEquals(maxVolume, mAudioManager.getStreamVolume(streams[i]));
            }

            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
        }

        // adjust volume
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_MUSIC);
        mAudioManager.adjustVolume(ADJUST_RAISE, 100);

        MediaPlayer mp = MediaPlayer.create(mContext, MP3_TO_PLAY);
        mp.setAudioStreamType(STREAM_MUSIC);
        mp.setLooping(true);
        mp.start();
        Thread.sleep(TIME_TO_PLAY);
        assertTrue(mAudioManager.isMusicActive());

        // adjust volume as ADJUST_SAME
        for (int k = 0; k < maxVolume; k++) {
            mAudioManager.adjustVolume(ADJUST_SAME, FLAG_SHOW_UI);
            assertEquals(maxVolume, mAudioManager.getStreamVolume(STREAM_MUSIC));
        }

        // adjust volume as ADJUST_RAISE
        mAudioManager.setStreamVolume(STREAM_MUSIC, 1, FLAG_SHOW_UI);
        for (int k = 0; k < maxVolume - 1; k++) {
            mAudioManager.adjustVolume(ADJUST_RAISE, FLAG_SHOW_UI);
            assertEquals(2 + k, mAudioManager.getStreamVolume(STREAM_MUSIC));
        }

        // adjust volume as ADJUST_LOWER
        mAudioManager.setStreamVolume(STREAM_MUSIC, maxVolume, FLAG_SHOW_UI);
        maxVolume = mAudioManager.getStreamVolume(STREAM_MUSIC);

        mAudioManager.adjustVolume(ADJUST_LOWER, FLAG_SHOW_UI);
        assertEquals(maxVolume - 1, mAudioManager.getStreamVolume(STREAM_MUSIC));
        mp.stop();
        mp.release();
        Thread.sleep(TIME_TO_PLAY);
        assertFalse(mAudioManager.isMusicActive());
    }

    public void setResult(int resultCode) {
        mSync.notifyResult();
        mResultCode = resultCode;
    }
}
