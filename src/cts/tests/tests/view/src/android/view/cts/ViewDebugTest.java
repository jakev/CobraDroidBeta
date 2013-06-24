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

package android.view.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewDebug.HierarchyTraceType;
import android.view.ViewDebug.RecyclerTraceType;
import android.widget.TextView;

@TestTargetClass(ViewDebug.class)
public class ViewDebugTest extends AndroidTestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ViewDebug",
        args = {}
    )
    public void testConstructor() {
        new ViewDebug();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startRecyclerTracing",
            args = {String.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "trace",
            args = {View.class, RecyclerTraceType.class, int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "stopRecyclerTracing",
            args = {}
        )
    })
    @ToBeFixed(bug = "1852451", explanation = "compiling error when set TRACE_RECYCLER to true")
    public void testRecyclerTracing() {
        final String recyclerTracePrefix = "ViewDebugTest";
        View ownerView = new View(getContext());
        View view = new View(getContext());

        // debugging should be disabled on production devices
        assertFalse(ViewDebug.TRACE_RECYCLER);

        // just call the methods; they should return immediately
        ViewDebug.startRecyclerTracing(recyclerTracePrefix, ownerView);
        ViewDebug.trace(view, RecyclerTraceType.NEW_VIEW, 0, 1);
        ViewDebug.stopRecyclerTracing();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startHierarchyTracing",
            args = {String.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "trace",
            args = {View.class, HierarchyTraceType.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "stopHierarchyTracing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "dumpCapturedView",
            args = {String.class, Object.class}
        )
    })
    @ToBeFixed(bug = "1852451", explanation = "throw IllegalStateException when set"
              + "TRACE_HIERARCHY to true")
    public void testHierarchyTracing() {
        final String hierarchyTracePrefix = "ViewDebugTest";
        View v1 = new View(getContext());
        View v2 = new View(getContext());

        // debugging should be disabled on production devices
        assertFalse(ViewDebug.TRACE_HIERARCHY);

        // just call the methods; they should return immediately
        ViewDebug.startHierarchyTracing(hierarchyTracePrefix, v1);
        ViewDebug.trace(v2, HierarchyTraceType.INVALIDATE);
        ViewDebug.stopHierarchyTracing();
        ViewDebug.dumpCapturedView("TAG", new TextView(getContext()));
    }

}
