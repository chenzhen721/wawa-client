package com.chen;

import com.chen.common.component.Event;
import com.chen.common.component.EventListener;
import com.chen.common.component.Receiver;
import com.chen.common.utils.StringUtils;
import com.chen.model.ComRequest;
import com.chen.model.ComResponse;
import com.chen.serialport.ComPort;

import javax.xml.ws.Response;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ClientServer implements EventListener<String>, Receiver {
    private static BlockingQueue<ComResponse> respQueue = new LinkedBlockingQueue<>();
    private ComPort comPort;
    private TokenServer tokenServer;

    private ClientServer(){

    }
    private static class SingletonHolder{
        private static ClientServer instance = new ClientServer();
    }
    //启动程序的同时，启动端口
    @SuppressWarnings("unchecked")
    public static ClientServer getInstance(String com) {
        if (com == null || "".equals(com.trim())) {
            return null;
        }
        ClientServer clientServer = SingletonHolder.instance;
        clientServer.tokenServer = new TokenServer();
        clientServer.comPort = new ComPort(com, 115200);
        clientServer.comPort.getReceiveDataObserver().addListener(clientServer);
        if (!clientServer.comPort.start()) {
            return null;
        }
        return clientServer;
    }

    @Override
    public void onActive(Event<String> msg) {
        //组装event， 放入respQueue中
        byte[] bytes = StringUtils.hexStringToBytes(msg.getMsg());
        if (StringUtils.sum_data(bytes, bytes.length - 2) != bytes[bytes.length - 1]) {
            System.out.println("error"); //
        }
        ComResponse comResponse = new ComResponse();
        comResponse.create(bytes);
        respQueue.offer(comResponse);
    }

    private final ExecutorService exec = Executors.newFixedThreadPool(1);

    @Override
    public ComResponse action(ComRequest request, int timeout) {
        /*final AtomicInteger flag = new AtomicInteger(1);*/
        Callable<ComResponse> callable = () -> {
            //调用完成后返回
            //各种校验后发送命令，然后等待结果返回
            if (tokenServer.apply()) {
                boolean isSend = comPort.sendData(request.toArray());
                if (!isSend) {
                    System.out.println("send data fail.");
                    return null;
                }
                ComResponse response;
                while (true) {
                    response = respQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (response.getCommand() != request.getCommand()) {
                        System.out.println("不是本次请求结果，丢弃！");
                        continue;
                    }
                    break;
                }
                /*flag.compareAndSet(0, 1);*/
                return response;
            }
            return null;
        };
        Future<ComResponse> future = exec.submit(callable);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException|ExecutionException|TimeoutException e) {
            e.printStackTrace();
        }
        //超时
        return null;
    }

    public void onClose() {
        comPort.destroy();
    }

    static class TokenServer {

        private static int token_waiting_time = 30; //获取token的最小间隔时间
        private AtomicBoolean token = new AtomicBoolean(true);
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        final Timer timer = new Timer();
        private TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                token.set(true);
            }
        };

        //获取token,并指定使用时长
        private boolean apply() {
            while (!token.compareAndSet(true, false)) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timer.schedule(timerTask, token_waiting_time);
            return true;
        }
    }

    public static void main(String[] args) {

    }


}
