package com.wawa.service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wawa.Main;
import com.wawa.capture.VideoStream;
import com.wawa.model.EventEnum;
import com.wawa.model.EventSetup;
import org.apache.commons.lang.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class VideoServer {
    private final Logger logger = LoggerFactory.getLogger(VideoServer.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Map<String, VideoStream> videoMap = new HashMap<>();
    private Map<String, VideoSocketClient> videoSocketMap = new HashMap<>();

    @Subscribe
    public void listener(EventSetup msg) {
        //写入properties文件内
        try {
            Properties prop = Main.prop;
            if (EventEnum.STARTUP == msg.getType() || EventEnum.SHUTDOWN == msg.getType()) {
                for (int i = 1; i < 3; i ++) {
                    String cameraName = "device.camera" + i;
                    String camera = prop.getProperty(cameraName);
                    if (StringUtils.isNotBlank(camera)) {
                        VideoStream videoStream = videoMap.get(camera);
                        if (videoStream == null) {
                            videoStream = new VideoStream(camera);
                            videoMap.put("camera" + i, new VideoStream(camera));
                        }
                        videoStream.start();
                        VideoSocketClient videoSocketClient = videoSocketMap.get("camera" + i);
                        if (videoSocketClient == null || !videoSocketClient.isConnecting()) {
                            URI serverUri;
                            try {
                                serverUri = new URI(prop.getProperty("stream.uri") +
                                        "?device_id=" + prop.getProperty("device.id") + "&camera=" + camera);
                            } catch (URISyntaxException e) {
                                logger.error("error uri." + prop.getProperty("stream.uri"));
                                return;
                            }
                            if (videoSocketClient == null) {
                                videoSocketClient = new VideoSocketClient(serverUri);
                                videoSocketClient.setStreamName("camera" + i);
                            }
                            videoSocketClient.connect();
                            videoSocketClient.register(this);
                            videoSocketMap.put("camera" + i, videoSocketClient);
                        }
                        //todo 开启心跳
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public class VideoSocketClient extends WebSocketClient {
        private String streamName = "";

        private final EventBus eventBus = new EventBus();
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
                        sendStream();
                        return null;
                    }
                });
                executorService.execute(futureTask);
            }
        }

        @Override
        public void onMessage(String message) {
            if (message != null) {
                if (futureTask == null || futureTask.isCancelled() || futureTask.isDone()) {
                    futureTask = new FutureTask<>(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            sendStream();
                            return null;
                        }
                    });
                    executorService.execute(futureTask);
                }
            }
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            //todo 发送通知 断线重连
            logger.info("close.i:" + i + ", s:" + s + ", b:" + b);
            EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);
            if (!futureTask.isCancelled()) {
                futureTask.cancel(true);
            }
        }

        @Override
        public void onError(Exception e) {
            //todo 连接出现问题，需要开启重连模式
            logger.error("error.");
            EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);

        }

        public void sendStream() {
            VideoStream videoStream = videoMap.get(streamName);
            if (videoStream != null) {
                byte[] tmp;
                while ((tmp = videoStream.readStream()) != null) {
                    this.send(tmp);
                }
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
