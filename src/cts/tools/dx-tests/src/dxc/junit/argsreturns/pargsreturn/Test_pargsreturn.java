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

package dxc.junit.argsreturns.pargsreturn;

import dxc.junit.DxTestCase;
import dxc.junit.argsreturns.pargsreturn.jm.T1;
import dxc.junit.argsreturns.pargsreturn.jm.T2;
import dxc.junit.argsreturns.pargsreturn.jm.T3;
import dxc.junit.argsreturns.pargsreturn.jm.T4;

public class Test_pargsreturn extends DxTestCase {

    /**
     * @title checks return value of a constant
     */
    public void testN1() {
        assertEquals(1234, new T1().run());
    }
    
    /**
     * @title checks whether the correct int value is returned.
     */
    public void testN2() {
        assertEquals(1234, new T2().run(1234));
    }
    
    /**
     * @title tests whether the correct int value is set.
     */
    public void testN3() {
        T3 t = new T3();
        t.run(1234);
        assertEquals(1234, t.i1);
    }

    /**
     * @title tests correct setting of field values
     */
    public void testN4() {
        T4 t = new T4();
        t.run(1234);
        assertEquals(50000000000l, t.j1);
        assertEquals(1234, t.i1);
    }

    
    

}
