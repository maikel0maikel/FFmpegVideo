package com.ffmpeg.maikel.video.video;

import android.hardware.Camera;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by maikel on 2018/3/13.
 */

public class VideoFrameCallback implements Runnable, Camera.PreviewCallback {

    private BlockingQueue<byte[]> readyFrames = new LinkedBlockingQueue<byte[]>();

    private boolean start;
    private Lock mFrameLock = new ReentrantLock();
    private Condition mFrameCondition = mFrameLock.newCondition();

    public void videoStart(boolean isStart){
        this.start = isStart;
    }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (start) {
            mFrameLock.lock();
            byte[] newFrame = new byte[data.length];
            System.arraycopy(data, 0, newFrame, 0, newFrame.length);
            readyFrames.offer(newFrame);
            mFrameCondition.signal();
            mFrameLock.unlock();
        }
    }

    @Override
    public void run() {
        while (start) {
            mFrameLock.lock();
            if (readyFrames.isEmpty()) {
                try {
                    mFrameCondition.await(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (readyFrames.isEmpty()) {
                continue;
            }
            byte[] videoFrame = readyFrames.poll();

            mFrameLock.unlock();
        }
        if (readyFrames != null)
            readyFrames.clear();
        readyFrames = null;
    }
}
