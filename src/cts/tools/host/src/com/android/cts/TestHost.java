/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      httprunPackage://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.cts;

import com.android.cts.HostConfig.CaseRepository;
import com.android.cts.HostConfig.PlanRepository;
import com.android.ddmlib.AndroidDebugBridge;

import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * Act as the host for the device connections, also provides management of
 * sessions.
 */
public class TestHost extends XMLResourceHandler implements SessionObserver {
    public static final String TEMP_PLAN_NAME = "tempPlan";

    enum ActionType {
        RUN_SINGLE_TEST, RUN_SINGLE_JAVA_PACKAGE, START_NEW_SESSION, RESUME_SESSION
    }
    /**
     * Definition of the modes the TestHost will run with.
     * <ul>
     *    <li> RUN: For this mode, the TestHost will run the plan or
     *              package directly without starting the UI.
     *    <li> CONSOLE: For this mode, the TestHost will start the UI
     *                  and wait for input from user.
     * </ul>
     */
    enum MODE {
        UNINITIALIZED, RUN, CONSOLE
    }

    static private ArrayList<TestSession> sSessions = new ArrayList<TestSession>();
    static private DeviceManager sDeviceManager = new DeviceManager();
    static private Object sTestSessionSync = new Object();

    static private ConsoleUi sConsoleUi;

    static private HostConfig sConfig;

    private static TestHost sInstance;
    static MODE sMode = MODE.UNINITIALIZED;

    public static void main(final String[] mainArgs) {
        CUIOutputStream.println("Android CTS version " + Version.asString());

        if (HostLock.lock() == false) {
            Log.e("Error: CTS is being used at the moment."
                    + " No more than one CTS instance is allowed simultaneously", null);
            exit();
        }

        sDeviceManager.initAdb();

        sConsoleUi = new ConsoleUi(getInstance());
        CommandParser cp = init(sConsoleUi, mainArgs);

        if (sMode == MODE.RUN) {
            try {
                /* After booting up, the connection between
                 * CTS host and device isn't ready. It's needed
                 * to wait for 3 seconds for device ready to
                 * start the the mode of no console UI.
                 */
                Thread.sleep(3000);
                cp.removeKey(CTSCommand.OPTION_CFG);
                sConsoleUi.processCommand(cp);
            } catch (InterruptedException e) {
                Log.e("Met InterruptedException", e);
            } catch (Exception e) {
                Log.e("Met exception when processing command", e);
            }
        } else if (sMode == MODE.CONSOLE) {
            sConsoleUi.startUi();
        }

        exit();
    }

    /**
     * Release host lock and then exit.
     */
    private static void exit() {
        Log.closeLog();
        HostLock.release();
        System.exit(-1);
    }

    /**
     * Extract mode from the options used to activating CTS.
     *
     * @param cp Command container.
     * @return The mode.
     */
    static private MODE getMode(final CommandParser cp) {
        String action = cp.getAction();
        if ((action != null) && (action.equals(CTSCommand.START))) {
            return MODE.RUN;
        } else {
            return MODE.CONSOLE;
        }
    }

