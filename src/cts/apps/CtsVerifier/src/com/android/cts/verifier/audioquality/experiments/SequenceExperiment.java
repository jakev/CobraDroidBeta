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

package com.android.cts.verifier.audioquality.experiments;

import com.android.cts.verifier.R;
import com.android.cts.verifier.audioquality.Utils;

import android.content.Context;

/**
 * An extension to LoopbackExperiment, in which the "playback and record"
 * cycle is repeated several times. A family of stimuli is defined, and
 * the experiment outcome may depend on the whole sequence of recordings.
 */
public class SequenceExperiment extends LoopbackExperiment {
    public SequenceExperiment(boolean enable) {
        super(enable);
    }

    protected int getTrials() {
        return 1;
    }

    protected byte[] getStim(Context context, int trial) {
        int stimNum = 2;
        byte[] data = Utils.getStim(context, stimNum);
        return data;
    }

    protected void compare(byte[][] stim, byte[][] record) {
        setScore(getString(R.string.aq_complete));
        setReport(getString(R.string.aq_loopback_report));
    }

    @Override
    public void run() {
        int n = getTrials();
        byte[][] playbackData = new byte[n][];
        byte[][] recordedData = new byte[n][];
        for (int trial = 0; trial < n; trial++) {
            playbackData[trial] = getStim(mContext, trial);
            recordedData[trial] = loopback(playbackData[trial]);
            setRecording(recordedData[trial], trial);
        }
        compare(playbackData, recordedData);
        mTerminator.terminate(false);
    }

    @Override
    public int getTimeout() {
        return TIMEOUT * getTrials();
    }
}
