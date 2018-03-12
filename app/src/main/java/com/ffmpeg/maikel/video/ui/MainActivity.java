package com.ffmpeg.maikel.video.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;

import com.ffmpeg.maikel.video.R;

/**
 * Created by maikel on 2018/3/12.
 */

public class MainActivity extends Activity{
    private static final int PERMISSION_REQUEST_CODE = 10;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }else{
            initView();
        }
    }

    private void initView(){
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                initView();
            }else {
                finish();
            }
        }
    }
}