    /**
     * Start zipped package.
     *
     * @param pathName  The path name of the zipped package.
     */
    public void startZippedPackage(final String pathName)
                throws FileNotFoundException,
                       IOException,
                       ParserConfigurationException,
                       TransformerFactoryConfigurationError,
                       TransformerException,
                       DeviceNotAvailableException,
                       TestNotFoundException,
                       SAXException,
                       TestPlanNotFoundException,
                       IllegalTestNameException,
                       InterruptedException, DeviceDisconnectedException,
                       NoSuchAlgorithmException, InvalidNameSpaceException,
                       InvalidApkPathException {

        // step 1: add package
        if (!addPackage(pathName)) {
            return;
        }

        // step 2: create plan
        ArrayList<String> packages = new ArrayList<String>();
        String pkgName = pathName.substring(pathName
                .lastIndexOf(File.separator) + 1, pathName.lastIndexOf("."));
        packages.add(pkgName);
        HashMap<String, ArrayList<String>> selectedResult =
                       new HashMap<String, ArrayList<String>>();
        selectedResult.put(pkgName, null);
        TestSessionBuilder.getInstance().serialize(TEMP_PLAN_NAME, packages, selectedResult);

        // step 3: start the plan
        TestSession ts = startSession(TEMP_PLAN_NAME, getFirstAvailableDevice().getSerialNumber(),
                null);

        // step 4: copy the resulting zip file
        String resultName = pathName.substring(0, pathName.lastIndexOf("."))
                + ".zip";
        TestSessionLog log = ts.getSessionLog();
        copyFile(log.getResultPath() + ".zip", resultName);

        // step 5: clear the temporary working environment
        removePlans(TEMP_PLAN_NAME);
        //give the system some time to avoid asserting
        Thread.sleep(1000);

        removePackages(pkgName);
        //give the system some time to avoid asserting
        Thread.sleep(1000);
    }

    /**
     * Copy the source file to the destination file.
     *
     * @param srcFileName The name of the source file.
     * @param dstFileName The name of the destination file.
     */
    private void copyFile(final String srcFileName, final String dstFileName) throws IOException {
        FileReader input = new FileReader(new File(srcFileName));
        BufferedWriter output = new BufferedWriter(new FileWriter(dstFileName));

        int c;
        while ((c = input.read()) != -1) {
            output.write(c);
        }

        input.close();
        output.flush();
        output.close();
    }

    /**
     * Add a package by the path and package name.
     *
     * @param pathName The path name.
     * @return If succeed in adding package, return true; else, return false.
     */
    public boolean addPackage(final String pathName) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {

        CaseRepository caseRepo = sConfig.getCaseRepository();
        if (!HostUtils.isFileExist(pathName)) {
            Log.e("Package error: package file " + pathName + " doesn't exist.", null);
            return false;
        }

        if (!caseRepo.isValidPackageName(pathName)) {
            return false;
        }

        caseRepo.addPackage(pathName);
        return true;
    }

    /**
     * Remove plans from the plan repository according to the specific plan name.
     *
     * @param name The plan name.
     */
    public void removePlans(final String name) {
        if ((name == null) || (name.length() == 0)) {
            CUIOutputStream.println("Please add plan name or all as parameter.");
            return;
        }

        PlanRepository planRepo = sConfig.getPlanRepository();
        if (name.equals(HostConfig.ALL)) {
            ArrayList<String> plans = planRepo.getAllPlanNames();
            for (String plan : plans) {
                removePlan(plan, planRepo);
            }
        } else {
            if (!planRepo.getAllPlanNames().contains(name)) {
                Log.e("No plan named " + name + " in repository!", null);
                return;
            }
            removePlan(name, planRepo);
        }
    }

    /**
     * Remove a specified plan from the plan repository.
     *
     * @param planName The plan name.
     * @param planRepo The plan repository.
     */
    private void removePlan(final String planName, final PlanRepository planRepo) {
        File planFile = new File(planRepo.getPlanPath(planName));
        if (!planFile.isFile() || !planFile.exists()) {
            Log.e("Can't locate the file of the plan, please check your repository!", null);
            return;
        }

        if (!planFile.canWrite()) {
            Log.e("Can't delete this plan, permission denied!", null);
            return;
        }

        if (!planFile.delete()) {
            Log.e(planName + " plan file delete failed", null);
        }
    }

    /**
     * Remove packages from the case repository..
     *
     * @param packageName The java package name to be removed from the case repository.
     */
    public void removePackages(final String packageName)
            throws IndexOutOfBoundsException {
        CaseRepository caseRepo = sConfig.getCaseRepository();

        if ((packageName == null) || (packageName.length() == 0)) {
            CUIOutputStream.println("Please add package name or all as parameter.");
            return;
        }

        caseRepo.removePackages(packageName);
    }

