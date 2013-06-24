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

package dxc.junit.opcodes.land;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.land.jm.T_land_1;

public class Test_land extends DxTestCase {

    /**
     * @title Arguments = 0xfffffff8aal, 0xfffffff1aal
     */
    public void testN1() {
        T_land_1 t = new T_land_1();
        assertEquals(0xfffffff0aal, t.run(0xfffffff8aal, 0xfffffff1aal));
    }

    /**
     * @title Arguments = 987654321, 123456789
     */
    public void testN2() {
        T_land_1 t = new T_land_1();
        assertEquals(39471121, t.run(987654321, 123456789));
    }

    /**
     * @title  Arguments = 0xABCDEF & -1
     */
    public void testN3() {
        T_land_1 t = new T_land_1();
        assertEquals(0xABCDEF, t.run(0xABCDEF, -1));
    }

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_land_1 t = new T_land_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE & Long.MIN_VALUE
     */
    public void testB2() {
        T_land_1 t = new T_land_1();
        assertEquals(0, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float & long
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int & long
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - reference & long
     */
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
