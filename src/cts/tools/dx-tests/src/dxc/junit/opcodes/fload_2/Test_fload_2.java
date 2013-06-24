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

package dxc.junit.opcodes.fload_2;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.fload_2.jm.T_fload_2_1;
import dxc.junit.opcodes.fload_2.jm.T_fload_2_6;

public class Test_fload_2 extends DxTestCase {

    /**
     * @title value is pushed onto the operand stack
     */
    public void testN1() {
        assertEquals(2f, T_fload_2_1.run());
    }

    /**
     * @title equality of fload_<n> and fload <n>
     */
    public void testN2() {
        assertTrue(T_fload_2_6.run());
    }

    /**
     * @constraint 4.8.1.22
     * @title (index must be no greater than the value
     * of max_locals-1).
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fload_2.jm.T_fload_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fload_2.jm.T_fload_2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - long
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fload_2.jm.T_fload_2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.5
     * @title stack size
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fload_2.jm.T_fload_2_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
