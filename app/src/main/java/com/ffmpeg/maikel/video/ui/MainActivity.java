package com.ffmpeg.maikel.video.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import com.ffmpeg.maikel.video.R;
import com.ffmpeg.maikel.video.presenter.BasePresenter;
import com.ffmpeg.maikel.video.presenter.CameraPresenter;
import com.ffmpeg.maikel.video.presenter.impl.CameraPresenterImpl;
import com.ffmpeg.maikel.video.ui.presenter.MainViewPresenter;
import com.ffmpeg.maikel.video.widget.CameraView;

/**
 * Created by maikel on 2018/3/12.
 */

public class MainActivity extends Activity implements MainViewPresenter {
    private static final int PERMISSION_REQUEST_CODE = 10;
    private CameraPresenter cameraPresenter;
    private CameraView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            initView();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.video_surface_view);
        new CameraPresenterImpl(this).start();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switchCamera:
                view.setEnabled(false);
                cameraPresenter.switchCamera();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initView();
            } else {
                finish();
            }
        }
    }

    @Override
    public void cameraOpened(Camera camera) {
        //set Camera icon enable
    }

    @Override
    public void cameraError() {
        finish();
    }

    @Override
    public void displayError() {

    }

    @Override
    public void cameraSate(boolean flag) {
        findViewById(R.id.switchCamera).setEnabled(flag);
    }

    @Override
    public CameraView getCameraView() {
        return mCameraView;
    }

    @Override
    public Context getViewContext() {
        return this;
    }

    @Override
    public void destroy() {
        finish();
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
        cameraPresenter = (CameraPresenter) presenter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoRotation();
    }

    private void setVideoRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (cameraPresenter != null) {
            cameraPresenter.rotation(rotation);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPresenter != null) {
            cameraPresenter.destroy();
        }
    }
}
