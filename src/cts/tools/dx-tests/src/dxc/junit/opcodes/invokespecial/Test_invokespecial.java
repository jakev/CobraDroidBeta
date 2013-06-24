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

package dxc.junit.opcodes.invokespecial;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_1;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_11;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_12;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_13;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_15;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_16;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_17;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_18;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_19;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_2;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_21;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_22;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_7;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_8;
import dxc.junit.opcodes.invokespecial.jm.T_invokespecial_9;

public class Test_invokespecial extends DxTestCase {
    /**
     * @title  Superclass' method call
     */
    public void testN1() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_1 t = new T_invokespecial_1();
        assertEquals(5, t.run());
    }

    /**
     * @title  private method call
     */
    public void testN2() {
        T_invokespecial_2 t = new T_invokespecial_2();
        assertEquals(345, t.run());
    }

    /**
     * @title  Invoke method of superclass of superclass
     */
    public void testN3() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper2
        T_invokespecial_15 t = new T_invokespecial_15();
        assertEquals(5, t.run());
    }

    /**
     * @title  Invoke protected method of superclass specifying "this"
     * class
     */
    public void testN4() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            T_invokespecial_17 t = new T_invokespecial_17();
            assertEquals(5, t.run());
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Invoke protected method of superclass if method with the
     * same name exists in "this" class
     */
    public void testN5() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_18 t = new T_invokespecial_18();
        assertEquals(5, t.run());
    }

    /**
     * @title  Check that method's arguments are popped from stack
     */
    public void testN6() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_19 t = new T_invokespecial_19();
        assertEquals(2, t.run());
    }

    /**
     * @title  Check that new frame is created by invokespecial
     */
    public void testN7() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_21 t = new T_invokespecial_21();
        assertEquals(1, t.run());
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN8() {
        assertTrue(T_invokespecial_22.execute());
    }



    /**
     * @title  method doesn't exist in "this" and superclass
     */
    public void testE1() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            T_invokespecial_7 t = new T_invokespecial_7();
            assertEquals(5, t.run());
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
            // expected
        } catch (VerifyError vfe) {
            // ok for dalvikvm; early resolution
            System.out.print("dvmvfe:");
        }
    }

    /**
     * @title  method has different signature
     */
    public void testE2() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            T_invokespecial_16 t = new T_invokespecial_16();
            assertEquals(5, t.run());
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
            // expected
        } catch (VerifyError vfe) {
            // ok for dalvikvm; early resolution
            System.out.print("dvmvfe:");
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE3() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_8 t = new T_invokespecial_8();
        try {
            assertEquals(5, t.run());
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @title  Attempt to invoke static method
     */
    public void testE4() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            T_invokespecial_11 t = new T_invokespecial_11();
            assertEquals(5, t.run());
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
            // expected
        } catch (VerifyError vfe) {
            // ok for dalvikvm;
            System.out.print("dvmvfe:");
        }
    }

    /**
     * @title  Native method can't be linked
     */
    public void testE5() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        T_invokespecial_9 t = new T_invokespecial_9();
        try {
            assertEquals(5, t.run());
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            // expected
        }
    }

    /**
     * @title  Attempt to invoke private method of superclass
     */
    public void testE6() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            T_invokespecial_12 t = new T_invokespecial_12();
            assertEquals(5, t.run());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) {
            // ok for dalvikvm;
            System.out.print("dvmvfe:");
        }
    }

    /**
     * @title  Attempt to invoke abstract method
     */
    public void testE7() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TAbstract
        T_invokespecial_13 t = new T_invokespecial_13();
        try {
            assertEquals(5, t.run());
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
            // expected
        }
    }

    /**
     * @constraint 4.8.1.13 
     * @title valid index into constant pool table
     */
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_3");
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
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.14 
     * @title only &lt;init&gt; may be called
     */
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.7 
     * @title invokespecial target must be in self or superclass
     */
    public void testVFE4() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TPlain
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1 
     * @title number of arguments
     */
    public void testVFE5() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1 
     * @title type of argument - int
     */
    public void testVFE6() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.7 
     * @title invokespecial must name method of "this" class or
     *                 superclass
     */
    public void testVFE7() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.7 
     * @title number of arguments passed to method
     */
    public void testVFE8() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.12 
     * @title types of arguments passed to method
     */
    public void testVFE9() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_24");
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
    public void testVFE10() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        //@uses dxc.junit.opcodes.invokespecial.jm.TPlain
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_25");
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
    public void testVFE11() {
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper
        //@uses dxc.junit.opcodes.invokespecial.jm.TSuper2
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_26");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
