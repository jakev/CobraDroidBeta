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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * Builder of test plan and also provides serialization for a test plan.
 */
public class TestSessionBuilder extends XMLResourceHandler {
    // defined for external document, which is from the configuration files
    // this should keep synchronized with the format of the configuration files

    private static final String TAG_TEST_SUITE = "TestSuite";
    private static final String TAG_TEST_CASE = "TestCase";
    public static final String TAG_TEST = "Test";

    // attribute name define
    public static final String ATTRIBUTE_SIGNATURE_CHECK = "signatureCheck";
    public static final String ATTRIBUTE_REFERENCE_APP_TEST = "referenceAppTest";
    public static final String ATTRIBUTE_PRIORITY = "priority";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_RUNNER = "runner";
    private static final String ATTRIBUTE_JAR_PATH = "jarPath";
    private static final String ATTRIBUTE_APP_NAME_SPACE = "appNameSpace";
    public static final String ATTRIBUTE_APP_PACKAGE_NAME = "appPackageName";
    private static final String ATTRIBUTE_TARGET_NAME_SPACE = "targetNameSpace";
    private static final String ATTRIBUTE_TARGET_BINARY_NAME = "targetBinaryName";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_CONTROLLER = "HostController";
    private static final String ATTRIBUTE_KNOWN_FAILURE = "KnownFailure";
    private static final String ATTRIBUTE_HOST_SIDE_ONLY = "hostSideOnly";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_FRAMEWORK_VERSION = "AndroidFramework";
    private static final String ATTRIBUTE_APK_TO_TEST_NAME = "apkToTestName";
    private static final String ATTRIBUTE_PACKAGE_TO_TEST = "packageToTest";
    private static TestSessionBuilder sInstance;

    private DocumentBuilder mDocBuilder;

    public static TestSessionBuilder getInstance()
            throws ParserConfigurationException {
        if (sInstance == null) {
            sInstance = new TestSessionBuilder();
        }

        return sInstance;
    }

