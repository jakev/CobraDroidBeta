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

package android.media.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.provider.Settings;
import android.test.AndroidTestCase;

@TestTargetClass(Ringtone.class)
public class RingtoneTest extends AndroidTestCase {

    private Context mContext;
    private Ringtone mRingtone;
    private AudioManager mAudioManager;
    private int mOriginalVolume;
    private int mOriginalRingerMode;
    private int mOriginalStreamType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRingtone = RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_RINGTONE_URI);
        // backup ringer settings
        mOriginalRingerMode = mAudioManager.getRingerMode();
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mOriginalStreamType = mRingtone.getStreamType();
        // set ringer to a reasonable volume
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume / 2,
                AudioManager.FLAG_ALLOW_RINGER_MODES);
    }

    @Override
    protected void tearDown() throws Exception {
        // restore original settings
        if (mRingtone != null) {
            if (mRingtone.isPlaying()) mRingtone.stop();
            mRingtone.setStreamType(mOriginalStreamType);
        }
        if (mAudioManager != null) {
            mAudioManager.setRingerMode(mOriginalRingerMode);
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mOriginalVolume,
                    AudioManager.FLAG_ALLOW_RINGER_MODES);
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPlaying",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {Context.class}
        )
    })
    public void testRingtone() {

        assertNotNull(mRingtone.getTitle(mContext));
        assertTrue(mOriginalStreamType >= 0);

        mRingtone.setStreamType(AudioManager.STREAM_MUSIC);
        assertEquals(AudioManager.STREAM_MUSIC, mRingtone.getStreamType());
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        assertEquals(AudioManager.STREAM_ALARM, mRingtone.getStreamType());

        mRingtone.play();
        assertTrue(mRingtone.isPlaying());
        mRingtone.stop();
        assertFalse(mRingtone.isPlaying());
    }
}
