package com.wawa.model;

public enum ActionTypeEnum {
    机器状态("STATUS"),
    上机投币("ASSIGN"),
    操控指令("OPERATE"),
    推流指令("STREAM"),
    重启指令("RESTART"),
            ;
    private String id;
    ActionTypeEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
