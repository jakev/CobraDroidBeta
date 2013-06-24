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

import android.test.AndroidTestCase;
import android.webkit.MimeTypeMap;

@TestTargetClass(android.webkit.MimeTypeMap.class)
public class MimeTypeMapTest extends AndroidTestCase {

    private MimeTypeMap mMimeTypeMap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mMimeTypeMap = MimeTypeMap.getSingleton();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFileExtensionFromUrl",
            args = {String.class}
        )
    })
    @ToBeFixed(explanation="Returns part of the domain name if the optional URL path is missing")
    public void testGetFileExtensionFromUrl() {
        assertEquals("html", MimeTypeMap.getFileExtensionFromUrl("http://localhost/index.html"));
        assertEquals("html", MimeTypeMap.getFileExtensionFromUrl("http://host/x.html?x=y"));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("http://www.example.com/"));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("https://example.com/foo"));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl(null));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl(""));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("http://abc/&%$.()*"));

        // ToBeFixed: Uncomment the following line after fixing the implementation
        //assertEquals("", MimeTypeMap.getFileExtensionFromUrl("http://www.example.com"));
}

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasMimeType",
            args = {String.class}
        )
    })
    public void testHasMimeType() {
        assertTrue(mMimeTypeMap.hasMimeType("audio/mpeg"));
        assertTrue(mMimeTypeMap.hasMimeType("text/plain"));

        assertFalse(mMimeTypeMap.hasMimeType("some_random_string"));

        assertFalse(mMimeTypeMap.hasMimeType(""));
        assertFalse(mMimeTypeMap.hasMimeType(null));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMimeTypeFromExtension",
            args = {String.class}
        )
    })
    public void testGetMimeTypeFromExtension() {
        assertEquals("audio/mpeg", mMimeTypeMap.getMimeTypeFromExtension("mp3"));
        assertEquals("application/zip", mMimeTypeMap.getMimeTypeFromExtension("zip"));

        assertNull(mMimeTypeMap.getMimeTypeFromExtension("some_random_string"));

        assertNull(mMimeTypeMap.getMimeTypeFromExtension(null));
        assertNull(mMimeTypeMap.getMimeTypeFromExtension(""));
}

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasExtension",
            args = {String.class}
        )
    })
    public void testHasExtension() {
        assertTrue(mMimeTypeMap.hasExtension("mp3"));
        assertTrue(mMimeTypeMap.hasExtension("zip"));

        assertFalse(mMimeTypeMap.hasExtension("some_random_string"));

        assertFalse(mMimeTypeMap.hasExtension(""));
        assertFalse(mMimeTypeMap.hasExtension(null));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExtensionFromMimeType",
            args = {String.class}
        )
    })
    public void testGetExtensionFromMimeType() {
        assertEquals("png", mMimeTypeMap.getExtensionFromMimeType("image/png"));
        assertEquals("zip", mMimeTypeMap.getExtensionFromMimeType("application/zip"));

        assertNull(mMimeTypeMap.getExtensionFromMimeType("some_random_string"));

        assertNull(mMimeTypeMap.getExtensionFromMimeType(null));
        assertNull(mMimeTypeMap.getExtensionFromMimeType(""));
}

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSingleton",
            args = {}
        )
    })
    public void testGetSingleton() {
        MimeTypeMap firstMimeTypeMap = MimeTypeMap.getSingleton();
        MimeTypeMap secondMimeTypeMap = MimeTypeMap.getSingleton();

        assertSame(firstMimeTypeMap, secondMimeTypeMap);
    }
}
