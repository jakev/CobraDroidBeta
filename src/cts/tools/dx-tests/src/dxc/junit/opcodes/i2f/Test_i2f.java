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

package dxc.junit.opcodes.i2f;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.i2f.jm.T_i2f_1;

public class Test_i2f extends DxTestCase {

    /**
     * @title  Argument = 123456
     */
    public void testN1() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(123456f, t.run(123456), 0f);
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(1f, t.run(1), 0f);
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(-1f, t.run(-1), 0f);
    }

    /**
     * @title  Argument = 33564439
     */
    public void testN4() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(3.356444E7f, t.run(33564439), 0f);
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(0f, t.run(0), 0f);
    }

    /**
     * @title  Argument = Argument = Integer.MAX_VALUE
     */
    public void testB2() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(2147483650f, t.run(Integer.MAX_VALUE), 0f);
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB3() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(-2147483650f, t.run(Integer.MIN_VALUE), 0f);
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_2");
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
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_3");
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
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_4");
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
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
