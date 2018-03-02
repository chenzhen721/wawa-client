package com.wawa.model;

/**
 * Created by Administrator on 2018/3/2.
 */
public class EventSetup {
    private String type;
    private String abc;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAbc() {
        return abc;
    }

    public void setAbc(String abc) {
        this.abc = abc;
    }

    @Override
    public String toString() {
        return "type:" + type;
    }
}
