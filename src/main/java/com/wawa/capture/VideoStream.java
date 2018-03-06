package com.wawa.capture;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wawa.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class VideoStream {
    Logger logger = LoggerFactory.getLogger(VideoStream.class);

    private final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(10);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private String exec;
    private Process process;
    private FutureTask streamFutureTask;
    private FutureTask errorFutureTask;
    private EventBus eventBus = new EventBus();


    public VideoStream(String cameraName) {
        this.exec = "ffmpeg -f dshow -i video=\"" + cameraName + "\" " +
                "-r 10 " +
                "-framerate 10 " +
                "-video_size 320x240 " +
                "-pix_fmt yuv420p " +
                "-c:v libx264 " +
                "-b:v 200k " +
                "-bufsize 300k " +
                "-vprofile baseline " +
                "-tune zerolatency " +
                "-f rawvideo -";
    }

    public void start() {
        if (exec == null) {
            logger.error("stream start error, exec null");
            return;
        }
        if (process != null && process.isAlive()) {
            destroy();
        }
        try {
            process = Runtime.getRuntime().exec(exec);
            startStream(process.getInputStream());
            errorStream(process.getErrorStream());
        } catch (Exception e) {
            logger.error("stream start error." + e);
            return;
        }
        logger.info("stream start success.");
    }

    private void startStream(final InputStream inputStream) {
        streamFutureTask = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //读取inputStream的内容
                byte[] tmp = new byte[8912];
                int len = inputStream.read(tmp, 0, tmp.length);
                while (len > -1) {
                    boolean offer = queue.add(Arrays.copyOf(tmp, len));
                    logger.debug("读取数量：" + len + "offerred stream, result:" + offer);
                    len = inputStream.read(tmp, 0, tmp.length);
                }
                return null;
            }
        });
        executorService.execute(streamFutureTask);
    }

    private void errorStream(final InputStream errorStream) {
        errorFutureTask = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //读取inputStream的内容
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                while ((line = reader.readLine()) != null) {
                    if (queue.size() > 10) {
                        queue.clear();
                        logger.info("====================>>:queue too full to clear.");
                    }
                    logger.info(line);
                    //todo 如果有错误信息 需要重启
                    //eventBus.post("error");
                }
                return null;
            }
        });
        executorService.execute(errorFutureTask);
    }

    //todo 这个地方需要好好做一下
    public byte[] readStream() {
        byte[] bytes = new byte[0];
        try {
            bytes = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bytes;
    }


    public void destroy() {
        process.destroy();
        process = null;
        streamFutureTask.cancel(true);
        streamFutureTask = null;
        errorFutureTask.cancel(true);
        errorFutureTask = null;
        queue.clear();
    }

    @Subscribe
    public void onMessage(String msg) {
        if (msg.equals("error")) {
            destroy();
            start();
        }
    }

    public static void main(String[] args) {
        /*try {
            File file = new File("D:/output.mp4");
            String pullStream = "ffmpeg -f dshow -i video=\"e2eSoft VCam\" -r 10 -framerate 10 -video_size 320x240 -pix_fmt yuv420p -c:v libx264 -b:v 200k -bufsize 300k -vprofile baseline -tune zerolatency -f rawvideo -";
            Process process = Runtime.getRuntime().exec(pullStream);
            while (process.isAlive()) {
                System.out.println("alive");
                //System.out.println(process.waitFor());

                *//*StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while((line = reader.readLine()) != null) {

                }
                System.out.println(sb);*//*
                writeByteToHex(process.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        VideoStream videoStream = new VideoStream("e2eSoft VCam");
        videoStream.start();

        //来了个消费者
        int i = 0;
        while (i++ < 100) {
            writeByteToHex(videoStream.readStream());
        }
        System.out.println("game over");
        videoStream.destroy();
        /*System.out.println("start again");
        videoStream.start();*/
        System.out.println("destroy success.");

        videoStream.start();
        i = 0;
        while (i++ < 100) {
            writeByteToHex(videoStream.readStream());
        }
        System.out.println("really game over.-_-");
        videoStream.destroy();
        System.exit(0);
    }


    public static void writeByteToHex(byte[] bytes) {
        System.out.println(StringUtils.bytes2HexString(bytes, bytes.length));
    }

}
