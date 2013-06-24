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

import com.android.cts.tradefed.device.DeviceInfoCollector;
import com.android.cts.tradefed.targetsetup.CtsBuildHelper;
import com.android.ddmlib.Log;
import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.tradefed.config.Option;
import com.android.tradefed.result.CollectingTestListener;
import com.android.tradefed.result.LogDataType;
import com.android.tradefed.result.TestResult;
import com.android.tradefed.result.TestRunResult;
import com.android.tradefed.result.TestResult.TestStatus;
import com.android.tradefed.targetsetup.IBuildInfo;
import com.android.tradefed.targetsetup.IFolderBuildInfo;
import com.android.tradefed.util.FileUtil;

import org.kxml2.io.KXmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Writes results to an XML files in the CTS format.
 * <p/>
 * Collects all test info in memory, then dumps to file when invocation is complete.
 * <p/>
 * Outputs xml in format governed by the cts_result.xsd
 */
public class CtsXmlResultReporter extends CollectingTestListener {

    private static final String LOG_TAG = "CtsXmlResultReporter";

    private static final String TEST_RESULT_FILE_NAME = "testResult.xml";
    private static final String CTS_RESULT_FILE_VERSION = "2.0";
    private static final String CTS_VERSION = "99";


    private static final String[] CTS_RESULT_RESOURCES = {"cts_result.xsl", "cts_result.css",
        "logo.gif", "newrule-green.png"};

    /** the XML namespace */
    private static final String ns = null;

    private static final String REPORT_DIR_NAME = "output-file-path";
    @Option(name=REPORT_DIR_NAME, description="root file system path to directory to store xml " +
            "test results and associated logs. If not specified, results will be stored at " +
            "<cts root>/repository/results")
    protected File mReportDir = null;

    protected IBuildInfo mBuildInfo;

    private String mStartTime;

