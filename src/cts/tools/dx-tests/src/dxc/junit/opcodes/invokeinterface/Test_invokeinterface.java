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

package dxc.junit.opcodes.invokeinterface;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.invokeinterface.jm.ITestImpl;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_1;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_11;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_12;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_13;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_14;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_15;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_16;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_17;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_19;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_3;
import dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_7;

public class Test_invokeinterface extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_invokeinterface_1 t = new T_invokeinterface_1();
        assertEquals(0, t.run("aa", "aa"));
        assertEquals(-1, t.run("aa", "bb"));
        assertEquals(1, t.run("bb", "aa"));
    }

    /**
     * @title  Check that new frame is created by invokeinterface and
     * arguments are passed to method
     */
    public void testN2() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        T_invokeinterface_14 t = new T_invokeinterface_14();
        ITestImpl impl = new ITestImpl();
        assertEquals(1, t.run(impl));
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN3() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        assertTrue(T_invokeinterface_19.execute());
    }


    /**
     * @title  method doesn't exist
     */
    public void testE1() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        try {
            T_invokeinterface_7 t = new T_invokeinterface_7();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  method has different signature
     */
    public void testE2() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        try {
            T_invokeinterface_16 t = new T_invokeinterface_16();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
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
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest        
        try {
            new T_invokeinterface_3(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title  object doesn't implement interface
     */
    public void testE4() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        T_invokeinterface_11 t = new T_invokeinterface_11();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
            // expected
        }
    }

    /**
     * @title  Native method can't be linked
     */
    public void testE5() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        T_invokeinterface_12 t = new T_invokeinterface_12();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            // expected
        }
    }

    /**
     * @title  Attempt to invoke abstract method
     */
    public void testE6() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImplAbstract
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        try {
            T_invokeinterface_13 t = new T_invokeinterface_13();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to invoke static method
     */
    public void testE7() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImplAbstract
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        try {
            T_invokeinterface_15 t = new T_invokeinterface_15();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @title  Attempt to invoke non-public interface method
     */
    public void testE8() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImplAbstract
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImpl
        try {
            T_invokeinterface_17 t = new T_invokeinterface_17();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.15 
     * @title valid index into constant pool table
     */
    public void testVFE1() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.15 
     * @title invalid index into constant pool table
     */
    public void testVFE2() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_23");
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
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_5");
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
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest        
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.15 
     * @title args_size value must match number of arguments
     */
    public void testVFE7() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.15 
     * @title 4th operand must be zero
     */
    public void testVFE8() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1 
     * @title number of arguments passed to method
     */
    public void testVFE9() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14 
     * @title only invokespecial may be used to call <init>
     */
    public void testVFE10() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITestImplAbstract        
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14 
     * @title only invokespecial may be used to call <clinit>
     */
    public void testVFE11() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.12 
     * @title types of arguments passed to method
     */
    public void testVFE12() {
        //@uses dxc.junit.opcodes.invokeinterface.jm.ITest
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
