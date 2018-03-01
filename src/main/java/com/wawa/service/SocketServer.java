package com.wawa.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wawa.common.component.Result;
import com.wawa.common.utils.JSONUtil;
import com.wawa.model.ActionTypeEnum;
import com.wawa.model.ComResponse;
import com.wawa.model.Response;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SocketServer {
    private final static Logger logger = LoggerFactory.getLogger(SocketServer.class);
    public static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    private static JavaType mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);
    private MachineInvoker machineInvoker;
    private URI serverUri;
    private SocketClient socketClient;
    //来个事件监听，断线重试

    public SocketServer(String uri) {
        this.machineInvoker = MachineInvoker.getInstance();
        try {
            this.serverUri = new URI(uri);
        } catch (URISyntaxException e) {
            logger.error("error uri." + uri);
        }
        socketClient = new SocketClient(serverUri);
        socketClient.connect();
        logger.info("123123123123123123123123123123.");
    }

    public void close() {
        socketClient.close();
    }

    public void send(String message) {
        socketClient.send(message);
    }

    public class SocketClient extends WebSocketClient {

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
            try {
                Map<String, Object> message = JSONUtil.jsonToBean(s, mapType);
                if (message.isEmpty() || !message.containsKey("id") || !message.containsKey("action")) {
                    logger.info("illigal message: " + s);
                    return;
                }
                String _id = String.valueOf(message.get("id"));
                String action = String.valueOf(message.get("action"));

                if (machineInvoker == null) {
                    Response resp = new Response();
                    resp.setId(_id);
                    resp.setCode("1");
                    this.send(JSONUtil.beanToJson(resp));
                    return;
                }

                //查询机器状态
                if (ActionTypeEnum.STATUS.name().equals(action)) {
                    ComResponse response = machineInvoker.status();
                    Response<String> resp = new Response<>();
                    resp.setId(_id);
                    resp.setCode("1");
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
                if (ActionTypeEnum.ASSIGN.name().equals(action)) {

                }
                if (ActionTypeEnum.OPERATE.name().equals(action)) {

                }
            } catch (Exception e) {
                logger.info("illigal message:" + s);
            }
            logger.error("error accur while received message:" + s);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            //todo 发送通知 无论什么原因断线都需要重连尝试
            logger.error("close.");

        }

        @Override
        public void onError(Exception e) {
            //todo 连接出现问题，需要开启重连模式
            logger.error("error.");
        }


    }

    public static void main(String[] args) {
        Response<String> response = new Response<>();
        response.setId("1");
        response.setCode("2");
        response.setData("3");
        System.out.println(JSONUtil.beanToJson(response));
    }

}
