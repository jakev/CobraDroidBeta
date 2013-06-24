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

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.List;

@TestTargetClass(CamcorderProfile.class)
public class CamcorderProfileTest extends AndroidTestCase {

    private static final String TAG = "CamcorderProfileTest";

    private void checkProfile(CamcorderProfile profile) {
        Log.v(TAG, String.format("profile: duration=%d, quality=%d, " +
            "fileFormat=%d, videoCodec=%d, videoBitRate=%d, videoFrameRate=%d, " +
            "videoFrameWidth=%d, videoFrameHeight=%d, audioCodec=%d, " +
            "audioBitRate=%d, audioSampleRate=%d, audioChannels=%d",
            profile.duration,
            profile.quality,
            profile.fileFormat,
            profile.videoCodec,
            profile.videoBitRate,
            profile.videoFrameRate,
            profile.videoFrameWidth,
            profile.videoFrameHeight,
            profile.audioCodec,
            profile.audioBitRate,
            profile.audioSampleRate,
            profile.audioChannels));
        assertTrue(profile.duration > 0);
        assertTrue(profile.quality == CamcorderProfile.QUALITY_LOW ||
                   profile.quality == CamcorderProfile.QUALITY_HIGH);
        assertTrue(profile.videoBitRate > 0);
        assertTrue(profile.videoFrameRate > 0);
        assertTrue(profile.videoFrameWidth > 0);
        assertTrue(profile.videoFrameHeight > 0);
        assertTrue(profile.audioBitRate > 0);
        assertTrue(profile.audioSampleRate > 0);
        assertTrue(profile.audioChannels > 0);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class}
        )
    })
    public void testGet() {
        CamcorderProfile lowProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        CamcorderProfile highProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        checkProfile(lowProfile);
        checkProfile(highProfile);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {int.class, int.class}
        )
    })
    public void testGetWithId() {
        int nCamera = Camera.getNumberOfCameras();
        for (int id = 0; id < nCamera; id++) {
            CamcorderProfile lowProfile = CamcorderProfile.get(id,
                    CamcorderProfile.QUALITY_LOW);
            CamcorderProfile highProfile = CamcorderProfile.get(id,
                    CamcorderProfile.QUALITY_HIGH);
            checkProfile(lowProfile);
            checkProfile(highProfile);
        }
    }
}
