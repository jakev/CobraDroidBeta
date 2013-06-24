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

package dxc.junit.verify.t482_11;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.verify.t482_11.jm.T_t482_11_2;

/**
 * 
 */
public class Test_t482_11 extends DxTestCase {

    /**
     * @constraint 4.8.2.11
     * @title  instance fields declared in the class may be accessed before
     * calling <init>
     */
    public void testN1() {
        // @uses dxc.junit.verify.t482_11.jm.TSuper
        T_t482_11_2 t = new T_t482_11_2();
        assertEquals(11, t.v);
    }

    /**
     * @constraint 4.8.2.11
     * @title  super.<init> or another <init> must be called
     */
    public void testVFE1() {
        // @uses dxc.junit.verify.t482_11.jm.TSuper
        try {
            Class.forName("dxc.junit.verify.t482_11.jm.T_t482_11_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.11
     * @title only instance fields declared in the class may be accessed
     * before calling <init>
     */
    public void testVFE2() {
        // @uses dxc.junit.verify.t482_11.jm.TSuper
        try {
            Class.forName("dxc.junit.verify.t482_11.jm.T_t482_11_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
