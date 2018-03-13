package com.ffmpeg.maikel.video.presenter.impl;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CameraProfile;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.ffmpeg.maikel.video.bean.SurpportedSize;
import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.ui.presenter.MainViewPresenter;
import com.ffmpeg.maikel.video.utils.CameraUtils;
import com.ffmpeg.maikel.video.utils.ScreenUtils;
import com.ffmpeg.maikel.video.video.VideoFrameCallback;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by maikel on 2018/3/12.
 */

public class CameraPresenterImpl implements CameraPresenter {
    private static final String TAG = CameraPresenterImpl.class.getSimpleName();
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private int mCameraId = FRONT_CAMERA;
    private Camera mCamera = null;
    private int defaultWidth;
    private int defaultHeight;
    private MainViewPresenter mView;

    private boolean isPreview;
    private Camera.Size mPreviewSize;

    private VideoFrameCallback videoFrameCallback;

    private String savedFormat;
    private int savedWidth;
    private int savedHeight;
    private int savedFrameRate;
    //private Camera.Size mPreviewSize;
    private Camera.Size size;
    private int mLastOrientation;
    private String fourCC;
    private PixelFormat pixelFormat;
    private boolean stopping;
    private SurfaceHolder mSurfaceHolder;
    private Camera.ErrorCallback errorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            Log.e(TAG, "Begin errorCallback.onError");
            if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
                Log.e(TAG, "Received CAMERA_ERROR_SERVER_DIED");
                stop();
                start(savedFormat, savedWidth, savedHeight, savedFrameRate);
            }
            Log.e(TAG, "End errorCallback.onError");
        }
    };

    public CameraPresenterImpl(MainViewPresenter view) {
        mView = view;
        mView.setPresenter(this);
        defaultWidth = ScreenUtils.getScreenDensity(mView.getViewContext()).widthPixels;
        defaultHeight = ScreenUtils.getScreenDensity(mView.getViewContext()).heightPixels;
    }

    private void initCamera() {
        try {
            mCamera = Camera.open(mCameraId);
            if (videoFrameCallback == null){
                videoFrameCallback = new VideoFrameCallback();
            }
            mCamera.setPreviewCallback(videoFrameCallback);
            //new Thread(videoFrameCallback).start();
            mCamera.setDisplayOrientation(90);
            mView.cameraOpened(mCamera);
            mView.getCameraView().initSurface(this);
        } catch (Exception e) {
            Log.e(TAG, "open camera error:" + e.getMessage());
            mView.cameraError();
        }
    }


    @Override
    public void rotation(int orientation) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraId, info);
        int degrees = 0;
        switch (orientation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(result);
        }
    }

    @Override
    public void switchCamera() {
        if (mCameraId==FRONT_CAMERA){
            mCameraId = BACK_CAMERA;
        }else {
            mCameraId = FRONT_CAMERA;
        }
        stopPreview();
        if (mCamera!=null){
            mCamera.release();
            mCamera = null;
        }
        initCamera();
        surfaceDisplay(mSurfaceHolder);
    }

    @Override
    public void start() {
        initCamera();
    }


    private boolean start(String format, int width, int height, int frameRate) {
        savedFormat = format;
        savedWidth = width;
        savedHeight = height;
        savedFrameRate = frameRate;
        Camera.Parameters parameters;
        int pixelFormatInt = 0;
        boolean forcedOrientation = false;
        Log.i(TAG, "	public boolean start(String format:" + format + ", int width:" + width + ", int height:" + height + ", int frameRate:" + frameRate + ")");
        SurpportedSize[] capabilityArray = CameraUtils.getSurpportedSize(mCamera);
        try {
            /* Check whether the resolution matches a particular capability. */
            boolean resolutionFound = false;
            for (int i = 0; i < capabilityArray.length; i++) {
                if ((capabilityArray[i].width == width) && (capabilityArray[i].height == height)) {
                    resolutionFound = true;
                    break;
                }
            }
            /* If there is no match, pick the resolution which is less than and closest to the resolution set, based on height. */
            if (!resolutionFound) {
                int newWidth = 0;
                int newHeight = 0;
                for (int i = 0; i < capabilityArray.length; i++) {
                    int curHeight = capabilityArray[i].height;
                    if (curHeight <= height && curHeight > newHeight) {
                        newHeight = curHeight;
                        newWidth = capabilityArray[i].width;
                    }
                }
                width = newWidth;
                height = newHeight;
            }
            int minFrameRateMatch = 0;
            int minFrameRate = 0;
            int maxFrameRate = 0;

			/* The frame rate provided is the max frame rate, find the min frame rate which is closest to the max frame rate.
             * If there is no matching frame rate, choose the one which is less than and closest to the provided frame rate. */
            for (int i = 0; i < capabilityArray.length; i++) {
                int curMaxFrameRate = capabilityArray[i].samplingRate;
                int curMinFrameRate = capabilityArray[i].samplingRateMin;
                if (curMaxFrameRate == frameRate) {
                    if (curMinFrameRate > minFrameRateMatch)
                        minFrameRateMatch = curMinFrameRate;
                    maxFrameRate = curMaxFrameRate;
                } else if (curMaxFrameRate < frameRate) {
                    if (curMaxFrameRate == maxFrameRate) {
                        if (curMinFrameRate > minFrameRate)
                            minFrameRate = curMinFrameRate;
                    } else if (curMaxFrameRate > maxFrameRate) {
                        minFrameRate = curMinFrameRate;
                        maxFrameRate = curMaxFrameRate;
                    }
                }
            }

			/* If there is an exact match, pick the min frame Rate from the one that closely matches the max frame rate. */
            if (maxFrameRate == frameRate) {
                minFrameRate = minFrameRateMatch;
            }

            Log.d(TAG, "Starting Camera. format: " + format + " width:" + width + " height:" + height + " min-frameRate: " + minFrameRate + " max-frameRate: " + maxFrameRate);

            parameters = mCamera.getParameters();
            /* Scale up by 1000 while setting fps, as represented by the camera parameters. */
            if (minFrameRate > 0 && maxFrameRate < minFrameRate)
                parameters.setPreviewFpsRange(minFrameRate * 1000, maxFrameRate * 1000);
            parameters.setPreviewSize(width, height);
            //parameters.setPreviewFormat(CameraUtils.YV12);
            mCamera.setParameters(parameters);
            /* get parameters and verify them */
            parameters = mCamera.getParameters();
            size = parameters.getPreviewSize();
            pixelFormatInt = parameters.getPreviewFormat();
            fourCC = CameraUtils.pixelFormatToString(pixelFormatInt);
            Log.d(TAG, "Pixel format: " + fourCC);
            pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(pixelFormatInt, pixelFormat);
            //setAdvancedCameraParameters();
            parameters = mCamera.getParameters();
            initializeBuffers();
            setPreviewCallbackWithBuffer(this);
            Log.i(TAG, "Camera Started");
        } catch (Exception ex) {
            if (mCamera != null)
                mCamera.release();
            Log.e(TAG, "Unable to start camera" + ex.toString());
            return false;
        }

        if (mCamera != null) {
            mCamera.setErrorCallback(errorCallback);
        }
        return true;
    }

    @Override
    public void displayFailure() {
        mView.displayError();
    }

    @Override
    public void startPreview(SurfaceHolder surfaceHolder) {
        if (mCamera == null) {
            Log.e(TAG, "<surfaceCreated> camera not found please check!");
            finishMain();
            return;
        }
        mSurfaceHolder = surfaceHolder;
        surfaceDisplay(surfaceHolder);
    }

    private void surfaceDisplay(SurfaceHolder surfaceHolder) {
        stopPreview();
        setCameraParams();
//        if (!start(CameraUtils.pixelFormatToString(CameraUtils.JPEG), defaultWidth, defaultHeight, 30)) {
//            finishMain();
//            return;
//        }
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            isPreview = true;
            mView.cameraSate(isPreview);
            //videoFrameCallback.videoStart(isPreview);
        } catch (IOException e) {
            Log.e(TAG, "<startPreview> camera can not preview!" + e.getMessage());
            finishMain();
        }
    }

    private void stopPreview() {
        if (mCamera != null && isPreview) {
            mCamera.stopPreview();
            isPreview = false;
        }
    }

    private void setCameraParams() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        mPreviewSize = getFixedSize(sizes, defaultWidth, defaultHeight);
