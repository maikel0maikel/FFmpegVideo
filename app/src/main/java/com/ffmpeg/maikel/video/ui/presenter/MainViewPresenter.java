package com.ffmpeg.maikel.video.ui.presenter;

import android.hardware.Camera;

/**
 * Created by maikel on 2018/3/12.
 */

public interface MainViewPresenter extends BaseView{

    void cameraOpened(Camera camera);

    void cameraError();

    void displayError();

    void switchCamera();


}
