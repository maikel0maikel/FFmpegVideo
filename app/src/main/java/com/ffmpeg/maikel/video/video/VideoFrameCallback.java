package com.ffmpeg.maikel.video.video;

import android.hardware.Camera;
import android.util.Log;

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

    private static final String TAG = VideoFrameCallback.class.getSimpleName();
    private static VideoFrameCallback videoFrameCallback = new VideoFrameCallback();
    BlockingQueue<byte[]> readyFrames = new LinkedBlockingQueue<byte[]>();
    private boolean start = true;
    private Lock mFrameLock = new ReentrantLock();
    private Condition mFrameCondition = mFrameLock.newCondition();

    public static VideoFrameCallback getInstance() {
        return videoFrameCallback;
    }

    public void videoStart(boolean isStart) {
        if (isStart) {
            Log.i(TAG,"thread is start");
            return;
        }
        mFrameLock.lock();
        this.start = isStart;
        mFrameCondition.signalAll();
        mFrameLock.unlock();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null || data.length == 0) {
            Log.e(TAG, "receive a null frame");
            return;
        }
        if (start) {
            mFrameLock.lock();
            if (readyFrames == null) {
                try {
                    mFrameCondition.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                readyFrames.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mFrameCondition.signalAll();
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
                Log.i(TAG,"readyFrames is empty");
                continue;
            }
            byte[] videoFrame = readyFrames.poll();
            Log.i(TAG, "remove frame");
            mFrameLock.unlock();
        }
        if (readyFrames != null)
            readyFrames.clear();
        readyFrames = null;
        Log.i(TAG,"end videoframecallback thread ");
    }
}