    /**
     * Initialize TestHost with the arguments passed in.
     *
     * @param mainArgs The arguments.
     * @return CommandParser which contains the command and options.
     */
    static CommandParser init(final ConsoleUi cui, final String[] mainArgs) {
        CommandParser cp = null;
        String cfgPath= null;

        if (mainArgs.length == 0) {
            sMode = MODE.CONSOLE;
            cfgPath = System.getProperty("HOST_CONFIG");
            if ((cfgPath == null) || (cfgPath.length() == 0)) {
                Log.e("Please make sure environment variable CTS_HOST_CFG is "
                       + "set as {cts install path}[/host_config.xml].", null);
                exit();
            }
        } else if (mainArgs.length == 1) {
            sMode = MODE.CONSOLE;
            cfgPath = mainArgs[0];
        } else {
            String cmdLine = "";
            for (int i = 0; i < mainArgs.length; i ++) {
                cmdLine += mainArgs[i] + " ";
            }

            try {
                cp = CommandParser.parse(cmdLine);
                if (!cui.validateCommandParams(cp)) {
                    Log.e("Please type in arguments correctly to activate CTS.", null);
                    exit();
                }
            } catch (UnknownCommandException e1) {
                Log.e("Please type in arguments correctly to activate CTS.", null);
                exit();
            } catch (CommandNotFoundException e1) {
                Log.e("Please type in arguments correctly to activate CTS.", null);
                exit();
            }

            sMode = getMode(cp);
            if (sMode == MODE.RUN) {
                if (cp.containsKey(CTSCommand.OPTION_CFG)) {
                    cfgPath = cp.getValue(CTSCommand.OPTION_CFG);
                } else {
                    cfgPath = System.getProperty("HOST_CONFIG");
                    if ((cfgPath == null) || (cfgPath.length() == 0)) {
                        Log.e("Please make sure environment variable CTS_HOST_CFG "
                               + "is set as {cts install path}[/host_config.xml].", null);
                        exit();
                    }
                }
            }
        }

        if ((cfgPath == null) || (cfgPath.length() == 0)) {
            Log.e("Please type in arguments correctly to activate CTS.", null);
            exit();
        }

        String filePath = getConfigFilePath(cfgPath);
        try {
            if (loadConfig(filePath) == false) {
                exit();
            }

            Log.initLog(sConfig.getLogRoot());
            sConfig.loadRepositories();
        } catch (Exception e) {
            Log.e("Error while parsing cts config file", e);
            exit();
        }
        return cp;
    }

    /**
     * Singleton generator.
     *
     * @return The TestHost.
     */
    public static TestHost getInstance() {
        if (sInstance == null) {
            sInstance = new TestHost();
        }

        return sInstance;
    }

    /**
     * Get configuration file from the arguments given.
     *
     * @param filePath The file path.
     * @return The the path of the configuration file.
     */
    static private String getConfigFilePath(final String filePath) {
        if (filePath != null) {
            if (!HostUtils.isFileExist(filePath)) {
                Log.e("Configuration file \"" + filePath + "\" doesn't exist.", null);
                exit();
            }
        } else {
            Log.e("Configuration file doesn't exist.", null);
            exit();
        }

        return filePath;
    }

    /**
     * Load configuration from the given file.
     *
     * @param configPath The configuration path.
     * @return If succeed, return true; else, return false.
     */
    static boolean loadConfig(final String configPath) throws SAXException,
            IOException, ParserConfigurationException {
        sConfig = HostConfig.getInstance();

        return sConfig.load(configPath);
    }

    /**
     * Get case repository.
     *
     * @return The case repository.
     */
    public HostConfig.CaseRepository getCaseRepository() {
        return sConfig.getCaseRepository();
    }

