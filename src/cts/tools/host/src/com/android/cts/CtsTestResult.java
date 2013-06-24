/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.cts;

import junit.framework.TestFailure;
import junit.framework.TestResult;

import java.util.Enumeration;
import java.util.HashMap;


/**
 * Store the result of a specific test.
 */
public class CtsTestResult {
    private int mResultCode;
    private String mFailedMessage;
    private String mStackTrace;

    public static final int CODE_INIT = -1;
    public static final int CODE_NOT_EXECUTED = 0;
    public static final int CODE_PASS = 1;
    public static final int CODE_FAIL = 2;
    public static final int CODE_ERROR = 3;
    public static final int CODE_TIMEOUT = 4;
    public static final int CODE_FIRST = CODE_INIT;
    public static final int CODE_LAST = CODE_TIMEOUT;

    public static final String STR_ERROR = "error";
    public static final String STR_TIMEOUT = "timeout";
    public static final String STR_NOT_EXECUTED = "notExecuted";
    public static final String STR_FAIL = "fail";
    public static final String STR_PASS = "pass";

    private static HashMap<Integer, String> sCodeToResultMap;
    private static HashMap<String, Integer> sResultToCodeMap;
    static {
        sCodeToResultMap = new HashMap<Integer, String>();
        sCodeToResultMap.put(CODE_NOT_EXECUTED, STR_NOT_EXECUTED);
        sCodeToResultMap.put(CODE_PASS, STR_PASS);
        sCodeToResultMap.put(CODE_FAIL, STR_FAIL);
        sCodeToResultMap.put(CODE_ERROR, STR_ERROR);
        sCodeToResultMap.put(CODE_TIMEOUT, STR_TIMEOUT);
        sResultToCodeMap = new HashMap<String, Integer>();
        for (int code : sCodeToResultMap.keySet()) {
            sResultToCodeMap.put(sCodeToResultMap.get(code), code);
        }
    }

    public CtsTestResult(int resCode) {
        mResultCode = resCode;
    }

    public CtsTestResult(int resCode, final String failedMessage, final String stackTrace) {
        mResultCode = resCode;
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }

    public CtsTestResult(final String result, final String failedMessage,
            final String stackTrace) throws InvalidTestResultStringException {
        if (!sResultToCodeMap.containsKey(result)) {
            throw new InvalidTestResultStringException(result);
        }

        mResultCode = sResultToCodeMap.get(result);
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }

    /**
     * Check if the result indicates failure.
     *
     * @return If failed, return true; else, return false.
     */
    public boolean isFail() {
        return mResultCode == CODE_FAIL;
    }

    /**
     * Check if the result indicates pass.
     *
     * @return If pass, return true; else, return false.
     */
    public boolean isPass() {
        return mResultCode == CODE_PASS;
    }

    /**
     * Check if the result indicates not executed.
     *
     * @return If not executed, return true; else, return false.
     */
    public boolean isNotExecuted() {
        return mResultCode == CODE_NOT_EXECUTED;
    }

    /**
     * Get result code of the test.
     *
     * @return The result code of the test.
     *         The following is the possible result codes:
     * <ul>
     *    <li> notExecuted
     *    <li> pass
     *    <li> fail
     *    <li> error
     *    <li> timeout
     * </ul>
     */
    public int getResultCode() {
        return mResultCode;
    }

    /**
     * Get the failed message.
     *
     * @return The failed message.
     */
    public String getFailedMessage() {
        return mFailedMessage;
    }

    /**
     * Get the stack trace.
     *
     * @return The stack trace.
     */
    public String getStackTrace() {
        return mStackTrace;
    }

    /**
     * Set the result.
     *
     * @param testResult The result in the form of JUnit test result.
     */
    @SuppressWarnings("unchecked")
    public void setResult(TestResult testResult) {
        int resCode = CODE_PASS;
        String failedMessage = null;
        String stackTrace = null;
        if ((testResult != null) && (testResult.failureCount() > 0 || testResult.errorCount() > 0)) {
            resCode = CODE_FAIL;
            Enumeration<TestFailure> failures = testResult.failures();
            while (failures.hasMoreElements()) {
                TestFailure failure = failures.nextElement();
                failedMessage += failure.exceptionMessage();
                stackTrace += failure.trace();
            }
            Enumeration<TestFailure> errors = testResult.errors();
            while (errors.hasMoreElements()) {
                TestFailure failure = errors.nextElement();
                failedMessage += failure.exceptionMessage();
                stackTrace += failure.trace();
            }
        }
        mResultCode = resCode;
        mFailedMessage = failedMessage;
        mStackTrace = stackTrace;
    }

    /**
     * Reverse the result code.
     */
    public void reverse() {
        if (isPass()) {
            mResultCode = CtsTestResult.CODE_FAIL;
        } else if (isFail()){
            mResultCode = CtsTestResult.CODE_PASS;
        }
    }

    /**
     * Get the test result as string.
     *
     * @return The readable result string.
     */
    public String getResultString() {
        return sCodeToResultMap.get(mResultCode);
    }

    /**
     * Check if the given resultType is a valid result type defined..
     *
     * @param resultType The result type to be checked.
     * @return If valid, return true; else, return false.
     */
    static public boolean isValidResultType(final String resultType) {
        return sResultToCodeMap.containsKey(resultType);
    }
}