    private TestSessionBuilder() throws ParserConfigurationException {
        mDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * Create TestSession via TestSessionLog.
     *
     * @param log The test session log.
     * @return The test session.
     */
    public TestSession build(TestSessionLog log) {
        if (log == null) {
            return null;
        }
        return new TestSession(log, 1);
    }

    /**
     * Create TestSession via TestPlan XML configuration file.
     *
     * @param config TestPlan XML configuration file.
     * @return TestSession.
     */
    public TestSession build(final String config) throws SAXException, IOException,
            TestPlanNotFoundException, TestNotFoundException, NoSuchAlgorithmException {
        File file = new File(config);
        if (!file.exists()) {
            throw new TestPlanNotFoundException();
        }
        Document doc = mDocBuilder.parse(file);

        // parse device configuration
        int numOfRequiredDevices = 1; // default is 1
        try {
            Node deviceConfigNode = doc.getElementsByTagName(
                    TestPlan.Tag.REQUIRED_DEVICE).item(0);
            numOfRequiredDevices = getAttributeValue(deviceConfigNode,
                    TestPlan.Attribute.AMOUNT);
        } catch (Exception e) {
        }

        Collection<TestPackage> packages = loadPackages(doc);
        if (packages.size() == 0) {
            throw new TestNotFoundException("No valid package in test plan.");
        }

        String planFileName = file.getName();
        int index = planFileName.indexOf(".");
        String planName;
        if (index != -1) {
            planName = planFileName.substring(0, planFileName.indexOf("."));
        } else{
            planName = planFileName;
        }

        TestSessionLog sessionLog = new TestSessionLog(packages, planName);
        TestSession ts = new TestSession(sessionLog, numOfRequiredDevices);
        return ts;
    }

    /**
     * Load TestPackages from a TestPlan DOM doc.
     *
     * @param doc TestPlan DOM Document
     * @return loaded test package from TestPlan DOM Document
     */
    private Collection<TestPackage> loadPackages(Document doc)
                throws SAXException, IOException, NoSuchAlgorithmException {

        ArrayList<TestPackage> packages = new ArrayList<TestPackage>();
        NodeList packageList = doc.getElementsByTagName(TestPlan.Tag.ENTRY);
        ArrayList<String> removedPkgList = new ArrayList<String>();
        for (int i = 0; i < packageList.getLength(); i++) {
            Node pNode = packageList.item(i);
            String uri = getStringAttributeValue(pNode, TestPlan.Attribute.URI);
            String list = getStringAttributeValue(pNode, TestPlan.Attribute.EXCLUDE);
            ArrayList<String> excludedList = null;
            if ((list != null) && (list.length() != 0)) {
                excludedList = getStrArrayList(list);
            }

            String packageBinaryName = HostConfig.getInstance().getPackageBinaryName(uri);
            if (packageBinaryName != null) {
                String xmlConfigFilePath =
                       HostConfig.getInstance().getCaseRepository().getXmlPath(packageBinaryName);
                File xmlFile = new File(xmlConfigFilePath);
                TestPackage pkg = loadPackage(xmlFile, excludedList);
                if (pkg instanceof SignatureCheckPackage) {
                    // insert the signature check package
                    // to the head of the list
                    packages.add(0, pkg);
                } else {
                    packages.add(pkg);
                }
            } else{
                removedPkgList.add(uri);
            }
        }
        if (removedPkgList.size() != 0) {
            CUIOutputStream.println("The following package(s) doesn't exist:");
            for (String pkgName : removedPkgList) {
                CUIOutputStream.println("    " + pkgName);
            }
        }
        return packages;
    }

    /**
     * Load TestPackage via Package XML configuration file.
     *
     * @param packageConfigFile test package XML file
     * @param excludedList The list containing the excluded suites and sub types.
     * @return loaded TestPackage from test package XML configuration file
     */
    public TestPackage loadPackage(final File packageConfigFile, ArrayList<String> excludedList)
                                throws SAXException, IOException, NoSuchAlgorithmException {
        Node pNode = mDocBuilder.parse(packageConfigFile).getDocumentElement();
        return loadPackage(pNode, excludedList);
    }

    /**
     * Load TestPackage via Package XML configuration file.
     *
     * @param pkgNode the test package node in the XML file
     * @param excludedList The list containing the excluded suites and sub types.
     * @return loaded TestPackage from test package XML configuration file
     */
    public TestPackage loadPackage(final Node pkgNode, ArrayList<String> excludedList)
                                throws NoSuchAlgorithmException {

        String appBinaryName, targetNameSpace, targetBinaryName, version, frameworkVersion,
               runner, jarPath, appNameSpace, appPackageName, hostSideOnly;
        NodeList suiteList = pkgNode.getChildNodes();

        appBinaryName = getStringAttributeValue(pkgNode, ATTRIBUTE_NAME);
        targetNameSpace = getStringAttributeValue(pkgNode, ATTRIBUTE_TARGET_NAME_SPACE);
        targetBinaryName = getStringAttributeValue(pkgNode, ATTRIBUTE_TARGET_BINARY_NAME);
        version = getStringAttributeValue(pkgNode, ATTRIBUTE_VERSION);
        frameworkVersion = getStringAttributeValue(pkgNode, ATTRIBUTE_FRAMEWORK_VERSION);
        runner = getStringAttributeValue(pkgNode, ATTRIBUTE_RUNNER);
        jarPath = getStringAttributeValue(pkgNode, ATTRIBUTE_JAR_PATH);
        appNameSpace = getStringAttributeValue(pkgNode, ATTRIBUTE_APP_NAME_SPACE);
        appPackageName = getStringAttributeValue(pkgNode, ATTRIBUTE_APP_PACKAGE_NAME);
        hostSideOnly = getStringAttributeValue(pkgNode, ATTRIBUTE_HOST_SIDE_ONLY);
        String signature = getStringAttributeValue(pkgNode, ATTRIBUTE_SIGNATURE_CHECK);
        String referenceAppTest = getStringAttributeValue(pkgNode, ATTRIBUTE_REFERENCE_APP_TEST);
        TestPackage pkg = null;

        if ("true".equals(referenceAppTest)) {
            String apkToTestName = getStringAttributeValue(pkgNode, ATTRIBUTE_APK_TO_TEST_NAME);
            String packageUnderTest = getStringAttributeValue(pkgNode, ATTRIBUTE_PACKAGE_TO_TEST);
            pkg = new ReferenceAppTestPackage(runner, appBinaryName, targetNameSpace,
                    targetBinaryName, version, frameworkVersion, jarPath,
                    appNameSpace, appPackageName,
                    apkToTestName, packageUnderTest);
        } else if ("true".equals(signature)) {
            pkg = new SignatureCheckPackage(runner, appBinaryName, targetNameSpace,
                    targetBinaryName, version, frameworkVersion, jarPath,
                    appNameSpace, appPackageName);
        } else if ("true".equals(hostSideOnly)) {
            pkg = new HostSideOnlyPackage(appBinaryName, version, frameworkVersion,
                    jarPath, appPackageName);
        } else {
            pkg = new TestPackage(runner, appBinaryName, targetNameSpace, targetBinaryName,
                    version, frameworkVersion, jarPath, appNameSpace, appPackageName);
        }

        for (int i = 0; i < suiteList.getLength(); i++) {
            Node sNode = suiteList.item(i);
            if (sNode.getNodeType() == Document.ELEMENT_NODE
                    && TAG_TEST_SUITE.equals(sNode.getNodeName())) {
                String fullSuiteName = getFullSuiteName(sNode);
                if (checkFullMatch(excludedList, fullSuiteName) == false) {
                    ArrayList<String> excludedCaseList =
                                          getExcludedList(excludedList, fullSuiteName);
                    TestSuite suite = loadSuite(pkg, sNode, excludedCaseList);
                    if ((suite.getTestCases().size() != 0) || (suite.getSubSuites().size() != 0)) {
                        pkg.addTestSuite(suite);
                    }
                } else {
                    Log.d("suite=" + fullSuiteName + " is fully excluded");
                }
            }
        }

        return pkg;
    }

    /**
     * Get string ArrayList from string.
     *
     * @param str The given string.
     * @return The list.
     */
    private ArrayList<String> getStrArrayList(String str) {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }

        String[] list = str.split(TestPlan.EXCLUDE_SEPARATOR);
        if ((list == null) || (list.length == 0)) {
            return null;
        }

        ArrayList<String> result = new ArrayList<String>();
        for (String s : list) {
            result.add(s);
        }

        return result;
    }

