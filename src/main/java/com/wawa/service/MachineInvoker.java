package com.wawa.service;

import com.wawa.Main;
import com.wawa.common.component.Result;
import com.wawa.service.C0Command;
import com.wawa.service.C1Command;
import com.wawa.service.C2Command;
import com.wawa.model.C1Config;
import com.wawa.model.C2Config;
import com.wawa.model.ComRequest;
import com.wawa.model.ComResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class MachineInvoker {
    public static final Logger logger = LoggerFactory.getLogger(MachineInvoker.class);
    private static MachineInvoker machineInvoker;
    private static ClientServer clientServer;
    private static Boolean isMock = false;

    private C0Command c0Command;
    private C1Command c1Command;
    private C2Command c2Command;

    private AtomicInteger currentStep = new AtomicInteger(0);

    private MachineInvoker() {

    }

    public static MachineInvoker getInstance() {
        if (machineInvoker == null) {
            return null;
        }
        return machineInvoker;
    }

    //初始化invoker
    public static void init(String comPort) {
        if (machineInvoker != null) {
            return;
        }
        machineInvoker = new MachineInvoker();
        clientServer = ClientServer.getInstance(comPort);
        C0Command c0Command = new C0Command(clientServer);
        C1Command c1Command = new C1Command(clientServer);
        C2Command c2Command = new C2Command(clientServer);
        machineInvoker.setC0Command(c0Command);
        machineInvoker.setC1Command(c1Command);
        machineInvoker.setC2Command(c2Command);
        if (Main.prop.containsKey("client.mock")) {
            isMock = Boolean.valueOf((String) Main.prop.get("client.mock"));
        }
    }

    public void destroy() {
        if (clientServer != null) {
            clientServer.onClose();
        }
    }

    /**
     * 0-空闲，1-游戏中
     * @return
     */
    public ComResponse status() {
        if (currentStep.get() == 0) {
            if (isMock != null && isMock) {
                return mockSuccess();
            }
            return c0Command.execute(C0Command.comRequest);
        }
        ComResponse comResponse = new ComResponse();
        comResponse.setCode(Result.busy);
        return comResponse;
    }

    public ComResponse start(C1Config c1Config) {
        ComResponse comResponse;
        int update = 0;
        try {
            boolean step = currentStep.compareAndSet(0, 1);
            if (!step) {
                comResponse = new ComResponse();
                comResponse.setCode(Result.fail);
                return comResponse;
            }
            ComRequest request = c1Command.create(c1Config);
            comResponse = c1Command.execute(request);
            if (isMock != null && isMock) {
                comResponse = mockSuccess();
            }
            if (!Result.success.equals(comResponse.getCode())) {
                if (Result.operating.equals(comResponse.getCode())) {
                    update = 2;
                }
                comResponse.setCode(Result.fail);
            } else {
                update = 2;
                comResponse.setCode(Result.success);
            }
            currentStep.compareAndSet(1, update);
        } finally {
            if (currentStep.get() == 1) {
                currentStep.compareAndSet(1, update);
            }
        }
        return comResponse;
    }

    /**
     * 行为控制
     * 0-u, 1-d, 2-l, 3-r, 8-doll
     * @param direction
     * @return
     */
    public ComResponse pressButton(C2Config c2Config, int direction) {
        if (currentStep.get() < 2 && !isMock) {
            ComResponse comResponse = new ComResponse();
            comResponse.setCode(Result.waitingConfig);
            return comResponse;
        }
        if (currentStep.get() == 2 || (currentStep.get() == 3 && direction == 8)) {
            switch (direction) {
                case 0:
                    break;
                case 1:
                    c2Config.setFbtime(0 - c2Config.getFbtime());
                    break;
                case 2:
                    c2Config.setLrtime(0 - c2Config.getLrtime());
                    break;
                case 3:
                    break;
                case 8:
                    c2Config.setDoll(1);
                    break;
            }
            ComResponse comResponse = null;
            if ((direction == 8 && currentStep.compareAndSet(2, 3)) || currentStep.get() == 2) {
                comResponse = c2Command.execute(c2Command.create(c2Config));
            } else if (currentStep.get() == 3){
                comResponse = c2Command.waitForResult();
            }
            if (isMock != null && isMock) {
                comResponse = mockSuccess();
                if (direction == 8 || Result.waitingConfig.equals(comResponse.getCode())) {
                    currentStep.compareAndSet(3, 0);
                }
                return comResponse;
            }
            if (comResponse != null) {
                if (direction == 8 || Result.waitingConfig.equals(comResponse.getCode())) {
                    currentStep.compareAndSet(3, 0);
                }
                return comResponse;
            }
        }
        return null;
    }

    /**
     * 4, 5, 6, 7
     * @param direction
     * @return
     */
    public ComResponse releaseButton(C2Config c2Config, int direction) {
        switch (direction) {
            case 4:
                c2Config.setFbtime(0);
                break;
            case 5:
                c2Config.setFbtime(0);
                break;
            case 6:
                c2Config.setFbtime(0);
                break;
            case 7:
                c2Config.setFbtime(0);
                break;
        }
        return c2Command.execute(c2Command.create(c2Config));
    }

    private ComResponse mockSuccess() {
        ComResponse comResponse = new ComResponse();
        comResponse.setCode(Result.success);
        comResponse.setResult(true);
        return comResponse;
    }

    public C0Command getC0Command() {
        return c0Command;
    }

    public void setC0Command(C0Command c0Command) {
        this.c0Command = c0Command;
    }

    public C1Command getC1Command() {
        return c1Command;
    }

    public void setC1Command(C1Command c1Command) {
        this.c1Command = c1Command;
    }

    public C2Command getC2Command() {
        return c2Command;
    }

    public void setC2Command(C2Command c2Command) {
        this.c2Command = c2Command;
    }

}
