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

import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit tests for {@link TestPackageXmlParser}.
 */
public class TestPackageXmlParserTest extends TestCase {

    private static String INSTR_TEST_DATA =
        "<TestPackage AndroidFramework=\"Android 1.0\" appNameSpace=\"com.example\" " +
        "appPackageName=\"android.example\" name=\"CtsExampleTestCases\" " +
        "runner=\"android.test.InstrumentationTestRunner\" version=\"1.0\">" +
        "</TestPackage>";

    private static String HOST_TEST_DATA =
        "<TestPackage hostSideOnly=\"true\" >\n" +
        "    <TestSuite name=\"com\" >\n" +
        "        <TestSuite name=\"example\" >\n" +
        "            <TestCase name=\"ExampleTest\" >\n" +
        "                <Test name=\"testFoo\" />\n" +
        "                <Test name=\"testFoo2\" />\n" +
        "            </TestCase>\n" +
        "        </TestSuite>\n" +
        "        <TestSuite name=\"example2\" >\n" +
        "            <TestCase name=\"Example2Test\" >\n" +
        "                <Test name=\"testFoo\" />\n" +
        "            </TestCase>\n" +
        "        </TestSuite>\n" +
        "    </TestSuite>\n" +
        "</TestPackage>";

    private static String BAD_HOST_TEST_DATA =
        "<TestPackage hostSideOnly=\"blah\" >" +
        "</TestPackage>";

    private static String NO_TEST_DATA =
        "<invalid />";

    /**
     * Test parsing test case xml containing an instrumentation test definition.
     */
    public void testParse_instrPackage() throws ParseException  {
        TestPackageXmlParser parser = new TestPackageXmlParser();
        parser.parse(getStringAsStream(INSTR_TEST_DATA));
        TestPackageDef def = parser.getTestPackageDef();
        assertEquals("com.example", def.getAppNameSpace());
        assertEquals("android.example", def.getUri());
        assertEquals("android.test.InstrumentationTestRunner", def.getRunner());
    }

    /**
     * Test parsing test case xml containing an host test attribute and test data.
     */
    public void testParse_hostTest() throws ParseException  {
        TestPackageXmlParser parser = new TestPackageXmlParser();
        parser.parse(getStringAsStream(HOST_TEST_DATA));
        TestPackageDef def = parser.getTestPackageDef();
        assertTrue(def.isHostSideTest());
        assertEquals(3, def.getTests().size());
        Iterator<TestIdentifier> iterator = def.getTests().iterator();

        TestIdentifier firstTest = iterator.next();
        assertEquals("com.example.ExampleTest", firstTest.getClassName());
        assertEquals("testFoo", firstTest.getTestName());

        TestIdentifier secondTest = iterator.next();
        assertEquals("com.example.ExampleTest", secondTest.getClassName());
        assertEquals("testFoo2", secondTest.getTestName());

        TestIdentifier thirdTest = iterator.next();
        assertEquals("com.example2.Example2Test", thirdTest.getClassName());
        assertEquals("testFoo", thirdTest.getTestName());
    }

    /**
     * Test parsing test case xml containing an invalid host test attribute.
     */
    public void testParse_badHostTest() throws ParseException  {
        TestPackageXmlParser parser = new TestPackageXmlParser();
        parser.parse(getStringAsStream(BAD_HOST_TEST_DATA));
        TestPackageDef def = parser.getTestPackageDef();
        assertFalse(def.isHostSideTest());
    }

    /**
     * Test parsing a test case xml with no test package data.
     */
    public void testParse_noData() throws ParseException  {
        TestPackageXmlParser parser = new TestPackageXmlParser();
        parser.parse(getStringAsStream(NO_TEST_DATA));
        assertNull(parser.getTestPackageDef());
    }

    private InputStream getStringAsStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }
}
