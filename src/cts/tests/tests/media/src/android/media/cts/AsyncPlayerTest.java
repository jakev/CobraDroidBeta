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

import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(AsyncPlayer.class)
public class AsyncPlayerTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AsyncPlayer",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {Context.class, Uri.class, boolean.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        )
    })
    public void testAsyncPlayer() throws Exception {
        final Uri PLAY_URI = Settings.System.DEFAULT_NOTIFICATION_URI;
        AsyncPlayer asyncPlayer = new AsyncPlayer(null);
        asyncPlayer.play(getContext(), PLAY_URI, true, AudioManager.STREAM_RING);
        final int PLAY_TIME = 3000;
        Thread.sleep(PLAY_TIME);
        asyncPlayer.stop();
    }

}
