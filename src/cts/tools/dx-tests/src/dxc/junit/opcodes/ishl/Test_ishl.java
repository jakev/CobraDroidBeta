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

package dxc.junit.opcodes.ishl;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.ishl.jm.T_ishl_1;

public class Test_ishl extends DxTestCase {

    /**
     * @title  15 << 1
     */
    public void testN1() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(30, t.run(15, 1));
    }

    /**
     * @title  33 << 2
     */
    public void testN2() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(132, t.run(33, 2));
    }

    /**
     * @title  -15 << 1
     */
    public void testN3() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(-30, t.run(-15, 1));
    }

    /**
     * @title  Arguments = 1 & -1
     */
    public void testN4() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0x80000000, t.run(1, -1));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 32.
     */
    public void testN5() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(66, t.run(33, 33));
    }

    /**
     * FIXME: do we need to check that all the shift distances (0..31) works
     * fine?
     */

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE & 1
     */
    public void testB2() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0xfffffffe, t.run(Integer.MAX_VALUE, 1));
    }

    /**
     * @title  Arguments = Integer.MIN_VALUE & 1
     */
    public void testB3() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, 1));
    }

    /**
     * @title  Arguments = 1 & 0
     */
    public void testB4() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(1, t.run(1, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - double & int
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long & int
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - reference & int
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
