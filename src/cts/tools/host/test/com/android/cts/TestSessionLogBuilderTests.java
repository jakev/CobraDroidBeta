/*
 * Copyright (C) 2009 The Android Open Source Project
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

import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Test the logic of TestSessionLogBuilder.
 *
 */
public class TestSessionLogBuilderTests extends CtsTestBase {

    public void testLoadTestSessionLogBuilder() throws IOException, NoSuchAlgorithmException,
            SAXException, TestPlanNotFoundException, TestNotFoundException,
            ParserConfigurationException {

        final String resultFile =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<?xml-stylesheet type=\"text/xsl\"  href=\"cts_result.xsl\"?>\n" +
            "\n" +
            "<TestResult endtime=\"Wed Apr 29 10:36:47 CST 2009\" " +
            "starttime=\"Wed Apr 29 10:36:30 CST 2009\" testPlan=\"location\" version=\"1.0\" profile=\"ALL\">\n" +
            "  <DeviceInfo>\n" +
            "    <Screen resolution=\"480x320\"/>\n" +
            "    <PhoneSubInfo subscriberId=\"15555218135\"/>\n" +
            "    <BuildInfo Xdpi=\"164.75456\" Ydpi=\"165.87724\" androidPlatformVersion=\"3\" " +
            "    buildID=\"CUPCAKE\" buildName=\"generic\" buildVersion=\"1.5\" " +
            "    build_board=\"unknown\" build_brand=\"generic\" build_device=\"generic\" " +
            "    build_fingerprint=\"test-keys\" build_model=\"generic\" build_type=\"eng\" " +
            "    deviceID=\"emulator-5554\" imei=\"000000000000000\" imsi=\"310260000000000\" " +
            "    keypad=\"qwty\" locales=\"en_US;\" navigation=\"trkball\" network=\"Android\" " +
            "    touch=\"finger\"/>\n" +
            "  </DeviceInfo>\n" +
            "  <Summary failed=\"22\" notExecuted=\"0\" pass=\"22\" timeout=\"0\"/>\n" +
            "  <TestPackage digest=\"7GDPKCxBGKuVyEkH1PGWJc=&#10;\" name=\"android.location\" " +
            "  appPackageName=\"android.location\">\n" +
            "    <TestSuite name=\"android\">\n" +
            "      <TestSuite name=\"location\">\n" +
            "        <TestSuite name=\"cts\">\n" +
            "          <TestCase name=\"LocationManagerTest\" priority=\"\">\n" +
            "           <Test endtime=\"Wed Apr 29 10:36:44 CST 2009\" name=\"testOne\"" +
            "            result=\"fail\" starttime=\"Thu Jan 01 07:00:00 CST 1970\">\n" +
            "             <FailedScene message=\"java.SecurityException: (Parcel.java:1234)\">\n" +
            "               <StackTrace>at android.os.Parcel.readException(Parcel.java:1234)\n" +
            "                  at android.os.Parcel.readException(Parcel.java:1222)\n" +
            "                  at android.location.addTestProvider(ILocationManager.java:821)\n" +
            "                  at android.location.addTestProvider(LocationManager.java:987)\n" +
            "               </StackTrace>\n" +
            "             </FailedScene>\n" +
            "           </Test>\n" +
            "           <Test endtime=\"Wed Apr 29 10:36:44 CST 2009\" name=\"testTwo\"" +
            "             result=\"fail\" starttime=\"Thu Jan 01 07:00:00 CST 1970\">\n" +
            "             <FailedScene message=\"java.SecurityException: (Parcel.java:1234)\">\n" +
            "               <StackTrace>at android.os.Parcel.readException(Parcel.java:1234)\n" +
            "                  at android.os.Parcel.readException(Parcel.java:1222)\n" +
            "                  at android.location.(ILocationManager.java:821)\n" +
            "               </StackTrace>\n" +
            "             </FailedScene>\n" +
            "           </Test>\n" +
            "          </TestCase>\n" +
            "          <TestCase name=\"AddressTest\" priority=\"\">\n" +
            "           <Test endtime=\"Wed Apr 29 10:36:43 CST 2009\" name=\"testThree\" " +
            "            result=\"pass\" starttime=\"Thu Jan 01 07:00:00 CST 1970\"/>\n" +
            "           <Test endtime=\"Wed Apr 29 10:36:43 CST 2009\" name=\"testFour\" " +
            "            result=\"pass\" starttime=\"Thu Jan 01 07:00:00 CST 1970\"/>\n" +
            "          </TestCase>\n" +
            "        </TestSuite>\n" +
            "      </TestSuite>\n" +
            "    </TestSuite>\n" +
            "  </TestPackage>\n" +
            "</TestResult>";

        final String pkgDescription =
            "<TestPackage name=\"android.location\" " +
            "appPackageName=\"android.location\" targetNameSpace=\"targetNameSpace\" " +
            " version=\"1.0\" AndroidFramework=\"Android 1.0\"" +
            " runner=\"runner\">\n" +
            "    <Description>something extracted from java doc</Description>\n" +
            "    <TestSuite name=\"android\">\n" +
            "      <TestSuite name=\"location\">\n" +
            "        <TestSuite name=\"cts\">\n" +
            "          <TestCase name=\"LocationManagerTest\" priority=\"\">\n" +
            "           <Test name=\"testOne\" />\n" +
            "           <Test name=\"testTwo\" />\n" +
            "          </TestCase>\n" +
            "          <TestCase name=\"AddressTest\" priority=\"\">\n" +
            "           <Test name=\"testThree\" />\n" +
            "           <Test name=\"testFour\" />\n" +
            "          </TestCase>\n" +
            "        </TestSuite>\n" +
            "      </TestSuite>\n" +
            "    </TestSuite>\n" +
            "</TestPackage>\n";

        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n" +
            "\t<Description>Demo test plan</Description>\n" +
            "\t\t<PlanSettings>\n" +
            "\t\t\t<RequiredDevice amount=\"" + 1 + "\"" + "/>\n" +
            "\t\t</PlanSettings>\n" +
            "\t<Entry uri=\"android.location\"/>\n" +
            "</TestPlan>";

        final String resultFileName = "testResult.xml";
        final String descriptionFileName = "CtsLocation.xml";

        HostConfig.getInstance().removeTestPacakges();

        String planPath =
            HostConfig.getInstance().getPlanRepository().getPlanPath("location");
        createFile(testPlanConfigStr, planPath);

        String resultPath =
            HostConfig.getInstance().getResultRepository().getRoot() + resultFileName;
        createFile(resultFile, resultPath);

        createTestPackage(pkgDescription, "android.location");
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(
                TestSessionLogBuilder.getInstance().build(resultPath));
        assertNotNull(ts);
        TestSessionLog log = ts.getSessionLog();
        assertNotNull(log);
        assertEquals("location", log.getTestPlanName());

        Collection<TestPackage> packages = log.getTestPackages();
        assertEquals(1, packages.size());
        TestPackage pkg = packages.iterator().next();

        Collection<Test> tests = pkg.getTests();
        assertNotNull(tests);
        assertEquals(4, tests.size());
        Iterator<Test> iterator = tests.iterator();
        Test test1 = iterator.next();
        assertEquals("android.location.cts.LocationManagerTest#testOne", test1.getFullName());

        CtsTestResult result = test1.getResult();
        assertNotNull(result);
        assertEquals("fail", result.getResultString());
        assertNotNull(result.getFailedMessage());
        assertNotNull(result.getStackTrace());

        Test test2 = iterator.next();
        Test test3 = iterator.next();
        assertEquals("android.location.cts.AddressTest#testThree", test3.getFullName());

        result = test3.getResult();
        assertNotNull(result);
        assertEquals("pass", result.getResultString());
        assertNull(result.getFailedMessage());
        assertNull(result.getStackTrace());
    }
}
