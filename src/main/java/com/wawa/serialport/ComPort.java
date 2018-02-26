package com.wawa.serialport;

import com.wawa.common.component.Event;
import com.wawa.common.component.EventObserver;
import com.wawa.common.utils.StringUtils;
import com.wawa.serialport.component.ReceiveDataListener;
import com.wawa.serialport.component.ReceiveDataObserver;
import gnu.io.SerialPort;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static com.wawa.common.utils.StringUtils.bytes2HexString;
import static com.wawa.common.utils.StringUtils.hexStringToBytes;

public class ComPort extends ReceiveDataObserver {
    static {
        System.out.println(System.getProperty("java.library.path"));
    }

    private String portName;
    private int baudrate;
    protected SerialPort mSerialPort;
    private EventObserver<String> receiveDataObserver;
    private Executor executor = Executors.newSingleThreadExecutor();

    private class ReadThread implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    if (mSerialPort == null) {
                        break;
                    }
                    byte[] buffer = SerialTool.readFromPort(mSerialPort);
                    if (buffer != null && buffer.length > 0) {
                        onDataReceived(buffer);
                    }
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ComPort(String portName, Integer baudrate) {
        this.portName = portName;
        this.baudrate = baudrate;
        this.receiveDataObserver = this;
    }

    public void setReceiveDataObserver(EventObserver<String> eventObserver) {
        this.receiveDataObserver = eventObserver;
    }

    public EventObserver getReceiveDataObserver() {
        return receiveDataObserver;
    }

    public boolean start() {
        try {
            System.out.println(SerialTool.findPort());
            if (mSerialPort == null) {
                try {
                    mSerialPort = SerialTool.openPort(this.portName, this.baudrate);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

			/* Create a receiving thread */
            ReadThread mReadThread = new ReadThread();
            FutureTask<String> futureTask = new FutureTask<>(mReadThread, "");
            executor.execute(futureTask);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }
    //protected abstract void onDataReceived(final byte[] buffer, final int size);

    public boolean sendData(byte[] buffer) {
        try {
            System.out.println(System.currentTimeMillis() - time + ":距离上次请求的时间间隔");
            time = System.currentTimeMillis();
            System.out.println("send:" + StringUtils.bytes2HexString(buffer));
            SerialTool.sendToPort(mSerialPort, buffer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    String readBuffer = "";
    public static Long time = System.currentTimeMillis();
    private synchronized void onDataReceived(byte[] buffer) {
        Long delta = System.currentTimeMillis() - time;
        System.out.println(delta + " :" + " ##### " + bytes2HexString(buffer));
        readBuffer += bytes2HexString(buffer);
        if (buffer.length < 6) {
            System.out.println("接收数据不合法，需要记录日志");
            return;
        }
        //指令一共6位
        //串口数据可能不是一次性给到
        while (readBuffer.length() >= 6 * 2) {
            if (readBuffer.startsWith("FF55")) {
                //数据校验 通过后发送给listener
                String msg = readBuffer.substring(0, 6 * 2);
                readBuffer = readBuffer.substring(6 * 2);
                if ("E0".equals(msg.substring(4, 6))) {
                    System.out.println("不支持的命令");
                }
                if ("E1".equals(msg.substring(4, 6))) {
                    System.out.println("输入的数据校验出错");
                }
                if (!check_sum_data(hexStringToBytes(msg))) {
                    System.out.println("接受数据校验不合法");
                }
                receiveDataObserver.fireEvent(new Event<>(msg));
            } else {
                //开头不正确
                if (readBuffer.contains("FF55")) {
                    readBuffer = readBuffer.substring(readBuffer.indexOf("FE"));
                } else {
                    readBuffer = "";
                }
            }
        }
        readBuffer = "";
    }

    public void destroy() {
        try {
            /*if (futureTask != null) {
                mReadThread.setRunningFlag(false);
                futureTask.cancel(true);
            }*/

            if (mSerialPort != null) {
                SerialTool.closePort(mSerialPort);
                mSerialPort = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean check_sum_data(byte[] data) {
        if (StringUtils.sum_data(data, data.length - 1) != data[data.length - 1]) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        ComPort comPort = new ComPort("COM3", 115200);
        comPort.setReceiveDataObserver(new ReceiveDataObserver(new ReceiveDataListener()));
        comPort.start();

        try {
            String a = "FF55C00000000000000000";
            String valid = bytes2HexString(new byte[] {StringUtils.sum_data(hexStringToBytes(a))});
            System.out.println("检查状态：" + a + valid);
            byte[] bytes = hexStringToBytes(a + valid);
            comPort.sendData(bytes);
            Thread.sleep(5);

            a = "FF55C30000000000000000";
            valid = bytes2HexString(new byte[] {StringUtils.sum_data(hexStringToBytes(a))});
            System.out.println("检查状态：" + a + valid);
            bytes = hexStringToBytes(a + valid);
            comPort.sendData(bytes);
                Thread.sleep(5);

            a = "FF55C15050502020802801";
            valid = bytes2HexString(new byte[] {StringUtils.sum_data(hexStringToBytes(a))});
            System.out.println("设置参数：" + a + valid);
            bytes = hexStringToBytes(a + valid);
            comPort.sendData(bytes);

                Thread.sleep(5);

            a = "FF55C20A0A000000010000";
            valid = bytes2HexString(new byte[] {StringUtils.sum_data(hexStringToBytes(a))});
            System.out.println("前后左右：" + a + valid);
            bytes = hexStringToBytes(a + valid);
            comPort.sendData(bytes);
                Thread.sleep(5);

            a = "FF55C20A0A010000000000";
            valid = bytes2HexString(new byte[] {StringUtils.sum_data(hexStringToBytes(a))});
            System.out.println("下抓状态：" + a + valid);
            bytes = hexStringToBytes(a + valid);
            comPort.sendData(bytes);

            Thread.sleep(1000);
            comPort.destroy();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
