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

package dxc.junit.opcodes.aastore;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.aastore.jm.T_aastore_1;
import dxc.junit.opcodes.aastore.jm.T_aastore_10;
import dxc.junit.opcodes.aastore.jm.T_aastore_11;
import dxc.junit.opcodes.aastore.jm.T_aastore_4;

public class Test_aastore extends DxTestCase {

    /**
     * @title Normal test. Trying different indexes 
     */
    public void testN1() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        t.run(arr, 0, "hello");
        assertEquals("hello", arr[0]);
    }

    /**
     * @title Normal test. Trying different indexes 
     */
    public void testN2() {
        T_aastore_1 t = new T_aastore_1();
        String[] value = {"world", null, ""};
        String[] arr = new String[2];
        for (int i = 0; i < value.length; i++) {
            t.run(arr, 1, value[i]);
            assertEquals(value[i], arr[1]);
        }
    }

    /**
     * @title Normal test. Trying different indexes 
     */
    public void testN3() {
        T_aastore_10 t = new T_aastore_10();
        Integer[] arr = new Integer[2];
        Integer value = new Integer(12345);
        t.run(arr, 0, value);
        assertEquals(value, arr[0]);
    }

    /**
     * @title  Check assignement compatibility rules
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.aastore.jm.SubClass
        // @uses dxc.junit.opcodes.aastore.jm.SuperClass
        // @uses dxc.junit.opcodes.aastore.jm.SuperInterface
        // @uses dxc.junit.opcodes.aastore.jm.SuperInterface2
        T_aastore_11 t = new T_aastore_11();
        assertEquals(3, t.run());

    }

    /**
     * @title ArrayIndexOutOfBoundsException expected
     */
    public void testE1() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        try {
            t.run(arr, arr.length, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE2() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        try {
            t.run(arr, -1, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE3() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = null;
        try {
            t.run(arr, 0, "abc");
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayStoreException
     */
    public void testE4() {
        T_aastore_4 t = new T_aastore_4();
        String[] arr = new String[2];
        try {
            t.run(arr, 0, this);
            fail("expected ArrayStoreException");
        } catch (ArrayStoreException aie) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_2");
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
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, double, String
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, int, long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - object, int, String
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float[], int, String
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long[], int, String
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, reference, String
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
