package com.ffmpeg.maikel.video.ui.presenter;

import com.ffmpeg.maikel.video.presenter.BasePresenter;

/**
 * Created by maikel on 2018/3/12.
 */

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);
}
