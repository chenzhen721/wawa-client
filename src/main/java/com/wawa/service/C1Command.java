package com.wawa.service;

import com.wawa.common.component.Command;
import com.wawa.common.component.Receiver;
import com.wawa.common.component.Result;
import com.wawa.model.C1Config;
import com.wawa.model.ComRequest;
import com.wawa.model.ComResponse;

/**
 * 查询机器状态接口: FF55C00000000000000000
 */
public class C1Command implements Command {
    //public static final String command = "C1";

    private Receiver receiver;

    public C1Command(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public ComResponse execute(ComRequest comRequest) {
        ComResponse comResponse = this.receiver.action(comRequest, 500);
        if (comResponse == null) {
            comResponse = new ComResponse();
        }
        if (comResponse.getArg1() == 0) {
            comResponse.setCode(Result.success);
        } else if (comResponse.getArg1() == 1) {
            comResponse.setCode(Result.operating);
        } else {
            comResponse.setCode(Result.fail);
        }
        return comResponse;
    }

    public ComRequest create(C1Config c1Config) {
        return ComRequest.create(c1Config.toBytes());
    }

}
