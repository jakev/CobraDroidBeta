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

package dxc.junit.opcodes.l2d;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.l2d.jm.T_l2d_1;

public class Test_l2d extends DxTestCase {

    /**
     * @title  Argument = 50000000000
     */
    public void testN1() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(5.0E10d, t.run(50000000000l), 0d);
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(1d, t.run(1l), 0d);
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(-1d, t.run(-1l), 0d);
    }

    /**
     * @title  Argument = Long.MAX_VALUE
     */
    public void testB1() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(9.223372036854776E18d, t.run(Long.MAX_VALUE), 0d);
    }

    /**
     * @title  Argument = Long.MIN_VALUE
     */
    public void testB2() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0d);
    }

    /**
     * @title  Argument = 0
     */
    public void testB3() {
        T_l2d_1 t = new T_l2d_1();
        assertEquals(0d, t.run(0), 0d);
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.l2d.jm.T_l2d_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - float
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.l2d.jm.T_l2d_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - integer
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.l2d.jm.T_l2d_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - reference
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.l2d.jm.T_l2d_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
