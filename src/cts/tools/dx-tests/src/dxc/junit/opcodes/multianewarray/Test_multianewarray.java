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

package dxc.junit.opcodes.multianewarray;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.multianewarray.jm.T_multianewarray_1;
import dxc.junit.opcodes.multianewarray.jm.T_multianewarray_2;
import dxc.junit.opcodes.multianewarray.jm.T_multianewarray_7;
import dxc.junit.opcodes.multianewarray.jm.T_multianewarray_9;

public class Test_multianewarray extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_multianewarray_1 t = new T_multianewarray_1();
        String[][][] res = t.run(2, 5, 4);

        assertEquals(2, res.length);

        // check default initialization
        for (int i = 0; i < 2; i++) {
            assertEquals(5, res[i].length);

            for (int j = 0; j < 5; j++) {
                assertEquals(4, res[i][j].length);

                for (int k = 0; j < 4; j++) {
                    assertNull(res[i][j][k]);
                }
            }
        }
    }

    /**
     * @title  if count is zero, no subsequent dimensions allocated
     */
    public void testN2() {
        T_multianewarray_1 t = new T_multianewarray_1();
        String[][][] res = t.run(2, 0, 4);

        try {
            String s = res[2][0][0];
            fail("expected ArrayIndexOutOfBoundsException");
            fail("dummy for s "+s);
        } catch (ArrayIndexOutOfBoundsException ae) {
            // expected
        }
    }

    /**
     * @title  multinewarray must only be used to create array with
     * dimensions specified by dimensions operand
     */
    public void testN3() {
        T_multianewarray_9 t = new T_multianewarray_9();
        String[][][] res = t.run(2, 1, 4);

        if (res.length != 2) fail("incorrect multiarray length");
        if (res[0].length != 1) fail("incorrect array length");

        try {
            int i = res[0][0].length;
            fail("expected NullPointerException");
            fail("dummy for i "+i);
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE1() {
        T_multianewarray_1 t = new T_multianewarray_1();
        try {
            t.run(2, -5, 3);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title expected IllegalAccessError
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.multianewarray.jm.sub.TestStubs$TestStub
        try {
            T_multianewarray_2 t = new T_multianewarray_2();
            t.run(2, 5, 3);
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title expected NoClassDefFoundError
     */
    public void testE3() {
        try {
        T_multianewarray_7 t = new T_multianewarray_7();
            t.run(2, 5, 3);
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - float
     */
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.19
     * @title dimension size must not be zero
     */
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title constant pool type
     */
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - reference
     */
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
