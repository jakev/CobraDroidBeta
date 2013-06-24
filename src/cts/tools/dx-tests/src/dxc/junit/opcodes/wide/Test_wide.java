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

package dxc.junit.opcodes.wide;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;


public class Test_wide extends DxTestCase {

    /**
     * Wide instruction is tested as part of wide version of particular
     * instructions so here we just test if wide instruction can't be applied to
     * wrong bytecode.
     */

    /**
     * @constraint 4.8.1.5 (?)
     * @title Wide instruction shall be applied only on defined
     * instructions 
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.wide.jm.T_wide_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.5 
     * @title bytecode modified with wide instruction must not be
     *          reachable directly
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.wide.jm.T_wide_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