    /**
     * Get plan repository.
     *
     * @return The plan repository.
     */
    public HostConfig.PlanRepository getPlanRepository() {
        return sConfig.getPlanRepository();
    }

    /**
     * Run the specified {@link TestSession} on the specified {@link TestDevice}(s)
     *
     * @param ts the specified {@link TestSession}
     * @param deviceId the ID of the specified {@link TestDevice}
     * @param testFullName The full name of the test to be run.
     * @param javaPkgName The specific java package name to be run.
     * @param type The action type to activate the test session.
     */
    static private void runTest(final TestSession ts, final String deviceId,
            final String testFullName, final String javaPkgName, ActionType type)
            throws DeviceNotAvailableException, TestNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, InvalidNameSpaceException,
            InvalidApkPathException {

        if (ts == null) {
            return;
        }

        ts.setObserver(getInstance());
        TestDevice device = sDeviceManager.allocateFreeDeviceById(deviceId);
        TestSessionLog sessionLog = ts.getSessionLog();
        ts.setTestDevice(device);
        ts.getDevice().installDeviceSetupApp();
        sessionLog.setDeviceInfo(ts.getDevice().getDeviceInfo());

        boolean finish = false;
        while (!finish) {
            ts.getDevice().disableKeyguard();
            try {
                switch (type) {
                case RUN_SINGLE_TEST:
                    ts.start(testFullName);
                    break;

                case RUN_SINGLE_JAVA_PACKAGE:
                    ts.start(javaPkgName);
                    break;

                case START_NEW_SESSION:
                    ts.start();
                    break;

                case RESUME_SESSION:
                    ts.resume();
                    break;
                }

                finish = true;
            } catch (ADBServerNeedRestartException e) {
                Log.d(e.getMessage());
                Log.i("Max ADB operations reached. Restarting ADB...");

                TestSession.setADBServerRestartedMode();
                sDeviceManager.restartADBServer(ts);

                type = ActionType.RESUME_SESSION;
            }
        }

        TestSession.resetADBServerRestartedMode();
        if (HostConfig.getMaxTestCount() > 0) {
            sDeviceManager.resetTestDevice(ts.getDevice());
        }

        ts.getDevice().uninstallDeviceSetupApp();
    }

    /**
     * Create {@link TestSession} according to the specified test plan.
     *
     * @param testPlanName the name of the specified test plan
     * @return a {@link TestSession}
     */
    static public TestSession createSession(final String testPlanName)
            throws IOException, TestNotFoundException, SAXException,
            ParserConfigurationException, TestPlanNotFoundException, NoSuchAlgorithmException {

        String testPlanPath = sConfig.getPlanRepository().getPlanPath(testPlanName);
        TestSession ts = TestSessionBuilder.getInstance().build(testPlanPath);
        sSessions.add(ts);

        return ts;
    }

    /** {@inheritDoc} */
    public void notifyFinished(final TestSession ts) {
        // As test run on a session, so just keep session info in debug level
        Log.d("Session " + ts.getId() + " finished.");

        synchronized (sTestSessionSync) {
            sTestSessionSync.notify();
        }
        ts.getSessionLog().sessionComplete();
    }

    /**
     * Tear down ADB connection.
     */
    public void tearDown() {
        AndroidDebugBridge.disconnectBridge();
        AndroidDebugBridge.terminate();
    }

    /**
     * Get the sessions connected with devices.
     *
     * @return The sessions.
     */
    public Collection<TestSession> getSessions() {
        return sSessions;
    }

    /**
     * Get session by session ID.
     *
     * @param sessionId The session ID.
     * @return The session.
     */
    public TestSession getSession(final int sessionId) {
        for (TestSession session : sSessions) {
            if (session.getId() == sessionId) {
                return session;
            }
        }
        return null;
    }

