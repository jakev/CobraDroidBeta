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

import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.result.ITestInvocationListener;
import com.android.tradefed.testtype.IRemoteTest;
import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import org.easymock.EasyMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

/**
 * Unit tests for {@link PlanTest}.
 */
public class PlanTestTest extends TestCase {

    /** the test fixture under test, with all external dependencies mocked out */
    private PlanTest mPlanTest;
    private ITestCaseRepo mMockRepo;
    private IPlanXmlParser mMockPlanParser;
    private ITestDevice mMockDevice;
    private ITestInvocationListener mMockListener;

    private static final String PLAN_NAME = "CTS";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockRepo = EasyMock.createMock(ITestCaseRepo.class);
        mMockPlanParser = EasyMock.createMock(IPlanXmlParser.class);
        mMockDevice = EasyMock.createMock(ITestDevice.class);
        mMockListener = EasyMock.createNiceMock(ITestInvocationListener.class);

        mPlanTest = new PlanTest() {
            @Override
            ITestCaseRepo createTestCaseRepo() {
                return mMockRepo;
            }

            @Override
            IPlanXmlParser createXmlParser() {
                return mMockPlanParser;
            }

            @Override
            InputStream createXmlStream(File xmlFile) throws FileNotFoundException {
                // return empty stream, not used
                return new ByteArrayInputStream(new byte[0]);
            }
        };
        mPlanTest.setDevice(mMockDevice);
        // not used, but needs to be non-null
        mPlanTest.setTestCaseDir(new File("tmp"));
        mPlanTest.setTestPlanDir(new File("tmp"));
        mPlanTest.setPlanName(PLAN_NAME);
    }

    /**
     * Test normal case {@link PlanTest#run(java.util.List)}.
     * <p/>
     * Not that interesting of a test in its current form, but sets the stage for testing more
     * complicated scenarios.
     */
    @SuppressWarnings("unchecked")
    public void testRun() throws DeviceNotAvailableException, ParseException {
        // expect
        mMockPlanParser.parse((InputStream)EasyMock.anyObject());
        Collection<String> uris = new ArrayList<String>(1);
        uris.add("test-uri");
        EasyMock.expect(mMockPlanParser.getTestUris()).andReturn(uris);

        IRemoteTest mockTest = EasyMock.createMock(IRemoteTest.class);
        Collection<IRemoteTest> tests = new ArrayList<IRemoteTest>(1);
        tests.add(mockTest);
        EasyMock.expect(mMockRepo.getTests(uris)).andReturn(tests);

        // expect
        mockTest.run((List<ITestInvocationListener>)EasyMock.anyObject());

        replayMocks();
        EasyMock.replay(mockTest);
        mPlanTest.run(mMockListener);
        verifyMocks();
    }

    private void replayMocks() {
        EasyMock.replay(mMockRepo, mMockPlanParser, mMockDevice, mMockListener);
    }

    private void verifyMocks() {
        EasyMock.verify(mMockRepo, mMockPlanParser, mMockDevice, mMockListener);
    }
}
