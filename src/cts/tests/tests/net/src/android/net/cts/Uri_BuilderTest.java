/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.net.cts;

import junit.framework.TestCase;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import android.net.Uri.Builder;
import android.net.Uri;

@TestTargetClass(Uri.Builder.class)
public class Uri_BuilderTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "Uri.Builder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "build",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "scheme",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "authority",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "path",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "query",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "opaquePart",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "fragment",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "appendEncodedPath",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "appendPath",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "appendQueryParameter",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "encodedAuthority",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "encodedFragment",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "encodedPath",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "encodedQuery",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "encodedOpaquePart",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Builder operations.",
            method = "toString",
            args = {}
        )
    })
    public void testBuilderOperations() {
        Uri uri = Uri.parse("http://google.com/p1?query#fragment");
        Builder builder = uri.buildUpon();
        uri = builder.appendPath("p2").build();
        assertEquals("http", uri.getScheme());
        assertEquals("google.com", uri.getAuthority());
        assertEquals("/p1/p2", uri.getPath());
        assertEquals("query", uri.getQuery());
        assertEquals("fragment", uri.getFragment());
        assertEquals(uri.toString(), builder.toString());

        uri = Uri.parse("mailto:nobody");
        builder = uri.buildUpon();
        uri = builder.build();
        assertEquals("mailto", uri.getScheme());
        assertEquals("nobody", uri.getSchemeSpecificPart());
        assertEquals(uri.toString(), builder.toString());

        uri = new Uri.Builder()
                .scheme("http")
                .encodedAuthority("google.com")
                .encodedPath("/p1")
                .appendEncodedPath("p2")
                .encodedQuery("query")
                .appendQueryParameter("query2", null)
                .encodedFragment("fragment")
                .build();
        assertEquals("http", uri.getScheme());
        assertEquals("google.com", uri.getEncodedAuthority());
        assertEquals("/p1/p2", uri.getEncodedPath());
        assertEquals("query&query2=null", uri.getEncodedQuery());
        assertEquals("fragment", uri.getEncodedFragment());

        uri = new Uri.Builder()
                .scheme("mailto")
                .encodedOpaquePart("nobody")
                .build();
        assertEquals("mailto", uri.getScheme());
        assertEquals("nobody", uri.getEncodedSchemeSpecificPart());
    }
}
