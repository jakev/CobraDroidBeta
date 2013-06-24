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

package android.widget.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import android.test.ActivityInstrumentationTestCase2;
import android.view.animation.cts.DelayedCheck;
import android.widget.Filter;
import android.widget.Filter.FilterListener;

@TestTargetClass(Filter.class)
public class FilterTest extends ActivityInstrumentationTestCase2<StubActivity> {
    private static final long TIME_OUT = 10000;
    private static final String TEST_CONSTRAINT = "filter test";
    private MockFilter mMockFilter;

    public FilterTest() {
        super("com.android.cts.stub", StubActivity.class);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Filter",
        args = {}
    )
    public void testConstructor() {
        new MockFilter();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "convertResultToString",
        args = {java.lang.Object.class}
    )
    public void testConvertResultToString() {
        final MockFilter filter = new MockFilter();
        assertEquals("", filter.convertResultToString(null));

        final String testStr = "Test";
        assertEquals(testStr, filter.convertResultToString(testStr));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {java.lang.CharSequence.class}
    )
    public void testFilter1() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMockFilter = new MockFilter();
                mMockFilter.filter(TEST_CONSTRAINT);
            }
        });

        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mMockFilter.hadPerformedFiltering();
            }
        }.run();
        assertEquals(TEST_CONSTRAINT, mMockFilter.getPerformFilteringConstraint());

        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mMockFilter.hadPublishedResults();
            }
        }.run();
        assertEquals(TEST_CONSTRAINT, mMockFilter.getPublishResultsConstraint());
        assertSame(mMockFilter.getExpectResults(), mMockFilter.getResults());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {java.lang.CharSequence.class, android.widget.Filter.FilterListener.class}
    )
    public void testFilter2() {
        final MockFilterListener mockFilterListener = new MockFilterListener();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMockFilter = new MockFilter();
                mMockFilter.filter(TEST_CONSTRAINT, mockFilterListener);
            }
        });

        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mMockFilter.hadPerformedFiltering();
            }
        }.run();
        assertEquals(TEST_CONSTRAINT, mMockFilter.getPerformFilteringConstraint());

        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mMockFilter.hadPublishedResults();
            }
        }.run();
        assertEquals(TEST_CONSTRAINT, mMockFilter.getPublishResultsConstraint());
        assertSame(mMockFilter.getExpectResults(), mMockFilter.getResults());

        new DelayedCheck(TIME_OUT) {
            @Override
            protected boolean check() {
                return mockFilterListener.hasCalledOnFilterComplete();
            }
        }.run();
    }

    private static class MockFilter extends Filter {
        private boolean mHadPublishedResults = false;
        private boolean mHadPerformedFiltering = false;
        private CharSequence mPerformFilteringConstraint;
        private CharSequence mPublishResultsConstraint;
        private FilterResults mResults;
        private FilterResults mExpectResults = new FilterResults();

        public MockFilter() {
            super();
        }

        public boolean hadPublishedResults() {
            return mHadPublishedResults;
        }

        public boolean hadPerformedFiltering() {
            return mHadPerformedFiltering;
        }

        public CharSequence getPerformFilteringConstraint() {
            return mPerformFilteringConstraint;
        }

        public CharSequence getPublishResultsConstraint() {
            return mPublishResultsConstraint;
        }

        public FilterResults getResults() {
            return mResults;
        }

        public FilterResults getExpectResults() {
            return mExpectResults;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mHadPerformedFiltering = true;
            mPerformFilteringConstraint = constraint;
            return mExpectResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mPublishResultsConstraint = constraint;
            mResults = results;
            mHadPublishedResults = true;
        }
    }

    private static class MockFilterListener implements FilterListener {
        private boolean mCalledOnFilterComplete = false;

        public void onFilterComplete(int count) {
            mCalledOnFilterComplete = true;
        }

        public boolean hasCalledOnFilterComplete() {
            return mCalledOnFilterComplete;
        }
    }
}
