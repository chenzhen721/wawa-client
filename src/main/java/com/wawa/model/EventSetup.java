package com.wawa.model;

/**
 * Created by Administrator on 2018/3/2.
 */
public class EventSetup {
    private EventEnum type;

    public EventEnum getType() {
        return type;
    }

    public void setType(EventEnum type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "type:" + type;
    }
}
