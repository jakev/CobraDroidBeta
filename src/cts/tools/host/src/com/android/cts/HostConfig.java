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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Holds CTS host configuration information, such as:
 * <ul>
 *    <li> test case repository
 *    <li> test plan repository
 *    <li> test result repository
 * </ul>
 */
public class HostConfig extends XMLResourceHandler {
    public static boolean DEBUG = false;

    public static final String ALL = "all";

    static final String SIGNATURE_TEST_PACKAGE_NAME = "SignatureTest";
    static final String DEFAULT_HOST_CONFIG_FILE_NAME = "host_config.xml";
    static final String FILE_SUFFIX_XML = ".xml";
    static final String FILE_SUFFIX_APK = ".apk";
    static final String FILE_SUFFIX_ZIP = ".zip";
    static final String FILE_SUFFIX_JAR = ".jar";
    static final String[] CTS_RESULT_RESOURCES = {"cts_result.xsl", "cts_result.css",
                                                  "logo.gif", "newrule-green.png"};

    private String mConfigRoot;
    private String mLogRoot;
    private CaseRepository mCaseRepos;
    private ResultRepository mResultRepos;
    private PlanRepository mPlanRepos;

    // key: app package name
    // value: TestPackage
    private HashMap<String, TestPackage> mTestPackageMap;

    enum Ints {
        // Number of tests executed between reboots. A value <= 0 disables reboots.
        maxTestCount (200),
        // Max size [tests] for a package to be run in batch mode
        maxTestsInBatchMode (0),
        // Max time [ms] between test status updates for both individual and batch mode.
        testStatusTimeoutMs (5 * 60 * 1000),
        // Max time [ms] from start of package in batch mode and the first test status update.
        batchStartTimeoutMs (30 * 60 * 1000),
        // Max time [ms] from start of test in individual mode to the first test status update.
        individualStartTimeoutMs (5 * 60 * 1000),
        // Timeout [ms] for the signature check
        signatureTestTimeoutMs (10 * 60 * 1000),
        // Timeout [ms] for package installations
        packageInstallTimeoutMs (2 * 60 * 1000),
        // Time to wait [ms] after a package installation or removal
        postInstallWaitMs (30 * 1000);

        private int value;

        Ints(int value) {
            this.value = value;
        }

        int value() {
            return value;
        }

        void setValue(int value) {
            this.value = value;
        }
    }

    private final static HostConfig sInstance = new HostConfig();

    private HostConfig() {
        mTestPackageMap = new HashMap<String, TestPackage>();
    }

    public static HostConfig getInstance() {
        return sInstance;
    }

    /**
     * Returns the max number of tests to run between reboots. A value of 0 or smaller indicates
     * that reboots should not be used.
     */
    public static int getMaxTestCount() {
        return Ints.maxTestCount.value();
    }

    /**
     * Load configuration.
     *
     * @param configPath The configuration path.
     * @return If succeed in loading, return true; else, return false.
     */
    public boolean load(String configPath) throws SAXException, IOException,
            ParserConfigurationException {

        String fileName = null;
        String[] subDirs = configPath.split("\\" + File.separator);
        for (String d : subDirs) {
            if (d.contains(FILE_SUFFIX_XML)) {
                fileName = d;
            }
        }

        String configFile = null;
        if (fileName == null) {
            //remove the possible trailing "/" of the path
            if (File.separatorChar == configPath.charAt(configPath.length() - 1)) {
                configPath = configPath.substring(0, configPath.length() - 1);
            }
            mConfigRoot = configPath;
            fileName = DEFAULT_HOST_CONFIG_FILE_NAME;
        } else {
            mConfigRoot = configPath.substring(0, configPath.length() - fileName.length() - 1);
        }
        configFile = mConfigRoot + File.separator + fileName;

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new File(configFile));

        String repositoryRoot = getStringAttributeValue(doc
                .getElementsByTagName("Repository").item(0), "root");
        if ((null == repositoryRoot) || (repositoryRoot.length() == 0)) {
            repositoryRoot = mConfigRoot;
        }

        String caseCfg = getStringAttributeValue(doc, "TestCase", "path", fileName);
        String planCfg = getStringAttributeValue(doc, "TestPlan", "path", fileName);
        String resCfg = getStringAttributeValue(doc, "TestResult", "path", fileName);
        if ((caseCfg == null) || (planCfg == null) || (resCfg == null)) {
            return false;
        }