    public void setReportDir(File reportDir) {
        mReportDir = reportDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invocationStarted(IBuildInfo buildInfo) {
        super.invocationStarted(buildInfo);
        if (mReportDir == null) {
            if (!(buildInfo instanceof IFolderBuildInfo)) {
                throw new IllegalArgumentException("build info is not a IFolderBuildInfo");
            }
            IFolderBuildInfo ctsBuild = (IFolderBuildInfo)buildInfo;
            try {
                CtsBuildHelper buildHelper = new CtsBuildHelper(ctsBuild.getRootDir());
                mReportDir = buildHelper.getResultsDir();

            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("unrecognized cts structure", e);
            }
        }
        // create a unique directory for saving results, using old cts host convention
        // TODO: in future, consider using LogFileSaver to create build-specific directories
        mReportDir = new File(mReportDir, getResultTimestamp());
        mReportDir.mkdirs();
        mStartTime = getTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testLog(String dataName, LogDataType dataType, InputStream dataStream) {
        // TODO: implement this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testFailed(TestFailure status, TestIdentifier test, String trace) {
        super.testFailed(status, test, trace);
        Log.i(LOG_TAG, String.format("Test %s#%s: %s\n%s", test.getClassName(), test.getTestName(),
                status.toString(), trace));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
        super.testRunEnded(elapsedTime, runMetrics);
        Log.i(LOG_TAG, String.format("Test run %s complete. Tests passed %d, failed %d, error %d",
                getCurrentRunResults().getName(), getCurrentRunResults().getNumPassedTests(),
                getCurrentRunResults().getNumFailedTests(),
                getCurrentRunResults().getNumErrorTests()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invocationEnded(long elapsedTime) {
        super.invocationEnded(elapsedTime);
        createXmlResult(mReportDir, mStartTime, elapsedTime);
        copyFormattingFiles(mReportDir);
        zipResults(mReportDir);
    }

    /**
     * Creates a report file and populates it with the report data from the completed tests.
     */
    private void createXmlResult(File reportDir, String startTimestamp, long elapsedTime) {
        String endTime = getTimestamp();

        OutputStream stream = null;
        try {
            stream = createOutputResultStream(reportDir);
            KXmlSerializer serializer = new KXmlSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument("UTF-8", false);
            serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.processingInstruction("xml-stylesheet type=\"text/xsl\"  " +
                    "href=\"cts_result.xsl\"");
            serializeResultsDoc(serializer, startTimestamp, endTime);
            serializer.endDocument();
            // TODO: output not executed timeout omitted counts
            String msg = String.format("XML test result file generated at %s. Total tests %d, " +
                    "Failed %d, Error %d", reportDir.getAbsolutePath(), getNumTotalTests(),
                    getNumFailedTests(), getNumErrorTests());
            Log.logAndDisplay(LogLevel.INFO, LOG_TAG, msg);
            Log.logAndDisplay(LogLevel.INFO, LOG_TAG, String.format("Time: %s",
                    formatElapsedTime(elapsedTime)));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to generate report data");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Output the results XML.
     *
     * @param serializer the {@link KXmlSerializer} to use
     * @param startTime the user-friendly starting time of the test invocation
     * @param endTime the user-friendly ending time of the test invocation
     * @throws IOException
     */
    private void serializeResultsDoc(KXmlSerializer serializer, String startTime, String endTime)
            throws IOException {
        serializer.startTag(ns, "TestResult");
        // TODO: output test plan and profile values
        serializer.attribute(ns, "testPlan", "unknown");
        serializer.attribute(ns, "profile", "unknown");
        serializer.attribute(ns, "starttime", startTime);
        serializer.attribute(ns, "endtime", endTime);
        serializer.attribute(ns, "version", CTS_RESULT_FILE_VERSION);

        serializeDeviceInfo(serializer);
        serializeHostInfo(serializer);
        serializeTestSummary(serializer);
        serializeTestResults(serializer);
    }

    /**
     * Output the device info XML.
     *
     * @param serializer
     */
    private void serializeDeviceInfo(KXmlSerializer serializer) throws IOException {
        serializer.startTag(ns, "DeviceInfo");

        TestRunResult deviceInfoResult = findRunResult(DeviceInfoCollector.APP_PACKAGE_NAME);
        if (deviceInfoResult == null) {
            Log.w(LOG_TAG, String.format("Could not find device info run %s",
                    DeviceInfoCollector.APP_PACKAGE_NAME));
            return;
        }
        // Extract metrics that need extra handling, and then dump the remainder into BuildInfo
        Map<String, String> metricsCopy = new HashMap<String, String>(
                deviceInfoResult.getRunMetrics());
        serializer.startTag(ns, "Screen");
        String screenWidth = metricsCopy.remove(DeviceInfoCollector.SCREEN_WIDTH);
        String screenHeight = metricsCopy.remove(DeviceInfoCollector.SCREEN_HEIGHT);
        serializer.attribute(ns, "resolution", String.format("%sx%s", screenWidth, screenHeight));
        serializer.endTag(ns, "Screen");

        serializer.startTag(ns, "PhoneSubInfo");
        serializer.attribute(ns, "subscriberId", metricsCopy.remove(
                DeviceInfoCollector.PHONE_NUMBER));
        serializer.endTag(ns, "PhoneSubInfo");

        String featureData = metricsCopy.remove(DeviceInfoCollector.FEATURES);
        String processData = metricsCopy.remove(DeviceInfoCollector.PROCESSES);

        // dump the remaining metrics without translation
        serializer.startTag(ns, "BuildInfo");
        for (Map.Entry<String, String> metricEntry : metricsCopy.entrySet()) {
            serializer.attribute(ns, metricEntry.getKey(), metricEntry.getValue());
        }
        serializer.endTag(ns, "BuildInfo");

        serializeFeatureInfo(serializer, featureData);
        serializeProcessInfo(serializer, processData);

        serializer.endTag(ns, "DeviceInfo");
    }

    /**
     * Prints XML indicating what features are supported by the device. It parses a string from the
     * featureData argument that is in the form of "feature1:true;feature2:false;featuer3;true;"
     * with a trailing semi-colon.
     *
     * <pre>
     *  <FeatureInfo>
     *     <Feature name="android.name.of.feature" available="true" />
     *     ...
     *   </FeatureInfo>
     * </pre>
     *
     * @param serializer used to create XML
     * @param featureData raw unparsed feature data
     */
    private void serializeFeatureInfo(KXmlSerializer serializer, String featureData) throws IOException {
        serializer.startTag(ns, "FeatureInfo");

        if (featureData == null) {
            featureData = "";
        }

        String[] featurePairs = featureData.split(";");
        for (String featurePair : featurePairs) {
            String[] nameTypeAvailability = featurePair.split(":");
            if (nameTypeAvailability.length >= 3) {
                serializer.startTag(ns, "Feature");
                serializer.attribute(ns, "name", nameTypeAvailability[0]);
                serializer.attribute(ns, "type", nameTypeAvailability[1]);
                serializer.attribute(ns, "available", nameTypeAvailability[2]);
                serializer.endTag(ns, "Feature");
            }
        }
        serializer.endTag(ns, "FeatureInfo");
    }

    /**
     * Prints XML data indicating what particular processes of interest were running on the device.
     * It parses a string from the rootProcesses argument that is in the form of
     * "processName1;processName2;..." with a trailing semi-colon.
     *
     * <pre>
     *   <ProcessInfo>
     *     <Process name="long_cat_viewer" uid="0" />
     *     ...
     *   </ProcessInfo>
     * </pre>
     *
     * @param document
     * @param parentNode
     * @param deviceInfo
     */
    private void serializeProcessInfo(KXmlSerializer serializer, String rootProcesses)
            throws IOException {
        serializer.startTag(ns, "ProcessInfo");

        if (rootProcesses == null) {
            rootProcesses = "";
        }

        String[] processNames = rootProcesses.split(";");
        for (String processName : processNames) {
            processName = processName.trim();
            if (processName.length() > 0) {
                serializer.startTag(ns, "Process");
                serializer.attribute(ns, "name", processName);
                serializer.attribute(ns, "uid", "0");
                serializer.endTag(ns, "Process");
            }
        }
        serializer.endTag(ns, "ProcessInfo");
    }

    /**
     * Finds the {@link TestRunResult} with the given name.
     *
     * @param runName
     * @return the {@link TestRunResult}
     */
    private TestRunResult findRunResult(String runName) {
        for (TestRunResult runResult : getRunResults()) {
            if (runResult.getName().equals(runName)) {
                return runResult;
            }
        }
        return null;
    }

    /**
     * Output the host info XML.
     *
     * @param serializer
     */
    private void serializeHostInfo(KXmlSerializer serializer) throws IOException {
        serializer.startTag(ns, "HostInfo");

        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {}
        serializer.attribute(ns, "name", hostName);

        serializer.startTag(ns, "Os");
        serializer.attribute(ns, "name", System.getProperty("os.name"));
        serializer.attribute(ns, "version", System.getProperty("os.version"));
        serializer.attribute(ns, "arch", System.getProperty("os.arch"));
        serializer.endTag(ns, "Os");

        serializer.startTag(ns, "Java");
        serializer.attribute(ns, "name", System.getProperty("java.vendor"));
        serializer.attribute(ns, "version", System.getProperty("java.version"));
        serializer.endTag(ns, "Java");

        serializer.startTag(ns, "Cts");
        serializer.attribute(ns, "version", CTS_VERSION);
        // TODO: consider outputting tradefed options here
        serializer.endTag(ns, "Cts");

        serializer.endTag(ns, "HostInfo");
    }

    /**
     * Output the test summary XML containing summary totals for all tests.
     *
     * @param serializer
     * @throws IOException
     */
    private void serializeTestSummary(KXmlSerializer serializer) throws IOException {
        serializer.startTag(ns, "Summary");
        serializer.attribute(ns, "failed", Integer.toString(getNumErrorTests() +
                getNumFailedTests()));
        // TODO: output notExecuted, timeout, and omitted count
        serializer.attribute(ns, "notExecuted", "0");
        serializer.attribute(ns, "timeout", "0");
        serializer.attribute(ns, "omitted", "0");
        serializer.attribute(ns, "pass", Integer.toString(getNumPassedTests()));
        serializer.attribute(ns, "total", Integer.toString(getNumTotalTests()));
        serializer.endTag(ns, "Summary");
    }

    /**
     * Output the detailed test results XML.
     *
     * @param serializer
     * @throws IOException
     */
    private void serializeTestResults(KXmlSerializer serializer) throws IOException {
        for (TestRunResult runResult : getRunResults()) {
            serializeTestRunResult(serializer, runResult);
        }
    }

    /**
     * Output the XML for one test run aka test package.
     *
     * @param serializer
     * @param runResult the {@link TestRunResult}
     * @throws IOException
     */
    private void serializeTestRunResult(KXmlSerializer serializer, TestRunResult runResult)
            throws IOException {
        if (runResult.getName().equals(DeviceInfoCollector.APP_PACKAGE_NAME)) {
            // ignore run results for the info collecting packages
            return;
        }
        serializer.startTag(ns, "TestPackage");
        serializer.attribute(ns, "name", runResult.getName());
        serializer.attribute(ns, "runTime", formatElapsedTime(runResult.getElapsedTime()));
        // TODO: generate digest
        serializer.attribute(ns, "digest", "");
        serializer.attribute(ns, "failed", Integer.toString(runResult.getNumErrorTests() +
                runResult.getNumFailedTests()));
        // TODO: output notExecuted, timeout, and omitted count
        serializer.attribute(ns, "notExecuted", "0");
        serializer.attribute(ns, "timeout", "0");
        serializer.attribute(ns, "omitted", "0");
        serializer.attribute(ns, "pass", Integer.toString(runResult.getNumPassedTests()));
        serializer.attribute(ns, "total", Integer.toString(runResult.getNumTests()));

        // the results XML needs to organize test's by class. Build a nested data structure that
        // group's the results by class name
        Map<String, Map<TestIdentifier, TestResult>> classResultsMap = buildClassNameMap(
                runResult.getTestResults());

        for (Map.Entry<String, Map<TestIdentifier, TestResult>> resultsEntry :
                classResultsMap.entrySet()) {
            serializer.startTag(ns, "TestCase");
            serializer.attribute(ns, "name", resultsEntry.getKey());
            serializeTests(serializer, resultsEntry.getValue());
            serializer.endTag(ns, "TestCase");
        }
        serializer.endTag(ns, "TestPackage");
    }

    /**
     * Organizes the test run results into a format organized by class name.
     */
    private Map<String, Map<TestIdentifier, TestResult>> buildClassNameMap(
            Map<TestIdentifier, TestResult> results) {
        // use a linked hashmap to have predictable iteration order
        Map<String, Map<TestIdentifier, TestResult>> classResultMap =
            new LinkedHashMap<String, Map<TestIdentifier, TestResult>>();
        for (Map.Entry<TestIdentifier, TestResult> resultEntry : results.entrySet()) {
            String className = resultEntry.getKey().getClassName();
            Map<TestIdentifier, TestResult> resultsForClass = classResultMap.get(className);
            if (resultsForClass == null) {
                resultsForClass = new LinkedHashMap<TestIdentifier, TestResult>();
                classResultMap.put(className, resultsForClass);
            }
            resultsForClass.put(resultEntry.getKey(), resultEntry.getValue());
        }
        return classResultMap;
    }

    /**
     * Output XML for given map of tests their results
     *
     * @param serializer
     * @param results
     * @throws IOException
     */
    private void serializeTests(KXmlSerializer serializer, Map<TestIdentifier, TestResult> results)
            throws IOException {
        for (Map.Entry<TestIdentifier, TestResult> resultEntry : results.entrySet()) {
            serializeTest(serializer, resultEntry.getKey(), resultEntry.getValue());
        }
    }

    /**
     * Output the XML for given test and result.
     *
     * @param serializer
     * @param testId
     * @param result
     * @throws IOException
     */
    private void serializeTest(KXmlSerializer serializer, TestIdentifier testId, TestResult result)
            throws IOException {
        serializer.startTag(ns, "Test");
        serializer.attribute(ns, "name", testId.getTestName());
        serializer.attribute(ns, "result", convertStatus(result.getStatus()));

        if (result.getStackTrace() != null) {
            String sanitizedStack = sanitizeStackTrace(result.getStackTrace());
            serializer.startTag(ns, "FailedScene");
            serializer.attribute(ns, "message", getFailureMessageFromStackTrace(sanitizedStack));
            serializer.text(sanitizedStack);
            serializer.endTag(ns, "FailedScene");
        }
        serializer.endTag(ns, "Test");
    }

    /**
     * Convert a {@link TestStatus} to the result text to output in XML
     *
     * @param status the {@link TestStatus}
     * @return
     */
    private String convertStatus(TestStatus status) {
        switch (status) {
            case ERROR:
                return "fail";
            case FAILURE:
                return "fail";
            case PASSED:
                return "pass";
            // TODO add notExecuted, omitted timeout
        }
        return "omitted";
    }

    /**
     * Strip out any invalid XML characters that might cause the report to be unviewable.
     * http://www.w3.org/TR/REC-xml/#dt-character
     */
    private static String sanitizeStackTrace(String trace) {
        if (trace != null) {
            return trace.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD]", "");
        } else {
            return null;
        }
    }

    private static String getFailureMessageFromStackTrace(String stack) {
        // This is probably too simplistic to work in all cases, but for now, just return first
        // line of stack as failure message
        int firstNewLine = stack.indexOf('\n');
        if (firstNewLine != -1) {
            return stack.substring(0, firstNewLine);
        }
        return stack;
    }

    /**
     * Return the current timestamp as a {@link String} suitable for displaying.
     * <p/>
     * Example: Fri Aug 20 15:13:03 PDT 2010
     */
    String getTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return dateFormat.format(new Date());
    }

    /**
     * Return the current timestamp in a compressed format, used to uniquely identify results.
     * <p/>
     * Example: 2010.08.16_11.42.12
     */
    private String getResultTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        return dateFormat.format(new Date());
    }

    /**
     * Return a prettified version of the given elapsed time
     * @return
     */
    private String formatElapsedTime(long elapsedTimeMs) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMs) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMs) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMs);
        StringBuilder time = new StringBuilder();
        if (hours > 0) {
            time.append(hours);
            time.append("h ");
        }
        if (minutes > 0) {
            time.append(minutes);
            time.append("m ");
        }
        time.append(seconds);
        time.append("s");

        return time.toString();
    }

