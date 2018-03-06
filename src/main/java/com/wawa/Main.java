package com.wawa;

import com.wawa.common.utils.Localmac;
import com.wawa.common.utils.PropertyUtils;
import com.wawa.gui.StartupGui;
import com.wawa.service.SocketServer;
import com.wawa.service.VideoServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String url = "http://test-server.doll520.com/public/machine_on";
    public static final String propName = "startup.ini";
    public static Properties prop = PropertyUtils.loadProperties(propName);

    private void init() {
        if (!prop.containsKey("device.id")) {
            String deviceId = Localmac.getMac();
            if (deviceId != null && !deviceId.equals("")) {
                prop.setProperty("device.id", "ww-" + deviceId);
            }
        }

        //启动初始化界面，让用户选择相关参数，并提交至服务器
        StartupGui startupGui = new StartupGui();
        //操作流
        startupGui.register(new SocketServer());
        //视频流
        startupGui.register(new VideoServer());
        // 显示应用 GUI
        SwingUtilities.invokeLater(startupGui::createAndShowGUI);

        logger.info("init success.");
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.init();
    }

}
