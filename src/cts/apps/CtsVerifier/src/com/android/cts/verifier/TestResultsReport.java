/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.cts.verifier;

import com.android.cts.verifier.TestListAdapter.TestListItem;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

/** Plain text report of the current test results. */
class TestResultsReport {

    private final Context mContext;

    private final TestListAdapter mAdapter;

    private final String mVersionName;

    TestResultsReport(Context context, TestListAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
        this.mVersionName = getVersionName(context);
    }

    private static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get find package information for "
                    + context.getPackageName());
        }
    }

    String getSubject() {
        return new StringBuilder()
                .append(mContext.getString(R.string.subject_header, mVersionName))
                .append(' ')
                .append(Build.FINGERPRINT)
                .toString();
    }

    String getBody() {
        StringBuilder builder = new StringBuilder()
                .append(mContext.getString(R.string.body_header, mVersionName))
                .append("\n\n")
                .append(Build.FINGERPRINT)
                .append("\n\n");

        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            TestListItem item = mAdapter.getItem(i);
            if (!item.isTest()) {
                builder.append(item.title).append('\n');
            } else {
                builder.append(item.title)
                        .append(".....")
                        .append(getTestResultString(mAdapter.getTestResult(i)))
                        .append('\n');
            }

            if (i + 1 < count && !mAdapter.getItem(i + 1).isTest()) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private String getTestResultString(int testResult) {
        int resId = 0;
        switch (testResult) {
            case TestResult.TEST_RESULT_PASSED:
                resId = R.string.pass_result;
                break;

            case TestResult.TEST_RESULT_FAILED:
                resId = R.string.fail_result;
                break;

            case TestResult.TEST_RESULT_NOT_EXECUTED:
                resId = R.string.not_executed_result;
                break;

            default:
                throw new IllegalArgumentException("Unknown test result: " + testResult);
        }
        return mContext.getString(resId);
    }
}
