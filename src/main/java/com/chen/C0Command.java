package com.chen;

import com.chen.common.component.Command;
import com.chen.common.component.Receiver;
import com.chen.common.component.Result;
import com.chen.model.ComRequest;
import com.chen.model.ComResponse;

import java.util.concurrent.TimeoutException;

/**
 * 查询机器状态接口: FF55C00000000000000000
 */
public class C0Command implements Command {
    public static final String command = "C0";

    private Receiver receiver;
    public static ComRequest comRequest = ComRequest.create(command);

    public C0Command(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public ComResponse execute(ComRequest comRequest) {
            ComResponse comResponse = receiver.action(C0Command.comRequest, 500);
            if (comResponse == null) {
                comResponse = new ComResponse();
            }
            if (comResponse.getArg1() == 0) {
                comResponse.setCode(Result.success); //空闲
            } else if (comResponse.getArg1() == 1) {
                comResponse.setCode(Result.busy); //机器忙
            } else {
                comResponse.setCode(Result.fail); //失败
            }
            return comResponse;
    }

}
