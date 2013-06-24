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

package android.widget.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.test.AndroidTestCase;
import android.widget.Gallery.LayoutParams;

import java.io.IOException;

/**
 * Test {@link LayoutParams}.
 */
@TestTargetClass(LayoutParams.class)
public class Gallery_LayoutParamsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        XmlResourceParser p = mContext.getResources().getLayout(R.layout.gallery_test);
        WidgetTestUtils.beginDocument(p, "LinearLayout");
        new LayoutParams(getContext(), p);

        LayoutParams params = new LayoutParams(320, 480);
        new LayoutParams(params);
    }
}
