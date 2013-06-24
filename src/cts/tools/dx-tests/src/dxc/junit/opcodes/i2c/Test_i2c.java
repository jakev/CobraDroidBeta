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

package dxc.junit.opcodes.i2c;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.i2c.jm.T_i2c_1;

public class Test_i2c extends DxTestCase {

    /**
     * @title  Argument = 65
     */
    public void testN1() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('A', t.run(65));
    }

    /**
     * @title  Argument = 65537
     */
    public void testN2() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0001', t.run(65537));
    }

    /**
     * @title  Argument = -2
     */
    public void testN3() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\ufffe', t.run(-2));
    }

    /**
     * @title  Argument = 0x110000
     */
    public void testN4() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(0x110000));
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(0));
    }

    /**
     * @title  Argument = 65535
     */
    public void testB2() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\uffff', t.run(65535));
    }

    /**
     * @title  Argument = Integer.MAX_VALUE
     */
    public void testB3() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\uffff', t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB4() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(Integer.MIN_VALUE));
    }

    /**
     * verify test
     */
    /**
     * @constraint 4.8.2
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2
     * @title type of argument - double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2
     * @title type of argument - long
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2
     * @title type of argument - reference
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
