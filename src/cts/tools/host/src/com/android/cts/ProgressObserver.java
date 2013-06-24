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

package com.android.cts;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Observes test progressing status.
 *
 */
public class ProgressObserver {
    private Timer mNotifyTimer;

    /**
     * Start a process displayer.
     */
    public void start() {
        mNotifyTimer = new Timer();
        mNotifyTimer.schedule(new ProgressPrinter(),
                ProgressPrinter.DELAY, ProgressPrinter.TIMEOUT);
    }

    /**
     * Stop a process displayer.
     */
    public void stop() {
        if (mNotifyTimer != null) {
            mNotifyTimer.cancel();
        }
        mNotifyTimer = null;
    }

    /**
     * Display running notification when a test/package is executing, </br>
     * especially for the ones running for a very long time.
     */
    class ProgressPrinter extends TimerTask {
        public final static int DELAY = 2000;
        public final static int TIMEOUT = 2000;

        /** {@inheritDoc} */
        @Override
        public void run() {
            CUIOutputStream.print(".");
        }
    }
}
