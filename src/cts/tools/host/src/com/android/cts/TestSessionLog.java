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

package com.android.cts;

import com.android.cts.TestDevice.DeviceParameterCollector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Store the information of a test plan.
 */
public class TestSessionLog extends XMLResourceHandler {
    private static final String EXPR_TEST_FAILED = ".+\\((\\S+):(\\d+)\\)";
    private static Pattern mTestFailedPattern = Pattern.compile(EXPR_TEST_FAILED);
    private static final String ATTRIBUTE_NAME = "name";
    static final String ATTRIBUTE_RESULT = "result";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_DIGEST = "digest";
    private static final String ATTRIBUTE_KNOWN_FAILURE = "KnownFailure";

    public static final String CTS_RESULT_FILE_NAME = "testResult.xml";
    private static final String CTS_RESULT_FILE_VERSION = "1.11";

    static final String ATTRIBUTE_STARTTIME = "starttime";
    static final String ATTRIBUTE_ENDTIME = "endtime";
    static final String ATTRIBUTE_TESTPLAN = "testPlan";
    static final String ATTRIBUTE_RESOLUTION = "resolution";
    static final String ATTRIBUTE_SUBSCRIBER_ID = "subscriberId";
    static final String ATTRIBUTE_DEVICE_ID = "deviceID";
    static final String ATTRIBUTE_BUILD_ID = "buildID";
    static final String ATTRIBUTE_BUILD_VERSION = "buildVersion";
    static final String ATTRIBUTE_ANDROID_PLATFORM_VERSION = "androidPlatformVersion";
    static final String ATTRIBUTE_LOCALES = "locales";
    static final String ATTRIBUTE_XDPI = "Xdpi";
    static final String ATTRIBUTE_YDPI = "Ydpi";
    static final String ATTRIBUTE_TOUCH = "touch";
    static final String ATTRIBUTE_NAVIGATION = "navigation";
    static final String ATTRIBUTE_KEYPAD = "keypad";
    static final String ATTRIBUTE_NETWORK = "network";
    static final String ATTRIBUTE_IMEI = "imei";
    static final String ATTRIBUTE_IMSI = "imsi";
    static final String ATTRIBUTE_BUILD_NAME = "buildName";
    static final String ATTRIBUTE_ARCH = "arch";
    static final String ATTRIBUTE_VALUE = "value";
    static final String ATTRIBUTE_AVAILABLE = "available";
    static final String ATTRIBUTE_TYPE = "type";
    static final String ATTRIBUTE_UID = "uid";
    static final String ATTRIBUTE_OPEN_GL_ES_VERSION = "openGlEsVersion";
    static final String ATTRIBUTE_PARTITIONS = "partitions";

    static final String ATTRIBUTE_PASS = "pass";
    static final String ATTRIBUTE_FAILED = "failed";
    static final String ATTRIBUTE_TIMEOUT = "timeout";
    static final String ATTRIBUTE_NOT_EXECUTED = "notExecuted";

    static final String TAG_DEVICEINFO = "DeviceInfo";
    static final String TAG_HOSTINFO = "HostInfo";
    static final String TAG_OSINFO = "Os";
    static final String TAG_JAVA = "Java";
    static final String TAG_CTS = "Cts";
    static final String TAG_INTVALUE = "IntValue";
    static final String TAG_SUMMARY = "Summary";
    static final String TAG_SCREEN = "Screen";
    static final String TAG_BUILD_INFO = "BuildInfo";
    static final String TAG_FEATURE_INFO = "FeatureInfo";
    static final String TAG_FEATURE = "Feature";
    static final String TAG_PROCESS_INFO = "ProcessInfo";
    static final String TAG_PROCESS = "Process";
    static final String TAG_PHONE_SUB_INFO = "PhoneSubInfo";
    static final String TAG_TEST_RESULT = "TestResult";
    static final String TAG_TESTPACKAGE = "TestPackage";
    static final String TAG_TESTSUITE = "TestSuite";
    static final String TAG_TESTCASE = "TestCase";
    static final String TAG_FAILED_SCENE = "FailedScene";
    static final String TAG_STACK_TRACE = "StackTrace";
    static final String TAG_FAILED_MESSAGE = "message";

    private Collection<TestPackage> mTestPackages;
    private Date mSessionStartTime;
    private Date mSessionEndTime;
    private String mResultPath;
    private String mResultDir;
    private String mTestPlanName;

    private ArrayList<DeviceParameterCollector> mDeviceParameterBase;

