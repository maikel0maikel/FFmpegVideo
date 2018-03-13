package com.ffmpeg.maikel.video.bean;

/**
 * Created by zWX396902 on 2018/3/13.
 */

public class SurpportedSize {
    public String format;
    public int width;
    public int height;
    public int samplingRateMin;
    public int samplingRate;

    public SurpportedSize(){
        this.format = "";
        this.width = 0;
        this.height = 0;
        this.samplingRateMin = 0;
        this.samplingRate = 0;
    }
    public SurpportedSize(String format, int width, int height, int samplingRateMin, int samplingRateMax) {
        this.format = format;
        this.width = width;
        this.height = height;
        this.samplingRateMin = samplingRateMin;
        this.samplingRate = samplingRateMax;
    }
}
