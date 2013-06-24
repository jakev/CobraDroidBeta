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

package dxc.junit.opcodes.invokestatic;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_1;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_12;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_13;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_14;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_15;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_16;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_17;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_18;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_2;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_4;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_5;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_6;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_7;
import dxc.junit.opcodes.invokestatic.jm.T_invokestatic_8;

public class Test_invokestatic extends DxTestCase {

    /**
     * @title  Static method from library class Math
     */
    public void testN1() {
        T_invokestatic_1 t = new T_invokestatic_1();
        assertEquals(1234567, t.run());
    }

    /**
     * @title  Static method from user class
     */
    public void testN2() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClass
        T_invokestatic_2 t = new T_invokestatic_2();
        assertEquals(777, t.run());
    }

    /**
     * @title  Check that <clinit> is called
     */
    public void testN3() {
        assertEquals(123456789l, T_invokestatic_4.run());
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN4() {
        assertTrue(T_invokestatic_12.execute());
    }

    /**
     * @title  Check that new frame is created by invokestatic and
     * arguments are passed to method
     */
    public void testN5() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClass
        T_invokestatic_15 t = new T_invokestatic_15();
        assertTrue(t.run());
    }

    /**
     * @title  Static protected method from other class in the same package
     */
    public void testN6() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClass
        T_invokestatic_18 t = new T_invokestatic_18();
        assertEquals(888, t.run());
    }

    /**
     * @title  attempt to call non-static method
     * 
     */
    public void testE1() {
        try {
            T_invokestatic_5 t = new T_invokestatic_5();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError icce) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Native method can't be linked
     * 
     */
    public void testE2() {
        T_invokestatic_6 t = new T_invokestatic_6();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
            // expected
        }
    }

    /**
     * @title  NoSuchMethodError
     */
    public void testE3() {
        try {
            T_invokestatic_7 t = new T_invokestatic_7();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError nsme) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to call private method of other class
     * 
     */
    public void testE5() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClass
        try {
            T_invokestatic_8 t = new T_invokestatic_8();
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
     * @title  method has different signature
     */
    public void testE6() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClass
        try {
               T_invokestatic_13 t = new T_invokestatic_13();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError nsme) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  initialization of referenced class throws exception
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClassInitError
        T_invokestatic_14 t = new T_invokestatic_14();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

    /**
     * @title  Attempt to call abstract method of other class
     * 
     */
    public void testE8() {
        // @uses dxc.junit.opcodes.invokestatic.jm.TestClassAbstract
        try {
            T_invokestatic_16 t = new T_invokestatic_16();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to call protected method of unrelated class
     * 
     */
    public void testE9() {
        // @uses dxc.junit.opcodes.invokestatic.TestStubs
        try {
            T_invokestatic_17 t = new T_invokestatic_17();
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
     * @constraint 4.8.1.13
     * @title invalid type into constant pool
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.13
     * @title invalid index into constant pool table
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.14 
     * @title &lt;clinit&gt; may not be called using invokestatic
     */
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1 
     * @title number of arguments passed to method
     */
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.14 
     * @title &lt;init&gt; may not be called using invokestatic
     */
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.12 
     * @title types of arguments passed to method
     */
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
