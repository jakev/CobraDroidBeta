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

package dxc.junit.opcodes.monitorexit;

import dxc.junit.DxTestCase;
import dxc.junit.DxUtil;
import dxc.junit.opcodes.monitorexit.jm.T_monitorexit_2;
import dxc.junit.opcodes.monitorexit.jm.T_monitorexit_3;
import dxc.junit.opcodes.monitorexit.jm.T_monitorexit_4;

public class Test_monitorexit extends DxTestCase {

    /**
     * @title  thread is not monitor owner
     */
    public void testE1() throws InterruptedException {
        //@uses dxc.junit.opcodes.monitorexit.TestRunnable
        final T_monitorexit_2 t = new T_monitorexit_2();
        final Object o = new Object();

        Runnable r = new TestRunnable(t, o);

        synchronized (o) {
            Thread th = new Thread(r);
            th.start();
            th.join();
        }
        if (t.result == false) {
            fail("expected IllegalMonitorStateException");
        }
    }


    /**
     * @title  structural lock rules violation
     */
    public void testE2() {
        T_monitorexit_3 t = new T_monitorexit_3();
        try {
            t.run();
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE3() {
        T_monitorexit_4 t = new T_monitorexit_4();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title number of arguments
     */
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.monitorexit.jm.T_monitorexit_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - int
     */
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.monitorexit.jm.T_monitorexit_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }

}


class TestRunnable implements Runnable {
    private T_monitorexit_2 t;
    private Object o;

    public TestRunnable(T_monitorexit_2 t, Object o) {
        this.t = t;
        this.o = o;
    }

    public void run() {
        try {
            t.run(o);
        } catch (IllegalMonitorStateException imse) {
            // expected
            t.result = true;
        }
    }
}
