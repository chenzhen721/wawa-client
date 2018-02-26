package com.wawa.common.utils;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

public class CaptureUtils {

    public static String[] cameraList() {
        try {
            String[] devices = VideoInputFrameGrabber.getDeviceDescriptions();
            if (devices .length > 0) {
                return devices;
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @Param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);
        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    data[i] = (byte)(data[i]^data[i+2]);
                    data[i+2] = (byte)(data[i]^data[i+2]);
                    data[i] = (byte)(data[i]^data[i+2]);
                }
                break;
            default:
                return null;
        }
        BufferedImage image2 = new BufferedImage(cols, rows, type);
        image2.getRaster().setDataElements(0, 0, cols, rows, data);
        return image2;
    }


}
