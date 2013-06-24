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

package dxc.junit.opcodes.aaload;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.aaload.jm.T_aaload_1;

public class Test_aaload extends DxTestCase {

    /**
     * @title Normal test. Trying different indexes 
     */
    public void testN1() {
        T_aaload_1 t = new T_aaload_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("a", t.run(arr, 0));
    }

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN2() {
        T_aaload_1 t = new T_aaload_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("b", t.run(arr, 1));
    }

    /**
     * @title ArrayIndexOutOfBoundsException expected
     */
    public void testE1() {
        T_aaload_1 t = new T_aaload_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // expected
        }
    }

    /**
     * @title Negative index. ArrayIndexOutOfBoundsException expected
     */
    public void testE2() {
        T_aaload_1 t = new T_aaload_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // expected
        }
    }

    /**
     * @title NullPointerException expected
     */
    public void testE3() {
        T_aaload_1 t = new T_aaload_1();
        String[] arr = null;
        try {
            t.run(arr, 0);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_2");
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
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, double
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - Object, int
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float[], int
     */
    public void testVFE6() {
        try { // opcodes.aastore.jm
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long[], int
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, reference
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.aaload.jm.T_aaload_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
