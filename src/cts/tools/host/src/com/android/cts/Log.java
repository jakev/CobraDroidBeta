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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Utility class to help the CTS logging
 */
public class Log {
    private static final String INFO_PREFIX = "\nCTS_INFO >>> ";
    private static final String ERROR_PREFIX = "\nCTS_ERROR >>> ";
    private static final String DEBUG_PREFIX = "\nCTS_DEBUG >>> ";
    private static final String LOG_FNAME_PREFIX = "log_";
    private static final String LOG_FNAME_SURFIX = "_.txt";

    private static PrintStream mOut = System.err;

    private static boolean TRACE = true;
    private static BufferedWriter mTraceOutput = null;

    private static boolean LOG = true;
    private static BufferedWriter mLogOutput = null;
    private static String mLogFileName;

    /**
     * Print the message to the information stream without adding prefix.
     *
     * @param msg The message to be printed.
     */
    public static void println(final String msg) {
        log(INFO_PREFIX + msg);
        mOut.println(msg);
    }

    /**
     * Add the message to the information stream.
     *
     * @param msg the message to be added to the information stream.
     */
    public static void i(final String msg) {
        log(INFO_PREFIX + msg);

        mOut.println(INFO_PREFIX + msg);
    }

    /**
     * Add the message to the error message stream.
     * @param msg The message to be added to the error message stream.
     * @param e The exception.
     */
    public static void e(final String msg, Exception e) {
        log(ERROR_PREFIX + msg);

        if (!HostConfig.DEBUG) {
            CUIOutputStream.println(ERROR_PREFIX + msg);
            if (e != null) {
                CUIOutputStream.println(e.toString());
            }
            return;
        }

        mOut.println(ERROR_PREFIX + msg);
        if (e != null) {
            e.printStackTrace();
        }
    }

    /**
     * Add the message to the debugging stream.
     *
     * @param msg The message to be added to the debugging stream.
     */
    public static void d(final String msg) {
        log(DEBUG_PREFIX + System.currentTimeMillis() + " " + msg);

        if (HostConfig.DEBUG) {
            mOut.println(DEBUG_PREFIX + msg);
        }
    }

    /**
     * Set the output stream.
     *
     * @param out The output stream.
     */
    public static void setOutput(PrintStream out) {
        if (out != null) {
            mOut = out;
        }
    }

    /**
     * Reset the output stream.
     */
    public static void resetOutput() {
        mOut = System.out;
    }

    /**
     * Initialize the log stream.
     *
     * @param path The path to add the log file.
     */
    public static void initLog(String path) {
        mLogFileName = path + File.separator + LOG_FNAME_PREFIX
            + HostUtils.getFormattedTimeString(System.currentTimeMillis(), "_", ".", ".")
            + LOG_FNAME_SURFIX;
        try {
            if (mLogOutput == null) {
                mLogOutput = new BufferedWriter(new FileWriter(mLogFileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the log stream.
     */
    public static void closeLog() {
        if (mLogOutput != null) {
            try {
                mLogOutput.close();
                mLogOutput = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log the message.
     *
     * @param msg The message to be logged.
     */
    public static void log(String msg) {
        if (LOG && (mLogOutput != null)) {
            try {
                if (msg != null) {
                    mLogOutput.write(msg + "\n");
                    mLogOutput.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add the message to the trace stream.
     *
     * @param msg The message to be added to the trace stream.
     */
    public static void t(String msg) {
        if (TRACE) {
            try {
                if (mTraceOutput == null) {
                    mTraceOutput = new BufferedWriter(new FileWriter("debug.txt"));
                }
                if (msg != null) {
                    mTraceOutput.write(msg + "\n");
                    mTraceOutput.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the trace stream.
     */
    public static void closeTrace() {
        if (mTraceOutput != null) {
            try {
                mTraceOutput.close();
                mTraceOutput = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
