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

import android.test.AndroidTestCase;
import android.webkit.WebView;
import android.webkit.WebView.WebViewTransport;

@TestTargetClass(WebViewTransport.class)
public class WebView_WebViewTransportTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWebView",
            args = {WebView.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWebView",
            args = {}
        )
    })
    public void testAccessWebView() {
        WebView webView = new WebView(mContext);
        WebViewTransport transport = webView.new WebViewTransport();

        assertNull(transport.getWebView());

        transport.setWebView(webView);
        assertSame(webView, transport.getWebView());
    }
}
