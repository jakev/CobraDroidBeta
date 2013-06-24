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

package dxc.junit.opcodes.areturn;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.areturn.jm.T_areturn_1;
import dxc.junit.opcodes.areturn.jm.T_areturn_12;
import dxc.junit.opcodes.areturn.jm.T_areturn_13;
import dxc.junit.opcodes.areturn.jm.T_areturn_2;
import dxc.junit.opcodes.areturn.jm.T_areturn_6;
import dxc.junit.opcodes.areturn.jm.T_areturn_7;
import dxc.junit.opcodes.areturn.jm.T_areturn_8;
import dxc.junit.opcodes.areturn.jm.T_areturn_9;

public class Test_areturn extends DxTestCase {

    /**
     * @title  simple
     */
    public void testN1() {
        T_areturn_1 t = new T_areturn_1();
        assertEquals("hello", t.run());
    }

    /**
     * @title  simple
     */
    public void testN2() {
        T_areturn_1 t = new T_areturn_1();
        assertEquals(t, t.run2());
    }

    /**
     * @title  simple
     */
    public void testN3() {
        T_areturn_1 t = new T_areturn_1();
        Integer a = 12345;
        assertEquals(a, t.run3());
    }

    /**
     * @title test for null
     */
    public void testN4() {
        T_areturn_2 t = new T_areturn_2();
        assertNull(t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN5() {
        T_areturn_6 t = new T_areturn_6();
        assertEquals("hello", t.run());
    }

    /**
     * @title  check that monitor is released by areturn
     */
    public void testN6() {
        assertTrue(T_areturn_7.execute());
    }

    /**
     * @title  assignment compatibility (TChild returned as TSuper)
     */
    public void testN7() {
        // @uses dxc.junit.opcodes.areturn.jm.TSuper
        // @uses dxc.junit.opcodes.areturn.jm.TInterface
        // @uses dxc.junit.opcodes.areturn.jm.TChild
        T_areturn_12 t = new T_areturn_12();
        assertTrue(t.run());
    }

    /**
     * @title  assignment compatibility (TChild returned as TInterface)
     */
    public void testN8() {
        // @uses dxc.junit.opcodes.areturn.jm.TInterface
        // @uses dxc.junit.opcodes.areturn.jm.TChild
        // @uses dxc.junit.opcodes.areturn.jm.TSuper
        T_areturn_13 t = new T_areturn_13();
        assertTrue(t.run());
    }

    /**
     * @title  Method is synchronized but thread is not monitor owner
     */
    public void testE1() {
        T_areturn_8 t = new T_areturn_8();
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
        T_areturn_9 t = new T_areturn_9();
        try {
            assertEquals("abc", t.run());
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
     * @title method's return type - void
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title method's return type - float
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_4");
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
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - float
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.5
     * @title stack size
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title assignment incompatible references
     */
    public void testVFE6() {
        // @uses dxc.junit.opcodes.areturn.jm.TInterface
        // @uses dxc.junit.opcodes.areturn.jm.TSuper
        // @uses dxc.junit.opcodes.areturn.jm.TChild
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title assignment incompatible references
     */
    @SuppressWarnings("cast")
    public void testVFE7() {
        // @uses dxc.junit.opcodes.areturn.jm.TSuper2
        // @uses dxc.junit.opcodes.areturn.Runner
        // @uses dxc.junit.opcodes.areturn.RunnerGenerator
        try {
            RunnerGenerator rg = (RunnerGenerator) Class.forName(
                    "dxc.junit.opcodes.areturn.jm.T_areturn_15").newInstance();
            Runner r = rg.run();
            assertFalse(r instanceof Runner);
            assertFalse(Runner.class.isAssignableFrom(r.getClass()));
            // only upon invocation of a concrete method,
            // a java.lang.IncompatibleClassChangeError is thrown
            r.doit();
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
