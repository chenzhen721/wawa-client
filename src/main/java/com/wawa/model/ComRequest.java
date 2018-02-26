package com.wawa.model;

import com.wawa.common.utils.StringUtils;

public class ComRequest {
    private byte prefix1 = (byte)0xFF;
    private byte prefix2 = (byte)0x55;
    private byte command;
    private byte arg1 = 0x0;
    private byte arg2 = 0x0;
    private byte arg3 = 0x0;
    private byte arg4 = 0x0;
    private byte arg5 = 0x0;
    private byte arg6 = 0x0;
    private byte arg7 = 0x0;
    private byte arg8 = 0x0;

    private ComRequest() {

    }

    public static ComRequest create(String hexString) {
        byte[] bytes = StringUtils.hexStringToBytes(hexString);
        return create(bytes);
    }

    public static ComRequest create(byte[] bytes) {
        int len = bytes.length;
        ComRequest comRequest = new ComRequest();
        comRequest.command = bytes[0];
        comRequest.arg1 = len >= 2 ? bytes[1] : 0x0;
        comRequest.arg2 = len >= 3 ? bytes[2] : 0x0;
        comRequest.arg3 = len >= 4 ? bytes[3] : 0x0;
        comRequest.arg4 = len >= 5 ? bytes[4] : 0x0;
        comRequest.arg5 = len >= 6 ? bytes[5] : 0x0;
        comRequest.arg6 = len >= 7 ? bytes[6] : 0x0;
        comRequest.arg7 = len >= 8 ? bytes[7] : 0x0;
        comRequest.arg8 = len >= 9 ? bytes[8] : 0x0;
        return comRequest;
    }

    public byte[] toArray() {
        byte[] bytes = new byte[12];
        bytes[0] = this.prefix1;
        bytes[1] = this.prefix2;
        bytes[2] = this.command;
        bytes[3] = this.arg1;
        bytes[4] = this.arg2;
        bytes[5] = this.arg3;
        bytes[6] = this.arg4;
        bytes[7] = this.arg5;
        bytes[8] = this.arg6;
        bytes[9] = this.arg7;
        bytes[10] = this.arg8;
        bytes[11] = StringUtils.sum_data(bytes, 11);
        return bytes;
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

    public byte getArg3() {
        return arg3;
    }

    public byte getArg4() {
        return arg4;
    }

    public byte getArg5() {
        return arg5;
    }

    public byte getArg6() {
        return arg6;
    }

    public byte getArg7() {
        return arg7;
    }

    public byte getArg8() {
        return arg8;
    }

}
