package com.wawa;

import com.wawa.common.utils.Localmac;
import com.wawa.service.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;

public class Main {
    private Logger logger = LoggerFactory.getLogger(Main.class);
    private SocketServer socketServer;

    private void init() {
        //todo 获取各种配置信息
        Properties prop = loadProperties("startup.ini");
        if (prop == null) {
            logger.error("error.");
            return;
        }
        String host = prop.getProperty("server.uri");

        logger.info("init success.");
        String localmac = null;
        try {
            localmac = Localmac.getLocalMac(InetAddress.getLocalHost());
        } catch (Exception e) {
            logger.error("" + e);
            return;
        }
        logger.info(localmac);
        String uri = host + "?device_id=ww-" + localmac;
        socketServer = new SocketServer(uri);
        int i = 0;
        /*while(i++ < 1000) {
            try {
                Thread.sleep(2000);
                socketServer.send("ping!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }


    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Main main = new Main();
                main.init();
            }
        }).start();
    }

    private Properties loadProperties(String fileName) {
        String filePath = System.getProperty("user.dir") + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error(fileName + " does not exists.");
            return null;
        }
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            return props;
        } catch (Exception e) {
            logger.error("" + e);
        }
        return null;
    }

}
