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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * {@link Activity}s to handle clicks to the pass and fail buttons of the pass fail buttons layout.
 *
 * <ol>
 *     <li>Include the pass fail buttons layout in your layout:
 *         <pre><include layout="@layout/pass_fail_buttons" /></pre>
 *     </li>
 *     <li>Extend one of the activities and call setPassFailButtonClickListeners after
 *         setting your content view.</li>
 *     <li>Make sure to call setResult(RESULT_CANCEL) in your Activity initially.</li>
 *     <li>Optionally call setInfoTextResources to add an info button that will show a
 *         dialog with instructional text.</li>
 * </ol>
 */
public class PassFailButtons {

    // Interface mostly for making documentation and refactoring easier...
    private interface PassFailActivity {

        /**
         * Hooks up the pass and fail buttons to click listeners that will record the test results.
         * <p>
         * Call from {@link Activity#onCreate} after {@link Activity #setContentView(int)}.
         */
        void setPassFailButtonClickListeners();

        /**
         * Adds an initial informational dialog that appears when entering the test activity for
         * the first time. Also enables the visibility of an "Info" button between the "Pass" and
         * "Fail" buttons that can be clicked to show the information dialog again.
         * <p>
         * Call from {@link Activity#onCreate} after {@link Activity #setContentView(int)}.
         *
         * @param titleId for the text shown in the dialog title area
         * @param messageId for the text shown in the dialog's body area
         */
        void setInfoResources(int titleId, int messageId, int viewId);

        Button getPassButton();
    }

    public static class Activity extends android.app.Activity implements PassFailActivity {

        public void setPassFailButtonClickListeners() {
            setPassFailClickListeners(this);
        }

        public void setInfoResources(int titleId, int messageId, int viewId) {
            setInfo(this, titleId, messageId, viewId);
        }

        public Button getPassButton() {
            return getPassButtonView(this);
        }
    }

    public static class ListActivity extends android.app.ListActivity implements PassFailActivity {

        public void setPassFailButtonClickListeners() {
            setPassFailClickListeners(this);
        }

        public void setInfoResources(int titleId, int messageId, int viewId) {
            setInfo(this, titleId, messageId, viewId);
        }

        public Button getPassButton() {
            return getPassButtonView(this);
        }
    }

    private static void setPassFailClickListeners(final android.app.Activity activity) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View target) {
                setTestResultAndFinish(activity, target);
            }
        };

        activity.findViewById(R.id.pass_button).setOnClickListener(clickListener);
        activity.findViewById(R.id.fail_button).setOnClickListener(clickListener);
    }

    private static void setInfo(final android.app.Activity activity, final int titleId,
            final int messageId, final int viewId) {
        // Show the middle "info" button and make it show the info dialog when clicked.
        View infoButton = activity.findViewById(R.id.info_button);
        infoButton.setVisibility(View.VISIBLE);
        infoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                showInfoDialog(activity, titleId, messageId, viewId);
            }
        });

        // Show the info dialog if the user has never seen it before.
        if (!hasSeenInfoDialog(activity)) {
            showInfoDialog(activity, titleId, messageId, viewId);
        }
    }

    private static boolean hasSeenInfoDialog(android.app.Activity activity) {
        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    TestResultsProvider.getTestNameUri(activity.getClass().getName()),
                    new String[] {TestResultsProvider.COLUMN_TEST_INFO_SEEN}, null, null, null);
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void showInfoDialog(final android.app.Activity activity, int titleId,
            int messageId, int viewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity).setIcon(
                android.R.drawable.ic_dialog_info).setTitle(titleId);
        if (viewId > 0) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            builder.setView(inflater.inflate(viewId, null));
        } else {
            builder.setMessage(messageId);
        }
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                markSeenInfoDialog(activity);
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                markSeenInfoDialog(activity);
            }
        })
    	.show();
    }

    private static void markSeenInfoDialog(android.app.Activity activity) {
        ContentResolver resolver = activity.getContentResolver();
        ContentValues values = new ContentValues(2);
        values.put(TestResultsProvider.COLUMN_TEST_NAME, activity.getClass().getName());
        values.put(TestResultsProvider.COLUMN_TEST_INFO_SEEN, 1);
        int numUpdated = resolver.update(
                TestResultsProvider.getTestNameUri(activity.getClass().getName()),
                values, null, null);
        if (numUpdated == 0) {
            resolver.insert(TestResultsProvider.RESULTS_CONTENT_URI, values);
        }
    }

    /** Set the test result corresponding to the button clicked and finish the activity. */
    private static void setTestResultAndFinish(android.app.Activity activity, View target) {
        switch (target.getId()) {
            case R.id.pass_button:
                TestResult.setPassedResult(activity);
                break;

            case R.id.fail_button:
                TestResult.setFailedResult(activity);
                break;

            default:
                throw new IllegalArgumentException("Unknown id: " + target.getId());
        }

        activity.finish();
    }

    private static Button getPassButtonView(android.app.Activity activity) {
        return (Button) activity.findViewById(R.id.pass_button);
    }
}
