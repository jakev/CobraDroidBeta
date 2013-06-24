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

package dxc.junit.opcodes.pop2;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.pop2.jm.T_pop2_1;
import dxc.junit.opcodes.pop2.jm.T_pop2_2;

public class Test_pop2 extends DxTestCase {

    /**
     * @title  type of argument - long
     */
    public void testN1() {
        T_pop2_1 t = new T_pop2_1();
        assertEquals(1234l, t.run());
    }

    /**
     * @title  type of argument - double
     */
    public void testN2() {
        T_pop2_2 t = new T_pop2_2();
        assertEquals(1234d, t.run());
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.pop2.jm.T_pop2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - int
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.pop2.jm.T_pop2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
