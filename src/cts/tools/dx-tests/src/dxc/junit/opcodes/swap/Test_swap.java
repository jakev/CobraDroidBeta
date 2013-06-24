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

package dxc.junit.opcodes.swap;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.swap.jm.T_swap_1;
import dxc.junit.opcodes.swap.jm.T_swap_6;
import dxc.junit.opcodes.swap.jm.T_swap_7;
import dxc.junit.opcodes.swap.jm.T_swap_8;

public class Test_swap extends DxTestCase {

    /**
     * @title  Integers
     */
    public void testN1() {
        T_swap_1 t = new T_swap_1();
        assertEquals(8, t.run(15, 8));
    }

    /**
     * @title  Floats
     */
    public void testN2() {
        T_swap_6 t = new T_swap_6();
        assertEquals(8f, t.run(15f, 8f));
    }

    /**
     * @title  References
     */
    public void testN3() {
        T_swap_7 t = new T_swap_7();
        int tmp[] = new int[1];
        assertEquals(tmp, t.run(this, tmp));
    }

    /**
     * @title  Reference & int
     */
    public void testN4() {
        T_swap_8 t = new T_swap_8();
        assertEquals(this, t.run(0xff, this));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_2");
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
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_3");
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
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long & long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
