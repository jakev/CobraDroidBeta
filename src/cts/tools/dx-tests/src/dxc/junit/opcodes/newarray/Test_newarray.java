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

package dxc.junit.opcodes.newarray;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.newarray.jm.T_newarray_1;
import dxc.junit.opcodes.newarray.jm.T_newarray_2;

public class Test_newarray extends DxTestCase {

    /**
     * @title  Array of ints
     */
    public void testN1() {
        T_newarray_1 t = new T_newarray_1();
        int[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);

        // check default initialization
        for (int i = 0; i < l; i++) {
            assertEquals(0, r[i]);
        }

    }

    /**
     * @title  Array of floats
     */
    public void testN2() {
        T_newarray_2 t = new T_newarray_2();
        float[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);

        // check default initialization
        for (int i = 0; i < l; i++) {
            assertEquals(0f, r[i]);
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE1() {
        T_newarray_2 t = new T_newarray_2();
        try {
            t.run(-1);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title  Array size = 0
     */
    public void testB1() {
        T_newarray_1 t = new T_newarray_1();
        int[] r = t.run(0);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - float
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.20
     * @title atype must take one of the following
     * values
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_5");
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
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
