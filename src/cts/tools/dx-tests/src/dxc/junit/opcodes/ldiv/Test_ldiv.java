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

package dxc.junit.opcodes.ldiv;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.ldiv.jm.T_ldiv_1;

public class Test_ldiv extends DxTestCase {

    /**
     * @title Arguments = 100000000000l, 40000000000l
     */
    public void testN1() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(2l, t.run(100000000000l, 40000000000l));
    }

    /**
     * @title  Rounding
     */
    public void testN2() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(8l, t.run(98765432123456l, 12345678912345l));
    }

    /**
     * @title  Dividend = 0
     */
    public void testN3() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(0l, t.run(0l, 98765432123456l));
    }

    /**
     * @title  Dividend is negative
     */
    public void testN4() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(-8, t.run(-98765432123456l, 12345678912345l));
    }

    /**
     * @title  Divisor is negative
     */
    public void testN5() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(-8, t.run(98765432123456l, -12345678912345l));
    }

    /**
     * @title  Both Dividend and divisor are negative
     */
    public void testN6() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(80l, t.run(-98765432123456l, -1234567891234l));
    }

    /**
     * @title Arguments = Long.MIN_VALUE, -1
     */
    public void testB1() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, -1));
    }
    /**
     * @title Arguments = Long.MIN_VALUE, 1
     */
    public void testB2() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, 1));
    }
    /**
     * @title Arguments = Long.MAX_VALUE, 1
     */
    public void testB3() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(9223372036854775807L, t.run(Long.MAX_VALUE, 1));
    }
    /**
     * @title Arguments = Long.MIN_VALUE, Long.MAX_VALUE
     */
    public void testB4() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(-1, t.run(Long.MIN_VALUE, Long.MAX_VALUE));
    }
    /**
     * @title Arguments = 1, Long.MAX_VALUE
     */
    public void testB5() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(0, t.run(1, Long.MAX_VALUE));
    }
    /**
     * @title Arguments = 1, Long.MIN_VALUE
     */
    public void testB6() {
        T_ldiv_1 t = new T_ldiv_1();
        assertEquals(0, t.run(1, Long.MIN_VALUE));
    }

    /**
     * @title  Divisor is 0
     */
    public void testE1() {
        T_ldiv_1 t = new T_ldiv_1();
        try {
            t.run(12345678912345l, 0);
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long / double
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int / long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float / long
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - reference / long
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.ldiv.jm.T_ldiv_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
