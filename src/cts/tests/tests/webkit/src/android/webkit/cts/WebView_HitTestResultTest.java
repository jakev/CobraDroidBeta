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

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.animation.cts.DelayedCheck;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;

@TestTargetClass(HitTestResult.class)
public class WebView_HitTestResultTest
        extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static long TEST_TIMEOUT = 5000L;
    private static long TIME_FOR_LAYOUT = 1000L;

    public WebView_HitTestResultTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    private void waitForLoading(final WebView webView, long timeout) throws InterruptedException {
        new DelayedCheck(timeout) {
            @Override
            protected boolean check() {
                return webView.getProgress() == 100;
            }
        }.run();
        Thread.sleep(TIME_FOR_LAYOUT);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExtra",
            args = {}
        )
    })
    public void testHitTestResult() throws InterruptedException {
        WebView webView = getActivity().getWebView();
        String anchor = "<p><a href=\"" + TestHtmlConstants.EXT_WEB_URL1
                + "\">normal anchor</a></p>";
        webView.loadDataWithBaseURL("fake://home", "<html><body>" + anchor
                + "</body></html>", "text/html", "UTF-8", null);
        waitForLoading(webView, TEST_TIMEOUT);

        // anchor
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        // extra sleep to make sure key has been fully handled
        Thread.sleep(500);
        HitTestResult result = webView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals(TestHtmlConstants.EXT_WEB_URL1, result.getExtra());
    }
}
