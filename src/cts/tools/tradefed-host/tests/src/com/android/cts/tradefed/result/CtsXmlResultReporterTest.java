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
package com.android.cts.tradefed.result;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.ddmlib.testrunner.ITestRunListener.TestFailure;
import com.android.tradefed.result.XmlResultReporter;
import com.android.tradefed.targetsetup.BuildInfo;
import com.android.tradefed.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Unit tests for {@link XmlResultReporter}.
 */
public class CtsXmlResultReporterTest extends TestCase {

    private CtsXmlResultReporter mResultReporter;
    private ByteArrayOutputStream mOutputStream;
    private File mReportDir;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mOutputStream = new ByteArrayOutputStream();
        mResultReporter = new CtsXmlResultReporter() {
            @Override
            OutputStream createOutputResultStream(File reportDir) throws IOException {
                return mOutputStream;
            }

            @Override
            String getTimestamp() {
                return "ignore";
            }
        };
        // TODO: use mock file dir instead
        mReportDir = FileUtil.createTempDir("foo");
        mResultReporter.setReportDir(mReportDir);
    }

    @Override
    protected void tearDown() throws Exception {
        if (mReportDir != null) {
            FileUtil.recursiveDelete(mReportDir);
        }
        super.tearDown();
    }

    /**
     * A simple test to ensure expected output is generated for test run with no tests.
     */
    public void testEmptyGeneration() {
        final String expectedOutput = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>" +
            "<?xml-stylesheet type=\"text/xsl\" href=\"cts_result.xsl\"?>" +
            "<TestResult testPlan=\"unknown\" profile=\"unknown\" starttime=\"ignore\" endtime=\"ignore\" version=\"2.0\"> " +
            "<Summary failed=\"0\" notExecuted=\"0\" timeout=\"0\" omitted=\"0\" pass=\"0\" total=\"0\" />" +
            "</TestResult>";
        mResultReporter.invocationStarted(new BuildInfo(1, "test", "test"));
        mResultReporter.invocationEnded(1);
        assertEquals(expectedOutput, getOutput());
    }

    /**
     * A simple test to ensure expected output is generated for test run with a single passed test.
     */
    public void testSinglePass() {
        Map<String, String> emptyMap = Collections.emptyMap();
        final TestIdentifier testId = new TestIdentifier("com.foo.FooTest", "testFoo");
        mResultReporter.invocationStarted(new BuildInfo());
        mResultReporter.testRunStarted("run", 1);
        mResultReporter.testStarted(testId);
        mResultReporter.testEnded(testId, emptyMap);
        mResultReporter.testRunEnded(3000, emptyMap);
        mResultReporter.invocationEnded(1);
        String output =  getOutput();
        // TODO: consider doing xml based compare
        assertTrue(output.contains(
                "<Summary failed=\"0\" notExecuted=\"0\" timeout=\"0\" omitted=\"0\" pass=\"1\" total=\"1\" />"));
        assertTrue(output.contains("<TestPackage name=\"run\" runTime=\"3s\" digest=\"\" " +
                "failed=\"0\" notExecuted=\"0\" timeout=\"0\" omitted=\"0\" pass=\"1\" total=\"1\">"));
        assertTrue(output.contains(String.format("<TestCase name=\"%s\">", testId.getClassName())));

        final String testCaseTag = String.format(
                "<Test name=\"%s\" result=\"pass\" />", testId.getTestName());
        assertTrue(output.contains(testCaseTag));
    }

    /**
     * A simple test to ensure expected output is generated for test run with a single failed test.
     */
    public void testSingleFail() {
        Map<String, String> emptyMap = Collections.emptyMap();
        final TestIdentifier testId = new TestIdentifier("FooTest", "testFoo");
        final String trace = "this is a trace\nmore trace";
        mResultReporter.invocationStarted(new BuildInfo());
        mResultReporter.testRunStarted("run", 1);
        mResultReporter.testStarted(testId);
        mResultReporter.testFailed(TestFailure.FAILURE, testId, trace);
        mResultReporter.testEnded(testId, emptyMap);
        mResultReporter.testRunEnded(3, emptyMap);
        mResultReporter.invocationEnded(1);
        String output =  getOutput();
        System.out.print(getOutput());
        // TODO: consider doing xml based compare
        assertTrue(output.contains(
                "<Summary failed=\"1\" notExecuted=\"0\" timeout=\"0\" omitted=\"0\" pass=\"0\" total=\"1\" />"));
        final String failureTag =
                "<FailedScene message=\"this is a trace\">this is a tracemore trace";
        assertTrue(output.contains(failureTag));
    }

    /**
     * Gets the output produced, stripping it of extraneous whitespace characters.
     */
    private String getOutput() {
        String output = mOutputStream.toString();
        // ignore newlines and tabs whitespace
        output = output.replaceAll("[\\r\\n\\t]", "");
        // replace two ws chars with one
        return output.replaceAll("  ", " ");
    }
}
