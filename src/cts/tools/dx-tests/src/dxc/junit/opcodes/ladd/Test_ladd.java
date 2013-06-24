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

package dxc.junit.opcodes.ladd;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.ladd.jm.T_ladd_1;

public class Test_ladd extends DxTestCase {

    /**
     * @title Arguments = 12345678l, 87654321l
     */
    public void testN1() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(99999999l, t.run(12345678l, 87654321l));
    }

    /**
     * @title Arguments = 0l, 87654321l
     */
    public void testN2() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(87654321l, t.run(0l, 87654321l));
    }

    /**
     * @title Arguments = -12345678l, 0l
     */
    public void testN3() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-12345678l, t.run(-12345678l, 0l));
    }

    /**
     * @title  Arguments: 0 + Long.MAX_VALUE
     */
    public void testB1() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(9223372036854775807L, t.run(0l, Long.MAX_VALUE));
    }

    /**
     * @title  Arguments: 0 + Long.MIN_VALUE
     */
    public void testB2() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775808L, t.run(0l, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: 0 + 0
     */
    public void testB3() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(0l, t.run(0l, 0l));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + Long.MAX_VALUE
     */
    public void testB4() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-2, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + Long.MIN_VALUE
     */
    public void testB5() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: Long.MIN_VALUE + Long.MIN_VALUE
     */
    public void testB6() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: Long.MIN_VALUE + 1
     */
    public void testB7() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775807l, t.run(Long.MIN_VALUE, 1l));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + 1
     */
    public void testB8() {
        T_ladd_1 t = new T_ladd_1();
        assertEquals(-9223372036854775808l, t.run(Long.MAX_VALUE, 1l));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */

    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long / double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long / integer
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long / float
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - reference / long
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ladd.jm.T_ladd_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
