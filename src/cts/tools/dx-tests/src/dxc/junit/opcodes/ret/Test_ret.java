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

package dxc.junit.opcodes.ret;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.ret.jm.T_ret_1;
import dxc.junit.opcodes.ret.jm.T_ret_1_w;

public class Test_ret extends DxTestCase {

    /**
     * NORMAL RET VERSION
     */

    /**
     * @title normal test
     */
    public void testN1() {
        T_ret_1 t = new T_ret_1();
        assertTrue(t.run());
    }

    /**
     * @constraint 4.8.1.21
     * @title index operand
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title variable referenced by index shall
     * contain returnAddress
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.24
     * @title each returnAddress can be returned only
     * once
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.22
     * @title single ret instruction
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * WIDE RET VERSION
     */

    /**
     * @title 
     */
    public void testN2() {
        T_ret_1_w t = new T_ret_1_w();
        assertTrue(t.run());
    }

    /**
     * @constraint 4.8.1.21
     * @title index operand
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_2_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title variable referenced by index shall
     * contain returnAddress
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.24
     * @title each returnAddress can be returned only
     * once
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.22
     * @title single ret instruction
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.ret.jm.T_ret_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