    public TestSessionLog(final Collection<TestPackage> packages, final String testPlanName) {
        mTestPackages = packages;

        mDeviceParameterBase = new ArrayList<TestDevice.DeviceParameterCollector>();
        mTestPlanName = testPlanName;

        mSessionStartTime = new Date();
        mSessionEndTime = new Date();
    }

    /**
     * Get the test plan name.
     *
     * @return The test plan name.
     */
    public String getTestPlanName() {
        return mTestPlanName;
    }

    /**
     * Get all result of this session.
     *
     * @return All the tests with a result code of this session.
     */
    public Collection<Test> getAllResults() {
        if (mTestPackages == null || mTestPackages.size() == 0) {
            return null;
        }

        ArrayList<Test> results = new ArrayList<Test>();
        for (TestPackage p : mTestPackages) {
            results.addAll(p.getTests());
        }

        return results;
    }

    /**
     * Get test list according to the result type code.
     *
     * @param resCode The result code.
     * @return The list of {@link Test}.
     */
    public Collection<Test> getTestList(int resCode) {
        if ((resCode < CtsTestResult.CODE_FIRST)
                || (resCode > CtsTestResult.CODE_LAST)) {
            return null;
        }

        ArrayList<Test> tests = new ArrayList<Test>();
        for (Test test : getAllResults()) {
            if (resCode == test.getResult().getResultCode()) {
                tests.add(test);
            }
        }

        return tests;
    }

    /**
     * Get TestSession start time
     *
     * @return The start time.
     */
    public Date getStartTime() {
        return mSessionStartTime;
    }

    /**
     * Get TestSession end time
     *
     * @return The end time.
     */
    public Date getEndTime() {
        return mSessionEndTime;
    }

    /**
     * Get test packages.
     *
     * @return The test packages.
     */
    public Collection<TestPackage> getTestPackages() {
        return mTestPackages;
    }

    /**
     * Get the path to the XML result file.
     *
     * @return The result path.
     */
    public String getResultPath() {
        return mResultPath;
    }

    /**
     * Get the result directory.  This is the directory that all result files
     * should go into.
     */
    public String getResultDir() {
        return mResultDir;
    }

    /**
     * set TestSession start time
     *
     * @param time The start time.
     */
    public void setStartTime(final long time) {
        mSessionStartTime.setTime(time);

        String startTimeStr = HostUtils.getFormattedTimeString(time, "_", ".", ".");
        mResultDir = HostConfig.getInstance().getResultRepository().getRoot()
            + File.separator + startTimeStr;
        mResultPath =  mResultDir + File.separator + CTS_RESULT_FILE_NAME;
        // Make sure the result directory exists
        new File(mResultDir).mkdirs();
    }

    /**
     * set TestSession end time
     *
     * @param time The end time.
     */
    public void setEndTime(final long time) {
        mSessionEndTime.setTime(time);
    }

    /**
     * Calling this functions indicates that the TestSession is complete.  This
     * indicates to the TestSessionLog that it is time to store the results
     * to the filesystem.
     */
    public void sessionComplete() {
        try {
            writeToFile(new File(mResultPath), createResultDoc());
            // Now zip up the results directory so we have something nice
            // that people can upload.
            HostUtils.zipUpDirectory(mResultDir,
                    mResultDir + ".zip",
                    new HostUtils.ZipFilenameTransformer() {
                        public String transform(String filename) {
                            if (filename.startsWith(mResultDir)) {
                                return filename.substring(mResultDir.length() + 1);
                            }
                            return filename;
                        }
            });
        } catch (Exception e) {
            Log.e("Got exception when trying to write to result file", e);
        }
        HostConfig.getInstance().extractResultResources(mResultDir);
    }

