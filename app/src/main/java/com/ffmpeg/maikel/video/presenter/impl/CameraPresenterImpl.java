package com.ffmpeg.maikel.video.presenter.impl;

import android.hardware.Camera;
import android.util.Log;

import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.ui.presenter.MainViewPresenter;

/**
 * Created by maikel on 2018/3/12.
 */

public class CameraPresenterImpl implements CameraPresenter {
    private static final String TAG = CameraPresenterImpl.class.getSimpleName();
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private int cameraId = BACK_CAMERA;
    private Camera mCamera = null;

    private MainViewPresenter mView;

    public CameraPresenterImpl(MainViewPresenter view) {
        mView = view;
        mView.setPresenter(this);
    }

    private void initCamera() {
        try {
            mCamera = Camera.open(BACK_CAMERA);
            mView.cameraOpened(mCamera);
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
        switch (rotation) {
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
        mCamera.setDisplayOrientation(result);
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

    }
    

}
