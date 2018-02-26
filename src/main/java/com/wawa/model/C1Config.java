package com.wawa.model;

/**
 * 抓取前设置控制命令的参数
 */
public class C1Config {
    /**
     * 设置天车前后速度，数值范围0~100%
     */
    private int FBspeed;
    /**
     * 设置天车左右速度，数值范围0~100%
     */
    private int LRspeed;
    /**
     * 设置天车上下速度，数值范围0~100%
     */
    private int UDspeed;
    /**
     * 弱爪力，数值范围0~100%
     */
    private int lightWeight;
    /**
     *强爪力，数值范围0~100%
     */
    private int heavyWeight;
    /**
     * 爪子上升到指定高度转到弱爪力 0~255%（100%表示上到顶掉）可以超过100%
     */
    private int heavyToLight;
    /**
     * 游戏时间 5~90秒
     */
    private int playtime;
    /**
     * 出口位置， 0为前出口， 1为后出口
     */
    private int exitDirection;

    public byte[] toBytes() {
        byte[] bytes = new byte[9];
        bytes[0] = -63;
        bytes[1] = (byte) (checkRange(FBspeed, 0, 100) & 0xFF);
        bytes[2] = (byte) (checkRange(LRspeed, 0, 100) & 0xFF);
        bytes[3] = (byte) (checkRange(UDspeed, 0, 100) & 0xFF);
        bytes[4] = (byte) (checkRange(lightWeight, 0, 100) & 0xFF);
        bytes[5] = (byte) (checkRange(heavyWeight, 0, 100) & 0xFF);
        bytes[6] = (byte) (checkRange(heavyToLight, 0, 255) & 0xFF);
        bytes[7] = (byte) (checkRange(playtime, 0, 100) & 0xFF);
        bytes[8] = (byte) (checkRange(exitDirection, 0, 1) & 0xFF);
        return bytes;
    }

    private int checkRange(int num, int min, int max) {
        return num < min ? min : num > max ? max : num;
    }

    public int getFBspeed() {
        return FBspeed;
    }

    public void setFBspeed(int FBspeed) {
        this.FBspeed = FBspeed;
    }

    public int getLRspeed() {
        return LRspeed;
    }

    public void setLRspeed(int LRspeed) {
        this.LRspeed = LRspeed;
    }

    public int getUDspeed() {
        return UDspeed;
    }

    public void setUDspeed(int UDspeed) {
        this.UDspeed = UDspeed;
    }

    public int getLightWeight() {
        return lightWeight;
    }

    public void setLightWeight(int lightWeight) {
        this.lightWeight = lightWeight;
    }

    public int getHeavyWeight() {
        return heavyWeight;
    }

    public void setHeavyWeight(int heavyWeight) {
        this.heavyWeight = heavyWeight;
    }

    public int getHeavyToLight() {
        return heavyToLight;
    }

    public void setHeavyToLight(int heavyToLight) {
        this.heavyToLight = heavyToLight;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    public int getExitDirection() {
        return exitDirection;
    }

    public void setExitDirection(int exitDirection) {
        this.exitDirection = exitDirection;
    }
}
