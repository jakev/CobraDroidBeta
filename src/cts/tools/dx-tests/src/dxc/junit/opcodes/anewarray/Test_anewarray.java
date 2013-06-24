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

package dxc.junit.opcodes.anewarray;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.anewarray.jm.T_anewarray_1;
import dxc.junit.opcodes.anewarray.jm.T_anewarray_6;
import dxc.junit.opcodes.anewarray.jm.T_anewarray_7;

public class Test_anewarray extends DxTestCase {
    
    /**
     * @title Test for Object
     */
    public void testN1() {
        T_anewarray_1 t = new T_anewarray_1();

        Object[] arr = t.run(10);
        assertNotNull(arr);
        assertEquals(10, arr.length);
        for (int i = 0; i < 10; i++)
            assertNull(arr[i]);
    }

    /**
     * @title Test for String
     */
    public void testN2() {
        T_anewarray_1 t = new T_anewarray_1();

        String[] arr2 = t.run2(5);
        assertNotNull(arr2);
        assertEquals(5, arr2.length);
        for (int i = 0; i < 5; i++)
            assertNull(arr2[i]);
    }

    /**
     * @title Test for Integer
     */
    public void testN3() {
        T_anewarray_1 t = new T_anewarray_1();

        Integer[] arr3 = t.run3(15);
        assertNotNull(arr3);
        assertEquals(15, arr3.length);
        for (int i = 0; i < 15; i++)
            assertNull(arr3[i]);
    }

    /**
     * @title if count is zero, no subsequent dimensions allocated
     */
    public void testE1() {
        T_anewarray_1 t = new T_anewarray_1();
        Object[] res = t.run(0);
        try {
            Object s = res[0];
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ae) {
            // expected
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE2() {
        T_anewarray_1 t = new T_anewarray_1();
        try {
            t.run(-2);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title expected NoClassDefFoundError
     */
    public void testE3() {
        try {
            T_anewarray_6 t = new T_anewarray_6();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; eagerly tries to load the array type
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title expected IllegalAccessError
     * <pre>
     * V(15469) +++ dvmAddClassToHash '[Ldxc/junit/opcodes/anewarray/jm/TestStubs$TestStub;' 0x973d7708 (isnew=1) --> 0x973e1f10  (dalvikvm)
     * V(15469) Created array class '[Ldxc/junit/opcodes/anewarray/jm/TestStubs$TestStub;' 0x973d7708 (access=0x6000.0010)  (dalvikvm)
     * </pre>
     * TestStub class is private. no IllegalAccessError is thrown, but VerifyError
     */
    public void testE4() {
        try {
            T_anewarray_7 t = new T_anewarray_7();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.19
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_2");
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
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_3");
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
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.17
     * @title array of more than 255 dimensions
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - reference
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.19
     * @title constant pool type
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
