package com.wawa.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.eventbus.AsyncEventBus;
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
import com.wawa.model.ActionResult;
import com.wawa.model.Response;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class SocketServer {
    private final static Logger logger = LoggerFactory.getLogger(SocketServer.class);
    public static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    private static JavaType mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);
    private final AsyncEventBus eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
    private MachineInvoker machineInvoker;
    private SocketClient socketClient;
    private Timer timer = new Timer();
    private TimerTask pingTimerTask;

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
            }
            if (EventEnum.SHUTDOWN == msg.getType()) {
                //尝试重启websocket
                logger.info("========>30秒后尝试重新连接machine socket");
                //服务器断开了连接，需要监听端口重启
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            boolean result = socketStart();
                            logger.info("restart machine socketServer result:" + result);
                        } catch (Exception e) {
                            logger.error("error to start machine socket.", e);
                        }
                    }
                }, 30000);
            }
        } catch (Exception e) {
            logger.error("machine socket server init error.", e);
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
                    logger.error("error uri." + prop.getProperty("server.uri"), e);
                    return false;
                }
                socketClient = new SocketClient(serverUri);
                register(this);
                socketClient.connect();
                heartBeat();
                return true;
            }
        } catch (Exception e) {
            logger.error("machine socketServer connect error.", e);
        }
        return false;
    }

    public class SocketClient extends WebSocketClient {

//        private final EventBus eventBus = new EventBus();

        private ResultTimer resultTimer;

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
            //轮询时间点修改
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

                Response<ActionResult> resp = new Response<>();
                resp.setId(_id);
                resp.setCode(1);
                ActionResult actionResult = new ActionResult();
                resp.setData(actionResult);
                actionResult.setAction_type(action);
                //查询机器状态
                if (ActionTypeEnum.机器状态.getId().equals(action)) {
                    ComResponse response = machineInvoker.status();
                    String data = "2";
                    if (Result.success.equals(response.getCode())) {
                        data = "0";
                    }
                    if (Result.busy.equals(response.getCode())) {
                        data = "1";
                    }
                    actionResult.setResult(data);
                    this.send(JSONUtil.beanToJson(resp));
                    return;
                }
                if (ActionTypeEnum.上机投币.getId().equals(action)) {
                    Map data = (Map)message.get("data");
                    String log_id = (String) message.get("_id"); //本次上机分配的记录ID
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
                    //投币成功，倒计时到指定时间重置
                    if (resultTimer == null) {
                        resultTimer = new ResultTimer(log_id, c1Config, System.currentTimeMillis());
                        resultTimer.schedule();
                    }
                    resp.setCode(1);
                    actionResult.setLog_id(log_id);
                    this.send(JSONUtil.beanToJson(resp));
                    return;
                }
                if (ActionTypeEnum.操控指令.getId().equals(action)) {
                    Map data = (Map)message.get("data");
                    resp.setCode(0);
                    //初始化c1config
                    if (data == null || !data.containsKey("direction") ||
                            (!data.containsKey("fbtime") && !data.containsKey("lrtime") && !data.containsKey("doll"))) {
                        if (needResponse)
                        this.send(JSONUtil.beanToJson(resp));
                        return;
                    }
                    int direction = (int) data.get("direction");
                    C2Config c2Config = new C2Config();
                    if (data.containsKey("fbtime")) {
                        c2Config.setFbtime((int)data.get("fbtime"));
                    }
                    if (data.containsKey("lrtime")) {
                        c2Config.setLrtime((int)data.get("lrtime"));
                    }
                    ComResponse comResponse = machineInvoker.pressButton(c2Config, direction);
                    //处理回调结果
                    logger.info("operate:" + s + ", result:" + JSONUtil.beanToJson(comResponse));
                    resp.setCode(1);
                    if (8 == direction) {
                        String log_id = (String) message.get("_id");
                        Boolean auto = false;
                        if (data.containsKey("auto")) {
                            auto = (Boolean) data.get("auto");
                        }
                        if (auto && resultTimer == null) {
                            //已经发送过，取消
                            return;
                        }
                        if (auto && !StringUtils.isNotBlank(resultTimer.getLogId())) {
                            log_id = resultTimer.getLogId();
                        }
                        actionResult.setLog_id(log_id);
                        resetTimer();
                    }
                    if (comResponse == null || !Result.success.equals(comResponse.getCode())) {
                        actionResult.setResult(Boolean.FALSE.toString());
                    } else {
                        String result = Boolean.TRUE.toString();
                        if (c2Config.getDoll() == 1) {
                            result = comResponse.getResult().toString();
                        }
                        actionResult.setResult(result);
                    }
                    if (needResponse) {
                        this.send(JSONUtil.beanToJson(resp));
                        //todo 如果刚好结果回调不对，那就发http通知游戏结果
                    }
                    return;
                }
            } catch (Exception e) {
                logger.info("illigal message:" + s, e);
            }
            logger.error("error accur while received message:" + s);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            //发送通知 断线重连
            logger.info("machine socket server on close.i:" + i + ", s:" + s + ", b:" + b);
            EventSetup eventSetup = new EventSetup();
            eventSetup.setType(EventEnum.SHUTDOWN);
            eventBus.post(eventSetup);
        }

        @Override
        public void onError(Exception e) {
            //连接出现问题，需要开启重连模式
            logger.error("machine socket error.", e);
        }

        /*public void register(Object event) {
            eventBus.register(event);
        }

        public void unregister(Object event) {
            eventBus.unregister(event);
        }*/

        public void resetTimer() {
            if (resultTimer != null) {
                resultTimer.cancelSchedule();
                resultTimer = null;
            }
        }

    }

    public class ResultTimer extends TimerTask {
        private String logId; //本次上机用户的ID
        private C1Config config; //上机时的参数
        private Long timestamp; //时间戳
        private Timer timer = new Timer();

        public ResultTimer(String logId, C1Config config, Long timestamp) {
            this.logId = logId;
            this.config = config;
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            try {
                Map<String, Object> req = new HashMap<>();
                req.put("id", System.currentTimeMillis());
                req.put("action", ActionTypeEnum.操控指令.getId());
                Map<String, Object> op = new HashMap<>();
                req.put("data", op);
                req.put("_id", this.logId);
                req.put("ts", System.currentTimeMillis());
                op.put("doll", 1);
                op.put("direction", 8);
                op.put("auto", true);
                socketClient.onMessage(JSONUtil.beanToJson(req));
            } catch (Exception e) {
                logger.error("auto get result by timeout.", e);
            }
        }

        public void schedule() {
            timer.schedule(this, Long.parseLong("" + ((config.getPlaytime() + 5) * 1000)));
        }

        public void cancelSchedule() {
            timer.cancel();
        }

        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }

        public C1Config getConfig() {
            return config;
        }

        public void setConfig(C1Config config) {
            this.config = config;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Timer getTimer() {
            return timer;
        }

        public void setTimer(Timer timer) {
            this.timer = timer;
        }
    }

    private void heartBeat() {
        //心跳
        if (pingTimerTask == null) {
            pingTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (socketClient != null && !socketClient.isClosed()) {
                            logger.debug("==========>: machine socket send ping.");
                            socketClient.sendPing();
                        }
                    } catch (Exception e) {
                        logger.error("error to ping SocketServer.", e);
                    }
                }
            };
            timer.scheduleAtFixedRate(pingTimerTask, 60000, 60000);
        }
    }

    public void register(Object event) {
        eventBus.register(event);
    }

    public void unregister(Object event) {
        eventBus.unregister(event);
    }

    public static void main(String[] args) {
        Response<String> response = new Response<>();
        response.setId("1");
        response.setCode(2);
        response.setData("3");
        System.out.println(JSONUtil.beanToJson(response));
    }

}
