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

/**
 * Constant value for CTS command
 */
public interface CTSCommand {
    // Define the commands
    static final String EXIT = "exit";
    static final String HELP = "help";
    static final String ADD = "add";
    static final String REMOVE = "rm";
    static final String START = "start";
    static final String LIST = "ls";
    static final String H = "h";
    static final String HISTORY = "history";

    // Define command options
    static final String OPTION_D = "-d";
    static final String OPTION_DEVICE = "--device";
    static final String OPTION_P = "-p";
    static final String OPTION_PACKAGE = "--package";
    static final String OPTION_R = "-r";
    static final String OPTION_RESULT = "--result";
    static final String OPTION_PLAN = "--plan";
    static final String OPTION_T = "-t";
    static final String OPTION_TEST = "--test";
    static final String OPTION_E = "-e";
    static final String OPTION_S = "-s";
    static final String OPTION_SESSION = "--session";
    static final String OPTION_CFG = "--config";
    static final String OPTION_DERIVED_PLAN = "--derivedplan";
}
