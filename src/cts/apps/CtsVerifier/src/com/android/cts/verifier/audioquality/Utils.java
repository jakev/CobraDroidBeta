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

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * File and data utilities for the Audio Verifier.
 */
public class Utils {
    public static final String TAG = "AudioQualityVerifier";
    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    /**
     * @param minBufferSize requested
     * @return the buffer size or a negative {@link AudioTrack} ERROR value
     */
    public static int getAudioTrackBufferSize(int minBufferSize) {
        int minHardwareBufferSize = AudioTrack.getMinBufferSize(
                AudioQualityVerifierActivity.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioQualityVerifierActivity.AUDIO_FORMAT);
        if (minHardwareBufferSize < 0) {
            return minHardwareBufferSize;
        } else {
            return Math.max(minHardwareBufferSize, minBufferSize);
        }
    }

    /**
     * @param minBufferSize requested
     * @return the buffer size or a negative {@link AudioRecord} ERROR value
     */
    public static int getAudioRecordBufferSize(int minBufferSize) {
        int minHardwareBufferSize = AudioRecord.getMinBufferSize(
                AudioQualityVerifierActivity.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioQualityVerifierActivity.AUDIO_FORMAT);
        if (minHardwareBufferSize < 0) {
            return minHardwareBufferSize;
        } else {
            return Math.max(minHardwareBufferSize, minBufferSize);
        }
    }

