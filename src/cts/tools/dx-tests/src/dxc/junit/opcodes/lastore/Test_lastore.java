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

package dxc.junit.opcodes.lastore;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.lastore.jm.T_lastore_1;

public class Test_lastore extends DxTestCase {

    /**
     * @title normal test. trying different indexes
     */
    public void testN1() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        t.run(arr, 1, 100000000000l);
        assertEquals(100000000000l, arr[1]);
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN2() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        t.run(arr, 0, 100000000000l);
        assertEquals(100000000000l, arr[0]);
    }

    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        try {
            t.run(arr, 2, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title  Exception - NullPointerException
     */
    public void testE2() {
        T_lastore_1 t = new T_lastore_1();
        try {
            t.run(null, 1, 100000000000l);
            fail("expected NullPointerException");
        } catch (NullPointerException np) {
            // expected
        }
    }

    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE3() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        try {
            t.run(arr, -1, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_2");
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
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, double,
     * long
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, int, int
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - object, int, long
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - double[], int,
     * long
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int[], int, long
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, reference,
     * long
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
