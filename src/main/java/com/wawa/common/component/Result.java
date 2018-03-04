package com.wawa.common.component;

public interface Result {
    public static final String success = "0000";
    public static final String busy = "0001";
    public static final String operating = "0002"; //游戏中
    public static final String waitingConfig = "0003"; //
    public static final String errorCode = "0004";
    public static final String timeout = "0005";
    public static final String fail = "0006";
    public static final String timeoutWhileOfferingRequest = "0007";
}
