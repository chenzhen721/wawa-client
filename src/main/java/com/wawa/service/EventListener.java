package com.wawa.service;

import com.google.common.eventbus.Subscribe;
import com.wawa.Main;
import com.wawa.common.utils.PropertyUtils;
import com.wawa.model.EventSetup;

/**
 * Created by Administrator on 2018/3/2.
 */
public class EventListener {
    @Subscribe
    public void listener(EventSetup msg) {
        System.out.println("told me:" + msg);
        //写入properties文件内
        if ("done".equals(msg.getType())) {
            PropertyUtils.writeProperties(Main.propName, Main.prop);
            //socketServer.send("");
        }
    }
    @Subscribe
    public void listen(Integer msg) {
        System.out.println("get integer:" + msg);
    }
}