        getConfigValues(doc);

        String caseRoot = repositoryRoot + File.separator + caseCfg;
        String planRoot = repositoryRoot + File.separator + planCfg;
        String resRoot = repositoryRoot + File.separator + resCfg;

        String logCfg = getStringAttributeValueOpt(doc, "TestLog", "path", fileName);
        if (null == logCfg) {
            mLogRoot = mConfigRoot;
        } else {
            mLogRoot = repositoryRoot + File.separator + logCfg;
        }

        boolean validCase = true;
        if (!validateDirectory(caseRoot)) {
            validCase = new File(caseRoot).mkdirs();
        }
        boolean validRes = true;
        if (!validateDirectory(resRoot)) {
            validRes = new File(resRoot).mkdirs();
        }
        if (validRes) {
            extractResultResources(resRoot);
        }
        boolean validPlan = true;
        if (!validateDirectory(planRoot)) {
            validPlan = new File(planRoot).mkdirs();
        }
        boolean validLog = true;
        if (!validateDirectory(mLogRoot)) {
            validLog = new File(mLogRoot).mkdirs();
        }

        mCaseRepos = new CaseRepository(caseRoot);
        mResultRepos = new ResultRepository(resRoot);
        mPlanRepos = new PlanRepository(planRoot);

        return validCase && validRes && validPlan && validLog;
    }

    /**
     * Extract the result resources into the specified directory.
     *
     * @param resRoot the directory to extract the resources into.
     */
    public void extractResultResources(String resRoot) {
        for (String res: CTS_RESULT_RESOURCES) {
            extractResource(res, resRoot);
        }
    }

    /**
     * Get the test packages.
     *
     * @return The test packages.
     */
    public Collection<TestPackage> getTestPackages() {
        return mTestPackageMap.values();
    }

    /**
     * Get the test package by the JAVA package name of the test package.
     *
     * @param packageName The JAVA package name.
     * @return The test package.
     */
    public TestPackage getTestPackage(final String packageName) {
        return mTestPackageMap.get(packageName);
    }

    /**
     * Load repositories.
     */
    public void loadRepositories() throws NoSuchAlgorithmException {
        loadTestPackages();
        loadTestResults();
    }

    /**
     * Load test results to create session accordingly.
     */
    private void loadTestResults() {
        getResultRepository().loadTestResults();
    }

    /**
     * Load all of the test packages.
     */
    public void loadTestPackages() throws NoSuchAlgorithmException {
        if (mTestPackageMap.size() == 0) {
            mCaseRepos.loadTestPackages();
        }
    }

    /**
     * Remove all of the test packages.
     */
    public void removeTestPacakges() {
        mTestPackageMap.clear();
    }

    /**
     * Get the package binary name.
     *
     * @param appPackageName The JAVA package name.
     * @return The binary name of the package.
     */
    public String getPackageBinaryName(String appPackageName) {

        for (TestPackage pkg : mTestPackageMap.values()) {
            if (appPackageName.equals(pkg.getAppPackageName())) {
                return pkg.getAppBinaryName();
            }
        }

        return null;
    }

    /**
     * Get the root directory of configuration.
     *
     * @return The root directory of configuration.
     */
    public String getConfigRoot() {
        return mConfigRoot;
    }

    /**
     * Get the root directory of log files.
     *
     * @return the root directory of log files.
     */
    public String getLogRoot() {
        return mLogRoot;
    }

    /**
     * Get string attribute value.
     *
     * @param doc The document.
     * @param tagName The tag name.
     * @param attrName The attribute name.
     * @param fileName The file name.
     * @return The attribute value.
     */
    private String getStringAttributeValue(final Document doc,
            final String tagName, final String attrName, final String fileName) {

        String cfgStr = null;
        try {
            cfgStr = getStringAttributeValue(doc
                    .getElementsByTagName(tagName).item(0), attrName);
            if ((null == cfgStr) || (cfgStr.length() == 0)) {
                Log.e("Configure error (in " + fileName
                        + "), pls make sure <" + tagName + ">'s attribute <"
                        + attrName + ">'s value is correctly set.", null);
                return null;
            }
        } catch (Exception e) {
            Log.e("Configure error (in " + fileName
                    + "), pls make sure <" + tagName
                    + ">'s value is correctly set.", null);
            return null;
        }

        return cfgStr;
    }

    /**
     * Get string attribute value if it exists.
     *
     * @param doc The document.
     * @param tagName The tag name.
     * @param attrName The attribute name.
     * @param fileName The file name.
     * @return The attribute value.
     */
    private String getStringAttributeValueOpt(final Document doc,
            final String tagName, final String attrName, final String fileName) {

        String cfgStr = null;
        try {
            cfgStr = getStringAttributeValue(doc
                    .getElementsByTagName(tagName).item(0), attrName);
        } catch (Exception e) {
            return null;
        }

        return cfgStr;
    }

    /**
     * Load configuration values from config file.
     *
     * @param doc The document from which to load the values.
     */
    private void getConfigValues(final Document doc) {
        NodeList intValues = doc.getElementsByTagName("IntValue");
        for (int i = 0; i < intValues.getLength(); i++) {
            Node n = intValues.item(i);
            String name = getStringAttributeValue(n, "name");
            String value = getStringAttributeValue(n, "value");
            try {
                Integer v = Integer.parseInt(value);
                Ints.valueOf(name).setValue(v);
            } catch (NumberFormatException e) {
                Log.e("Configuration error. Illegal value for " + name, e);
            } catch (IllegalArgumentException e) {
                Log.e("Unknown configuration value " + name, e);
            }
        }
    }

    /**
     * Validate the directory.
     *
     * @param path The path to be validated.
     * @return If valid directory, return true; else, return false.
     */
    private boolean validateDirectory(final String path) {

        File pathFile = new File(path);
        if ((null == pathFile) || (pathFile.exists() == false)
                || (pathFile.isDirectory() == false)) {
            return false;
        }

        return true;
    }

    /**
     * Extract a resource into the given destination directory. This is used
     * for resource files such as images and CSS.
     *
     * @param name The name of the resource.
     * @param dest The directory the resource should be extracted to.
     * @return true, if successful, or the file already existed.
     */
    private boolean extractResource(String name, String dest) {
        File file = new File(dest, name);
        if (!file.exists()) {
            // do not extract again if the file is already there
            InputStream in = getClass().getResourceAsStream(File.separator + name);
            if (in != null) {
                try {
                    FileOutputStream fout = new FileOutputStream(file);
                    byte[] data = new byte[512];
                    int len = in.read(data);
                    while (len > 0) {
                        fout.write(data, 0, len);
                        len = in.read(data);
                    }
                    fout.flush();
                    fout.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the case repository.
     *
     * @return The case repository.
     */
    public CaseRepository getCaseRepository() {
        return mCaseRepos;
    }

    /**
     * Get the plan repository.
     *
     * @return The plan repository.
     */
    public PlanRepository getPlanRepository() {
        return mPlanRepos;
    }

    /**
     * Get the result repository.
     *
     * @return The result repository.
     */
    public ResultRepository getResultRepository() {
        return mResultRepos;
    }

    /**
     * Storing the root information of some repository.
     *
     */
    class Repository {
        protected String mRoot;

        Repository(String root) {
            mRoot = root;
        }

        /**
         * Get the root of the repository.
         *
         * @return The root of the repository.
         */
        public String getRoot() {
            return mRoot;
        }

        /**
         * Check if the specified file is a valid XML file.
         *
         * @param f The file to be valid.
         * @return If valid XML file, return true; else, return false.
         */
        public boolean isValidXmlFile(File f) {
            if (f.getPath().endsWith(FILE_SUFFIX_XML)) {
                return true;
            }

            return false;
        }
    }

    /**
     * Storing the information of result repository.
     */
    class ResultRepository extends Repository {

        ResultRepository(String root) {
            super(root);
        }

        /**
         * Load test results to create session accordingly.
         */
        public void loadTestResults() {

            for (File f : new File(mRoot).listFiles()) {
                if (f.isDirectory()) {
                    String pathName = mRoot + File.separator + f.getName()
                                + File.separator + TestSessionLog.CTS_RESULT_FILE_NAME;
                    if (HostUtils.isFileExist(pathName)) {
                        try {
                            TestSessionLog log =
                                TestSessionLogBuilder.getInstance().build(pathName);
                            TestSession ts = TestSessionBuilder.getInstance().build(log);
                            if (ts != null) {
                                TestHost.getInstance().addSession(ts);
                            }
                        } catch (Exception e) {
                            Log.e("Error importing existing result from " + pathName, e);
                        }
                    }
                }
            }
        }
     }

    /**
     * Storing the information of case repository.
     */
    class CaseRepository extends Repository {
        CaseRepository(String root) {
            super(root);
        }

        /**
         * Get package names.
         *
         * @return The JAVA package names.
         */
        public ArrayList<String> getPackageNames() {
            ArrayList<String> packageNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                String binaryName = pkg.getAppBinaryName();
                if (binaryName.equals(SIGNATURE_TEST_PACKAGE_NAME)) {
                    packageNames.add(0, binaryName);
                } else {
                    packageNames.add(pkg.getAppPackageName());
                }
            }

            return packageNames;
        }

        /**
         * Get package binary names.
         *
         * @return The package binary names.
         */
        public ArrayList<String> getPackageBinaryNames() {
            ArrayList<String> pkgBinaryNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                String pkgBinaryName = pkg.getAppBinaryName();
                if (pkgBinaryName.equals(SIGNATURE_TEST_PACKAGE_NAME)) {
                    pkgBinaryNames.add(0, pkgBinaryName);
                } else {
                    pkgBinaryNames.add(pkg.getAppBinaryName());
                }
            }

            return pkgBinaryNames;
        }

        /**
         * Load package XML file names.
         *
         * @return The package XML file names.
         */
        public List<String> loadPackageXmlFileNames() {
            ArrayList<String> packageXmlFileNames = new ArrayList<String>();

            for (File f : new File(mRoot).listFiles()) {
                if (isValidXmlFile(f)) {
                    String fileName = f.getName();
                    String name = fileName.substring(0, fileName.lastIndexOf("."));
                    packageXmlFileNames.add(name);
                }
            }

            return packageXmlFileNames;
        }

        /**
         * Load test packages.
         */
        public void loadTestPackages() throws NoSuchAlgorithmException {
            List<String> pkgXmlFileNameList = loadPackageXmlFileNames();
            for (String pkgXmlFileName : pkgXmlFileNameList) {
                String xmlPath = getRoot() + File.separator
                        + pkgXmlFileName + FILE_SUFFIX_XML;
                TestPackage pkg = loadPackage(xmlPath);
                if (isValidPackage(pkg)) {
                    mTestPackageMap.put(pkg.getAppPackageName(), pkg);
                }
            }
        }

        /**
         * Get package binary name.
         *
         * @param packagePath The package path.
         * @return The package binary name.
         */
        private String getPackageBinaryName(String packagePath) {
            return packagePath.substring(packagePath.lastIndexOf(File.separator) + 1,
                    packagePath.lastIndexOf("."));
        }

        /**
         * Check if the specified package is a valid package.
         *
         * @param pkg The specified package to be checked.
         * @return If valid package, return true; else, return false.
         */
        private boolean isValidPackage(TestPackage pkg) {
            if (pkg == null) {
                return false;
            }

            String pkgFileName = pkg.getAppBinaryName();
            String apkFilePath = mRoot + File.separator + pkgFileName + FILE_SUFFIX_APK;
            String xmlFilePath = mRoot + File.separator + pkgFileName + FILE_SUFFIX_XML;
            File xmlFile = new File(xmlFilePath);
            if (pkg.isHostSideOnly()) {
                if (xmlFile.exists() && xmlFile.isFile()) {
                    return true;
                }
            } else {
                File apkFile = new File(apkFilePath);
                if (xmlFile.exists() && xmlFile.isFile()
                        && apkFile.exists() && apkFile.isFile()) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Add package to case repository.
         *
         * @param packagePath The package to be added.
         * @return If valid package and succeed in add it, return true; else, return false.
         */
        public boolean addPackage(String packagePath) throws FileNotFoundException,
                IOException, NoSuchAlgorithmException {
            ZipFile zipFile = new ZipFile(packagePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            ArrayList<String> filePathList = new ArrayList<String>();
            String xmlFilePath = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                String name = entry.getName();
                if (name.endsWith(FILE_SUFFIX_APK)
                        || name.endsWith(FILE_SUFFIX_XML)
                        || name.endsWith(FILE_SUFFIX_JAR)) {
                    int index = name.lastIndexOf(File.separator);
                    String fileName = name;
                    if (index != -1) {
                        fileName = name.substring(index + 1);
                    }
                    String filePath = mRoot + File.separator + fileName;
                    writeToFile(zipFile.getInputStream(entry), filePath);
                    filePathList.add(filePath);
                    if (name.endsWith(FILE_SUFFIX_XML)) {
                        xmlFilePath = filePath;
                    }
                }
            }

            String packageName = getPackageBinaryName(packagePath);
            PackageZipFileValidator zipValidator = new PackageZipFileValidator();
            if (zipValidator.validate(filePathList, packageName, xmlFilePath) == false) {
                for (String filePath : filePathList) {
                    deleteFile(filePath);
                }
                return false;
            }

            TestPackage pkg = loadPackage(xmlFilePath);
            if (pkg != null) {
                mTestPackageMap.put(pkg.getAppPackageName(), pkg);
            }

            return true;
        }

        /**
         * Load the package from the package description XML file.
         *
         * @param xmlFileName The package description XML file.
         * @return The TestPackage.
         */
        private TestPackage loadPackage(String xmlFileName) throws NoSuchAlgorithmException {
            if ((xmlFileName == null) || (xmlFileName.length() == 0)) {
                return null;
            }

            File xmlFile = new File(xmlFileName);
            TestSessionBuilder sessionBuilder;
            TestPackage pkg = null;
            try {
                sessionBuilder = TestSessionBuilder.getInstance();
                pkg = sessionBuilder.loadPackage(xmlFile, null);
            } catch (ParserConfigurationException e) {
            } catch (SAXException e) {
            } catch (IOException e) {
            }
            return pkg;
        }

        /**
         * check if the packagePath is valid against the case repository
         *
         * @param packagePath the path to be checked
         * @return if the path isn't suffixed with .zip, return false;
         *         if the package name exists in case repository, return false;
         *         for other conditions, return true;
         */
        public boolean isValidPackageName(String packagePath) {
            if (!packagePath.endsWith(FILE_SUFFIX_ZIP)) {
                Log.e("Package error: package name " + packagePath + " is not a zip file.", null);
                return false;
            }

            String fileName = packagePath.substring(packagePath.lastIndexOf(File.separator) + 1,
                       packagePath.length() - FILE_SUFFIX_ZIP.length());

            String path = mRoot + File.separator + fileName;
            if (HostUtils.isFileExist(path + FILE_SUFFIX_APK)
                    || HostUtils.isFileExist(path + FILE_SUFFIX_XML)) {
                Log.e("Package error: package name " + fileName + " exists already.", null);
                return false;
            }

            return true;
        }

        /**
         * Validate zipped package file against package logic
         */
        class PackageZipFileValidator {
            /**
             * validate the package content to see if it contains enough data
             **
             * @param filePathList The file path list contained in the package zip file.
             * @param packageName The package name.
             * @param xmlFilePath The description XML file path.
             * @return If valid, return true; else, return false.
             */
            public boolean validate(ArrayList<String> filePathList, String packageName,
                    String xmlFilePath) throws NoSuchAlgorithmException {
                if (xmlFilePath == null) {
                    Log.e("Package error: package doesn't contain XML file: "
                            + packageName + FILE_SUFFIX_XML, null);
                    return false;
                } else {
                    TestPackage pkg = loadPackage(xmlFilePath);
                    if (pkg == null) {
                        Log.e("Package error: the description XML file contained in : "
                                + packageName + FILE_SUFFIX_APK + " is invalid.", null);
                        return false;
                    } else {
                        if (!validateTargetApk(filePathList, pkg.getTargetBinaryName())) {
                            return false;
                        }

                        if (!validateHostControllerJar(filePathList, pkg.getJarPath())) {
                            return false;
                        }

                        String apkFilePath = mRoot + File.separator
                                + packageName + FILE_SUFFIX_APK;
                        if (!filePathList.contains(apkFilePath)) {
                            Log.e("Package error: package doesn't contain APK file: "
                                            + packageName + FILE_SUFFIX_APK, null);
                            return false;
                        }
                    }
                }

                return true;
            }

            /**
             * Validate host controller jar file described in the package description XML file.
             *
             * @param filePathList  The files contained in the zipped package file.
             * @param hostControllerJarPath The host controller jar file path.
             * @return If the host controller jar file contained in the zipped package,
             *         return true; else, return false.
             */
            private boolean validateHostControllerJar(ArrayList<String> filePathList,
                    String hostControllerJarPath) {
                if ((hostControllerJarPath != null) && (hostControllerJarPath.length() != 0)) {
                    String targetFilePath =
                        mRoot + File.separator + hostControllerJarPath + FILE_SUFFIX_JAR;
                    if (filePathList.contains(targetFilePath)) {
                        return true;
                    }
                } else {
                    return true;
                }

                //String jarFileName = getPackageName(hostControllerJarPath);
                Log.e("Package error: host controler jar file "
                        + hostControllerJarPath + FILE_SUFFIX_JAR
                        + " is not contained in the package zip file.", null);
                return false;
            }

            /**
             * Validate target APK file described in the package description XML file.
             *
             * @param filePathList The files contained in the zipped package file.
             * @param targetName The target APK name.
             * @return If the target APK file contained in the zipped package file, return true;
             *         else, return false.
             */
            private boolean validateTargetApk(ArrayList<String> filePathList, String targetName) {
                if ((targetName != null) && (targetName.length() != 0)) {
                    String targetFileName = mRoot + File.separator + targetName + FILE_SUFFIX_APK;
                    if (filePathList.contains(targetFileName)) {
                        return true;
                    }
                } else {
                    return true;
                }

                Log.e("Package error: target file " + targetName + FILE_SUFFIX_APK
                        + " is not contained in the package zip file.", null);
                return false;
            }
        }

        /**
         * Write the input stream to file.
         *
         * @param in The input stream.
         * @param path The file to write to.
         */
        private void writeToFile(InputStream in, String path) throws IOException {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }

            in.close();
            out.close();
        }

        /**
         * Remove packages from case repository.
         *
         * @param packageName Package to be removed.
         */
        public void removePackages(String packageName) {
            if ((packageName == null) || (packageName.length() == 0)) {
                return;
            }

            if (packageName.equals(ALL)) {
                ArrayList<String> packageNames = getCaseRepository().getPackageNames();
                for (String pkgName : packageNames) {
                    removePackage(pkgName);
                }
            } else {
                if (!getPackageNames().contains(packageName)) {
                    Log.e("Package " + packageName + " doesn't exist in repository!", null);
                    return;
                }
                removePackage(packageName);
            }
        }

        /**
         * Remove the specified package.
         *
         * @param packageName The package name.
         */
        private void removePackage(String packageName) {
            TestPackage pkg = getTestPackage(packageName);
            if (pkg != null) {
                ArrayList<String> targetBinaryNames = getTargetBinaryNames();
                String targetBinaryName = pkg.getTargetBinaryName();
                if ((targetBinaryName != null) && (targetBinaryName.length() != 0)
                        && (getReferenceCount(targetBinaryNames, targetBinaryName) == 1)) {
                    String targetBinaryFileName = mRoot + File.separator + targetBinaryName
                            + FILE_SUFFIX_APK;
                    deleteFile(targetBinaryFileName);
                }

                ArrayList<String> hostControllers = getHostControllers();
                String hostControllerPath = pkg.getJarPath();
                if ((hostControllerPath != null) && (hostControllerPath.length() != 0)
                        && (getReferenceCount(hostControllers, hostControllerPath) == 1)) {
                    String jarFilePath = mRoot + File.separator
                            + hostControllerPath + FILE_SUFFIX_JAR;
                    deleteFile(jarFilePath);
                }
            }

            String packageBinaryName = pkg.getAppBinaryName();
            mTestPackageMap.remove(pkg.getAppPackageName());

            String apkPath = mRoot + File.separator + packageBinaryName + FILE_SUFFIX_APK;
            String xmlPath = mRoot + File.separator + packageBinaryName + FILE_SUFFIX_XML;
            deleteFile(apkPath);
            deleteFile(xmlPath);
        }

        /**
         * Get the reference count of the specific value against the value list.
         *
         * @param list The value list to be checked against.
         * @param value The value to be checked.
         * @return The reference count.
         */
        private int getReferenceCount(ArrayList<String> list, String value) {
            if ((list == null) || (list.size() == 0) || (value == null)) {
                return 0;
            }

            int count = 0;
            for (String str : list) {
                if (value.equals(str)) {
                    count ++;
                }
            }

            return count;
        }

        /**
         * Get the target binary names contained with the test package description XML files.
         *
         * @return The target binary names.
         */
        private ArrayList<String> getTargetBinaryNames() {
            ArrayList<String> targetBinaryNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                targetBinaryNames.add(pkg.getTargetBinaryName());
            }
            return targetBinaryNames;
        }

        /**
         * Get the host controllers contained with the test package description XML files.
         *
         * @return The host controllers.
         */
        private ArrayList<String> getHostControllers() {
            ArrayList<String> hostControllers = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                hostControllers.add(pkg.getJarPath());
            }
            return hostControllers;
        }

        /**
         * Delete the specific file.
         *
         * @param filepath The file to be deleted.
         */
        private void deleteFile(String filepath) {
            File file = new File(filepath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }

        /**
         * Get package's APK file path via the package name.
         *
         * @param packageName The package name.
         * @return The package's APK file path.
         */
        public String getApkPath(String packageName) {
            return mRoot + File.separator + packageName + FILE_SUFFIX_APK;
        }

        /**
         * Get package's XML file path via the package name.
         * @param packageName The package name.
         * @return The package's XML file path.
         */
        public String getXmlPath(String packageName) {
            return mRoot + File.separator + packageName + FILE_SUFFIX_XML;
        }

        /**
         * List available package and suite.
         *
         * @param expectPackage expected package name
         * @return list which contains available packages, suites and cases.
         */
        @SuppressWarnings("unchecked")
        public List<ArrayList<String>> listAvailablePackage(String expectPackage) {
            ArrayList<String> packageList = new ArrayList<String>();
            ArrayList<String> suiteList = new ArrayList<String>();
            ArrayList<String> caseList = new ArrayList<String>();
            ArrayList<String> testList = new ArrayList<String>();

            for (TestPackage testPackage : mTestPackageMap.values()) {
                String appPackageName = testPackage.getAppPackageName();
                if (expectPackage.equals(appPackageName)) {
                    testPackage.getTestSuiteNames(appPackageName, suiteList, caseList);
                } else if (appPackageName.startsWith(expectPackage)) {
                    packageList.add(appPackageName);
                } else {
                    if (expectPackage.indexOf(Test.METHOD_SEPARATOR) == -1) {
                        testPackage.getTestCaseNames(expectPackage, caseList, testList);
                    } else {
                        testPackage.getTestNames(expectPackage, testList);
                    }
                }
            }

            return Arrays.asList(packageList, suiteList, caseList, testList);
        }
    }

    /**
     * Storing information of test plans.
     *
     */
    class PlanRepository extends Repository {

        PlanRepository(String root) {
            super(root);
        }

        /**
         * Get the path of the specified plan.
         *
         * @param name The plan name.
         * @return The plan path.
         */
        public String getPlanPath(String name) {
            if (mRoot == null) {
                Log.e("Repository uninitialized!", null);
                return null;
            }

            return mRoot + File.separator + name + FILE_SUFFIX_XML;
        }

        /**
         * Get all plan names in the plan repository.
         * @return Plan names.
         */
        public ArrayList<String> getAllPlanNames() {
            ArrayList<String> plans = new ArrayList<String>();

            if (mRoot == null) {
                Log.e("Not specify repository, please check your cts config",
                        null);
                return plans;
            }

            File planRepository = new File(mRoot);
            if (!planRepository.exists()) {
                Log.e("Plan Repository doesn't exist: " + mRoot, null);
                return null;
            }

            for (File f : planRepository.listFiles()) {
                String name = f.getName();

                if (name.endsWith(FILE_SUFFIX_XML)) {
                    plans.add(name.substring(0, name.length() - FILE_SUFFIX_XML.length()));
                }
            }

            return plans;
        }
    }
}
