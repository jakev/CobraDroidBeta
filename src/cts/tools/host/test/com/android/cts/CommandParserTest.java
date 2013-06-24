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
import java.util.Set;

/**
 * Test the logic of parsing the command, option, and parameters.
 */
public class CommandParserTest extends CtsTestBase {

    /**
     * Test parsing the simple normal command.
     */
    public void testParseSimpleCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "start";
        final String option = "--plan";
        final String value = "test_plan";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option + " " + value);
        assertEquals(1, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertEquals(value, cp.getValue(CTSCommand.OPTION_PLAN));
    }

    /**
     * Test parsing command with multiple options.
     */
    public void testParseMultiOptionsCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "start";
        final String option1 = "--plan";
        final String value1 = "test_plan";
        final String option2 = "-d";
        final String value2 = "0";
        final String unexistOption = "unexist";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1 + " " + value1
                + " " + option2 + " " + value2);
        assertEquals(2, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_PLAN));
        assertEquals(value1, cp.getValue(CTSCommand.OPTION_PLAN));
        assertTrue(cp.containsKey(CTSCommand.OPTION_DEVICE));
        assertEquals(value2, cp.getValue(CTSCommand.OPTION_DEVICE));
        assertFalse(cp.containsKey(unexistOption));
    }

    /**
     * Test parsing command with multiple same options.
     */
    public void testParseSameOptionCommand() throws CommandNotFoundException{
        final String action = "ls";
        final String option1 = "-d";
        final String value1 = "test_plan";
        final String option2 = "-d";
        final String value2 = "0";
        try {
            CommandParser.parse(action + " " + option1 + " "
                    + value1 + " " + option2 + " " + value2);
            fail("no exception");
        } catch (UnknownCommandException e) {
        }
    }

    /**
     * Test parsing command with option without value.
     */
    public void testParseNoValueForOptionCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-d";

        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1);
        assertEquals(1, cp.getOptionSize());
        assertEquals(action, cp.getAction());
        assertTrue(cp.containsKey(CTSCommand.OPTION_DEVICE));
        assertEquals("", cp.getValue(CTSCommand.OPTION_DEVICE));
    }

    /**
     * Test parsing command with single illegal option.
     */
    public void testParseIllOptionCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String actionValue = "devices";
        CommandParser cp = CommandParser.parse(action + " " + actionValue);
        assertEquals(action, cp.getAction());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(1, actionValues.size());
        assertTrue(actionValues.contains(actionValue));
    }

    /**
     * Test parsing command with multiple illegal options.
     */
    public void testParseMultiIllOptionCommand() throws CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-devices";
        final String value1 = "v1";
        final String option2 = "op2";
        final String value2 = "v2";
        try {
            CommandParser.parse(action + " " + option1 + " " + value1 + " "
                    + option2 + " " + value2);
            fail("no exception");
        } catch (UnknownCommandException e) {
        }
    }

    /**
     * Test parsing command and then get the options.
     */
    public void testGetOptions()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String option1 = "-d";
        final String value1 = "v1";
        final String option2 = "--plan";
        final String value2 = "v2";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + option1 + " " + value1
                + " " + option2 + " " + value2);
        assertEquals(2, cp.getOptionSize());
        Set<String> set = cp.getOptionKeys();
        assertEquals(2, set.size());
        assertTrue(set.contains(CTSCommand.OPTION_DEVICE));
        assertTrue(set.contains(CTSCommand.OPTION_PLAN));
    }

    /**
     * Test parsing empty command.
     */
    public void testParseEmptyCommand() throws UnknownCommandException {
        try {
            CommandParser.parse("");
            CommandParser.parse("             ");
            fail("should throw out exception");
        } catch (CommandNotFoundException e) {
        }
    }

    /**
     * Test parsing command without option.
     */
    public void testParseSingleCommand()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "exit";
        CommandParser cp;
        cp = CommandParser.parse(action);
        assertEquals(action, cp.getAction());
        assertEquals(0, cp.getOptionSize());
        assertEquals(0, cp.getOptionKeys().size());
    }

    /**
     * Test parsing command with number.
     */
    public void testParseNumberOption()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "h";
        final String actionValue = "1234";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + actionValue);
        assertEquals(action, cp.getAction());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(1, actionValues.size());
        assertTrue(actionValues.contains(actionValue));
        assertEquals(0, cp.getOptionSize());
        Set<String> set = cp.getOptionKeys();
        assertEquals(0, set.size());
    }

    /**
     * Test parsing command with negative number.
     */
    public void testParseValueNegative()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "ls";
        final String resultOption = "-r";
        final String resultValue = "-13";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + resultOption + " "
                + resultValue);
        assertEquals(action, cp.getAction());
        assertEquals(1, cp.getOptionSize());
        assertTrue(cp.containsKey(CTSCommand.OPTION_RESULT));
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
    }

    /**
     * Test parsing command with capital letter.
     */
    public void testParseCapitalLetter()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "LS";
        final String resultOption = "-R";
        final String resultValue = "-13";
        CommandParser cp;
        cp = CommandParser.parse(action + " " + resultOption + " "
                + resultValue);
        assertEquals(action.toLowerCase(), cp.getAction());
        assertEquals(1, cp.getOptionSize());
        assertTrue(cp.containsKey(CTSCommand.OPTION_RESULT));
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
    }

    /**
     * Test parsing command with multiple action values.
     */
    public void testParseActionValue()
                throws UnknownCommandException, CommandNotFoundException {
        final String action = "h";
        final String actionValue1 = "192";
        final String actionValue2 = "e";
        CommandParser cp = CommandParser.parse(action + " " + actionValue1 + " "
                + actionValue2);
        assertEquals(action, cp.getAction());
        assertEquals(0, cp.getOptionKeys().size());
        ArrayList<String> actionValues = cp.getActionValues();
        assertEquals(2, actionValues.size());
        assertTrue(actionValues.contains(actionValue1));
        assertTrue(actionValues.contains(actionValue2));
    }

    /**
     * Test parsing command of list result.
     */
    public void testParseListResultCmd() throws UnknownCommandException,
            CommandNotFoundException {
        final String action = "ls";
        final String resultOpt = "-r";
        final String resultValue = "pass";
        final String sessionOpt = "-s";
        final String sessionOptComplete = "--session";
        final String sessionId = "1";
        String cmdStr;
        CommandParser cp;

        cmdStr = action + " " + resultOpt + " " + sessionOpt + " " + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals("", cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));

        cmdStr = action + " " + resultOpt + " " + sessionOptComplete + " "
                + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals("", cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));

        cmdStr = action + " " + resultOpt + " " + resultValue + " " + sessionOptComplete + " "
                + sessionId;
        cp = CommandParser.parse(cmdStr);
        assertEquals(action, cp.getAction());
        assertEquals(2, cp.getOptionSize());
        assertEquals(resultValue, cp.getValue(CTSCommand.OPTION_RESULT));
        assertEquals(sessionId, cp.getValue(CTSCommand.OPTION_SESSION));
    }
}