    /**
     * Get session by test plan name.
     *
     * @param testPlanName Test plan name.
     * @return The session corresponding to the test plan name.
     */
    public ArrayList<TestSession> getSessionList(final String testPlanName) {
        ArrayList<TestSession> list = new ArrayList<TestSession>();
        for (TestSession session : sSessions) {
            if (testPlanName.equals(session.getSessionLog().getTestPlanName())) {
                list.add(session);
            }
        }
        return list;
    }

    /**
     * List the ID, name and status of all {@link TestDevice} which connected to
     * the {@link TestHost}.
     *
     * @return a string list of {@link TestDevice}'s id, name and status.
     */
    public String[] listDevices() {
        ArrayList<String> deviceList = new ArrayList<String>();
        TestDevice[] devices = sDeviceManager.getDeviceList();

        for (TestDevice device : devices) {
            deviceList.add(device.getSerialNumber() + "\t" + device.getStatusAsString());
        }
        return deviceList.toArray(new String[deviceList.size()]);
    }

    /**
     * Get device list connected with the host.
     *
     * @return The device list connected with the host.
     */
    public TestDevice[] getDeviceList() {
        return sDeviceManager.getDeviceList();
    }

    /**
     * Get the first available device.
     *
     * @return the first available device or null if none are available.
     */
    public TestDevice getFirstAvailableDevice() {
        for (TestDevice td : sDeviceManager.getDeviceList()) {
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                return td;
            }
        }
        return null;
    }

    /**
     * Get session logs.
     *
     * @return Session logs.
     */
    public Collection<TestSessionLog> getSessionLogs() {
        ArrayList<TestSessionLog> sessionLogs = new ArrayList<TestSessionLog>();
        for (TestSession session : sSessions) {
            sessionLogs.add(session.getSessionLog());
        }
        return sessionLogs;
    }
    /**
     * Start a test session.
     *
     * @param testPlanName TestPlan config file name
     * @param deviceId Target device ID
     * @param profile The profile of the device being tested.
     * @param javaPkgName The specific java package name to be run.
     */
    public TestSession startSession(final String testPlanName,
            String deviceId, final String javaPkgName)
            throws IOException, DeviceNotAvailableException,
            TestNotFoundException, SAXException, ParserConfigurationException,
            TestPlanNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, NoSuchAlgorithmException,
            InvalidNameSpaceException, InvalidApkPathException {

        TestSession ts = createSession(testPlanName);
        if ((javaPkgName != null) && (javaPkgName.length() != 0)) {
            runTest(ts, deviceId, null, javaPkgName, ActionType.RUN_SINGLE_JAVA_PACKAGE);
        } else {
            runTest(ts, deviceId, null, javaPkgName, ActionType.START_NEW_SESSION);
        }

        ts.getSessionLog().sessionComplete();
        return ts;
    }

    /**
     * Start a test session.
     *
     * @param ts The test session.
     * @param deviceId Target device ID.
     * @param testFullName Specific test full name.
     * @param javaPkgName The specific java package name to be run.
     * @param type The action type to activate the test session.
     */
    public TestSession startSession(final TestSession ts, String deviceId,
            final String testFullName, final String javaPkgName, ActionType type)
            throws DeviceNotAvailableException,
            TestNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, InvalidNameSpaceException,
            InvalidApkPathException {

        runTest(ts, deviceId, testFullName, javaPkgName, type);
        ts.getSessionLog().sessionComplete();
        return ts;
    }

    /**
     * Get plan name from what is typed in by the user.
     *
     * @param rawPlanName The raw plan name.
     * @return The plan name.
     */
    public String getPlanName(final String rawPlanName) {
        if (rawPlanName.indexOf("\\") != -1) {
            return rawPlanName.replaceAll("\\\\", "");
        }
        if (rawPlanName.indexOf("\"") != -1) {
            return rawPlanName.replaceAll("\"", "");
        }
        return rawPlanName;
    }

    /**
     * Add test session.
     *
     * @param ts The test session.
     */
    public void addSession(TestSession ts) {
        sSessions.add(ts);
    }
}
