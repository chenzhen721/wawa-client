package com.wawa.model;

import com.wawa.common.utils.StringUtils;

public class ComResponse {
    private String code;
    private Boolean result; //只作为游戏结果使用
    private byte prefix1;
    private byte prefix2;
    private byte command;
    private byte arg1 = 0x5;
    private byte arg2;

    public void create(String hexString){
        byte[] bytes = StringUtils.hexStringToBytes(hexString);
        create(bytes);
    }

    public void create(byte[] bytes) {
        prefix1 = bytes[0];
        prefix2 = bytes[1];
        command = bytes[2];
        arg1 = bytes[3];
        arg2 = bytes[4];
    }

    public boolean valid() {
        if (prefix1 != (byte)0xFF || prefix2 != (byte)0x55) {
            return false;
        }
        return true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public byte getPrefix1() {
        return prefix1;
    }

    public byte getPrefix2() {
        return prefix2;
    }

    public byte getCommand() {
        return command;
    }

    public byte getArg1() {
        return arg1;
    }

    public byte getArg2() {
        return arg2;
    }

}
