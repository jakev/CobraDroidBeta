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

package com.android.cts;

import com.android.ddmlib.RawImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utilities for CTS host.
 *
 */
public class HostUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",
            Locale.ENGLISH);

    /**
     * Check if the given file exists
     *
     * @param name the file name to be checked
     * @return if the file exists, return true;
     *         else, return false
     */
    public static boolean isFileExist(final String name) {
        return new File(name).exists();
    }

    /**
     * Convert a 16bpp RawImage into a BufferedImage.
     *
     * @param rawImage the image to convert.
     * @return the BufferedImage.
     */
    public static BufferedImage convertRawImageToBufferedImage(RawImage rawImage) {
        assert rawImage.bpp == 16;

        BufferedImage im = new BufferedImage(rawImage.width,
                rawImage.height, BufferedImage.TYPE_USHORT_565_RGB);
        SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_USHORT,
                rawImage.width,
                rawImage.height,
                // RGB565
                new int[] { 0xf800, 0x07e0, 0x001f });

        // It would be more efficient to just subclass DataBuffer and provide a
        // TYPE_USHORT interface to the byte array.  But Raster.createRaster at
        // some point uses instanceof(DataBufferUShort) to verify that the DataBuffer
        // is of the right type (instead of just checking DataBuffer.getDataType).
        // And since DataBufferUShort is final, it can't be subclassed to get around
        // the check either.  So copy the data into a short[] instead to work around the problem.
        short shortData[] = new short[rawImage.size / 2];
        for (int x = 0; x < shortData.length; x++) {
            int rawImageOffset = x * 2;
            int a = 0xff & rawImage.data[rawImageOffset];
            int b = 0xff & rawImage.data[rawImageOffset + 1];
            shortData[x] = (short)((b << 8) | a);
        }
        DataBuffer db = new DataBufferUShort(shortData, shortData.length);
        Raster raster = Raster.createRaster(sampleModel,
                db, null);
        im.setData(raster);
        return im;
    }

    /**
     * Interface used with visitAllFilesUnder
     */
    public interface FileVisitor {
        /**
         * Gets called on every file visited.
         * @param f the File for the file being visited.
         */
        void visitFile(File f);
    }

    /**
     * Recursively visit all files under a given path.
     *
     * @param root the path to start at.
     * @param filter the file filter to match.  null means to visit all files.
     * @param visitor the visitor to visit with.
     */
    public static void visitAllFilesUnder(File root, FilenameFilter filter, FileVisitor visitor) {
        File[] files = root.listFiles(filter);
        // A null file may indicate not having enough permissions to view that directory
        if (files != null) {
            for (File f : files) {
                visitor.visitFile(f);

                if (f.isDirectory()) {
                    visitAllFilesUnder(f, filter, visitor);
                }
            }
        }
    }

    /**
     * Recursively visit all files under a given path.
     *
     * @param path the path to start at.
     * @param filter the file filter to match.  null means to visit all files.
     * @param visitor the visitor to visit with.
     */
    public static void visitAllFilesUnder(String path, FilenameFilter filter, FileVisitor visitor) {
        visitAllFilesUnder(new File(path), filter, visitor);
    }

    // Private class to help zipUpDirectory
    private static class ZipFileVisitor implements FileVisitor {
        private final ZipOutputStream zipOutputStream;
        private boolean ok = true;
        private IOException caughtException;
        private final ZipFilenameTransformer transformer;

        public ZipFileVisitor(ZipOutputStream zipOutputStream,
                ZipFilenameTransformer transformer) {
            this.zipOutputStream = zipOutputStream;
            this.transformer = transformer;
        }

        public void visitFile(File f) {
            String path = f.getPath();
            if (transformer != null) {
                path = transformer.transform(path);
            }
            ZipEntry ze = new ZipEntry(path);
            try {
                zipOutputStream.putNextEntry(ze);
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(f));
                    byte[] buffer = new byte[4096];
                    int bytesRead = is.read(buffer);
                    while (bytesRead > 0) {
                        zipOutputStream.write(buffer, 0, bytesRead);
                        bytesRead = is.read(buffer);
                    }
                    zipOutputStream.closeEntry();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                ok = false;
                caughtException = e;
            }
        }

        /**
         * Indicates that the visitor ran without errors
         * @return true if everything ran OK.
         */
        boolean isOk() {
            return ok;
        }

        /**
         * If an IOException was thrown while zipping, it gets kept here.
         *
         * @return the IOException that was caught, or null if none was.
         */
        IOException getCaughtException() {
            return caughtException;
        }

    }

    /**
     * Indicates some issue with zipping up the file.
     */
    static class ZipFileException extends Exception {
        ZipFileException(IOException ioException) {
            super("Caught wrapped exception", ioException);
        }
    }

    /**
     * Interface provided to rename files before they get zipped.
     */
    public interface ZipFilenameTransformer {
        /**
         * Transform a local filesystem filename into a zipfile filename.
         *
         * @param filename the input filename
         * @return the filename to be saved to the zipfile as.
         */
        String transform(String filename);
    }

    /**
     * Recursively zip up a directory into a zip file.
     *
     * @param sourceDir the directory to zip up
     * @param outputFilePath the zipfile to create.
     * @param transformer filepath transformer.  can be null.
     * @throws IOException if there were issues writing the zipfile.
     */
    public static void zipUpDirectory(String sourceDir,
            String outputFilePath,
            ZipFilenameTransformer transformer)
    throws IOException, ZipFileException {
        // I <3 abstractions
        FileOutputStream fileOut = new FileOutputStream(outputFilePath);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
        final ZipOutputStream zipOutputStream = new ZipOutputStream(bufOut);

        ZipFileVisitor zfv = new ZipFileVisitor(zipOutputStream, transformer);
        visitAllFilesUnder(sourceDir, null, zfv);
        zipOutputStream.close();
        if (!zfv.isOk()) {
            throw new ZipFileException(zfv.getCaughtException());
        }
    }

    /**
     * Get the formatted time string.
     *
     * @param milliSec The time in milliseconds.
     * @param separator The separator between the date information and time information.
     * @param dateSeparator The date separator separating the date information nibbles.
     * @param timeSeparator The time separator separating the time information nibbles.
     * @return The formated time string.
     */
    public static String getFormattedTimeString(long milliSec, String separator,
            String dateSeparator, String timeSeparator) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSec);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int date  = cal.get(Calendar.DATE);
        int hour  = cal.get(Calendar.HOUR_OF_DAY);
        int min   = cal.get(Calendar.MINUTE);
        int sec   = cal.get(Calendar.SECOND);

        Formatter fmt = new Formatter();
        if ((separator == null) || (separator.length() == 0)) {
            separator = "_";
        }

        if ((dateSeparator == null) || (dateSeparator.length() == 0)) {
            dateSeparator = ".";
        }

        if ((timeSeparator == null) || (timeSeparator.length() == 0)) {
            timeSeparator = ".";
        }

        final String formatStr = "%4d" + dateSeparator + "%02d" + dateSeparator + "%02d"
                         + separator + "%02d" + timeSeparator + "%02d" + timeSeparator + "%02d";
        fmt.format(formatStr, year, month, date, hour, min, sec);

        return fmt.toString();
    }

    /**
     * Convert the given byte array into a lowercase hex string.
     *
     * @param arr The array to convert.
     * @return The hex encoded string.
     */
    public static String toHexString(byte[] arr) {
        StringBuffer buf = new StringBuffer(arr.length * 2);
        for (byte b : arr) {
            buf.append(String.format("%02x", b & 0xFF));
        }
        return buf.toString();
    }

    /**
     * Strip control characters from the given string.
     */
    public static String replaceControlChars(String s) {
        // Replace any character < 0x20, except for tab, lf and cr
        return s.replaceAll("[\\x00-\\x1f&&[^\t\n\r]]", "?");
    }

    public static Date dateFromString(String s) throws ParseException {
        return dateFormat.parse(s);
    }

    public static String dateToString(Date d) {
        return dateFormat.format(d);
    }
}
