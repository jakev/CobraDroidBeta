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

package android.apidemos.cts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.cts.refapp.ReferenceAppTestCase;
import android.view.KeyEvent;

import com.example.android.apis.ApiDemos;

import java.util.List;

public class ApiDemosTest extends ReferenceAppTestCase<ApiDemos> {
    public ApiDemosTest() {
        super("com.example.android.apis", ApiDemos.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Make sure the list view always resets its selection to the top of
        // the list.
        final ApiDemos a = getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                a.setSelection(0);
            }
        });
    }

    public void testdPadNav() {
        final ApiDemos a = getActivity();
        assert(a.getSelectedItemPosition() == 0);

        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assert(a.getSelectedItemPosition() == 0);

        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assert(a.getSelectedItemPosition() == 1);

        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        assert(a.getSelectedItemPosition() == 1);

        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assert(a.getSelectedItemPosition() == 1);
    }

    public void testNumberOfItemsInListView() {
        final ApiDemos a = getActivity();

        // ApiDemo's builds its list by looking at all the Intent's in it's
        // package that are marked as CATEGORY_SAMPLE_CODE.
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        PackageManager pm = a.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        int numberOfActivities = list.size();

        for (int x = 0; x < numberOfActivities; ++x) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            assert(a.getSelectedItemPosition() == x + 1);
        }

        // Should be at bottom of the list
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assert(a.getSelectedItemPosition() == numberOfActivities);

        // Record what the bottom of the list looks like.
        takeSnapshot("snap1");
    }
}
