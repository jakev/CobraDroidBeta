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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.cts.DelayedCheck;
import android.webkit.CacheManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebView.PictureListener;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;

@TestTargetClass(android.webkit.WebView.class)
public class WebViewTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final int INITIAL_PROGRESS = 100;
    private static long TEST_TIMEOUT = 20000L;
    private static long TIME_FOR_LAYOUT = 1000L;

    private WebView mWebView;
    private CtsTestServer mWebServer;

    public WebViewTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
        File f = getActivity().getFileStreamPath("snapshot");
        if (f.exists()) {
            f.delete();
        }
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

    private void startWebServer(boolean secure) throws Exception {
        assertNull(mWebServer);
        mWebServer = new CtsTestServer(getActivity(), secure);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class, AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new WebView(getActivity());
        new WebView(getActivity(), null);
        new WebView(getActivity(), null, 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findAddress",
        args = {String.class}
    )
    public void testFindAddress() {
        /*
         * Info about USPS
         * http://en.wikipedia.org/wiki/Postal_address#United_States
         * http://www.usps.com/
         */
        // full address
        assertEquals("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFORNIA 92926",
                WebView.findAddress("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFORNIA 92926"));
        // full address ( with abbreviated street type and state)
        assertEquals("455 LARKSPUR DR CALIFORNIA SPRINGS CA 92926",
                WebView.findAddress("455 LARKSPUR DR CALIFORNIA SPRINGS CA 92926"));
        // misspell the state ( CALIFORNIA -> CALIFONIA )
        assertNull(WebView.findAddress("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFONIA 92926"));
        // without optional zip code
        assertEquals("455 LARKSPUR DR CALIFORNIA SPRINGS CA",
                WebView.findAddress("455 LARKSPUR DR CALIFORNIA SPRINGS CA"));
        // house number, street name and street type are missing
        assertNull(WebView.findAddress("CALIFORNIA SPRINGS CA"));
        // city & state are missing
        assertNull(WebView.findAddress("455 LARKSPUR DR"));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getZoomControls",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSettings",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    @UiThreadTest
    public void testGetZoomControls() {
         WebSettings settings = mWebView.getSettings();
         assertTrue(settings.supportZoom());
         View zoomControls = mWebView.getZoomControls();
         assertNotNull(zoomControls);

         // disable zoom support
         settings.setSupportZoom(false);
         assertFalse(settings.supportZoom());
         assertNull(mWebView.getZoomControls());
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "invokeZoomPicker",
        args = {},
        notes = "Cannot test the effect of this method"
    )
    public void testInvokeZoomPicker() throws Exception {
        WebSettings settings = mWebView.getSettings();
        assertTrue(settings.supportZoom());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        mWebView.invokeZoomPicker();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "zoomIn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "zoomOut",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScale",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSettings",
            args = {}
        )
    })
    @UiThreadTest
    public void testZoom() {
        WebSettings settings = mWebView.getSettings();
        settings.setSupportZoom(false);
        assertFalse(settings.supportZoom());
        float currScale = mWebView.getScale();
        float previousScale = currScale;

        // can zoom in or out although zoom support is disabled in web settings
        assertTrue(mWebView.zoomIn());
        currScale = mWebView.getScale();
        assertTrue(currScale > previousScale);

        // zoom in
        assertTrue(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale < previousScale);

        // enable zoom support
        settings.setSupportZoom(true);
        assertTrue(settings.supportZoom());
        currScale = mWebView.getScale();

        assertTrue(mWebView.zoomIn());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale > previousScale);

        // zoom in until it reaches maximum scale
        while (currScale > previousScale) {
            mWebView.zoomIn();
            previousScale = currScale;
            currScale = mWebView.getScale();
        }

        // can not zoom in further
        assertFalse(mWebView.zoomIn());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertEquals(currScale, previousScale);

        // zoom out
        assertTrue(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale < previousScale);

        // zoom out until it reaches minimum scale
        while (currScale < previousScale) {
            mWebView.zoomOut();
            previousScale = currScale;
            currScale = mWebView.getScale();
        }

        // can not zoom out further
        assertFalse(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertEquals(currScale, previousScale);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScrollBarStyle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayHorizontalScrollbar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayVerticalScrollbar",
            args = {}
        )
    })
    @UiThreadTest
    public void testSetScrollBarStyle() {
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());

        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());

        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalScrollbarOverlay",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalScrollbarOverlay",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayHorizontalScrollbar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayVerticalScrollbar",
            args = {}
        )
    })
    public void testScrollBarOverlay() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);

        String p = "<p style=\"height:" + dimension + "px;" +
                "width:" + dimension + "px;margin:0px auto;\">Test scroll bar overlay.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());
        int startX = mWebView.getScrollX();
        int startY = mWebView.getScrollY();

        final int bigVelocity = 10000;
        // fling to the max and wait for ending scroll
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(bigVelocity, bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();

        int overlayOffsetX = mWebView.getScrollX() - startX;
        int insetOffsetY = mWebView.getScrollY() - startY;

        // scroll back
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(-bigVelocity, -bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();

        mWebView.setHorizontalScrollbarOverlay(false);
        mWebView.setVerticalScrollbarOverlay(true);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());

        // fling to the max and wait for ending scroll
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(bigVelocity, bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();

        int insetOffsetX = mWebView.getScrollX() - startX;
        int overlayOffsetY = mWebView.getScrollY() - startY;

        assertTrue(overlayOffsetY > insetOffsetY);
        assertTrue(overlayOffsetX > insetOffsetX);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadUrl",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginalUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProgress",
            args = {}
        )
    })
    public void testLoadUrl() throws Exception {
        assertNull(mWebView.getUrl());
        assertNull(mWebView.getOriginalUrl());
        assertEquals(INITIAL_PROGRESS, mWebView.getProgress());

        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(100, mWebView.getProgress());
        assertEquals(url, mWebView.getUrl());
        assertEquals(url, mWebView.getOriginalUrl());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, mWebView.getTitle());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginalUrl",
            args = {}
        )
    })
    public void testGetOriginalUrl() throws Exception {
        assertNull(mWebView.getUrl());
        assertNull(mWebView.getOriginalUrl());

        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        String redirect = mWebServer.getRedirectingAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        // set the web view client so that redirects are loaded in the WebView itself
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(redirect);

        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(url, mWebView.getUrl());
        assertEquals(redirect, mWebView.getOriginalUrl());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "stopLoading",
        args = {}
    )
    public void testStopLoading() throws Exception {
        assertNull(mWebView.getUrl());
        assertEquals(INITIAL_PROGRESS, mWebView.getProgress());

        startWebServer(false);
        String url = mWebServer.getDelayedAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        mWebView.stopLoading();
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return 100 == mWebView.getProgress();
            }
        }.run();
        assertNull(mWebView.getUrl());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoBack",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoForward",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoBackOrForward",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goBack",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goForward",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goBackOrForward",
            args = {int.class}
        )
    })
    public void testGoBackAndForward() throws Exception {
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(false, 1);

        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);

        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(false, 1);

        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);
        assertGoBackOrForwardBySteps(true, -1);
        assertGoBackOrForwardBySteps(false, 1);

        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);

        mWebView.goBack();
        delayedCheckWebBackForwardList(url2, 1, 3);
        assertGoBackOrForwardBySteps(true, -1);
        assertGoBackOrForwardBySteps(true, 1);

        mWebView.goForward();
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);

        mWebView.goBackOrForward(-2);
        delayedCheckWebBackForwardList(url1, 0, 3);
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(true, 2);

        mWebView.goBackOrForward(2);
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addJavascriptInterface",
        args = {Object.class, String.class}
    )
    public void testAddJavascriptInterface() throws Exception {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        final DummyJavaScriptInterface obj = new DummyJavaScriptInterface();
        mWebView.addJavascriptInterface(obj, "dummy");
        assertFalse(obj.hasChangedTitle());

        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.ADD_JAVA_SCRIPT_INTERFACE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return obj.hasChangedTitle();
            }
        }.run();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBackgroundColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reload",
            args = {}
        )
    })
    public void testCapturePicture() throws Exception {
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.BLANK_PAGE_URL);
        // showing the blank page will make the picture filled with background color
        assertLoadUrlSuccessfully(mWebView, url);
        Picture p = mWebView.capturePicture();
        Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b));
        // default color is white
        assertBitmapFillWithColor(b, Color.WHITE);

        mWebView.setBackgroundColor(Color.CYAN);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        // the content of the picture will not be updated automatically
        p.draw(new Canvas(b));
        assertBitmapFillWithColor(b, Color.WHITE);
        // update the content
        p = mWebView.capturePicture();
        p.draw(new Canvas(b));
        assertBitmapFillWithColor(b, Color.CYAN);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setPictureListener",
        args = {PictureListener.class}
    )
    public void testSetPictureListener() throws Exception {
        final MyPictureListener listener = new MyPictureListener();
        mWebView.setPictureListener(listener);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return listener.callCount > 0;
            }
        }.run();
        assertEquals(mWebView, listener.webView);
        assertNotNull(listener.picture);

        final int oldCallCount = listener.callCount;
        url = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return listener.callCount > oldCallCount;
            }
        }.run();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "savePicture",
            args = {Bundle.class, File.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Cannot test whether picture has been restored correctly.",
            method = "restorePicture",
            args = {Bundle.class, File.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reload",
            args = {}
        )
    })
    public void testSaveAndRestorePicture() throws Throwable {
        mWebView.setBackgroundColor(Color.CYAN);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.BLANK_PAGE_URL);
        assertLoadUrlSuccessfully(mWebView, url);

        final Bundle bundle = new Bundle();
        final File f = getActivity().getFileStreamPath("snapshot");
        if (f.exists()) {
            f.delete();
        }

        try {
            assertTrue(bundle.isEmpty());
            assertEquals(0, f.length());
            assertTrue(mWebView.savePicture(bundle, f));

            // File saving is done in a separate thread.
            new DelayedCheck() {
                @Override
                protected boolean check() {
                    return f.length() > 0;
                }
            }.run();

            assertFalse(bundle.isEmpty());

            Picture p = Picture.createFromStream(new FileInputStream(f));
            Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
            p.draw(new Canvas(b));
            assertBitmapFillWithColor(b, Color.CYAN);

            mWebView.setBackgroundColor(Color.WHITE);
            mWebView.reload();
            waitForLoadComplete(mWebView, TEST_TIMEOUT);

            b = Bitmap.createBitmap(mWebView.getWidth(), mWebView.getHeight(), Config.ARGB_8888);
            mWebView.draw(new Canvas(b));
            assertBitmapFillWithColor(b, Color.WHITE);
            runTestOnUiThread(new Runnable() {
                public void run() {
                    assertTrue(mWebView.restorePicture(bundle, f));
                }
            });
            getInstrumentation().waitForIdleSync();
            // Cannot test whether the picture has been restored successfully.
            // Drawing the webview into a canvas will draw white, but on the display it is cyan
        } finally {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHttpAuthUsernamePassword",
            args = {String.class, String.class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHttpAuthUsernamePassword",
            args = {String.class, String.class}
        )
    })
    public void testAccessHttpAuthUsernamePassword() {
        try {
            WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();

            String host = "http://localhost:8080";
            String realm = "testrealm";
            String userName = "user";
            String password = "password";

            String[] result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNull(result);

            mWebView.setHttpAuthUsernamePassword(host, realm, userName, password);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(password, result[1]);

            String newPassword = "newpassword";
            mWebView.setHttpAuthUsernamePassword(host, realm, userName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(newPassword, result[1]);

            String newUserName = "newuser";
            mWebView.setHttpAuthUsernamePassword(host, realm, newUserName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(newUserName, result[0]);
            assertEquals(newPassword, result[1]);

            // the user is set to null, can not change any thing in the future
            mWebView.setHttpAuthUsernamePassword(host, realm, null, password);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertNull(result[0]);
            assertEquals(password, result[1]);

            mWebView.setHttpAuthUsernamePassword(host, realm, userName, null);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(null, result[1]);

            mWebView.setHttpAuthUsernamePassword(host, realm, null, null);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertNull(result[0]);
            assertNull(result[1]);

            mWebView.setHttpAuthUsernamePassword(host, realm, newUserName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(newUserName, result[0]);
            assertEquals(newPassword, result[1]);
        } finally {
            WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "savePassword",
        args = {String.class, String.class, String.class}
    )
    public void testSavePassword() {
        WebViewDatabase db = WebViewDatabase.getInstance(getActivity());
        try {
            db.clearUsernamePassword();

            String host = "http://localhost:8080";
            String userName = "user";
            String password = "password";
            assertFalse(db.hasUsernamePassword());
            mWebView.savePassword(host, userName, password);
            assertTrue(db.hasUsernamePassword());
        } finally {
            db.clearUsernamePassword();
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadData",
            args = {String.class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        )
    })
    public void testLoadData() throws Exception {
        assertNull(mWebView.getTitle());
        mWebView.loadData("<html><head><title>Hello,World!</title></head><body></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("Hello,World!", mWebView.getTitle());

        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        AssetManager assets = getActivity().getAssets();
        Bitmap b1 = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.SMALL_IMG_URL));
        b1 = b1.copy(Config.ARGB_8888, true);

        Picture p = mWebView.capturePicture();
        Bitmap b2 = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b2));
        assertTrue(checkBitmapInsideAnother(b1, b2));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadDataWithBaseURL",
            args = {String.class, String.class, String.class, String.class, String.class}
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
        )
    })
    public void testLoadDataWithBaseUrl() throws Exception {
        assertNull(mWebView.getTitle());
        assertNull(mWebView.getUrl());
        String imgUrl = TestHtmlConstants.SMALL_IMG_URL; // relative

        startWebServer(false);
        String baseUrl = mWebServer.getAssetUrl("foo.html");
        String failUrl = "random";
        mWebView.loadDataWithBaseURL(baseUrl,
                "<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8", failUrl);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        // check that image was retrieved from the server
        assertTrue(mWebServer.getLastRequestUrl().endsWith(imgUrl));
        // the fail URL is used for the history entry, even if the load succeeds
        assertEquals(failUrl, mWebView.getUrl());

        imgUrl = TestHtmlConstants.LARGE_IMG_URL;
        mWebView.loadDataWithBaseURL(baseUrl,
                "<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8", null);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        // check that image was retrieved from the server
        assertTrue(mWebServer.getLastRequestUrl().endsWith(imgUrl));
        // no history item saved, URL is still the last one
        assertEquals("about:blank", mWebView.getUrl());
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "findAll",
        args = {String.class},
        notes = "Cannot check highlighting"
    )
    public void testFindAll() throws Throwable {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                String p = "<p>Find all instances of find on the page and highlight them.</p>";

                mWebView.loadData("<html><body>" + p +"</body></html>", "text/html", "UTF-8");
            }
        });
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        runTestOnUiThread(new Runnable() {
            public void run() {
                assertEquals(2, mWebView.findAll("find"));
            }
        });
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findNext",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findAll",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearMatches",
            args = {}
        )
    })
    public void testFindNext() throws Throwable {
        // Reset the scaling so that finding the next "all" text will require scrolling.
        mWebView.setInitialScale(100);

        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = Math.max(metrics.widthPixels, metrics.heightPixels);
        // create a paragraph high enough to take up the entire screen
        String p = "<p style=\"height:" + dimension + "px;\">" +
                "Find all instances of a word on the page and highlight them.</p>";

        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        // highlight all the strings found
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.findAll("all");
            }
        });
        getInstrumentation().waitForIdleSync();
        int previousScrollY = mWebView.getScrollY();

        // Can not use @UiThreadTest here as we need wait in other thread until the scroll in UI
        // thread finishes
        findNextOnUiThread(true);
        delayedCheckStopScrolling();
        // assert that the view scrolls and focuses "all" in the second page
        assertTrue(mWebView.getScrollY() > previousScrollY);
        previousScrollY = mWebView.getScrollY();

        findNextOnUiThread(true);
        delayedCheckStopScrolling();
        // assert that the view scrolls and focuses "all" in the first page
        assertTrue(mWebView.getScrollY() < previousScrollY);
        previousScrollY = mWebView.getScrollY();

        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        // assert that the view scrolls and focuses "all" in the second page
        assertTrue(mWebView.getScrollY() > previousScrollY);
        previousScrollY = mWebView.getScrollY();

        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        // assert that the view scrolls and focuses "all" in the first page
        assertTrue(mWebView.getScrollY() < previousScrollY);
        previousScrollY = mWebView.getScrollY();

        // clear the result
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.clearMatches();
            }
        });
        getInstrumentation().waitForIdleSync();

        // can not scroll any more
        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() == previousScrollY);

        findNextOnUiThread(true);
        delayedCheckStopScrolling();

        assertTrue(mWebView.getScrollY() == previousScrollY);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "documentHasImages",
        args = {android.os.Message.class}
    )
    public void testDocumentHasImages() throws Exception {
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        // create the handler in other thread
        final DocumentHasImageCheckHandler handler =
            new DocumentHasImageCheckHandler(mWebView.getHandler().getLooper());
        Message response = new Message();
        response.setTarget(handler);
        assertFalse(handler.hasCalledHandleMessage());
        mWebView.documentHasImages(response);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(1, handler.getMsgArg1());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pageDown",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pageUp",
            args = {boolean.class}
        )
    })
    public void testPageScroll() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;\">" +
                "Scroll by half the size of the page.</p>";
        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        assertTrue(pageDownOnUiThread(false));

        // scroll to the bottom
        while (pageDownOnUiThread(false)) {
            // do nothing
        }
        assertFalse(pageDownOnUiThread(false));
        int bottomScrollY = mWebView.getScrollY();

        assertTrue(pageUpOnUiThread(false));

        // scroll to the top
        while (pageUpOnUiThread(false)) {
            // do nothing
        }
        assertFalse(pageUpOnUiThread(false));
        int topScrollY = mWebView.getScrollY();

        // jump to the bottom
        assertTrue(pageDownOnUiThread(true));
        assertEquals(bottomScrollY, mWebView.getScrollY());

        // jump to the top
        assertTrue(pageUpOnUiThread(true));
        assertEquals(topScrollY, mWebView.getScrollY());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContentHeight",
        args = {}
    )
    public void testGetContentHeight() throws InterruptedException {
        mWebView.loadData("<html><body></body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(mWebView.getHeight(), mWebView.getContentHeight() * mWebView.getScale(), 2f);

        final int pageHeight = 600;
        // set the margin to 0
        String p = "<p style=\"height:" + pageHeight + "px;margin:0px auto;\">Get the height of "
                + "HTML content.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebView.getContentHeight() > pageHeight);
        int extraSpace = mWebView.getContentHeight() - pageHeight;

        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(pageHeight + pageHeight + extraSpace, mWebView.getContentHeight());
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Cannot test whether the view is cleared.",
        method = "clearView",
        args = {}
    )
    public void testClearView() throws Throwable {
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        AssetManager assets = getActivity().getAssets();
        Bitmap b1 = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.SMALL_IMG_URL));
        b1 = b1.copy(Config.ARGB_8888, true);

        Picture p = mWebView.capturePicture();
        Bitmap b2 = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b2));
        // the image is painted
        assertTrue(checkBitmapInsideAnother(b1, b2));

        mWebView.clearView();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.invalidate();
            }
        });
        getInstrumentation().waitForIdleSync();
        // Can not check whether method clearView() take effect by automatic testing:
        // 1. Can not use getMeasuredHeight() and getMeasuredWidth() to
        //    check that the onMeasure() returns 0
        // 2. Can not use capturePicture() to check that the content has been cleared.
        //    The result of capturePicture() is not updated after clearView() is called.
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "clearCache",
        args = {boolean.class}
    )
    public void testClearCache() throws Exception {
        final File cacheFileBaseDir = CacheManager.getCacheFileBaseDir();
        mWebView.clearCache(true);
        assertEquals(0, cacheFileBaseDir.list().length);

        startWebServer(false);
        mWebView.loadUrl(mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL));
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        int cacheFileCount = cacheFileBaseDir.list().length;
        assertTrue(cacheFileCount > 0);

        mWebView.clearCache(false);
        // the cache files are still there
        // can not check other effects of the method
        assertEquals(cacheFileCount, cacheFileBaseDir.list().length);

        mWebView.clearCache(true);
        // check the files are deleted
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return cacheFileBaseDir.list().length == 0;
            }
        }.run();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "enablePlatformNotifications",
            args = {},
            notes = "Cannot simulate data state or proxy changes"
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "disablePlatformNotifications",
            args = {},
            notes = "Cannot simulate data state or proxy changes"
        )
    })
    public void testPlatformNotifications() {
        WebView.enablePlatformNotifications();
        WebView.disablePlatformNotifications();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getPluginList",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "refreshPlugins",
            args = {boolean.class}
        )
    })
    public void testAccessPluginList() {
        assertNotNull(WebView.getPluginList());

        // can not find a way to install plugins
        mWebView.refreshPlugins(false);
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "destroy",
        args = {}
    )
    public void testDestroy() {
        // Create a new WebView, since we cannot call destroy() on a view in the hierarchy
        WebView localWebView = new WebView(getActivity());
        localWebView.destroy();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "flingScroll",
        args = {int.class, int.class}
    )
    public void testFlingScroll() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;" +
                "width:" + dimension + "px\">Test fling scroll.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        int previousScrollX = mWebView.getScrollX();
        int previousScrollY = mWebView.getScrollY();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(100, 100);
            }
        });

        int timeSlice = 500;
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() > previousScrollX);
        assertTrue(mWebView.getScrollY() > previousScrollY);

        previousScrollY = mWebView.getScrollY();
        previousScrollX = mWebView.getScrollX();
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() >= previousScrollX);
        assertTrue(mWebView.getScrollY() >= previousScrollY);

        previousScrollY = mWebView.getScrollY();
        previousScrollX = mWebView.getScrollX();
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() >= previousScrollX);
        assertTrue(mWebView.getScrollY() >= previousScrollY);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestFocusNodeHref",
        args = {android.os.Message.class}
    )
    public void testRequestFocusNodeHref() throws InterruptedException {
        String links = "<DL><p><DT><A HREF=\"" + TestHtmlConstants.HTML_URL1
                + "\">HTML_URL1</A><DT><A HREF=\"" + TestHtmlConstants.HTML_URL2
                + "\">HTML_URL2</A></DL><p>";
        mWebView.loadData("<html><body>" + links + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        final HrefCheckHandler handler = new HrefCheckHandler(mWebView.getHandler().getLooper());
        Message hrefMsg = new Message();
        hrefMsg.setTarget(handler);

        // focus on first link
        handler.reset();
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mWebView.requestFocusNodeHref(hrefMsg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(TestHtmlConstants.HTML_URL1, handler.getResultUrl());

        // focus on second link
        handler.reset();
        hrefMsg = new Message();
        hrefMsg.setTarget(handler);
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mWebView.requestFocusNodeHref(hrefMsg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(TestHtmlConstants.HTML_URL2, handler.getResultUrl());

        mWebView.requestFocusNodeHref(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestImageRef",
        args = {android.os.Message.class}
    )
    public void testRequestImageRef() throws Exception {
        AssetManager assets = getActivity().getAssets();
        Bitmap bitmap = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.LARGE_IMG_URL));
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();

        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.LARGE_IMG_URL);
        mWebView.loadData("<html><title>Title</title><body><img src=\"" + imgUrl
                + "\"/></body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        final HrefCheckHandler handler = new HrefCheckHandler(mWebView.getHandler().getLooper());
        Message msg = new Message();
        msg.setTarget(handler);

        // touch the image
        handler.reset();
        int[] location = new int[2];
        mWebView.getLocationOnScreen(location);
        getInstrumentation().sendPointerSync(
                MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN,
                        location[0] + imgWidth / 2,
                        location[1] + imgHeight / 2, 0));
        mWebView.requestImageRef(msg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(imgUrl, handler.mResultUrl);
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "debugDump",
        args = {}
    )
    public void testDebugDump() {
        mWebView.debugDump();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHitTestResult",
        args = {}
    )
    public void testGetHitTestResult() throws Throwable {
        String anchor = "<p><a href=\"" + TestHtmlConstants.EXT_WEB_URL1
                + "\">normal anchor</a></p>";
        String blankAnchor = "<p><a href=\"\">blank anchor</a></p>";
        String form = "<p><form><input type=\"text\" name=\"Test\"><br>"
                + "<input type=\"submit\" value=\"Submit\"></form></p>";
        String phoneNo = "3106984000";
        String tel = "<p><a href=\"tel:" + phoneNo + "\">Phone</a></p>";
        String email = "test@gmail.com";
        String mailto = "<p><a href=\"mailto:" + email + "\">Email</a></p>";
        String location = "shanghai";
        String geo = "<p><a href=\"geo:0,0?q=" + location + "\">Location</a></p>";
        mWebView.loadDataWithBaseURL("fake://home", "<html><body>" + anchor + blankAnchor + form
                + tel + mailto + geo + "</body></html>", "text/html", "UTF-8", null);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);

        // anchor
        moveFocusDown();
        HitTestResult result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals(TestHtmlConstants.EXT_WEB_URL1, result.getExtra());

        // blank anchor
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals("fake://home", result.getExtra());

        // text field
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.EDIT_TEXT_TYPE, result.getType());
        assertNull(result.getExtra());

        // submit button
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.UNKNOWN_TYPE, result.getType());
        assertNull(result.getExtra());

        // phone number
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.PHONE_TYPE, result.getType());
        assertEquals(phoneNo, result.getExtra());

        // email
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.EMAIL_TYPE, result.getType());
        assertEquals(email, result.getExtra());

        // geo address
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.GEO_TYPE, result.getType());
        assertEquals(location, result.getExtra());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setInitialScale",
        args = {int.class}
    )
    public void testSetInitialScale() throws InterruptedException {
        String p = "<p style=\"height:1000px;width:1000px\">Test setInitialScale.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        final float defaultScale = getInstrumentation().getTargetContext().getResources().
            getDisplayMetrics().density;
        assertEquals(defaultScale, mWebView.getScale(), .01f);

        mWebView.setInitialScale(0);
        // modify content to fool WebKit into re-loading
        mWebView.loadData("<html><body>" + p + "2" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(defaultScale, mWebView.getScale(), .01f);

        mWebView.setInitialScale(50);
        mWebView.loadData("<html><body>" + p + "3" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(0.5f, mWebView.getScale(), .02f);

        mWebView.setInitialScale(0);
        mWebView.loadData("<html><body>" + p + "4" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(defaultScale, mWebView.getScale(), .01f);
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No API to trigger receiving an icon. Favicon not loaded automatically.",
        method = "getFavicon",
        args = {}
    )
    @ToBeFixed(explanation = "Favicon is not loaded automatically.")
    public void testGetFavicon() throws Exception {
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.TEST_FAVICON_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        mWebView.getFavicon();
        // ToBeFixed: Favicon is not loaded automatically.
        // assertNotNull(mWebView.getFavicon());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearHistory",
        args = {}
    )
    public void testClearHistory() throws Exception {
        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);

        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);

        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);

        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);

        mWebView.clearHistory();

        // only current URL is left after clearing
        delayedCheckWebBackForwardList(url3, 0, 1);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restoreState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "copyBackForwardList",
            args = {}
        )
    })
    @ToBeFixed(explanation="Web history items do not get inflated after restore.")
    public void testSaveAndRestoreState() throws Throwable {
        // nothing to save
        assertNull(mWebView.saveState(new Bundle()));

        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);

        // make a history list
        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);
        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);
        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);

        // save the list
        Bundle bundle = new Bundle();
        WebBackForwardList saveList = mWebView.saveState(bundle);
        assertNotNull(saveList);
        assertEquals(3, saveList.getSize());
        assertEquals(2, saveList.getCurrentIndex());
        assertEquals(url1, saveList.getItemAtIndex(0).getUrl());
        assertEquals(url2, saveList.getItemAtIndex(1).getUrl());
        assertEquals(url3, saveList.getItemAtIndex(2).getUrl());

        // change the content to a new "blank" web view without history
        final WebView newWebView = new WebView(getActivity());

        WebBackForwardList copyListBeforeRestore = newWebView.copyBackForwardList();
        assertNotNull(copyListBeforeRestore);
        assertEquals(0, copyListBeforeRestore.getSize());

        // restore the list
        final WebBackForwardList restoreList = newWebView.restoreState(bundle);
        assertNotNull(restoreList);
        assertEquals(3, restoreList.getSize());
        assertEquals(2, saveList.getCurrentIndex());
        /* ToBeFixed: The WebHistoryItems do not get inflated. Uncomment remaining tests when fixed.
        // wait for the list items to get inflated
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return restoreList.getItemAtIndex(0).getUrl() != null &&
                       restoreList.getItemAtIndex(1).getUrl() != null &&
                       restoreList.getItemAtIndex(2).getUrl() != null;
            }
        }.run();
        assertEquals(url1, restoreList.getItemAtIndex(0).getUrl());
        assertEquals(url2, restoreList.getItemAtIndex(1).getUrl());
        assertEquals(url3, restoreList.getItemAtIndex(2).getUrl());

        WebBackForwardList copyListAfterRestore = newWebView.copyBackForwardList();
        assertNotNull(copyListAfterRestore);
        assertEquals(3, copyListAfterRestore.getSize());
        assertEquals(2, copyListAfterRestore.getCurrentIndex());
        assertEquals(url1, copyListAfterRestore.getItemAtIndex(0).getUrl());
        assertEquals(url2, copyListAfterRestore.getItemAtIndex(1).getUrl());
        assertEquals(url3, copyListAfterRestore.getItemAtIndex(2).getUrl());
        */
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setWebViewClient",
        args = {WebViewClient.class}
    )
    public void testSetWebViewClient() throws Throwable {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);

        assertFalse(webViewClient.onScaleChangedCalled());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.zoomIn();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(webViewClient.onScaleChangedCalled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCertificate",
            args = {SslCertificate.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCertificate",
            args = {}
        )
    })
    public void testAccessCertificate() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView = new WebView(getActivity());
                getActivity().setContentView(mWebView);
            }
        });
        getInstrumentation().waitForIdleSync();

        // need the client to handle error
        mWebView.setWebViewClient(new MockWebViewClient());

        mWebView.setCertificate(null);
        startWebServer(true);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        // attempt to load the url.
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return mWebView.getCertificate() != null;
            }
        }.run();
        SslCertificate cert = mWebView.getCertificate();
        assertNotNull(cert);
        assertEquals("Android", cert.getIssuedTo().getUName());
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "WebViewClient.onReceivedSslError() is hidden, cannot store SSL preferences.",
        method = "clearSslPreferences",
        args = {}
    )
    public void testClearSslPreferences() {
        mWebView.clearSslPreferences();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestChildRectangleOnScreen",
        args = {View.class, Rect.class, boolean.class}
    )
    public void testRequestChildRectangleOnScreen() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        final int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;width:" + dimension + "px\">&nbsp;</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        getInstrumentation().waitForIdleSync();

        runTestOnUiThread(new Runnable() {
            public void run() {
                int origX = mWebView.getScrollX();
                int origY = mWebView.getScrollY();

                int half = dimension / 2;
                Rect rect = new Rect(half, half, half + 1, half + 1);
                assertTrue(mWebView.requestChildRectangleOnScreen(mWebView, rect, true));
                assertTrue(mWebView.getScrollX() > origX);
                assertTrue(mWebView.getScrollY() > origY);
            }
        });
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDownloadListener",
            args = {DownloadListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "requestFocus",
            args = {int.class, Rect.class}
        )
    })
    @ToBeFixed(explanation="Mime type and content length passed to listener are incorrect.")
    public void testSetDownloadListener() throws Throwable {
        final String mimeType = "application/octet-stream";
        final int length = 100;
        final MyDownloadListener listener = new MyDownloadListener();

        startWebServer(false);
        String url = mWebServer.getBinaryUrl(mimeType, length);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setDownloadListener(listener);
        mWebView.loadData("<html><body><a href=\"" + url + "\">link</a></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        // focus on the link
        runTestOnUiThread(new Runnable() {
            public void run() {
                assertTrue(mWebView.requestFocus(View.FOCUS_DOWN, null));
            }
        });
        getInstrumentation().waitForIdleSync();
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return listener.called;
            }
        }.run();
        assertEquals(url, listener.url);
        assertTrue(listener.contentDisposition.contains("test.bin"));
        // ToBeFixed: uncomment the following tests after fixing the framework
        // assertEquals(mimeType, listener.mimeType);
        // assertEquals(length, listener.contentLength);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setLayoutParams() is incomplete.")
    @UiThreadTest
    public void testSetLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, 800);
        mWebView.setLayoutParams(params);
        assertSame(params, mWebView.getLayoutParams());
    }

    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "No documentation",
        method = "setMapTrackballToArrowKeys",
        args = {boolean.class}
    )
    public void testSetMapTrackballToArrowKeys() {
        mWebView.setMapTrackballToArrowKeys(true);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setNetworkAvailable",
        args = {boolean.class}
    )
    public void testSetNetworkAvailable() throws Exception {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.NETWORK_STATE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        assertEquals("ONLINE", mWebView.getTitle());

        mWebView.setNetworkAvailable(false);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("OFFLINE", mWebView.getTitle());

        mWebView.setNetworkAvailable(true);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("ONLINE", mWebView.getTitle());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setWebChromeClient",
        args = {WebChromeClient.class}
    )
    public void testSetWebChromeClient() throws Throwable {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);

        assertFalse(webChromeClient.onProgressChangedCalled());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);

        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.onProgressChangedCalled();
            }
        }.run();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onAttachedToWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDetachedFromWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onChildViewAdded",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onChildViewRemoved",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDraw",
            args = {Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onFocusChanged",
            args = {boolean.class, int.class, Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onGlobalFocusChanged",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onScrollChanged",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSizeChanged",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeScroll",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeHorizontalScrollRange",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeVerticalScrollRange",
            args = {}
        )
    })
    public void testInternals() {
        // Do not test these APIs. They are implementation details.
    }

    private static class MockWebViewClient extends WebViewClient {
        private boolean mOnScaleChangedCalled = false;

        public boolean onScaleChangedCalled() {
            return mOnScaleChangedCalled;
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            mOnScaleChangedCalled = true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }
    }

    private static class MockWebChromeClient extends WebChromeClient {
        private boolean mOnProgressChanged = false;

        public boolean onProgressChangedCalled() {
            return mOnProgressChanged;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mOnProgressChanged = true;
        }
    }

    private static class HrefCheckHandler extends Handler {
        private boolean mHadRecieved;

        private String mResultUrl;

        public HrefCheckHandler(Looper looper) {
            super(looper);
        }

        public boolean hasCalledHandleMessage() {
            return mHadRecieved;
        }

        public String getResultUrl() {
            return mResultUrl;
        }

        public void reset(){
            mResultUrl = null;
            mHadRecieved = false;
        }

        @Override
        public void handleMessage(Message msg) {
            mHadRecieved = true;
            mResultUrl = msg.getData().getString("url");
        }
    }

    private static class DocumentHasImageCheckHandler extends Handler {
        private boolean mReceived;

        private int mMsgArg1;

        public DocumentHasImageCheckHandler(Looper looper) {
            super(looper);
        }

        public boolean hasCalledHandleMessage() {
            return mReceived;
        }

        public int getMsgArg1() {
            return mMsgArg1;
        }

        public void reset(){
            mMsgArg1 = -1;
            mReceived = false;
        }

        @Override
        public void handleMessage(Message msg) {
            mReceived = true;
            mMsgArg1 = msg.arg1;
        }
    };

    private void findNextOnUiThread(final boolean forward) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.findNext(forward);
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    private void moveFocusDown() throws Throwable {
        // send down key and wait for idle
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        // waiting for idle isn't always sufficient for the key to be fully processed
        Thread.sleep(500);
    }

    private boolean pageDownOnUiThread(final boolean bottom) throws Throwable {
        PageDownRunner runner = new PageDownRunner(bottom);
        runTestOnUiThread(runner);
        getInstrumentation().waitForIdleSync();
        return runner.mResult;
    }

    private class PageDownRunner implements Runnable {
        private boolean mResult, mBottom;

        public PageDownRunner(boolean bottom) {
            mBottom = bottom;
        }

        public void run() {
            mResult = mWebView.pageDown(mBottom);
        }
    }

    private boolean pageUpOnUiThread(final boolean top) throws Throwable {
        PageUpRunner runner = new PageUpRunner(top);
        runTestOnUiThread(runner);
        getInstrumentation().waitForIdleSync();
        return runner.mResult;
    }

    private class PageUpRunner implements Runnable {
        private boolean mResult, mTop;

        public PageUpRunner(boolean top) {
            this.mTop = top;
        }

        public void run() {
            mResult = mWebView.pageUp(mTop);
        }
    }

    private void delayedCheckStopScrolling() {
        new DelayedCheck() {
            private int scrollY = mWebView.getScrollY();

            @Override
            protected boolean check() {
                if (scrollY == mWebView.getScrollY()){
                    return true;
                } else {
                    scrollY = mWebView.getScrollY();
                    return false;
                }
            }
        }.run();
    }

    private void delayedCheckWebBackForwardList(final String currUrl, final int currIndex,
            final int size) {
        new DelayedCheck() {
            @Override
            protected boolean check() {
                WebBackForwardList list = mWebView.copyBackForwardList();
                return checkWebBackForwardList(list, currUrl, currIndex, size);
            }
        }.run();
    }

    private boolean checkWebBackForwardList(WebBackForwardList list, String currUrl,
            int currIndex, int size) {
        return (list != null)
                && (list.getSize() == size)
                && (list.getCurrentIndex() == currIndex)
                && list.getItemAtIndex(currIndex).getUrl().equals(currUrl);
    }

    private void assertGoBackOrForwardBySteps(boolean expected, int steps) {
        // skip if steps equals to 0
        if (steps == 0)
            return;

        int start = steps > 0 ? 1 : steps;
        int end = steps > 0 ? steps : -1;

        // check all the steps in the history
        for (int i = start; i <= end; i++) {
            assertEquals(expected, mWebView.canGoBackOrForward(i));

            // shortcut methods for one step
            if (i == 1) {
                assertEquals(expected, mWebView.canGoForward());
            } else if (i == -1) {
                assertEquals(expected, mWebView.canGoBack());
            }
        }
    }

    private void assertBitmapFillWithColor(Bitmap bitmap, int color) {
        for (int i = 0; i < bitmap.getWidth(); i ++)
            for (int j = 0; j < bitmap.getHeight(); j ++) {
                assertEquals(color, bitmap.getPixel(i, j));
            }
    }

    // Find b1 inside b2
    private boolean checkBitmapInsideAnother(Bitmap b1, Bitmap b2) {
        int w = b1.getWidth();
        int h = b1.getHeight();

        for (int i = 0; i < (b2.getWidth()-w+1); i++) {
            for (int j = 0; j < (b2.getHeight()-h+1); j++) {
                if (checkBitmapInsideAnother(b1, b2, i, j))
                    return true;
            }
        }
        return false;
    }

    private boolean comparePixel(int p1, int p2, int maxError) {
        int err;
        err = Math.abs(((p1&0xff000000)>>>24) - ((p2&0xff000000)>>>24));
        if (err > maxError)
            return false;

        err = Math.abs(((p1&0x00ff0000)>>>16) - ((p2&0x00ff0000)>>>16));
        if (err > maxError)
            return false;

        err = Math.abs(((p1&0x0000ff00)>>>8) - ((p2&0x0000ff00)>>>8));
        if (err > maxError)
            return false;

        err = Math.abs(((p1&0x000000ff)>>>0) - ((p2&0x000000ff)>>>0));
        if (err > maxError)
            return false;

        return true;
    }

    private boolean checkBitmapInsideAnother(Bitmap b1, Bitmap b2, int x, int y) {
        for (int i = 0; i < b1.getWidth(); i++)
            for (int j = 0; j < b1.getHeight(); j++) {
                if (!comparePixel(b1.getPixel(i, j), b2.getPixel(x + i, y + j), 10)) {
                    return false;
                }
            }
        return true;
    }

    private void assertLoadUrlSuccessfully(WebView webView, String url)
            throws InterruptedException {
        webView.loadUrl(url);
        waitForLoadComplete(webView, TEST_TIMEOUT);
    }

    private void waitForLoadComplete(final WebView webView, long timeout)
            throws InterruptedException {
        new DelayedCheck(timeout) {
            @Override
            protected boolean check() {
                return webView.getProgress() == 100;
            }
        }.run();
        Thread.sleep(TIME_FOR_LAYOUT);
    }

    private final class DummyJavaScriptInterface {
        private boolean mTitleChanged;

        private boolean hasChangedTitle() {
            return mTitleChanged;
        }

        public void onLoad(String oldTitle) {
            mWebView.getHandler().post(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:changeTitle(\"new title\")");
                    mTitleChanged = true;
                }
            });
        }
    }

    private final class MyDownloadListener implements DownloadListener {
        public String url;
        public String mimeType;
        public long contentLength;
        public String contentDisposition;
        public boolean called;

        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                String mimetype, long contentLength) {
            this.called = true;
            this.url = url;
            this.mimeType = mimetype;
            this.contentLength = contentLength;
            this.contentDisposition = contentDisposition;
        }
    }

    private static class MyPictureListener implements PictureListener {
        public int callCount;
        public WebView webView;
        public Picture picture;

        public void onNewPicture(WebView view, Picture picture) {
            this.callCount += 1;
            this.webView = view;
            this.picture = picture;
        }
    }
}
