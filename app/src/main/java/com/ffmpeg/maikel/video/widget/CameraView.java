package com.ffmpeg.maikel.video.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.ui.MainActivity;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private int mLastOrientation;
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
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.e(TAG, "camera display failure :" + e.getMessage());
            cameraPresenter.destroy();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        if (mCamera == null) {
            Log.e(TAG, "<surfaceChanged> camera not found please check!");
            destroyMainView();
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
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
        mPreviewSize = getFixedSize(sizes,getMeasuredWidth(),getMeasuredHeight());
        //设置预览的宽度、高度
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        //设置帧率30帧，这里应该做成动态帧率根据网络状态
        parameters.setPreviewFrameRate(30);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreview = true;
        } catch (IOException e) {
            Log.e(TAG, "<surfaceChanged> camera can not preview!");
            destroyMainView();
        }
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
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        //找到比较合适的尺寸
        for (Camera.Size size:sizes){
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio)>ASPECT_TOLERANCE){
                continue;
            }
            if (Math.abs(size.height - targetHeight)<minDiff){
                fixedSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        //没有找到合适的，则忽略ASPECT_TOLERANCE
        if (fixedSize == null){
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size:sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    fixedSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return fixedSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final  int widthSize = resolveSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        final int heightSize = resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);
        setMeasuredDimension(widthSize,heightSize);
        if (mPreviewSize==null){
            mPreviewSize = getFixedSize(mCamera.getParameters().getSupportedPreviewSizes(),Math.max(widthSize, heightSize), Math.min(widthSize, heightSize));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int curOrientation =
                getContext().getResources().getConfiguration().orientation;
        if (changed &&  mLastOrientation != curOrientation) {
            mLastOrientation = curOrientation;
            final int width = right - left;
            final int height = bottom - top;
            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
                if (curOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    previewWidth = mPreviewSize.height;
                    previewHeight = mPreviewSize.width;
                }
            }
            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

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
