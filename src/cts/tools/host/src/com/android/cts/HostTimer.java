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
 * Host timer.
 * Generally, there are two use cases of this general host timer:
 * <ul>
 *    <li> Use it as general timer to guard host from running for
 *         too long under some situations.
 *    <li> Use it as special timer where host needs to run very
 *         long to communicate with device to fetch result section
 *         by section which requires restarting the timer.
 * </ul>
 */
public class HostTimer {
    private final static int INIT = 0;
    private final static int RUNNING = 1;
    private final static int CANCELLED = 2;
    private final static int TIMEOUT = 3;

    private boolean mIsNotified;
    private int mStatus;
    private int mDelay;
    private TimerTask mTimerTask;
    private Timer mTimer;

    public HostTimer(TimerTask task, int delay) {
        mDelay = delay;
        mTimerTask = task;
        mStatus = INIT;
        mIsNotified = false;
        mTimer = null;
    }

    /**
     * Mark notified.
     */
    public void setNotified() {
        mIsNotified = true;
    }

    /**
     * Get the notification status.
     *
     * @return The notification status.
     */
    public boolean isNotified() {
        return mIsNotified;
    }

    /**
     * Clear the status of notification.
     */
    public void resetNotified() {
        mIsNotified = false;
    }

    /**
     * Wait on.
     */
    public void waitOn() throws InterruptedException {
        Log.d("HostTimer.waitOn(): mIsNotified=" + mIsNotified + ", this=" + this);
        if (!mIsNotified) {
            wait();
        }
        mIsNotified = false;
    }

    /**
     * Set the time to delay.
     *
     * @param delay The time to delay.
     */
    public void setDelay(int delay) {
        mDelay = delay;
    }

    /**
     * Set the timer task.
     *
     * @param task The timer task.
     */
    public void setTimerTask(TimerTask task) {
        mTimerTask = task;
    }

    /**
     * Check if the watch dog timer timed out.
     *
     * @return If timeout, return true; else return false.
     */
    public boolean isTimeOut() {
        return (mStatus == TIMEOUT);
    }

    /**
     * Start the watch dog timer.
     */
    public void start() {
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, mDelay);
        mStatus = RUNNING;
    }

    /**
     * Restart the watch dog timer.
     */
    public void restart(TimerTask task, int delay) {
        mTimer.cancel();
        mTimerTask = task;
        mDelay = delay;
        start();
    }

    /**
     * Send notify to thread waiting on this object.
     */
    public void sendNotify() {
        Log.d("HostTimer.sendNotify(): mIsNotified=" + mIsNotified + ", this=" + this);
        mIsNotified = true;
        notify();
    }

    /**
     * Cancel the timer. To keep the status info, call this
     * cancel in stead of the one inherited from parent.
     *
     * @param timeout If true, the cancellation is caused by timer timing out;
     *                If false, the cancellation is no caused by timer timing out.
     */
    public void cancel(boolean timeout) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mStatus == RUNNING) {
            if (timeout) {
                mStatus = TIMEOUT;
            } else {
                mStatus = CANCELLED;
            }
        }
    }
}
