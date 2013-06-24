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

import java.util.ArrayList;
import java.util.List;

/**
 * CommandHistory holds max 50 executed command.
 * User can view what has been input and select one to execute again.
 * Example:
 * <ul>
 *    <li> "cts_host > h"  to view latest 50 executed command.
 *    <li> "cts_host > h 10" to view latest 10 executed command.
 *    <li> "cts_host > h -e 5" to execute the 5th executed command.
 * </ul>
 */
public class CommandHistory {
    private static final int CMD_RECORD_DEPTH = 50;

    private List<String> mCmdRecords;

    public CommandHistory() {
        mCmdRecords = new ArrayList<String>();
    }

    /**
     * Check if the command is history command.
     *
     * @param cmd The command string.
     * @return If it's history command, return true; else return false.
     */
    public boolean isHistoryCommand(final String cmd) {
        return CTSCommand.HISTORY.equals(cmd) || CTSCommand.H.equals(cmd);
    }

    /**
     * Get the number of commands recorded.
     *
     * @return The number of commands recorded.
     */
    public int size() {
        return mCmdRecords.size();
    }

    /**
     * Get command by index from command history cache.
     *
     * @param index The command index.
     * @return The command corresponding to the command index.
     */
    public String get(final int index) {
        return mCmdRecords.get(index);
    }

    /**
     * display specified number of commands from command history cache onto CTS console.
     *
     * @param cmdCount The command count requested.
     */
    public void show(final int cmdCount) {
        int cmdSize = mCmdRecords.size();
        int start = 0;

        if (cmdSize == 0) {
            CUIOutputStream.println("no history command list");
            return;
        }
        if (cmdCount < cmdSize) {
            start = cmdSize - cmdCount;
        }

        for (; start < cmdSize; start ++) {
            String cmdLine = mCmdRecords.get(start);
            CUIOutputStream.println("  " + Long.toString(start) + "\t" + cmdLine);
        }
    }

    /**
     * Add a command to the command cache.
     *
     * @param cp The command container.
     * @param cmdLine The command line.
     */
    public void addCommand(final CommandParser cp,
            final String cmdLine) {
        if ((cmdLine == null) || (cmdLine.length() == 0)) {
            return;
        }

        if (isValidCommand(cp.getAction()) && (!hasCommand(cmdLine))) {
            mCmdRecords.add(cmdLine);
            if (mCmdRecords.size() > CMD_RECORD_DEPTH) {
                mCmdRecords.remove(0);
            }
        }
    }

    /**
     * Check if the command contains valid action.
     *
     * @param action The action contained in the command.
     * @return If valid, return true; else, return false.
     */
    private boolean isValidCommand(final String action) {
        if (!(CTSCommand.HISTORY.equals(action) || CTSCommand.H.equals(action))) {
            if (CTSCommand.ADD.equals(action)
                    || CTSCommand.EXIT.equals(action)
                    || CTSCommand.HELP.equals(action)
                    || CTSCommand.LIST.equals(action)
                    || CTSCommand.REMOVE.equals(action)
                    || CTSCommand.START.equals(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the command is a duplicate one.
     *
     * @param cmdLine The command to be checked against the commands recorded.
     * @return If duplicated, return true; else, return false.
     */
    private boolean hasCommand(final String cmdLine) {
        for(String cmd : mCmdRecords) {
            if (cmd.equals(cmdLine)) {
                return true;
            }
        }
        return false;
    }
}
