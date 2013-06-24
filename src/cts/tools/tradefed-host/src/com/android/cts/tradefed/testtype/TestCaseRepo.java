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

import com.android.ddmlib.Log;
import com.android.tradefed.testtype.IRemoteTest;
import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Retrieves CTS test case definitions from the repository.
 */
class TestCaseRepo implements ITestCaseRepo {

    private static final String LOG_TAG = "TestCaseRepo";

    private File mTestCaseDir;

    /** mapping of uri to test definition */
    private Map<String, TestPackageDef> mTestMap;

    /**
     * Creates a {@link TestCaseRepo}, initialized from provided repo files
     *
     * @param testCaseDir directory containing all test case definition xml and build files
     */
    public TestCaseRepo(File testCaseDir) {
        mTestCaseDir = testCaseDir;
        mTestMap = new Hashtable<String, TestPackageDef>();
        parse(mTestCaseDir);
    }

    /**
     * Builds mTestMap based on directory contents
     */
    private void parse(File dir) {
        File[] xmlFiles = dir.listFiles(new XmlFilter());
        for (File xmlFile : xmlFiles) {
            parseTestFromXml(xmlFile);
        }
    }

    /**
     * @param xmlFile
     * @throws ParseException
     */
    private void parseTestFromXml(File xmlFile)  {
        TestPackageXmlParser parser = new TestPackageXmlParser();
        try {
            parser.parse(createStreamFromFile(xmlFile));
            TestPackageDef def = parser.getTestPackageDef();
            if (def != null) {
                mTestMap.put(def.getUri(), def);
            } else {
                Log.w(LOG_TAG, String.format("Could not find test package info in xml file %s",
                        xmlFile.getAbsolutePath()));
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, String.format("Could not find test case xml file %s",
                    xmlFile.getAbsolutePath()));
            Log.e(LOG_TAG, e);
        } catch (ParseException e) {
            Log.e(LOG_TAG, String.format("Failed to parse test case xml file %s",
                    xmlFile.getAbsolutePath()));
            Log.e(LOG_TAG, e);
        }
    }

    /**
     * Helper method to create a stream to read data from given file
     * <p/>
     * Exposed for unit testing
     *
     * @param xmlFile
     * @return
     *
     */
    InputStream createStreamFromFile(File xmlFile) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(xmlFile));
    }

    private static class XmlFilter implements FilenameFilter {

        /**
         * {@inheritDoc}
         */
        public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IRemoteTest> getTests(Collection<String> testUris) {
        Collection<IRemoteTest> tests = new ArrayList<IRemoteTest>(testUris.size());
        for (String uri : testUris) {
            TestPackageDef def = mTestMap.get(uri);
            if (def != null) {
                IRemoteTest test = def.createTest(mTestCaseDir);
                if (test != null) {
                    tests.add(test);
                } else {
                    Log.w(LOG_TAG, String.format("Failed to create test from package uri %s", uri));
                }
            } else {
                Log.w(LOG_TAG, String.format("Could not find test with uri %s", uri));
            }
        }
        return tests;
    }
}
