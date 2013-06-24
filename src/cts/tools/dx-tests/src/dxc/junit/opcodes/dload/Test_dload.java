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

package dxc.junit.opcodes.dload;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.dload.jm.T_dload_1;
import dxc.junit.opcodes.dload.jm.T_dload_1_w;
import dxc.junit.opcodes.dload.jm.T_dload_2;
import dxc.junit.opcodes.dload.jm.T_dload_2_w;

public class Test_dload extends DxTestCase {

    /*
     * NORMAL dload VERSION
     */

    /**
     * @title  Test dload 1
     */
    public void testN1() {
        T_dload_1 t = new T_dload_1();
        assertEquals(1d, t.run());
    }

    /**
     * @title  Test dload 255
     */
    public void testN2() {
        T_dload_2 t = new T_dload_2();
        assertEquals(1d, t.run());
    }

    /**
     * @constraint 4.8.1.21
     * @title index must be no greater than the value
     * of max_locals-1
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - float
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_4");
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
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_5");
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
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /*
     * WIDE dload VERSION
     */

    /**
     * @title  Test dload 257
     */
    public void testN3() {
        T_dload_1_w t = new T_dload_1_w();
        assertEquals(1d, t.run());
    }

    /**
     * @title  Test dload_w 1
     */
    public void testN4() {
        T_dload_2_w t = new T_dload_2_w();
        assertEquals(1d, t.run());
    }

    /**
     * @constraint 4.8.1.25
     * @title index must be no greater than the value
     * of max_locals-1
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - double
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - long
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.5
     * @title stack size
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
