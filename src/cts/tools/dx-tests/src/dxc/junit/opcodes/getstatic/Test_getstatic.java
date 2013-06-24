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

package dxc.junit.opcodes.getstatic;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_1;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_10;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_11;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_12;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_2;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_5;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_6;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_7;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_8;
import dxc.junit.opcodes.getstatic.jm.T_getstatic_9;

public class Test_getstatic extends DxTestCase {
    
    /**
     * @title  type - int
     */
    public void testN1() {
        T_getstatic_1 t = new T_getstatic_1();
        assertEquals(5, t.run());
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_getstatic_2 t = new T_getstatic_2();
        assertEquals(123d, t.run());
    }

    /**
     * @title  access protected field from subclass
     */
    public void testN3() {
        // @uses dxc.junit.opcodes.getstatic.jm.T_getstatic_1
        T_getstatic_11 t = new T_getstatic_11();
        assertEquals(10, t.run());
    }

    /**
     * @title  attempt to access non-static field
     */
    public void testE1() {
        T_getstatic_5 t = new T_getstatic_5();
        try {
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
            // expected
        }
    }

    /**
     * @title  attempt to access of non-accessible field
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.getstatic.TestStubs        
        try {
            T_getstatic_6 t = new T_getstatic_6();
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
            T_getstatic_7 t = new T_getstatic_7();
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
               T_getstatic_8 t = new T_getstatic_8();
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
            T_getstatic_10 t = new T_getstatic_10();
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
     * @title  initialization of referenced class throws exception
     */
    public void testE6() {
        // @uses dxc.junit.opcodes.getstatic.jm.StubInitError
        T_getstatic_9 t = new T_getstatic_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

    //  FIXME: "fail" commented out temporarily - check
    /**
     * @title  attempt to read superclass' private field from subclass
     * 
     * FIXME: is this a JVM bug?
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.getstatic.jm.T_getstatic_1
        try {
            T_getstatic_12 t = new T_getstatic_12();
            t.run();
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
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_4");
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
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_3");
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
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
