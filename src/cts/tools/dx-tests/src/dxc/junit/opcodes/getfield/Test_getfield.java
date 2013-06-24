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

package dxc.junit.opcodes.getfield;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.getfield.jm.T_getfield_1;
import dxc.junit.opcodes.getfield.jm.T_getfield_10;
import dxc.junit.opcodes.getfield.jm.T_getfield_11;
import dxc.junit.opcodes.getfield.jm.T_getfield_12;
import dxc.junit.opcodes.getfield.jm.T_getfield_14;
import dxc.junit.opcodes.getfield.jm.T_getfield_16;
import dxc.junit.opcodes.getfield.jm.T_getfield_17;
import dxc.junit.opcodes.getfield.jm.T_getfield_2;
import dxc.junit.opcodes.getfield.jm.T_getfield_5;
import dxc.junit.opcodes.getfield.jm.T_getfield_6;
import dxc.junit.opcodes.getfield.jm.T_getfield_7;
import dxc.junit.opcodes.getfield.jm.T_getfield_8;
import dxc.junit.opcodes.getfield.jm.T_getfield_9;
import dxc.junit.opcodes.getfield.jm.TestStubs;

public class Test_getfield extends DxTestCase {
    private int TestStubField = 123;
    protected int TestStubFieldP = 0;

    private int privateInt = 456;
    
    /**
     * @title  type - int
     */
    public void testN1() {
        T_getfield_1 t = new T_getfield_1();
        assertEquals(5, t.run());
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_getfield_2 t = new T_getfield_2();
        assertEquals(123d, t.run());
    }

    /**
     * @title  access protected field from subclass
     */
    public void testN3() {
     // @uses dxc.junit.opcodes.getfield.jm.T_getfield_1
        T_getfield_11 t = new T_getfield_11();
        assertEquals(10, t.run());
    }

    /**
     * @title  assignment compatible references
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.getfield.jm.TChild        
        // @uses dxc.junit.opcodes.getfield.jm.TSuper        
        T_getfield_14 t = new T_getfield_14();
        assertEquals(0, t.run().compareTo("abc"));
    }

    /**
     * @title  attempt to access static field
     */
    public void testE1() {
        // @uses dxc.junit.opcodes.getstatic.jm.T_getstatic_1
        try {
            T_getfield_5 t = new T_getfield_5();
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
     * @title  attempt to access of non-accessible private field
     */
    public void testE2() {
        try {
            T_getfield_6 t = new T_getfield_6();
            int res = t.run();
            System.out.println("res:"+res);
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
            // need to include the constructor call into the try-catch block,
            // since class resolution can take place at any time.
            // (not only when t.run() is called
            T_getfield_7 t = new T_getfield_7();
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
            T_getfield_8 t = new T_getfield_8();
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
     * @title  attempt to get int from float field
     */
    public void testE5() {
        try {
            T_getfield_10 t = new T_getfield_10();
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
     * @title expected NullPointerException
     */
    public void testE6() {
        T_getfield_9 t = new T_getfield_9();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    //FIXME: "fail" commented out temporarily - check
    /**
     * @title  attempt to read superclass' private field from subclass
     * in same package
     * 
     * FIXME: this seems to be a bug in JDK 1.5?
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.getfield.jm.T_getfield_1
        try {
            T_getfield_12 t = new T_getfield_12();
            //fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  attempt to read private field of a class which was passed
     * as argument
     */
    public void testE9() {
        // @uses dxc.junit.opcodes.getfield.jm.TestStubs
        try {
            T_getfield_17 t = new T_getfield_17();
            t.run(new dxc.junit.opcodes.getfield.jm.TestStubs());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    

    /**
     * @title  attempt to access of non-accessible protected field
     */
    public void testE8() {
        try {
            T_getfield_16 t = new T_getfield_16();
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
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.5
     * @title stack size
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.12
     * @title constant pool type
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title assignment incompatible references
     */
    public void testVFE4() {
        // @uses dxc.junit.opcodes.getfield.jm.TChild        
        // @uses dxc.junit.opcodes.getfield.jm.TSuper        
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
