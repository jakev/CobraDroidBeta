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

package android.view.inputmethod.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.os.Binder;
import android.os.Parcel;
import android.test.AndroidTestCase;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputBinding;

@TestTargetClass(InputBinding.class)
public class InputBindingTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getConnection",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getConnectionToken",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InputBinding",
            args = {android.view.inputmethod.InputConnection.class,
                    android.view.inputmethod.InputBinding.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InputBinding",
            args = {android.view.inputmethod.InputConnection.class, android.os.IBinder.class,
                    int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public void testInputBinding() {
        View view = new View(getContext());
        BaseInputConnection bic = new BaseInputConnection(view, false);
        Binder binder = new Binder();
        int uid = 1;
        int pid = 2;
        InputBinding inputBinding = new InputBinding(bic, binder, uid, pid);
        new InputBinding(bic, inputBinding);
        assertSame(bic, inputBinding.getConnection());
        assertSame(binder, inputBinding.getConnectionToken());
        assertEquals(uid, inputBinding.getUid());
        assertEquals(pid, inputBinding.getPid());

        assertNotNull(inputBinding.toString());
        assertEquals(0, inputBinding.describeContents());

        Parcel p = Parcel.obtain();
        inputBinding.writeToParcel(p, 0);
        p.setDataPosition(0);
        InputBinding target = InputBinding.CREATOR.createFromParcel(p);
        assertEquals(uid, target.getUid());
        assertEquals(pid, target.getPid());
        assertSame(binder, target.getConnectionToken());
    }
}
