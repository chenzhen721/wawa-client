package com.chen;

import com.chen.common.component.Command;
import com.chen.common.component.Receiver;
import com.chen.common.component.Result;
import com.chen.model.C1Config;
import com.chen.model.C2Config;
import com.chen.model.ComRequest;
import com.chen.model.ComResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 查询机器状态接口: FF55C00000000000000000
 */
public class C2Command implements Command {
    public static final String command = "C2";

    private Receiver receiver;

    public C2Command(Receiver receiver) {
        this.receiver = receiver;
    }
    private final ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    public ComResponse execute(ComRequest comRequest) {
        ComResponse comResponse = this.receiver.action(comRequest, 500);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (comResponse == null) {
            comResponse = new ComResponse();
        }
        if (comResponse.getArg1() == 1) {
            comResponse.setCode(Result.waitingConfig);
            return comResponse;
        }
        if (comResponse.getArg1() == 2 || comResponse.getArg1() == 3) {
            comResponse.setCode(Result.fail);
            return comResponse;
        }
        if (comResponse.getArg1() == 0){ //判断是否请求成功
            comResponse.setCode(Result.success);
            //判断是否是已经下抓
            if (comRequest.getArg3() == 0) {
                return comResponse;
            }
            //如果已经下抓，轮询接口查询抓取结果，
            final AtomicInteger flag = new AtomicInteger(1);
            Callable<ComResponse> callable = () -> {
                //调用完成后返回
                //各种校验后发送命令，然后等待结果返回
                while (flag.get() == 1) {
                    ComResponse response = this.receiver.action(C0Command.comRequest, 500);
                    if (response.getArg1() == (byte)0x0) {
                        return response;
                    }
                    Thread.sleep(500);
                }
                return null;
            };
            Future<ComResponse> future = exec.submit(callable);
            try {
                comResponse = future.get(20000, TimeUnit.MILLISECONDS);
                if (comResponse == null) {
                    comResponse = new ComResponse();
                }
                comResponse.setCode(Result.success);
                if (comResponse.getArg2() != 0) {
                    comResponse.setResult(true);
                } else {
                    comResponse.setResult(false);
                }
                return comResponse;
            } catch (InterruptedException|ExecutionException |TimeoutException e) {
                e.printStackTrace();
                flag.set(0); //
            }
            //超时
            comResponse.setCode(Result.timeoutWhileOfferingRequest);
            comResponse.setResult(false);
            return comResponse;
        }
        comResponse.setCode(Result.fail);
        return comResponse;
    }

    public ComRequest create(C2Config c2Config) {
        return ComRequest.create(c2Config.toBytes());
    }

}
