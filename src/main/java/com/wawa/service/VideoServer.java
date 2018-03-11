package com.wawa.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.wawa.Main;
import com.wawa.capture.VideoStream;
import com.wawa.common.utils.JSONUtil;
import com.wawa.model.ActionTypeEnum;
import com.wawa.model.EventEnum;
import com.wawa.model.EventSetup;
import org.apache.commons.lang.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class VideoServer {
    private final Logger logger = LoggerFactory.getLogger(VideoServer.class);
    public static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    private static JavaType mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Map<String, VideoStream> videoMap = new HashMap<>();
    private Map<String, VideoSocketClient> videoSocketMap = new HashMap<>();
    private Timer timer = new Timer();
    private TimerTask pingTimerTask;

    @Subscribe
    public void listener(EventSetup msg) {
        //写入properties文件内
        try {
            //Properties prop = Main.prop;
            if (EventEnum.STARTUP == msg.getType()) {
                videoStart();
                socketStart();
                //心跳
                if (pingTimerTask == null) {
                    pingTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            logger.debug("==========>: send ping.");
                            for (WebSocketClient client : videoSocketMap.values()) {
                                try {
                                    client.sendPing();
                                } catch (Exception e) {
                                    logger.error("error to ping server." + e);
                                }
                            }
                        }
                    };
                    timer.scheduleAtFixedRate(pingTimerTask, 60000, 60000);
                }
            }
            if (EventEnum.SHUTDOWN == msg.getType()) {
                //尝试重启websocket
                logger.info("========>尝试重新连接socket");
                //服务器断开了连接，需要监听端口重启
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            socketStart();
                        } catch (Exception e) {
                            logger.error("error to start socket." + e);
                        }
                    }
                }, 60000);
            }
        } catch (Exception e) {
            logger.error("" + e);
        }
    }

    private void videoStart() {
        try {
            Properties prop = Main.prop;
            for (int i = 1; i < 3; i ++) {
                String cameraName = "device.camera" + i;
                String camera = prop.getProperty(cameraName);
                String videoKey = "camera" + i;
                if (StringUtils.isNotBlank(camera)) {
                    VideoStream videoStream = videoMap.get(videoKey);
                    if (videoStream == null) {
                        videoStream = new VideoStream(camera);
                        videoMap.put("camera" + i, videoStream);
                        videoStream.start();
                    }
                } else {
                    if (videoMap.containsKey(videoKey)) {
                        VideoStream videoStream = videoMap.remove(videoKey);
                        videoStream.destroy();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("failed to open video," + e);
        }
    }

    private boolean socketStart() {
        Properties prop = Main.prop;
        try {
            for (int i = 1; i < 3; i ++) {
                String cameraName = "device.camera" + i;
                String camera = prop.getProperty(cameraName);
                String socketKey = "camera" + i;
                if (StringUtils.isNotBlank(camera)) {
                    VideoSocketClient videoSocketClient = videoSocketMap.get(socketKey);
                    if (videoSocketClient == null || videoSocketClient.isClosed()) {
                        URI serverUri;
                        try {
                            serverUri = new URI(prop.getProperty("stream.uri") +
                                    "?device_id=" + prop.getProperty("device.id") + "&stream=" + i);
                        } catch (URISyntaxException e) {
                            logger.error("error uri." + prop.getProperty("stream.uri"));
                            return false;
                        }
                        if (videoSocketClient == null) {
                            videoSocketClient = new VideoSocketClient(serverUri);
                            videoSocketClient.setStreamName("camera" + i);
                        }
                        videoSocketClient.connect();
                        videoSocketClient.register(this);
                        videoSocketMap.put("camera" + i, videoSocketClient);
                    }
                } else {
                    if (videoSocketMap.containsKey(socketKey)) {
                        VideoSocketClient videoSocketClient = videoSocketMap.remove(socketKey);
                        videoSocketClient.unregister(this);
                        videoSocketClient.close();
                        videoSocketClient.futureTask.cancel(false);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("failed to open socket," + e);
        }
        return false;
    }

    public class VideoSocketClient extends WebSocketClient {
        private String streamName = "";

        private final AsyncEventBus eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
        private FutureTask futureTask;

        public VideoSocketClient(URI serverUri) {
            super(serverUri);
        }

        public void setStreamName(String streamName) {
            this.streamName = streamName;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            short status = handshakedata.getHttpStatus();
            if (101 != status) {
                logger.error("error to connect to server. status:" + status + ",message:" + handshakedata.getHttpStatusMessage());
                return;
            }
            logger.info("ready to upstream");
            VideoStream videoStream = videoMap.get(streamName);
            if (videoStream != null) {
                futureTask = new FutureTask<>(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        while (!futureTask.isCancelled()) {
                            sendStream();
                        }
                        return null;
                    }
                });
                executorService.execute(futureTask);
            }
        }

        @Override
        public void onMessage(String s) {
            logger.info("received message." + s);
            //轮询时间点修改
            try {
                Map<String, Object> message = JSONUtil.jsonToBean(s, mapType);
                if (message.isEmpty() || !message.containsKey("action")) {
                    logger.info("illigal message: " + s);
                    return;
                }
                String action = String.valueOf(message.get("action"));
                if (ActionTypeEnum.推流指令.getId().equals(action)) {
                    Boolean data = Boolean.valueOf(String.valueOf(message.get("data")));
                    if (data) {//开启推流
                        if (futureTask == null || futureTask.isCancelled() || futureTask.isDone()) {
                            futureTask = new FutureTask<>(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    while (!futureTask.isCancelled()) {
                                        sendStream();
                                    }
                                    return null;
                                }
                            });
                            executorService.execute(futureTask);
                        }
                    } else {//关闭推流
                        if (futureTask != null && !futureTask.isCancelled() && !futureTask.isDone()) {
                            futureTask.cancel(false);
                        }
                    }
                    return;
                }
                //FIXME reload prop
                if (ActionTypeEnum.重启指令.getId().equals(action)) {
                    return;
                }
            } catch (Exception e) {
                logger.info("illigal message:" + s + ", exception:" + e);
            }
            logger.error("error accur while received message:" + s);
        }



        @Override
        public void onClose(int i, String s, boolean b) {
            //发送通知 断线重连
            logger.info("close.i:" + i + ", s:" + s + ", b:" + b);
            if (i != 1000 && i != 1001) {
                for (Map.Entry<String, VideoSocketClient> entry : videoSocketMap.entrySet()) {
                    if (this.equals(entry.getValue())) {
                        videoSocketMap.remove(entry.getKey());
                    }
                }
                EventSetup eventSetup = new EventSetup();
                eventSetup.setType(EventEnum.SHUTDOWN);
                eventBus.post(eventSetup);
                if (!futureTask.isCancelled()) {
                    futureTask.cancel(true);
                }
            }
        }

        @Override
        public void onError(Exception e) {
            // 连接出现问题
            logger.error("error." + e);
            /*EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);*/

        }

        public void sendStream() {
                try {
                    VideoStream videoStream = videoMap.get(streamName);
                    if (videoStream != null) {
                        byte[] tmp = videoStream.readStream();
                        if (tmp != null)
                        this.send(tmp);
                    }
                } catch (Exception e) {
                    logger.error("" + e);
                }
        }

        public void register(Object event) {
            eventBus.register(event);
        }

        public void unregister(Object event) {
            eventBus.unregister(event);
        }
    }
}
