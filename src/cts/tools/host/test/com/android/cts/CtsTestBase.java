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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Set up the test environment and offer the utility APIs.
 */
public abstract class CtsTestBase extends TestCase {
    public static final String APK_SUFFIX = ".apk";
    public static final String DESCRITION_SUFFIX = ".xml";

    protected static final String ROOT = "tmp";
    protected static final String CONFIG_PATH = ROOT + File.separator + "host_config.xml";

    private static final String CASE_REPOSITORY = "case_rep_demo";
    private static final String RESULT_REPOSITORY = "result_rep_demo";
    private static final String PLAN_REPOSITORY = "plan_rep_demo";

    /** {@inheritDoc} */
    @Override
    public void setUp() {
        // create root direcoty for the test
        new File(ROOT).mkdirs();

        initConfig();
        Log.initLog(ROOT);
    }

    /** {@inheritDoc} */
    @Override
    public void tearDown() {
        Log.closeLog();
        clearDirectory(ROOT);
    }

    /**
     * Initialize the configuration for tests.
     */
    private void initConfig() {
        StringBuilder buf = new StringBuilder();

        buf.append("<HostConfiguration>");
        buf.append("\t<Repository root=\"" + ROOT + "\" >");
        buf.append("\t\t<TestPlan path=\"" + PLAN_REPOSITORY + "\" />");
        buf.append("\t\t<TestCase path=\"" + CASE_REPOSITORY + "\" />");
        buf.append("\t\t<TestResult path=\"" + RESULT_REPOSITORY + "\" />");
        buf.append("\t</Repository>");
        buf.append("</HostConfiguration>");
        try {
            new File(ROOT + File.separator + PLAN_REPOSITORY).mkdirs();
            new File(ROOT + File.separator + CASE_REPOSITORY).mkdirs();
            new File(ROOT + File.separator + RESULT_REPOSITORY).mkdirs();
            createFile(buf.toString(), CONFIG_PATH);

        } catch (IOException e1) {
            fail("Can't create config file");
        }

        try {
            TestHost.loadConfig(CONFIG_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't initiate config");
        }
    }

    /**
     * Create test package with the package name and the xml message as the content.
     * 
     * @param xmlMsg The message as the content of the package.
     * @param packageName The package name.
     */
    protected void createTestPackage(String xmlMsg, String packageName) throws IOException {
        String caseRoot = HostConfig.getInstance().getCaseRepository()
                .getRoot();

        String apkPath = caseRoot + File.separator + packageName + APK_SUFFIX;
        String xmlPath = caseRoot + File.separator + packageName
                + DESCRITION_SUFFIX;

        createFile(null, apkPath);
        createFile(xmlMsg, xmlPath);
    }

    /**
     * Delete the test package.
     */
    protected void deleteTestPackage(String path) {
        String apkPath = path + File.separator + APK_SUFFIX;
        String desPath = path + File.separator + DESCRITION_SUFFIX;

        deleteFile(apkPath);
        deleteFile(desPath);
    }

    /**
     * Create the specified file with the specified content.
     * 
     * @param content The content written into the file.
     * @param filePath The file to be created.
     */
    protected void createFile(String content, String filePath)
            throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        if (content != null) {
            out.write(content);
        }

        out.close();
    }

    /**
     * Delete the specified file.
     * 
     * @param path The file to be deleted.
     */
    protected void deleteFile(String path) {
        File f = new File(path);

        if (f.exists() && f.canWrite()) {
            f.delete();
        }
    }

    /**
     * Clear the directory by deleting the files and directories under it.
     * 
     * @param path The directory to be cleared.
     */
    private void clearDirectory(String path) {
        File root = new File(path);
        for (File f : root.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirectory(f);
            }
        }

        root.delete();
    }

    /**
     * Deleted the directory, including the files and sub-directories under it.
     * 
     * @param path The directory to be deleted.
     */
    private void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }

            path.delete();
        }
    }
}
