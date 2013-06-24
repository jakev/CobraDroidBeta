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

package android.graphics.drawable.cts;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.R.attr;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.test.InstrumentationTestCase;
import android.util.StateSet;
import android.util.Xml;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

@TestTargetClass(StateListDrawable.class)
public class StateListDrawableTest extends InstrumentationTestCase {
    private MockStateListDrawable mStateListDrawable;

    private Resources mResources;

    private DrawableContainerState mDrawableContainerState;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStateListDrawable = new MockStateListDrawable();
        mDrawableContainerState = (DrawableContainerState) mStateListDrawable.getConstantState();
        mResources = getInstrumentation().getTargetContext().getResources();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "StateListDrawable",
        args = {}
    )
    public void testStateListDrawable() {
        new StateListDrawable();
        // Check the values set in the constructor
        assertNotNull(new StateListDrawable().getConstantState());
        assertTrue(new MockStateListDrawable().hasCalledOnStateChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addState",
        args = {int[].class, android.graphics.drawable.Drawable.class}
    )
    public void testAddState() {
        assertEquals(0, mDrawableContainerState.getChildCount());

        // nothing happens if drawable is null
        mStateListDrawable.reset();
        mStateListDrawable.addState(StateSet.WILD_CARD, null);
        assertEquals(0, mDrawableContainerState.getChildCount());
        assertFalse(mStateListDrawable.hasCalledOnStateChanged());

        // call onLevelChanged to assure that the correct drawable is selected.
        mStateListDrawable.reset();
        mStateListDrawable.addState(StateSet.WILD_CARD, new MockDrawable());
        assertEquals(1, mDrawableContainerState.getChildCount());
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());

        mStateListDrawable.reset();
        mStateListDrawable.addState(new int[] { attr.state_focused, - attr.state_selected },
                new MockDrawable());
        assertEquals(2, mDrawableContainerState.getChildCount());
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());

        // call onLevelChanged will not throw NPE here because the first drawable with wild card
        // state is matched first. There is no chance that other drawables will be matched.
        mStateListDrawable.reset();
        mStateListDrawable.addState(null, new MockDrawable());
        assertEquals(3, mDrawableContainerState.getChildCount());
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isStateful",
        args = {}
    )
    public void testIsStateful() {
        assertTrue(new StateListDrawable().isStateful());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onStateChange",
        args = {int[].class}
    )
    public void testOnStateChange() {
        mStateListDrawable.addState(new int[] { attr.state_focused, - attr.state_selected },
                new MockDrawable());
        mStateListDrawable.addState(StateSet.WILD_CARD, new MockDrawable());
        mStateListDrawable.addState(StateSet.WILD_CARD, new MockDrawable());

        // the method is not called if same state is set
        mStateListDrawable.reset();
        mStateListDrawable.setState(mStateListDrawable.getState());
        assertFalse(mStateListDrawable.hasCalledOnStateChanged());

        // the method is called if different state is set
        mStateListDrawable.reset();
        mStateListDrawable.setState(new int[] { attr.state_focused, - attr.state_selected });
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());

        mStateListDrawable.reset();
        mStateListDrawable.setState(null);
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());

        // check that correct drawable is selected.
        mStateListDrawable.onStateChange(new int[] { attr.state_focused, - attr.state_selected });
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);

        assertFalse(mStateListDrawable.onStateChange(new int[] { attr.state_focused }));
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);

        assertTrue(mStateListDrawable.onStateChange(StateSet.WILD_CARD));
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[1]);

        // null state will match the wild card
        assertFalse(mStateListDrawable.onStateChange(null));
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[1]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "If drawable with wild card state is added before any other drawables, "
                + "this drawable is always matched at first",
        method = "onStateChange",
        args = {int[].class}
    )
    public void testOnStateChangeWithWildCardAtFirst() {
        mStateListDrawable.addState(StateSet.WILD_CARD, new MockDrawable());
        mStateListDrawable.addState(new int[] { attr.state_focused, - attr.state_selected },
                new MockDrawable());

        // matches the first wild card although the second one is more accurate
        mStateListDrawable.onStateChange(new int[] { attr.state_focused, - attr.state_selected });
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onStateChange",
        args = {int[].class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "StateListDrawable#onStateChange(int[]) when matching the null state")
    public void testOnStateChangeWithNullStateSet() {
        assertEquals(0, mDrawableContainerState.getChildCount());
        try {
            mStateListDrawable.addState(null, new MockDrawable());
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        assertEquals(1, mDrawableContainerState.getChildCount());

        try {
            mStateListDrawable.onStateChange(StateSet.WILD_CARD);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    public void testInflate() throws XmlPullParserException, IOException {
        XmlResourceParser parser = getResourceParser(R.xml.selector_correct);

        mStateListDrawable.reset();
        mStateListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        // android:visible="false"
        assertFalse(mStateListDrawable.isVisible());
        // android:constantSize="true"
        assertTrue(mDrawableContainerState.isConstantSize());
        // android:variablePadding="true"
        assertNull(mDrawableContainerState.getConstantPadding());
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());
        assertEquals(2, mDrawableContainerState.getChildCount());
        // check the android:state_* by calling setState
        mStateListDrawable.setState(new int[]{ attr.state_focused, - attr.state_pressed });
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);
        mStateListDrawable.setState(StateSet.WILD_CARD);
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[1]);

        mStateListDrawable = new MockStateListDrawable();
        mDrawableContainerState = (DrawableContainerState) mStateListDrawable.getConstantState();
        assertNull(mStateListDrawable.getCurrent());
        mStateListDrawable.reset();
        assertTrue(mStateListDrawable.isVisible());
        parser = getResourceParser(R.xml.selector_missing_selector_attrs);
        mStateListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        // use current the visibility
        assertTrue(mStateListDrawable.isVisible());
        // default value of android:constantSize is false
        assertFalse(mDrawableContainerState.isConstantSize());
        // default value of android:variablePadding is false
        // TODO: behavior of mDrawableContainerState.getConstantPadding() when variablePadding is
        // false is undefined
        //assertNotNull(mDrawableContainerState.getConstantPadding());
        assertTrue(mStateListDrawable.hasCalledOnStateChanged());
        assertEquals(1, mDrawableContainerState.getChildCount());
        mStateListDrawable.setState(new int[]{ - attr.state_pressed, attr.state_focused });
        assertSame(mStateListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);
        mStateListDrawable.setState(StateSet.WILD_CARD);
        assertNull(mStateListDrawable.getCurrent());

        parser = getResourceParser(R.xml.selector_missing_item_drawable);
        try {
            mStateListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if drawable of item is missing");
        } catch (XmlPullParserException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "StateListDrawable#inflate(Resources, XmlPullParser, AttributeSet) when param r,"
            + "parser or attrs is out of bounds")
    public void testInflateWithNullParameters() throws XmlPullParserException, IOException{
        XmlResourceParser parser = getResourceParser(R.xml.level_list_correct);
        try {
            mStateListDrawable.inflate(null, parser, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if resource is null");
        } catch (NullPointerException e) {
        }

        try {
            mStateListDrawable.inflate(mResources, null, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if parser is null");
        } catch (NullPointerException e) {
        }

        try {
            mStateListDrawable.inflate(mResources, parser, null);
            fail("Should throw XmlPullParserException if AttributeSet is null");
        } catch (NullPointerException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "mutate",
        args = {}
    )
    @ToBeFixed(bug = "", explanation = "mutate() always throw NullPointerException.")
    public void testMutate() {
        StateListDrawable d1 =
            (StateListDrawable) mResources.getDrawable(R.drawable.statelistdrawable);
        StateListDrawable d2 =
            (StateListDrawable) mResources.getDrawable(R.drawable.statelistdrawable);
        StateListDrawable d3 =
            (StateListDrawable) mResources.getDrawable(R.drawable.statelistdrawable);

        d1.getCurrent().setAlpha(100);
        assertEquals(100, ((BitmapDrawable) d1.getCurrent()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d2.getCurrent()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d3.getCurrent()).getPaint().getAlpha());

        d1.mutate();

        // TODO: add verification

    }

    private XmlResourceParser getResourceParser(int resId) throws XmlPullParserException,
            IOException {
        XmlResourceParser parser = getInstrumentation().getTargetContext().getResources().getXml(
                resId);
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }
        return parser;
    }

    private class MockStateListDrawable extends StateListDrawable {
        private boolean mHasCalledOnStateChanged;

        public boolean hasCalledOnStateChanged() {
            return mHasCalledOnStateChanged;
        }

        public void reset() {
            mHasCalledOnStateChanged = false;
        }

        @Override
        protected boolean onStateChange(int[] stateSet) {
            boolean result = super.onStateChange(stateSet);
            mHasCalledOnStateChanged = true;
            return result;
        }
    }

    private class MockDrawable extends Drawable {
        @Override
        public void draw(Canvas canvas) {
        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }
}
