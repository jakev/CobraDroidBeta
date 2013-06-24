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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.android.cts.HostConfig.CaseRepository;

/**
 * Test the console commands.
 */
public class ConsoleTests extends CtsTestBase {

    private String mPath1;
    private String mPath2;
    private String mPath3;

    /** {@inheritDoc} */
    @Override
    public void tearDown() {
        HostConfig.getInstance().removeTestPacakges();
        deleteTestPackage(mPath1);
        deleteTestPackage(mPath2);
        deleteTestPackage(mPath3);

        super.tearDown();
    }

    /**
     * Test adding package to test case repository and then getting the package names.
     */
    public void testAddPackage() throws Exception {
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());

        mPath1 = ROOT + File.separator + "com.google.android.cts.p1.zip";
        mPath2 = ROOT + File.separator + "com.google.android.cts.p2.zip";
        mPath3 = ROOT + File.separator + "net.sf.jlee.cts.p3.zip";

        final String expPackageName1 = "com.google.android.cts.p1";
        final String expPackageName2 = "com.google.android.cts.p2";
        final String expPackageName3 = "net.sf.jlee.cts.p3";

        HostConfig.getInstance().removeTestPacakges();

        ArrayList<String> pNames = HostConfig.getInstance().getCaseRepository()
                .getPackageBinaryNames();

        assertEquals(0, pNames.size());
        createTestPackageZip(mPath1, expPackageName1);
        createTestPackageZip(mPath2, expPackageName2);
        createTestPackageZip(mPath3, expPackageName3);

        // add package 1
        String cmdline = CTSCommand.ADD + " " + "-p" + " "
                + mPath1;
        cui.processCommand(CommandParser.parse(cmdline));

        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(1, pNames.size());
        assertTrue(pNames.contains(expPackageName1));

        // add package 2
        cmdline = CTSCommand.ADD + " " + "-p" + " " + mPath2;
        cui.processCommand(CommandParser.parse(cmdline));

        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(2, pNames.size());
        assertTrue(pNames.contains(expPackageName1));
        assertTrue(pNames.contains(expPackageName2));

        // add package 2
        cmdline = CTSCommand.ADD + " " + "-p" + " " + mPath3;
        cui.processCommand(CommandParser.parse(cmdline));

        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(3, pNames.size());
        assertTrue(pNames.contains(expPackageName1));
        assertTrue(pNames.contains(expPackageName2));
        assertTrue(pNames.contains(expPackageName3));

