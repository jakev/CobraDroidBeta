/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.cts.tradefed.testtype;

import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit tests for {@link PlanXmlParser}.
 */
public class PlanXmlParserTest extends TestCase {

    private static final String TEST_URI1 = "foo";
    private static final String TEST_URI2 = "foo2";

    static final String TEST_DATA =
        "<TestPlan version=\"1.0\">" +
            String.format("<Entry uri=\"%s\" />", TEST_URI1) +
            String.format("<Entry uri=\"%s\" />", TEST_URI2) +
        "</TestPlan>";

    /**
     * Simple test for parsing a plan containing two uris
     */
    public void testParse() throws ParseException  {
        PlanXmlParser parser = new PlanXmlParser();
        parser.parse(getStringAsStream(TEST_DATA));
        assertEquals(2, parser.getTestUris().size());
        Iterator<String> iter = parser.getTestUris().iterator();
        // assert uris in order
        assertEquals(TEST_URI1, iter.next());
        assertEquals(TEST_URI2, iter.next());
    }

    private InputStream getStringAsStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }
}
