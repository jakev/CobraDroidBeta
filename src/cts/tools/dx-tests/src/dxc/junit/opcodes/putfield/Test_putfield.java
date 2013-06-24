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

package dxc.junit.opcodes.putfield;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.putfield.jm.T_putfield_1;
import dxc.junit.opcodes.putfield.jm.T_putfield_10;
import dxc.junit.opcodes.putfield.jm.T_putfield_11;
import dxc.junit.opcodes.putfield.jm.T_putfield_12;
import dxc.junit.opcodes.putfield.jm.T_putfield_13;
import dxc.junit.opcodes.putfield.jm.T_putfield_14;
import dxc.junit.opcodes.putfield.jm.T_putfield_15;
import dxc.junit.opcodes.putfield.jm.T_putfield_16;
import dxc.junit.opcodes.putfield.jm.T_putfield_18;
import dxc.junit.opcodes.putfield.jm.T_putfield_2;
import dxc.junit.opcodes.putfield.jm.T_putfield_7;
import dxc.junit.opcodes.putfield.jm.T_putfield_8;
import dxc.junit.opcodes.putfield.jm.T_putfield_9;

public class Test_putfield extends DxTestCase {

    /**
     * @title  type - int
     */
    public void testN1() {
        T_putfield_1 t = new T_putfield_1();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_putfield_2 t = new T_putfield_2();
        assertEquals(0d, t.st_d1);
        t.run();
        assertEquals(1000000d, t.st_d1);
    }

    /**
     * @title  modification of final field
     */
    public void testN3() {
        T_putfield_12 t = new T_putfield_12();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.putfield.jm.T_putfield_1
        T_putfield_14 t = new T_putfield_14();
        assertEquals(0, t.getProtectedField());
        t.run();
        assertEquals(1000000, t.getProtectedField());
    }

    /**
     * @title  assignment compatible object references
     */
    public void testN5() {
        // @uses dxc.junit.opcodes.putfield.jm.TChild
        // @uses dxc.junit.opcodes.putfield.jm.TSuper
        T_putfield_18 t = new T_putfield_18();
        assertEquals(0, t.run().compareTo("xyz"));
    }

    /**
     * @title  assignment compatible values
     */
    public void testN6() {
        T_putfield_16 t = new T_putfield_16();
        assertNull(t.o);
        t.run();
        assertEquals("", (String) t.o);
    }

    /**
     * @title  attempt to set static field
     */
    public void testE1() {
        try {
            T_putfield_7 t = new T_putfield_7();
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
     * @title  modification of non-accessible private field
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.putfield.TPutfield
        try {
            T_putfield_8 t = new T_putfield_8();
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
     * @title expected NullPointerException
     */
    public void testE3() {
        T_putfield_9 t = new T_putfield_9();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @title expected NoSuchFieldError
     */
    public void testE4() {
        try {
        T_putfield_10 t = new T_putfield_10();
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
        // @uses dxc.junit.opcodes.putfield.TPutfield
        try {
            T_putfield_11 t = new T_putfield_11();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }


    //  FIXME: "fail" commented out temporarily - check
    /**
     * @title  modification of superclass' private field from subclass
     * FIXME: is this a JVM bug?
     */
    public void testE6() {
        // @uses dxc.junit.opcodes.putfield.jm.T_putfield_1
        try {
            T_putfield_15 t = new T_putfield_15();
            assertEquals(0, t.getPvtField());
            t.run();
            assertEquals(10101, t.getPvtField());
            //fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  modification of non-accessible protected field
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.putfield.TPutfield
        try {
            T_putfield_13 t = new T_putfield_13();
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
     * @constraint 4.8.1.12
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_3");
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
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_4");
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
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_5");
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
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_6");
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
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.16
     * @title type of argument - assignment
     * incompatible object references
     */
    public void testVFE6() {
        // @uses dxc.junit.opcodes.putfield.jm.TChild
        // @uses dxc.junit.opcodes.putfield.jm.TSuper
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
