/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.cts.verifier.bluetooth;

import com.android.cts.verifier.PassFailButtons;
import com.android.cts.verifier.R;
import com.android.cts.verifier.TestResult;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothTestActivity extends PassFailButtons.ListActivity {

    public static final int TEST_BLUETOOTH_TOGGLE = 0;
    public static final int TEST_SECURE_SERVER = 1;
    public static final int TEST_INSECURE_SERVER = 2;
    public static final int TEST_SECURE_CLIENT = 3;
    public static final int TEST_INSECURE_CLIENT = 4;

    private static final int START_TOGGLE_BLUETOOTH_TEST_REQUEST = 1;
    private static final int START_SECURE_SERVER_REQUEST = 2;
    private static final int START_INSECURE_SERVER_REQUEST = 3;
    private static final int START_SECURE_PICK_SERVER_REQUEST = 4;
    private static final int START_INSECURE_PICK_SERVER_REQUEST = 5;
    private static final int START_SECURE_CLIENT_REQUEST = 6;
    private static final int START_INSECURE_CLIENT_REQUEST = 7;

    private TestListItem mBluetoothToggleTest;
    private TestListItem mSecureServerTest;
    private TestListItem mInsecureServerTest;
    private TestListItem mSecureClientTest;
    private TestListItem mInsecureClientTest;

    private static final String TABLE_NAME = "results";
    private static final String _ID = "_id";
    private static final String COLUMN_TEST_ID = "test_id";
    private static final String COLUMN_TEST_RESULT = "test_result";
    private static final String[] ALL_COLUMNS = {
          _ID,
          COLUMN_TEST_ID,
          COLUMN_TEST_RESULT,
    };

    private TestListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_main);
        setPassFailButtonClickListeners();
        setInfoResources(R.string.bluetooth_test, R.string.bluetooth_test_info, -1);

        mBluetoothToggleTest = TestListItem.newTest(R.string.bt_toggle_bluetooth,
                TEST_BLUETOOTH_TOGGLE);
        mSecureServerTest = TestListItem.newTest(R.string.bt_secure_server,
                TEST_SECURE_SERVER);
        mInsecureServerTest = TestListItem.newTest(R.string.bt_insecure_server,
                TEST_INSECURE_SERVER);
        mSecureClientTest = TestListItem.newTest(R.string.bt_secure_client,
                TEST_SECURE_CLIENT);
        mInsecureClientTest = TestListItem.newTest(R.string.bt_insecure_client,
                TEST_INSECURE_CLIENT);

        mAdapter = new TestListAdapter(this);
        mAdapter.add(TestListItem.newCategory(R.string.bt_control));
        mAdapter.add(mBluetoothToggleTest);

        mAdapter.add(TestListItem.newCategory(R.string.bt_device_communication));
        mAdapter.add(mSecureServerTest);
        mAdapter.add(mInsecureServerTest);
        mAdapter.add(mSecureClientTest);
        mAdapter.add(mInsecureClientTest);

        setListAdapter(mAdapter);
        refreshTestResults();

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            showNoBluetoothDialog();
        }
    }

    private void refreshTestResults() {
        new RefreshTask().execute();
    }

    private void showNoBluetoothDialog() {
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.bt_not_available_title)
        .setMessage(R.string.bt_not_available_message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
        .show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TestListItem testItem = (TestListItem) l.getItemAtPosition(position);
        switch (testItem.mId) {
            case TEST_BLUETOOTH_TOGGLE:
                startToggleBluetoothActivity();
                break;

            case TEST_SECURE_SERVER:
                startServerActivity(true);
                break;

            case TEST_INSECURE_SERVER:
                startServerActivity(false);
                break;

            case TEST_SECURE_CLIENT:
                startDevicePickerActivity(true);
                break;

            case TEST_INSECURE_CLIENT:
                startDevicePickerActivity(false);
                break;
        }
    }

    private void startToggleBluetoothActivity() {
        Intent intent = new Intent(this, BluetoothToggleActivity.class);
        startActivityForResult(intent, START_TOGGLE_BLUETOOTH_TEST_REQUEST);
    }

    private void startServerActivity(boolean secure) {
        Intent intent = new Intent(this, MessageTestActivity.class)
                .putExtra(MessageTestActivity.EXTRA_SECURE, secure);
        startActivityForResult(intent, secure
                ? START_SECURE_SERVER_REQUEST
                : START_INSECURE_SERVER_REQUEST);
    }

    private void startDevicePickerActivity(boolean secure) {
        Intent intent = new Intent(this, DevicePickerActivity.class);
        startActivityForResult(intent, secure
                ? START_SECURE_PICK_SERVER_REQUEST
                : START_INSECURE_PICK_SERVER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_TOGGLE_BLUETOOTH_TEST_REQUEST:
                handleEnableBluetoothResult(resultCode, data);
                break;

            case START_SECURE_SERVER_REQUEST:
                handleServerResult(resultCode, data, true);
                break;

            case START_INSECURE_SERVER_REQUEST:
                handleServerResult(resultCode, data, false);
                break;

            case START_SECURE_PICK_SERVER_REQUEST:
                handleDevicePickerResult(resultCode, data, true);
                break;

            case START_INSECURE_PICK_SERVER_REQUEST:
                handleDevicePickerResult(resultCode, data, false);
                break;

            case START_SECURE_CLIENT_REQUEST:
                handleClientResult(resultCode, data, true);
                break;

            case START_INSECURE_CLIENT_REQUEST:
                handleClientResult(resultCode, data, false);
                break;
        }
    }

    private void handleEnableBluetoothResult(int resultCode, Intent data) {
        if (data != null) {
            TestResult result = TestResult.fromActivityResult(resultCode, data);
            mBluetoothToggleTest.setResult(result.getResult());
            updateTest(mBluetoothToggleTest);
        }
    }

    private void updateTest(TestListItem item) {
        new UpdateTask().execute(item.mId, item.mResult);
    }

    private void handleServerResult(int resultCode, Intent data, boolean secure) {
        if (data != null) {
            TestResult result = TestResult.fromActivityResult(resultCode, data);
            TestListItem test = secure ? mSecureServerTest : mInsecureServerTest;
            test.setResult(result.getResult());
            updateTest(test);
        }
    }

    private void handleDevicePickerResult(int resultCode, Intent data, boolean secure) {
        if (resultCode == RESULT_OK) {
            String address = data.getStringExtra(DevicePickerActivity.EXTRA_DEVICE_ADDRESS);
            startClientActivity(address, secure);
        }
    }

    private void startClientActivity(String address, boolean secure) {
        Intent intent = new Intent(this, MessageTestActivity.class)
                .putExtra(MessageTestActivity.EXTRA_DEVICE_ADDRESS, address)
                .putExtra(MessageTestActivity.EXTRA_SECURE, secure);
        startActivityForResult(intent, secure
                ? START_SECURE_CLIENT_REQUEST
                : START_INSECURE_CLIENT_REQUEST);
    }

    private void handleClientResult(int resultCode, Intent data, boolean secure) {
        if (data != null) {
            TestResult result = TestResult.fromActivityResult(resultCode, data);
            TestListItem test = secure ? mSecureClientTest : mInsecureClientTest;
            test.setResult(result.getResult());
            updateTest(test);
        }
    }

    private class UpdateTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer[] resultPairs) {
            TestResultsHelper openHelper = new TestResultsHelper(BluetoothTestActivity.this);
            SQLiteDatabase db = openHelper.getWritableDatabase();

            int testId = resultPairs[0];
            int testResult = resultPairs[1];

            ContentValues values = new ContentValues(2);
            values.put(COLUMN_TEST_ID, testId);
            values.put(COLUMN_TEST_RESULT, testResult);

            try {
                if (0 == db.update(TABLE_NAME, values, COLUMN_TEST_ID + " = ?",
                        new String[] {Integer.toString(testId)})) {
                    db.insert(TABLE_NAME, null, values);
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            refreshTestResults();
        }
    }

    private class RefreshTask extends AsyncTask<Void, Void, Map<Integer, Integer>> {

        @Override
        protected Map<Integer, Integer> doInBackground(Void... params) {
            Map<Integer, Integer> results = new HashMap<Integer, Integer>();
            TestResultsHelper openHelper = new TestResultsHelper(BluetoothTestActivity.this);
            SQLiteDatabase db = openHelper.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    int testId = cursor.getInt(1);
                    int testResult = cursor.getInt(2);
                    results.put(testId, testResult);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(Map<Integer, Integer> results) {
            super.onPostExecute(results);
            for (Integer testId : results.keySet()) {
                TestListItem item = mAdapter.getTest(testId);
                if (item != null) {
                    item.setResult(results.get(testId));
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    static class TestListItem {

        static final int NUM_VIEW_TYPES = 2;

        static final int VIEW_TYPE_CATEGORY = 0;

        static final int VIEW_TYPE_TEST = 1;

        final int mViewType;

        final int mTitle;

        final int mId;

        int mResult;

        static TestListItem newTest(int title, int id) {
            return new TestListItem(VIEW_TYPE_TEST, title, id);
        }

        static TestListItem newCategory(int title) {
            return new TestListItem(VIEW_TYPE_CATEGORY, title, -1);
        }

        private TestListItem(int viewType, int title, int id) {
            this.mViewType = viewType;
            this.mTitle = title;
            this.mId = id;
        }

        public boolean isTest() {
            return mViewType == VIEW_TYPE_TEST;
        }

        public void setResult(int result) {
            mResult = result;
        }
    }

    static class TestListAdapter extends BaseAdapter {

        private static final int PADDING = 10;

        private final List<TestListItem> mItems = new ArrayList<TestListItem>();

        private final Map<Integer, TestListItem> mTestsById = new HashMap<Integer, TestListItem>();

        private final LayoutInflater mInflater;

        public TestListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        public void add(TestListItem item) {
            mItems.add(item);
            if (item.isTest()) {
                mTestsById.put(item.mId, item);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int backgroundResource = 0;
            int iconResource = 0;

            TestListItem item = getItem(position);

            TextView textView = null;
            if (convertView == null) {
                int layout = getLayout(position);
                textView = (TextView) mInflater.inflate(layout, parent, false);
            } else {
                textView = (TextView) convertView;
            }

            textView.setText(item.mTitle);

            if (item.isTest()) {
                switch (item.mResult) {
                    case TestResult.TEST_RESULT_PASSED:
                        backgroundResource = R.drawable.test_pass_gradient;
                        iconResource = R.drawable.fs_good;
                        break;

                    case TestResult.TEST_RESULT_FAILED:
                        backgroundResource = R.drawable.test_fail_gradient;
                        iconResource = R.drawable.fs_error;
                        break;

                    case TestResult.TEST_RESULT_NOT_EXECUTED:
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown test result: " + item.mResult);
                }

                textView.setBackgroundResource(backgroundResource);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconResource, 0);
                textView.setPadding(PADDING, 0, PADDING, 0);
                textView.setCompoundDrawablePadding(PADDING);
            }

            return textView;
        }

        private int getLayout(int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case TestListItem.VIEW_TYPE_CATEGORY:
                    return R.layout.test_category_row;
                case TestListItem.VIEW_TYPE_TEST:
                    return android.R.layout.simple_list_item_1;
                default:
                    throw new IllegalArgumentException("Illegal view type: " + viewType);

            }
        }

        public TestListItem getTest(int id) {
            return mTestsById.get(id);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public TestListItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).mId;
        }

        @Override
        public int getViewTypeCount() {
            return TestListItem.NUM_VIEW_TYPES;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).mViewType;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != TestListItem.VIEW_TYPE_CATEGORY;
        }
    }

    class TestResultsHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "bluetooth_results.db";

        private static final int DATABASE_VERSION = 1;

        TestResultsHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TEST_ID + " INTEGER, "
                    + COLUMN_TEST_RESULT + " INTEGER DEFAULT 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
