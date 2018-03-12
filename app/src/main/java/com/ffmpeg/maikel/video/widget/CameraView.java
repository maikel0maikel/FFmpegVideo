package com.ffmpeg.maikel.video.widget;

import android.content.Context;
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
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private int cameraId = BACK_CAMERA;
    private Camera mCamera = null;
    private SurfaceHolder mHolder;
    private boolean isPreview = false;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private CameraPresenter cameraPresenter;

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
            if (mCamera != null) {
                mCamera.release();
            }
            cameraPresenter.destroy();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        if (mCamera == null) {
            Log.e(TAG, "<surfaceChanged> camera not found please check!");
            destroyView();
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size1, Camera.Size size2) {
                return size1.width * size1.height - size2.width * size2.height;
            }
        });
        int count = sizes.size();
        for (int i = 0; i < count; i++) {
            Camera.Size size = sizes.get(i);
            if ((size.width >= mVideoWidth && size.height >= mVideoHeight) || i == count - 1) {
                mVideoWidth = size.width;
                mVideoHeight = size.height;
                break;
            }
        }
        //设置预览的宽度、高度
        parameters.setPreviewSize(mVideoWidth, mVideoHeight);
        //设置帧率30帧，这里应该做成动态帧率根据网络状态
        parameters.setPreviewFrameRate(30);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "<surfaceChanged> camera can not preview!");
            destroyView();
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

    private void stopPreview() {
        if (mCamera != null && isPreview) {
            mCamera.stopPreview();
        }
    }

    private void destroyView() {
        if (cameraPresenter != null) {
            cameraPresenter.destroy();
        }
    }

    public void destroy() {
        stopPreview();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }
    }
}
