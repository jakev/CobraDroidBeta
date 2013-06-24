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
package com.android.apps.tag;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Dialog with introductory text blurbs and pictures explaining NFC.
 */
public class AboutActivity extends Activity implements OnClickListener {

    private int mPage;
    private View[] mPageContainers;
    private Button mBack;
    private Button mNext;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        LayoutInflater inflater = LayoutInflater.from(this);
        setContentView(inflater.inflate(R.layout.intro_to_nfc, null));

        mPageContainers = new View[2];
        mPageContainers[0] = findViewById(R.id.page_1);
        mPageContainers[1] = findViewById(R.id.page_2);

        mBack = (Button) findViewById(R.id.back);
        mNext = (Button) findViewById(R.id.next);

        mBack.setOnClickListener(this);
        mNext.setOnClickListener(this);

        mPage = -1;
        navigate(0);
    }

    private void navigate(int page) {
        if (mPage == page) {
            return;
        }

        if (page >= mPageContainers.length) {
            finish();
            return;
        }

        mPage = page;
        for (int i = 0, len = mPageContainers.length; i < len; i++) {
            View view = mPageContainers[i];
            view.setVisibility((i == page) ? View.VISIBLE : View.GONE);
        }

        mBack.setEnabled((mPage == 0) ? false : true);

        if (mPage < mPageContainers.length - 1) {
            mNext.setText(getResources().getString(R.string.next));
        } else {
            mNext.setText(getResources().getString(R.string.close));
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mBack) {
            navigate(mPage - 1);
        } else if (view == mNext) {
            navigate(mPage + 1);
        }
    }
}
