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

package dxc.junit.opcodes.if_icmpne;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_1;

public class Test_if_icmpne extends DxTestCase {

    /**
     * @title  Arguments = 5, 6
     */
    public void testN1() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1, t.run(5, 6));
    }

    /**
     * @title  Arguments = 0x0f0e0d0c, 0x0f0e0d0c
     */
    public void testN2() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        /*
         * Compare with 1234 to check that in case of failed comparison
         * execution proceeds at the address following if_acmpeq instruction
         */
        assertEquals(1234, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }

    /**
     * @title  Arguments = 5, -5
     */
    public void testN3() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1, t.run(5, -5));
    }

    /**
     * @title  Arguments = 0x01001234, 0x1234
     */
    public void testN4() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1, t.run(0x01001234, 0x1234));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE, Integer.MAX_VALUE
     */
    public void testB1() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * @title  Arguments = Integer.MIN_VALUE, Integer.MIN_VALUE
     */
    public void testB2() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * @title  Arguments = 0, 1234567
     */
    public void testB3() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1, t.run(0, 1234567));
    }

    /**
     * @title  Arguments = 0, 0
     */
    public void testB4() {
        T_if_icmpne_1 t = new T_if_icmpne_1();
        assertEquals(1234, t.run(0, 0));
    }


    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int, double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long, int
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target shall be inside the
     * method
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target shall not be "inside" wide
     * instruction
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - reference, int
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmpne.jm.T_if_icmpne_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
