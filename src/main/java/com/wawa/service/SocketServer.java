package com.wawa.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wawa.Main;
import com.wawa.common.component.Result;
import com.wawa.common.utils.JSONUtil;
import com.wawa.common.utils.PropertyUtils;
import com.wawa.model.ActionTypeEnum;
import com.wawa.model.C1Config;
import com.wawa.model.C2Config;
import com.wawa.model.ComResponse;
import com.wawa.model.EventEnum;
import com.wawa.model.EventSetup;
import com.wawa.model.Response;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class SocketServer {
    private final static Logger logger = LoggerFactory.getLogger(SocketServer.class);
    public static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    private static JavaType mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);
    private MachineInvoker machineInvoker;
    private SocketClient socketClient;
    private Timer timer = new Timer();
    private TimerTask pingTimerTask;
    //来个事件监听，断线重试

    public void close() {
        socketClient.close();
    }

    public void send(String message) {
        socketClient.send(message);
    }

    @Subscribe
    public void listener(EventSetup msg) {
        //写入properties文件内
        try {
            if (EventEnum.SETUPDONE == msg.getType()) {
                PropertyUtils.writeProperties(Main.propName, Main.prop);
                return;
            }
            if (EventEnum.STARTUP == msg.getType()) {
                socketStart();
                //心跳
                if (pingTimerTask == null) {
                    pingTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            logger.debug("==========>: send ping.");
                            try {
                                socketClient.sendPing();
                            } catch (Exception e) {
                                logger.error("error to ping SocketServer." + e);
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
                            boolean result = socketStart();
                            logger.info("restart socketServer result:" + result);
                        } catch (Exception e) {
                            logger.error("error to start socket." + e);
                        }
                    }
                }, 60000);
            }
        } catch (Exception e) {
            logger.error("socket server init error." + e);
        }
    }

    private boolean socketStart() {
        Properties prop = Main.prop;
        try {
            if (machineInvoker == null) {
                MachineInvoker.init(prop.getProperty("device.comport"));
                machineInvoker = MachineInvoker.getInstance();
            }
            if (socketClient == null || socketClient.isClosed()) {
                URI serverUri;
                try {
                    serverUri = new URI(prop.getProperty("server.uri") + "?device_id=" + prop.getProperty("device.id"));
                } catch (URISyntaxException e) {
                    logger.error("error uri." + prop.getProperty("server.uri"));
                    return false;
                }
                socketClient = new SocketClient(serverUri);
                socketClient.register(this);
                socketClient.connect();
                return true;
            }
        } catch (Exception e) {
            logger.error("socketServer connect error." + e);
        }
        return false;
    }

    public class SocketClient extends WebSocketClient {

        private final EventBus eventBus = new EventBus();

        public SocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            short status = serverHandshake.getHttpStatus();
            if (101 != status) {
                logger.error("error to connect to server. status:" + status + ",message:" + serverHandshake.getHttpStatusMessage());
                return;
            }
            logger.info("ready to operate");
        }

        @Override
        public void onMessage(String s) {
            logger.info("received message." + s);
            //todo 轮询时间点修改
            try {
                Map<String, Object> message = JSONUtil.jsonToBean(s, mapType);
                boolean needResponse = message.containsKey("id");
                if (message.isEmpty() || !message.containsKey("action")) {
                    logger.info("illigal message: " + s);
                    Response resp = new Response();
                    if (message.containsKey("id")) {
                        resp.setId(String.valueOf(message.get("id")));
                    }
                    resp.setCode(0);
                    if (needResponse) {
                        this.send(JSONUtil.beanToJson(resp));
                    }
                    return;
                }
                String _id = "";
                if (message.containsKey("id")) {
                    _id = String.valueOf(message.get("id"));
                }
                String action = String.valueOf(message.get("action"));

                if (machineInvoker == null) {
                    if (needResponse) {
                        Response resp = new Response();
                        resp.setId(_id);
                        resp.setCode(0);
                        this.send(JSONUtil.beanToJson(resp));
                    }
                    return;
                }

                //查询机器状态
                if (ActionTypeEnum.机器状态.getId().equals(action)) {
                    ComResponse response = machineInvoker.status();
                    Response<String> resp = new Response<>();
                    resp.setId(_id);
                    resp.setCode(1);
                    String data = "2";
                    if (Result.success.equals(response.getCode())) {
                        data = "0";
                    }
                    if (Result.busy.equals(response.getCode())) {
                        data = "1";
                    }
                    resp.setData(data);
                    this.send(JSONUtil.beanToJson(resp));
                    return;
                }
                if (ActionTypeEnum.上机投币.getId().equals(action)) {
                    Map data = (Map)message.get("data");
                    Response<String> resp = new Response<>();
                    resp.setId(_id);
                    resp.setCode(0);
                    //初始化c1config
                    if (data == null) {
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    C1Config c1Config = JSONUtil.jsonToBean(JSONUtil.beanToJson(data), C1Config.class);
                    //其它参数如果不填就使用默认值
                    if (c1Config.getPlaytime() < 5 || c1Config.getPlaytime() > 90) {
                        logger.error("assign config error." + s);
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    ComResponse comResponse = machineInvoker.start(c1Config);
                    if (!Result.success.equals(comResponse.getCode())) {
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    resp.setCode(1);
                    this.send(JSONUtil.beanToJson(resp));
                    return;
                }
                if (ActionTypeEnum.操控指令.getId().equals(action)) {
                    Map data = (Map)message.get("data");
                    Response<Boolean> resp = new Response<>();
                    resp.setId(_id);
                    resp.setCode(0);
                    //初始化c1config
                    if (data == null || !data.containsKey("direction") ||
                            (!data.containsKey("FBtime") && !data.containsKey("LRtime") && !data.containsKey("doll"))) {
                        if (needResponse)
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    C2Config c2Config = new C2Config();
                    if (data.containsKey("FBtime")) {
                        c2Config.setFBtime((int)data.get("FBtime"));
                    }
                    if (data.containsKey("LRtime")) {
                        c2Config.setFBtime((int)data.get("LRtime"));
                    }
                    ComResponse comResponse = machineInvoker.pressButton(c2Config, (int)data.get("direction"));
                    //处理回调结果
                    logger.info("operate result:" + JSONUtil.beanToJson(comResponse));
                    if (comResponse == null || !Result.success.equals(comResponse.getCode())) {
                        resp.setCode(1);
                        resp.setData(false);
                        if (needResponse)
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    resp.setCode(1);
                    resp.setData(comResponse.getResult());
                    if (needResponse)
                    this.send(JSONUtil.beanToJson(resp));
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
            EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);
        }

        @Override
        public void onError(Exception e) {
            //连接出现问题，需要开启重连模式
            logger.error("error." + e);
            /*EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);*/
        }

        public void register(Object event) {
            eventBus.register(event);
        }

        public void unregister(Object event) {
            eventBus.unregister(event);
        }

    }

    public static void main(String[] args) {
        Response<String> response = new Response<>();
        response.setId("1");
        response.setCode(2);
        response.setData("3");
        System.out.println(JSONUtil.beanToJson(response));
    }

}
