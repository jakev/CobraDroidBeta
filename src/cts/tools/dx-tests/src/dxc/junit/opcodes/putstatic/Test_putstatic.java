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

package dxc.junit.opcodes.putstatic;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_1;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_10;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_11;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_12;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_13;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_14;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_15;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_16;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_2;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_7;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_8;
import dxc.junit.opcodes.putstatic.jm.T_putstatic_9;

public class Test_putstatic extends DxTestCase {

    /**
     * @title  type - int
     */
    public void testN1() {
        T_putstatic_1 t = new T_putstatic_1();
        assertEquals(0, T_putstatic_1.st_i1);
        t.run();
        assertEquals(1000000, T_putstatic_1.st_i1);
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_putstatic_2 t = new T_putstatic_2();
        assertEquals(0d, T_putstatic_2.st_d1);
        t.run();
        assertEquals(1000000d, T_putstatic_2.st_d1);
    }

    /**
     * @title  modification of final field
     */
    public void testN3() {
        T_putstatic_12 t = new T_putstatic_12();
        assertEquals(0, T_putstatic_12.st_i1);
        t.run();
        assertEquals(1000000, T_putstatic_12.st_i1);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.putstatic.jm.T_putstatic_1
        T_putstatic_14 t = new T_putstatic_14();
        assertEquals(0, T_putstatic_14.getProtectedField());
        t.run();
        assertEquals(1000000, T_putstatic_14.getProtectedField());
    }

    /**
     * @title  assignment compatible references
     */
    public void testN5() {
        T_putstatic_16 t = new T_putstatic_16();
        assertNull(T_putstatic_16.o);
        t.run();
        assertEquals("", (String) T_putstatic_16.o);
    }

    /**
     * @title  attempt to set non-static field
     */
    public void testE1() {
        try {
            T_putstatic_7 t = new T_putstatic_7();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  modification of non-accessible field
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.putstatic.TestStubs
        try {
            T_putstatic_8 t = new T_putstatic_8();
            t.run();
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
            T_putstatic_9 t = new T_putstatic_9();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title expected NoSuchFieldError
     */
    public void testE4() {
        try {
            T_putstatic_10 t = new T_putstatic_10();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Modification of final field in other class
     */
    public void testE5() {
        // @uses dxc.junit.opcodes.putstatic.TestStubs
        try {
            T_putstatic_11 t = new T_putstatic_11();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  initialization of referenced class throws exception
     */
    public void testE6() {
        // @uses dxc.junit.opcodes.putstatic.jm.StubInitError
        T_putstatic_13 t = new T_putstatic_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

    //  FIXME: "fail" commented out temporarily - check
    /**
     * @title  modification of superclass' private field from subclass
     * FIXME: is this a bug in JVM?
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.putstatic.jm.T_putstatic_1
        try {
            assertEquals(0, T_putstatic_15.getPvtField());
            T_putstatic_15 t = new T_putstatic_15();
            t.run();
            assertEquals(12321, T_putstatic_15.getPvtField());
            //fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.12
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_3");
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
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.17
     * @title type of argument - float instead of
     * int
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.17
     * @title type of argument - assignment
     * incompatible references
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.17
     * @title type of argument - assignment
     * incompatible values
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }


    /**
     * @constraint 4.8.1.12
     * @title constant pool type
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
