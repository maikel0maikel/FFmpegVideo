package com.ffmpeg.maikel.video.presenter.impl;

import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.ui.presenter.MainViewPresenter;
import com.ffmpeg.maikel.video.utils.ScreenUtils;


/**
 * Created by maikel on 2018/3/12.
 */

public class CameraPresenterImpl implements CameraPresenter {
    private static final String TAG = CameraPresenterImpl.class.getSimpleName();
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private int mCameraId = FRONT_CAMERA;
    private Camera mCamera = null;

    private MainViewPresenter mView;


    public CameraPresenterImpl(MainViewPresenter view) {
        mView = view;
        mView.setPresenter(this);
    }

    private void initCamera() {
        try {
            mCamera = Camera.open(mCameraId);
            mCamera.setDisplayOrientation(90);
            mView.cameraOpened(mCamera);
            if (mView.getCameraView() != null) {
                int w = ScreenUtils.getScreenDensity(mView.getViewContext()).widthPixels;
                int h = ScreenUtils.getScreenDensity(mView.getViewContext()).heightPixels;
                mView.getCameraView().initSurface(mCamera, w, h, this);
            }
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

    }

    @Override
    public void start() {
        initCamera();
    }

    @Override
    public void displayFailure() {
        mView.displayError();
    }

    @Override
    public void destroy() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void finishMain() {
        mView.destroy();
    }


}
