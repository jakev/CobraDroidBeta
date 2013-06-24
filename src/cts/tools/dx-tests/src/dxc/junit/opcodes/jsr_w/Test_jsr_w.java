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

package dxc.junit.opcodes.jsr_w;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.jsr_w.jm.T_jsr_w_1;
import dxc.junit.opcodes.jsr_w.jm.T_jsr_w_2;

public class Test_jsr_w extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_jsr_w_1 t = new T_jsr_w_1();
        assertTrue(t.run());
    }

    /**
     * @title  nested jsrs
     */
    public void testN2() {
        T_jsr_w_2 t = new T_jsr_w_2();
        assertTrue(t.run());
    }

    /**
     * @constraint 4.8.2.23
     * @title recursion of jsr
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_4");
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
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.7
     * @title branch target inside wide instruction
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
