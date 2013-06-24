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

package dxc.junit.opcodes.iand;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.iand.jm.T_iand_1;

public class Test_iand extends DxTestCase {

    /**
     * @title Arguments = 15, 8
     */
    public void testN1() {
        T_iand_1 t = new T_iand_1();
        assertEquals(8, t.run(15, 8));
    }

    /**
     * @title Arguments = 0xfffffff8, 0xfffffff1
     */
    public void testN2() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0xfffffff0, t.run(0xfffffff8, 0xfffffff1));
    }

    /**
     * @title  Arguments = 0xcafe & -1
     */
    public void testN3() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0xcafe, t.run(0xcafe, -1));
    }

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE & Integer.MIN_VALUE
     */
    public void testB2() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - double & int
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long & int
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int & reference
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
