package com.ffmpeg.maikel.video.widget;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by maikel on 2018/3/12.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = CameraView.class.getSimpleName();
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private int cameraId = BACK_CAMERA;
    private Camera mCamera = null;
    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initCamera(){
        try {
            mCamera = Camera.open(cameraId);
        }catch (Exception e){
            Log.e(TAG,"open camera failure:"+cameraId);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
