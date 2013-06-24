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

package dxc.junit.opcodes.l2f;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.l2f.jm.T_l2f_1;

public class Test_l2f extends DxTestCase {

    /**
     * @title  Argument = 123456789012345
     */
    public void testN1() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(1.23456788E14f, t.run(123456789012345l), 0f);
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(1f, t.run(1l), 0f);
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(-1f, t.run(-1l), 0f);
    }

    /**
     * @title  Argument = Long.MAX_VALUE
     */
    public void testB1() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(9.223372036854776E18, t.run(Long.MAX_VALUE), 0f);
    }

    /**
     * @title  Argument = Long.MIN_VALUE
     */
    public void testB2() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0f);
    }

    /**
     * @title  Argument = 0
     */
    public void testB3() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(0f, t.run(0l), 0f);
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_3");
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
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_4");
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
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
