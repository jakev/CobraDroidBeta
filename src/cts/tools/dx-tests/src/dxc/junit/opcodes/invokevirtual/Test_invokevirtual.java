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

package dxc.junit.opcodes.invokevirtual;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_1;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_13;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_14;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_15;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_17;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_18;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_19;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_2;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_20;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_3;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_4;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_5;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_6;
import dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_7;

public class Test_invokevirtual extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_invokevirtual_1 t = new T_invokevirtual_1();
        int a = 1;
        String sa = "a" + a;
        String sb = "a1";
        assertTrue(t.run(sa, sb));
        assertFalse(t.run(this, sa));
        assertFalse(t.run(sb, this));
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN2() {
        assertTrue(T_invokevirtual_2.execute());
    }

    /**
     * @title  Invoke protected method of superclass
     */
    public void testN3() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        T_invokevirtual_7 t = new T_invokevirtual_7();
        assertEquals(5, t.run());
    }

    /**
     * @title  Private method call
     */
    public void testN4() {
        T_invokevirtual_13 t = new T_invokevirtual_13();
        assertEquals(345, t.run());
    }

    /**
     * @title  Check that new frame is created by invokevirtual and
     * arguments are passed to method
     */
    public void testN5() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        T_invokevirtual_14 t = new T_invokevirtual_14();
        assertTrue(t.run());
    }

    /**
     * @title  Recursion of method lookup procedure
     */
    public void testN6() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        T_invokevirtual_17 t = new T_invokevirtual_17();
        assertEquals(5, t.run());
    }

    /**
     * @title 
     */
    public void testE1() {
        T_invokevirtual_3 t = new T_invokevirtual_3();
        String s = "s";
        try {
            t.run(null, s);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title  Native method can't be linked
     */
    public void testE2() {
        T_invokevirtual_4 t = new T_invokevirtual_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
            // expected
        }
    }

    /**
     * @title  Attempt to invoke static method
     */
    public void testE3() {
        try {
            T_invokevirtual_5 t = new T_invokevirtual_5();
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
     * @title  Attempt to invoke abstract method
     */
    public void testE4() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.ATest
        T_invokevirtual_6 t = new T_invokevirtual_6();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError iae) {
            // expected
        }
    }

    /**
     * @title  Attempt to invoke non-existing method
     */
    public void testE5() {
        try {
            T_invokevirtual_15 t = new T_invokevirtual_15();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to invoke private method of other class
     */
    public void testE6() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        // @uses dxc.junit.opcodes.invokevirtual.TProtected
        try {
            T_invokevirtual_18 t = new T_invokevirtual_18();
            t.run(new TProtected());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to invoke protected method of other class
     */
    public void testE7() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        // @uses dxc.junit.opcodes.invokevirtual.TProtected
        try {
            T_invokevirtual_20 t = new T_invokevirtual_20();
            t.run(new TProtected());
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
    public void testE8() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        try {
            T_invokevirtual_19 t = new T_invokevirtual_19();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.13 
     * @title valid index into constant pool table
     */
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_8");
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
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.14 
     * @title &lt;clinit&gt; may not be called using invokevirtual
     */
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_10");
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
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1 
     * @title type of argument - int
     */
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.14 
     * @title &lt;init&gt; may not be called using invokevirtual
     */
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.12 
     * @title types of arguments passed to method
     */
    public void testVFE7() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.13 
     * @title assignment incompatible references when accessing
     *                  protected method
     */
    public void testVFE8() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        // @uses dxc.junit.opcodes.invokevirtual.jm.TPlain
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.13 
     * @title assignment incompatible references when accessing
     *                  public method
     */
    public void testVFE9() {
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper
        // @uses dxc.junit.opcodes.invokevirtual.jm.TSuper2
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
