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
