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

package dxc.junit.opcodes.i2s;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.i2s.jm.T_i2s_1;

public class Test_i2s extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(1, t.run(1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(-1, t.run(-1));
    }

    /**
     * @title  Argument = 32767
     */
    public void testN3() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(32767, t.run(32767));
    }

    /**
     * @title  Argument = -32768
     */
    public void testN4() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(-32768, t.run(-32768));
    }

    /**
     * @title  Argument = -32769
     */
    public void testN5() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(32767, t.run(-32769));
    }

    /**
     * @title  Argument = 32768
     */
    public void testN6() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(-32768, t.run(32768));
    }

    /**
     * @title  Argument = 0x10fedc;
     */
    public void testN7() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(0xfffffedc, t.run(0x10fedc));
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(0, t.run(0));
    }

    /**
     * @title  Argument = Integer.MAX_VALUE
     */
    public void testB2() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB3() {
        T_i2s_1 t = new T_i2s_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2s.jm.T_i2s_2");
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
            Class.forName("dxc.junit.opcodes.i2s.jm.T_i2s_3");
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
            Class.forName("dxc.junit.opcodes.i2s.jm.T_i2s_4");
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
            Class.forName("dxc.junit.opcodes.i2s.jm.T_i2s_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
