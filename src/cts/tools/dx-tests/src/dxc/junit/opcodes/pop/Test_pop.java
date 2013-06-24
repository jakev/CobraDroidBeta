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

package dxc.junit.opcodes.pop;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.pop.jm.T_pop_1;
import dxc.junit.opcodes.pop.jm.T_pop_2;

public class Test_pop extends DxTestCase {

    /**
     * @title  type of argument - int
     */
    public void testN1() {
        T_pop_1 t = new T_pop_1();
        assertEquals(1234, t.run());
    }

    /**
     * @title  type of argument - float
     */
    public void testN2() {
        T_pop_2 t = new T_pop_2();
        assertEquals(1234f, t.run());
    }


    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.pop.jm.T_pop_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - long
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.pop.jm.T_pop_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