//        //设置预览的宽度、高度
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        //设置帧率30帧，这里应该做成动态帧率根据网络状态
        parameters.setPreviewFrameRate(30);
        mCamera.setParameters(parameters);
    }

    /**
     * 查找合适的size
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getFixedSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null) {
            return null;
        }
        Camera.Size fixedSize = null;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        float minDiff = Float.MAX_VALUE;
        int targetHeight = h;
        //找到比较合适的尺寸
        for (Camera.Size size : sizes) {
            float ratio = (float) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                fixedSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        //没有找到合适的，则忽略ASPECT_TOLERANCE
        if (fixedSize == null) {
            minDiff = Float.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    fixedSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return fixedSize;
    }

    public void stop() {
        Log.i(TAG, "stop");
        stopping = true;
        if (mCamera != null) {
            destroy();
            Log.i(TAG, "stop: Camera stopped");
        }
        Log.i(TAG, "stop: draining frames");
        drainFrames();
    }

    private void initializeBuffers() {
        int bufSize = (size.width * size.height * pixelFormat.bitsPerPixel) / 8;

        Log.d(TAG, "Using callback buffers");

		/*
         * Must call this before calling addCallbackBuffer to get all the
		 * reflection variables setup
		 */
        initializeAddCallbackBufferMethod();
        /*
		 * Add three buffers to the buffer queue. I re-queue them once they are
		 * used in onPreviewFrame, so we should not need many of them.
		 */
        byte[] buffer = new byte[bufSize];
        addCallbackBuffer(buffer);
        buffer = new byte[bufSize];
        addCallbackBuffer(buffer);
        buffer = new byte[bufSize];
        addCallbackBuffer(buffer);
    }

    private void setAdvancedCameraParameters() {
        Log.i(TAG, "Setting advanced camera parameters");
        Camera.Parameters mParameters = mCamera.getParameters();

        // Set flash mode.
        String flashMode = Camera.Parameters.FLASH_MODE_OFF;

        List<String> supportedFlash = mParameters.getSupportedFlashModes();
        if (CameraUtils.isSupported(flashMode, supportedFlash)) {
            mParameters.setFlashMode(flashMode);
        } else {
            flashMode = mParameters.getFlashMode();
            if (flashMode == null) {
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
        }

        // Set white balance parameter.
        String whiteBalance = Camera.Parameters.WHITE_BALANCE_AUTO;
        if (CameraUtils.isSupported(whiteBalance,
                mParameters.getSupportedWhiteBalance())) {
            mParameters.setWhiteBalance(whiteBalance);
        } else {
            whiteBalance = mParameters.getWhiteBalance();
            if (whiteBalance == null) {
                whiteBalance = Camera.Parameters.WHITE_BALANCE_AUTO;
            }
        }

        // Set zoom.
        if (mParameters.isZoomSupported()) {
            mParameters.setZoom(0);
        }

        // Set continuous autofocus. API9
        if (Build.MANUFACTURER.toLowerCase().equalsIgnoreCase("amazon") && Build.DEVICE.equalsIgnoreCase("d01e")) {
            Log.i(TAG, "support for autofocus is off for amazon kindle HD");
        } else {
            List<String> supportedFocus = mParameters.getSupportedFocusModes();
            if (CameraUtils.isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }

        if (Build.MANUFACTURER.toLowerCase().equalsIgnoreCase("samsung") &&
                Build.DEVICE.equalsIgnoreCase("manta") &&
                Build.MODEL.equalsIgnoreCase("Nexus 10")) {
            Log.i(TAG, "video-stabilization-supported is off for Nexus 10");
        } else {
            String stabSupported = mParameters.get("video-stabilization-supported");
            if ("true".equals(stabSupported)) {
                mParameters.set("video-stabilization", "true");
            }
        }

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(CameraProfile.QUALITY_HIGH);
        mParameters.setJpegQuality(jpegQuality);

        mCamera.setParameters(mParameters);
    }


    /**
     * Use this method instead of setPreviewCallback if you want to use manually
     * allocated buffers. Assumes that "this" implements Camera.PreviewCallback
     */
    private void setPreviewCallbackWithBuffer(Object previewCallbackObj) {
        try {
            Class c = Class.forName("android.hardware.Camera");

            Method spcwb = null;
			/*
			 * This way of finding our method is a bit inefficient, but I am a
			 * reflection novice, and didn't want to waste the time figuring out
			 * the right way to do it. since this method is only called once,
			 * this should not cause performance issues
			 */
            Method[] m = c.getMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].getName().compareTo("setPreviewCallbackWithBuffer") == 0) {
                    spcwb = m[i];
                    break;
                }
            }

			/*
			 * If we were able to find the setPreviewCallbackWithBuffer method
			 * of Camera, we can now invoke it on our Camera instance, setting
			 * 'this' to be the callback handler
			 */
            if (spcwb != null) {
                Object[] arglist = new Object[1];
                arglist[0] = previewCallbackObj;
                spcwb.invoke(mCamera, arglist);
                Log.i(TAG, "setPreviewCallbackWithBuffer: Called method");
            } else {
                Log.i(TAG, "setPreviewCallbackWithBuffer: Did not find method");
            }

        } catch (Exception e) {
			/* TODO Auto-generated catch block */
            Log.i(TAG, "setPreviewCallbackWithBuffer error" + e.toString());
        }
    }

    /**
     * These variables are re-used over and over by addCallbackBuffer
     */
    Method mAcb;
    Object[] mArglist;

    private void initializeAddCallbackBufferMethod() {
        try {
            Class mC = Class.forName("android.hardware.Camera");

            Class[] mPartypes = new Class[1];
            mPartypes[0] = (new byte[1]).getClass(); // There is probably a
            // better way to do
            // this.
            mAcb = mC.getMethod("addCallbackBuffer", mPartypes);
            mArglist = new Object[1];
        } catch (Exception e) {
            Log.e(TAG, "Problem setting up for addCallbackBuffer: "
                    + e.toString());
        }
    }

    /**
     * This method allows you to add a byte buffer to the queue of buffers to be
     * used by preview. See:
     * http://android.git.kernel.org/?p=platform/frameworks
     * /base.git;a=blob;f=core/java/android/hardware/Camera.java;hb=9d
     * b3d07b9620b4269ab33f78604a36327e536ce1
     *
     * @param b The buffer to register. Size should be width * height *
     *          bitsPerPixel / 8.
     */
    private void addCallbackBuffer(byte[] b) {
        if (stopping)
            return;
		/*
		 * Check to be sure initializeAddCallbackBufferMethod has been called to
		 * setup mAcb and mArglist
		 */
        if (mArglist == null) {
            initializeAddCallbackBufferMethod();
        }

        mArglist[0] = b;
        try {
            mAcb.invoke(mCamera, mArglist);
        } catch (Exception e) {
            Log.e(TAG, "invoking addCallbackBuffer failed: " + e.toString());
        }
    }


    private void drainFrames() {

        mArglist = null;
    }

    @Override
    public void destroy() {
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
                isPreview = false;
            }
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if (videoFrameCallback != null) {
            videoFrameCallback.videoStart(false);
        }
    }

    @Override
    public void finishMain() {
        mView.destroy();
    }


}
