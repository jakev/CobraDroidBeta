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

package dxc.junit.opcodes.iinc;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.iinc.jm.T_iinc_1;
import dxc.junit.opcodes.iinc.jm.T_iinc_1_w;
import dxc.junit.opcodes.iinc.jm.T_iinc_2;
import dxc.junit.opcodes.iinc.jm.T_iinc_2_w;
import dxc.junit.opcodes.iinc.jm.T_iinc_3;
import dxc.junit.opcodes.iinc.jm.T_iinc_3_w;
import dxc.junit.opcodes.iinc.jm.T_iinc_4;
import dxc.junit.opcodes.iinc.jm.T_iinc_4_w;
import dxc.junit.opcodes.iinc.jm.T_iinc_5;
import dxc.junit.opcodes.iinc.jm.T_iinc_5_w;
import dxc.junit.opcodes.iinc.jm.T_iinc_6;
import dxc.junit.opcodes.iinc.jm.T_iinc_6_w;

public class Test_iinc extends DxTestCase {

    /*
     * NORMAL IINC VERSION
     */

    /**
     * @title  Increment by 1
     */
    public void testN1() {
        T_iinc_1 t = new T_iinc_1();
        assertEquals(5, t.run(4));
    }

    /**
     * @title  Increment by -1
     */
    public void testN2() {
        T_iinc_2 t = new T_iinc_2();
        assertEquals(3, t.run(4));
    }

    /**
     * @title  Increment by 63
     */
    public void testN3() {
        T_iinc_3 t = new T_iinc_3();
        assertEquals(67, t.run(4));
    }

    /**
     * @title  Increment by 0
     */
    public void testB1() {
        T_iinc_4 t = new T_iinc_4();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Increment by 0
     */
    public void testB2() {
        T_iinc_4 t = new T_iinc_4();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by 127
     */
    public void testB3() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(128, t.run(1));
    }

    /**
     * @title  Increment by 127
     */
    public void testB4() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(126, t.run(-1));
    }

    /**
     * @title  Increment by 127
     */
    public void testB5() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(-2147483521, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by -128
     */
    public void testB6() {
        T_iinc_6 t = new T_iinc_6();
        assertEquals(-127, t.run(1));
    }

    /**
     * @title  Increment by -128
     */
    public void testB7() {
        T_iinc_6 t = new T_iinc_6();
        assertEquals(-128, t.run(0));
    }

    /**
     * @constraint 4.8.1.21
     * @title index must be no greater than
     * max_locals-1.
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.21
     * @title index must be a nonnegative integer.
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - double
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /*
     * WIDE IINC VERSION
     */

    /**
     * @title  Increment by 1
     */
    public void testN4() {
        T_iinc_1_w t = new T_iinc_1_w();
        assertEquals(5, t.run(4));
    }

    /**
     * @title  Increment by -1
     */
    public void testN5() {
        T_iinc_2_w t = new T_iinc_2_w();
        assertEquals(3, t.run(4));
    }

    /**
     * @title  Increment by 7763
     */
    public void testN6() {
        T_iinc_3_w t = new T_iinc_3_w();
        assertEquals(7767, t.run(4));
    }

    /**
     * @title  Increment by 0
     */
    public void testB8() {
        T_iinc_4_w t = new T_iinc_4_w();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Increment by 0
     */
    public void testB9() {
        T_iinc_4_w t = new T_iinc_4_w();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by 32767
     */
    public void testB10() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(32768, t.run(1));
    }

    /**
     * @title  Increment by 32767
     */
    public void testB11() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(32766, t.run(-1));
    }

    /**
     * @title  Increment by 32767
     */
    public void testB12() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(-2147450881, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by -32768
     */
    public void testB13() {
        T_iinc_6_w t = new T_iinc_6_w();
        assertEquals(-32767, t.run(1));
    }

    /**
     * @title  Increment by -32768
     */
    public void testB14() {
        T_iinc_6_w t = new T_iinc_6_w();
        assertEquals(-32768, t.run(0));
    }

    /**
     * @constraint 4.8.1.25
     * @title index must be no greater than
     * max_locals-1.
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_7_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.25
     * @title index must be a nonnegative integer.
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_8_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - double
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_9_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - long
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_10_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
