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

package dxc.junit.verify;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests {

    public static void run() {
        TestRunner.main(new String[] {AllTests.class.getName()});
    }

    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for java vm: test that "
                + "structurally damaged files are rejected by the verifier");
        suite.addTestSuite(dxc.junit.verify.t481_1.Test_t481_1.class);
        suite.addTestSuite(dxc.junit.verify.t481_2.Test_t481_2.class);
        suite.addTestSuite(dxc.junit.verify.t481_3.Test_t481_3.class);
        suite.addTestSuite(dxc.junit.verify.t481_4.Test_t481_4.class);
        suite.addTestSuite(dxc.junit.verify.t481_6.Test_t481_6.class);

        suite.addTestSuite(dxc.junit.verify.t482_2.Test_t482_2.class);
        suite.addTestSuite(dxc.junit.verify.t482_3.Test_t482_3.class);
        suite.addTestSuite(dxc.junit.verify.t482_4.Test_t482_4.class);
        suite.addTestSuite(dxc.junit.verify.t482_8.Test_t482_8.class);
        suite.addTestSuite(dxc.junit.verify.t482_9.Test_t482_9.class);
        suite.addTestSuite(dxc.junit.verify.t482_10.Test_t482_10.class);
        suite.addTestSuite(dxc.junit.verify.t482_11.Test_t482_11.class);
        suite.addTestSuite(dxc.junit.verify.t482_14.Test_t482_14.class);
        suite.addTestSuite(dxc.junit.verify.t482_20.Test_t482_20.class);

        return suite;
    }
}
