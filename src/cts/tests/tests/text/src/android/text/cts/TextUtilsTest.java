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

package android.text.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.AndroidTestCase;
import android.text.GetChars;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.EllipsizeCallback;
import android.text.TextUtils.TruncateAt;
import android.text.style.BackgroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.StringBuilderPrinter;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Test {@link TextUtils}.
 */
@TestTargetClass(TextUtils.class)
public class TextUtilsTest extends AndroidTestCase {
    private static String mEllipsis;
    private int mStart;
    private int mEnd;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEllipsis = getEllipsis();
        resetRange();
    }

    private void resetRange() {
        mStart = -1;
        mEnd = -1;
    }

    /**
     * Get the ellipsis from system.
     * @return the string of ellipsis.
     */
    private String getEllipsis() {
        String text = "xxxxx";
        TextPaint p = new TextPaint();
        float width = p.measureText(text.substring(1));
        String re = TextUtils.ellipsize(text, p, width, TruncateAt.START).toString();
        return re.substring(0, re.indexOf("x"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "commaEllipsize",
        args = {CharSequence.class, TextPaint.class, float.class, String.class, String.class}
    )
    @ToBeFixed(bug = "1688347 ", explanation = "The javadoc for commaEllipsize() " +
            "does not discuss any of the corner cases")
    public void testCommaEllipsize() {
        TextPaint p = new TextPaint();
        String text = "long, string, to, truncate";

        float textWidth = p.measureText("long, 3 plus");
        // avail is shorter than text width for only one item plus the appropriate ellipsis.
        // issue 1688347, the expected result for this case does not be described
        // in the javadoc of commaEllipsize().
        assertEquals("",
                TextUtils.commaEllipsize(text, p, textWidth - 1.4f, "plus 1", "%d plus").toString());
        // avail is long enough for only one item plus the appropriate ellipsis.
        assertEquals("long, 3 plus",
                TextUtils.commaEllipsize(text, p, textWidth, "plus 1", "%d plus").toString());

        // avail is long enough for two item plus the appropriate ellipsis.
        textWidth = p.measureText("long, string, 2 more");
        assertEquals("long, string, 2 more",
                TextUtils.commaEllipsize(text, p, textWidth, "more 1", "%d more").toString());

        // avail is long enough for the whole sentence.
        textWidth = p.measureText("long, string, to, truncate");
        assertEquals("long, string, to, truncate",
                TextUtils.commaEllipsize(text, p, textWidth, "more 1", "%d more").toString());

        // the sentence is extended, avail is NOT long enough for the whole sentence.
        assertEquals("long, string, to, more 1", TextUtils.commaEllipsize(
                text + "-extended", p, textWidth, "more 1", "%d more").toString());

        // exceptional value
        assertEquals("", TextUtils.commaEllipsize(text, p, -1f, "plus 1", "%d plus").toString());

        assertEquals(text, TextUtils.commaEllipsize(
                text, p, Float.MAX_VALUE, "more 1", "%d more").toString());

        assertEquals("long, string, to, null", TextUtils.commaEllipsize(
                text + "-extended", p, textWidth, null, "%d more").toString());

        try {
            TextUtils.commaEllipsize(null, p, textWidth, "plus 1", "%d plus");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // issue 1688347, not clear what is supposed to happen if the text to truncate is null.
        }

        try {
            TextUtils.commaEllipsize(text, null, textWidth, "plus 1", "%d plus");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // issue 1688347, not clear what is supposed to happen if TextPaint is null.
        }

        try {
            TextUtils.commaEllipsize(text, p, textWidth, "plus 1", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // issue 1688347, not clear what is supposed to happen
            // if the string for "%d more" in the current locale is null.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "concat",
        args = {CharSequence[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for concat() is incomplete." +
            "1. doesn't explain @param and @return" +
            "2. doesn't describe the expected result when parameter is empty" +
            "3. doesn't discuss the case that parameter is expectional.")
    public void testConcat() {
        // issue 1695243
        // the javadoc for concat() doesn't describe the expected result when parameter is empty.
        assertEquals("", TextUtils.concat().toString());

        assertEquals("first", TextUtils.concat("first").toString());

        assertEquals("first, second", TextUtils.concat("first", ", ", "second").toString());

        SpannableString string1 = new SpannableString("first");
        SpannableString string2 = new SpannableString("second");
        final String url = "www.test_url.com";
        URLSpan urlSpan = new URLSpan(url);
        string1.setSpan(urlSpan, 0, string1.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.GREEN);
        string2.setSpan(bgColorSpan, 0, string2.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final String comma = ", ";
        Spanned strResult = (Spanned) TextUtils.concat(string1, comma, string2);
        assertEquals(string1.toString() + comma + string2.toString(), strResult.toString());
        Object spans[] = strResult.getSpans(0, strResult.length(), Object.class);
        assertEquals(2, spans.length);
        assertTrue(spans[0] instanceof URLSpan);
        assertEquals(url, ((URLSpan) spans[0]).getURL());
        assertTrue(spans[1] instanceof BackgroundColorSpan);
        assertEquals(Color.GREEN, ((BackgroundColorSpan) spans[1]).getBackgroundColor());
        assertEquals(0, strResult.getSpanStart(urlSpan));
        assertEquals(string1.length() - 1, strResult.getSpanEnd(urlSpan));
        assertEquals(string1.length() + comma.length(), strResult.getSpanStart(bgColorSpan));
        assertEquals(strResult.length() - 1, strResult.getSpanEnd(bgColorSpan));

        assertEquals(string1, TextUtils.concat(string1));

        // issue 1695243, the javadoc for concat() doesn't describe
        // the expected result when parameters are null.
        assertEquals(null, TextUtils.concat((CharSequence) null));

        try {
            TextUtils.concat((CharSequence[]) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "copySpansFrom",
        args = {Spanned.class, int.class, int.class, Class.class, Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1688347", explanation = "the javadoc for copySpansFrom() does not exist.")
    public void testCopySpansFrom() {
        Object[] spans;
        String text = "content";
        SpannableString source1 = new SpannableString(text);
        int midPos = source1.length() / 2;
        final String url = "www.test_url.com";
        URLSpan urlSpan = new URLSpan(url);
        source1.setSpan(urlSpan, 0, midPos, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.GREEN);
        source1.setSpan(bgColorSpan, midPos - 1,
                source1.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // normal test
        SpannableString dest1 = new SpannableString(text);
        TextUtils.copySpansFrom(source1, 0, source1.length(), Object.class, dest1, 0);
        spans = dest1.getSpans(0, dest1.length(), Object.class);
        assertEquals(2, spans.length);
        assertTrue(spans[0] instanceof URLSpan);
        assertEquals(url, ((URLSpan) spans[0]).getURL());
        assertTrue(spans[1] instanceof BackgroundColorSpan);
        assertEquals(Color.GREEN, ((BackgroundColorSpan) spans[1]).getBackgroundColor());
        assertEquals(0, dest1.getSpanStart(urlSpan));
        assertEquals(midPos, dest1.getSpanEnd(urlSpan));
        assertEquals(Spanned.SPAN_INCLUSIVE_INCLUSIVE, dest1.getSpanFlags(urlSpan));
        assertEquals(midPos - 1, dest1.getSpanStart(bgColorSpan));
        assertEquals(source1.length() - 1, dest1.getSpanEnd(bgColorSpan));
        assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, dest1.getSpanFlags(bgColorSpan));

        SpannableString source2 = new SpannableString(text);
        source2.setSpan(urlSpan, 0, source2.length() - 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        SpannableString dest2 = new SpannableString(text);
        TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, dest2, 0);
        spans = dest2.getSpans(0, dest2.length(), Object.class);
        assertEquals(1, spans.length);
        assertTrue(spans[0] instanceof URLSpan);
        assertEquals(url, ((URLSpan) spans[0]).getURL());
        assertEquals(0, dest2.getSpanStart(urlSpan));
        assertEquals(source2.length() - 1, dest2.getSpanEnd(urlSpan));
        assertEquals(Spanned.SPAN_EXCLUSIVE_INCLUSIVE, dest2.getSpanFlags(urlSpan));

        SpannableString dest3 = new SpannableString(text);
        TextUtils.copySpansFrom(source2, 0, source2.length(), BackgroundColorSpan.class, dest3, 0);
        spans = dest3.getSpans(0, dest3.length(), Object.class);
        assertEquals(0, spans.length);
        TextUtils.copySpansFrom(source2, 0, source2.length(), URLSpan.class, dest3, 0);
        spans = dest3.getSpans(0, dest3.length(), Object.class);
        assertEquals(1, spans.length);

        SpannableString dest4 = new SpannableString("short");
        try {
            TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, dest4, 0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        TextUtils.copySpansFrom(source2, 0, dest4.length(), Object.class, dest4, 0);
        spans = dest4.getSpans(0, dest4.length(), Object.class);
        assertEquals(1, spans.length);
        assertEquals(0, dest4.getSpanStart(spans[0]));
        // issue 1688347, not clear the expected result when 'start ~ end' only
        // covered a part of the span.
        assertEquals(dest4.length(), dest4.getSpanEnd(spans[0]));

        SpannableString dest5 = new SpannableString("longer content");
        TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, dest5, 0);
        spans = dest5.getSpans(0, 1, Object.class);
        assertEquals(1, spans.length);

        dest5 = new SpannableString("longer content");
        TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, dest5, 2);
        spans = dest5.getSpans(0, 1, Object.class);
        assertEquals(0, spans.length);
        spans = dest5.getSpans(2, dest5.length(), Object.class);
        assertEquals(1, spans.length);
        try {
            TextUtils.copySpansFrom(source2, 0, source2.length(),
                    Object.class, dest5, dest5.length() - source2.length() + 2);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // issue 1688347, no javadoc about the expected behavior of the exceptional argument.
        // exceptional source start
        SpannableString dest6 = new SpannableString("exceptional test");
        TextUtils.copySpansFrom(source2, -1, source2.length(), Object.class, dest6, 0);
        spans = dest6.getSpans(0, dest6.length(), Object.class);
        assertEquals(1, spans.length);
        dest6 = new SpannableString("exceptional test");
        TextUtils.copySpansFrom(source2, Integer.MAX_VALUE, source2.length() - 1,
                    Object.class, dest6, 0);
        spans = dest6.getSpans(0, dest6.length(), Object.class);
        assertEquals(0, spans.length);

        // exceptional source end
        dest6 = new SpannableString("exceptional test");
        TextUtils.copySpansFrom(source2, 0, -1, Object.class, dest6, 0);
        spans = dest6.getSpans(0, dest6.length(), Object.class);
        assertEquals(0, spans.length);
        TextUtils.copySpansFrom(source2, 0, Integer.MAX_VALUE, Object.class, dest6, 0);
        spans = dest6.getSpans(0, dest6.length(), Object.class);
        assertEquals(1, spans.length);

        // exceptional class kind
        dest6 = new SpannableString("exceptional test");
        TextUtils.copySpansFrom(source2, 0, source2.length(), null, dest6, 0);
        spans = dest6.getSpans(0, dest6.length(), Object.class);
        assertEquals(1, spans.length);

        // exceptional destination offset
        dest6 = new SpannableString("exceptional test");
        try {
            TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, dest6, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        try {
            TextUtils.copySpansFrom(source2, 0, source2.length(),
                    Object.class, dest6, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }

        // exceptional source
        try {
            TextUtils.copySpansFrom(null, 0, source2.length(), Object.class, dest6, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expect
        }

        // exceptional destination
        try {
            TextUtils.copySpansFrom(source2, 0, source2.length(), Object.class, null, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expect
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ellipsize",
        args = {CharSequence.class, TextPaint.class, float.class, TruncateAt.class}
    )
    @ToBeFixed(bug = "1688347", explanation = "" +
            "1. the javadoc for ellipsize() is incomplete." +
            "   - doesn't explain @param and @return" +
            "   - doesn't describe expected behavior if user pass an exceptional argument." +
            "2. ellipsize() is not defined for TruncateAt.MARQUEE. " +
            "   In the code it looks like this does the same as MIDDLE. " +
            "   In other methods, MARQUEE is equivalent to END, except for the first line.")
    public void testEllipsize() {
        TextPaint p = new TextPaint();
        
        // turn off kerning. with kerning enabled, different methods of measuring the same text
        // produce different results.
        p.setFlags(p.getFlags() & ~p.DEV_KERN_TEXT_FLAG);
        
        CharSequence text = "long string to truncate";

        float textWidth = p.measureText(mEllipsis + "uncate");
        assertEquals(mEllipsis + "uncate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.START).toString());

        textWidth = p.measureText("long str" + mEllipsis);
        assertEquals("long str" + mEllipsis,
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.END).toString());

        textWidth = p.measureText("long" + mEllipsis + "ate");
        assertEquals("long" + mEllipsis + "ate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE).toString());

        // issue 1688347, ellipsize() is not defined for TruncateAt.MARQUEE.
        // In the code it looks like this does the same as MIDDLE.
        // In other methods, MARQUEE is equivalent to END, except for the first line.
        assertEquals("long" + mEllipsis + "ate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MARQUEE).toString());

        textWidth = p.measureText(mEllipsis);
        assertEquals(mEllipsis, TextUtils.ellipsize(text, p, textWidth, TruncateAt.END).toString());
        assertEquals("", TextUtils.ellipsize(text, p, textWidth - 1, TruncateAt.END).toString());
        assertEquals("", TextUtils.ellipsize(text, p, -1f, TruncateAt.END).toString());
        assertEquals(text,
                TextUtils.ellipsize(text, p, Float.MAX_VALUE, TruncateAt.END).toString());

        assertEquals(mEllipsis,
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.START).toString());
        assertEquals(mEllipsis,
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE).toString());

        try {
            TextUtils.ellipsize(text, null, textWidth, TruncateAt.MIDDLE);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            TextUtils.ellipsize(null, p, textWidth, TruncateAt.MIDDLE);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ellipsize",
        args = {CharSequence.class, TextPaint.class, float.class, TruncateAt.class,
                boolean.class, EllipsizeCallback.class}
    )
    @ToBeFixed(bug = "1688347", explanation = "" +
            "1. the javadoc for ellipsize() is incomplete." +
            "   - doesn't explain @param and @return" +
            "   - doesn't describe expected behavior if user pass an exceptional argument." +
            "2. ellipsize() is not defined for TruncateAt.MARQUEE. " +
            "   In the code it looks like this does the same as MIDDLE. " +
            "   In other methods, MARQUEE is equivalent to END, except for the first line.")
    public void testEllipsizeCallback() {
        TextPaint p = new TextPaint();

        // turn off kerning. with kerning enabled, different methods of measuring the same text
        // produce different results.
        p.setFlags(p.getFlags() & ~p.DEV_KERN_TEXT_FLAG);
        
        TextUtils.EllipsizeCallback callback = new TextUtils.EllipsizeCallback() {
            public void ellipsized(final int start, final int end) {
                mStart = start;
                mEnd = end;
            }
        };

        String text = "long string to truncate";

        // TruncateAt.START, does not specify preserveLength
        resetRange();
        float textWidth = p.measureText(mEllipsis + "uncate");
        assertEquals(mEllipsis + "uncate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.START, false,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length() - "uncate".length(), mEnd);

        // TruncateAt.START, specify preserveLength
        resetRange();
        int ellipsisNum = text.length() - "uncate".length();
        assertEquals(getBlankString(true, ellipsisNum) + "uncate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.START, true,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length() - "uncate".length(), mEnd);

        // TruncateAt.END, specify preserveLength
        resetRange();
        textWidth = p.measureText("long str" + mEllipsis);
        ellipsisNum = text.length() - "long str".length();
        assertEquals("long str" + getBlankString(true, ellipsisNum),
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.END, true, callback).toString());
        assertEquals("long str".length(), mStart);
        assertEquals(text.length(), mEnd);

        // TruncateAt.MIDDLE, specify preserveLength
        resetRange();
        textWidth = p.measureText("long" + mEllipsis + "ate");
        ellipsisNum = text.length() - "long".length() - "ate".length();
        assertEquals("long" + getBlankString(true, ellipsisNum) + "ate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE, true,
                        callback).toString());
        assertEquals("long".length(), mStart);
        assertEquals(text.length() - "ate".length(), mEnd);

        // TruncateAt.MIDDLE, specify preserveLength, but does not specify callback.
        resetRange();
        assertEquals("long" + getBlankString(true, ellipsisNum) + "ate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE, true,
                        null).toString());
        assertEquals(-1, mStart);
        assertEquals(-1, mEnd);

        // TruncateAt.MARQUEE, specify preserveLength
        // issue 1688347, ellipsize() is not defined for TruncateAt.MARQUEE.
        // In the code it looks like this does the same as MIDDLE.
        // In other methods, MARQUEE is equivalent to END, except for the first line.
        resetRange();
        textWidth = p.measureText("long" + mEllipsis + "ate");
        ellipsisNum = text.length() - "long".length() - "ate".length();
        assertEquals("long" + getBlankString(true, ellipsisNum) + "ate",
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.MARQUEE, true,
                        callback).toString());
        assertEquals("long".length(), mStart);
        assertEquals(text.length() - "ate".length(), mEnd);

        // avail is not long enough for ELLIPSIS, and preserveLength is specified.
        resetRange();
        textWidth = p.measureText(mEllipsis);
        assertEquals(getBlankString(false, text.length()),
                TextUtils.ellipsize(text, p, textWidth - 1f, TruncateAt.END, true,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length(), mEnd);

        // avail is not long enough for ELLIPSIS, and preserveLength doesn't be specified.
        resetRange();
        assertEquals("",
                TextUtils.ellipsize(text, p, textWidth - 1f, TruncateAt.END, false,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length(), mEnd);

        // avail is long enough for ELLIPSIS, and preserveLength is specified.
        resetRange();
        assertEquals(getBlankString(true, text.length()),
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.END, true, callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length(), mEnd);

        // avail is long enough for ELLIPSIS, and preserveLength doesn't be specified.
        resetRange();
        assertEquals(mEllipsis,
                TextUtils.ellipsize(text, p, textWidth, TruncateAt.END, false,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(text.length(), mEnd);

        // avail is long enough for the whole sentence.
        resetRange();
        assertEquals(text,
                TextUtils.ellipsize(text, p, Float.MAX_VALUE, TruncateAt.END, true,
                        callback).toString());
        assertEquals(0, mStart);
        assertEquals(0, mEnd);

        textWidth = p.measureText("long str" + mEllipsis);
        try {
            TextUtils.ellipsize(text, null, textWidth, TruncateAt.END, true, callback);
        } catch (NullPointerException e) {
            // expected
        }

        try {
            TextUtils.ellipsize(null, p, textWidth, TruncateAt.END, true, callback);
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * Get a blank string which is filled up by '\uFEFF'.
     *
     * @param isNeedStart - boolean whether need to start with char '\u2026' in the string.
     * @param len - int length of string.
     * @return a blank string which is filled up by '\uFEFF'.
     */
    private String getBlankString(boolean isNeedStart, int len) {
        StringBuilder buf = new StringBuilder();

        int i = 0;
        if (isNeedStart) {
            buf.append('\u2026');
            i++;
        }
        for (; i < len; i++) {
            buf.append('\uFEFF');
        }

        return buf.toString();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {CharSequence.class, CharSequence.class}
    )
    public void testEquals() {
        // compare with itself.
        // String is a subclass of CharSequence and overrides equals().
        String string = "same object";
        assertTrue(TextUtils.equals(string, string));

        // SpannableString is a subclass of CharSequence and does NOT override equals().
        SpannableString spanString = new SpannableString("same object");
        final String url = "www.test_url.com";
        spanString.setSpan(new URLSpan(url), 0, spanString.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        assertTrue(TextUtils.equals(spanString, spanString));

        // compare with other objects which have same content.
        assertTrue(TextUtils.equals("different object", "different object"));

        SpannableString urlSpanString = new SpannableString("same content");
        SpannableString bgColorSpanString = new SpannableString(
                "same content");
        URLSpan urlSpan = new URLSpan(url);
        urlSpanString.setSpan(urlSpan, 0, urlSpanString.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.GREEN);
        bgColorSpanString.setSpan(bgColorSpan, 0, bgColorSpanString.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        assertTrue(TextUtils.equals(bgColorSpanString, urlSpanString));

        // compare with other objects which have different content.
        assertFalse(TextUtils.equals("different content A", "different content B"));
        assertFalse(TextUtils.equals(spanString, urlSpanString));
        assertFalse(TextUtils.equals(spanString, bgColorSpanString));

        // compare with null
        assertTrue(TextUtils.equals(null, null));
        assertFalse(TextUtils.equals(spanString, null));
        assertFalse(TextUtils.equals(null, string));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "expandTemplate",
        args = {CharSequence.class, CharSequence[].class}
    )
    @ToBeFixed(bug = "1695243", explanation =
            "the javadoc for expandTemplate() is incomplete." +
            "1. not clear what is supposed to happen if template or values is null." +
            "2. doesn't discuss the case that ^0 in template string.")
    public void testExpandTemplate() {
        // ^1 at the start of template string.
        assertEquals("value1 template to be expanded",
                TextUtils.expandTemplate("^1 template to be expanded", "value1").toString());
        // ^1 at the end of template string.
        assertEquals("template to be expanded value1",
                TextUtils.expandTemplate("template to be expanded ^1", "value1").toString());
        // ^1 in the middle of template string.
        assertEquals("template value1 to be expanded",
                TextUtils.expandTemplate("template ^1 to be expanded", "value1").toString());
        // ^1 followed by a '0'
        assertEquals("template value10 to be expanded",
                TextUtils.expandTemplate("template ^10 to be expanded", "value1").toString());
        // ^1 followed by a 'a'
        assertEquals("template value1a to be expanded",
                TextUtils.expandTemplate("template ^1a to be expanded", "value1").toString());
        // no ^1
        assertEquals("template ^a to be expanded",
                TextUtils.expandTemplate("template ^a to be expanded", "value1").toString());
        assertEquals("template to be expanded",
                TextUtils.expandTemplate("template to be expanded", "value1").toString());
        // two consecutive ^ in the input to produce a single ^ in the output.
        assertEquals("template ^ to be expanded",
                TextUtils.expandTemplate("template ^^ to be expanded", "value1").toString());
        // two ^ with a space in the middle.
        assertEquals("template ^ ^ to be expanded",
                TextUtils.expandTemplate("template ^ ^ to be expanded", "value1").toString());
        // ^1 follow a '^'
        assertEquals("template ^1 to be expanded",
                TextUtils.expandTemplate("template ^^1 to be expanded", "value1").toString());
        // ^1 followed by a '^'
        assertEquals("template value1^ to be expanded",
                TextUtils.expandTemplate("template ^1^ to be expanded", "value1").toString());

        // 9 replacement values
        final int MAX_SUPPORTED_VALUES_NUM = 9;
        CharSequence values[] = createCharSequenceArray(MAX_SUPPORTED_VALUES_NUM);
        String expected = "value1 value2 template value3 value4 to value5 value6" +
                " be value7 value8 expanded value9";
        String template = "^1 ^2 template ^3 ^4 to ^5 ^6 be ^7 ^8 expanded ^9";
        assertEquals(expected, TextUtils.expandTemplate(template, values).toString());

        //  only up to 9 replacement values are supported
        values = createCharSequenceArray(MAX_SUPPORTED_VALUES_NUM + 1);
        try {
            TextUtils.expandTemplate(template, values);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expect
        }

        // template string is ^0
        try {
            TextUtils.expandTemplate("template ^0 to be expanded", "value1");
        } catch (IllegalArgumentException e) {
            // issue 1695243, doesn't discuss the case that ^0 in template string.
        }

        // template string is ^0
        try {
            TextUtils.expandTemplate("template ^0 to be expanded");
        } catch (IllegalArgumentException e) {
            // issue 1695243, doesn't discuss the case that ^0 in template string.
        }

        // the template requests 2 values but only 1 is provided
        try {
            TextUtils.expandTemplate("template ^2 to be expanded", "value1");
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expect
        }

        // values is null
        try {
            TextUtils.expandTemplate("template ^2 to be expanded", (CharSequence[]) null);
        } catch (NullPointerException e) {
            // expected
        }

        // the template requests 2 values but only one null value is provided
        try {
            TextUtils.expandTemplate("template ^2 to be expanded", (CharSequence) null);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expect
        }

        // the template requests 2 values and 2 values is provided, but all values are null.
        try {
            TextUtils.expandTemplate("template ^2 to be expanded",
                    (CharSequence) null, (CharSequence) null);
        } catch (NullPointerException e) {
            // expected
        }

        // the template requests 2 values but no value is provided.
        try {
            TextUtils.expandTemplate("template ^2 to be expanded");
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // template is null
        try {
            TextUtils.expandTemplate(null, "value1");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * Create a char sequence array with the specified length
     * @param len the length of the array
     * @return The char sequence array with the specified length.
     * The value of each item is "value[index+1]"
     */
    private CharSequence[] createCharSequenceArray(int len) {
        CharSequence array[] = new CharSequence[len];

        for (int i = 0; i < len; i++) {
            array[i] = "value" + (i + 1);
        }

        return array;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChars",
        args = {CharSequence.class, int.class, int.class, char[].class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getChars() does not exist.")
    public void testGetChars() {
        char[] destOriginal = "destination".toCharArray();
        char[] destResult = destOriginal.clone();

        // check whether GetChars.getChars() is called and with the proper parameters.
        MockGetChars mockGetChars = new MockGetChars();
        int start = 1;
        int end = destResult.length;
        int destOff = 2;
        TextUtils.getChars(mockGetChars, start, end, destResult, destOff);
        assertTrue(mockGetChars.hasCalledGetChars());
        assertEquals(start, mockGetChars.ReadGetCharsParams().start);
        assertEquals(end, mockGetChars.ReadGetCharsParams().end);
        assertEquals(destResult, mockGetChars.ReadGetCharsParams().dest);
        assertEquals(destOff, mockGetChars.ReadGetCharsParams().destoff);

        // use MockCharSequence to do the test includes corner cases.
        MockCharSequence mockCharSequence = new MockCharSequence("source string mock");
        // get chars to place at the beginning of the destination except the latest one char.
        destResult = destOriginal.clone();
        start = 0;
        end = destResult.length - 1;
        destOff = 0;
        TextUtils.getChars(mockCharSequence, start, end, destResult, destOff);
        // chars before end are copied from the mockCharSequence.
        for (int i = 0; i < end - start; i++) {
            assertEquals(mockCharSequence.charAt(start + i), destResult[destOff + i]);
        }
        // chars after end doesn't be changed.
        for (int i = destOff + (end - start); i < destOriginal.length; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }

        // get chars to place at the end of the destination except the earliest two chars.
        destResult = destOriginal.clone();
        start = 0;
        end = destResult.length - 2;
        destOff = 2;
        TextUtils.getChars(mockCharSequence, start, end, destResult, destOff);
        // chars before start doesn't be changed.
        for (int i = 0; i < destOff; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }
        // chars after start are copied from the mockCharSequence.
        for (int i = 0; i < end - start; i++) {
            assertEquals(mockCharSequence.charAt(start + i), destResult[destOff + i]);
        }

        // get chars to place at the end of the destination except the earliest two chars
        // and the latest one word.
        destResult = destOriginal.clone();
        start = 1;
        end = destResult.length - 2;
        destOff = 0;
        TextUtils.getChars(mockCharSequence, start, end, destResult, destOff);
        for (int i = 0; i < destOff; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }
        for (int i = 0; i < end - start; i++) {
            assertEquals(mockCharSequence.charAt(start + i), destResult[destOff + i]);
        }
        for (int i = destOff + (end - start); i < destOriginal.length; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }

        // get chars to place the whole of the destination
        destResult = destOriginal.clone();
        start = 0;
        end = destResult.length;
        destOff = 0;
        TextUtils.getChars(mockCharSequence, start, end, destResult, destOff);
        for (int i = 0; i < end - start; i++) {
            assertEquals(mockCharSequence.charAt(start + i), destResult[destOff + i]);
        }

        // exceptional start.
        end = 2;
        destOff = 0;
        destResult = destOriginal.clone();
        try {
            TextUtils.getChars(mockCharSequence, -1, end, destResult, destOff);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        destResult = destOriginal.clone();
        TextUtils.getChars(mockCharSequence, Integer.MAX_VALUE, end, destResult, destOff);
        for (int i = 0; i < destResult.length; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }

        // exceptional end.
        destResult = destOriginal.clone();
        start = 0;
        destOff = 0;
        try {
            TextUtils.getChars(mockCharSequence, start, destResult.length + 1, destResult, destOff);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        destResult = destOriginal.clone();
        TextUtils.getChars(mockCharSequence, start, -1, destResult, destOff);
        for (int i = 0; i < destResult.length; i++) {
            assertEquals(destOriginal[i], destResult[i]);
        }

        // exceptional destOff.
        destResult = destOriginal.clone();
        start = 0;
        end = 2;
        try {
            TextUtils.getChars(mockCharSequence, start, end, destResult, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        try {
            TextUtils.getChars(mockCharSequence, start, end, destResult, Integer.MIN_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }

        // exceptional source
        start = 0;
        end = 2;
        destOff =0;
        try {
            TextUtils.getChars(null, start, end, destResult, destOff);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }

        // exceptional destination
        try {
            TextUtils.getChars(mockCharSequence, start, end, null, destOff);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * MockGetChars for test.
     */
    private class MockGetChars implements GetChars {
        private boolean mHasCalledGetChars;
        private GetCharsParams mGetCharsParams = new GetCharsParams();

        class GetCharsParams {
            int start;
            int end;
            char[] dest;
            int destoff;
        }

        public boolean hasCalledGetChars() {
            return mHasCalledGetChars;
        }

        public void reset() {
            mHasCalledGetChars = false;
        }

        public GetCharsParams ReadGetCharsParams() {
            return mGetCharsParams;
        }

        public void getChars(int start, int end, char[] dest, int destoff) {
            mHasCalledGetChars = true;
            mGetCharsParams.start = start;
            mGetCharsParams.end = end;
            mGetCharsParams.dest = dest;
            mGetCharsParams.destoff = destoff;
        }

        public char charAt(int arg0) {
            return 0;
        }

        public int length() {
            return 100;
        }

        public CharSequence subSequence(int arg0, int arg1) {
            return null;
        }
    }

    /**
     * MockCharSequence for test.
     */
    private class MockCharSequence implements CharSequence {
        private char mText[];

        public MockCharSequence() {
            this("");
        }

        public MockCharSequence(String text) {
            mText = text.toCharArray();
        }

        public char charAt(int arg0) {
            if (arg0 >= 0 && arg0 < mText.length) {
                return mText[arg0];
            }
            throw new IndexOutOfBoundsException();
        }

        public int length() {
            return mText.length;
        }

        public CharSequence subSequence(int arg0, int arg1) {
            return null;
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOffsetAfter",
        args = {CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getOffsetAfter() does not exist.")
    public void testGetOffsetAfter() {
        // the first '\uD800' is index 9, the second 'uD800' is index 16
        // the '\uDBFF' is index 26
        final int POS_FIRST_D800 = 9;       // the position of the first '\uD800'.
        final int POS_SECOND_D800 = 16;
        final int POS_FIRST_DBFF = 26;
        final int SUPPLEMENTARY_CHARACTERS_OFFSET = 2;  // the offset for a supplementary characters
        final int NORMAL_CHARACTERS_OFFSET = 1;
        SpannableString text = new SpannableString(
                "string to\uD800\uDB00 get \uD800\uDC00 offset \uDBFF\uDFFF after");
        assertEquals(0 + 1, TextUtils.getOffsetAfter(text, 0));
        assertEquals(text.length(), TextUtils.getOffsetAfter(text, text.length()));
        assertEquals(text.length(), TextUtils.getOffsetAfter(text, text.length() - 1));
        assertEquals(POS_FIRST_D800 + NORMAL_CHARACTERS_OFFSET,
                TextUtils.getOffsetAfter(text, POS_FIRST_D800));
        assertEquals(POS_SECOND_D800 + SUPPLEMENTARY_CHARACTERS_OFFSET,
                TextUtils.getOffsetAfter(text, POS_SECOND_D800));
        assertEquals(POS_FIRST_DBFF + SUPPLEMENTARY_CHARACTERS_OFFSET,
                TextUtils.getOffsetAfter(text, POS_FIRST_DBFF));

        // the CharSequence string has a span.
        MockReplacementSpan mockReplacementSpan = new MockReplacementSpan();
        text.setSpan(mockReplacementSpan, POS_FIRST_D800 - 1, text.length() - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        assertEquals(text.length() - 1, TextUtils.getOffsetAfter(text, POS_FIRST_D800));

        try {
            TextUtils.getOffsetAfter(text, -1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.getOffsetAfter(text, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.getOffsetAfter(null, 0);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * MockReplacementSpan for test.
     */
    private class MockReplacementSpan extends ReplacementSpan {
        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
                int y, int bottom, Paint paint) {
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
            return 0;
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOffsetBefore",
        args = {CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getOffsetBefore() does not exist.")
    public void testGetOffsetBefore() {
        // the first '\uDC00' is index 10, the second 'uDC00' is index 17
        // the '\uDFFF' is index 27
        final int POS_FIRST_DC00 = 10;
        final int POS_SECOND_DC00 = 17;
        final int POS_FIRST_DFFF = 27;
        final int SUPPLYMENTARY_CHARACTERS_OFFSET = 2;
        final int NORMAL_CHARACTERS_OFFSET = 1;
        SpannableString text = new SpannableString(
                "string to\uD700\uDC00 get \uD800\uDC00 offset \uDBFF\uDFFF before");
        assertEquals(0, TextUtils.getOffsetBefore(text, 0));
        assertEquals(0, TextUtils.getOffsetBefore(text, 1));
        assertEquals(text.length() - 1, TextUtils.getOffsetBefore(text, text.length()));
        assertEquals(POS_FIRST_DC00 + 1 - NORMAL_CHARACTERS_OFFSET,
                TextUtils.getOffsetBefore(text, POS_FIRST_DC00 + 1));
        assertEquals(POS_SECOND_DC00 + 1 - SUPPLYMENTARY_CHARACTERS_OFFSET,
                TextUtils.getOffsetBefore(text, POS_SECOND_DC00 + 1));
        assertEquals(POS_FIRST_DFFF + 1 - SUPPLYMENTARY_CHARACTERS_OFFSET,
                TextUtils.getOffsetBefore(text, POS_FIRST_DFFF + 1));

        // the CharSequence string has a span.
        MockReplacementSpan mockReplacementSpan = new MockReplacementSpan();
        text.setSpan(mockReplacementSpan, 0, POS_FIRST_DC00 + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        assertEquals(0, TextUtils.getOffsetBefore(text, POS_FIRST_DC00));

        try {
            TextUtils.getOffsetBefore(text, -1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.getOffsetBefore(text, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.getOffsetBefore(null, POS_FIRST_DC00);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getReverse",
        args = {CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getReverse() does not exist.")
    public void testGetReverse() {
        String source = "string to be reversed";
        assertEquals("gnirts", TextUtils.getReverse(source, 0, "string".length()).toString());
        assertEquals("desrever",
                TextUtils.getReverse(source, source.length() - "reversed".length(),
                        source.length()).toString());
        assertEquals("", TextUtils.getReverse(source, 0, 0).toString());

        // issue 1695243, exception is thrown after the result of some cases
        // convert to a string, is this expected?
        CharSequence result = TextUtils.getReverse(source, -1, "string".length());
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        TextUtils.getReverse(source, 0, source.length() + 1);
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        TextUtils.getReverse(source, "string".length(), 0);
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        TextUtils.getReverse(source, 0, Integer.MAX_VALUE);
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        TextUtils.getReverse(source, Integer.MIN_VALUE, "string".length());
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        TextUtils.getReverse(null, 0, "string".length());
        try {
            result.toString();
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTrimmedLength",
        args = {CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for getReverse() is incomplete." +
            "1. doesn't explain @param and @return." +
            "2. doesn't discuss the case that parameter is expectional.")
    public void testGetTrimmedLength() {
        assertEquals("normalstring".length(), TextUtils.getTrimmedLength("normalstring"));
        assertEquals("normal string".length(), TextUtils.getTrimmedLength("normal string"));
        assertEquals("blank before".length(), TextUtils.getTrimmedLength(" \t  blank before"));
        assertEquals("blank after".length(), TextUtils.getTrimmedLength("blank after   \n    "));
        assertEquals("blank both".length(), TextUtils.getTrimmedLength(" \t   blank both  \n "));

        char[] allTrimmedChars = new char[] {
                '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007',
                '\u0008', '\u0009', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015',
                '\u0016', '\u0017', '\u0018', '\u0019', '\u0020'
        };
        assertEquals(0, TextUtils.getTrimmedLength(String.valueOf(allTrimmedChars)));

        try {
            TextUtils.getTrimmedLength(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "htmlEncode",
        args = {String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for htmlEncode() is incomplete." +
            "1. doesn't discuss the case that parameter is expectional.")
    public void testHtmlEncode() {
        assertEquals("&lt;_html_&gt;\\ &amp;&quot;&apos;string&apos;&quot;",
                TextUtils.htmlEncode("<_html_>\\ &\"'string'\""));

         try {
             TextUtils.htmlEncode(null);
             fail("Should throw NullPointerException!");
         } catch (NullPointerException e) {
             // expected
         }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, char.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf1() {
        String searchString = "string to be searched";
        final int INDEX_OF_FIRST_R = 2;     // first occurrence of 'r'
        final int INDEX_OF_FIRST_T = 1;
        final int INDEX_OF_FIRST_D = searchString.length() - 1;

        assertEquals(INDEX_OF_FIRST_T, TextUtils.indexOf(searchString, 't'));
        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(searchString, 'r'));
        assertEquals(INDEX_OF_FIRST_D, TextUtils.indexOf(searchString, 'd'));
        assertEquals(-1, TextUtils.indexOf(searchString, 'f'));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(stringBuffer, 'r'));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(stringBuilder, 'r'));

        MockGetChars mockGetChars = new MockGetChars();
        assertFalse(mockGetChars.hasCalledGetChars());
        TextUtils.indexOf(mockGetChars, 'r');
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(mockCharSequence, 'r'));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, char.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf2() {
        String searchString = "string to be searched";
        final int INDEX_OF_FIRST_R = 2;
        final int INDEX_OF_SECOND_R = 16;

        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(searchString, 'r', 0));
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(searchString, 'r', INDEX_OF_FIRST_R + 1));
        assertEquals(-1, TextUtils.indexOf(searchString, 'r', searchString.length()));
        assertEquals(INDEX_OF_FIRST_R, TextUtils.indexOf(searchString, 'r', Integer.MIN_VALUE));
        assertEquals(2, TextUtils.indexOf(searchString, 'r', Integer.MAX_VALUE));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(stringBuffer, 'r', INDEX_OF_FIRST_R + 1));
        try {
            TextUtils.indexOf(stringBuffer, 'r', Integer.MIN_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        assertEquals(-1, TextUtils.indexOf(stringBuffer, 'r', Integer.MAX_VALUE));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_SECOND_R,
                TextUtils.indexOf(stringBuilder, 'r', INDEX_OF_FIRST_R + 1));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.indexOf(mockGetChars, 'r', INDEX_OF_FIRST_R + 1);
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(mockCharSequence, 'r',
                INDEX_OF_FIRST_R + 1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, char.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf3() {
        String searchString = "string to be searched";
        final int INDEX_OF_FIRST_R = 2;
        final int INDEX_OF_SECOND_R = 16;

        assertEquals(INDEX_OF_FIRST_R,
                TextUtils.indexOf(searchString, 'r', 0, searchString.length()));
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(searchString, 'r',
                INDEX_OF_FIRST_R + 1, searchString.length()));
        assertEquals(-1, TextUtils.indexOf(searchString, 'r',
                INDEX_OF_FIRST_R + 1, INDEX_OF_SECOND_R));

        try {
            TextUtils.indexOf(searchString, 'r', Integer.MIN_VALUE, INDEX_OF_SECOND_R);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        assertEquals(-1,
                TextUtils.indexOf(searchString, 'r', Integer.MAX_VALUE, INDEX_OF_SECOND_R));
        assertEquals(-1, TextUtils.indexOf(searchString, 'r', 0, Integer.MIN_VALUE));
        try {
            TextUtils.indexOf(searchString, 'r', 0, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(stringBuffer, 'r',
                INDEX_OF_FIRST_R + 1, searchString.length()));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(stringBuilder, 'r',
                INDEX_OF_FIRST_R + 1, searchString.length()));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.indexOf(mockGetChars, 'r', INDEX_OF_FIRST_R + 1, searchString.length());
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_SECOND_R, TextUtils.indexOf(mockCharSequence, 'r',
                INDEX_OF_FIRST_R + 1, searchString.length()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf4() {
        String searchString = "string to be searched by string";
        final int SEARCH_INDEX = 13;

        assertEquals(0, TextUtils.indexOf(searchString, "string"));
        assertEquals(SEARCH_INDEX, TextUtils.indexOf(searchString, "search"));
        assertEquals(-1, TextUtils.indexOf(searchString, "tobe"));
        assertEquals(0, TextUtils.indexOf(searchString, ""));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(SEARCH_INDEX, TextUtils.indexOf(stringBuffer, "search"));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(SEARCH_INDEX, TextUtils.indexOf(stringBuilder, "search"));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.indexOf(mockGetChars, "search");
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(SEARCH_INDEX, TextUtils.indexOf(mockCharSequence, "search"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf5() {
        String searchString = "string to be searched by string";
        final int INDEX_OF_FIRST_STRING = 0;
        final int INDEX_OF_SECOND_STRING = 25;

        assertEquals(INDEX_OF_FIRST_STRING, TextUtils.indexOf(searchString, "string", 0));
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(searchString, "string",
                INDEX_OF_FIRST_STRING + 1));
        assertEquals(-1, TextUtils.indexOf(searchString, "string", INDEX_OF_SECOND_STRING + 1));
        assertEquals(INDEX_OF_FIRST_STRING, TextUtils.indexOf(searchString, "string",
                Integer.MIN_VALUE));
        assertEquals(0, TextUtils.indexOf(searchString, "string", Integer.MAX_VALUE));

        assertEquals(1, TextUtils.indexOf(searchString, "", 1));
        assertEquals(Integer.MAX_VALUE, TextUtils.indexOf(searchString, "", Integer.MAX_VALUE));

        assertEquals(0, TextUtils.indexOf(searchString, searchString, 0));
        assertEquals(-1, TextUtils.indexOf(searchString, searchString + "longer needle", 0));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuffer, "string",
                INDEX_OF_FIRST_STRING + 1));
        try {
            TextUtils.indexOf(stringBuffer, "string", Integer.MIN_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        assertEquals(-1, TextUtils.indexOf(stringBuffer, "string", Integer.MAX_VALUE));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuilder, "string",
                INDEX_OF_FIRST_STRING + 1));

        MockGetChars mockGetChars = new MockGetChars();
        assertFalse(mockGetChars.hasCalledGetChars());
        TextUtils.indexOf(mockGetChars, "string", INDEX_OF_FIRST_STRING + 1);
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(mockCharSequence, "string",
                INDEX_OF_FIRST_STRING + 1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "indexOf",
        args = {CharSequence.class, CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for indexOf() does not exist.")
    public void testIndexOf6() {
        String searchString = "string to be searched by string";
        final int INDEX_OF_FIRST_STRING = 0;
        final int INDEX_OF_SECOND_STRING = 25;

        assertEquals(INDEX_OF_FIRST_STRING, TextUtils.indexOf(searchString, "string", 0,
                searchString.length()));
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(searchString, "string",
                INDEX_OF_FIRST_STRING + 1, searchString.length()));
        assertEquals(-1, TextUtils.indexOf(searchString, "string", INDEX_OF_FIRST_STRING + 1,
                INDEX_OF_SECOND_STRING - 1));
        assertEquals(INDEX_OF_FIRST_STRING, TextUtils.indexOf(searchString, "string",
                Integer.MIN_VALUE, INDEX_OF_SECOND_STRING - 1));
        assertEquals(0, TextUtils.indexOf(searchString, "string", Integer.MAX_VALUE,
                INDEX_OF_SECOND_STRING - 1));

        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(searchString, "string",
                INDEX_OF_FIRST_STRING + 1, Integer.MIN_VALUE));
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(searchString, "string",
                INDEX_OF_FIRST_STRING + 1, Integer.MAX_VALUE));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuffer, "string",
                INDEX_OF_FIRST_STRING + 1, searchString.length()));
        try {
            TextUtils.indexOf(stringBuffer, "string", Integer.MIN_VALUE,
                    INDEX_OF_SECOND_STRING - 1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        assertEquals(-1, TextUtils.indexOf(stringBuffer, "string", Integer.MAX_VALUE,
                searchString.length()));
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuffer,
                "string", INDEX_OF_FIRST_STRING + 1, Integer.MIN_VALUE));
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuffer,
                "string", INDEX_OF_FIRST_STRING + 1, Integer.MAX_VALUE));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(stringBuilder, "string",
                INDEX_OF_FIRST_STRING + 1, searchString.length()));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.indexOf(mockGetChars, "string", INDEX_OF_FIRST_STRING + 1, searchString.length());
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_SECOND_STRING, TextUtils.indexOf(mockCharSequence, "string",
                INDEX_OF_FIRST_STRING + 1, searchString.length()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isDigitsOnly",
        args = {CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for isDigitsOnly() is incomplete.")
    public void testIsDigitsOnly() {
        assertFalse(TextUtils.isDigitsOnly("no digit"));
        assertFalse(TextUtils.isDigitsOnly("character and 56 digits"));
        assertTrue(TextUtils.isDigitsOnly("0123456789"));
        assertFalse(TextUtils.isDigitsOnly("1234 56789"));

        try {
            TextUtils.isDigitsOnly(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // issue 1695243, not clear what is supposed result if the CharSequence is null.
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isEmpty",
        args = {CharSequence.class}
    )
    public void testIsEmpty() {
        assertFalse(TextUtils.isEmpty("not empty"));
        assertFalse(TextUtils.isEmpty("    "));
        assertTrue(TextUtils.isEmpty(""));
        assertTrue(TextUtils.isEmpty(null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isGraphic",
        args = {char.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for isGraphic() is incomplete.")
    public void testIsGraphicChar() {
        assertTrue(TextUtils.isGraphic('a'));
        assertTrue(TextUtils.isGraphic("\uBA00"));

        // LINE_SEPARATOR
        assertFalse(TextUtils.isGraphic('\u2028'));

        // PARAGRAPH_SEPARATOR
        assertFalse(TextUtils.isGraphic('\u2029'));

        // CONTROL
        assertFalse(TextUtils.isGraphic('\u0085'));

        // UNASSIGNED
        assertFalse(TextUtils.isGraphic('\u0D00'));

        // SURROGATE
        assertFalse(TextUtils.isGraphic('\uD800'));

        // SPACE_SEPARATOR
        assertFalse(TextUtils.isGraphic('\u0020'));

        try {
            assertFalse(TextUtils.isGraphic((Character) null));
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isGraphic",
        args = {CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for isGraphic() is incomplete.")
    public void testIsGraphicCharSequence() {
        assertTrue(TextUtils.isGraphic("printable characters"));

        assertFalse(TextUtils.isGraphic("\u2028\u2029\u0085\u0D00\uD800\u0020"));

        assertTrue(TextUtils.isGraphic("a\u2028\u2029\u0085\u0D00\uD800\u0020"));

        try {
            TextUtils.isGraphic(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @SuppressWarnings("unchecked")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "join",
        args = {CharSequence.class, Iterable.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for join() is incomplete.")
    public void testJoin1() {
        ArrayList<CharSequence> charTokens = new ArrayList<CharSequence>();
        charTokens.add("string1");
        charTokens.add("string2");
        charTokens.add("string3");
        assertEquals("string1|string2|string3", TextUtils.join("|", charTokens));
        assertEquals("string1; string2; string3", TextUtils.join("; ", charTokens));
        assertEquals("string1string2string3", TextUtils.join("", charTokens));

        // issue 1695243, not clear what is supposed result if the delimiter or tokens are null.
        assertEquals("string1nullstring2nullstring3", TextUtils.join(null, charTokens));
        try {
            TextUtils.join("|", (Iterable) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }

        ArrayList<SpannableString> spannableStringTokens = new ArrayList<SpannableString>();
        spannableStringTokens.add(new SpannableString("span 1"));
        spannableStringTokens.add(new SpannableString("span 2"));
        spannableStringTokens.add(new SpannableString("span 3"));
        assertEquals("span 1;span 2;span 3", TextUtils.join(";", spannableStringTokens));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "join",
        args = {CharSequence.class, Object[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for join() is incomplete.")
    public void testJoin2() {
        CharSequence[] charTokens = new CharSequence[] { "string1", "string2", "string3" };
        assertEquals("string1|string2|string3", TextUtils.join("|", charTokens));
        assertEquals("string1; string2; string3", TextUtils.join("; ", charTokens));
        assertEquals("string1string2string3", TextUtils.join("", charTokens));

        // issue 1695243, not clear what is supposed result if the delimiter or tokens are null.
        assertEquals("string1nullstring2nullstring3", TextUtils.join(null, charTokens));
        try {
            TextUtils.join("|", (Object[]) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }

        SpannableString[] spannableStringTokens = new SpannableString[] {
                new SpannableString("span 1"),
                new SpannableString("span 2"),
                new SpannableString("span 3") };
        assertEquals("span 1;span 2;span 3", TextUtils.join(";", spannableStringTokens));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "lastIndexOf",
        args = {CharSequence.class, char.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for lastIndexOf() does not exist.")
    public void testLastIndexOf1() {
        String searchString = "string to be searched";
        final int INDEX_OF_LAST_R = 16;
        final int INDEX_OF_LAST_T = 7;
        final int INDEX_OF_LAST_D = searchString.length() - 1;

        assertEquals(INDEX_OF_LAST_T, TextUtils.lastIndexOf(searchString, 't'));
        assertEquals(INDEX_OF_LAST_R, TextUtils.lastIndexOf(searchString, 'r'));
        assertEquals(INDEX_OF_LAST_D, TextUtils.lastIndexOf(searchString, 'd'));
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'f'));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_LAST_R, TextUtils.lastIndexOf(stringBuffer, 'r'));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_LAST_R, TextUtils.lastIndexOf(stringBuilder, 'r'));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.lastIndexOf(mockGetChars, 'r');
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_LAST_R, TextUtils.lastIndexOf(mockCharSequence, 'r'));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "lastIndexOf",
        args = {CharSequence.class, char.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for lastIndexOf() does not exist.")
    public void testLastIndexOf2() {
        String searchString = "string to be searched";
        final int INDEX_OF_FIRST_R = 2;
        final int INDEX_OF_SECOND_R = 16;

        assertEquals(INDEX_OF_SECOND_R,
                TextUtils.lastIndexOf(searchString, 'r', searchString.length()));
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'r', 0));
        assertEquals(INDEX_OF_FIRST_R,
                TextUtils.lastIndexOf(searchString, 'r', INDEX_OF_FIRST_R));
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'r', Integer.MIN_VALUE));
        assertEquals(INDEX_OF_SECOND_R,
                TextUtils.lastIndexOf(searchString, 'r', Integer.MAX_VALUE));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_FIRST_R,
                TextUtils.lastIndexOf(stringBuffer, 'r', INDEX_OF_FIRST_R));
        assertEquals(-1, TextUtils.lastIndexOf(stringBuffer, 'r', Integer.MIN_VALUE));
        assertEquals(INDEX_OF_SECOND_R,
                TextUtils.lastIndexOf(stringBuffer, 'r', Integer.MAX_VALUE));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_FIRST_R,
                TextUtils.lastIndexOf(stringBuilder, 'r', INDEX_OF_FIRST_R));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.lastIndexOf(mockGetChars, 'r', INDEX_OF_FIRST_R);
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_FIRST_R,
                TextUtils.lastIndexOf(mockCharSequence, 'r', INDEX_OF_FIRST_R));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "lastIndexOf",
        args = {CharSequence.class, char.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for lastIndexOf() does not exist.")
    public void testLastIndexOf3() {
        String searchString = "string to be searched";
        final int INDEX_OF_FIRST_R = 2;
        final int INDEX_OF_SECOND_R = 16;

        assertEquals(INDEX_OF_SECOND_R, TextUtils.lastIndexOf(searchString, 'r', 0,
                searchString.length()));
        assertEquals(INDEX_OF_FIRST_R, TextUtils.lastIndexOf(searchString, 'r', 0,
                INDEX_OF_SECOND_R - 1));
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'r', 0, INDEX_OF_FIRST_R - 1));

        try {
            TextUtils.lastIndexOf(searchString, 'r', Integer.MIN_VALUE, INDEX_OF_SECOND_R - 1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expect
        }
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'r', Integer.MAX_VALUE,
                INDEX_OF_SECOND_R - 1));
        assertEquals(-1, TextUtils.lastIndexOf(searchString, 'r', 0, Integer.MIN_VALUE));
        assertEquals(INDEX_OF_SECOND_R, TextUtils.lastIndexOf(searchString, 'r', 0,
                Integer.MAX_VALUE));

        StringBuffer stringBuffer = new StringBuffer(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.lastIndexOf(stringBuffer, 'r', 0,
                INDEX_OF_SECOND_R - 1));

        StringBuilder stringBuilder = new StringBuilder(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.lastIndexOf(stringBuilder, 'r', 0,
                INDEX_OF_SECOND_R - 1));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.lastIndexOf(mockGetChars, 'r', 0, INDEX_OF_SECOND_R - 1);
        assertTrue(mockGetChars.hasCalledGetChars());

        MockCharSequence mockCharSequence = new MockCharSequence(searchString);
        assertEquals(INDEX_OF_FIRST_R, TextUtils.lastIndexOf(mockCharSequence, 'r', 0,
                INDEX_OF_SECOND_R - 1));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "regionMatches",
        args = {CharSequence.class, int.class, CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for regionMatches() does not exist.")
    public void testRegionMatches() {
        assertFalse(TextUtils.regionMatches("one", 0, "two", 0, "one".length()));
        assertTrue(TextUtils.regionMatches("one", 0, "one", 0, "one".length()));
        try {
            TextUtils.regionMatches("one", 0, "one", 0, "one".length() + 1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        String one = "Hello Android, hello World!";
        String two = "Hello World";
        // match "Hello"
        assertTrue(TextUtils.regionMatches(one, 0, two, 0, "Hello".length()));

        // match "Hello A" and "Hello W"
        assertFalse(TextUtils.regionMatches(one, 0, two, 0, "Hello A".length()));

        // match "World"
        assertTrue(TextUtils.regionMatches(one, "Hello Android, hello ".length(),
                two, "Hello ".length(), "World".length()));
        assertFalse(TextUtils.regionMatches(one, "Hello Android, hello ".length(),
                two, 0, "World".length()));

        try {
            TextUtils.regionMatches(one, Integer.MIN_VALUE, two, 0, "Hello".length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            TextUtils.regionMatches(one, Integer.MAX_VALUE, two, 0, "Hello".length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.regionMatches(one, 0, two, Integer.MIN_VALUE, "Hello".length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            TextUtils.regionMatches(one, 0, two, Integer.MAX_VALUE, "Hello".length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.regionMatches(one, 0, two, 0, Integer.MIN_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            TextUtils.regionMatches(one, 0, two, 0, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            TextUtils.regionMatches(null, 0, two, 0, "Hello".length());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
        try {
            TextUtils.regionMatches(one, 0, null, 0, "Hello".length());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "replace",
        args = {CharSequence.class, String[].class, CharSequence[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for replace() is incomplete.")
    public void testReplace() {
        String template = "this is a string to be as the template for replacement";

        String sources[] = new String[] { "string" };
        CharSequence destinations[] = new CharSequence[] { "text" };
        SpannableStringBuilder replacedString = (SpannableStringBuilder) TextUtils.replace(template,
                sources, destinations);
        assertEquals("this is a text to be as the template for replacement",
                replacedString.toString());

        sources = new String[] {"is", "the", "for replacement"};
        destinations = new CharSequence[] {"was", "", "to be replaced"};
        replacedString = (SpannableStringBuilder)TextUtils.replace(template, sources, destinations);
        assertEquals("thwas is a string to be as  template to be replaced",
                replacedString.toString());

        sources = new String[] {"is", "for replacement"};
        destinations = new CharSequence[] {"was", "", "to be replaced"};
        replacedString = (SpannableStringBuilder)TextUtils.replace(template, sources, destinations);
        assertEquals("thwas is a string to be as the template ", replacedString.toString());

        sources = new String[] {"is", "the", "for replacement"};
        destinations = new CharSequence[] {"was", "to be replaced"};
        try {
            TextUtils.replace(template, sources, destinations);
            fail("Should throw ArrayIndexOutOfBoundsException!");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.replace(null, sources, destinations);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            TextUtils.replace(template, null, destinations);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            TextUtils.replace(template, sources, null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "split",
        args = {String.class, Pattern.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for split() is incomplete." +
            "1. not clear what is supposed result if the pattern string is empty.")
    public void testSplitPattern() {
        String testString = "abccbadecdebz";
        assertEquals(calculateCharsCount(testString, "c") + 1,
                TextUtils.split(testString, Pattern.compile("c")).length);
        assertEquals(calculateCharsCount(testString, "a") + 1,
                TextUtils.split(testString, Pattern.compile("a")).length);
        assertEquals(calculateCharsCount(testString, "z") + 1,
                TextUtils.split(testString, Pattern.compile("z")).length);
        assertEquals(calculateCharsCount(testString, "de") + 1,
                TextUtils.split(testString, Pattern.compile("de")).length);
        int totalCount = 1 + calculateCharsCount(testString, "a")
                + calculateCharsCount(testString, "b") + calculateCharsCount(testString, "c");
        assertEquals(totalCount,
                TextUtils.split(testString, Pattern.compile("[a-c]")).length);
        assertEquals(0, TextUtils.split("", Pattern.compile("a")).length);
        // issue 1695243, not clear what is supposed result if the pattern string is empty.
        assertEquals(testString.length() + 2,
                TextUtils.split(testString, Pattern.compile("")).length);

        try {
            TextUtils.split(null, Pattern.compile("a"));
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
        try {
            TextUtils.split("abccbadecdebz", (Pattern) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
    }

    /*
     * return the appearance count of searched chars in text.
     */
    private int calculateCharsCount(CharSequence text, CharSequence searches) {
        int count = 0;
        int start = TextUtils.indexOf(text, searches, 0);

        while (start != -1) {
            count++;
            start = TextUtils.indexOf(text, searches, start + 1);
        }
        return count;
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "split",
        args = {String.class, String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for split() is incomplete." +
            "1. not clear what is supposed result if the pattern string is empty.")
    public void testSplitString() {
        String testString = "abccbadecdebz";
        assertEquals(calculateCharsCount(testString, "c") + 1,
                TextUtils.split("abccbadecdebz", "c").length);
        assertEquals(calculateCharsCount(testString, "a") + 1,
                TextUtils.split("abccbadecdebz", "a").length);
        assertEquals(calculateCharsCount(testString, "z") + 1,
                TextUtils.split("abccbadecdebz", "z").length);
        assertEquals(calculateCharsCount(testString, "de") + 1,
                TextUtils.split("abccbadecdebz", "de").length);
        assertEquals(0, TextUtils.split("", "a").length);
        // issue 1695243, not clear what is supposed result if the pattern string is empty.
        assertEquals(testString.length() + 2,
                TextUtils.split("abccbadecdebz", "").length);

        try {
            TextUtils.split(null, "a");
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
        try {
            TextUtils.split("abccbadecdebz", (String) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expect
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "stringOrSpannedString",
        args = {CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for" +
            " stringOrSpannedString() does not exist.")
    public void testStringOrSpannedString() {
        assertNull(TextUtils.stringOrSpannedString(null));

        SpannedString spannedString = new SpannedString("Spanned String");
        assertSame(spannedString, TextUtils.stringOrSpannedString(spannedString));

        SpannableString spannableString = new SpannableString("Spannable String");
        assertEquals("Spannable String",
                TextUtils.stringOrSpannedString(spannableString).toString());
        assertEquals(SpannedString.class,
                TextUtils.stringOrSpannedString(spannableString).getClass());

        StringBuffer stringBuffer = new StringBuffer("String Buffer");
        assertEquals("String Buffer",
                TextUtils.stringOrSpannedString(stringBuffer).toString());
        assertEquals(String.class,
                TextUtils.stringOrSpannedString(stringBuffer).getClass());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "substring",
        args = {CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for substring() is incomplete." +
            "1. doesn't explain @param and @return" +
            "2. not clear what is supposed to happen if source is null." +
            "3. doesn't explain the thrown IndexOutOfBoundsException")
    public void testSubString() {
        String string = "String";
        assertSame(string, TextUtils.substring(string, 0, string.length()));
        assertEquals("Strin", TextUtils.substring(string, 0, string.length() - 1));
        assertEquals("", TextUtils.substring(string, 1, 1));

        try {
            TextUtils.substring(string, string.length(), 0);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.substring(string, -1, string.length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.substring(string, Integer.MAX_VALUE, string.length());
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.substring(string, 0, -1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.substring(string, 0, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            TextUtils.substring(null, 0, string.length());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }

        StringBuffer stringBuffer = new StringBuffer("String Buffer");
        assertEquals("Strin", TextUtils.substring(stringBuffer, 0, string.length() - 1));
        assertEquals("", TextUtils.substring(stringBuffer, 1, 1));

        MockGetChars mockGetChars = new MockGetChars();
        TextUtils.substring(mockGetChars, 0, string.length());
        assertTrue(mockGetChars.hasCalledGetChars());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {CharSequence.class, Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for writeToParcel() is incomplete." +
            "1. doesn't explain @param and @return" +
            "2. not clear is it the supposed result when the CharSequence is null.")
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();

        Parcelable.Creator<CharSequence> creator = TextUtils.CHAR_SEQUENCE_CREATOR;

        String string = "String";
        TextUtils.writeToParcel(string, p, 0);
        p.setDataPosition(0);
        assertEquals(string, creator.createFromParcel(p).toString());
        p.recycle();

        p = Parcel.obtain();
        TextUtils.writeToParcel(null, p, 0);
        p.setDataPosition(0);
        assertNull(creator.createFromParcel(p));
        p.recycle();

        p = Parcel.obtain();
        SpannableString spannableString = new SpannableString("Spannable String");
        URLSpan urlSpan = new URLSpan("URL Span");
        int urlSpanStart = spannableString.length() >> 1;
        int urlSpanEnd = spannableString.length();
        spannableString.setSpan(urlSpan, urlSpanStart, urlSpanEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        TextUtils.writeToParcel(spannableString, p, 0);
        p.setDataPosition(0);
        SpannableString ret = (SpannableString) creator.createFromParcel(p);
        assertEquals("Spannable String", ret.toString());
        Object[] spans = ret.getSpans(0, ret.length(), Object.class);
        assertEquals(1, spans.length);
        assertEquals("URL Span", ((URLSpan) spans[0]).getURL());
        assertEquals(urlSpanStart, ret.getSpanStart(spans[0]));
        assertEquals(urlSpanEnd, ret.getSpanEnd(spans[0]));
        assertEquals(Spanned.SPAN_INCLUSIVE_INCLUSIVE, ret.getSpanFlags(spans[0]));
        p.recycle();

        p = Parcel.obtain();
        ColorStateList colors = new ColorStateList(new int[][] {
                new int[] {android.R.attr.state_focused}, new int[0]},
                new int[] {Color.rgb(0, 255, 0), Color.BLACK});
        int textSize = 20;
        TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(
                null, Typeface.ITALIC, textSize, colors, null);
        int textAppearanceSpanStart = 0;
        int textAppearanceSpanEnd = spannableString.length() >> 1;
        spannableString.setSpan(textAppearanceSpan, textAppearanceSpanStart,
                textAppearanceSpanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        TextUtils.writeToParcel(spannableString, p, -1);
        p.setDataPosition(0);
        ret = (SpannableString) creator.createFromParcel(p);
        assertEquals("Spannable String", ret.toString());
        spans = ret.getSpans(0, ret.length(), Object.class);
        assertEquals(2, spans.length);
        assertEquals("URL Span", ((URLSpan) spans[0]).getURL());
        assertEquals(urlSpanStart, ret.getSpanStart(spans[0]));
        assertEquals(urlSpanEnd, ret.getSpanEnd(spans[0]));
        assertEquals(Spanned.SPAN_INCLUSIVE_INCLUSIVE, ret.getSpanFlags(spans[0]));
        assertEquals(null, ((TextAppearanceSpan) spans[1]).getFamily());

        assertEquals(Typeface.ITALIC, ((TextAppearanceSpan) spans[1]).getTextStyle());
        assertEquals(textSize, ((TextAppearanceSpan) spans[1]).getTextSize());

        assertEquals(colors.toString(), ((TextAppearanceSpan) spans[1]).getTextColor().toString());
        assertEquals(null, ((TextAppearanceSpan) spans[1]).getLinkTextColor());
        assertEquals(textAppearanceSpanStart, ret.getSpanStart(spans[1]));
        assertEquals(textAppearanceSpanEnd, ret.getSpanEnd(spans[1]));
        assertEquals(Spanned.SPAN_INCLUSIVE_EXCLUSIVE, ret.getSpanFlags(spans[1]));
        p.recycle();

        try {
            TextUtils.writeToParcel(spannableString, null, 0);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCapsMode",
        args = {CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1586346", explanation = "return cap mode which is NOT set in reqModes")
    public void testGetCapsMode() {
        final int CAP_MODE_ALL = TextUtils.CAP_MODE_CHARACTERS
                | TextUtils.CAP_MODE_WORDS | TextUtils.CAP_MODE_SENTENCES;
        final int CAP_MODE_CHARACTERS_AND_WORD =
                TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS;
        String testString = "Start. Sentence word!No space before\n\t" +
                "Paragraph? (\"\'skip begin\'\"). skip end";

        // CAP_MODE_SENTENCES should be in effect in the whole text.
        for (int i = 0; i < testString.length(); i++) {
            assertEquals(TextUtils.CAP_MODE_CHARACTERS,
                    TextUtils.getCapsMode(testString, i, TextUtils.CAP_MODE_CHARACTERS));
        }

        // all modes should be in effect at the start of the text.
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, 0, TextUtils.CAP_MODE_WORDS));
        // issue 1586346
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, 0, TextUtils.CAP_MODE_SENTENCES));
        assertEquals(CAP_MODE_CHARACTERS_AND_WORD,
                TextUtils.getCapsMode(testString, 0, CAP_MODE_ALL));

        // all mode should be in effect at the position after "." or "?" or "!" + " ".
        int offset = testString.indexOf("Sentence word!");
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        assertEquals(TextUtils.CAP_MODE_SENTENCES,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        // issue 1586346
        assertEquals(CAP_MODE_CHARACTERS_AND_WORD,
                TextUtils.getCapsMode(testString, 0, CAP_MODE_ALL));

        // CAP_MODE_SENTENCES should NOT be in effect at the position after other words + " ".
        offset = testString.indexOf("word!");
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        assertEquals(0,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        // issue 1586346
        assertEquals(TextUtils.CAP_MODE_CHARACTERS,
                TextUtils.getCapsMode(testString, offset, CAP_MODE_ALL));

        // if no space after "." or "?" or "!", CAP_MODE_SENTENCES and CAP_MODE_WORDS
        // should NOT be in effect.
        offset = testString.indexOf("No space before");
        assertEquals(0,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        assertEquals(0,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        assertEquals(TextUtils.CAP_MODE_CHARACTERS,
                TextUtils.getCapsMode(testString, offset, CAP_MODE_ALL));

        // all mode should be in effect at a beginning of a new paragraph.
        offset = testString.indexOf("Paragraph");
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        // issue 1586346
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        assertEquals(CAP_MODE_CHARACTERS_AND_WORD,
                TextUtils.getCapsMode(testString, offset, CAP_MODE_ALL));

        // some special word which means the start of a sentence should be skipped.
        offset = testString.indexOf("skip begin");
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        assertEquals(TextUtils.CAP_MODE_SENTENCES,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        // issue 1586346
        assertEquals(TextUtils.CAP_MODE_SENTENCES | TextUtils.CAP_MODE_CHARACTERS,
                TextUtils.getCapsMode(testString, offset, CAP_MODE_ALL));

        // some special word which means the end of a sentence should be skipped.
        offset = testString.indexOf("skip end");
        assertEquals(TextUtils.CAP_MODE_WORDS,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_WORDS));
        assertEquals(TextUtils.CAP_MODE_SENTENCES,
                TextUtils.getCapsMode(testString, offset, TextUtils.CAP_MODE_SENTENCES));
        // issue 1586346
        assertEquals(TextUtils.CAP_MODE_SENTENCES | TextUtils.CAP_MODE_CHARACTERS,
                TextUtils.getCapsMode(testString, offset, CAP_MODE_ALL));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCapsMode",
        args = {CharSequence.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for substring() is incomplete." +
            "1. doesn't describe the expected result when parameter is exceptional.")
    public void testGetCapsModeException() {
        String testString = "Start. Sentence word!No space before\n\t" +
                "Paragraph? (\"\'skip begin\'\"). skip end";

        int offset = testString.indexOf("Sentence word!");
        assertEquals(TextUtils.CAP_MODE_CHARACTERS,
                TextUtils.getCapsMode(null, offset, TextUtils.CAP_MODE_CHARACTERS));

        try {
            TextUtils.getCapsMode(null, offset, TextUtils.CAP_MODE_SENTENCES);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals(0, TextUtils.getCapsMode(testString, -1, TextUtils.CAP_MODE_SENTENCES));

        try {
            TextUtils.getCapsMode(testString, testString.length() + 1,
                    TextUtils.CAP_MODE_SENTENCES);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dumpSpans",
        args = {java.lang.CharSequence.class, android.util.Printer.class, java.lang.String.class}
    )
    public void testDumpSpans() {
        StringBuilder builder = new StringBuilder();
        StringBuilderPrinter printer = new StringBuilderPrinter(builder);
        CharSequence source = "test dump spans";
        String prefix = "prefix";

        assertEquals(0, builder.length());
        TextUtils.dumpSpans(source, printer, prefix);
        assertTrue(builder.length() > 0);

        builder = new StringBuilder();
        printer = new StringBuilderPrinter(builder);
        assertEquals(0, builder.length());
        SpannableString spanned = new SpannableString(source);
        spanned.setSpan(new Object(), 0, source.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        TextUtils.dumpSpans(spanned, printer, prefix);
        assertTrue(builder.length() > 0);
    }
}
