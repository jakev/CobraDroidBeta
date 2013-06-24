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

package android.webkit.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.os.Message;

import junit.framework.TestCase;

@TestTargetClass(android.webkit.SslErrorHandler.class)
public class SslErrorHandlerTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "cancel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "handleMessage",
            args = {Message.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "proceed",
            args = {}
        )
    })
    public void testSslErrorHandler() {
    }
}
