package com.ffmpeg.maikel.video.presenter;

import android.view.SurfaceHolder;

/**
 * Created by maikel on 2018/3/12.
 */

public interface CameraPresenter extends BasePresenter{

    void rotation(int orientation);

    void switchCamera();

    void start();

    void displayFailure();

    void startPreview(SurfaceHolder surfaceHolder);

    void destroy();

    void finishMain();

}