        deleteTestPackage(expPackageName1);
        deleteTestPackage(expPackageName2);
        deleteTestPackage(expPackageName3);
    }

    /**
     * Test removing package after adding the packages into the test case repository.
     */
    public void testRemovePackage() throws Exception {
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        mPath1 = ROOT + File.separator + "com.google.android.cts.p1.zip";
        mPath2 = ROOT + File.separator + "com.google.android.cts.p2.zip";
        mPath3 = ROOT + File.separator + "net.sf.jlee.cts.p3.zip";

        final String expPackageName1 = "com.google.android.cts.p1";
        final String expPackageName2 = "com.google.android.cts.p2";
        final String expPackageName3 = "net.sf.jlee.cts.p3";

        HostConfig.getInstance().removeTestPacakges();
        createTestPackageZip(mPath1, expPackageName1);
        createTestPackageZip(mPath2, expPackageName2);
        createTestPackageZip(mPath3, expPackageName3);

        // add package 1
        String cmdLine = CTSCommand.ADD + " -p " + mPath1;
        cui.processCommand(CommandParser.parse(cmdLine));
        cmdLine = CTSCommand.ADD + " -p " + mPath2;
        cui.processCommand(CommandParser.parse(cmdLine));
        cmdLine = CTSCommand.ADD + " -p " + mPath3;
        cui.processCommand(CommandParser.parse(cmdLine));

        ArrayList<String> pNames = HostConfig.getInstance().getCaseRepository()
                .getPackageBinaryNames();
        assertEquals(3, pNames.size());
        assertTrue(pNames.contains(expPackageName1));
        assertTrue(pNames.contains(expPackageName2));
        assertTrue(pNames.contains(expPackageName3));

        cmdLine = CTSCommand.REMOVE + " " + "-p" + " "
                + expPackageName1;
        cui.processCommand(CommandParser.parse(cmdLine));
        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(2, pNames.size());
        assertTrue(pNames.contains(expPackageName2));
        assertTrue(pNames.contains(expPackageName3));

        cmdLine = CTSCommand.REMOVE + " " + "-p" + " "
                + expPackageName2;
        cui.processCommand(CommandParser.parse(cmdLine));
        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(1, pNames.size());
        assertTrue(pNames.contains(expPackageName3));

        cmdLine = CTSCommand.REMOVE + " " + "-p" + " "
                + expPackageName3;
        cui.processCommand(CommandParser.parse(cmdLine));
        pNames = HostConfig.getInstance().getCaseRepository().getPackageBinaryNames();
        assertEquals(0, pNames.size());

        deleteTestPackage(expPackageName1);
        deleteTestPackage(expPackageName2);
        deleteTestPackage(expPackageName3);
    }

    /**
     * Test validating partial zipped package when adding package..
     */
    public void testValidatePartialZipPackageName() throws Exception {
        final String pkgName1 = "com.google.android.cts.apkPartial.zip";
        final String pkgName2 = "com.google.android.cts.xmlPartial.zip";
        mPath1 = ROOT + File.separator + pkgName1;
        mPath2 = ROOT + File.separator + pkgName2;

        HostConfig config = HostConfig.getInstance();
        HostConfig.CaseRepository caseRepos = config.getCaseRepository();

        createPartialTestPackageZip(mPath1, HostConfig.FILE_SUFFIX_APK);
        assertFalse(caseRepos.addPackage(mPath1));

        createPartialTestPackageZip(mPath2, HostConfig.FILE_SUFFIX_XML);
        assertFalse(caseRepos.addPackage(mPath2));
    }

    /**
     * Test validating package file suffix when adding package.
     */
    public void testValidatePackageSuffix() throws Exception {
        final String content = "test test test";
        final String pkgName1 = "com.google.android.cts.invalidSuffix.txt";
        mPath1 = ROOT + File.separator + pkgName1;

        createFile(content, mPath1);

        HostConfig config = HostConfig.getInstance();
        HostConfig.CaseRepository caseRepos = config.getCaseRepository();

        assertFalse(caseRepos.isValidPackageName(mPath1));
    }

    /**
     * Test validate duplicate package name by adding the same package twice.
     */
    public void testValidateDuplicatePackageName() throws Exception {
        final String name = "com.google.android.cts.mypackage";
        mPath1 = ROOT + File.separator + name + HostConfig.FILE_SUFFIX_ZIP;

        HostConfig config = HostConfig.getInstance();
        HostConfig.CaseRepository caseRepos = config.getCaseRepository();
        createTestPackageZip(mPath1, name);

        caseRepos.removePackages(name);
        assertTrue(caseRepos.addPackage(mPath1));
        assertFalse(caseRepos.isValidPackageName(mPath1));
        caseRepos.removePackages(name);

        deleteTestPackage(name);
    }

    /**
     * Create zipped test package.
     *
     * @param zipFilePath The file name with path.
     * @param packageName The package name.
     */
    private void createTestPackageZip(String zipFilePath, final String packageName)
            throws IOException {
        final String descriptionConfigStr = "<TestPackage name=\""
            + packageName + "\" " + "appPackageName=\"" + packageName
            + "\""
            + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
            + " runner=\"android.test.InstrumentationTestRunner\" jarPath=\"\">\n"
            + "  <Description>something extracted from java doc</Description>\n"
            + "  <TestSuite name=\"com.google\">\n"
            + "     <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
            + "         <Description>" + "something extracted from java doc"
            + "         </Description>\n"
            + "         <!-- Test Methods -->\n"
            + "         <Test method=\"testName1\" type=\"automatic\"/>\n"
            + "     </TestCase>\n"
            + "  </TestSuite>\n" + "</TestPackage>\n";

        createTestPackage(descriptionConfigStr, packageName);
        String apkFile = ROOT + File.separator + packageName + HostConfig.FILE_SUFFIX_APK;
        String xmlFile = ROOT + File.separator + packageName + HostConfig.FILE_SUFFIX_XML;
        String zipFile = ROOT + File.separator + packageName + ".zip";
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        addEntry(out, apkFile);
        addEntry(out, xmlFile);
        out.close();

        deleteTestPackage(packageName);
    }

    /**
     * Add entry into the zip ouput stream.
     *
     * @param out The zip output stream.
     * @param filePath The entry to be added into the zip output stream.
     */
    private void addEntry(ZipOutputStream out, String filePath) throws IOException {
        byte[] buf = new byte[1024];
        FileInputStream in = new FileInputStream(filePath);
        out.putNextEntry(new ZipEntry(filePath));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
    }

    /**
     * Create test package with the package name and the xml message as the content.
     *
     * @param xmlMsg The message as the content of the package.
     * @param packageName The package name.
     */
    @Override
    protected void createTestPackage(String xmlMsg, String packageName) throws IOException {
        String caseRoot = ROOT;

        String apkPath = caseRoot + File.separator + packageName + APK_SUFFIX;
        String xmlPath = caseRoot + File.separator + packageName + DESCRITION_SUFFIX;

        createFile(null, apkPath);
        createFile(xmlMsg, xmlPath);
    }
    /**
     * Create partial test package.
     *
     * @param zipFilePath The file name with path.
     * @param suffix The file suffix.
     */
    private void createPartialTestPackageZip(String zipFilePath, String suffix)
                 throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFilePath));

        String packageName = zipFilePath.substring(zipFilePath
                .lastIndexOf(File.separator), zipFilePath.lastIndexOf("."));
        String file = packageName + suffix;

        addFileToZip(file, out);

        out.close();
    }

    /**
     * Add file to zip output stream.
     *
     * @param filename The file to be added to the zip output stream.
     * @param out The zip output stream.
     */
    private void addFileToZip(String filename, ZipOutputStream out)
            throws IOException {
        out.putNextEntry(new ZipEntry(filename));

        out.closeEntry();
    }

    /**
     * Test listing package contents with different levels of expectation.
     */
    public void testListPackage() throws IOException, NoSuchAlgorithmException {

        List<ArrayList<String>> list = null;
        ArrayList<String> packageList = null;
        ArrayList<String> suiteList = null;
        ArrayList<String> caseList = null;
        ArrayList<String> testList = null;

        final String packageName = "com.google";
        final String suiteName   = "com.google.cts";
        final String caseName    = "CtsTest";
        final String testName    = "testHello";

        final String expect1 = "com";
        final String expect2 = "com.google";
        final String expect3 = "com.google.cts";
        final String expect4 = "com.google.cts.CtsTest";
        final String expect5 = "com.google.cts.CtsTest#testHello";
        final String expect6 = "com.google.cts.CtsTest#test";
        final CaseRepository caseRepository = HostConfig.getInstance()
                .getCaseRepository();

        final String descriptionConfigStr = "<TestPackage name=\""
                + packageName + "\" appPackageName=\"" + packageName
                + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\">\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "      <TestSuite name=\"" + suiteName + "\">\n"
                + "         <TestCase name=\"" + caseName + "\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Cases -->\n"
                + "             <Test name=\"" + testName + "\"" + " type=\"automatic\"" + ">\n"
                + "                 <Description>Simple deadloop test</Description>"
                + "             </Test>"
                + "        </TestCase>"
                + "     </TestSuite>\n"
                + "</TestPackage>\n";

        final String caseDescPath = caseRepository.getXmlPath(packageName);
        final String caseAPK = caseRepository.getApkPath(packageName);

        try {
            createFile(descriptionConfigStr, caseDescPath);
            createFile("", caseAPK);

            HostConfig.getInstance().loadTestPackages();

            list = caseRepository.listAvailablePackage(expect1);
            packageList = list.get(0);
            suiteList = list.get(1);
            caseList = list.get(2);
            testList = list.get(3);
            assertEquals(1, packageList.size());
            assertEquals(expect2, packageList.get(0));
            assertEquals(0, suiteList.size());
            assertEquals(0, caseList.size());
            assertEquals(0, testList.size());

            list = caseRepository.listAvailablePackage(expect2);
            packageList = list.get(0);
            suiteList = list.get(1);
            caseList = list.get(2);
            testList = list.get(3);
            assertEquals(0, packageList.size());
            assertEquals(1, suiteList.size());
            assertEquals(expect3, suiteList.get(0));
            assertEquals(0, caseList.size());
            assertEquals(0, testList.size());

            list = caseRepository.listAvailablePackage(expect3);
            packageList = list.get(0);
            suiteList = list.get(1);
            caseList = list.get(2);
            testList = list.get(3);
            assertEquals(0, packageList.size());
            assertEquals(0, suiteList.size());
            assertEquals(1, caseList.size());
            assertEquals(expect4, caseList.get(0));
            assertEquals(0, testList.size());

            list = caseRepository.listAvailablePackage(expect4);
            packageList = list.get(0);
            suiteList = list.get(1);
            caseList = list.get(2);
            testList = list.get(3);
            assertEquals(0, packageList.size());
            assertEquals(0, suiteList.size());
            assertEquals(0, caseList.size());
            assertEquals(1, testList.size());
            assertEquals(expect5, testList.get(0));

            list = caseRepository.listAvailablePackage(expect6);
            packageList = list.get(0);
            suiteList = list.get(1);
            caseList = list.get(2);
            testList = list.get(3);
            assertEquals(0, packageList.size());
            assertEquals(0, suiteList.size());
            assertEquals(0, caseList.size());
            assertEquals(1, testList.size());
            assertEquals(expect5, testList.get(0));
        } finally {
            deleteFile(caseDescPath);
            deleteFile(caseAPK);
        }
    }

    /**
     * Test starting console UI.
     */
    public void testStartUi() throws Exception {
        String cmdLine = CONFIG_PATH;
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        CommandParser cp = TestHost.init(cui, cmdLine.split(" "));
        assertEquals(null, cp);
        assertEquals(TestHost.MODE.CONSOLE, TestHost.sMode);

        cmdLine = ROOT;
        cp = TestHost.init(cui, cmdLine.split(" "));
        assertEquals(null, cp);
        assertEquals(TestHost.MODE.CONSOLE, TestHost.sMode);
    }

    /**
     * Test starting test plan directly when activating CTS from console.
     */
    public void testStartPlanDirectly() throws Exception {
        String cmdLine = "start --plan demo --config " + CONFIG_PATH;
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        CommandParser cp = TestHost.init(cui, cmdLine.split(" "));
        assertEquals(CTSCommand.START, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_CFG));
        assertTrue(cp.getValue(CTSCommand.OPTION_CFG) != null);
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertTrue(cp.getValue(CTSCommand.OPTION_PLAN) != null);
        cp.removeKey(CTSCommand.OPTION_CFG);
        assertFalse(cp.containsKey(CTSCommand.OPTION_CFG));
        assertEquals(null, cp.getValue(CTSCommand.OPTION_CFG));

        cmdLine = "start --plan demo --config " + ROOT;
        cp = TestHost.init(cui, cmdLine.split(" "));
        assertTrue(cp.containsKey(CTSCommand.OPTION_CFG));
        assertTrue(cp.getValue(CTSCommand.OPTION_CFG) != null);
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertTrue(cp.getValue(CTSCommand.OPTION_PLAN) != null);
        cp.removeKey(CTSCommand.OPTION_CFG);
        assertFalse(cp.containsKey(CTSCommand.OPTION_CFG));
        assertEquals(null, cp.getValue(CTSCommand.OPTION_CFG));
    }

    /**
     * Test starting package directly when activating CTS from console.
     */
    public void testStartPackageDirectly() throws Exception {
        String cmdLine = "start -p demo.zip --config " + CONFIG_PATH;
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        CommandParser cp = TestHost.init(cui, cmdLine.split(" "));
        assertEquals(CTSCommand.START, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_CFG));
        assertTrue(cp.getValue(CTSCommand.OPTION_CFG) != null);
        assertTrue(cp.containsKey(
                CTSCommand.OPTION_PACKAGE) || cp.containsKey(CTSCommand.OPTION_P));
        assertEquals("demo.zip", cp.getValue(CTSCommand.OPTION_PACKAGE));
        cp.removeKey(CTSCommand.OPTION_CFG);
        assertFalse(cp.containsKey(CTSCommand.OPTION_CFG));
        assertEquals(null, cp.getValue(CTSCommand.OPTION_CFG));

        cmdLine = "start --package demo.zip --config " + ROOT;
        cp = TestHost.init(cui, cmdLine.split(" "));
        assertTrue(cp.containsKey(CTSCommand.OPTION_CFG));
        assertTrue(cp.getValue(CTSCommand.OPTION_CFG) != null);
        assertTrue(cp.containsKey(
                CTSCommand.OPTION_PACKAGE) || cp.containsKey(CTSCommand.OPTION_P));
        assertEquals("demo.zip", cp.getValue(CTSCommand.OPTION_PACKAGE));
        cp.removeKey(CTSCommand.OPTION_CFG);
        assertFalse(cp.containsKey(CTSCommand.OPTION_CFG));
        assertEquals(null, cp.getValue(CTSCommand.OPTION_CFG));
    }

    /**
     * Test getting device ID from the device ID string list.
     */
    public void testGetDeviceId() {
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        ArrayList<TestDevice> devList = new ArrayList<TestDevice>();
        devList.add(new TestDevice("dev-100"));
        devList.add(new TestDevice("dev-101"));
        TestDevice[] devices = devList.toArray(new TestDevice[4]);

        int deviceId = cui.getDeviceId(devices, "dev-100");
        assertEquals(0, deviceId);

        deviceId = cui.getDeviceId(devices, "dev-101");
        assertEquals(1, deviceId);
    }
}
