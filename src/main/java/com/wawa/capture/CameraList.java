package com.wawa.capture;

import com.wawa.common.utils.CaptureUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class CameraList {

    private final ExecutorService exec = Executors.newFixedThreadPool(1);
    private int flag = 0;//类静态变量，用于控制按下按钮后 停止摄像头的读取
    private FutureTask<String> future;
    private VideoCapture camera;
    private JLabel label = new JLabel("");
    private int deviceIndex;

    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    public CameraList() {
    }

    public void playVideoOnLabel() {
        Callable<String> callable = () -> {
            String msg="fps:";
            DecimalFormat df = new DecimalFormat(".##");//数字格式化
            //我们的操作

            if(!camera.isOpened()){//isOpened函数用来判断摄像头调用是否成功
                System.out.println("Camera Error");//如果摄像头调用失败，输出错误信息
            } else {
                Mat frame = new Mat();//创建一个输出帧
                double start = System.currentTimeMillis();
                double end;
                //read方法读取摄像头的当前帧
                while (camera.isOpened() && camera.read(frame)) {

                    float scale = resizeRate(frame.width(), frame.height(), label.getWidth(), label.getHeight());
                    Imgproc.resize(frame, frame, new Size(frame.width()*scale,frame.height()*scale));

                    BufferedImage bufferedImage = CaptureUtils.matToBufferedImage(frame);
                    end = System.currentTimeMillis();
                    String text = msg + df.format((1000.0/(end-start)));
                    int srcImgWidth = frame.width();
                    int srcImgHeight = frame.height();
                    BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
                    if (bufferedImage != null) {
                        Graphics2D g = bufImg.createGraphics();
                        Font font = new Font("微软雅黑", Font.PLAIN, 35);
                        g.drawImage(bufferedImage, 0, 0, srcImgWidth, srcImgHeight, null);
                        g.setColor(new Color(29, 55, 141)); //根据图片的背景设置水印颜色
                        g.setFont(font);              //设置字体

                        //设置水印的坐标
                        int x = 40;
                        int y = 40;
                        g.drawString(text, x, y);  //画出水印
                        g.dispose();
                    }

                    if (bufferedImage != null) {
                        label.setIcon(new ImageIcon(bufImg));//转换图像格式并输出
                    }
                    frame.release();
                    start = end;
                    try {
                        Thread.sleep(30);//线程暂停100ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return "closing camera.";
            }
            return "open camera failed.";
        };
        future = new FutureTask<>(callable);
        exec.submit(future);
        if (future.isDone()) {
            try {
                System.out.println(future.get()); //预警
            } catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private static float resizeRate(int originWidth, int originHeight, int width, int height) {
        if (width <= height) {
            return txfloat(originWidth, width);
        }
        if (width > height) {
            return txfloat(originHeight, width);
        }
        return 1f;
    }

    private static float txfloat(int a, int b) {
        return (float)b/a;
    }

    public static Dimension getScreenSize() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = screensize.width;
        int h = screensize.height;

        return new Dimension(w, h);
    }

    public static void main(String[] args) {
        CameraList cameraList = new CameraList();
        cameraList.onOpen(1);
        SwingUtilities.invokeLater(()-> {
            JFrame frame = new JFrame("娃娃机");
            JPanel jpanel = new JPanel();
            //设置panel的layout以及sieze
            jpanel.setLayout(null);
            Dimension dimension = getScreenSize();
            int frame_width = (int) (dimension.getWidth() * 0.8);
            int frame_height = (int) (dimension.getHeight() * 0.8);
            jpanel.setPreferredSize(new Dimension(frame_width, frame_height));

            //日志列表
            JFrame.setDefaultLookAndFeelDecorated(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(jpanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(false);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    cameraList.onClose();
                    System.exit(0);
                }
            });
            //获取设备描述信息
            try {
                cameraList.getLabel().setBounds(20 + frame_width/3 + 50, 40, 400, 600);
                jpanel.add(cameraList.getLabel());
                jpanel.updateUI();
                jpanel.repaint();
                cameraList.playVideoOnLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static int getWatermarkLength(String waterMarkContent, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(waterMarkContent.toCharArray(), 0, waterMarkContent.length());
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(){
        camera = new VideoCapture(deviceIndex);//创建Opencv中的视频捕捉对象
        if(!camera.isOpened()){//isOpened函数用来判断摄像头调用是否成功
            System.out.println("Camera Error.");//如果摄像头调用失败，输出错误信息
        }
        this.label.setEnabled(true);
        this.flag = 0;
    }

    public void onOpen(int deviceIndex) {
        this.deviceIndex = deviceIndex;
        initialize();
    }

    public void onClose() {
        if (this.camera != null) {
            this.camera.release();
            try {
                System.out.println(future.get()); //todo 关闭结果
            } catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (this.label != null) {
            this.label.setEnabled(false);
        }
        this.flag = 1;
    }

    public VideoCapture getCamera() {
        return camera;
    }

    public void setCamera(VideoCapture camera) {
        this.camera = camera;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }
}