    /**
     * Creates the output stream to use for test results. Exposed for mocking.
     */
    OutputStream createOutputResultStream(File reportDir) throws IOException {
        File reportFile = new File(reportDir, TEST_RESULT_FILE_NAME);
        Log.i(LOG_TAG, String.format("Created xml report file at %s",
                reportFile.getAbsolutePath()));
        return new FileOutputStream(reportFile);
    }

    /**
     * Copy the xml formatting files stored in this jar to the results directory
     *
     * @param resultsDir
     */
    private void copyFormattingFiles(File resultsDir) {
        for (String resultFileName : CTS_RESULT_RESOURCES) {
            InputStream configStream = getClass().getResourceAsStream(
                    String.format("/result/%s", resultFileName));
            if (configStream != null) {
                File resultFile = new File(resultsDir, resultFileName);
                try {
                    FileUtil.writeToFile(configStream, resultFile);
                } catch (IOException e) {
                    Log.w(LOG_TAG, String.format("Failed to write %s to file", resultFileName));
                }
            } else {
                Log.w(LOG_TAG, String.format("Failed to load %s from jar", resultFileName));
            }
        }
    }

    /**
     * Zip the contents of the given results directory.
     *
     * @param resultsDir
     */
    private void zipResults(File resultsDir) {
        // TODO: implement this
    }
}
