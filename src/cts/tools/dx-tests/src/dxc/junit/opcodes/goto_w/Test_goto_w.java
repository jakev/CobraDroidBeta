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

package dxc.junit.opcodes.goto_w;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.goto_w.jm.T_goto_w_1;
import dxc.junit.opcodes.goto_w.jm.T_goto_w_5;

public class Test_goto_w extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_goto_w_1 t = new T_goto_w_1();
        assertEquals(0, t.run(20));
    }

    /**
     * @title normal test
     */
    public void testN2() {
        T_goto_w_1 t = new T_goto_w_1();
        assertEquals(-20, t.run(-20));
    }

    /**
     * @title  negative offset
     */
    public void testN3() {
        T_goto_w_5 t = new T_goto_w_5();
        assertTrue(t.run());
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target is inside instruction
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target shall be inside the
     * method
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target shall not be "inside" wide
     * instruction
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
