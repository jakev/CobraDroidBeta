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

import android.test.ActivityInstrumentationTestCase2;
import android.view.animation.cts.DelayedCheck;
import android.webkit.CacheManager;
import android.webkit.WebView;
import android.webkit.CacheManager.CacheResult;

import java.util.Map;

@TestTargetClass(android.webkit.CacheManager.class)
public class CacheManagerTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long CACHEMANAGER_INIT_TIMEOUT = 5000l;
    private static final long NETWORK_OPERATION_DELAY = 10000l;

    private WebView mWebView;
    private CtsTestServer mWebServer;

    public CacheManagerTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCacheFileBaseDir",
        args = {}
    )
    public void testGetCacheFileBaseDir() {
        assertTrue(CacheManager.getCacheFileBaseDir().exists());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startCacheTransaction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "endCacheTransaction",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc of these two methods doesn't exist.")
    public void testCacheTransaction() {
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCacheFile",
            args = {String.class, Map.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "saveCacheFile",
            args = {String.class, CacheResult.class}
        )
    })
    public void testCacheFile() throws Exception {
        mWebServer = new CtsTestServer(getActivity());
        final String url = mWebServer.getAssetUrl(TestHtmlConstants.EMBEDDED_IMG_URL);

        // Wait for CacheManager#init() finish.
        new DelayedCheck(CACHEMANAGER_INIT_TIMEOUT) {
            @Override
            protected boolean check() {
                return CacheManager.getCacheFileBaseDir() != null;
            }
        }.run();

        mWebView.clearCache(true);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                CacheResult result = CacheManager.getCacheFile(url, null);
                return result == null;
            }
        }.run();

        loadUrl(url);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                CacheResult result = CacheManager.getCacheFile(url, null);
                return result != null;
            }
        }.run();

        // Can not test saveCacheFile(), because the output stream is null and
        // saveCacheFile() will throw a NullPointerException.  There is no
        // public API to set the output stream.
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "cacheDisabled",
        args = {}
    )
    public void testCacheDisabled() {
        assertFalse(CacheManager.cacheDisabled());

        // Because setCacheDisabled is package private, we can not call it.
        // cacheDisabled() always return false. How to let it return true?
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
