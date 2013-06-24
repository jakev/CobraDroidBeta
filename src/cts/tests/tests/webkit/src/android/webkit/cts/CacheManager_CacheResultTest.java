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

import org.apache.http.HttpStatus;
import org.apache.http.impl.cookie.DateUtils;

import android.test.ActivityInstrumentationTestCase2;
import android.view.animation.cts.DelayedCheck;
import android.webkit.CacheManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.CacheManager.CacheResult;

import java.io.File;
import java.io.InputStream;

@TestTargetClass(android.webkit.CacheManager.CacheResult.class)
public class CacheManager_CacheResultTest
        extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long NETWORK_OPERATION_DELAY = 10000l;

    private WebView mWebView;
    private CtsTestServer mWebServer;

    public CacheManager_CacheResultTest() {
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
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContentLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getETag",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastModified",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalPath",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMimeType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOutputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpires",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHttpStatusCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEncoding",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEncoding",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInputStream",
            args = {InputStream.class}
        )
    })
    public void testCacheResult() throws Exception {
        final long validity = 5 * 50 * 1000; // 5 min
        final long age = 30 * 60 * 1000; // 30 min
        final long tolerance = 5 * 1000; // 5s

        mWebServer = new CtsTestServer(getActivity());
        final String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebServer.setDocumentAge(age);
        mWebServer.setDocumentValidity(validity);

        mWebView.clearCache(true);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                CacheResult result =
                    CacheManager.getCacheFile(url, null);
                return result == null;
            }
        }.run();
        final long time = System.currentTimeMillis();
        loadUrl(url);
        CacheResult result = CacheManager.getCacheFile(url, null);
        assertNotNull(result);
        assertNotNull(result.getInputStream());
        assertTrue(result.getContentLength() > 0);
        assertNull(result.getETag());
        assertEquals(time - age,
                DateUtils.parseDate(result.getLastModified()).getTime(), tolerance);
        File file = new File(CacheManager.getCacheFileBaseDir().getPath(), result.getLocalPath());
        assertTrue(file.exists());
        assertNull(result.getLocation());
        assertEquals("text/html", result.getMimeType());
        assertNull(result.getOutputStream());
        assertEquals(time + validity, result.getExpires(), tolerance);
        assertEquals(HttpStatus.SC_OK, result.getHttpStatusCode());
        assertNotNull(result.getEncoding());

        result.setEncoding("iso-8859-1");
        assertEquals("iso-8859-1", result.getEncoding());

        result.setInputStream(null);
        assertNull(result.getInputStream());
    }

    private void loadUrl(String url){
        mWebView.loadUrl(url);
        // check whether loadURL successfully
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
        assertEquals(100, mWebView.getProgress());
    }
}
