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

package com.android.cellbroadcastreceiver.tests;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * Activity to send test cell broadcast messages from GUI.
 */
public class SendTestBroadcastActivity extends Activity {
    private static String TAG = "SendTestBroadcastActivity";

    /** Whether to delay before sending test message. */
    private boolean mDelayBeforeSending;

    /** Delay time before sending test message (when box is checked). */
    private static final int DELAY_BEFORE_SENDING_MSEC = 5000;

    /** Callback for sending test message after delay */
    private OnClickListener mPendingButtonClick;

    private Handler mDelayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // call the onClick() method again, passing null View.
            // The callback will ignore mDelayBeforeSending when the View is null.
            mPendingButtonClick.onClick(null);
        }
    };



    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView(int)} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_buttons);
                
        /* Send an ETWS normal broadcast message to app. */
        Button etwsNormalTypeButton = (Button) findViewById(R.id.button_etws_normal_type);
        etwsNormalTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendEtwsMessageNormal(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send an ETWS cancel broadcast message to app. */
        Button etwsCancelTypeButton = (Button) findViewById(R.id.button_etws_cancel_type);
        etwsCancelTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendEtwsMessageCancel(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send an ETWS test broadcast message to app. */
        Button etwsTestTypeButton = (Button) findViewById(R.id.button_etws_test_type);
        etwsTestTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendEtwsMessageTest(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a GSM 7-bit broadcast message to app. */
        Button gsm7bitTypeButton = (Button) findViewById(R.id.button_gsm_7bit_type);
        gsm7bitTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bit(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS 7-bit broadcast message to app. */
        Button gsm7bitUmtsTypeButton = (Button) findViewById(R.id.button_gsm_7bit_umts_type);
        gsm7bitUmtsTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a GSM 7-bit no padding broadcast message to app. */
        Button gsm7bitNoPaddingButton = (Button) findViewById(R.id.button_gsm_7bit_nopadding_type);
        gsm7bitNoPaddingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitNoPadding(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS 7-bit no padding broadcast message to app. */
        Button gsm7bitNoPaddingUmtsTypeButton =
                (Button) findViewById(R.id.button_gsm_7bit_nopadding_umts_type);
        gsm7bitNoPaddingUmtsTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitNoPaddingUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS 7-bit multi-page broadcast message to app. */
        Button gsm7bitMultipageButton =
                (Button) findViewById(R.id.button_gsm_7bit_multipage_type);
        gsm7bitMultipageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitMultipageGsm(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS 7-bit multi-page broadcast message to app. */
        Button gsm7bitMultipageUmtsButton =
                (Button) findViewById(R.id.button_gsm_7bit_multipage_umts_type);
        gsm7bitMultipageUmtsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitMultipageUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a GSM 7-bit broadcast message with language to app. */
        Button gsm7bitWithLanguageButton =
                (Button) findViewById(R.id.button_gsm_7bit_with_language_type);
        gsm7bitWithLanguageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitWithLanguage(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS 7-bit broadcast message with language to app. */
        Button gsm7bitWithLanguageUmtsButton =
                (Button) findViewById(R.id.button_gsm_7bit_with_language_body_umts_type);
        gsm7bitWithLanguageUmtsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessage7bitWithLanguageInBodyUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a GSM UCS-2 broadcast message to app. */
        Button gsmUcs2TypeButton = (Button) findViewById(R.id.button_gsm_ucs2_type);
        gsmUcs2TypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessageUcs2(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS UCS-2 broadcast message to app. */
        Button gsmUcs2UmtsTypeButton = (Button) findViewById(R.id.button_gsm_ucs2_umts_type);
        gsmUcs2UmtsTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessageUcs2Umts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS UCS-2 multipage broadcast message to app. */
        Button gsmUcs2MultipageUmtsTypeButton =
                (Button) findViewById(R.id.button_gsm_ucs2_multipage_umts_type);
        gsmUcs2MultipageUmtsTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessageUcs2MultipageUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a GSM UCS-2 broadcast message with language to app. */
        Button gsmUcs2WithLanguageTypeButton =
                (Button) findViewById(R.id.button_gsm_ucs2_with_language_type);
        gsmUcs2WithLanguageTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessageUcs2WithLanguageInBody(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Send a UMTS UCS-2 broadcast message with language to app. */
        Button gsmUcs2WithLanguageUmtsTypeButton =
                (Button) findViewById(R.id.button_gsm_ucs2_with_language_umts_type);
        gsmUcs2WithLanguageUmtsTypeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mDelayBeforeSending && v != null) {
                    mPendingButtonClick = this;
                    mDelayHandler.sendEmptyMessageDelayed(0, DELAY_BEFORE_SENDING_MSEC);
                } else {
                    SendTestMessages.testSendMessageUcs2WithLanguageUmts(SendTestBroadcastActivity.this);
                }
            }
        });

        /* Update boolean to delay before sending when box is checked. */
        final CheckBox delayCheckbox = (CheckBox) findViewById(R.id.button_delay_broadcast);
        delayCheckbox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDelayBeforeSending = delayCheckbox.isChecked();
            }
        });
    }
}
