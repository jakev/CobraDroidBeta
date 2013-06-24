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

package dxc.junit.opcodes.fneg;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.fneg.jm.T_fneg_1;

public class Test_fneg extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-1f, t.run(1f));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(1f, t.run(-1f));
    }

    /**
     * @title  Argument = +0
     */
    public void testN3() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-0f, t.run(+0f));
    }

    /**
     * @title  Argument = -2.7
     */
    public void testN4() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(2.7f, t.run(-2.7f));
    }

    /**
     * @title  Argument = Float.NaN
     */
    public void testB1() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.NaN, t.run(Float.NaN));
    }

    /**
     * @title  Argument = Float.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.NEGATIVE_INFINITY));
    }

    /**
     * @title  Argument = Float.POSITIVE_INFINITY
     */
    public void testB3() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(Float.POSITIVE_INFINITY));
    }

    /**
     * @title  Argument = Float.MAX_VALUE
     */
    public void testB4() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-3.4028235E38f, t.run(Float.MAX_VALUE));
    }

    /**
     * @title  Argument = Float.MIN
     */
    public void testB5() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-1.4E-45f, t.run(Float.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_2");
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
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_3");
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
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_4");
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
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
