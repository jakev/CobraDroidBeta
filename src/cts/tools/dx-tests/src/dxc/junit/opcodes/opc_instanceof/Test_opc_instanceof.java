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

package dxc.junit.opcodes.opc_instanceof;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_1;
import dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_2;
import dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_3;
import dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_7;

public class Test_opc_instanceof extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertTrue(t.run(""));
    }

    /**
     * @title check null value
     */
    public void testN2() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertFalse(t.run(null));
    }

    /**
     * @title normal test
     */
    public void testN3() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertFalse(t.run(this));
    }

    /**
     * @title normal test
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.opc_instanceof.jm.SubClass
        // @uses dxc.junit.opcodes.opc_instanceof.jm.SuperClass
        // @uses dxc.junit.opcodes.opc_instanceof.jm.SuperInterface
        // @uses dxc.junit.opcodes.opc_instanceof.jm.SuperInterface2
        T_opc_instanceof_2 t = new T_opc_instanceof_2();
        assertEquals(0, t.run());
    }

    /**
     * @title expected IllegalAccessError
     */
    public void testE1() {
        // @uses dxc.junit.opcodes.opc_instanceof.jm.TestStubs$TestStub
        T_opc_instanceof_3 t = new T_opc_instanceof_3();
        try {
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
     * @title expected NoClassDefFoundError
     */
    public void testE2() {
        T_opc_instanceof_7 t = new T_opc_instanceof_7();
        try {
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
     * @constraint 4.8.1.16
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title type of argument - float
     */
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_6");
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
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
