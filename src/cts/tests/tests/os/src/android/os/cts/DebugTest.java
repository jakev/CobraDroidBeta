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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Debug;
import android.test.AndroidTestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;
import dalvik.system.VMDebug;

@TestTargetClass(Debug.class)
public class DebugTest extends AndroidTestCase {
    private boolean mHasNofify;

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Debug.stopAllocCounting();
        Debug.resetAllCounts();
    }

    
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "changeDebugPort",
        args = {int.class}
    )
    public void testChangeDebugPort() {
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "enableEmulatorTraceOutput",
        args = {}
    )
    public void testEnableEmulatorTraceOutput() {
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "isDebuggerConnected",
        args = {}
    )
    public void testIsDebuggerConnected() {
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "printLoadedClasses",
        args = {int.class}
    )
    public void testPrintLoadedClasses() {
        Debug.printLoadedClasses(Debug.SHOW_FULL_DETAIL);
        Debug.printLoadedClasses(Debug.SHOW_CLASSLOADER);
        Debug.printLoadedClasses(Debug.SHOW_INITIALIZED);
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "setAllocationLimit",
        args = {int.class}
    )
    public void testSetAllocationLimit() {
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "setGlobalAllocationLimit",
        args = {int.class}
    )
    public void testSetGlobalAllocationLimit() {
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startMethodTracing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startMethodTracing",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startMethodTracing",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startMethodTracing",
            args = {java.lang.String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopMethodTracing",
            args = {}
        )
    })
    public void testStartMethodTracing() throws InterruptedException {
        final long debugTime = 3000;
        final String traceName = getFileName();

        final int bufSize = 1024 * 1024 * 2;
        final int debug_flag = VMDebug.TRACE_COUNT_ALLOCS;

        Debug.startMethodTracing();
        Thread.sleep(debugTime);
        Debug.stopMethodTracing();

        Debug.startMethodTracing(traceName);
        Thread.sleep(debugTime);
        Debug.stopMethodTracing();

        Debug.startMethodTracing(traceName, bufSize);
        Thread.sleep(debugTime);
        Debug.stopMethodTracing();

        Debug.startMethodTracing(traceName, bufSize, debug_flag);
        Thread.sleep(debugTime);
        Debug.stopMethodTracing();
    }

    private String getFileName() {
        File dir = getContext().getFilesDir();
        File file = new File(dir, "debug.trace");
        return file.getAbsolutePath();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNativeTracing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopNativeTracing",
            args = {}
        )
    })
    public void testStartNativeTracing() {
        Debug.startNativeTracing();

        Debug.stopNativeTracing();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "threadCpuTimeNanos",
        args = {}
    )
    public void testThreadCpuTimeNanos() throws InterruptedException {
        final int TIME = 1000;
        if (Debug.threadCpuTimeNanos() != -1) {
            long currentCupTimeNanos = Debug.threadCpuTimeNanos();
            mHasNofify = false;
            final Object lock = new Object();
            TestThread t = new TestThread(new Runnable() {
                public void run() {
                    long time = System.currentTimeMillis();
                    while (true) {
                        if (System.currentTimeMillis() - time > TIME) {
                            break;
                        }
                        Math.random();
                        if (System.currentTimeMillis() - time > TIME / 2 && !mHasNofify) {
                            synchronized (lock) {
                                mHasNofify = true;
                                lock.notify();
                            }
                            break;
                        }
                    }
                }
            });
            t.start();
            synchronized (lock) {
                if (!mHasNofify) {
                    lock.wait();
                }
            }
            assertTrue(Debug.threadCpuTimeNanos() > currentCupTimeNanos);
            Thread.sleep(TIME / 2);
        }
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "waitForDebugger",
        args = {}
    )
    @ToBeFixed(bug = "1419964", explanation = "This API leads to enter into a infinite cycle " +
         "which is always waiting a debugger. However the debugger waiting could not be cancled.")
    public void testWaitForDebugger() {
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "waitingForDebugger",
        args = {}
    )
    public void testWaitingForDebugger() {
        assertFalse(Debug.waitingForDebugger());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startAllocCounting",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopAllocCounting",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMemoryInfo",
            args = {android.os.Debug.MemoryInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalExternalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalExternalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalExternalFreedCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalExternalFreedSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalFreedCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalFreedSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGlobalGcInvocationCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreadAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreadAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreadExternalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreadExternalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreadGcInvocationCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLoadedClassCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNativeHeapAllocatedSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNativeHeapFreeSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNativeHeapSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBinderDeathObjectCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBinderLocalObjectCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBinderProxyObjectCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBinderReceivedTransactions",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBinderSentTransactions",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method: resetAllCounts",
            method = "resetAllCounts",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalFreedCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalFreedSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalGcInvocationCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalExternalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalExternalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalExternalFreedCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetGlobalExternalFreedSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetThreadAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetThreadAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetThreadExternalAllocCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetThreadExternalAllocSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetThreadGcInvocationCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dumpHprofData",
            args = {String.class}
        )
    })
    public void testGetAndReset() throws IOException {
        final String dumpFile = getFileName();
        Debug.startAllocCounting();

        final int MIN_GLOBAL_ALLOC_COUNT = 100;
        final int ARRAY_SIZE = 100;
        final int MIN_GLOBAL_ALLOC_SIZE = MIN_GLOBAL_ALLOC_COUNT * ARRAY_SIZE;
        for(int i = 0; i < MIN_GLOBAL_ALLOC_COUNT; i++){
            // for test alloc huge memory
            int[] test = new int[ARRAY_SIZE];
        }

        assertTrue(Debug.getGlobalAllocCount() >= MIN_GLOBAL_ALLOC_COUNT);
        assertTrue(Debug.getGlobalAllocSize() >= MIN_GLOBAL_ALLOC_SIZE);
        assertTrue(Debug.getGlobalFreedCount() >= 0);
        assertTrue(Debug.getGlobalFreedSize() >= 0);
        assertTrue(Debug.getNativeHeapSize() >= 0);
        assertTrue(Debug.getGlobalExternalAllocCount() >= 0);
        assertTrue(Debug.getGlobalExternalAllocSize() >= 0);
        assertTrue(Debug.getGlobalExternalFreedCount() >= 0);
        assertTrue(Debug.getGlobalExternalFreedSize() >= 0);
        assertTrue(Debug.getLoadedClassCount() >= 0);
        assertTrue(Debug.getNativeHeapAllocatedSize() >= 0);
        assertTrue(Debug.getNativeHeapFreeSize() >= 0);
        assertTrue(Debug.getNativeHeapSize() >= 0);
        assertTrue(Debug.getThreadAllocCount() >= 0);
        assertTrue(Debug.getThreadAllocSize() >= 0);
        assertTrue(Debug.getThreadExternalAllocCount() >=0);
        assertTrue(Debug.getThreadExternalAllocSize() >= 0);
        assertTrue(Debug.getThreadGcInvocationCount() >= 0);
        assertTrue(Debug.getBinderDeathObjectCount() >= 0);
        assertTrue(Debug.getBinderLocalObjectCount() >= 0);
        assertTrue(Debug.getBinderProxyObjectCount() >= 0);
        Debug.getBinderReceivedTransactions();
        Debug.getBinderSentTransactions();

        Debug.stopAllocCounting();

        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);

        Debug.resetGlobalAllocCount();
        assertEquals(0, Debug.getGlobalAllocCount());

        Debug.resetGlobalAllocSize();
        assertEquals(0, Debug.getGlobalAllocSize());

        Debug.resetGlobalExternalAllocCount();
        assertEquals(0, Debug.getGlobalExternalAllocCount());

        Debug.resetGlobalExternalAllocSize();
        assertEquals(0, Debug.getGlobalExternalAllocSize());

        Debug.resetGlobalExternalFreedCount();
        assertEquals(0, Debug.getGlobalExternalFreedCount());

        Debug.resetGlobalExternalFreedSize();
        assertEquals(0, Debug.getGlobalExternalFreedSize());

        Debug.resetGlobalFreedCount();
        assertEquals(0, Debug.getGlobalFreedCount());

        Debug.resetGlobalFreedSize();
        assertEquals(0, Debug.getGlobalFreedSize());

        Debug.resetGlobalGcInvocationCount();
        assertEquals(0, Debug.getGlobalGcInvocationCount());

        Debug.resetThreadAllocCount();
        assertEquals(0, Debug.getThreadAllocCount());

        Debug.resetThreadAllocSize();
        assertEquals(0, Debug.getThreadAllocSize());

        Debug.resetThreadExternalAllocCount();
        assertEquals(0, Debug.getThreadExternalAllocCount());

        Debug.resetThreadExternalAllocSize();
        assertEquals(0, Debug.getThreadExternalAllocSize());

        Debug.resetThreadGcInvocationCount();
        assertEquals(0, Debug.getThreadGcInvocationCount());

        Debug.resetAllCounts();
        Debug.dumpHprofData(dumpFile);
    }

    public void testDumpService() throws Exception {
        File file = getContext().getFileStreamPath("dump.out");
        file.delete();
        assertFalse(file.exists());

        FileOutputStream out = getContext().openFileOutput("dump.out", Context.MODE_PRIVATE);
        assertFalse(Debug.dumpService("xyzzy -- not a valid service name", out.getFD(), null));
        out.close();

        // File was opened, but nothing was written
        assertTrue(file.exists());
        assertEquals(0, file.length());

        out = getContext().openFileOutput("dump.out", Context.MODE_PRIVATE);
        assertTrue(Debug.dumpService(Context.POWER_SERVICE, out.getFD(), null));
        out.close();

        // Don't require any specific content, just that something was written
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
