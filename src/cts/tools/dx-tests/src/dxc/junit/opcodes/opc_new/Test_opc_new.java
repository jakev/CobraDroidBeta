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

package dxc.junit.opcodes.opc_new;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_1;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_3;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_4;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_5;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_8;
import dxc.junit.opcodes.opc_new.jm.T_opc_new_9;

public class Test_opc_new extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_opc_new_1 t = new T_opc_new_1();
        String s = t.run();
        assertNotNull(s);
        assertEquals(0, s.compareTo("abc"));
    }

    /**
     * @title expected Error (exception during class loading)
     */
    public void testE1() {
        try {
            T_opc_new_3.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

    /**
     * @title expected IllegalAccessError
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.opc_new.jm.TestStubs$TestStub
        try {
            T_opc_new_4 t = new T_opc_new_4();
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
            T_opc_new_5 t = new T_opc_new_5();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError iae) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.18 
     * @title attempt to instantiate interface
     */
    public void testE4() {
        // @uses dxc.junit.opcodes.opc_new.jm.TestInterface
        try {
            T_opc_new_8 t = new T_opc_new_8();
            t.run();
            fail("expected InstantiationError");
        } catch (InstantiationError ie) {
            // expected
        } catch (VerifyError vfe) { 
            // ok for dalvikvm; 
            System.out.print("dvmvfe:"); 
        }
    }

    /**
     * @constraint 4.8.1.18 
     * @title attempt to instantiate abstract
     * class
     */
    public void testE5() {
        // @uses dxc.junit.opcodes.opc_new.jm.TestAbstractClass
        T_opc_new_9 t = new T_opc_new_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error iae) {
            // expected
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.18
     * @title attempt to create array using new
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint n/a
     * @title  Attempt to access uninitialized class (before <init> is
     * called)
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title constant pool type
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
