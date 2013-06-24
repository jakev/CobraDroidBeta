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

package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * This class manages the view for given conversation.
 */
public class CellBroadcastListItem extends RelativeLayout {
    private static final String TAG = "CellBroadcastListItem";
    private static final boolean DEBUG = false;

    private CellBroadcastMessage mCbMessage;

    private TextView mChannelView;
    private TextView mMessageView;
    private TextView mDateView;

    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    public CellBroadcastListItem(Context context) {
        super(context);
    }

    public CellBroadcastListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    CellBroadcastMessage getMessage() {
        return mCbMessage;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mChannelView = (TextView) findViewById(R.id.channel);
        mDateView = (TextView) findViewById(R.id.date);
        mMessageView = (TextView) findViewById(R.id.message);
    }

    /**
     * Only used for header binding.
     * @param message the message contents to bind
     */
    public void bind(CellBroadcastMessage message) {
        mCbMessage = message;

        Drawable background = message.isRead() ?
                getResources().getDrawable(R.drawable.list_item_background_read) :
                getResources().getDrawable(R.drawable.list_item_background_unread);

        setBackgroundDrawable(background);

        mChannelView.setText(message.getDialogTitleResource());
        mDateView.setText(message.getDateString(getContext()));
        mMessageView.setText(formatMessage(message));
    }

    private static CharSequence formatMessage(CellBroadcastMessage message) {
        String body = message.getMessageBody();

        SpannableStringBuilder buf = new SpannableStringBuilder(body);

        // Unread messages are shown in bold
        if (!message.isRead()) {
            buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return buf;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Speak the date first, then channel name, then message body
        event.getText().add(mCbMessage.getSpokenDateString(getContext()));
        mChannelView.dispatchPopulateAccessibilityEvent(event);
        mMessageView.dispatchPopulateAccessibilityEvent(event);
        return true;
    }
}
