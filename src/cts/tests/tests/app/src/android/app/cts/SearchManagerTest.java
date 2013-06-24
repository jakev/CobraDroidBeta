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

package android.app.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

@TestTargetClass(SearchManager.class)
public class SearchManagerTest extends CTSActivityTestCaseBase {

    private void setupActivity(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(getInstrumentation().getTargetContext(), SearchManagerStubActivity.class);
        getInstrumentation().getTargetContext().startActivity(intent);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, ComponentName.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopSearch",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isVisible",
            args = {}
        )
    })
    public void testStopSearch() throws InterruptedException {
        SearchManagerStubActivity.setCTSResult(this);
        setupActivity(SearchManagerStubActivity.TEST_STOP_SEARCH);
        waitForResult();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnDismissListener",
            args = {android.app.SearchManager.OnDismissListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDismiss",
            args = {DialogInterface.class}
        )
    })
    @ToBeFixed(bug = "1731631", explanation = "From the doc of SearchManager, "
            + "we see that \"If the user simply canceled the search UI, "
            + "your activity will regain input focus and proceed as before. "
            + "See setOnDismissListener(SearchManager.OnDismissListener) and "
            + "setOnCancelListener(SearchManager.OnCancelListener) "
            + "if you required direct notification of search dialog dismissals.\" "
            + "So that means if the SearchManager has set the OnDismissListener "
            + "and user cancel the search UI, OnDismissListener#onDismiss() will be called. "
            + "But we have tried to cancel the search UI with the back key "
            + "but onDismiss() is not called. Is this a bug?")
    public void testSetOnDismissListener() throws InterruptedException {
        SearchManagerStubActivity.setCTSResult(this);
        setupActivity(SearchManagerStubActivity.TEST_ON_DISMISSLISTENER);
        waitForResult();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnCancelListener",
            args = {android.app.SearchManager.OnCancelListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCancel",
            args = {DialogInterface.class}
        )
    })
    public void testSetOnCancelListener() throws InterruptedException {
        SearchManagerStubActivity.setCTSResult(this);
        setupActivity(SearchManagerStubActivity.TEST_ON_CANCELLISTENER);
        waitForResult();
    }
}
