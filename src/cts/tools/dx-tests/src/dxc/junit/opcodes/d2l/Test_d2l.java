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

package dxc.junit.opcodes.d2l;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.d2l.jm.T_d2l_1;

public class Test_d2l extends DxTestCase {

    /**
     * @title  Argument = 2.9999999
     */
    public void testN1() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(2l, t.run(2.9999999d));
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(1l, t.run(1d));
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(-1l, t.run(-1d));
    }

    /**
     * @title  Argument = Double.MAX_VALUE
     */
    public void testB1() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Double.MAX_VALUE));
    }

    /**
     * @title  Argument = Double.MIN_VALUE
     */
    public void testB2() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0, t.run(Double.MIN_VALUE));
    }

    /**
     * @title  Argument = -0
     */
    public void testB3() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0l, t.run(-0));
    }

    /**
     * @title  Argument = NaN
     */
    public void testB4() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(0l, t.run(Double.NaN));
    }

    /**
     * @title  Argument = POSITIVE_INFINITY
     */
    public void testB5() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Double.POSITIVE_INFINITY));
    }

    /**
     * @title  Argument = NEGATIVE_INFINITY
     */
    public void testB6() {
        T_d2l_1 t = new T_d2l_1();
        assertEquals(Long.MIN_VALUE, t.run(Double.NEGATIVE_INFINITY));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_2");
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
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - long
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_4");
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
            Class.forName("dxc.junit.opcodes.d2l.jm.T_d2l_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
