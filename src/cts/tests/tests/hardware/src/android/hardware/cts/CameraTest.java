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

package android.hardware.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.test.MoreAsserts;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This test case must run with hardware. It can't be tested in emulator
 */
@LargeTest
@TestTargetClass(Camera.class)
public class CameraTest extends ActivityInstrumentationTestCase2<CameraStubActivity> {
    private String TAG = "CameraTest";
    private static final String PACKAGE = "com.android.cts.stub";
    private static final boolean LOGV = false;
    private final String JPEG_PATH = Environment.getExternalStorageDirectory().getPath() +
            "/test.jpg";
    private byte[] mJpegData;

    private boolean mPreviewCallbackResult = false;
    private boolean mShutterCallbackResult = false;
    private boolean mRawPictureCallbackResult = false;
    private boolean mJpegPictureCallbackResult = false;
    private boolean mErrorCallbackResult = false;
    private boolean mAutoFocusSucceeded = false;

    private static final int WAIT_FOR_COMMAND_TO_COMPLETE = 1500;  // Milliseconds.
    private static final int WAIT_FOR_FOCUS_TO_COMPLETE = 3000;
    private static final int WAIT_FOR_SNAPSHOT_TO_COMPLETE = 5000;

    private PreviewCallback mPreviewCallback = new PreviewCallback();
    private TestShutterCallback mShutterCallback = new TestShutterCallback();
    private RawPictureCallback mRawPictureCallback = new RawPictureCallback();
    private JpegPictureCallback mJpegPictureCallback = new JpegPictureCallback();
    private TestErrorCallback mErrorCallback = new TestErrorCallback();
    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();

    private Looper mLooper = null;
    private final ConditionVariable mPreviewDone = new ConditionVariable();
    private final ConditionVariable mFocusDone = new ConditionVariable();
    private final ConditionVariable mSnapshotDone = new ConditionVariable();

    Camera mCamera;

    public CameraTest() {
        super(PACKAGE, CameraStubActivity.class);
        if (LOGV) Log.v(TAG, "Camera Constructor");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // to start CameraStubActivity.
        getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        super.tearDown();
    }

    /*
     * Initializes the message looper so that the Camera object can
     * receive the callback messages.
     */
    private void initializeMessageLooper(final int cameraId) {
        final ConditionVariable startDone = new ConditionVariable();
        new Thread() {
            @Override
            public void run() {
                Log.v(TAG, "start loopRun");
                // Set up a looper to be used by camera.
                Looper.prepare();
                // Save the looper so that we can terminate this thread
                // after we are done with it.
                mLooper = Looper.myLooper();
                mCamera = Camera.open(cameraId);
                Log.v(TAG, "camera is opened");
                startDone.open();
                Looper.loop(); // Blocks forever until Looper.quit() is called.
                if (LOGV) Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();

        Log.v(TAG, "start waiting for looper");
        if (!startDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            Log.v(TAG, "initializeMessageLooper: start timeout");
            fail("initializeMessageLooper: start timeout");
        }
    }

    /*
     * Terminates the message looper thread.
     */
    private void terminateMessageLooper() throws Exception {
        mLooper.quit();
        // Looper.quit() is asynchronous. The looper may still has some
        // preview callbacks in the queue after quit is called. The preview
        // callback still uses the camera object (setHasPreviewCallback).
        // After camera is released, RuntimeException will be thrown from
        // the method. So we need to join the looper thread here.
        mLooper.getThread().join();
        mCamera.release();
        mCamera = null;
    }

    //Implement the previewCallback
    private final class PreviewCallback
            implements android.hardware.Camera.PreviewCallback {
        public void onPreviewFrame(byte [] data, Camera camera) {
            assertNotNull(data);
            Size size = camera.getParameters().getPreviewSize();
            assertEquals(size.width * size.height * 3 / 2, data.length);
            mPreviewCallbackResult = true;
            mCamera.stopPreview();
            if (LOGV) Log.v(TAG, "notify the preview callback");
            mPreviewDone.open();
            if (LOGV) Log.v(TAG, "Preview callback stop");
        }
    }

    //Implement the shutterCallback
    private final class TestShutterCallback implements ShutterCallback {
        public void onShutter() {
            mShutterCallbackResult = true;
            if (LOGV) Log.v(TAG, "onShutter called");
        }
    }

    //Implement the RawPictureCallback
    private final class RawPictureCallback implements PictureCallback {
        public void onPictureTaken(byte [] rawData, Camera camera) {
            if (rawData != null) {
                mRawPictureCallbackResult = true;
            } else {
                mRawPictureCallbackResult = false;
            }
            if (LOGV) Log.v(TAG, "RawPictureCallback callback");
        }
    }

    // Implement the JpegPictureCallback
    private final class JpegPictureCallback implements PictureCallback {
        public void onPictureTaken(byte[] rawData, Camera camera) {
            try {
                mJpegData = rawData;
                if (rawData != null) {
                    // try to store the picture on the SD card
                    File rawoutput = new File(JPEG_PATH);
                    FileOutputStream outStream = new FileOutputStream(rawoutput);
                    outStream.write(rawData);
                    outStream.close();
                    mJpegPictureCallbackResult = true;

                    if (LOGV) {
                        Log.v(TAG, "JpegPictureCallback rawDataLength = " + rawData.length);
                    }
                } else {
                    mJpegPictureCallbackResult = false;
                }
                mSnapshotDone.open();
                if (LOGV) Log.v(TAG, "Jpeg Picture callback");
            } catch (IOException e) {
                // no need to fail here; callback worked fine
                Log.w(TAG, "Error writing picture to sd card.");
            }
        }
    }

    // Implement the ErrorCallback
    private final class TestErrorCallback implements ErrorCallback {
        public void onError(int error, Camera camera) {
            mErrorCallbackResult = true;
            fail("The Error code is: " + error);
        }
    }

    private final class AutoFocusCallback
            implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusSucceeded = success;
            Log.v(TAG, "AutoFocusCallback success=" + success);
            mFocusDone.open();
        }
    }

    private void waitForPreviewDone() {
        if (LOGV) Log.v(TAG, "Wait for preview callback");
        if (!mPreviewDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            // timeout could be expected or unexpected. The caller will decide.
            Log.v(TAG, "waitForPreviewDone: timeout");
        }
        mPreviewDone.close();
    }

