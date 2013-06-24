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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Builder of test session from the test result XML file.
 */
public class TestSessionLogBuilder extends XMLResourceHandler {
    private static TestSessionLogBuilder sInstance;

    private DocumentBuilder mDocBuilder;

    public static TestSessionLogBuilder getInstance()
            throws ParserConfigurationException {
        if (sInstance == null) {
            sInstance = new TestSessionLogBuilder();
        }

        return sInstance;
    }

    private TestSessionLogBuilder() throws ParserConfigurationException {
        mDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * Create TestSessionLog from the result XML file.
     *
     * @param resultFilePath The result file path.
     * @return TestSessionLog.
     */
    public TestSessionLog build(final String resultFilePath) throws SAXException, IOException,
            TestPlanNotFoundException, TestNotFoundException,
            NoSuchAlgorithmException, ParserConfigurationException {

        File file = new File(resultFilePath);
        if (!file.exists()) {
            throw new TestPlanNotFoundException();
        }

        Document doc = mDocBuilder.parse(file);
        return loadSessionLog(doc);
    }

    /**
     * Load TestSessionLog from a Test result DOM doc.
     *
     * @param doc Test result DOM Document.
     * @return loaded test session log from Test result DOM Document.
     */
    private TestSessionLog loadSessionLog(Document doc)
                throws NoSuchAlgorithmException, ParserConfigurationException,
                SAXException, IOException, TestPlanNotFoundException,
                TestNotFoundException {

        ArrayList<TestPackage> pkgsFromResult = new ArrayList<TestPackage>();
        NodeList resultList = doc.getElementsByTagName(TestSessionLog.TAG_TEST_RESULT);

        // currently, there should be just one test result tag in the result file
        Node resultNode = resultList.item(0);
        String planName = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_TESTPLAN);
        String start = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_STARTTIME);
        String end = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_ENDTIME);
        String planFilePath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSession sessionFromPlan = TestSessionBuilder.getInstance().build(planFilePath);

        NodeList pkgList = resultNode.getChildNodes();
        for (int i = 0; i < pkgList.getLength(); i++) {
            Node pkgNode = pkgList.item(i);
            if (pkgNode.getNodeType() == Document.ELEMENT_NODE
                    && TestSessionLog.TAG_TESTPACKAGE.equals(pkgNode.getNodeName())) {
                TestPackage pkg = TestSessionBuilder.getInstance().loadPackage(pkgNode, null);
                if (pkg != null) {
                    pkgsFromResult.add(pkg);
                }
            }
        }

        Collection<TestPackage> pkgsFromPlan = sessionFromPlan.getSessionLog().getTestPackages();
        for (TestPackage pkgFromPlan : pkgsFromPlan) {
            for (TestPackage pkgFromResult : pkgsFromResult) {
                if (pkgFromPlan.getAppPackageName().equals(pkgFromResult.getAppPackageName())) {
                    Collection<Test> testsFromPlan = pkgFromPlan.getTests();
                    Collection<Test> testsFromResult = pkgFromResult.getTests();
                    for (Test testFromPlan : testsFromPlan) {
                        for (Test testFromResult : testsFromResult) {
                            if (testFromPlan.getFullName().equals(testFromResult.getFullName())) {
                                CtsTestResult result = testFromResult.getResult();
                                testFromPlan.addResult(testFromResult.getResult());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        TestSessionLog log = new TestSessionLog(pkgsFromPlan, planName);
        try {
            log.setStartTime(HostUtils.dateFromString(start).getTime());
            log.setEndTime(HostUtils.dateFromString(end).getTime());
        } catch (NullPointerException ignored) {
            // use default time
        } catch (ParseException ignored) {
            // use default time
        }
        return log;
    }
}
