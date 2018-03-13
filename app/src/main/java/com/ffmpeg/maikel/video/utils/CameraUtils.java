package com.ffmpeg.maikel.video.utils;

import android.hardware.Camera;
import android.util.Log;

import com.ffmpeg.maikel.video.bean.SurpportedSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maikel on 2018/3/13.
 */

public class CameraUtils {
    private static final String TAG = CameraUtils.class.getSimpleName();
    public static final int JPEG = 256;
    public static final int NV16 = 16;
    public static final int NV21 = 17;
    public static final int YUY2 = 20;
    public static final int YV12 = 842094169;

    private CameraUtils() {
    }

    public static SurpportedSize[] getSurpportedSize(Camera camera) {
        SurpportedSize[] surpportedSize = null;
        Camera.Parameters parameters = camera.getParameters();
        List<Integer> formatList = parameters.getSupportedPreviewFormats();//
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        List<int[]> supportedPreviewFps = parameters.getSupportedPreviewFpsRange();//帧率
        List<int[]> supportedPreviewMaxFps = new ArrayList<int[]>();

        for (int[] sfps : supportedPreviewFps) {
            boolean alreadyAdded = false;
            for (int[] fps : supportedPreviewMaxFps) {
                if (fps[1] == sfps[1]) {
                    alreadyAdded = true;
                    if (fps[0] < sfps[0])
                        fps[0] = sfps[0];
                    break;
                }
            }
            if (!alreadyAdded) {
                supportedPreviewMaxFps.add(sfps);
            }
        }
        if (formatList == null || sizeList == null || supportedPreviewFps == null) {
            Log.e(TAG, "Failed to get capabilities list");
        }
        int numCapabilities = 0;
        for (Integer previewFormat : formatList) {
            for (Camera.Size previewSize : sizeList) {
                for (int[] sfps : supportedPreviewMaxFps) {
                    Log.d(TAG, "Found Configuration format : " + Integer.toString(previewFormat) + " size: " + Integer.toString(previewSize.width) + "x" + Integer.toString(previewSize.height) + " min-sample-rate: " + Integer.toString(sfps[0] / 1000) + " max-sampling-rate: " + Integer.toString(sfps[1] / 1000));
                    numCapabilities++;
                }
            }
        }

        surpportedSize = new SurpportedSize[numCapabilities];
        int i = 0;
        for (Integer previewFormat : formatList) {
            for (Camera.Size previewSize : sizeList) {
                for (int[] sfps : supportedPreviewMaxFps) {
                    String format = pixelFormatToString(previewFormat);
                        /* Since the frame rate is scaled up by 1000, scale down by the same. */
                    surpportedSize[i] = new SurpportedSize(format, previewSize.width, previewSize.height, sfps[0] / 1000, sfps[1] / 1000);
                    i++;
                }
            }
        }
        return surpportedSize;
    }

    public static String pixelFormatToString(int formatInt) {
        String format = "";

        switch (formatInt) {
            case JPEG:
                format = "JPEG";
                break;
            case NV16:
                format = "NV16";
                break;
            case NV21:
                format = "NV21";
                break;
            case YUY2:
                format = "NV21"; /* Android's YUY2 is screwed up */
                break;
            case YV12:
                format = "YV12";
                break;
        }
        return format;
    }
    public static int pixelFormatFromString(String format) {
        int pixelFormatInt = 0;
        if (format.equals("JPEG")) {
            pixelFormatInt = JPEG;
        } else if (format.equals("NV16")) {
            pixelFormatInt = NV16;
        }  else if (format.equals("NV21")) {
            pixelFormatInt = NV21;
        }  else if (format.equals("YUY2")) {
            pixelFormatInt = YUY2;
        }  else if (format.equals("YV12")) {
            pixelFormatInt = YV12;
        }
        return pixelFormatInt;
    }

    public static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
}
