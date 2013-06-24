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
import android.view.animation.cts.DelayedCheck;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;

@TestTargetClass(WebBackForwardList.class)
public class WebBackForwardListTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {

    private static final int TEST_TIMEOUT = 10000;

    private WebView mWebView;

    public WebBackForwardListTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentItem",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentIndex",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getItemAtIndex",
            args = {int.class}
        )
    })
    public void testGetCurrentItem() throws Exception {
        mWebView = getActivity().getWebView();
        WebBackForwardList list = mWebView.copyBackForwardList();

        assertNull(list.getCurrentItem());
        assertEquals(0, list.getSize());
        assertEquals(-1, list.getCurrentIndex());
        assertNull(list.getItemAtIndex(-1));
        assertNull(list.getItemAtIndex(2));

        CtsTestServer server = new CtsTestServer(getActivity(), false);
        try {
            String url1 = server.getAssetUrl(TestHtmlConstants.HTML_URL1);
            String url2 = server.getAssetUrl(TestHtmlConstants.HTML_URL2);
            String url3 = server.getAssetUrl(TestHtmlConstants.HTML_URL3);

            mWebView.loadUrl(url1);
            checkBackForwardList(mWebView, url1);

            mWebView.loadUrl(url2);
            checkBackForwardList(mWebView, url1, url2);

            mWebView.loadUrl(url3);
            checkBackForwardList(mWebView, url1, url2, url3);
        } finally {
            server.shutdown();
        }
    }

    private void checkBackForwardList(final WebView view, final String... url) {
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                if (view.getProgress() < 100) {
                    return false;
                }
                WebBackForwardList list = view.copyBackForwardList();
                if (list.getSize() != url.length) {
                    return false;
                }
                if (list.getCurrentIndex() != url.length - 1) {
                    return false;
                }
                for (int i = 0; i < url.length; i++) {
                    WebHistoryItem item = list.getItemAtIndex(i);
                    if (!url[i].equals(item.getUrl())) {
                        return false;
                    }
                }
                return true;
            }

        }.run();
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "clone() is protected and WebBackForwardList cannot be subclassed here",
        method = "clone",
        args = {}
    )
    public void testClone() {
    }

}
