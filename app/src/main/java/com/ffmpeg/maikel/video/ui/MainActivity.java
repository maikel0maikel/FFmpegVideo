package com.ffmpeg.maikel.video.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
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
        Window window = getWindow();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenW = displayMetrics.widthPixels;
        int screenH = displayMetrics.heightPixels;
        mCameraView.initSurface(camera,screenW,screenW,cameraPresenter);
    }

    @Override
    public void cameraError() {
        finish();
    }

    @Override
    public void displayError() {

    }

    @Override
    public void switchCamera() {
        cameraPresenter.switchCamera();
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
        cameraPresenter = (CameraPresenter) presenter;
    }
}
