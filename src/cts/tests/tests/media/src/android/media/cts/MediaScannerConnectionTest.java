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

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.IBinder;
import android.test.AndroidTestCase;
import android.view.animation.cts.DelayedCheck;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@TestTargetClass(MediaScannerConnection.class)
public class MediaScannerConnectionTest extends AndroidTestCase {

    private static final String MEDIA_TYPE = "audio/mpeg";
    private File mMediaFile;
    private static final int TIME_OUT = 2000;
    private MockMediaScannerConnection mMediaScannerConnection;
    private MockMediaScannerConnectionClient mMediaScannerConnectionClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // prepare the media file.
        InputStream in = null;
        FileOutputStream fOut = null;
        String fileName = "test" + System.currentTimeMillis();
        try {
            fOut = getContext().openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            in = getContext().getResources().openRawResource(R.raw.testmp3);
            byte[] bs = new byte[1024];
            int size = in.read(bs);
            while (size != -1) {
                fOut.write(bs, 0, size);
                size = in.read(bs);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fOut != null) {
                fOut.flush();
                fOut.close();
            }
        }
        File dir = getContext().getFilesDir();
        mMediaFile = new File(dir, fileName);
        assertTrue(mMediaFile.exists());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mMediaFile != null) {
            mMediaFile.delete();
        }
        if (mMediaScannerConnection != null) {
            mMediaScannerConnection.disconnect();
            mMediaScannerConnection = null;
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "MediaScannerConnection",
            args = {Context.class, MediaScannerConnectionClient.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "connect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "disconnect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isConnected",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onServiceConnected",
            args = {ComponentName.class, IBinder.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onServiceDisconnected",
            args = {ComponentName.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "scanFile",
            args = {String.class, String.class}
        )
    })
    @ToBeFixed(bug = "1567087", explanation = "onServiceDisconnected is not called")
    public void testMediaScannerConnection() throws InterruptedException {
        mMediaScannerConnectionClient = new MockMediaScannerConnectionClient();
        mMediaScannerConnection = new MockMediaScannerConnection(getContext(),
                                    mMediaScannerConnectionClient);

        assertFalse(mMediaScannerConnection.isConnected());

        // test connect and disconnect.
        mMediaScannerConnection.connect();
        checkConnectionState(true);

        assertTrue(mMediaScannerConnection.mIsOnServiceConnectedCalled);
        mMediaScannerConnection.disconnect();

        checkConnectionState(false);

        // FIXME: onServiceDisconnected is not called.
        assertFalse(mMediaScannerConnection.mIsOnServiceDisconnectedCalled);
        mMediaScannerConnection.connect();

        checkConnectionState(true);

        mMediaScannerConnection.scanFile(mMediaFile.getAbsolutePath(), MEDIA_TYPE);

        checkMediaScannerConnection();

        assertEquals(mMediaFile.getAbsolutePath(), mMediaScannerConnectionClient.mediaPath);
        assertNotNull(mMediaScannerConnectionClient.mediaUri);
    }

    private void checkMediaScannerConnection() {
        new DelayedCheck(TIME_OUT) {
            protected boolean check() {
                return mMediaScannerConnectionClient.isOnMediaScannerConnectedCalled;
            }
        }.run();
        new DelayedCheck(TIME_OUT) {
            protected boolean check() {
                return mMediaScannerConnectionClient.mediaPath != null;
            }
        }.run();
    }

    private void checkConnectionState(final boolean expected) {
        new DelayedCheck(TIME_OUT) {
            protected boolean check() {
                return mMediaScannerConnection.isConnected() == expected;
            }
        }.run();
    }

    class MockMediaScannerConnection extends MediaScannerConnection {

        public boolean mIsOnServiceConnectedCalled;
        public boolean mIsOnServiceDisconnectedCalled;
        public MockMediaScannerConnection(Context context, MediaScannerConnectionClient client) {
            super(context, client);
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            super.onServiceConnected(className, service);
            mIsOnServiceConnectedCalled = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            super.onServiceDisconnected(className);
            mIsOnServiceDisconnectedCalled = true;
            // this is not called.
        }
    }

    class MockMediaScannerConnectionClient implements MediaScannerConnectionClient {

        public boolean isOnMediaScannerConnectedCalled;
        public String mediaPath;
        public Uri mediaUri;
        public void onMediaScannerConnected() {
            isOnMediaScannerConnectedCalled = true;
        }

        public void onScanCompleted(String path, Uri uri) {
            mediaPath = path;
            if (uri != null) {
                mediaUri = uri;
            }
        }

    }

}