    private boolean waitForFocusDone() {
        boolean result = mFocusDone.block(WAIT_FOR_FOCUS_TO_COMPLETE);
        if (!result) {
            // timeout could be expected or unexpected. The caller will decide.
            Log.v(TAG, "waitForFocusDone: timeout");
        }
        mFocusDone.close();
        return result;
    }

    private void waitForSnapshotDone() {
        if (!mSnapshotDone.block(WAIT_FOR_SNAPSHOT_TO_COMPLETE)) {
            // timeout could be expected or unexpected. The caller will decide.
            Log.v(TAG, "waitForSnapshotDone: timeout");
        }
        mSnapshotDone.close();
    }

    private void checkPreviewCallback() throws Exception {
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        if (LOGV) Log.v(TAG, "check preview callback");
        mCamera.startPreview();
        waitForPreviewDone();
        mCamera.setPreviewCallback(null);
    }

    /*
     * Test case 1: Take a picture and verify all the callback
     * functions are called properly.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startPreview",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPreviewDisplay",
            args = {android.view.SurfaceHolder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takePicture",
            args = {android.hardware.Camera.ShutterCallback.class,
                    android.hardware.Camera.PictureCallback.class,
                    android.hardware.Camera.PictureCallback.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "autoFocus",
            args = {android.hardware.Camera.AutoFocusCallback.class}
        )
    })
    @UiThreadTest
    public void testTakePicture() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testTakePictureByCamera(id);
        }
    }

    private void testTakePictureByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        Size pictureSize = mCamera.getParameters().getPictureSize();
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();
        mCamera.autoFocus(mAutoFocusCallback);
        assertTrue(waitForFocusDone());
        mJpegData = null;
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        terminateMessageLooper();
        assertTrue(mShutterCallbackResult);
        assertTrue(mJpegPictureCallbackResult);
        assertNotNull(mJpegData);
        Bitmap b = BitmapFactory.decodeByteArray(mJpegData, 0, mJpegData.length);
        assertEquals(pictureSize.width, b.getWidth());
        assertEquals(pictureSize.height, b.getHeight());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopPreview",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPreviewCallback",
            args = {android.hardware.Camera.PreviewCallback.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startPreview",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPreviewDisplay",
            args = {android.view.SurfaceHolder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setErrorCallback",
            args = {android.hardware.Camera.ErrorCallback.class}
        )
    })
    @UiThreadTest
    public void testPreviewCallback() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testPreviewCallbackByCamera(id);
        }
    }

    private void testPreviewCallbackByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setPreviewCallback(mPreviewCallback);
        mCamera.setErrorCallback(mErrorCallback);
        checkPreviewCallback();
        terminateMessageLooper();
        assertTrue(mPreviewCallbackResult);

        mPreviewCallbackResult = false;
        initializeMessageLooper(cameraId);
        checkPreviewCallback();
        terminateMessageLooper();
        assertFalse(mPreviewCallbackResult);

        // Test all preview sizes.
        initializeMessageLooper(cameraId);
        Parameters parameters = mCamera.getParameters();
        for (Size size: parameters.getSupportedPreviewSizes()) {
            mPreviewCallbackResult = false;
            mCamera.setPreviewCallback(mPreviewCallback);
            parameters.setPreviewSize(size.width, size.height);
            mCamera.setParameters(parameters);
            assertEquals(size, mCamera.getParameters().getPreviewSize());
            checkPreviewCallback();
            assertTrue(mPreviewCallbackResult);
            try {
                // Wait for a while to throw away the remaining preview frames.
                Thread.sleep(1000);
            } catch(Exception e) {
                // ignore
            }
            mPreviewDone.close();
        }
        terminateMessageLooper();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOneShotPreviewCallback",
        args = {PreviewCallback.class}
    )
    @UiThreadTest
    public void testSetOneShotPreviewCallback() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testSetOneShotPreviewCallbackByCamera(id);
        }
    }

    private void testSetOneShotPreviewCallbackByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setOneShotPreviewCallback(mPreviewCallback);
        checkPreviewCallback();
        terminateMessageLooper();
        assertTrue(mPreviewCallbackResult);

        mPreviewCallbackResult = false;
        initializeMessageLooper(cameraId);
        checkPreviewCallback();
        terminateMessageLooper();
        assertFalse(mPreviewCallbackResult);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setPreviewDisplay",
        args = {SurfaceHolder.class}
    )
    @UiThreadTest
    public void testSetPreviewDisplay() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testSetPreviewDisplayByCamera(id);
        }
    }

    private void testSetPreviewDisplayByCamera(int cameraId) throws Exception {
        SurfaceHolder holder = getActivity().getSurfaceView().getHolder();
        initializeMessageLooper(cameraId);

        // Check the order: startPreview->setPreviewDisplay.
        mCamera.setOneShotPreviewCallback(mPreviewCallback);
        mCamera.startPreview();
        mCamera.setPreviewDisplay(holder);
        waitForPreviewDone();
        terminateMessageLooper();
        assertTrue(mPreviewCallbackResult);

        // Check the order: setPreviewDisplay->startPreview.
        initializeMessageLooper(cameraId);
        mPreviewCallbackResult = false;
        mCamera.setOneShotPreviewCallback(mPreviewCallback);
        mCamera.setPreviewDisplay(holder);
        mCamera.startPreview();
        waitForPreviewDone();
        mCamera.stopPreview();
        assertTrue(mPreviewCallbackResult);

        // Check the order: setting preview display to null->startPreview->
        // setPreviewDisplay.
        mPreviewCallbackResult = false;
        mCamera.setOneShotPreviewCallback(mPreviewCallback);
        mCamera.setPreviewDisplay(null);
        mCamera.startPreview();
        mCamera.setPreviewDisplay(holder);
        waitForPreviewDone();
        terminateMessageLooper();
        assertTrue(mPreviewCallbackResult);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDisplayOrientation",
        args = {int.class}
    )
    @UiThreadTest
    public void testDisplayOrientation() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testDisplayOrientationByCamera(id);
        }
    }

    private void testDisplayOrientationByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);

        // Check valid arguments.
        mCamera.setDisplayOrientation(0);
        mCamera.setDisplayOrientation(90);
        mCamera.setDisplayOrientation(180);
        mCamera.setDisplayOrientation(270);

        // Check invalid arguments.
        try {
            mCamera.setDisplayOrientation(45);
            fail("Should throw exception for invalid arguments");
        } catch (RuntimeException ex) {
            // expected
        }

        // Start preview.
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();

        // Check setting orientation during preview is not allowed.
        try {
            mCamera.setDisplayOrientation(90);
            fail("Should throw exception for setting orientation during preview.");
        } catch (RuntimeException ex) {
            // expected
        }

        terminateMessageLooper();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setParameters",
            args = {android.hardware.Camera.Parameters.class}
        )
    })
    @UiThreadTest
    public void testParameters() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testParametersByCamera(id);
        }
    }

    private void testParametersByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        // we can get parameters just by getxxx method due to the private constructor
        Parameters pSet = mCamera.getParameters();
        assertParameters(pSet);
        terminateMessageLooper();
    }

    // Also test Camera.Parameters
    private void assertParameters(Parameters parameters) {
        // Parameters constants
        final int PICTURE_FORMAT = ImageFormat.JPEG;
        final int PREVIEW_FORMAT = ImageFormat.NV21;
        final int PREVIEW_FRAMERATE = 10;

        // Before setting Parameters
        final int origPictureFormat = parameters.getPictureFormat();
        final int origPictureWidth = parameters.getPictureSize().width;
        final int origPictureHeight = parameters.getPictureSize().height;
        final int origPreviewFormat = parameters.getPreviewFormat();
        final int origPreviewWidth = parameters.getPreviewSize().width;
        final int origPreviewHeight = parameters.getPreviewSize().height;
        final int origPreviewFrameRate = parameters.getPreviewFrameRate();

        assertTrue(origPictureWidth > 0);
        assertTrue(origPictureHeight > 0);
        assertTrue(origPreviewWidth > 0);
        assertTrue(origPreviewHeight > 0);
        assertTrue(origPreviewFrameRate > 0);

        // The default preview format must be yuv420 (NV21).
        assertEquals(ImageFormat.NV21, origPreviewFormat);

        // The default picture format must be Jpeg.
        assertEquals(ImageFormat.JPEG, origPictureFormat);

        // If camera supports flash, the default flash mode must be off.
        String flashMode = parameters.getFlashMode();
        assertTrue(flashMode == null || flashMode.equals(parameters.FLASH_MODE_OFF));

        // Some parameters must be supported.
        List<Size> previewSizes = parameters.getSupportedPreviewSizes();
        List<Size> pictureSizes = parameters.getSupportedPictureSizes();
        List<Integer> previewFormats = parameters.getSupportedPreviewFormats();
        List<Integer> pictureFormats = parameters.getSupportedPictureFormats();
        List<Integer> frameRates = parameters.getSupportedPreviewFrameRates();
        List<String> focusModes = parameters.getSupportedFocusModes();
        String focusMode = parameters.getFocusMode();
        float focalLength = parameters.getFocalLength();
        float horizontalViewAngle = parameters.getHorizontalViewAngle();
        float verticalViewAngle = parameters.getVerticalViewAngle();
        int jpegQuality = parameters.getJpegQuality();
        int jpegThumnailQuality = parameters.getJpegThumbnailQuality();
        assertTrue(previewSizes != null && previewSizes.size() != 0);
        assertTrue(pictureSizes != null && pictureSizes.size() != 0);
        assertTrue(previewFormats != null && previewFormats.size() != 0);
        assertTrue(pictureFormats != null && pictureFormats.size() != 0);
        assertTrue(frameRates != null && frameRates.size() != 0);
        assertTrue(focusModes != null && focusModes.size() != 0);
        assertNotNull(focusMode);
        assertTrue(focalLength > 0);
        assertTrue(horizontalViewAngle > 0 && horizontalViewAngle <= 360);
        assertTrue(verticalViewAngle > 0 && verticalViewAngle <= 360);
        Size previewSize = previewSizes.get(0);
        Size pictureSize = pictureSizes.get(0);
        assertTrue(jpegQuality >= 1 && jpegQuality <= 100);
        assertTrue(jpegThumnailQuality >= 1 && jpegThumnailQuality <= 100);

        // If a parameter is supported, both getXXX and getSupportedXXX have to
        // be non null.
        if (parameters.getWhiteBalance() != null) {
            assertNotNull(parameters.getSupportedWhiteBalance());
        }
        if (parameters.getSupportedWhiteBalance() != null) {
            assertNotNull(parameters.getWhiteBalance());
        }
        if (parameters.getColorEffect() != null) {
            assertNotNull(parameters.getSupportedColorEffects());
        }
        if (parameters.getSupportedColorEffects() != null) {
            assertNotNull(parameters.getColorEffect());
        }
        if (parameters.getAntibanding() != null) {
            assertNotNull(parameters.getSupportedAntibanding());
        }
        if (parameters.getSupportedAntibanding() != null) {
            assertNotNull(parameters.getAntibanding());
        }
        if (parameters.getSceneMode() != null) {
            assertNotNull(parameters.getSupportedSceneModes());
        }
        if (parameters.getSupportedSceneModes() != null) {
            assertNotNull(parameters.getSceneMode());
        }
        if (parameters.getFlashMode() != null) {
            assertNotNull(parameters.getSupportedFlashModes());
        }
        if (parameters.getSupportedFlashModes() != null) {
            assertNotNull(parameters.getFlashMode());
        }

        // Check if the sizes value contain invalid characters.
        assertNoLetters(parameters.get("preview-size-values"), "preview-size-values");
        assertNoLetters(parameters.get("picture-size-values"), "picture-size-values");
        assertNoLetters(parameters.get("jpeg-thumbnail-size-values"),
                "jpeg-thumbnail-size-values");

        // Set the parameters.
        parameters.setPictureFormat(PICTURE_FORMAT);
        assertEquals(PICTURE_FORMAT, parameters.getPictureFormat());
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        assertEquals(pictureSize.width, parameters.getPictureSize().width);
        assertEquals(pictureSize.height, parameters.getPictureSize().height);
        parameters.setPreviewFormat(PREVIEW_FORMAT);
        assertEquals(PREVIEW_FORMAT, parameters.getPreviewFormat());
        parameters.setPreviewFrameRate(frameRates.get(0));
        assertEquals(frameRates.get(0).intValue(), parameters.getPreviewFrameRate());
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        assertEquals(previewSize.width, parameters.getPreviewSize().width);
        assertEquals(previewSize.height, parameters.getPreviewSize().height);

        mCamera.setParameters(parameters);
        Parameters paramActual = mCamera.getParameters();

        assertTrue(isValidPixelFormat(paramActual.getPictureFormat()));
        assertEquals(pictureSize.width, paramActual.getPictureSize().width);
        assertEquals(pictureSize.height, paramActual.getPictureSize().height);
        assertTrue(isValidPixelFormat(paramActual.getPreviewFormat()));
        assertEquals(previewSize.width, paramActual.getPreviewSize().width);
        assertEquals(previewSize.height, paramActual.getPreviewSize().height);
        assertTrue(paramActual.getPreviewFrameRate() > 0);

        checkExposureCompensation(parameters);
    }

    private void checkExposureCompensation(Parameters parameters) {
        assertEquals(0, parameters.getExposureCompensation());
        int max = parameters.getMaxExposureCompensation();
        int min = parameters.getMinExposureCompensation();
        float step = parameters.getExposureCompensationStep();
        if (max == 0 && min == 0) {
            assertEquals(0f, step, 0.000001f);
            return;
        }
        assertTrue(step > 0);
        assertTrue(max >= 0);
        assertTrue(min <= 0);
    }

    private boolean isValidPixelFormat(int format) {
        return (format == ImageFormat.RGB_565) || (format == ImageFormat.NV21)
                || (format == ImageFormat.JPEG) || (format == ImageFormat.YUY2);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setJpegThumbnailSize",
            args = {android.hardware.Camera.Size.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJpegThumbnailSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getJpegSupportedThumbnailSizes",
            args = {}
        )
    })
    @UiThreadTest
    public void testJpegThumbnailSize() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testJpegThumbnailSizeByCamera(id);
        }
    }

    private void testJpegThumbnailSizeByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        // Thumbnail size parameters should have valid values.
        Parameters p = mCamera.getParameters();
        Size size = p.getJpegThumbnailSize();
        assertTrue(size.width > 0 && size.height > 0);
        List<Size> sizes = p.getSupportedJpegThumbnailSizes();
        assertTrue(sizes.size() >= 2);
        assertTrue(sizes.contains(size));
        assertTrue(sizes.contains(mCamera.new Size(0, 0)));

        // Test if the thumbnail size matches the setting.
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        assertTrue(mJpegPictureCallbackResult);
        ExifInterface exif = new ExifInterface(JPEG_PATH);
        assertTrue(exif.hasThumbnail());
        byte[] thumb = exif.getThumbnail();
        Bitmap b = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
        assertEquals(size.width, b.getWidth());
        assertEquals(size.height, b.getHeight());

        // Test no thumbnail case.
        p.setJpegThumbnailSize(0, 0);
        mCamera.setParameters(p);
        Size actual = mCamera.getParameters().getJpegThumbnailSize();
        assertEquals(0, actual.width);
        assertEquals(0, actual.height);
        mCamera.startPreview();
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        assertTrue(mJpegPictureCallbackResult);
        exif = new ExifInterface(JPEG_PATH);
        assertFalse(exif.hasThumbnail());

        terminateMessageLooper();
    }

    @UiThreadTest
    public void testJpegExif() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testJpegExifByCamera(id);
        }
    }

    private void testJpegExifByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        Camera.Parameters parameters = mCamera.getParameters();
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();
        double focalLength = (double)parameters.getFocalLength();
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        ExifInterface exif = new ExifInterface(JPEG_PATH);
        assertNotNull(exif.getAttribute(ExifInterface.TAG_MAKE));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_MODEL));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_DATETIME));
        assertTrue(exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0) != 0);
        assertTrue(exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0) != 0);
        checkGpsDataNull(exif);
        double exifFocalLength = (double)exif.getAttributeDouble(
                ExifInterface.TAG_FOCAL_LENGTH, -1);
        assertEquals(focalLength, exifFocalLength, 0.001);

        // Test gps exif tags.
        mCamera.startPreview();
        parameters.setGpsLatitude(37.736071);
        parameters.setGpsLongitude(-122.441983);
        parameters.setGpsAltitude(21);
        parameters.setGpsTimestamp(1199145600);
        String thirtyTwoCharacters = "GPS NETWORK HYBRID ARE ALL FINE.";
        parameters.setGpsProcessingMethod(thirtyTwoCharacters);
        mCamera.setParameters(parameters);
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        exif = new ExifInterface(JPEG_PATH);
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));
        assertEquals(thirtyTwoCharacters,
                exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));

        // Test gps tags do not exist after calling removeGpsData.
        mCamera.startPreview();
        parameters.removeGpsData();
        mCamera.setParameters(parameters);
        mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
        waitForSnapshotDone();
        exif = new ExifInterface(JPEG_PATH);
        checkGpsDataNull(exif);
        terminateMessageLooper();
    }

    private void checkGpsDataNull(ExifInterface exif) {
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "lock",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unlock",
            args = {}
        )
    })
    @UiThreadTest
    public void testLockUnlock() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testLockUnlockByCamera(id);
        }
    }

    private void testLockUnlockByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        Camera.Parameters parameters = mCamera.getParameters();
        SurfaceHolder surfaceHolder;
        surfaceHolder = getActivity().getSurfaceView().getHolder();
        Size size = parameters.getPreviewSize();
        mCamera.setParameters(parameters);
        mCamera.setPreviewDisplay(surfaceHolder);
        mCamera.startPreview();
        mCamera.lock();  // Locking again from the same process has no effect.
        try {
            recordVideo(size, surfaceHolder);
            fail("Recording should not succeed because camera is locked.");
        } catch (Exception e) {
            // expected
        }

        mCamera.unlock();  // Unlock the camera so media recorder can use it.
        try {
            mCamera.setParameters(parameters);
            fail("setParameters should not succeed because camera is unlocked.");
        } catch (RuntimeException e) {
            // expected
        }

        try {
            recordVideo(size, surfaceHolder);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
        mCamera.lock();  // should not fail
        mCamera.setParameters(parameters);  // should not fail
        terminateMessageLooper();
    }

    private void recordVideo(Size size, SurfaceHolder surfaceHolder) throws Exception {
        MediaRecorder recorder = new MediaRecorder();
        try {
            recorder.setCamera(mCamera);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setOutputFile("/dev/null");
            recorder.setVideoSize(size.width, size.height);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            recorder.setPreviewDisplay(surfaceHolder.getSurface());
            recorder.prepare();
            recorder.start();
            Thread.sleep(5000);
            recorder.stop();
        } finally {
            recorder.release();
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addCallbackBuffer",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPreviewCallbackWithBuffer",
            args = {android.hardware.Camera.PreviewCallback.class}
        )
    })
    @UiThreadTest
    public void testPreviewCallbackWithBuffer() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testPreviewCallbackWithBufferByCamera(id);
        }
    }

    private void testPreviewCallbackWithBufferByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        SurfaceHolder surfaceHolder;
        surfaceHolder = getActivity().getSurfaceView().getHolder();
        mCamera.setPreviewDisplay(surfaceHolder);
        Parameters parameters = mCamera.getParameters();
        PreviewCallbackWithBuffer callback = new PreviewCallbackWithBuffer();
        // Test all preview sizes.
        for (Size size: parameters.getSupportedPreviewSizes()) {
            parameters.setPreviewSize(size.width, size.height);
            mCamera.setParameters(parameters);
            assertEquals(size, mCamera.getParameters().getPreviewSize());
            callback.mNumCbWithBuffer1 = 0;
            callback.mNumCbWithBuffer2 = 0;
            callback.mNumCbWithBuffer3 = 0;
            callback.mBuffer1 = new byte[size.width * size.height * 3 / 2];
            callback.mBuffer2 = new byte[size.width * size.height * 3 / 2];
            callback.mBuffer3 = new byte[size.width * size.height * 3 / 2];

            // Test if we can get the preview callbacks with specified buffers.
            mCamera.addCallbackBuffer(callback.mBuffer1);
            mCamera.addCallbackBuffer(callback.mBuffer2);
            mCamera.setPreviewCallbackWithBuffer(callback);
            mCamera.startPreview();
            waitForPreviewDone();
            assertEquals(1, callback.mNumCbWithBuffer1);
            assertEquals(1, callback.mNumCbWithBuffer2);
            assertEquals(0, callback.mNumCbWithBuffer3);

            // Test if preview callback with buffer still works during preview.
            mCamera.addCallbackBuffer(callback.mBuffer3);
            waitForPreviewDone();
            assertEquals(1, callback.mNumCbWithBuffer1);
            assertEquals(1, callback.mNumCbWithBuffer2);
            assertEquals(1, callback.mNumCbWithBuffer3);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
        }
        terminateMessageLooper();
    }

    private final class PreviewCallbackWithBuffer
            implements android.hardware.Camera.PreviewCallback {
        public int mNumCbWithBuffer1, mNumCbWithBuffer2, mNumCbWithBuffer3;
        public byte[] mBuffer1, mBuffer2, mBuffer3;
        public void onPreviewFrame(byte[] data, Camera camera) {
            assertNotNull(data);
            if (data == mBuffer1) {
                mNumCbWithBuffer1++;
            } else if (data == mBuffer2) {
                mNumCbWithBuffer2++;
            } else if (data == mBuffer3) {
                mNumCbWithBuffer3++;
            } else {
                fail("Invalid byte array.");
            }

            if ((mNumCbWithBuffer1 == 1 && mNumCbWithBuffer2 == 1)
                    || mNumCbWithBuffer3 == 1) {
                mPreviewDone.open();
            }
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSmoothZoom",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopSmoothZoom",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setZoomChangeListener",
            args = {android.hardware.Camera.OnZoomChangeListener.class}
        )
    })
    @UiThreadTest
    public void testZoom() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            initializeMessageLooper(id);
            testImmediateZoom();
            testSmoothZoom();
            terminateMessageLooper();
        }
    }

    private void testImmediateZoom() throws Exception {
        Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) return;

        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());

        // Test the zoom parameters.
        assertEquals(0, parameters.getZoom());  // default zoom should be 0.
        for (Size size: parameters.getSupportedPreviewSizes()) {
            parameters = mCamera.getParameters();
            parameters.setPreviewSize(size.width, size.height);
            mCamera.setParameters(parameters);
            parameters = mCamera.getParameters();
            int maxZoom = parameters.getMaxZoom();
            assertTrue(maxZoom >= 0);

            // Zoom ratios should be sorted from small to large.
            List<Integer> ratios = parameters.getZoomRatios();
            assertEquals(maxZoom + 1, ratios.size());
            assertEquals(100, ratios.get(0).intValue());
            for (int i = 0; i < ratios.size() - 1; i++) {
                assertTrue(ratios.get(i) < ratios.get(i + 1));
            }
            mCamera.startPreview();
            waitForPreviewDone();

            // Test each zoom step.
            for (int i = 0; i <= maxZoom; i++) {
                parameters.setZoom(i);
                mCamera.setParameters(parameters);
                assertEquals(i, mCamera.getParameters().getZoom());
            }

            // It should throw exception if an invalid value is passed.
            try {
                parameters.setZoom(maxZoom + 1);
                mCamera.setParameters(parameters);
                fail("setZoom should throw exception.");
            } catch (RuntimeException e) {
                // expected
            }
            assertEquals(maxZoom, mCamera.getParameters().getZoom());

            mCamera.takePicture(mShutterCallback, mRawPictureCallback,
                                mJpegPictureCallback);
            waitForSnapshotDone();
        }
    }

    private void testSmoothZoom() throws Exception {
        Parameters parameters = mCamera.getParameters();
        if (!parameters.isSmoothZoomSupported()) return;
        assertTrue(parameters.isZoomSupported());

        ZoomListener zoomListener = new ZoomListener();
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.setZoomChangeListener(zoomListener);
        mCamera.startPreview();
        waitForPreviewDone();

        // Immediate zoom should not generate callbacks.
        int maxZoom = parameters.getMaxZoom();
        parameters.setZoom(maxZoom);
        mCamera.setParameters(parameters);
        assertEquals(maxZoom, mCamera.getParameters().getZoom());
        parameters.setZoom(0);
        mCamera.setParameters(parameters);
        assertEquals(0, mCamera.getParameters().getZoom());
        assertFalse(zoomListener.mZoomDone.block(500));

        // Nothing will happen if zoom is not moving.
        mCamera.stopSmoothZoom();

        // It should not generate callbacks if zoom value is not changed.
        mCamera.startSmoothZoom(0);
        assertFalse(zoomListener.mZoomDone.block(500));

        // Test startSmoothZoom.
        mCamera.startSmoothZoom(maxZoom);
        assertEquals(true, zoomListener.mZoomDone.block(5000));
        assertEquals(maxZoom, zoomListener.mValues.size());
        for(int i = 0; i < maxZoom; i++) {
            // Make sure we get all the callbacks in order.
            assertEquals(i + 1, zoomListener.mValues.get(i).intValue());
        }

        // Test startSmoothZoom. Make sure we get all the callbacks.
        if (maxZoom > 1) {
            zoomListener.mValues = new ArrayList<Integer>();
            zoomListener.mStopped = false;
            Log.e(TAG, "zoomListener.mStopped = " + zoomListener.mStopped);
            zoomListener.mZoomDone.close();
            mCamera.startSmoothZoom(maxZoom / 2);
            assertTrue(zoomListener.mZoomDone.block(5000));
            assertEquals(maxZoom - (maxZoom / 2), zoomListener.mValues.size());
            int i = maxZoom - 1;
            for(Integer value: zoomListener.mValues) {
                assertEquals(i, value.intValue());
                i--;
            }
        }

        // It should throw exception if an invalid value is passed.
        try {
            mCamera.startSmoothZoom(maxZoom + 1);
            fail("startSmoothZoom should throw exception.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Test stopSmoothZoom.
        zoomListener.mValues = new ArrayList<Integer>();
        zoomListener.mStopped = false;
        zoomListener.mZoomDone.close();
        parameters.setZoom(0);
        mCamera.setParameters(parameters);
        assertEquals(0, mCamera.getParameters().getZoom());
        mCamera.startSmoothZoom(maxZoom);
        mCamera.stopSmoothZoom();
        assertTrue(zoomListener.mZoomDone.block(5000));
        for(int i = 0; i < zoomListener.mValues.size() - 1; i++) {
            // Make sure we get all the callbacks in order (except the last).
            assertEquals(i + 1, zoomListener.mValues.get(i).intValue());
        }
    }

    private final class ZoomListener
            implements android.hardware.Camera.OnZoomChangeListener {
        public ArrayList<Integer> mValues = new ArrayList<Integer>();
        public boolean mStopped;
        public final ConditionVariable mZoomDone = new ConditionVariable();

        public void onZoomChange(int value, boolean stopped, Camera camera) {
            mValues.add(value);
            assertFalse(mStopped);
            mStopped = stopped;
            if (stopped) {
                mZoomDone.open();
            }
        }
    }

    @UiThreadTest
    public void testFocusDistances() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testFocusDistancesByCamera(id);
        }
    }

    private void testFocusDistancesByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();
        waitForPreviewDone();
        Parameters parameters = mCamera.getParameters();

        // Test every supported focus mode.
        for (String focusMode: parameters.getSupportedFocusModes()) {
            parameters.setFocusMode(focusMode);
            mCamera.setParameters(parameters);
            parameters = mCamera.getParameters();
            assertEquals(focusMode, parameters.getFocusMode());
            checkFocusDistances(parameters);
            if (Parameters.FOCUS_MODE_AUTO.equals(focusMode)
                    || Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
                mCamera.autoFocus(mAutoFocusCallback);
                assertTrue(waitForFocusDone());
                parameters = mCamera.getParameters();
                checkFocusDistances(parameters);
            }
        }

        // Test if the method throws exception if the argument is invalid.
        try {
            parameters.getFocusDistances(null);
            fail("getFocusDistances should not accept null.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            parameters.getFocusDistances(new float[2]);
            fail("getFocusDistances should not accept a float array with two elements.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            parameters.getFocusDistances(new float[4]);
            fail("getFocusDistances should not accept a float array with four elements.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        terminateMessageLooper();
    }

    private void checkFocusDistances(Parameters parameters) {
        float[] distances = new float[3];
        parameters.getFocusDistances(distances);

        // Focus distances should be greater than 0.
        assertTrue(distances[Parameters.FOCUS_DISTANCE_NEAR_INDEX] > 0);
        assertTrue(distances[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX] > 0);
        assertTrue(distances[Parameters.FOCUS_DISTANCE_FAR_INDEX] > 0);

        // Make sure far focus distance >= optimal focus distance >= near focus distance.
        assertTrue(distances[Parameters.FOCUS_DISTANCE_FAR_INDEX] >=
                   distances[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX]);
        assertTrue(distances[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX] >=
                   distances[Parameters.FOCUS_DISTANCE_NEAR_INDEX]);

        // Far focus distance should be infinity in infinity focus mode.
        if (Parameters.FOCUS_MODE_INFINITY.equals(parameters.getFocusMode())) {
            assertEquals(Float.POSITIVE_INFINITY,
                         distances[Parameters.FOCUS_DISTANCE_FAR_INDEX]);
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancelAutofocus",
            args = {}
        )
    })
    @UiThreadTest
    public void testCancelAutofocus() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testCancelAutofocusByCamera(id);
        }
    }

    private void testCancelAutofocusByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();

        // No op if autofocus is not in progress.
        mCamera.cancelAutoFocus();

        // Try to cancel autofocus immediately.
        mCamera.autoFocus(mAutoFocusCallback);
        mCamera.cancelAutoFocus();
        checkFocusDistanceNotChanging();

        // Try to cancel autofocus after it starts for some time.
        mCamera.autoFocus(mAutoFocusCallback);
        Thread.sleep(500);
        mCamera.cancelAutoFocus();
        checkFocusDistanceNotChanging();

        // Try to cancel autofocus after it completes. It should be no op.
        mCamera.autoFocus(mAutoFocusCallback);
        assertTrue(waitForFocusDone());
        mCamera.cancelAutoFocus();

        // Test the case calling cancelAutoFocus and release in a row.
        mCamera.autoFocus(mAutoFocusCallback);
        mCamera.cancelAutoFocus();
        mCamera.release();

        // Ensure the camera can be opened if release is called right after AF.
        mCamera = Camera.open(cameraId);
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        mCamera.startPreview();
        mCamera.autoFocus(mAutoFocusCallback);
        mCamera.release();

        terminateMessageLooper();
    }

    private void checkFocusDistanceNotChanging() throws Exception {
        float[] distances1 = new float[3];
        float[] distances2 = new float[3];
        Parameters parameters = mCamera.getParameters();
        parameters.getFocusDistances(distances1);
        Thread.sleep(100);
        parameters = mCamera.getParameters();
        parameters.getFocusDistances(distances2);
        assertEquals(distances1[Parameters.FOCUS_DISTANCE_NEAR_INDEX],
                     distances2[Parameters.FOCUS_DISTANCE_NEAR_INDEX]);
        assertEquals(distances1[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX],
                     distances2[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX]);
        assertEquals(distances1[Parameters.FOCUS_DISTANCE_FAR_INDEX],
                     distances2[Parameters.FOCUS_DISTANCE_FAR_INDEX]);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNumberOfCameras",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCameraInfo",
            args = {int.class, CameraInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {int.class}
        )
    })
    @UiThreadTest
    public void testMultipleCameras() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        Log.v(TAG, "total " + nCameras + " cameras");
        assertTrue(nCameras >= 0);

        boolean backCameraExist = false;
        CameraInfo info = new CameraInfo();
        for (int i = 0; i < nCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                backCameraExist = true;
                break;
            }
        }
        // Make sure original open still works. It must return a back-facing
        // camera.
        mCamera = Camera.open();
        if (mCamera != null) {
            mCamera.release();
            assertTrue(backCameraExist);
        } else {
            assertFalse(backCameraExist);
        }

        for (int id = -1; id <= nCameras; id++) {
            Log.v(TAG, "testing camera #" + id);

            boolean isBadId = (id < 0 || id >= nCameras);

            try {
                Camera.getCameraInfo(id, info);
                if (isBadId) {
                    fail("getCameraInfo should not accept bad cameraId (" + id + ")");
                }
            } catch (RuntimeException e) {
                if (!isBadId) throw e;
            }

            int facing = info.facing;
            int orientation = info.orientation;
            assertTrue(facing == CameraInfo.CAMERA_FACING_BACK ||
                       facing == CameraInfo.CAMERA_FACING_FRONT);
            assertTrue(orientation == 0 || orientation == 90 ||
                       orientation == 180 || orientation == 270);

            Camera camera = null;
            try {
                camera = Camera.open(id);
                if (isBadId) {
                    fail("open() should not accept bad cameraId (" + id + ")");
                }
            } catch (RuntimeException e) {
                if (!isBadId) throw e;
            } finally {
                if (camera != null) {
                    camera.release();
                }
            }
        }
    }

    @UiThreadTest
    public void testPreviewPictureSizesCombination() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testPreviewPictureSizesCombinationByCamera(id);
        }
    }

    private void testPreviewPictureSizesCombinationByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());
        Parameters parameters = mCamera.getParameters();
        PreviewCbForPreviewPictureSizesCombination callback =
            new PreviewCbForPreviewPictureSizesCombination();

        // Test all combination of preview sizes and picture sizes.
        for (Size previewSize: parameters.getSupportedPreviewSizes()) {
            for (Size pictureSize: parameters.getSupportedPictureSizes()) {
                Log.v(TAG, "Test previewSize=(" + previewSize.width + "," +
                        previewSize.height + ") pictureSize=(" +
                        pictureSize.width + "," + pictureSize.height + ")");
                mPreviewCallbackResult = false;
                mCamera.setPreviewCallback(callback);
                callback.expectedPreviewSize = previewSize;
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                mCamera.setParameters(parameters);
                assertEquals(previewSize, mCamera.getParameters().getPreviewSize());
                assertEquals(pictureSize, mCamera.getParameters().getPictureSize());

                // Check if the preview size is the same as requested.
                mCamera.startPreview();
                waitForPreviewDone();
                assertTrue(mPreviewCallbackResult);

                // Check if the picture size is the same as requested.
                mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
                waitForSnapshotDone();
                assertTrue(mJpegPictureCallbackResult);
                assertNotNull(mJpegData);
                Bitmap b = BitmapFactory.decodeByteArray(mJpegData, 0, mJpegData.length);
                assertEquals(pictureSize.width, b.getWidth());
                assertEquals(pictureSize.height, b.getHeight());
                b.recycle();
                b = null;
            }
        }
        terminateMessageLooper();
    }

    private final class PreviewCbForPreviewPictureSizesCombination
            implements android.hardware.Camera.PreviewCallback {
        public Size expectedPreviewSize;
        public void onPreviewFrame(byte[] data, Camera camera) {
            assertNotNull(data);
            Size size = camera.getParameters().getPreviewSize();
            assertEquals(expectedPreviewSize, size);
            assertEquals(size.width * size.height * 3 / 2, data.length);
            camera.setPreviewCallback(null);
            mPreviewCallbackResult = true;
            mPreviewDone.open();
        }
    }

    public void testPreviewFpsRange() throws Exception {
        int nCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < nCameras; id++) {
            Log.v(TAG, "Camera id=" + id);
            testPreviewFpsRangeByCamera(id);
        }
    }

    private void testPreviewFpsRangeByCamera(int cameraId) throws Exception {
        initializeMessageLooper(cameraId);
        mCamera.setPreviewDisplay(getActivity().getSurfaceView().getHolder());

        // Test if the parameters exists and minimum fps <= maximum fps.
        int[] defaultFps = new int[2];
        Parameters parameters = mCamera.getParameters();
        parameters.getPreviewFpsRange(defaultFps);
        List<int[]> fpsList = parameters.getSupportedPreviewFpsRange();
        assertTrue(fpsList.size() > 0);
        boolean found = false;
        for(int[] fps: fpsList) {
            assertTrue(fps[Parameters.PREVIEW_FPS_MIN_INDEX] > 0);
            assertTrue(fps[Parameters.PREVIEW_FPS_MIN_INDEX] <=
                       fps[Parameters.PREVIEW_FPS_MAX_INDEX]);
            if (!found && Arrays.equals(defaultFps, fps)) {
                found = true;
            }
        }
        assertTrue("Preview fps range must be in the supported list.", found);

        // Test if the list is properly sorted.
        for (int i = 0; i < fpsList.size() - 1; i++) {
            int minFps1 = fpsList.get(i)[Parameters.PREVIEW_FPS_MIN_INDEX];
            int maxFps1 = fpsList.get(i)[Parameters.PREVIEW_FPS_MAX_INDEX];
            int minFps2 = fpsList.get(i + 1)[Parameters.PREVIEW_FPS_MIN_INDEX];
            int maxFps2 = fpsList.get(i + 1)[Parameters.PREVIEW_FPS_MAX_INDEX];
            assertTrue(maxFps1 < maxFps2
                    || (maxFps1 == maxFps2 && minFps1 < minFps2));
        }

        // Test if the actual fps is within fps range.
        Size size = parameters.getPreviewSize();
        byte[] buffer1 = new byte[size.width * size.height * 3 / 2];
        byte[] buffer2 = new byte[size.width * size.height * 3 / 2];
        byte[] buffer3 = new byte[size.width * size.height * 3 / 2];
        FpsRangePreviewCb callback = new FpsRangePreviewCb();
        int[] readBackFps = new int[2];
        for (int[] fps: fpsList) {
            parameters = mCamera.getParameters();
            parameters.setPreviewFpsRange(fps[Parameters.PREVIEW_FPS_MIN_INDEX],
                                          fps[Parameters.PREVIEW_FPS_MAX_INDEX]);
            callback.reset(fps[Parameters.PREVIEW_FPS_MIN_INDEX] / 1000.0,
                           fps[Parameters.PREVIEW_FPS_MAX_INDEX] / 1000.0);
            mCamera.setParameters(parameters);
            parameters = mCamera.getParameters();
            parameters.getPreviewFpsRange(readBackFps);
            MoreAsserts.assertEquals(fps, readBackFps);
            mCamera.addCallbackBuffer(buffer1);
            mCamera.addCallbackBuffer(buffer2);
            mCamera.addCallbackBuffer(buffer3);
            mCamera.setPreviewCallbackWithBuffer(callback);
            mCamera.startPreview();
            try {
                // Test the frame rate for a while.
                Thread.sleep(3000);
            } catch(Exception e) {
                // ignore
            }
            mCamera.stopPreview();
        }

        // Test the invalid fps cases.
        parameters = mCamera.getParameters();
        parameters.setPreviewFpsRange(-1, -1);
        try {
            mCamera.setParameters(parameters);
            fail("Should throw an exception if fps range is negative.");
        } catch (RuntimeException e) {
            // expected
        }
        parameters.setPreviewFpsRange(10, 5);
        try {
            mCamera.setParameters(parameters);
            fail("Should throw an exception if fps range is invalid.");
        } catch (RuntimeException e) {
            // expected
        }

        terminateMessageLooper();
    }

    private final class FpsRangePreviewCb
            implements android.hardware.Camera.PreviewCallback {
        private double mMinFps, mMaxFps, mMaxFrameInterval, mMinFrameInterval;
        // An array storing the arrival time of the frames in the last second.
        private ArrayList<Long> mFrames = new ArrayList<Long>();
        private long firstFrameArrivalTime;

        public void reset(double minFps, double maxFps) {
            this.mMinFps = minFps;
            this.mMaxFps = maxFps;
            mMaxFrameInterval = 1000.0 / mMinFps;
            mMinFrameInterval = 1000.0 / mMaxFps;
            Log.v(TAG, "Min fps=" + mMinFps + ". Max fps=" + mMaxFps
                    + ". Min frame interval=" + mMinFrameInterval
                    + ". Max frame interval=" + mMaxFrameInterval);
            mFrames.clear();
            firstFrameArrivalTime = 0;
        }

        // This method tests if the actual fps is between minimum and maximum.
        // It also tests if the frame interval is too long.
        public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
            long arrivalTime = System.currentTimeMillis();
            camera.addCallbackBuffer(data);
            if (firstFrameArrivalTime == 0) firstFrameArrivalTime = arrivalTime;

            // Remove the frames that arrived before the last second.
            Iterator<Long> it = mFrames.iterator();
            while(it.hasNext()) {
                long time = it.next();
                if (arrivalTime - time > 1000 && mFrames.size() > 2) {
                    it.remove();
                } else {
                    break;
                }
            }

            // Start the test after one second.
            if (arrivalTime - firstFrameArrivalTime > 1000) {
                assertTrue(mFrames.size() >= 2);

                // Check the frame interval and fps. The interval check
                // considers the time variance passing frames from the camera
                // hardware to the callback. It should be a constant time, not a
                // ratio. The fps check is more strict because individual
                // variance is averaged out.

                // Check if the frame interval is too large or too small.
                double intervalMargin = 30;  // ms
                long lastArrivalTime = mFrames.get(mFrames.size() - 1);
                double interval = arrivalTime - lastArrivalTime;
                if (LOGV) Log.v(TAG, "Frame interval=" + interval);
                assertTrue("Frame interval (" + interval + "ms) is too large." +
                        " mMaxFrameInterval=" + mMaxFrameInterval + "ms",
                        interval < mMaxFrameInterval + intervalMargin);
                assertTrue("Frame interval (" + interval + "ms) is too small." +
                        " mMinFrameInterval=" + mMinFrameInterval + "ms",
                        interval > mMinFrameInterval - intervalMargin);

                // Check if the fps is within range.
                double fpsMargin = 0.03;
                double avgInterval = (double)(arrivalTime - mFrames.get(0))
                        / mFrames.size();
                double fps = 1000.0 / avgInterval;
                assertTrue("Actual fps (" + fps + ") should be larger than " +
                           "min fps (" + mMinFps + ")",
                           fps >= mMinFps * (1 - fpsMargin));
                assertTrue("Actual fps (" + fps + ") should be smaller than " +
                           "max fps (" + mMaxFps + ")",
                           fps <= mMaxFps * (1 + fpsMargin));
            }
            // Add the arrival time of this frame to the list.
            mFrames.add(arrivalTime);
        }
    }

    private void assertEquals(Size expected, Size actual) {
        assertEquals(expected.width, actual.width);
        assertEquals(expected.height, actual.height);
    }

    private void assertNoLetters(String value, String key) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            assertFalse("Parameter contains invalid characters. key,value=("
                    + key + "," + value + ")",
                    Character.isLetter(c) && c != 'x');
        }
    }
}
