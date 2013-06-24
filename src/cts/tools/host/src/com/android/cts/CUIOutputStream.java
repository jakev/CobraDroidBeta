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

import java.io.PrintStream;

/**
 * CTS UI output stream. Handle all output of CTS UI
 */
final class CUIOutputStream {

    private static PrintStream sOutput = System.out;
    public static final String CTS_PROMPT_SIGN = "cts_host > ";

    /**
     * Print a line of message onto the CTS host console.
     *
     * @param msg The message to be print.
     */
    static public void print(final String msg) {
        sOutput.print(msg);

        Log.log(msg);
    }

    /**
     * Print a line of message onto the CTS host console with a carriage return.
     *
     * @param msg The message to be print.
     */
    static public void println(final String msg) {
        sOutput.println(msg);

        Log.log(msg);
    }

    /**
     * Write the buffer with given offset and length.
     *
     * @param buf The buffer.
     * @param off The offset to start writing.
     * @param len The length in byte to write.
     */
    static public void write(byte[] buf, int off, int len) {
        sOutput.write(buf, off, len);
    }

    /**
     * Write a byte.
     *
     * @param c The byte to write.
     */
    static public void write(int c) {
        sOutput.write(c);
    }

    /**
     * Flush the write buffer.
     */
    static public void flush() {
        sOutput.flush();
    }

    /**
     * Print prompt.
     */
    static public void printPrompt() {
        print(CTS_PROMPT_SIGN);

        Log.log(CTS_PROMPT_SIGN);
    }
}
