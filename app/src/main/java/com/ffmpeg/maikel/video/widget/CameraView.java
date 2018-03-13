package com.ffmpeg.maikel.video.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CameraProfile;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ffmpeg.maikel.video.bean.SurpportedSize;
import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.utils.CameraUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by maikel on 2018/3/12.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = CameraView.class.getSimpleName();
    private Camera mCamera = null;
    private SurfaceHolder mHolder;
    private boolean isPreview = false;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private CameraPresenter cameraPresenter;
    private Camera.Size mPreviewSize;
    private Camera.Size size;
    private int mLastOrientation;
    private String fourCC;
    private PixelFormat pixelFormat;
    private boolean stopping;
    BlockingQueue<byte[]> readyFrames = new LinkedBlockingQueue<byte[]>();
    private String savedFormat;
    private int savedWidth;
    private int savedHeight;
    private int savedFrameRate;

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

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initSurface(Camera camera, int w, int h, CameraPresenter cameraPresenter) {
        mVideoWidth = w;
        mVideoHeight = h;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCamera.setPreviewCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.cameraPresenter = cameraPresenter;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        stopPreview();
        if (!start(CameraUtils.pixelFormatToString(CameraUtils.JPEG), mVideoWidth, mVideoHeight, 30)){
            destroyMainView();
            return;
        }
        if (mCamera == null){
            Log.e(TAG, "<surfaceCreated> camera not found please check!");
            destroyMainView();
            return;
        }
        setCameraParams();
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.e(TAG, "camera display failure :" + e.getMessage());
            cameraPresenter.destroy();
            return;
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreview = true;
        } catch (IOException e) {
            Log.e(TAG, "<surfaceChanged> camera can not preview!" + e.getMessage());
            destroyMainView();
        }

    }

    private void setCameraParams(){
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        mPreviewSize = getFixedSize(sizes, mVideoWidth, mVideoHeight);
//        //设置预览的宽度、高度
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        //设置帧率30帧，这里应该做成动态帧率根据网络状态
        parameters.setPreviewFrameRate(30);
        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
       // stopPreview();
        if (mCamera == null) {
            Log.e(TAG, "<surfaceChanged> camera not found please check!");
            destroyMainView();
            return;
        }
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//        Collections.sort(sizes, new Comparator<Camera.Size>() {
//            @Override
//            public int compare(Camera.Size size1, Camera.Size size2) {
//                return size1.width * size1.height - size2.width * size2.height;
//            }
//        });
//        int count = sizes.size();
//        for (int i = 0; i < count; i++) {
//            Camera.Size size = sizes.get(i);
//            if ((size.width >= mVideoWidth && size.height >= mVideoHeight) || i == count - 1) {
//                mVideoWidth = size.width;
//                mVideoHeight = size.height;
//                break;
//            }
//        }
//        mPreviewSize = getFixedSize(sizes, width, height);
//        //设置预览的宽度、高度
//        parameters.setPreviewSize(width, height);
//        //设置帧率30帧，这里应该做成动态帧率根据网络状态
//        parameters.setPreviewFrameRate(30);
//        mCamera.setParameters(parameters);
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//            isPreview = true;
//        } catch (IOException e) {
//            Log.e(TAG, "<surfaceChanged> camera can not preview!" + e.getMessage());
//            destroyMainView();
//        }
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
            this.setVisibility(View.VISIBLE);
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

    public void stop() {
        Log.i(TAG, "stop");
        stopping = true;
        if (mCamera != null) {
            setVisibility(View.GONE);
            cameraPresenter.destroy();
            Log.i(TAG, "stop: Camera stopped");
        }

        Log.i(TAG, "stop: draining frames");
        drainFrames();
        if (mHolder != null) {
            mHolder.removeCallback(this);
            mHolder = null;
        }
    }

    private void drainFrames() {
        if (readyFrames != null)
            readyFrames.clear();
        readyFrames = null;
        mArglist = null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        destroy();
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //receive video frame
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


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        final int widthSize = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//        final int heightSize = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
//        setMeasuredDimension(widthSize, heightSize);
//        if (mPreviewSize == null) {
//            mPreviewSize = getFixedSize(mCamera.getParameters().getSupportedPreviewSizes(), Math.max(widthSize, heightSize), Math.min(widthSize, heightSize));
//        }
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        int curOrientation =
//                getContext().getResources().getConfiguration().orientation;
//        if (changed && mLastOrientation != curOrientation) {
//            mLastOrientation = curOrientation;
//            final int width = right - left;
//            final int height = bottom - top;
//            int previewWidth = width;
//            int previewHeight = height;
//            if (mPreviewSize != null) {
//                previewWidth = mPreviewSize.width;
//                previewHeight = mPreviewSize.height;
//                if (curOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                    previewWidth = mPreviewSize.height;
//                    previewHeight = mPreviewSize.width;
//                }
//            }
//            // Center the child SurfaceView within the parent.
//            if (width * previewHeight > height * previewWidth) {
//                final int scaledChildWidth = previewWidth * height / previewHeight;
//                layout((width - scaledChildWidth) / 2, 0,
//                        (width + scaledChildWidth) / 2, height);
//            } else {
//                final int scaledChildHeight = previewHeight * width / previewWidth;
//                layout(0, (height - scaledChildHeight) / 2,
//                        width, (height + scaledChildHeight) / 2);
//            }
//        }
//    }

    private void stopPreview() {
        if (mCamera != null && isPreview) {
            mCamera.stopPreview();
            isPreview = false;
        }
    }

    private void destroyMainView() {
        if (cameraPresenter != null) {
            cameraPresenter.finishMain();
        }
    }

    private void destroy() {
        stopPreview();
        if (cameraPresenter != null) {
            cameraPresenter.destroy();
        }
    }
}
