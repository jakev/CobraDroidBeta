/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox.ui;

import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.R;
import com.android.quicksearchbox.Source;
import com.android.quicksearchbox.Suggestion;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.SuggestionFormatter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * View for the items in the suggestions list. This includes promoted suggestions,
 * sources, and suggestions under each source.
 */
public class DefaultSuggestionView extends RelativeLayout implements SuggestionView {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SuggestionView";

    private TextView mText1;
    private TextView mText2;
    private ImageView mIcon1;
    private ImageView mIcon2;
    private final SuggestionFormatter mSuggestionFormatter;
    private boolean mRefineable;
    private int mPosition;
    private SuggestionClickListener mClickListener;
    private KeyListener mKeyListener;

    public DefaultSuggestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSuggestionFormatter = QsbApplication.get(context).getSuggestionFormatter();
    }

    public DefaultSuggestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSuggestionFormatter = QsbApplication.get(context).getSuggestionFormatter();
    }

    public DefaultSuggestionView(Context context) {
        super(context);
        mSuggestionFormatter = QsbApplication.get(context).getSuggestionFormatter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText1 = (TextView) findViewById(R.id.text1);
        mText2 = (TextView) findViewById(R.id.text2);
        mIcon1 = (ImageView) findViewById(R.id.icon1);
        mIcon2 = (ImageView) findViewById(R.id.icon2);
        // for some reason, creating mKeyListener inside the constructor causes it not to work.
        mKeyListener = new KeyListener();

        setOnKeyListener(mKeyListener);
    }

    public void bindAsSuggestion(SuggestionCursor suggestion, SuggestionClickListener onClick) {
        setOnClickListener(new ClickListener());
        setOnLongClickListener(new LongClickListener());
        mPosition = suggestion.getPosition();
        mClickListener = onClick;

        CharSequence text1 = formatText(suggestion.getSuggestionText1(), suggestion, true);
        CharSequence text2 = suggestion.getSuggestionText2Url();
        if (text2 != null) {
            text2 = formatUrl(text2);
        } else {
            text2 = formatText(suggestion.getSuggestionText2(), suggestion, false);
        }
        Drawable icon1 = getSuggestionDrawableIcon1(suggestion);
        Drawable icon2 = getSuggestionDrawableIcon2(suggestion);
        if (DBG) {
            Log.d(TAG, "bindAsSuggestion(), text1=" + text1 + ",text2=" + text2
                    + ",icon1=" + icon1 + ",icon2=" + icon2);
        }
        // If there is no text for the second line, allow the first line to be up to two lines
        if (TextUtils.isEmpty(text2)) {
            mText1.setSingleLine(false);
            mText1.setMaxLines(2);
            mText1.setEllipsize(TextUtils.TruncateAt.START);
        } else {
            mText1.setSingleLine(true);
            mText1.setMaxLines(1);
            mText1.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
        setText1(text1);
        setText2(text2);
        setIcon1(icon1);
        setIcon2(icon2, null);
        updateRefinable(suggestion);
    }

    protected void updateRefinable(SuggestionCursor suggestion) {
        mRefineable =
                suggestion.isWebSearchSuggestion()
                && mIcon2.getDrawable() == null
                && !TextUtils.isEmpty(suggestion.getSuggestionQuery());
        setRefinable(suggestion, mRefineable);
    }

    protected void setRefinable(SuggestionCursor suggestion, boolean refinable) {
        if (refinable) {
            mIcon2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "Clicked query refine");
                    SuggestionsAdapter adapter =
                            (SuggestionsAdapter) ((ListView) getParent()).getAdapter();
                    adapter.onIcon2Clicked(mPosition);
                }
            });
            mIcon2.setFocusable(true);
            mIcon2.setOnKeyListener(mKeyListener);
            Drawable icon2 = getContext().getResources().getDrawable(R.drawable.edit_query);
            Drawable background =
                    getContext().getResources().getDrawable(R.drawable.edit_query_background);
            setIcon2(icon2, background);
        } else {
            mIcon2.setOnClickListener(null);
            mIcon2.setFocusable(false);
            mIcon2.setOnKeyListener(null);
        }
    }

    private CharSequence formatUrl(CharSequence url) {
        SpannableString text = new SpannableString(url);
        ColorStateList colors = getResources().getColorStateList(R.color.url_text);
        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors, null),
                0, url.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    public Drawable getSuggestionDrawableIcon1(Suggestion suggestion) {
        Source source = suggestion.getSuggestionSource();
        String iconId = suggestion.getSuggestionIcon1();
        Drawable icon1 = iconId == null ? null : source.getIcon(iconId);
        return icon1 == null ? source.getSourceIcon() : icon1;
    }

    public Drawable getSuggestionDrawableIcon2(Suggestion suggestion) {
        Source source = suggestion.getSuggestionSource();
        String iconId = suggestion.getSuggestionIcon2();
        return iconId == null ? null : source.getIcon(iconId);
    }

    private CharSequence formatText(String str, SuggestionCursor suggestion,
                boolean highlightSuggested) {
        boolean isHtml = "html".equals(suggestion.getSuggestionFormat());
        if (isHtml && looksLikeHtml(str)) {
            return Html.fromHtml(str);
        } else if (highlightSuggested && suggestion.isWebSearchSuggestion() &&
                !TextUtils.isEmpty(suggestion.getUserQuery())) {
            return mSuggestionFormatter.formatSuggestion(suggestion.getUserQuery(), str);
        } else {
            return str;
        }
    }

    private boolean looksLikeHtml(String str) {
        if (TextUtils.isEmpty(str)) return false;
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (c == '>' || c == '&') return true;
        }
        return false;
    }

    /**
     * Sets the first text line.
     */
    private void setText1(CharSequence text) {
        mText1.setText(text);
    }

    /**
     * Sets the second text line.
     */
    private void setText2(CharSequence text) {
        mText2.setText(text);
        if (TextUtils.isEmpty(text)) {
            mText2.setVisibility(GONE);
        } else {
            mText2.setVisibility(VISIBLE);
        }
    }

    /**
     * Sets the left-hand-side icon.
     */
    private void setIcon1(Drawable icon) {
        setViewDrawable(mIcon1, icon);
    }

    /**
     * Sets the right-hand-side icon and its background.
     */
    private void setIcon2(Drawable icon, Drawable background) {
        setViewDrawable(mIcon2, icon);
        mIcon2.setBackgroundDrawable(background);
    }

    /**
     * Sets the drawable in an image view, makes sure the view is only visible if there
     * is a drawable.
     */
    private static void setViewDrawable(ImageView v, Drawable drawable) {
        // Set the icon even if the drawable is null, since we need to clear any
        // previous icon.
        v.setImageDrawable(drawable);

        if (drawable == null) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);

            // This is a hack to get any animated drawables (like a 'working' spinner)
            // to animate. You have to setVisible true on an AnimationDrawable to get
            // it to start animating, but it must first have been false or else the
            // call to setVisible will be ineffective. We need to clear up the story
            // about animated drawables in the future, see http://b/1878430.
            drawable.setVisible(false, false);
            drawable.setVisible(true, false);
        }
    }

    protected void fireOnSuggestionQuickContactClicked() {
        if (mClickListener != null) {
            mClickListener.onSuggestionQuickContactClicked(mPosition);
        }
    }

    private class ClickListener implements OnClickListener {
        public void onClick(View v) {
            if (DBG) Log.d(TAG, "onItemClick(" + mPosition + ")");
            if (mClickListener != null) {
                mClickListener.onSuggestionClicked(mPosition);
            }
        }
    }

    private class LongClickListener implements OnLongClickListener {
        public boolean onLongClick(View v) {
            if (DBG) Log.d(TAG, "onItemLongClick(" + mPosition + ")");
            if (mClickListener != null) {
                return mClickListener.onSuggestionLongClicked(mPosition);
            }
            return false;
        }
    }

    private class KeyListener implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean consumed = false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && v != mIcon2) {
                    consumed = mIcon2.requestFocus();
                    if (DBG) Log.d(TAG, "onKey Icon2 accepted focus: " + consumed);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v == mIcon2) {
                    consumed = requestFocus();
                    if (DBG) Log.d(TAG, "onKey SuggestionView accepted focus: " + consumed);
                }
            }
            return consumed;
        }
    }

}
