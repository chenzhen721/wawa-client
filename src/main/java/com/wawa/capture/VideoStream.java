package com.wawa.capture;

import com.google.common.eventbus.Subscribe;
import com.wawa.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VideoStream {
    private final Logger logger = LoggerFactory.getLogger(VideoStream.class);

    private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(10);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private String exec;
    private Process process;
    private FutureTask streamFutureTask;
    private FutureTask errorFutureTask;


    public VideoStream(String cameraName) {
        this.exec = "ffmpeg -f dshow -i video=\"" + cameraName + "\" " +
                "-r 18 " +
                "-framerate 15 " +
                "-video_size 640x480 " +
                "-pix_fmt yuv420p " +
                "-c:v libx264 " +
                "-b:v 1000k " +
                "-bufsize 1k " +
                "-rtbufsize 1k " +
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
                byte[] tmp = new byte[89120];
                int len = inputStream.read(tmp, 0, tmp.length);
                while (len > -1) {
                    if (queue.size() >= 5) {
                        //logger.info("清空的队列数量：" + queue.size());
                        queue.clear();
                    }
                    boolean offer = queue.add(Arrays.copyOf(tmp, len));
                    //logger.debug("读取数量：" + len + " offerred stream, result:" + offer);
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
                    logger.debug(line);
                    //eventBus.post("error");
                }
                return null;
            }
        });
        executorService.execute(errorFutureTask);
    }

    public byte[] readStream() {
        byte[] bytes = null;
        try {
            bytes = queue.poll(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("" + e);
        }
        return bytes;
    }


    public void destroy() {
        try {
            process.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        VideoStream videoStream = new VideoStream("ICT Camera");
        videoStream.start();

        //来了个消费者
        int i = 0;
        while (i++ < 100000) {
            writeByteToHex(videoStream.readStream());
        }
        System.out.println("game over");
        videoStream.destroy();
        System.out.println("destroy success.");
        System.exit(0);
    }


    public static void writeByteToHex(byte[] bytes) {
        if (bytes != null) {
            System.out.println(StringUtils.bytes2HexString(bytes, bytes.length));
        }
    }

}
