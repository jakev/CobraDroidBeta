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

package dxc.junit.opcodes.i2b;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.i2b.jm.T_i2b_1;

public class Test_i2b extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(1, t.run(1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-1, t.run(-1));
    }

    /**
     * @title  Argument = 16
     */
    public void testN3() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(16, t.run(16));
    }

    /**
     * @title  Argument = -32
     */
    public void testN4() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-32, t.run(-32));
    }

    /**
     * @title  Argument = 134
     */
    public void testN5() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-122, t.run(134));
    }

    /**
     * @title  Argument = -134
     */
    public void testN6() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(122, t.run(-134));
    }

    /**
     * @title s. Argument = 127
     */
    public void testB1() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(127, t.run(127));
    }

    /**
     * @title s. Argument = 128
     */
    public void testB2() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-128, t.run(128));
    }

    /**
     * @title s. Argument = 0
     */
    public void testB3() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(0, t.run(0));
    }

    /**
     * @title s. Argument = -128
     */
    public void testB4() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-128, t.run(-128));
    }

    /**
     * @title s. Argument = -129
     */
    public void testB5() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(127, t.run(-129));
    }

    /**
     * @title s. Argument = Integer.MAX_VALUE
     */
    public void testB6() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title s. Argument = Integer.MIN_VALUE
     */
    public void testB7() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_2");
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
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_3");
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
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_4");
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
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
