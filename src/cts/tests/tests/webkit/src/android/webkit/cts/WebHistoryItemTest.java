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

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.graphics.Bitmap;
import android.test.ActivityInstrumentationTestCase2;
import android.view.animation.cts.DelayedCheck;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@TestTargetClass(android.webkit.WebHistoryItem.class)
public class WebHistoryItemTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private CtsTestServer mWebServer;

    public WebHistoryItemTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebServer = new CtsTestServer(getActivity());
    }

    @Override
    protected void tearDown() throws Exception {
        mWebServer.shutdown();
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFavicon",
            args = {}
        )
    })
    public void testWebHistoryItem() {
        final WebView view = getActivity().getWebView();
        view.setWebChromeClient(new WebChromeClient());
        WebBackForwardList list = view.copyBackForwardList();
        assertEquals(0, list.getSize());

        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(view, url);
        list = view.copyBackForwardList();
        assertEquals(1, list.getSize());
        WebHistoryItem item = list.getCurrentItem();
        assertNotNull(item);
        int firstId = item.getId();
        assertEquals(url, item.getUrl());
        assertNull(item.getOriginalUrl());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, item.getTitle());
        Bitmap icon = view.getFavicon();
        assertEquals(icon, item.getFavicon());

        url = mWebServer.getAssetUrl(TestHtmlConstants.BR_TAG_URL);
        assertLoadUrlSuccessfully(view, url);
        list = view.copyBackForwardList();
        assertEquals(2, list.getSize());
        item = list.getCurrentItem();
        assertNotNull(item);
        assertEquals(TestHtmlConstants.BR_TAG_TITLE, item.getTitle());
        int secondId = item.getId();
        assertTrue(firstId != secondId);
    }

    private void assertLoadUrlSuccessfully(final WebView view, String url) {
        view.loadUrl(url);
        // wait for the page load to complete
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return view.getProgress() == 100;
            }
        }.run();
    }
}
