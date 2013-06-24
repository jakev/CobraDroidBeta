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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandParser is responsible for parsing command line arguments. To get
 * action, option or values easy via functions.
 * For example:
 * <ul>
 *    <li> CommandParser cp = CommandParser.parse("start -plan test_plan")
 *    <li> cp.getAction() will get "start"
 *    <li> cp.getValue("-plan") will get "test_plan"
 *    <li> cp.containsKey("-noplan") will get null
 * </ul>
 *
 */
public class CommandParser {

    /**
     * The hash map mapping the options and option values.
     */
    private HashMap<String, String> mValues = new HashMap<String, String>();

    /**
     * The action the user chose to ask CTS host to take.
     */
    private String mAction;
    private ArrayList<String> mActionValues = new ArrayList<String>();
    private int mArgLength;
    private static final String COMMAND_PARSE_EXPRESSION = "(((\\\\\\s)|[\\S&&[^\"]])+|\".+\")";

    private static Set<String> sOptionsSet = new HashSet<String>(Arrays.asList(
            CTSCommand.OPTION_CFG, CTSCommand.OPTION_PACKAGE, CTSCommand.OPTION_PLAN,
            CTSCommand.OPTION_DEVICE, CTSCommand.OPTION_RESULT, CTSCommand.OPTION_E,
            CTSCommand.OPTION_SESSION, CTSCommand.OPTION_TEST, CTSCommand.OPTION_DERIVED_PLAN));
    private static HashMap<String, String> sOptionMap = new HashMap<String, String>();
    static {
        final String[] keys = new String[] {
                CTSCommand.OPTION_CFG,
                CTSCommand.OPTION_P,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PLAN,
                CTSCommand.OPTION_D,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_R,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_E,
                CTSCommand.OPTION_S,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_T,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_DERIVED_PLAN};

        final String[] values = new String[] {
                CTSCommand.OPTION_CFG,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PLAN,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_E,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_DERIVED_PLAN};

        for (int i = 0; i < keys.length; i++) {
            sOptionMap.put(keys[i], values[i]);
        }
    }

    /**
     * Parse the command line into array of argument.
     *
     * @param line The original command line.
     * @return The command container.
     */
    public static CommandParser parse(final String line)
            throws UnknownCommandException, CommandNotFoundException {
        ArrayList<String> arglist = new ArrayList<String>();

        Pattern p = Pattern.compile(COMMAND_PARSE_EXPRESSION);
        Matcher m = p.matcher(line);
        while (m.find()) {
            arglist.add(m.group(1));
        }
        CommandParser cp = new CommandParser();
        if (arglist.size() == 0) {
            throw new CommandNotFoundException("No command");
        }
        cp.parse(arglist);
        return cp;
    }

    /**
     * Parse the argument list.
     *
     * @param arglist The argument list.
     */
    private void parse(ArrayList<String> arglist)
            throws UnknownCommandException {
        mArgLength = arglist.size();
        int currentArgIndex = 0;
        mAction = arglist.get(currentArgIndex).toLowerCase();
        String originalOption = null;
        String option = null;

        // parse action values
        while (++currentArgIndex < arglist.size()) {
            originalOption = arglist.get(currentArgIndex).trim();
            if (originalOption.startsWith("-")) {
                if (isNumber(originalOption)) {
                    mActionValues.add(originalOption);
                } else {
                    --currentArgIndex;
                    break;
                }
            } else {
                mActionValues.add(originalOption);
            }
        }

        // parse option
        while (++currentArgIndex < arglist.size()) {
            originalOption = arglist.get(currentArgIndex).trim().toLowerCase();
            option = originalOption;
            if (!option.startsWith("-")) {
                throw new UnknownCommandException(
                        "Option should start with '-'");
            }

            option = inputToOption(option);
            if (!sOptionsSet.contains(option)) {
                throw new UnknownCommandException("Unknown option :"
                        + originalOption);
            }

            if (mValues.containsKey(option)) {
                throw new UnknownCommandException("Duplicate option: "
                         + originalOption);
            }

            if (currentArgIndex + 1 == arglist.size()) {
                mValues.put(option, "");
                continue;
            }

            String value = arglist.get(++currentArgIndex).trim();
            if (value.startsWith("-")) {
                if (!isNumber(value)) {
                    value = "";
                    currentArgIndex--;
                }
            }

            mValues.put(option, value);
        }
    }

    /**
     * Translate the input to option.
     *
     * @param option The option typed in.
     * @return The option found.
     */
    private String inputToOption(String option) throws UnknownCommandException {
        String op = sOptionMap.get(option);
        if (op == null) {
            throw new UnknownCommandException("Unknow option " + option);
        }

        return op;
    }

    /**
     * Check if the option is a number.
     *
     * @param option The option.
     * @return If the option is a number, return true; else, return false.
     */
    private boolean isNumber(String option) {
        try {
            Integer.parseInt(option);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the arguments size.
     *
     * @return The argument size.
     */
    public int getArgSize() {
        return mArgLength;
    }

    /**
     * Get the action.
     *
     * @return The action.
     */
    public String getAction() {
        return mAction;
    }

    /**
     * Get the option size.
     *
     * @return The option size.
     */
    public int getOptionSize() {
        return mValues.size();
    }

    /**
     * Get command option by hash key from parsed argument list.
     *
     * @param key The key.
     * @return The value according to the key.
     */
    public String getValue(String key) {
        if (mValues.containsKey(key)) {
            return mValues.get(key);
        } else {
            return null;
        }
    }

    /**
     * Check if option list contains the key.
     *
     * @param key The key.
     * @return If containing the key, return true; else, return false.
     */
    public boolean containsKey(String key) {
        return mValues.containsKey(key);
    }

    /**
     * Get all of the option keys.
     *
     * @return All of the option keys.
     */
    public Set<String> getOptionKeys() {
        return mValues.keySet();
    }

    /**
     * Get action list.
     *
     * @return The action list.
     */
    public ArrayList<String> getActionValues() {
        return mActionValues;
    }

    /**
     * Remove a specific key.
     *
     * @param key The key to be removed.
     */
    public void removeKey(String key) {
        mValues.remove(key);
    }

}
