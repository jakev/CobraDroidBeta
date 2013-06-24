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

package dxc.junit.opcodes.fadd;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.fadd.jm.T_fadd_1;

public class Test_fadd extends DxTestCase {

    /**
     * @title  Arguments = 2.7f, 3.14f
     */
    public void testN1() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(5.84f, t.run(2.7f, 3.14f));
    }

    /**
     * @title  Arguments = 0, -3.14f
     */
    public void testN2() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(-3.14f, t.run(0, -3.14f));
    }

    /**
     * @title Arguments = -3.14f, -2.7f
     */
    public void testN3() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(-5.84f, t.run(-3.14f, -2.7f));
    }


    /**
     * @title  Arguments = Float.MAX_VALUE, Float.NaN
     */
    public void testB1() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(3.3028235E38f, 0.11E38f));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY,
     * Float.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_fadd_1 t = new T_fadd_1();
        assertTrue(Float.isNaN(t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY)));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY,
     * Float.POSITIVE_INFINITY
     */
    public void testB3() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY, -2.7f
     */
    public void testB4() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                -2.7f));
    }

    /**
     * @title  Arguments = +0, -0f
     */
    public void testB5() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(+0f, t.run(+0f, -0f));
    }

    /**
     * @title  Arguments = -0f, -0f
     */
    public void testB6() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(-0f, t.run(-0f, -0f));
    }

    /**
     * @title  Arguments = -2.7f, 2.7f
     */
    public void testB7() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(+0f, t.run(-2.7f, 2.7f));
    }

    /**
     * @title  Arguments = Float.MAX_VALUE, Float.MAX_VALUE
     */
    public void testB8() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.MAX_VALUE,
                Float.MAX_VALUE));
    }

    /**
     * @title  Arguments = Float.MIN_VALUE, -1.4E-45f
     */
    public void testB9() {
        T_fadd_1 t = new T_fadd_1();
        assertEquals(0f, t.run(Float.MIN_VALUE, -1.4E-45f));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fadd.jm.T_fadd_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float, double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fadd.jm.T_fadd_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long, float
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fadd.jm.T_fadd_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float, reference
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fadd.jm.T_fadd_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
