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

import com.android.cts.verifier.audioquality.experiments.BiasExperiment;
import com.android.cts.verifier.audioquality.experiments.ColdLatencyExperiment;
import com.android.cts.verifier.audioquality.experiments.OverflowExperiment;
import com.android.cts.verifier.audioquality.experiments.GainLinearityExperiment;
import com.android.cts.verifier.audioquality.experiments.GlitchExperiment;
import com.android.cts.verifier.audioquality.experiments.SoundLevelExperiment;
import com.android.cts.verifier.audioquality.experiments.SpectrumShapeExperiment;
import com.android.cts.verifier.audioquality.experiments.WarmLatencyExperiment;

import android.content.Context;

import java.util.ArrayList;

/**
 * Data shared between the VerifierActivity and ExperimentService
 */
public class VerifierExperiments {

    private static ArrayList<Experiment> mExperiments = null;

    private VerifierExperiments() {
    }

    public static ArrayList<Experiment> getExperiments(Context context) {
        if (mExperiments == null) {
            mExperiments = new ArrayList<Experiment>();
            mExperiments.add(new SoundLevelExperiment());
            mExperiments.add(new BiasExperiment());
            mExperiments.add(new OverflowExperiment());
            mExperiments.add(new GainLinearityExperiment());
            mExperiments.add(new SpectrumShapeExperiment());
            mExperiments.add(new GlitchExperiment(0));
            mExperiments.add(new GlitchExperiment(7));
            mExperiments.add(new ColdLatencyExperiment());
            mExperiments.add(new WarmLatencyExperiment());
            for (Experiment exp : mExperiments) {
                exp.init(context);
            }
        }
        return mExperiments;
    }
}
