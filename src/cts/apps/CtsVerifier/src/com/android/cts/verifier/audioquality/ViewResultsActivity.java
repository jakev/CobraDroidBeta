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

package com.android.cts.verifier.audioquality;

import com.android.cts.verifier.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This Activity allows the user to examine the results of the
 * experiments which have been run so far.
 */
public class ViewResultsActivity extends Activity implements View.OnClickListener {
    private TextView mTextView;
    private Button mDismissButton;
    private Button mSendResultsButton;
    private String mResults;

    private ArrayList<Experiment> mExperiments;

    // The package of the Gmail application
    private static final String GMAIL_PACKAGE = "com.google.android.gm";

    // The Gmail compose activity name
    private static final String GMAIL_ACTIVITY = GMAIL_PACKAGE
            + ".ComposeActivityGmail";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aq_view_results);

        mDismissButton = (Button) findViewById(R.id.dismissButton);
        mDismissButton.setOnClickListener(this);

        mSendResultsButton = (Button) findViewById(R.id.sendResultsButton);
        mSendResultsButton.setOnClickListener(this);

        mTextView = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        mResults = intent.getStringExtra(AudioQualityVerifierActivity.EXTRA_RESULTS);
        mTextView.setText(mResults);

        mExperiments = VerifierExperiments.getExperiments(this);
    }

    private void sendResults() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setComponent(new ComponentName(GMAIL_PACKAGE, GMAIL_ACTIVITY));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.aq_subject));
        intent.putExtra(Intent.EXTRA_TEXT, mResults);

        ArrayList<Parcelable> attachments = new ArrayList<Parcelable>();
        for (Experiment exp : mExperiments) {
            List<String> filenames = exp.getAudioFileNames();
            for (String filename : filenames) {
                attachments.add(Uri.fromFile(new File(filename)));
            }
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

        startActivity(intent);
    }

    // Implements View.OnClickListener
    public void onClick(View v) {
        if (v == mDismissButton) {
            finish();
        } else if (v == mSendResultsButton) {
            sendResults();
        }
    }
}
