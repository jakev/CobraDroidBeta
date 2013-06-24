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

package dxc.junit.opcodes.lstore;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.lstore.jm.T_lstore_1;
import dxc.junit.opcodes.lstore.jm.T_lstore_1_w;
import dxc.junit.opcodes.lstore.jm.T_lstore_2;
import dxc.junit.opcodes.lstore.jm.T_lstore_2_w;

public class Test_lstore extends DxTestCase {

    /*
     * NORMAL ISTORE VERSION
     */

    /**
     * @title  lstore 0
     */
    public void testN1() {
        assertEquals(1234567890123l, T_lstore_1.run());
    }

    /**
     * @title  lstore 255
     */
    public void testN2() {
        assertEquals(1234567890123l, T_lstore_2.run());
    }

    /**
     * @constraint 4.8.1.22
     * @title index must be no greater than the value
     * of max_locals-1
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - double
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - int
     */
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_5");
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
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /*
     * WIDE ISTORE VERSION
     */

    /**
     * @title  lstore_w 0
     */
    public void testN3() {
        assertEquals(1234567890123l, T_lstore_1_w.run());
    }

    /**
     * @title  lstore 257
     */
    public void testN4() {
        assertEquals(1234567890123l, T_lstore_2_w.run());
    }

    /**
     * @constraint 4.8.1.25
     * @title index must be no greater than the value
     * of max_locals-1
     */
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - double
     */
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - int
     */
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of argument - float
     */
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}