    /**
     * Create result Doc in XML format.
     *
     * @return Result document.
     */
    protected Document createResultDoc() {
        try {

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            ProcessingInstruction pr = doc.createProcessingInstruction(
                    "xml-stylesheet", "type=\"text/xsl\"  href=\"cts_result.xsl\"");
            doc.appendChild(pr);
            Node root = doc.createElement(TAG_TEST_RESULT);
            doc.appendChild(root);

            setAttribute(doc, root, ATTRIBUTE_VERSION, CTS_RESULT_FILE_VERSION);
            setAttribute(doc, root, ATTRIBUTE_STARTTIME, HostUtils.dateToString(mSessionStartTime));
            setAttribute(doc, root, ATTRIBUTE_ENDTIME, HostUtils.dateToString(mSessionEndTime));
            setAttribute(doc, root, ATTRIBUTE_TESTPLAN, mTestPlanName);

            // set device information
            for (int i = 0; i < mDeviceParameterBase.size(); i ++) {
                DeviceParameterCollector bldInfo = mDeviceParameterBase.get(i);
                // set device setting
                Node deviceSettingNode = doc.createElement(TAG_DEVICEINFO);

                Node screenNode = doc.createElement(TAG_SCREEN);
                setAttribute(doc, screenNode, ATTRIBUTE_RESOLUTION, bldInfo.getScreenResolution());

                setAttribute(doc, screenNode, DeviceParameterCollector.SCREEN_SIZE,
                        bldInfo.getScreenSize());
                setAttribute(doc, screenNode, DeviceParameterCollector.SCREEN_DENSITY,
                        bldInfo.getScreenDensity());
                setAttribute(doc, screenNode, DeviceParameterCollector.SCREEN_DENSITY_BUCKET,
                        bldInfo.getScreenDensityBucket());

                deviceSettingNode.appendChild(screenNode);
                Node simCardNode = doc.createElement(TAG_PHONE_SUB_INFO);
                setAttribute(doc, simCardNode, ATTRIBUTE_SUBSCRIBER_ID, bldInfo.getPhoneNumber());
                deviceSettingNode.appendChild(simCardNode);
                root.appendChild(deviceSettingNode);

                Node devInfoNode = doc.createElement(TAG_BUILD_INFO);
                setAttribute(doc, devInfoNode, ATTRIBUTE_DEVICE_ID, bldInfo.getSerialNumber());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_ID, bldInfo.getBuildId());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_NAME, bldInfo.getProductName());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_VERSION,
                             bldInfo.getBuildVersion());
                setAttribute(doc, devInfoNode, ATTRIBUTE_ANDROID_PLATFORM_VERSION,
                             bldInfo.getAndroidPlatformVersion());
                setAttribute(doc, devInfoNode, ATTRIBUTE_LOCALES, bldInfo.getLocales());
                setAttribute(doc, devInfoNode, ATTRIBUTE_XDPI, bldInfo.getXdpi());
                setAttribute(doc, devInfoNode, ATTRIBUTE_YDPI, bldInfo.getYdpi());
                setAttribute(doc, devInfoNode, ATTRIBUTE_TOUCH, bldInfo.getTouchInfo());
                setAttribute(doc, devInfoNode, ATTRIBUTE_NAVIGATION, bldInfo.getNavigation());
                setAttribute(doc, devInfoNode, ATTRIBUTE_KEYPAD, bldInfo.getKeypad());
                setAttribute(doc, devInfoNode, ATTRIBUTE_NETWORK, bldInfo.getNetwork());
                setAttribute(doc, devInfoNode, ATTRIBUTE_IMEI, bldInfo.getIMEI());
                setAttribute(doc, devInfoNode, ATTRIBUTE_IMSI, bldInfo.getIMSI());
                setAttribute(doc, devInfoNode, ATTRIBUTE_OPEN_GL_ES_VERSION,
                        bldInfo.getOpenGlEsVersion());
                setAttribute(doc, devInfoNode, ATTRIBUTE_PARTITIONS,
                        bldInfo.getPartitions());

                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_FINGERPRINT, bldInfo.getBuildFingerPrint());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_TYPE, bldInfo.getBuildType());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_MODEL, bldInfo.getBuildModel());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_MANUFACTURER,
                                bldInfo.getBuildManufacturer());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_BRAND, bldInfo.getBuildBrand());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_BOARD, bldInfo.getBuildBoard());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_DEVICE, bldInfo.getBuildDevice());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_ABI, bldInfo.getBuildAbi());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_ABI2, bldInfo.getBuildAbi2());

                deviceSettingNode.appendChild(devInfoNode);

                addFeatureInfo(doc, deviceSettingNode, bldInfo);
                addProcessInfo(doc, deviceSettingNode, bldInfo);
            }

            Node hostInfo = doc.createElement(TAG_HOSTINFO);
            root.appendChild(hostInfo);
            String hostName = "";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {}
            setAttribute(doc, hostInfo, ATTRIBUTE_NAME, hostName);
            Node osInfo = doc.createElement(TAG_OSINFO);
            hostInfo.appendChild(osInfo);
            setAttribute(doc, osInfo, ATTRIBUTE_NAME, System.getProperty("os.name"));
            setAttribute(doc, osInfo, ATTRIBUTE_VERSION, System.getProperty("os.version"));
            setAttribute(doc, osInfo, ATTRIBUTE_ARCH, System.getProperty("os.arch"));
            Node javaInfo = doc.createElement(TAG_JAVA);
            hostInfo.appendChild(javaInfo);
            setAttribute(doc, javaInfo, ATTRIBUTE_NAME, System.getProperty("java.vendor"));
            setAttribute(doc, javaInfo, ATTRIBUTE_VERSION, System.getProperty("java.version"));
            Node ctsInfo = doc.createElement(TAG_CTS);
            hostInfo.appendChild(ctsInfo);
            setAttribute(doc, ctsInfo, ATTRIBUTE_VERSION, Version.asString());
            for (HostConfig.Ints i : HostConfig.Ints.values()) {
                Node intValue = doc.createElement(TAG_INTVALUE);
                ctsInfo.appendChild(intValue);
                setAttribute(doc, intValue, ATTRIBUTE_NAME, i.name());
                setAttribute(doc, intValue, ATTRIBUTE_VALUE, i.value());
            }

            int passNum = getTestList(CtsTestResult.CODE_PASS).size();
            int failNum = getTestList(CtsTestResult.CODE_FAIL).size();
            int notExecutedNum = getTestList(CtsTestResult.CODE_NOT_EXECUTED).size();
            int timeOutNum = getTestList(CtsTestResult.CODE_TIMEOUT).size();
            Node summaryNode = doc.createElement(TAG_SUMMARY);
            root.appendChild(summaryNode);
            setAttribute(doc, summaryNode, ATTRIBUTE_PASS, passNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_FAILED, failNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_NOT_EXECUTED, notExecutedNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_TIMEOUT, timeOutNum);

            for (TestPackage testPackage : mTestPackages) {
                Node testPackageNode = doc.createElement(TAG_TESTPACKAGE);
                setAttribute(doc, testPackageNode, ATTRIBUTE_NAME, testPackage.getAppBinaryName());
                setAttribute(doc, testPackageNode, TestSessionBuilder.ATTRIBUTE_APP_PACKAGE_NAME,
                        testPackage.getAppPackageName());
                setAttribute(doc, testPackageNode, ATTRIBUTE_DIGEST,
                             testPackage.getMessageDigest());

                if (testPackage instanceof SignatureCheckPackage) {
                    setAttribute(doc, testPackageNode,
                              TestSessionBuilder.ATTRIBUTE_SIGNATURE_CHECK, "true");
                }

                for (TestSuite testSuite : testPackage.getTestSuites()) {
                    outputTestSuite(doc, testPackage, testPackageNode, testSuite);
                }
                root.appendChild(testPackageNode);
            }

            return doc;
        } catch (Exception e) {
            Log.e("create result doc failed", e);
        }
        return null;
    }

    /**
     * Creates a {@link #TAG_FEATURE_INFO} tag with {@link #TAG_FEATURE} elements indicating
     * what features are supported by the device. It parses a string from the deviceInfo argument
     * that is in the form of "feature1:true;feature2:false;featuer3;true;" with a trailing
     * semi-colon.
     *
     * <pre>
     *  <FeatureInfo>
     *     <Feature name="android.name.of.feature" available="true" />
     *     ...
     *   </FeatureInfo>
     * </pre>
     * @param document used to create elements
     * @param parentNode to attach the FeatureInfo element to
     * @param deviceInfo to get the feature data from
     */
    private void addFeatureInfo(Document document, Node parentNode,
            DeviceParameterCollector deviceInfo) {
        Node featureInfo = document.createElement(TAG_FEATURE_INFO);
        parentNode.appendChild(featureInfo);

        String features = deviceInfo.getFeatures();
        if (features == null) {
            features = "";
        }

        String[] featurePairs = features.split(";");
        for (String featurePair : featurePairs) {
            String[] nameTypeAvailability = featurePair.split(":");
            if (nameTypeAvailability.length >= 3) {
                Node feature = document.createElement(TAG_FEATURE);
                featureInfo.appendChild(feature);

                setAttribute(document, feature, ATTRIBUTE_NAME, nameTypeAvailability[0]);
                setAttribute(document, feature, ATTRIBUTE_TYPE, nameTypeAvailability[1]);
                setAttribute(document, feature, ATTRIBUTE_AVAILABLE, nameTypeAvailability[2]);
            }
        }
    }

    /**
     * Creates a {@link #TAG_PROCESS_INFO} tag with {@link #TAG_PROCESS} elements indicating
     * what particular processes of interest were running on the device. It parses a string from
     * the deviceInfo argument that is in the form of "processName1;processName2;..." with a
     * trailing semi-colon.
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
    private void addProcessInfo(Document document, Node parentNode,
            DeviceParameterCollector deviceInfo) {
        Node processInfo = document.createElement(TAG_PROCESS_INFO);
        parentNode.appendChild(processInfo);

        String rootProcesses = deviceInfo.getProcesses();
        if (rootProcesses == null) {
            rootProcesses = "";
        }

        String[] processNames = rootProcesses.split(";");
        for (String processName : processNames) {
            processName = processName.trim();
            if (processName.length() > 0) {
                Node process = document.createElement(TAG_PROCESS);
                processInfo.appendChild(process);

                setAttribute(document, process, ATTRIBUTE_NAME, processName);
                setAttribute(document, process, ATTRIBUTE_UID, "0");
            }
        }
    }

    /**
     * Output TestSuite and result to XML DOM Document.
     *
     * @param doc The document.
     * @param parentNode The parent node.
     * @param testSuite The test suite.
     */
    private void outputTestSuite(final Document doc,
            final TestPackage testPackage, final Node parentNode,
            TestSuite testSuite) {

        Collection<TestSuite> subSuites = testSuite.getSubSuites();
        Collection<TestCase> testCases = testSuite.getTestCases();

        Node testSuiteNode = doc.createElement(TAG_TESTSUITE);
        setAttribute(doc, testSuiteNode, ATTRIBUTE_NAME, testSuite.getName());

        for (TestCase testCase : testCases) {
            Node testCaseNode = doc.createElement(TAG_TESTCASE);
            testSuiteNode.appendChild(testCaseNode);
            setAttribute(doc, testCaseNode, ATTRIBUTE_NAME, testCase.getName());
            setAttribute(doc, testCaseNode, TestSessionBuilder.ATTRIBUTE_PRIORITY,
                    testCase.getPriority());

            Collection<Test> tests = testCase.getTests();
            for (Test test : tests) {
                Node testNode = doc.createElement(TestSessionBuilder.TAG_TEST);
                testCaseNode.appendChild(testNode);

                if (test.isKnownFailure()) {
                    setAttribute(doc, testNode, ATTRIBUTE_KNOWN_FAILURE, test.getKnownFailure());
                }

                CtsTestResult result = test.getResult();
                setAttribute(doc, testNode, ATTRIBUTE_NAME, test.getName());
                setAttribute(doc, testNode, ATTRIBUTE_RESULT, result.getResultString());
                setAttribute(doc, testNode, ATTRIBUTE_STARTTIME,
                             new Date(test.getStartTime()).toString());
                setAttribute(doc, testNode, ATTRIBUTE_ENDTIME,
                             new Date(test.getEndTime()).toString());

                String failedMessage = result.getFailedMessage();

                if (failedMessage != null) {
                    // failure message may contain control characters < 0x20 that get translated
                    // into illegal XML character entities. Replace them first.
                    failedMessage = HostUtils.replaceControlChars(failedMessage);
                    Node failedMessageNode = doc.createElement(TAG_FAILED_SCENE);
                    testNode.appendChild(failedMessageNode);
                    setAttribute(doc, failedMessageNode,TAG_FAILED_MESSAGE, failedMessage);

                    String stackTrace = sanitizeStackTrace(result.getStackTrace());
                    if (stackTrace != null) {
                        Node stackTraceNode = doc.createElement(TAG_STACK_TRACE);
                        failedMessageNode.appendChild(stackTraceNode);
                        Node stackTraceTextNode = doc.createTextNode(stackTrace);
                        stackTraceNode.appendChild(stackTraceTextNode);
                    }
                }
            }
        }

        for (TestSuite subSuite : subSuites) {
            outputTestSuite(doc, testPackage, testSuiteNode, subSuite);
            parentNode.appendChild(testSuiteNode);
        }
        parentNode.appendChild(testSuiteNode);
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

    /**
     * Fetch failed file name and line number
     *
     * @param failedResult failed message
     * @return failed file name and line number
     */
    public final static String[] getFailedLineNumber(final String failedResult) {
        Matcher m = mTestFailedPattern.matcher(failedResult);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return null;
    }

    /**
     * set the device information of a specific device
     *
     * @param dInfo The device information.
     */
    public void setDeviceInfo(final TestDevice.DeviceParameterCollector dInfo) {
        for (DeviceParameterCollector collector : mDeviceParameterBase) {
            if (collector.getSerialNumber().equals(dInfo.getSerialNumber())) {
                //if there has information for the device with given serial number,
                //replace it with the new information.
                mDeviceParameterBase.remove(collector);
                break;
            }
        }
        mDeviceParameterBase.add(dInfo);
    }
}
