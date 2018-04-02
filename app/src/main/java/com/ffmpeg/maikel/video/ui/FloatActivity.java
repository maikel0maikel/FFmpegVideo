package com.ffmpeg.maikel.video.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by maikel on 2018/4/2.
 */

public class FloatActivity extends Activity {
    private float touchStartX,touchStartY;
    WindowManager.LayoutParams wlp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);
        wlp = getWindow().getAttributes();
        wlp.dimAmount = 0;
        wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        wlp.width = 108*5;
        wlp.height = 192*5;
        getWindow().setAttributes(wlp);
        View view = getWindow().getDecorView();
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (!initViewPlace){
                            initViewPlace = true;
                            touchStartX =  event.getRawX();
                            touchStartY =   event.getRawY();
                            x = event.getRawX();
                            y = event.getRawY();
                        }else {
                            touchStartX += (event.getRawX()-x);
                            touchStartY += (event.getRawY()-y);
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        x = event.getRawX();
                        y = event.getRawY();
                        wlp.x += (int) (x - touchStartX);
                        wlp.y +=(int) (y-touchStartY);
                        //getWindow().setAttributes(wlp);
                        changeWindowSize(wlp);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }



    private void changeWindowSize(WindowManager.LayoutParams lp){
        View view = getWindow().getDecorView();
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        if (!initViewPlace){
//                            initViewPlace = true;
//                            touchStartX =  event.getRawX();
//                            touchStartY =   event.getRawY();
//                            x = event.getRawX();
//                            y = event.getRawY();
//                        }else {
//                            touchStartX += (event.getRawX()-x);
//                            touchStartY += (event.getRawY()-y);
//                        }
//
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        x = event.getRawX();
//                        y = event.getRawY();
//                        wlp.x += (int) (x - touchStartX);
//                        wlp.y +=(int) (y-touchStartY);
//                        //getWindow().setAttributes(wlp);
//                        changeWindowSize(wlp);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        break;
//                }
//                return true;
//            }
//        });
        getWindowManager().updateViewLayout(view,lp);
       // getWindow().setAttributes(wlp);
    }
    private boolean initViewPlace = false;
    private float x,y;
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                if (!initViewPlace){
//                    initViewPlace = true;
//                    touchStartX =  event.getRawX();
//                    touchStartY =   event.getRawY();
//                    x = event.getRawX();
//                    y = event.getRawY();
//                }else {
//                    touchStartX += (event.getRawX()-x);
//                    touchStartY += (event.getRawY()-y);
//                }
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//                x = event.getRawX();
//                y = event.getRawY();
//                wlp.x += (int) (x - touchStartX);
//                wlp.y +=(int) (y-touchStartY);
//                //getWindow().setAttributes(wlp);
//                changeWindowSize(wlp);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        return true;
//    }
}
