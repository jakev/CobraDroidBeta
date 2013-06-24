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

package android.net.cts;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.test.AndroidTestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(LocalSocket.class)
public class LocalSocketTest extends AndroidTestCase{
    public final static String mSockAddr = "com.android.net.LocalSocketTest";

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "LocalSocket",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "connect",
            args = {android.net.LocalSocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "getAncillaryFileDescriptors",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "getFileDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "getInputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "getOutputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "getPeerCredentials",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "isConnected",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "setFileDescriptorsForSend",
            args = {java.io.FileDescriptor[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "shutdownInput",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test core functions of LocalSocket",
            method = "shutdownOutput",
            args = {}
        )
    })
    public void testLocalConnections() throws IOException{
        // create client and server socket
        LocalServerSocket localServerSocket = new LocalServerSocket(mSockAddr);
        LocalSocket clientSocket = new LocalSocket();

        // establish connection between client and server
        LocalSocketAddress locSockAddr = new LocalSocketAddress(mSockAddr);
        assertFalse(clientSocket.isConnected());
        clientSocket.connect(locSockAddr);
        assertTrue(clientSocket.isConnected());
        LocalSocket serverSocket = localServerSocket.accept();

        Credentials credent = clientSocket.getPeerCredentials();
        assertTrue(0 != credent.getPid());

        // send data from client to server
        OutputStream clientOutStream = clientSocket.getOutputStream();
        clientOutStream.write(12);
        InputStream serverInStream = serverSocket.getInputStream();
        assertEquals(12, serverInStream.read());

        //send data from server to client
        OutputStream serverOutStream = serverSocket.getOutputStream();
        serverOutStream.write(3);
        InputStream clientInStream = clientSocket.getInputStream();
        assertEquals(3, clientInStream.read());

        // Test sending and receiving file descriptors
        clientSocket.setFileDescriptorsForSend(new FileDescriptor[]{FileDescriptor.in});
        clientOutStream.write(32);
        assertEquals(32, serverInStream.read());

        FileDescriptor[] out = serverSocket.getAncillaryFileDescriptors();
        assertEquals(1, out.length);
        FileDescriptor fd = clientSocket.getFileDescriptor();
        assertTrue(fd.valid());

        //shutdown input stream of client
        clientSocket.shutdownInput();
        assertEquals(-1, clientInStream.read());

        //shutdown output stream of client
        clientSocket.shutdownOutput();
        try {
            clientOutStream.write(10);
            fail("testLocalSocket shouldn't come to here");
        } catch (IOException e) {
            // expected
        }

        //shutdown input stream of server
        serverSocket.shutdownInput();
        assertEquals(-1, serverInStream.read());

        //shutdown output stream of server
        serverSocket.shutdownOutput();
        try {
            serverOutStream.write(10);
            fail("testLocalSocket shouldn't come to here");
        } catch (IOException e) {
            // expected
        }

        //close client socket
        clientSocket.close();
        try {
            clientInStream.read();
            fail("testLocalSocket shouldn't come to here");
        } catch (IOException e) {
            // expected
        }

        //close server socket
        serverSocket.close();
        try {
            serverInStream.read();
            fail("testLocalSocket shouldn't come to here");
        } catch (IOException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "bind",
            args = {android.net.LocalSocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "connect",
            args = {android.net.LocalSocketAddress.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "getLocalSocketAddress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "getReceiveBufferSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "getRemoteSocketAddress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "getSendBufferSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "getSoTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "isBound",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "isClosed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "isInputShutdown",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "isOutputShutdown",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "setReceiveBufferSize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "setSendBufferSize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "setSoTimeout",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test secondary functions of LocalSocket",
            method = "toString",
            args = {}
        )
    })
    public void testAccessors() throws IOException{
        LocalSocket socket = new LocalSocket();
        LocalSocketAddress addr = new LocalSocketAddress("secondary");

        assertFalse(socket.isBound());
        socket.bind(addr);
        assertTrue(socket.isBound());
        assertEquals(addr, socket.getLocalSocketAddress());

        String str = socket.toString();
        assertTrue(str.contains("impl:android.net.LocalSocketImpl"));

        socket.setReceiveBufferSize(1999);
        assertEquals(1999 << 1, socket.getReceiveBufferSize());

        socket.setSendBufferSize(1998);
        assertEquals(1998 << 1, socket.getSendBufferSize());

        // Timeout is not support at present, so set is ignored
        socket.setSoTimeout(1996);
        assertEquals(0, socket.getSoTimeout());

        try {
            socket.getRemoteSocketAddress();
            fail("testLocalSocketSecondary shouldn't come to here");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            socket.isClosed();
            fail("testLocalSocketSecondary shouldn't come to here");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            socket.isInputShutdown();
            fail("testLocalSocketSecondary shouldn't come to here");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            socket.isOutputShutdown();
            fail("testLocalSocketSecondary shouldn't come to here");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            socket.connect(addr, 2005);
            fail("testLocalSocketSecondary shouldn't come to here");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
