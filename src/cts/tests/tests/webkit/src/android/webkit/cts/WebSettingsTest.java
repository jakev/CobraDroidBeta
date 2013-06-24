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

import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.animation.cts.DelayedCheck;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.TextSize;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests for {@link android.webkit.WebSettings}
 */
@TestTargetClass(android.webkit.WebSettings.class)
public class WebSettingsTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {

    private static final String LOG_TAG = "WebSettingsTest";

    private WebView mWebView;
    private WebSettings mSettings;
    private CtsTestServer mWebServer;

    public WebSettingsTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();

        // Set a web chrome client in order to receive progress updates.
        mWebView.setWebChromeClient(new WebChromeClient());

        mSettings = mWebView.getSettings();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        // clear the cache to prevent side effects
        mWebView.clearCache(true);
        super.tearDown();
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUserAgentString",
            args = {}
    )
    /**
     * Verifies that the default user agent string follows the format defined in Android
     * compatibility definition:
     * <p/>
     * Mozilla/5.0 (Linux; U; Android <version>; <language>-<country>; <devicemodel>;
     * Build/<buildID>) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1
     */
    public void testUserAgentString_default() {
        final String actualUserAgentString = mSettings.getUserAgentString();
        Log.i(LOG_TAG, String.format("Checking user agent string %s", actualUserAgentString));
        final String patternString = "Mozilla/5\\.0 \\(Linux; U; Android (.+); (\\w+)-(\\w+);" +
            " (.+) Build/(.+)\\) AppleWebKit/533\\.1 \\(KHTML, like Gecko\\) Version/4\\.0" +
            "( Mobile)? Safari/533\\.1";
        Log.i(LOG_TAG, String.format("Trying to match pattern %s", patternString));
        final Pattern userAgentExpr = Pattern.compile(patternString);
        Matcher patternMatcher = userAgentExpr.matcher(actualUserAgentString);
        assertTrue(String.format("User agent string did not match expected pattern. \nExpected " +
                        "pattern:\n%s\nActual:\n%s", patternString, actualUserAgentString),
                        patternMatcher.find());
        assertEquals(Build.VERSION.RELEASE, patternMatcher.group(1));
        Locale currentLocale = Locale.getDefault();
        assertEquals(currentLocale.getLanguage().toLowerCase(), patternMatcher.group(2));
        assertEquals(currentLocale.getCountry().toLowerCase(), patternMatcher.group(3));
        assertEquals(Build.MODEL, patternMatcher.group(4));
        assertEquals(Build.ID, patternMatcher.group(5));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUserAgentString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setUserAgentString",
            args = {String.class}
        )
    })
    public void testAccessUserAgentString() throws Exception {
        startWebServer();
        String url = mWebServer.getUserAgentUrl();

        String defaultUserAgent = mSettings.getUserAgentString();
        assertNotNull(defaultUserAgent);
        loadUrl(url);
        assertEquals(defaultUserAgent, mWebView.getTitle());

        // attempting to set a null string has no effect
        mSettings.setUserAgentString(null);
        assertEquals(defaultUserAgent, mSettings.getUserAgentString());
        loadUrl(url);
        assertEquals(defaultUserAgent, mWebView.getTitle());

        // attempting to set an empty string has no effect
        mSettings.setUserAgentString("");
        assertEquals(defaultUserAgent, mSettings.getUserAgentString());
        loadUrl(url);
        assertEquals(defaultUserAgent, mWebView.getTitle());

        String customUserAgent = "Cts/test";
        mSettings.setUserAgentString(customUserAgent);
        assertEquals(customUserAgent, mSettings.getUserAgentString());
        loadUrl(url);
        assertEquals(customUserAgent, mWebView.getTitle());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUserAgent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setUserAgent",
            args = {int.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testAccessUserAgent() throws Exception {
        startWebServer();
        String url = mWebServer.getUserAgentUrl();

        mSettings.setUserAgent(1);
        assertEquals(1, mSettings.getUserAgent());
        loadUrl(url);
        String userAgent1 = mWebView.getTitle();
        assertNotNull(userAgent1);

        mSettings.setUserAgent(3);
        assertEquals(1, mSettings.getUserAgent());
        loadUrl(url);
        assertEquals(userAgent1, mWebView.getTitle());

        mSettings.setUserAgent(2);
        assertEquals(2, mSettings.getUserAgent());
        loadUrl(url);
        String userAgent2 = mWebView.getTitle();
        assertNotNull(userAgent2);

        mSettings.setUserAgent(3);
        assertEquals(2, mSettings.getUserAgent());
        loadUrl(url);
        assertEquals(userAgent2, mWebView.getTitle());

        mSettings.setUserAgent(0);
        assertEquals(0, mSettings.getUserAgent());
        loadUrl(url);
        String userAgent0 = mWebView.getTitle();
        assertNotNull(userAgent0);

        final String customUserAgent = "Cts/Test";
        mSettings.setUserAgentString(customUserAgent);
        assertEquals(-1, mSettings.getUserAgent());
        loadUrl(url);
        assertEquals(customUserAgent, mWebView.getTitle());
    }


    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAllowFileAccess",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setAllowFileAccess",
            args = {boolean.class}
        )
    })
    @ToBeFixed(explanation = "Cannot block file access using setAllowFileAccess(false)")
    public void testAccessAllowFileAccess() {
        assertTrue(mSettings.getAllowFileAccess());

        String fileUrl = TestHtmlConstants.getFileUrl(TestHtmlConstants.HELLO_WORLD_URL);
        loadUrl(fileUrl);
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, mWebView.getTitle());

        fileUrl = TestHtmlConstants.getFileUrl(TestHtmlConstants.BR_TAG_URL);
        mSettings.setAllowFileAccess(false);
        assertFalse(mSettings.getAllowFileAccess());
        loadUrl(fileUrl);
        // direct file:// access still works with access disabled
        assertEquals(TestHtmlConstants.BR_TAG_TITLE, mWebView.getTitle());

        // ToBeFixed: How does this API prevent file access?
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBlockNetworkImage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBlockNetworkImage",
            args = {boolean.class}
        )
    })
    @ToBeFixed(explanation = "Implementation does not work as expected.")
    public void testAccessBlockNetworkImage() throws Exception {
        String url = TestHtmlConstants.EMBEDDED_IMG_URL;
        final String ext = MimeTypeMap.getFileExtensionFromUrl(url);

        mWebView.clearCache(true);
        assertFalse(mSettings.getBlockNetworkImage());
        assertTrue(mSettings.getLoadsImagesAutomatically());
        loadAssetUrl(url);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return !mWebServer.getLastRequestUrl().endsWith(ext);
            }
        }.run();

        /* ToBeFixed: Uncomment after fixing the framework
        mWebView.clearCache(true);
        mSettings.setBlockNetworkImage(true);
        assertTrue(mSettings.getBlockNetworkImage());
        loadUrl(url);
        assertTrue(mWebServer.getLastRequestUrl().endsWith(ext));
        */
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCacheMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCacheMode",
            args = {int.class}
        )
    })
    public void testAccessCacheMode() throws Exception {
        assertEquals(WebSettings.LOAD_DEFAULT, mSettings.getCacheMode());

        mSettings.setCacheMode(WebSettings.LOAD_NORMAL);
        assertEquals(WebSettings.LOAD_NORMAL, mSettings.getCacheMode());

        mSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        assertEquals(WebSettings.LOAD_CACHE_ELSE_NETWORK, mSettings.getCacheMode());
        loadAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        int firstFetch = mWebServer.getRequestCount();
        loadAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertEquals(firstFetch, mWebServer.getRequestCount());

        mSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        assertEquals(WebSettings.LOAD_NO_CACHE, mSettings.getCacheMode());
        loadAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        int secondFetch = mWebServer.getRequestCount();
        loadAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        int thirdFetch = mWebServer.getRequestCount();
        assertTrue(firstFetch < secondFetch);
        assertTrue(secondFetch < thirdFetch);

        mSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        assertEquals(WebSettings.LOAD_CACHE_ONLY, mSettings.getCacheMode());
        loadAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertEquals(thirdFetch, mWebServer.getRequestCount());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCursiveFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setCursiveFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessCursiveFontFamily() throws Exception {
        assertNotNull(mSettings.getCursiveFontFamily());

        String newCusiveFamily = "Apple Chancery";
        mSettings.setCursiveFontFamily(newCusiveFamily);
        assertEquals(newCusiveFamily, mSettings.getCursiveFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFantasyFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setFantasyFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessFantasyFontFamily() {
        assertNotNull(mSettings.getFantasyFontFamily());

        String newFantasyFamily = "Papyrus";
        mSettings.setFantasyFontFamily(newFantasyFamily);
        assertEquals(newFantasyFamily, mSettings.getFantasyFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFixedFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setFixedFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessFixedFontFamily() {
        assertNotNull(mSettings.getFixedFontFamily());

        String newFixedFamily = "Courier";
        mSettings.setFixedFontFamily(newFixedFamily);
        assertEquals(newFixedFamily, mSettings.getFixedFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSansSerifFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setSansSerifFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSansSerifFontFamily() {
        assertNotNull(mSettings.getSansSerifFontFamily());

        String newFixedFamily = "Verdana";
        mSettings.setSansSerifFontFamily(newFixedFamily);
        assertEquals(newFixedFamily, mSettings.getSansSerifFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSerifFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setSerifFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSerifFontFamily() {
        assertNotNull(mSettings.getSerifFontFamily());

        String newSerifFamily = "Times";
        mSettings.setSerifFontFamily(newSerifFamily);
        assertEquals(newSerifFamily, mSettings.getSerifFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStandardFontFamily",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setStandardFontFamily",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessStandardFontFamily() {
        assertNotNull(mSettings.getStandardFontFamily());

        String newStandardFamily = "Times";
        mSettings.setStandardFontFamily(newStandardFamily);
        assertEquals(newStandardFamily, mSettings.getStandardFontFamily());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultFontSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setDefaultFontSize",
            args = {int.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessDefaultFontSize() {
        int defaultSize = mSettings.getDefaultFontSize();
        assertTrue(defaultSize > 0);

        mSettings.setDefaultFontSize(1000);
        int maxSize = mSettings.getDefaultFontSize();
        // cannot check exact size set, since the implementation caps it at an arbitrary limit
        assertTrue(maxSize > defaultSize);

        mSettings.setDefaultFontSize(-10);
        int minSize = mSettings.getDefaultFontSize();
        assertTrue(minSize > 0);
        assertTrue(minSize < maxSize);

        mSettings.setDefaultFontSize(10);
        assertEquals(10, mSettings.getDefaultFontSize());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultFixedFontSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setDefaultFixedFontSize",
            args = {int.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessDefaultFixedFontSize() {
        int defaultSize = mSettings.getDefaultFixedFontSize();
        assertTrue(defaultSize > 0);

        mSettings.setDefaultFixedFontSize(1000);
        int maxSize = mSettings.getDefaultFixedFontSize();
        // cannot check exact size, since the implementation caps it at an arbitrary limit
        assertTrue(maxSize > defaultSize);

        mSettings.setDefaultFixedFontSize(-10);
        int minSize = mSettings.getDefaultFixedFontSize();
        assertTrue(minSize > 0);
        assertTrue(minSize < maxSize);

        mSettings.setDefaultFixedFontSize(10);
        assertEquals(10, mSettings.getDefaultFixedFontSize());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultTextEncodingName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setDefaultTextEncodingName",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessDefaultTextEncodingName() {
        assertNotNull(mSettings.getDefaultTextEncodingName());

        String newEncodingName = "iso-8859-1";
        mSettings.setDefaultTextEncodingName(newEncodingName);
        assertEquals(newEncodingName, mSettings.getDefaultTextEncodingName());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJavaScriptCanOpenWindowsAutomatically",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setJavaScriptCanOpenWindowsAutomatically",
            args = {boolean.class}
        )
    })
    public void testAccessJavaScriptCanOpenWindowsAutomatically() throws Exception {
        mWebView.setWebViewClient(new WebViewClient());
        mSettings.setJavaScriptEnabled(true);

        mSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        assertFalse(mSettings.getJavaScriptCanOpenWindowsAutomatically());
        loadAssetUrl(TestHtmlConstants.POPUP_URL);
        new DelayedCheck(10000) {
            protected boolean check() {
                String title = mWebView.getTitle();
                return title != null && title.length() > 0;
            }
        }.run();
        assertEquals("Popup blocked", mWebView.getTitle());

        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        assertTrue(mSettings.getJavaScriptCanOpenWindowsAutomatically());
        loadAssetUrl(TestHtmlConstants.POPUP_URL);
        new DelayedCheck(10000) {
            protected boolean check() {
                String title = mWebView.getTitle();
                return title != null && title.length() > 0;
            }
        }.run();
        assertEquals("Popup allowed", mWebView.getTitle());
}

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJavaScriptEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setJavaScriptEnabled",
            args = {boolean.class}
        )
    })
    public void testAccessJavaScriptEnabled() throws Exception {
        mSettings.setJavaScriptEnabled(true);
        assertTrue(mSettings.getJavaScriptEnabled());
        loadAssetUrl(TestHtmlConstants.JAVASCRIPT_URL);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return mWebView.getTitle() != null;
            }
        }.run();
        assertEquals("javascript on", mWebView.getTitle());

        mSettings.setJavaScriptEnabled(false);
        assertFalse(mSettings.getJavaScriptEnabled());
        loadAssetUrl(TestHtmlConstants.JAVASCRIPT_URL);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return mWebView.getTitle() != null;
            }
        }.run();
        assertEquals("javascript off", mWebView.getTitle());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutAlgorithm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setLayoutAlgorithm",
            args = {LayoutAlgorithm.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessLayoutAlgorithm() {
        assertEquals(WebSettings.LayoutAlgorithm.NARROW_COLUMNS, mSettings.getLayoutAlgorithm());

        mSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        assertEquals(WebSettings.LayoutAlgorithm.NORMAL, mSettings.getLayoutAlgorithm());

        mSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        assertEquals(WebSettings.LayoutAlgorithm.SINGLE_COLUMN, mSettings.getLayoutAlgorithm());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLightTouchEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setLightTouchEnabled",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessLightTouchEnabled() {
        assertFalse(mSettings.getLightTouchEnabled());

        mSettings.setLightTouchEnabled(true);
        assertTrue(mSettings.getLightTouchEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLoadsImagesAutomatically",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setLoadsImagesAutomatically",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessLoadsImagesAutomatically() throws Exception {
        mWebView.clearCache(true);
        assertTrue(mSettings.getLoadsImagesAutomatically());
        String url = TestHtmlConstants.EMBEDDED_IMG_URL;
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        loadAssetUrl(url);
        Thread.sleep(1000);
        assertFalse(mWebServer.getLastRequestUrl().endsWith(ext));

        mWebView.clearCache(true);
        mSettings.setLoadsImagesAutomatically(false);
        assertFalse(mSettings.getLoadsImagesAutomatically());
        loadAssetUrl(url);
        Thread.sleep(1000);
        assertTrue(mWebServer.getLastRequestUrl().endsWith(ext));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMinimumFontSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setMinimumFontSize",
            args = {int.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessMinimumFontSize() {
        assertEquals(8, mSettings.getMinimumFontSize());

        mSettings.setMinimumFontSize(100);
        assertEquals(72, mSettings.getMinimumFontSize());

        mSettings.setMinimumFontSize(-10);
        assertEquals(1, mSettings.getMinimumFontSize());

        mSettings.setMinimumFontSize(10);
        assertEquals(10, mSettings.getMinimumFontSize());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMinimumLogicalFontSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setMinimumLogicalFontSize",
            args = {int.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessMinimumLogicalFontSize() {
        assertEquals(8, mSettings.getMinimumLogicalFontSize());

        mSettings.setMinimumLogicalFontSize(100);
        assertEquals(72, mSettings.getMinimumLogicalFontSize());

        mSettings.setMinimumLogicalFontSize(-10);
        assertEquals(1, mSettings.getMinimumLogicalFontSize());

        mSettings.setMinimumLogicalFontSize(10);
        assertEquals(10, mSettings.getMinimumLogicalFontSize());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNavDump",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setNavDump",
            args = {boolean.class}
        )
    })
    @ToBeFixed(explanation = "NavDump feature is not documented")
    public void testAccessNavDump() {
        assertFalse(mSettings.getNavDump());

        mSettings.setNavDump(true);
        assertTrue(mSettings.getNavDump());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPluginsEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setPluginsEnabled",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessPluginsEnabled() {
        assertFalse(mSettings.getPluginsEnabled());

        mSettings.setPluginsEnabled(true);
        assertTrue(mSettings.getPluginsEnabled());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPluginsPath",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setPluginsPath",
            args = {String.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessPluginsPath() {
        assertNotNull(mSettings.getPluginsPath());

        String pluginPath = "pluginPath";
        mSettings.setPluginsPath(pluginPath);
        // plugin path always empty
        assertEquals("", mSettings.getPluginsPath());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSaveFormData",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setSaveFormData",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSaveFormData() {
        assertTrue(mSettings.getSaveFormData());

        mSettings.setSaveFormData(false);
        assertFalse(mSettings.getSaveFormData());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSavePassword",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setSavePassword",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSavePassword() {
        assertTrue(mSettings.getSavePassword());

        mSettings.setSavePassword(false);
        assertFalse(mSettings.getSavePassword());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setTextSize",
            args = {TextSize.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessTextSize() {
        assertEquals(TextSize.NORMAL, mSettings.getTextSize());

        mSettings.setTextSize(TextSize.LARGER);
        assertEquals(TextSize.LARGER, mSettings.getTextSize());

        mSettings.setTextSize(TextSize.LARGEST);
        assertEquals(TextSize.LARGEST, mSettings.getTextSize());

        mSettings.setTextSize(TextSize.SMALLER);
        assertEquals(TextSize.SMALLER, mSettings.getTextSize());

        mSettings.setTextSize(TextSize.SMALLEST);
        assertEquals(TextSize.SMALLEST, mSettings.getTextSize());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUseDoubleTree",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setUseDoubleTree",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods take effect")
    public void testAccessUseDoubleTree() {
        assertFalse(mSettings.getUseDoubleTree());

        mSettings.setUseDoubleTree(true);
        // setUseDoubleTree is a no-op
        assertFalse(mSettings.getUseDoubleTree());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUseWideViewPort",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setUseWideViewPort",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessUseWideViewPort() {
        assertFalse(mSettings.getUseWideViewPort());

        mSettings.setUseWideViewPort(true);
        assertTrue(mSettings.getUseWideViewPort());
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setNeedInitialFocus",
        args = {boolean.class}
    )
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testSetNeedInitialFocus() {
        mSettings.setNeedInitialFocus(false);

        mSettings.setNeedInitialFocus(true);
    }

    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setRenderPriority",
        args = {RenderPriority.class}
    )
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods take effect")
    public void testSetRenderPriority() {
        mSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        mSettings.setRenderPriority(WebSettings.RenderPriority.LOW);

        mSettings.setRenderPriority(WebSettings.RenderPriority.NORMAL);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "supportMultipleWindows",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setSupportMultipleWindows",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSupportMultipleWindows() {
        assertFalse(mSettings.supportMultipleWindows());

        mSettings.setSupportMultipleWindows(true);
        assertTrue(mSettings.supportMultipleWindows());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "supportZoom",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSupportZoom",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessSupportZoom() {
        assertTrue(mSettings.supportZoom());

        mSettings.setSupportZoom(false);
        assertFalse(mSettings.supportZoom());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBuiltInZoomControls",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "setBuiltInZoomControls",
            args = {boolean.class}
        )
    })
    @ToBeFixed( bug = "1665811", explanation = "Can not check whether methods " +
            "take effect by automatic testing")
    public void testAccessBuiltInZoomControls() {
        assertFalse(mSettings.getBuiltInZoomControls());

        mSettings.setBuiltInZoomControls(true);
        assertTrue(mSettings.getBuiltInZoomControls());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAppCacheEnabled",
            args = {}
        )
    })
    public void testSetAppCacheEnabled() throws Exception {
        // Tests that when AppCache is enabled and used, but the database path
        // is not set or is set to an inaccessible path, the WebView does not crash.
        startWebServer();
        String url = mWebServer.getAppCacheUrl();
        mSettings.setAppCacheEnabled(true);
        mSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(url);
        new DelayedCheck(10000) {
            protected boolean check() {
                return mWebView.getTitle().equals("Done");
            }
        }.run();

        mSettings.setAppCachePath("/data/foo");
        mWebView.loadUrl(url);
        new DelayedCheck(10000) {
            protected boolean check() {
                return mWebView.getTitle().equals("Done");
            }
        }.run();
    }

    /**
     * Starts the internal web server. The server will be shut down automatically
     * during tearDown().
     *
     * @throws Exception
     */
    private void startWebServer() throws Exception {
        assertNull(mWebServer);
        mWebServer = new CtsTestServer(getActivity(), false);
    }

    /**
     * Load the given asset from the internal web server. Starts the server if
     * it is not already running.
     *
     * @param asset The name of the asset to load.
     * @throws Exception
     */
    private void loadAssetUrl(String asset) throws Exception {
        if (mWebServer == null) {
            startWebServer();
        }
        String url = mWebServer.getAssetUrl(asset);
        loadUrl(url);
    }

    /**
     * Fully load the page at the given URL.
     *
     * @param url The URL of the page to load.
     */
    private void loadUrl(String url) {
        mWebView.loadUrl(url);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
        assertEquals(100, mWebView.getProgress());
    }
}
