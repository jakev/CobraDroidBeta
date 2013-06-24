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

package android.view.animation.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.test.ActivityInstrumentationTestCase2;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;

/**
 * Test {@link Animation}.
 */
@TestTargetClass(Animation.class)
public class AnimationTest extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private static final float ALPHA_DELTA = 0.001f;

    /** It is defined in R.anim.accelerate_alpha */
    private static final long ACCELERATE_ALPHA_DURATION = 1000;

    /** It is defined in R.anim.decelerate_alpha */
    private static final long DECELERATE_ALPHA_DURATION = 2000;

    private Activity mActivity;
    private Object mLockObject = new Object();

    public AnimationTest() {
        super("com.android.cts.stub", AnimationTestStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Animation}",
            method = "Animation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Animation}",
            method = "Animation",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        XmlResourceParser parser = mActivity.getResources().getAnimation(R.anim.alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Animation(mActivity, attrs) {
        };

        new Animation() {
        };
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#ensureInterpolator()}",
            method = "ensureInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getInterpolator()}",
            method = "getInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setInterpolator(Context, int)}",
            method = "setInterpolator",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setInterpolator(Interpolator)}",
            method = "setInterpolator",
            args = {android.view.animation.Interpolator.class}
        )
    })
    @ToBeFixed(bug = "1561186", explanation = "javadoc of setInterpolator(Interpolator) " +
            "conflicts with ensureInterpolator()'s. can not get the specific default " +
            "Interpolator type")
    public void testAccessInterpolator() {
        // check default interpolator
        MyAnimation myAnimation = new MyAnimation();
        Interpolator interpolator = myAnimation.getInterpolator();
        assertTrue(interpolator instanceof AccelerateDecelerateInterpolator); // issue 1561186.

        myAnimation.ensureInterpolator();
        assertSame(interpolator, myAnimation.getInterpolator());

        myAnimation.setInterpolator(null);
        assertNull(myAnimation.getInterpolator());
        myAnimation.ensureInterpolator();
        interpolator = myAnimation.getInterpolator();
        assertTrue(interpolator instanceof AccelerateDecelerateInterpolator);

        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        interpolator = animation.getInterpolator();
        assertNotNull(interpolator);
        assertTrue(interpolator instanceof DecelerateInterpolator);

        animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        interpolator = animation.getInterpolator();
        assertNotNull(interpolator);
        assertTrue(interpolator instanceof AccelerateInterpolator);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "check default fillAfter",
            method = "getFillAfter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "check default fillBefore",
            method = "getFillBefore",
            args = {}
        )
    })
    public void testDefaultFill() {
        Animation animation = new Animation() {
        };
        assertTrue(animation.getFillBefore());
        assertFalse(animation.getFillAfter());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getFillAfter()}",
            method = "getFillAfter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setFillAfter(boolean)}",
            method = "setFillAfter",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getFillBefore()}",
            method = "getFillBefore",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setFillBefore(boolean)}",
            method = "setFillBefore",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#isFillEnabled()}",
            method = "isFillEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setFillEnabled(boolean)}",
            method = "setFillEnabled",
            args = {boolean.class}
        )
    })
    @ToBeFixed(bug = "1698355", explanation = "When isFillEnabled() is false," +
            " it's the same as setting fillBefore and fillAfter to true. But javadoc of" +
            " setFillEnabled(boolean) says \"fillBefore and fillAfter are ignored\"," +
            " it's unclear.")
    public void testAccessFill() {
        View animWindow = mActivity.findViewById(R.id.anim_window);
        // XML file of R.anim.accelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //     android:interpolator="@android:anim/accelerate_interpolator"
        //     android:fromAlpha="0.1"
        //     android:toAlpha="0.9"
        //     android:duration="1000" />
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        assertFalse(animation.isFillEnabled());
        assertTrue(animation.getFillBefore());
        assertFalse(animation.getFillAfter());

        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, animation);

        // fillBefore and fillAfter are ignored when fillEnabled is false
        Transformation transformation = new Transformation();
        // check alpha before start
        animation.getTransformation(animation.getStartTime() - 1, transformation);
        float alpha = transformation.getAlpha();
        assertEquals(0.1f, alpha, ALPHA_DELTA);  // issue 1698355

        transformation = new Transformation();
        // check alpha after the end
        animation.getTransformation(animation.getStartTime() + animation.getDuration() + 1,
                transformation);
        alpha = transformation.getAlpha();
        assertEquals(0.9f, alpha, ALPHA_DELTA);  // issue 1698355

        animation.setFillEnabled(true);
        animation.setFillBefore(false);
        assertTrue(animation.isFillEnabled());
        assertFalse(animation.getFillBefore());
        assertFalse(animation.getFillAfter());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, animation);

        transformation = new Transformation();
        animation.getTransformation(animation.getStartTime() - 1, transformation);
        alpha = transformation.getAlpha();
        assertEquals(1.0f, alpha, ALPHA_DELTA);

        transformation = new Transformation();
        animation.getTransformation(animation.getStartTime() + animation.getDuration() + 1,
                transformation);
        alpha = transformation.getAlpha();
        assertEquals(1.0f, alpha, ALPHA_DELTA);

        animation.setFillBefore(true);
        animation.setFillAfter(true);
        assertTrue(animation.isFillEnabled());
        assertTrue(animation.getFillBefore());
        assertTrue(animation.getFillAfter());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, animation);

        transformation = new Transformation();
        animation.getTransformation(animation.getStartTime() - 1, transformation);
        alpha = transformation.getAlpha();
        assertEquals(0.1f, alpha, ALPHA_DELTA);

        transformation = new Transformation();
        animation.getTransformation(animation.getStartTime() + animation.getDuration() + 1,
                transformation);
        alpha = transformation.getAlpha();
        assertEquals(0.9f, alpha, ALPHA_DELTA);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#computeDurationHint()}",
        method = "computeDurationHint",
        args = {}
    )
    public void testComputeDurationHint() {
        // start offset is 0, duration is 2000, repeat count is 0.
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        assertEquals(2000, animation.computeDurationHint());

        // start offset is 0, duration is 2000, repeat count is 2.
        animation.setRepeatCount(2);
        assertEquals(6000, animation.computeDurationHint());

        // start offset is 800, duration is 2000, repeat count is 2.
        animation.setStartOffset(800);
        assertEquals(8400, animation.computeDurationHint());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getRepeatMode()}. It cannot be set in XML",
            method = "getRepeatMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setRepeatMode(int)}. It cannot be set in XML",
            method = "setRepeatMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getRepeatCount()}.",
            method = "getRepeatCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setRepeatCount(int)}.",
            method = "setRepeatCount",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getDuration()}",
            method = "getDuration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setDuration(long)}.",
            method = "setDuration",
            args = {long.class}
        )
    })
    public void testRepeatAnimation() {
        // check default repeatMode
        Animation animation = new Animation() {
        };
        assertEquals(Animation.RESTART, animation.getRepeatMode());

        final View animWindow = mActivity.findViewById(R.id.anim_window);

        // XML file of R.anim.decelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //      android:interpolator="@android:anim/decelerate_interpolator"
        //      android:fromAlpha="0.0"
        //      android:toAlpha="1.0"
        //      android:duration="2000" />
        final Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        assertEquals(Animation.RESTART, animation.getRepeatMode());
        long duration = anim.getDuration();
        assertEquals(DECELERATE_ALPHA_DURATION, duration);
        // repeat count is 0, repeat mode does not make sense.
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);

        // test repeat mode REVERSE
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        // we have to DelayedCheck the animation status on test thread,
        // it cannot be done on UI thread, so we invoke runOnMainSync method here.
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                animWindow.startAnimation(anim);
            }
        });

        // check whether animation has started
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return anim.hasStarted();
            }
        }.run();

        Transformation transformation = new Transformation();
        long startTime = anim.getStartTime();
        anim.getTransformation(startTime, transformation);
        float alpha1 = transformation.getAlpha();
        assertEquals(0.0f, alpha1, ALPHA_DELTA);

        anim.getTransformation(startTime + 1000, transformation);
        float alpha2 = transformation.getAlpha();

        anim.getTransformation(startTime + 2000, transformation);
        float alpha3 = transformation.getAlpha();
        assertEquals(1.0f, alpha3, ALPHA_DELTA);

        // wait for animation has ended.
        // timeout is larger than duration, in case the system is sluggish
        new DelayedCheck(duration * 2 + 1000) {
            @Override
            protected boolean check() {
                return anim.hasEnded();
            }
        }.run();

        // get start time of reversing.
        startTime = anim.getStartTime();
        anim.getTransformation(startTime + 3000, transformation);
        float alpha4 = transformation.getAlpha();

        anim.getTransformation(startTime + 4000, transformation);
        float alpha5 = transformation.getAlpha();
        assertEquals(0.0f, alpha5, ALPHA_DELTA);

        // check decelerating delta alpha when reverse. alpha should change form 0.0f to 1.0f
        // and then from 1.0f to 0.0f
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        // the animation plays backward
        float delta3 = alpha3 - alpha4;
        float delta4 = alpha4 - alpha5;
        assertTrue(delta1 > delta2);
        assertTrue(delta3 > delta4);

        // test repeat mode RESTART
        anim.setRepeatMode(Animation.RESTART);
        // we have to DelayedCheck the animation status on test thread,
        // it cannot be done on UI thread, so we invoke runOnMainSync method here.
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                animWindow.startAnimation(anim);
            }
        });

        // check whether animation has started
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return anim.hasStarted();
            }
        }.run();

        transformation = new Transformation();
        startTime = anim.getStartTime();
        anim.getTransformation(startTime, transformation);
        alpha1 = transformation.getAlpha();
        assertEquals(0.0f, alpha1, ALPHA_DELTA);

        anim.getTransformation(startTime + 1000, transformation);
        alpha2 = transformation.getAlpha();

        anim.getTransformation(startTime + 2000, transformation);
        alpha3 = transformation.getAlpha();
        assertEquals(1.0f, alpha3, ALPHA_DELTA);

        // wait for animation has ended.
        // timeout is larger than duration, in case the system is sluggish
        new DelayedCheck(duration * 2 + 1000) {
            @Override
            protected boolean check() {
                return anim.hasEnded();
            }
        }.run();

        // get start time of restarting.
        startTime = anim.getStartTime();
        anim.getTransformation(startTime + 3000, transformation);
        alpha4 = transformation.getAlpha();

        anim.getTransformation(startTime + 4000, transformation);
        alpha5 = transformation.getAlpha();
        assertEquals(1.0f, alpha5, ALPHA_DELTA);

        // check decelerating delta alpha when restart. alpha should change form 0.0f to 1.0f
        // and then from 0.0f to 1.0f again
        delta1 = alpha2 - 0.0f;
        delta2 = alpha3 - alpha2;
        // the animation restarts from the beginning
        delta3 = alpha4 - 0.0f;
        delta4 = alpha5 - alpha4;
        assertTrue(delta1 > delta2);
        assertTrue(delta3 > delta4);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getStartOffset()}",
            method = "getStartOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setStartOffset(long)}",
            method = "setStartOffset",
            args = {long.class}
        )
    })
    public void testAccessStartOffset() {
        final long startOffset = 800;
        // check default startOffset
        Animation animation = new Animation() {
        };
        assertEquals(0, animation.getStartOffset());

        View animWindow = mActivity.findViewById(R.id.anim_window);
        // XML file of R.anim.accelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //      android:interpolator="@android:anim/accelerate_interpolator"
        //      android:fromAlpha="0.1"
        //      android:toAlpha="0.9"
        //      android:duration="1000" />
        animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        animation.setStartOffset(startOffset);
        assertEquals(startOffset, animation.getStartOffset());

        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow,
                animation, ACCELERATE_ALPHA_DURATION + startOffset);

        Transformation transformation = new Transformation();
        long startTime = animation.getStartTime();
        animation.getTransformation(startTime, transformation);
        float alpha1 = transformation.getAlpha();
        assertEquals(0.1f, alpha1, ALPHA_DELTA);

        animation.getTransformation(startTime + 400, transformation);
        float alpha2 = transformation.getAlpha();
        // alpha is 0.1f during start offset
        assertEquals(0.1f, alpha2, ALPHA_DELTA);

        animation.getTransformation(startTime + startOffset, transformation);
        float alpha3 = transformation.getAlpha();
        // alpha is 0.1f during start offset
        assertEquals(0.1f, alpha3, ALPHA_DELTA);

        animation.getTransformation(startTime + startOffset + 1, transformation);
        float alpha4 = transformation.getAlpha();
        // alpha is lager than 0.1f after start offset
        assertTrue(alpha4 > 0.1f);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "getStartTime",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "setStartTime",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "hasStarted",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "hasEnded",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "startNow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test run an accelerate alpha animation",
            method = "getTransformation",
            args = {long.class, android.view.animation.Transformation.class}
        )
    })
    public void testRunAccelerateAlpha() {
        // check default startTime
        Animation animation = new Animation() {
        };
        assertEquals(Animation.START_ON_FIRST_FRAME, animation.getStartTime());

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        animation.setStartTime(currentTime);
        assertEquals(currentTime, animation.getStartTime());

        View animWindow = mActivity.findViewById(R.id.anim_window);

        // XML file of R.anim.accelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //      android:interpolator="@android:anim/accelerate_interpolator"
        //      android:fromAlpha="0.1"
        //      android:toAlpha="0.9"
        //      android:duration="1000" />
        Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        assertEquals(Animation.START_ON_FIRST_FRAME, anim.getStartTime());
        assertFalse(anim.hasStarted());
        currentTime = AnimationUtils.currentAnimationTimeMillis();
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);
        // add delta 100 in case the system is sluggish when run animation
        assertEquals(currentTime, anim.getStartTime(), 100);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getTransformation(long, Transformation)}",
            method = "getTransformation",
            args = {long.class, android.view.animation.Transformation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#applyTransformation(float, Transformation)}",
            method = "applyTransformation",
            args = {float.class, android.view.animation.Transformation.class}
        )
    })
    @ToBeFixed(bug = "1698355", explanation = "getTransformation is very poorly documented.")
    public void testGetTransformation() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);

        // XML file of R.anim.accelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //      android:interpolator="@android:anim/accelerate_interpolator"
        //      android:fromAlpha="0.1"
        //      android:toAlpha="0.9"
        //      android:duration="1000" />
        final Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        assertFalse(anim.hasStarted());

        // we have to DelayedCheck the animation status on test thread,
        // it cannot be done on UI thread, so we invoke runOnMainSync method here.
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                animWindow.startAnimation(anim);
            }
        });

        // check whether animation has started
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return anim.hasStarted();
            }
        }.run();

        // check transformation objects that is provided by the
        // caller and will be filled in by the animation.
        Transformation transformation = new Transformation();
        long startTime = anim.getStartTime();
        assertTrue(anim.getTransformation(startTime, transformation));
        float alpha1 = transformation.getAlpha();
        assertEquals(0.1f, alpha1, ALPHA_DELTA);

        assertTrue(anim.getTransformation(startTime + 250, transformation));
        float alpha2 = transformation.getAlpha();

        assertTrue(anim.getTransformation(startTime + 500, transformation));
        float alpha3 = transformation.getAlpha();

        assertTrue(anim.getTransformation(startTime + 750, transformation));
        float alpha4 = transformation.getAlpha();

        // wait for animation has ended.
        // timeout is larger than duration, in case the system is sluggish
        new DelayedCheck(2000) {
            @Override
            protected boolean check() {
                return anim.hasEnded();
            }
        }.run();

        assertFalse(anim.getTransformation(startTime + 1000, transformation));
        float alpha5 = transformation.getAlpha();
        assertEquals(0.9f, alpha5, ALPHA_DELTA);

        // check decelerating delta alpha
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        float delta3 = alpha4 - alpha3;
        float delta4 = alpha5 - alpha4;
        assertTrue(delta1 < delta2);
        assertTrue(delta2 < delta3);
        assertTrue(delta3 < delta4);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getZAdjustment()}",
            method = "getZAdjustment",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setZAdjustment(int)}",
            method = "setZAdjustment",
            args = {int.class}
        )
    })
    public void testAccessZAdjustment() {
        // check default zAdjustment
        Animation animation = new Animation() {
        };
        assertEquals(Animation.ZORDER_NORMAL, animation.getZAdjustment());

        animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        assertEquals(Animation.ZORDER_NORMAL, animation.getZAdjustment());

        animation.setZAdjustment(Animation.ZORDER_TOP);
        assertEquals(Animation.ZORDER_TOP, animation.getZAdjustment());

        animation.setZAdjustment(Animation.ZORDER_BOTTOM);
        assertEquals(Animation.ZORDER_BOTTOM, animation.getZAdjustment());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#initialize(int, int, int, int)}}",
            method = "initialize",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#isInitialized()}",
            method = "isInitialized",
            args = {}
        )
    })
    public void testInitialize() {
        Animation animation = new Animation() {
        };

        assertFalse(animation.isInitialized());
        animation.initialize(320, 480, 320, 480);
        assertTrue(animation.isInitialized());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#resolveSize(int, float, int, int)}",
        method = "resolveSize",
        args = {int.class, float.class, int.class, int.class}
    )
    public void testResolveSize() {
        MyAnimation myAnimation = new MyAnimation();

        assertEquals(1.0f, myAnimation.resolveSize(Animation.ABSOLUTE, 1.0f, 0, 0));
        assertEquals(2.0f, myAnimation.resolveSize(Animation.ABSOLUTE, 2.0f, 0, 0));

        assertEquals(6.0f, myAnimation.resolveSize(Animation.RELATIVE_TO_SELF, 3.0f, 2, 0));
        assertEquals(9.0f, myAnimation.resolveSize(Animation.RELATIVE_TO_SELF, 3.0f, 3, 0));

        assertEquals(18.0f, myAnimation.resolveSize(Animation.RELATIVE_TO_PARENT, 3.0f, 0, 6));
        assertEquals(12.0f, myAnimation.resolveSize(Animation.RELATIVE_TO_PARENT, 3.0f, 0, 4));

        int unknownType = 7;
        assertEquals(8.0f, myAnimation.resolveSize(unknownType, 8.0f, 3, 4));
        assertEquals(10.0f, myAnimation.resolveSize(unknownType, 10.0f, 3, 4));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#restrictDuration(long)}",
        method = "restrictDuration",
        args = {long.class}
    )
    public void testRestrictDuration() {
        Animation animation = new Animation() {
        };

        animation.setStartOffset(1000);
        animation.restrictDuration(500);
        assertEquals(500, animation.getStartOffset());
        assertEquals(0, animation.getDuration());
        assertEquals(0, animation.getRepeatCount());

        animation.setStartOffset(1000);
        animation.setDuration(1000);
        animation.restrictDuration(1500);
        assertEquals(500, animation.getDuration());

        animation.setStartOffset(1000);
        animation.setDuration(1000);
        animation.setRepeatCount(3);
        animation.restrictDuration(4500);
        assertEquals(1, animation.getRepeatCount());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#scaleCurrentDuration(float)}",
        method = "scaleCurrentDuration",
        args = {float.class}
    )
    public void testScaleCurrentDuration() {
        Animation animation = new Animation() {
        };

        animation.setDuration(10);
        animation.scaleCurrentDuration(0);
        assertEquals(0, animation.getDuration());

        animation.setDuration(10);
        animation.scaleCurrentDuration(2);
        assertEquals(20, animation.getDuration());

        animation.setDuration(10);
        animation.scaleCurrentDuration(-1);
        assertEquals(-10, animation.getDuration());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#setAnimationListener(AnimationListener)}",
            method = "setAnimationListener",
            args = {android.view.animation.Animation.AnimationListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Animation#getTransformation(long, Transformation)}",
            method = "getTransformation",
            args = {long.class, android.view.animation.Transformation.class}
        )
    })
    public void testSetAnimationListener() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);

        // XML file of R.anim.accelerate_alpha
        // <alpha xmlns:android="http://schemas.android.com/apk/res/android"
        //      android:interpolator="@android:anim/accelerate_interpolator"
        //      android:fromAlpha="0.1"
        //      android:toAlpha="0.9"
        //      android:duration="1000" />
        final Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        MockAnimationListener listener = new MockAnimationListener();
        anim.setAnimationListener(listener);
        assertFalse(listener.hasAnimationStarted());
        assertFalse(listener.hasAnimationEnded());
        assertFalse(listener.hasAnimationRepeated());

        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);
        assertTrue(listener.hasAnimationStarted());
        assertTrue(listener.hasAnimationEnded());
        assertFalse(listener.hasAnimationRepeated());

        listener.reset();
        anim.setRepeatCount(2);
        anim.setRepeatMode(Animation.REVERSE);

        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim, 3000);
        assertTrue(listener.hasAnimationStarted());
        assertTrue(listener.hasAnimationRepeated());
        assertTrue(listener.hasAnimationEnded());

        listener.reset();
        // onAnimationEnd will not be invoked and animation should not end
        anim.setRepeatCount(Animation.INFINITE);

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                animWindow.startAnimation(anim);
            }
        });
        synchronized(mLockObject) {
            try {
                mLockObject.wait(4 * ACCELERATE_ALPHA_DURATION);
            } catch (InterruptedException e) {
                fail("thrown unexpected InterruptedException");
            }
        }

        assertTrue(listener.hasAnimationStarted());
        assertTrue(listener.hasAnimationRepeated());
        assertFalse(listener.hasAnimationEnded());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#start()}",
        method = "start",
        args = {}
    )
    public void testStart() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        animation.setStartTime(0);

        animation.start();
        assertEquals(Animation.START_ON_FIRST_FRAME, animation.getStartTime());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#startNow()}",
        method = "startNow",
        args = {}
    )
    public void testStartNow() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_alpha);
        animation.setStartTime(0);

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        animation.startNow();
        assertEquals(currentTime, animation.getStartTime(), 100);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#willChangeBounds()}, this method always returns true",
        method = "willChangeBounds",
        args = {}
    )
    public void testWillChangeBounds() {
        Animation animation = new Animation() {
        };

        assertTrue(animation.willChangeBounds());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Animation#willChangeTransformationMatrix()}," +
                " this method always returns true",
        method = "willChangeTransformationMatrix",
        args = {}
    )
    public void testWillChangeTransformationMatrix() {
        Animation animation = new Animation() {
        };

        assertTrue(animation.willChangeTransformationMatrix());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        MyAnimation myAnimation = new MyAnimation();
        myAnimation.setDuration(3000);
        myAnimation.setFillAfter(true);
        myAnimation.setFillBefore(false);
        myAnimation.setFillEnabled(true);
        myAnimation.setStartTime(1000);
        myAnimation.setRepeatCount(10);
        myAnimation.setRepeatMode(Animation.REVERSE);

        Animation cloneAnimation = myAnimation.clone();
        assertNotSame(myAnimation, cloneAnimation);
        assertEquals(myAnimation.getDuration(), cloneAnimation.getDuration());
        assertEquals(myAnimation.getFillAfter(), cloneAnimation.getFillAfter());
        assertEquals(myAnimation.getFillBefore(), cloneAnimation.getFillBefore());
        assertEquals(myAnimation.isFillEnabled(), cloneAnimation.isFillEnabled());
        assertEquals(myAnimation.getStartOffset(), cloneAnimation.getStartOffset());
        assertEquals(myAnimation.getRepeatCount(), cloneAnimation.getRepeatCount());
        assertEquals(myAnimation.getRepeatMode(), cloneAnimation.getRepeatMode());
    }

    private class MyAnimation extends Animation {
        @Override
        protected void ensureInterpolator() {
            super.ensureInterpolator();
        }

        @Override
        protected float resolveSize(int type, float value, int size, int parentSize) {
            return super.resolveSize(type, value, size, parentSize);
        }

        @Override
        protected Animation clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private class MockAnimationListener implements AnimationListener {
        private boolean mHasAnimationStarted = false;
        private boolean mHasAnimationEnded = false;
        private boolean mHasAnimationRepeated = false;

        public void onAnimationStart(Animation animation) {
            mHasAnimationStarted = true;
        }
        public void onAnimationEnd(Animation animation) {
            synchronized(mLockObject) {
                mHasAnimationEnded = true;
                mLockObject.notifyAll();
            }
        }
        public void onAnimationRepeat(Animation animation) {
            mHasAnimationRepeated = true;
        }

        public boolean hasAnimationStarted() {
            return mHasAnimationStarted;
        }

        public boolean hasAnimationEnded() {
            return mHasAnimationEnded;
        }

        public boolean hasAnimationRepeated() {
            return mHasAnimationRepeated;
        }

        public void reset() {
            mHasAnimationStarted = false;
            mHasAnimationEnded = false;
            mHasAnimationRepeated = false;
        }
    }
}
