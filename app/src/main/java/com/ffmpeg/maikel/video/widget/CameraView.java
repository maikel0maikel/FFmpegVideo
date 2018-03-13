package com.ffmpeg.maikel.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ffmpeg.maikel.video.presenter.CameraPresenter;


/**
 * Created by maikel on 2018/3/12.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraView.class.getSimpleName();
    private SurfaceHolder mHolder;
    private CameraPresenter cameraPresenter;



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

    public void initSurface(CameraPresenter cameraPresenter) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.cameraPresenter = cameraPresenter;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraPresenter.startPreview(mHolder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // stopPreview();
//        if (mCamera == null) {
//            Log.e(TAG, "<surfaceChanged> camera not found please check!");
//            destroyMainView();
//            return;
//        }
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//        Collections.sort(sizes, new Comparator<Camera.Size>() {
//            @Override
//            public int compare(Camera.Size size1, Camera.Size size2) {
//                return size1.width * size1.height - size2.width * size2.height;
//            }
//        });
//        int count = sizes.size();
//        for (int i = 0; i < count; i++) {
//            Camera.Size size = sizes.get(i);
//            if ((size.width >= mVideoWidth && size.height >= mVideoHeight) || i == count - 1) {
//                mVideoWidth = size.width;
//                mVideoHeight = size.height;
//                break;
//            }
//        }
//        mPreviewSize = getFixedSize(sizes, width, height);
//        //设置预览的宽度、高度
//        parameters.setPreviewSize(width, height);
//        //设置帧率30帧，这里应该做成动态帧率根据网络状态
//        parameters.setPreviewFrameRate(30);
//        mCamera.setParameters(parameters);
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//            isPreview = true;
//        } catch (IOException e) {
//            Log.e(TAG, "<surfaceChanged> camera can not preview!" + e.getMessage());
//            destroyMainView();
//        }
    }





    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        destroy();
    }


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        final int widthSize = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//        final int heightSize = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
//        setMeasuredDimension(widthSize, heightSize);
//        if (mPreviewSize == null) {
//            mPreviewSize = getFixedSize(mCamera.getParameters().getSupportedPreviewSizes(), Math.max(widthSize, heightSize), Math.min(widthSize, heightSize));
//        }
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        int curOrientation =
//                getContext().getResources().getConfiguration().orientation;
//        if (changed && mLastOrientation != curOrientation) {
//            mLastOrientation = curOrientation;
//            final int width = right - left;
//            final int height = bottom - top;
//            int previewWidth = width;
//            int previewHeight = height;
//            if (mPreviewSize != null) {
//                previewWidth = mPreviewSize.width;
//                previewHeight = mPreviewSize.height;
//                if (curOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                    previewWidth = mPreviewSize.height;
//                    previewHeight = mPreviewSize.width;
//                }
//            }
//            // Center the child SurfaceView within the parent.
//            if (width * previewHeight > height * previewWidth) {
//                final int scaledChildWidth = previewWidth * height / previewHeight;
//                layout((width - scaledChildWidth) / 2, 0,
//                        (width + scaledChildWidth) / 2, height);
//            } else {
//                final int scaledChildHeight = previewHeight * width / previewWidth;
//                layout(0, (height - scaledChildHeight) / 2,
//                        width, (height + scaledChildHeight) / 2);
//            }
//        }
//    }

//    private void stopPreview() {
//        if (mCamera != null && isPreview) {
//            mCamera.stopPreview();
//            isPreview = false;
//        }
//    }

//    private void destroyMainView() {
//        if (cameraPresenter != null) {
//            cameraPresenter.finishMain();
//        }
//    }

    private void destroy() {
        if (cameraPresenter != null) {
            cameraPresenter.destroy();
        }
    }
}
