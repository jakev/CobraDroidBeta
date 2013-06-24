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

package dxc.junit.opcodes.checkcast;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.checkcast.jm.T_checkcast_1;
import dxc.junit.opcodes.checkcast.jm.T_checkcast_2;
import dxc.junit.opcodes.checkcast.jm.T_checkcast_3;
import dxc.junit.opcodes.checkcast.jm.T_checkcast_7;

public class Test_checkcast extends DxTestCase {
    
    /**
     * @title normal test
     */
    public void testN1() {
        T_checkcast_1 t = new T_checkcast_1();
        String s = "";
        assertEquals(s, t.run(s));
    }

    /**
     * @title check null value
     */
    public void testN2() {
        T_checkcast_1 t = new T_checkcast_1();
        assertNull(t.run(null));
    }

    /**
     * @title normal class
     */
    public void testN4() {
        // @uses dxc.junit.opcodes.checkcast.jm.SubClass
        // @uses dxc.junit.opcodes.checkcast.jm.SuperClass
        // @uses dxc.junit.opcodes.checkcast.jm.SuperInterface
        // @uses dxc.junit.opcodes.checkcast.jm.SuperInterface2
        
        T_checkcast_2 t = new T_checkcast_2();
        assertEquals(5, t.run());
    }

    /**
     * @title expected ClassCastException
     */
    public void testE1() {
        T_checkcast_1 t = new T_checkcast_1();
        try {
            t.run(this);
            fail("expected ClassCastException");
        } catch (ClassCastException iae) {
            // expected
        }
    }

    /**
     * @title expected ClassCastException. checkcast [[[Ldxc/junit/opcodes/checkcast/jm/TestStubs$TestStub;
     * dalvikvm throws ClassCastException as demanded by checkcast, but
     * does not claim a IllegalAccessError to a private class - ok, 
     * since checkcast does what the spec says it should do.
     */
    public void testE2() {
        // @uses dxc.junit.opcodes.checkcast.jm.TestStubs$TestStub
        try {
            T_checkcast_3 t = new T_checkcast_3();
            t.run();
            fail("expected ClassCastException");
        } catch (ClassCastException cce) {
            // expected
        }
    }

    /**
     * @title expected NoClassDefFoundError
     */
    public void testE3() {
        try {
            T_checkcast_7 t = new T_checkcast_7();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError iae) {
            // expected
        } catch (VerifyError vfe) {
            // ok for dalvikvm; early resolution
            System.out.print("dvmvfe:");
        }
    }

    /**
     * @constraint 4.8.1.16
     * @title constant pool index
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_4");
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
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_5");
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
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_6");
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
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
