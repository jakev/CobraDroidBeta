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

package dxc.junit.opcodes.aconst_null;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.aconst_null.jm.T_aconst_null_1;

public class Test_aconst_null extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        assertEquals(null, new T_aconst_null_1().run());
    }

    /**
     * @constraint 4.8.2.5
     * @title stack size
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aconst_null.jm.T_aconst_null_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
