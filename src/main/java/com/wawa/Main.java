package com.wawa;

import com.wawa.common.utils.Localmac;
import com.wawa.common.utils.PropertyUtils;
import com.wawa.gui.StartupGui;
import com.wawa.service.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String propName = "startup.ini";
    public static Properties prop = PropertyUtils.loadProperties(propName);

    private void init() {
        if (!prop.containsKey("device.id")) {
            String deviceId = Localmac.getMac();
            if (deviceId != null && !deviceId.equals("")) {
                prop.setProperty("device.id", "ww-" + deviceId);
            }
        }
        //todo 请求服务端配置中心拉取配置
        //todo 如果失败另说




        //启动初始化界面，让用户选择相关参数，并提交至服务器
        StartupGui startupGui = new StartupGui();
        startupGui.register(new EventListener());
        // 显示应用 GUI
        SwingUtilities.invokeLater(startupGui::createAndShowGUI);

        logger.info("init success.");
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.init();
    }

}
