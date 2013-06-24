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

package dxc.junit.opcodes.opc_return;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.opc_return.jm.T_opc_return_1;
import dxc.junit.opcodes.opc_return.jm.T_opc_return_2;
import dxc.junit.opcodes.opc_return.jm.T_opc_return_3;
import dxc.junit.opcodes.opc_return.jm.T_opc_return_4;

public class Test_opc_return extends DxTestCase {

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN1() {
        T_opc_return_1 t = new T_opc_return_1();
        assertEquals(123456, t.run());
    }

    /**
     * @title  check that monitor is released by return
     */
    public void testN2() {
        assertTrue(T_opc_return_2.execute());
    }


    /**
     * @title  Method is synchronized but thread is not monitor owner
     */
    public void testE1() {
        T_opc_return_3 t = new T_opc_return_3();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @title  Lock structural rule 1 is violated
     */
    public void testE2() {
        T_opc_return_4 t = new T_opc_return_4();
        try {
            t.run();
            // the JVM spec says that it is optional to implement the structural
            // lock rules, see JVM spec 8.13 and monitorenter/exit opcodes.
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title method's return type - int
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.opc_return.jm.T_opc_return_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title method's return type - reference
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_return.jm.T_opc_return_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