    /**
     *  Time delay.
     *
     *  @param ms time in milliseconds to pause for
     */
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {}
    }

    public static String getExternalDir(Context context, Object exp) {
        checkExternalStorageAvailable();
        // API level 8:
        // return context.getExternalFilesDir(null).getAbsolutePath();
        // API level < 8:
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        dir += "/Android/data/" + exp.getClass().getPackage().getName() + "/files";
        checkMakeDir(dir);
        return dir;
    }

    private static void checkExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // TODO: Raise a Toast and supply internal storage instead
        }
    }

    private static void checkMakeDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * Convert a string (e.g. the name of an experiment) to something more suitable
     * for use as a filename.
     *
     * @param s the string to be cleaned
     * @return a string which is similar (not necessarily unique) and safe for filename use
     */
    public static String cleanString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) sb.append('_');
            else if (Character.isLetterOrDigit(c)) sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Convert a sub-array from bytes to shorts.
     *
     * @param data array of bytes to be converted
     * @param start first index to convert (should be even)
     * @param len number of bytes to convert (should be even)
     * @return an array of half the length, containing shorts
     */
    public static short[] byteToShortArray(byte[] data, int start, int len) {
        short[] samples = new short[len / 2];
        ByteBuffer bb = ByteBuffer.wrap(data, start, len);
        bb.order(BYTE_ORDER);
        for (int i = 0; i < len / 2; i++) {
            samples[i] = bb.getShort();
        }
        return samples;
    }

    /**
     * Convert a byte array to an array of shorts (suitable for the phone test
     * native library's audio sample data).
     *
     * @param data array of bytes to be converted
     * @return an array of half the length, containing shorts
     */
    public static short[] byteToShortArray(byte[] data) {
        int len = data.length / 2;
        short[] samples = new short[len];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(BYTE_ORDER);
        for (int i = 0; i < len; i++) {
            samples[i] = bb.getShort();
        }
        return samples;
    }

    /**
     * Convert a short array (as returned by the phone test native library)
     * to an array of bytes.
     *
     * @param samples array of shorts to be converted
     * @return an array of twice the length, broken out into bytes
     */
    public static byte[] shortToByteArray(short[] samples) {
        int len = samples.length;
        byte[] data = new byte[len * 2];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(BYTE_ORDER);
        for (int i = 0; i < len; i++) {
            bb.putShort(samples[i]);
        }
        return data;
    }

    /**
     * Scale the amplitude of an array of samples.
     *
     * @param samples to be scaled
     * @param db decibels to scale up by (may be negative)
     * @return the scaled samples
     */
    public static short[] scale(short[] samples, float db) {
        short[] scaled = new short[samples.length];
        // Convert decibels to a linear ratio:
        double ratio = Math.pow(10.0, db / 20.0);
        for (int i = 0; i < samples.length; i++) {
            scaled[i] = (short) (samples[i] * ratio);
        }
        return scaled;
    }

    /**
     * Read an entire file into memory.
     *
     * @param filename to be opened
     * @return the file data, or null in case of error
     */
    private static byte[] readFile(String filename) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e1) {
            return null;
        }

        File file = new File(filename);
        int len = (int) file.length();
        byte[] data = new byte[len];

        int pos = 0;
        int bytes = 0;
        int count;
        while (pos < len) {
            try {
                count = fis.read(data, pos, len - pos);
            } catch (IOException e) {
                return null;
            }
            if (count < 1) return null;
            pos += count;
        }

        try {
            fis.close();
        } catch (IOException e) {}
        return data;
    }

    /**
     * Read an entire file from an InputStream.
     * Useful as AssetManager returns these.
     *
     * @param stream to read file contents from
     * @return file data
     */
    public static byte[] readFile(InputStream stream) {
        final int CHUNK_SIZE = 10000;
        ByteArrayBuilder bab = new ByteArrayBuilder();
        byte[] buf = new byte[CHUNK_SIZE];
        int count;
        while (true) {
            try {
                count = stream.read(buf, 0, CHUNK_SIZE);
            } catch (IOException e) {
                return null;
            }
            if (count == -1) break; // EOF
            bab.append(buf, count);
        }
        return bab.toByteArray();
    }

    /**
     * Save binary (audio) data to a file.
     *
     * @param filename to be written
     * @param data contents
     */
    public static void saveFile(String filename, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file " + filename, e);
        }
    }

    /**
     * Push an entire array of audio data to an AudioTrack.
     *
     * @param at destination
     * @param data to be written
     * @return true if successful, or false on error
     */
    public static boolean writeAudio(AudioTrack at, byte[] data) {
        int pos = 0;
        int len = data.length;
        int count;

        while (pos < len) {
            count = at.write(data, pos, len - pos);
            if (count < 0) return false;
            pos += count;
        }
        at.flush();
        return true;
    }

    /**
     * Determine the number of audio samples in a file
     *
     * @param filename file containing audio data
     * @return number of samples in file, or 0 if file does not exist
     */
    public static int duration(String filename) {
        File file = new File(filename);
        int len = (int) file.length();
        return len / AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
    }

    /**
     * Determine the number of audio samples in a stimulus asset
     *
     * @param context to look up stimulus
     * @param stimNum index number of this stimulus
     * @return number of samples in stimulus
     */
    public static int duration(Context context, int stimNum) {
        byte[] data = AudioAssets.getStim(context, stimNum);
        return data.length / AudioQualityVerifierActivity.BYTES_PER_SAMPLE;
    }

    public static void playRawFile(String filename) {
        byte[] data = readFile(filename);
        if (data == null) {
            Log.e(TAG, "Cannot read " + filename);
            return;
        }
        playRaw(data);
    }

    public static void playStim(Context context, int stimNum) {
        Utils.playRaw(getStim(context, stimNum));
    }

    public static byte[] getStim(Context context, int stimNum) {
        return AudioAssets.getStim(context, stimNum);
    }

    public static byte[] getPinkNoise(Context context, int ampl, int duration) {
        return AudioAssets.getPinkNoise(context, ampl, duration);
    }

    public static void playRaw(byte[] data) {
        Log.i(TAG, "Playing " + data.length + " bytes of pre-recorded audio");
        AudioTrack at = new AudioTrack(AudioQualityVerifierActivity.PLAYBACK_STREAM, AudioQualityVerifierActivity.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioQualityVerifierActivity.AUDIO_FORMAT,
                data.length, AudioTrack.MODE_STREAM);
        writeAudio(at, data);
        at.play();
    }

    /**
     * The equivalent of a simplified StringBuilder, but for bytes.
     */
    public static class ByteArrayBuilder {
        private byte[] buf;
        private int capacity, size;

        public ByteArrayBuilder() {
            capacity = 100;
            size = 0;
            buf = new byte[capacity];
        }

        public void append(byte[] b, int nBytes) {
            if (nBytes < 1) return;
            if (size + nBytes > capacity) expandCapacity(size + nBytes);
            System.arraycopy(b, 0, buf, size, nBytes);
            size += nBytes;
        }

        public byte[] toByteArray() {
            byte[] result = new byte[size];
            System.arraycopy(buf, 0, result, 0, size);
            return result;
        }

        private void expandCapacity(int min) {
            capacity *= 2;
            if (capacity < min) capacity = min;
            byte[] expanded = new byte[capacity];
            System.arraycopy(buf, 0, expanded, 0, size);
            buf = expanded;
        }
    }
}
