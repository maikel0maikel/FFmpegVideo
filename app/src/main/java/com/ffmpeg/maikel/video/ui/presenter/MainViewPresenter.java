package com.ffmpeg.maikel.video.ui.presenter;

import android.content.Context;
import android.hardware.Camera;

import com.ffmpeg.maikel.video.widget.CameraView;

/**
 * Created by maikel on 2018/3/12.
 */

public interface MainViewPresenter extends BaseView{

    void cameraOpened(Camera camera);

    void cameraError();

    void displayError();

    void cameraSate(boolean flag);



    CameraView getCameraView();

    Context getViewContext();

    void destroy();
}

