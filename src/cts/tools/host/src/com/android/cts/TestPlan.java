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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Define TestPlan tags and attributes.
 */
public class TestPlan extends XMLResourceHandler{
    public static final String EXCLUDE_SEPARATOR = ";";

    public interface Tag {
        public static final String TEST_SUITE = "TestSuite";
        public static final String ENTRY = "Entry";
        public static final String TEST_PLAN = "TestPlan";
        public static final String PLAN_SETTING = "PlanSettings";
        public static final String REQUIRED_DEVICE = "RequiredDevice";
        public static final String TEST_CASE = "TestCase";
    }

    public interface Attribute {
        public static final String NAME = "name";
        public static final String URI = "uri";
        public static final String EXCLUDE = "exclude";
        public static final String AMOUNT = "amount";
    }

    /**
     * Get test package names via test plan file path.
     *
     * @param planPath TestPlan configuration file path
     * @param removedPkgList The removed package list.
     * @return The package names.
     */
    public static Collection<String> getEntries(String planPath,
            ArrayList<String> removedPkgList)
            throws SAXException, IOException, ParserConfigurationException {
        ArrayList<String> entries = new ArrayList<String>();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        File planFile = new File(planPath);
        Document doc = builder.parse(planFile);

        NodeList pkgEntries = doc.getElementsByTagName(TestPlan.Tag.ENTRY);
        for (int i = 0; i < pkgEntries.getLength(); i++) {
            Node pkgEntry = pkgEntries.item(i);
            String uri = getStringAttributeValue(pkgEntry, TestPlan.Attribute.URI);

            String packageBinaryName = HostConfig.getInstance().getPackageBinaryName(uri);
            if (packageBinaryName != null) {
                entries.add(getStringAttributeValue(pkgEntry, TestPlan.Attribute.URI));
            } else {
                removedPkgList.add(uri);
            }
        }

        return entries;
    }

    /**
     * Check if the given package name is valid in the case repository.
     *
     * @param pkgName
     * @return if both the apk file and xml file exist, return true;
     *         else, return false.
     */
    public static boolean isValidPackageName(String pkgName) {
        String xmlPath = HostConfig.getInstance().getCaseRepository().getXmlPath(pkgName);
        String apkPath = HostConfig.getInstance().getCaseRepository().getApkPath(pkgName);
        File xmlFile = new File(xmlPath);
        File apkFile = new File(apkPath);

        if (xmlFile.exists() && apkFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
