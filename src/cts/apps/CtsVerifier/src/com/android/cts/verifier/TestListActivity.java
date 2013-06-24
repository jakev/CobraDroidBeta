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

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/** {@link ListActivity} that displays a  list of manual tests. */
public class TestListActivity extends ListActivity {

    private static final int LAUNCH_TEST_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestListAdapter adapter = new TestListAdapter(this);
        setListAdapter(adapter);

        TestResultContentObserver observer = new TestResultContentObserver(adapter);
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(TestResultsProvider.RESULTS_CONTENT_URI, true, observer);
    }

    /** Launch the activity when its {@link ListView} item is clicked. */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Intent intent = getIntent(position);
        startActivityForResult(intent, LAUNCH_TEST_REQUEST_CODE);
    }

    private Intent getIntent(int position) {
        TestListAdapter adapter = getListAdapter();
        TestListItem item = adapter.getItem(position);
        return item.intent;
    }

    @Override
    public TestListAdapter getListAdapter() {
        return (TestListAdapter) super.getListAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LAUNCH_TEST_REQUEST_CODE:
                handleLaunchTestResult(resultCode, data);
                break;

            default:
                throw new IllegalArgumentException("Unknown request code: " + requestCode);
        }
    }

    private void handleLaunchTestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            TestResult testResult = TestResult.fromActivityResult(resultCode, data);
            ContentValues values = new ContentValues(2);
            values.put(TestResultsProvider.COLUMN_TEST_RESULT, testResult.getResult());
            values.put(TestResultsProvider.COLUMN_TEST_NAME, testResult.getName());

            ContentResolver resolver = getContentResolver();
            int numUpdated = resolver.update(TestResultsProvider.RESULTS_CONTENT_URI, values,
                    TestResultsProvider.COLUMN_TEST_NAME + " = ?",
                    new String[] {testResult.getName()});

            if (numUpdated == 0) {
                resolver.insert(TestResultsProvider.RESULTS_CONTENT_URI, values);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                handleClearItemSelected();
                return true;

            case R.id.copy:
                handleCopyItemSelected();
                return true;

            case R.id.share:
                handleShareItemSelected();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleClearItemSelected() {
        ContentResolver resolver = getContentResolver();
        resolver.delete(TestResultsProvider.RESULTS_CONTENT_URI, "1", null);
        Toast.makeText(this, R.string.test_results_cleared, Toast.LENGTH_SHORT).show();
    }

    private void handleCopyItemSelected() {
        TestResultsReport report = new TestResultsReport(this, getListAdapter());
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setText(report.getBody());
        Toast.makeText(this, R.string.test_results_copied, Toast.LENGTH_SHORT).show();
    }

    private void handleShareItemSelected() {
        Intent target = new Intent(Intent.ACTION_SEND);
        target.setType("text/plain");

        TestResultsReport report = new TestResultsReport(this, getListAdapter());
        target.putExtra(Intent.EXTRA_SUBJECT, report.getSubject());
        target.putExtra(Intent.EXTRA_TEXT, report.getBody());
        startActivity(Intent.createChooser(target, getString(R.string.share_test_results)));
    }

    /**
     * {@link ContentResolver} that refreshes the {@link TestListAdapter} and thus
     * the {@link ListView} when the test results change.
     */
    private static class TestResultContentObserver extends ContentObserver {

        private final TestListAdapter mAdapter;

        public TestResultContentObserver(TestListAdapter adapter) {
            super(new Handler());
            this.mAdapter = adapter;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // TODO: Could be improved by just refreshing the particular test result.
            mAdapter.refreshTestResults();
        }
    }
}
