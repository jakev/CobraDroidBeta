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

package android.os.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.content.Context;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@TestTargetClass(ParcelFileDescriptor.class)
public class ParcelFileDescriptorTest extends AndroidTestCase {
    private static final long DURATION = 100l;

    private TestThread mTestThread;

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ParcelFileDescriptor",
            args = {android.os.ParcelFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {java.io.File.class, int.class}
        )
    })
    public void testConstructorAndOpen() throws Exception {
        ParcelFileDescriptor tempFile = makeParcelFileDescriptor(getContext());

        ParcelFileDescriptor pfd = new ParcelFileDescriptor(tempFile);
        AutoCloseInputStream in = new AutoCloseInputStream(pfd);
        try {
            // read the data that was wrote previously
            assertEquals(0, in.read());
            assertEquals(1, in.read());
            assertEquals(2, in.read());
            assertEquals(3, in.read());
        } finally {
            in.close();
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "fromSocket",
        args = {java.net.Socket.class}
    )
    public void testFromSocket() throws Throwable {
        final int PORT = 12222;
        final int DATA = 1;

        mTestThread = new TestThread(new Runnable() {
            public void run() {
                try {
                    ServerSocket ss;
                    ss = new ServerSocket(PORT);
                    Socket sSocket = ss.accept();
                    OutputStream out = sSocket.getOutputStream();
                    out.write(DATA);
                    Thread.sleep(DURATION);
                    out.close();
                } catch (Exception e) {
                    mTestThread.setThrowable(e);
                }
            }
        });
        mTestThread.start();

        Thread.sleep(DURATION);
        Socket socket;
        socket = new Socket(InetAddress.getLocalHost(), PORT);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
        AutoCloseInputStream in = new AutoCloseInputStream(pfd);
        assertEquals(DATA, in.read());
        in.close();
        socket.close();
        pfd.close();

        mTestThread.joinAndCheck(DURATION * 2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(new Socket());
        assertNotNull(pfd.toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {
        ParcelFileDescriptor pf = makeParcelFileDescriptor(getContext());

        Parcel pl = Parcel.obtain();
        pf.writeToParcel(pl, ParcelFileDescriptor.PARCELABLE_WRITE_RETURN_VALUE);
        pl.setDataPosition(0);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.CREATOR.createFromParcel(pl);
        AutoCloseInputStream in = new AutoCloseInputStream(pfd);
        try {
            // read the data that was wrote previously
            assertEquals(0, in.read());
            assertEquals(1, in.read());
            assertEquals(2, in.read());
            assertEquals(3, in.read());
        } finally {
            in.close();
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "close",
        args = {}
    )
    public void testClose() throws Exception {
        ParcelFileDescriptor pf = makeParcelFileDescriptor(getContext());
        AutoCloseInputStream in1 = new AutoCloseInputStream(pf);
        try {
            assertEquals(0, in1.read());
        } finally {
            in1.close();
        }

        pf.close();

        AutoCloseInputStream in2 = new AutoCloseInputStream(pf);
        try {
            assertEquals(0, in2.read());
            fail("Failed to throw exception.");
        } catch (Exception e) {
            // expected
        } finally {
            in2.close();
        }
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "getStatSize",
        args = {}
    )
    @ToBeFixed(bug="1695243", explanation="getStatSize() will return -1 if the fd is not a file,"
            + " but here it will throw IllegalArgumentException, it's not the same with javadoc.")
    public void testGetStatSize() throws Exception {
        ParcelFileDescriptor pf = makeParcelFileDescriptor(getContext());
        assertTrue(pf.getStatSize() >= 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: getFileDescriptor",
        method = "getFileDescriptor",
        args = {}
    )
    public void testGetFileDescriptor() {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(new Socket());
        assertNotNull(pfd.getFileDescriptor());

        ParcelFileDescriptor p = new ParcelFileDescriptor(pfd);
        assertSame(pfd.getFileDescriptor(), p.getFileDescriptor());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: describeContents",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(new Socket());
        assertTrue((Parcelable.CONTENTS_FILE_DESCRIPTOR & pfd.describeContents()) != 0);
    }

    static ParcelFileDescriptor makeParcelFileDescriptor(Context con) throws Exception {
        final String fileName = "testParcelFileDescriptor";

        FileOutputStream fout = null;

        fout = con.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);

        try {
            fout.write(new byte[] { 0x0, 0x1, 0x2, 0x3 });
        } finally {
            fout.close();
        }

        File dir = con.getFilesDir();
        File file = new File(dir, fileName);
        ParcelFileDescriptor pf = null;

        pf = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);

        return pf;
    }
}
