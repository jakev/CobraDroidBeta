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

package dot.junit.opcodes.xor_long;

import dot.junit.DxTestCase;
import dot.junit.DxUtil;
import dot.junit.opcodes.xor_long.d.T_xor_long_1;
import dot.junit.opcodes.xor_long.d.T_xor_long_2;

public class Test_xor_long extends DxTestCase {
    /**
     * @title Arguments = 23423432423777l, 23423432423778l
     */
    public void testN1() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(3, t.run(23423432423777l, 23423432423778l));
    }

    /**
     * @title Arguments = 0xfffffff5, 0xfffffff1
     */
    public void testN2() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(4, t.run(0xfffffff5, 0xfffffff1));
    }

    /**
     * @title Arguments = 0xABCDEFAB & -1
     */
    public void testN3() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0x54321054, t.run(0xABCDEFAB, -1l));
    }
    
    /**
     * @title Types of arguments - double, long. Dalvik doens't distinguish 64-bits types internally,
     * so this (double ^ long ) makes no sense but shall not crash the VM.  
     */
    public void testN4() {
        T_xor_long_2 t = new T_xor_long_2();
        try {
            t.run(3.14d, 1234l);
        } catch (Throwable e) {
        }
    } 

    /**
     * @title Arguments = 0 & -1
     */
    public void testB1() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(-1l, t.run(0l, -1l));
    }

    /**
     * @title Arguments = Long.MAX_VALUE & Long.MIN_VALUE
     */
    public void testB2() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0xffffffff, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title Arguments = Long.MAX_VALUE & Long.MAX_VALUE
     */
    public void testB3() {
        T_xor_long_1 t = new T_xor_long_1();
        assertEquals(0l, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    /**
     * @constraint A24 
     * @title number of registers
     */
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.xor_long.d.T_xor_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    
    

    
    /**
     * @constraint B1 
     * @title types of arguments - int, long
     */
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.xor_long.d.T_xor_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * 
     * @constraint B1 
     * @title types of arguments - float, long
     */
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.xor_long.d.T_xor_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * 
     * @constraint B1 
     * @title types of arguments - reference, long
     */
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.xor_long.d.T_xor_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