    /**
     * Get excluded list from a list by offered expectation.
     *
     * @param excludedList The list containing excluded items.
     * @param expectation The expectations.
     * @return The excluded list.
     */
    private ArrayList<String> getExcludedList(ArrayList<String> excludedList, String expectation) {
        if ((excludedList == null) || (excludedList.size() == 0)) {
            return null;
        }

        ArrayList<String> list = new ArrayList<String>();
        for (String str : excludedList) {
            if (str.startsWith(expectation)) {
                list.add(str);
            }
        }

        if (list.size() == 0) {
            return null;
        } else {
            return list;
        }
    }

    /**
     * Check if the expectation is fully matched among a list.
     *
     * @param list The array list.
     * @param expectation The expectation.
     * @return If there is full match of expectation among the list, return true;
     *         else, return false.
     */
    private boolean checkFullMatch(ArrayList<String> list, String expectation) {
        if ((list == null) || (list.size() == 0)) {
            return false;
        }

        for (String str : list) {
            if (str.equals(expectation)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load TestSuite via suite node. Package Name is used to output test result.
     *
     * @param pkg TestPackage
     * @param sNode suite node
     * @param excludedCaseList The list containing the excluded cases and sub types.
     * @return TestSuite
     */
    private TestSuite loadSuite(final TestPackage pkg, Node sNode,
                                ArrayList<String> excludedCaseList) {
        NodeList cNodes = sNode.getChildNodes();
        String fullSuiteName = getFullSuiteName(sNode);
        String suiteName = getStringAttributeValue(sNode, TestPlan.Attribute.NAME);
        TestSuite suite = new TestSuite(pkg, suiteName, fullSuiteName);

        for (int i = 0; i < cNodes.getLength(); i++) {
            Node cNode = cNodes.item(i);
            if (cNode.getNodeType() == Document.ELEMENT_NODE) {
                if (cNode.getNodeName().equals(TAG_TEST_SUITE)) {
                    String subSuiteName = getFullSuiteName(cNode);
                    if (checkFullMatch(excludedCaseList, subSuiteName) == false) {
                        ArrayList<String> excludedList = getExcludedList(excludedCaseList,
                                                             subSuiteName);
                        TestSuite subSuite = loadSuite(pkg, cNode, excludedList);
                        if ((subSuite.getTestCases().size() != 0)
                            || (subSuite.getSubSuites().size() != 0)) {
                            suite.addSubSuite(subSuite);
                        }
                    } else {
                        Log.d("suite=" + subSuiteName + " is fully excluded");
                    }
                } else if (cNode.getNodeName().equals(TAG_TEST_CASE)) {
                    String cName = getStringAttributeValue(cNode, ATTRIBUTE_NAME);
                    String priority = getStringAttributeValue(cNode, ATTRIBUTE_PRIORITY);

                    TestCase testCase = new TestCase(suite, cName, priority);
                    String fullCaseName = fullSuiteName + "." + testCase.getName();
                    if (checkFullMatch(excludedCaseList, fullCaseName) == false) {
                        NodeList mNodes = cNode.getChildNodes();
                        for (int t = 0; t < mNodes.getLength(); t ++) {
                            Node testNode = mNodes.item(t);
                            if ((testNode.getNodeType() == Document.ELEMENT_NODE)
                                    && (testNode.getNodeName().equals(TAG_TEST))) {
                                Test test = loadTest(pkg, testCase, testNode);
                                if (!checkFullMatch(excludedCaseList, test.getFullName())) {
                                    testCase.addTest(test);
                                } else {
                                    Log.d("Test=" + test.getFullName() + " is excluded");
                                }
                            }
                        }
                        if (testCase.getTests().size() != 0) {
                            suite.addTestCase(testCase);
                        }
                    } else {
                        Log.d("case=" + fullCaseName + " is fully excluded");
                    }
                }
            }
        }

        return suite;
    }

    /**
     * Load test via test node.
     *
     * @param pkg The test package.
     * @param testCase The test case.
     * @param testNode The test node.
     * @return The test loaded.
     */
    private Test loadTest(final TestPackage pkg, TestCase testCase,
            Node testNode) {
        String cType = getStringAttributeValue(testNode, ATTRIBUTE_TYPE);
        String name = getStringAttributeValue(testNode, ATTRIBUTE_NAME);
        String description = getStringAttributeValue(testNode,
                ATTRIBUTE_CONTROLLER);
        String knownFailure = getStringAttributeValue(testNode,
                ATTRIBUTE_KNOWN_FAILURE);
        String fullJarPath =
            HostConfig.getInstance().getCaseRepository().getRoot()
            + File.separator + pkg.getJarPath();
        CtsTestResult testResult = loadTestResult(testNode);
        Test test = null;
        if (pkg.isHostSideOnly()) {
            test = new HostSideOnlyTest(testCase, name, cType,
                    knownFailure,
                    CtsTestResult.CODE_NOT_EXECUTED);
            description = test.getFullName();
        } else {
            test = new Test(testCase, name, cType,
                    knownFailure,
                    CtsTestResult.CODE_NOT_EXECUTED);
        }

        TestController controller =
            genTestControler(fullJarPath, description);
        test.setTestController(controller);
        if (testResult != null) {
            test.addResult(testResult);
        }
        return test;
    }

    /**
     * Load the CTS test result from the test node.
     *
     * @param testNode The test node.
     * @return The CTS test result.
     */
    private CtsTestResult loadTestResult(Node testNode) {
        String result = getStringAttributeValue(testNode,
                TestSessionLog.ATTRIBUTE_RESULT);

        String failedMessage = null;
        String stackTrace = null;
        NodeList nodes = testNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i ++) {
            Node rNode = nodes.item(i);
            if ((rNode.getNodeType() == Document.ELEMENT_NODE)
                    && (rNode.getNodeName().equals(TestSessionLog.TAG_FAILED_SCENE))) {
                failedMessage = getStringAttributeValue(rNode, TestSessionLog.TAG_FAILED_MESSAGE);
                stackTrace = getStringAttributeValue(rNode, TestSessionLog.TAG_STACK_TRACE);
                if (stackTrace == null) {
                    NodeList sNodeList = rNode.getChildNodes();
                    for (int j = 0; j < sNodeList.getLength(); j ++) {
                        Node sNode = sNodeList.item(i);
                        if ((sNode.getNodeType() == Document.ELEMENT_NODE)
                                && (sNode.getNodeName().equals(TestSessionLog.TAG_STACK_TRACE))) {
                            stackTrace = sNode.getTextContent();
                        }
                    }
                }
                break;
            }
        }

        CtsTestResult testResult = null;
        if (result != null) {
            try {
                testResult = new CtsTestResult(result, failedMessage, stackTrace);
            } catch (InvalidTestResultStringException e) {
            }
        }

        return testResult;
    }

    /**
     * Generate controller according to the description string.
     *
     * @return The test controller.
     */
    public TestController genTestControler(String jarPath, String description) {
        if ((jarPath == null) || (jarPath.length() == 0)
                || (description == null) || (description.length() == 0)) {
            return null;
        }

        String packageName = description.substring(0, description.lastIndexOf("."));
        String className   = description.substring(packageName.length() + 1,
                             description.lastIndexOf(Test.METHOD_SEPARATOR));
        String methodName  = description.substring(
                             description.lastIndexOf(Test.METHOD_SEPARATOR) + 1,
                             description.length());

        return new TestController(jarPath, packageName, className, methodName);
    }

    /**
     * Get the full suite name of the specified suite node. Since the test
     * suite can be nested, so the full name of a tests suite is combined
     * with his name and his ancestor suite's names.
     *
     * @param node The specified suite node.
     * @return The full name of the given suite node.
     */
    private String getFullSuiteName(Node node) {
        StringBuilder buf = new StringBuilder();
        buf.append(getStringAttributeValue(node, TestPlan.Attribute.NAME));

        Node parent = node.getParentNode();
        while (parent != null) {
            if (parent.getNodeType() == Document.ELEMENT_NODE
                    && parent.getNodeName() == TAG_TEST_SUITE) {
                buf.insert(0, ".");
                buf.insert(0, getStringAttributeValue(parent, TestPlan.Attribute.NAME));
            }

            parent = parent.getParentNode();
        }

        return buf.toString();
    }

    /**
     * Create TestPlan which contain a series TestPackages.
     *
     * @param planName test plan name
     * @param packageNames Package names to be added
     * @param selectedResult The selected result mapping selected
     *                       package with selected removal result.
     */
    public void serialize(String planName,
            ArrayList<String> packageNames, HashMap<String, ArrayList<String>> selectedResult)
            throws ParserConfigurationException, FileNotFoundException, IOException,
            TransformerFactoryConfigurationError, TransformerException {
        File plan = new File(HostConfig.getInstance().getPlanRepository()
                .getPlanPath(planName));
        if (plan.exists()) {
            Log.e("Plan " + planName + " already exist, please use another name!",
                    null);
            return;
        }

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Node root = doc.createElement(TestPlan.Tag.TEST_PLAN);
        setAttribute(doc, root, ATTRIBUTE_VERSION, "1.0");
        doc.appendChild(root);

        // append device configure node
        Node deviceConfigNode = doc.createElement(TestPlan.Tag.PLAN_SETTING);

        root.appendChild(deviceConfigNode);

        // append test packages
        for (String pName : packageNames) {
            if (selectedResult.containsKey(pName)) {
                Node entryNode = doc.createElement(TestPlan.Tag.ENTRY);

                setAttribute(doc, entryNode, TestPlan.Attribute.URI, pName);
                ArrayList<String> excluded = selectedResult.get(pName);
                if ((excluded != null) && (excluded.size() != 0)) {
                    String excludedList = "";
                    for (String str : excluded) {
                        excludedList += str + TestPlan.EXCLUDE_SEPARATOR;
                    }
                    setAttribute(doc, entryNode, TestPlan.Attribute.EXCLUDE, excludedList);
                }
                root.appendChild(entryNode);
            }
        }

        writeToFile(plan, doc);
    }

}
