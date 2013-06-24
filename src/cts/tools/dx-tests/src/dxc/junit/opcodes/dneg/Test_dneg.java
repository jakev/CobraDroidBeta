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

package dxc.junit.opcodes.dneg;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.dneg.jm.T_dneg_1;

public class Test_dneg extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-1d, t.run(1d));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(1d, t.run(-1d));
    }

    /**
     * @title  Argument = +0
     */
    public void testN3() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-0d, t.run(+0d));
    }

    /**
     * @title  Argument = -2.7
     */
    public void testN4() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(2.7d, t.run(-2.7d));
    }


    /**
     * @title  Argument = Double.NaN
     */
    public void testB1() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.NaN, t.run(Double.NaN));
    }

    /**
     * @title  Argument = Double.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.NEGATIVE_INFINITY));
    }

    /**
     * @title  Argument = Double.POSITIVE_INFINITY
     */
    public void testB3() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY));
    }

    /**
     * @title  Argument = Double.MAX_VALUE
     */
    public void testB4() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-1.7976931348623157E308d, t.run(Double.MAX_VALUE));
    }

    /**
     * @title  Argument = Double.MIN
     */
    public void testB5() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-4.9E-324d, t.run(Double.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_2");
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
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_3");
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
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_4");
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
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
