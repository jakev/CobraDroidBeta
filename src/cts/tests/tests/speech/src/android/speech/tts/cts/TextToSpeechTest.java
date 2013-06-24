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
package android.speech.tts.cts;

import dalvik.annotation.TestTargetClass;

import android.media.MediaPlayer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.test.AndroidTestCase;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Tests for {@link android.speech.tts.TextToSpeech}
 */
@TestTargetClass(TextToSpeech.class)
public class TextToSpeechTest extends AndroidTestCase {

    private TextToSpeech mTts;

    private static final String UTTERANCE = "utterance";
    private static final String SAMPLE_TEXT = "This is a sample text to speech string";
    private static final String SAMPLE_FILE_NAME = "mytts.wav";
    /** maximum time to wait for tts to be initialized */
    private static final int TTS_INIT_MAX_WAIT_TIME = 30 * 1000;
    /** maximum time to wait for speech call to be complete */
    private static final int TTS_SPEECH_MAX_WAIT_TIME = 5 * 1000;
    private static final String LOG_TAG = "TextToSpeechTest";

    /**
     * Listener for waiting for TTS engine initialization completion.
     */
    private static class InitWaitListener implements OnInitListener {
        private int mStatus = TextToSpeech.ERROR;

        public void onInit(int status) {
            mStatus = status;
            synchronized(this) {
                notify();
            }
        }

        public boolean waitForInit() throws InterruptedException {
            if (mStatus == TextToSpeech.SUCCESS) {
                return true;
            }
            synchronized (this) {
                wait(TTS_INIT_MAX_WAIT_TIME);
            }
            return mStatus == TextToSpeech.SUCCESS;
        }
    }

    /**
     * Listener for waiting for utterance completion.
     */
    private static class UtteranceWaitListener implements OnUtteranceCompletedListener {
        private boolean mIsComplete = false;
        private final String mExpectedUtterance;

        public UtteranceWaitListener(String expectedUtteranceId) {
            mExpectedUtterance = expectedUtteranceId;
        }

        public void onUtteranceCompleted(String utteranceId) {
            if (mExpectedUtterance.equals(utteranceId)) {
                synchronized(this) {
                    mIsComplete = true;
                    notify();
                }
            }
        }

        public boolean waitForComplete() throws InterruptedException {
            if (mIsComplete) {
                return true;
            }
            synchronized (this) {
                wait(TTS_SPEECH_MAX_WAIT_TIME);
                return mIsComplete;
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        InitWaitListener listener = new InitWaitListener();
        mTts = new TextToSpeech(getContext(), listener);
        assertTrue(listener.waitForInit());
        assertTrue(checkAndSetLanguageAvailable());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mTts.shutdown();
    }

    /**
     * Ensures at least one language is available for tts
     */
    private boolean  checkAndSetLanguageAvailable() {
        // checks if at least one language is available in Tts
        for (Locale locale : Locale.getAvailableLocales()) {
            int availability = mTts.isLanguageAvailable(locale);
            if (availability == TextToSpeech.LANG_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                mTts.setLanguage(locale);
                return true;
            }
        }
        return false;
    }

    /**
     * Tests that {@link TextToSpeech#synthesizeToFile(String, java.util.HashMap, String)} produces
     * a non-zero sized file.
     * @throws InterruptedException
     */
    public void testSynthesizeToFile() throws Exception {
        File sampleFile = new File(Environment.getExternalStorageDirectory(), SAMPLE_FILE_NAME);
        try {
            assertFalse(sampleFile.exists());
            // use an utterance listener to determine when synthesizing is complete
            HashMap<String, String> param = new HashMap<String,String>();
            param.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE);
            UtteranceWaitListener listener = new UtteranceWaitListener(UTTERANCE);
            mTts.setOnUtteranceCompletedListener(listener);

            int result = mTts.synthesizeToFile(SAMPLE_TEXT, param, sampleFile.getPath());
            assertEquals(TextToSpeech.SUCCESS, result);

            assertTrue(listener.waitForComplete());
            assertTrue(sampleFile.exists());
            assertTrue(isMusicFile(sampleFile.getPath()));

        } finally {
            deleteFile(sampleFile);
        }
    }

    /**
     * Determine if given file path is a valid, playable music file.
     */
    private boolean isMusicFile(String filePath) {
        // use media player to play the file. If it succeeds with no exceptions, assume file is
        //valid
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            mp.stop();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while attempting to play music file", e);
            return false;
        }
    }

    /**
     * Deletes the file at given path
     * @param sampleFilePath
     */
    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
