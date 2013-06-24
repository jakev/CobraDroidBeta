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

package dxc.junit.opcodes.ldc;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.ldc.jm.T_ldc_1;
import dxc.junit.opcodes.ldc.jm.T_ldc_2;
import dxc.junit.opcodes.ldc.jm.T_ldc_3;

public class Test_ldc extends DxTestCase {

    /**
     * @title push string into stack
     */
    public void testN1() {
        T_ldc_1 t = new T_ldc_1();
        // lcd is hard to test isolated
        String res = t.run();
        assertEquals(5, res.length());
        assertEquals('h', res.charAt(0));
    }

    /**
     * @title push float into stack
     */
    public void testN2() {
        T_ldc_2 t = new T_ldc_2();
        float a = 1.5f;
        float b = 0.04f;
        assertEquals(a + b, t.run(), 0f);
        assertEquals(1.54f, t.run(), 0f);
    }

    /**
     * @title push int into stack
     */
    public void testN3() {
        T_ldc_3 t = new T_ldc_3();
        int a = 1000000000;
        int b = 1000000000;
        assertEquals(a + b, t.run());
    }

    /**
     * @constraint 4.8.1.10
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ldc.jm.T_ldc_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.10
     * @title wrong constant pool entry type (long)
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ldc.jm.T_ldc_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
