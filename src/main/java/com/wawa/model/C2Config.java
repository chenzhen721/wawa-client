package com.wawa.model;

/**
 * 抓取时控制命令的参数
 */
public class C2Config {

    /**
     * 加载天车前后运行时间，数值范围 -100~100。（单位x100ms，100等于10秒）
     */
    private int fbtime;
    /**
     * 加载天车左右运行时间，数值范围 -100~100。（单位x100ms，100等于10秒）
     */
    private int lrtime;
    /**
     * 0为无效，数值不为0表示要下爪
     */
    private int doll;
    /**
     * 上到顶继续保持强爪力的时间 0~100（单位x10ms，100等于1秒）
     */
    private int keepTillTop;

    public byte[] toBytes() {
        byte[] bytes = new byte[8];
        bytes[0] = -62;
        bytes[1] = (byte) (checkRange(fbtime, -100, 100) & 0xFF);
        bytes[2] = (byte) (checkRange(lrtime, -100, 100) & 0xFF);
        bytes[3] = (byte) (checkRange(doll, 0, 100) & 0xFF);
        bytes[4] = 0;
        bytes[5] = 0;
        bytes[6] = (byte) (checkRange(keepTillTop, 0, 100) & 0xFF);
        return bytes;
    }

    private int checkRange(int num, int min, int max) {
        return num < min ? min : num > max ? max : num;
    }

    public int getFbtime() {
        return fbtime;
    }

    public void setFbtime(int fbtime) {
        this.fbtime = fbtime;
    }

    public int getLrtime() {
        return lrtime;
    }

    public void setLrtime(int lrtime) {
        this.lrtime = lrtime;
    }

    public int getDoll() {
        return doll;
    }

    public void setDoll(int doll) {
        this.doll = doll;
    }

    public int getKeepTillTop() {
        return keepTillTop;
    }

    public void setKeepTillTop(int keepTillTop) {
        this.keepTillTop = keepTillTop;
    }
}
