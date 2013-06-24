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
import dalvik.annotation.ToBeFixed;

import android.graphics.Bitmap;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.animation.cts.DelayedCheck;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
 
@TestTargetClass(android.webkit.WebViewClient.class)
public class WebViewClientTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long TEST_TIMEOUT = 5000;

    private WebView mWebView;
    private CtsTestServer mWebServer;

    public WebViewClientTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
        mWebView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    protected void tearDown() throws Exception {
        mWebView.clearHistory();
        mWebView.clearCache(true);
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "shouldOverrideUrlLoading",
        args = {WebView.class, String.class}
    )
    public void testShouldOverrideUrlLoading() {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        assertFalse(webViewClient.shouldOverrideUrlLoading(mWebView, null));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPageStarted",
            args = {WebView.class, String.class, Bitmap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPageFinished",
            args = {WebView.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLoadResource",
            args = {WebView.class, String.class}
        )
    })
    public void testLoadPage() throws Exception {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);
        mWebServer = new CtsTestServer(getActivity());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);

        assertFalse(webViewClient.hasOnPageStartedCalled());
        assertFalse(webViewClient.hasOnLoadResourceCalled());
        assertFalse(webViewClient.hasOnPageFinishedCalled());
        mWebView.loadUrl(url);

        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasOnPageStartedCalled();
            }
        }.run();

        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasOnLoadResourceCalled();
            }
        }.run();

        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasOnPageFinishedCalled();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onReceivedError",
        args = {WebView.class, int.class, String.class, String.class}
    )
    public void testOnReceivedError() throws Exception {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);

        String wrongUri = "invalidscheme://some/resource";
        assertEquals(0, webViewClient.hasOnReceivedErrorCode());
        assertLoadUrlSuccessfully(mWebView, wrongUri);
        assertEquals(WebViewClient.ERROR_UNSUPPORTED_SCHEME,
                webViewClient.hasOnReceivedErrorCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onFormResubmission",
        args = {WebView.class, Message.class, Message.class}
    )
    public void testOnFormResubmission() throws Exception {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebServer = new CtsTestServer(getActivity());

        assertFalse(webViewClient.hasOnFormResubmissionCalled());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.JS_FORM_URL);
        // this loads a form, which automatically posts itself
        assertLoadUrlSuccessfully(mWebView, url);
        Thread.sleep(1000); // allow for javascript to post the form
        // the URL should have changed when the form was posted
        assertFalse(url.equals(mWebView.getUrl()));
        // reloading the current URL should trigger the callback
        mWebView.reload();
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasOnFormResubmissionCalled();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "doUpdateVisitedHistory",
        args = {WebView.class, String.class, boolean.class}
    )
    public void testDoUpdateVisitedHistory() throws Exception {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);
        mWebServer = new CtsTestServer(getActivity());

        assertFalse(webViewClient.hasDoUpdateVisitedHistoryCalled());
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.BR_TAG_URL);
        assertLoadUrlSuccessfully(mWebView, url1);
        assertLoadUrlSuccessfully(mWebView, url2);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasDoUpdateVisitedHistoryCalled();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onReceivedHttpAuthRequest",
        args = {WebView.class, HttpAuthHandler.class, String.class, String.class}
    )
    public void testOnReceivedHttpAuthRequest() throws Exception {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);
        mWebServer = new CtsTestServer(getActivity());

        assertFalse(webViewClient.hasOnReceivedHttpAuthRequestCalled());
        String url = mWebServer.getAuthAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        assertTrue(webViewClient.hasOnReceivedHttpAuthRequestCalled());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "shouldOverrideKeyEvent",
        args = {WebView.class, KeyEvent.class}
    )
    public void testShouldOverrideKeyEvent() {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);

        assertFalse(webViewClient.shouldOverrideKeyEvent(mWebView, null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onUnhandledKeyEvent",
        args = {WebView.class, KeyEvent.class}
    )
    public void testOnUnhandledKeyEvent() throws Throwable {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertFalse(webViewClient.hasOnUnhandledKeyEventCalled());
        sendKeys(KeyEvent.KEYCODE_1);

        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return webViewClient.hasOnUnhandledKeyEventCalled();
            }
        }.run();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onScaleChanged",
        args = {WebView.class, float.class, float.class}
    )
    public void testOnScaleChanged() throws Throwable {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);

        assertFalse(webViewClient.hasOnScaleChangedCalled());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.zoomIn();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(webViewClient.hasOnScaleChangedCalled());
    }

    private void assertLoadUrlSuccessfully(final WebView view, String url) {
        view.loadUrl(url);
        // wait until load is complete
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return view.getProgress() == 100;
            }
        }.run();
    }

    private class MockWebViewClient extends WebViewClient {
        private boolean mOnPageStartedCalled;
        private boolean mOnPageFinishedCalled;
        private boolean mOnLoadResourceCalled;
        private int mOnReceivedErrorCode;
        private boolean mOnFormResubmissionCalled;
        private boolean mDoUpdateVisitedHistoryCalled;
        private boolean mOnReceivedHttpAuthRequestCalled;
        private boolean mOnUnhandledKeyEventCalled;
        private boolean mOnScaleChangedCalled;

        public boolean hasOnPageStartedCalled() {
            return mOnPageStartedCalled;
        }

        public boolean hasOnPageFinishedCalled() {
            return mOnPageFinishedCalled;
        }

        public boolean hasOnLoadResourceCalled() {
            return mOnLoadResourceCalled;
        }

        public int hasOnReceivedErrorCode() {
            return mOnReceivedErrorCode;
        }

        public boolean hasOnFormResubmissionCalled() {
            return mOnFormResubmissionCalled;
        }

        public boolean hasDoUpdateVisitedHistoryCalled() {
            return mDoUpdateVisitedHistoryCalled;
        }

        public boolean hasOnReceivedHttpAuthRequestCalled() {
            return mOnReceivedHttpAuthRequestCalled;
        }

        public boolean hasOnUnhandledKeyEventCalled() {
            return mOnUnhandledKeyEventCalled;
        }

        public boolean hasOnScaleChangedCalled() {
            return mOnScaleChangedCalled;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mOnPageStartedCalled = true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            assertTrue(mOnPageStartedCalled);
            assertTrue(mOnLoadResourceCalled);
            mOnPageFinishedCalled = true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            assertTrue(mOnPageStartedCalled);
            mOnLoadResourceCalled = true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mOnReceivedErrorCode = errorCode;
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            mOnFormResubmissionCalled = true;
            dontResend.sendToTarget();
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            mDoUpdateVisitedHistoryCalled = true;
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
            mOnReceivedHttpAuthRequestCalled = true;
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
            mOnUnhandledKeyEventCalled = true;
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            mOnScaleChangedCalled = true;
        }
    }
}
