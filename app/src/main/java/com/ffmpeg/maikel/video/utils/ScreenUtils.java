package com.ffmpeg.maikel.video.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by maikel on 2018/3/13.
 */

public class ScreenUtils {
    private ScreenUtils(){}

    public static DisplayMetrics getScreenDensity(Context context){
        Resources resources = context.getResources();
       return resources.getDisplayMetrics();
    }
}
