package com.wawa.gui;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.eventbus.EventBus;
import com.wawa.Main;
import com.wawa.capture.CameraList;
import com.wawa.common.serialport.SerialTool;
import com.wawa.common.utils.CaptureUtils;
import com.wawa.common.utils.JSONUtil;
import com.wawa.common.utils.http.HttpClientUtils;
import com.wawa.gui.component.VideoComboBox;
import com.wawa.model.EventEnum;
import com.wawa.model.EventSetup;
import com.wawa.model.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Administrator on 2018/3/2.
 */
public class StartupGui extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(StartupGui.class);
    public static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    public static int frame_width = 0;
    public static int frame_height = 0;
    public static final double rate = 0.5;
    public static final JPanel jpanel = new JPanel();
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private EventBus eventBus = new EventBus(); //按钮事件触发

    private static JLabel jLabel = new JLabel("设备编号"); //device.id mac通常是mac地址
    private static JLabel jLabel00 = new JLabel("");
    private static JLabel jLabel1 = new JLabel("设备名称"); //device.name手动指定（方便查看）
    private static JTextField jTextField1 = new JTextField("");
    private static JLabel jLabel2 = new JLabel("操作控制URI"); //server.uri
    //    private static JLabel jLabel22 = new JLabel("");
    private static JTextField jLabel22 = new JTextField("");
    private static JLabel jLabel3 = new JLabel("视频流URI"); //stream.uri
    private static JLabel jLabel33 = new JLabel("");
    private static JLabel jLabel4 = new JLabel("娃娃机串口号"); //device.comport
    private static JComboBox<String> comPortBox1 = new JComboBox<>();
    private static JLabel jLabel5 = new JLabel("摄像头1"); //device.camera1
    private static JComboBox<String> cameraBox1 = new JComboBox<>();
    private static JLabel jLabel6 = new JLabel("摄像头2"); //device.camera2
    private static JComboBox<String> cameraBox2 = new JComboBox<>();
    private static JLabel jLabel7 = new JLabel("商户号"); //device.appid
    private static JLabel jLabel77 = new JLabel("");
    private static JLabel jLabel8 = new JLabel("访问令牌"); //device.token
    private static JLabel jLabel88 = new JLabel("");
    private static JButton jButton = new JButton("同步配置");
    private static JButton jButton1 = new JButton("启动服务");
    private static JLabel jLabel9 = new JLabel("");

    static {
        Dimension dimension = getScreenSize();
        frame_width = (int) (dimension.getWidth() * rate);
        frame_height = (int) (dimension.getHeight() * rate);
        int width = 200, height = 25, deltaHeight = 40, deltaWidth = 120;
        int x = frame_width / 2 - width;
        int y = frame_height / 12;
        jLabel7.setBounds(x, y, width, height);
        jLabel77.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel7);
        jpanel.add(jLabel77);
        y = y + deltaHeight;
        jLabel8.setBounds(x, y, width, height);
        jLabel88.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel8);
        jpanel.add(jLabel88);
        y = y + deltaHeight;
        jLabel.setBounds(x, y, width, height);
        jLabel00.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel);
        jpanel.add(jLabel00);
        y = y + deltaHeight;
        jLabel1.setBounds(x, y, width, height);
        jTextField1.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel1);
        jpanel.add(jTextField1);
        y = y + deltaHeight;
        jLabel2.setBounds(x, y, width, height);
        jLabel22.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel2);
        jpanel.add(jLabel22);
        y = y + deltaHeight;
        jLabel3.setBounds(x, y, width, height);
        jLabel33.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel3);
        jpanel.add(jLabel33);
        y = y + deltaHeight;
        jLabel4.setBounds(x, y, width, height);
        comPortBox1.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel4);
        jpanel.add(comPortBox1);
        y = y + deltaHeight;
        jLabel5.setBounds(x, y, width, height);
        cameraBox1.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel5);
        jpanel.add(cameraBox1);
        y = y + deltaHeight;
        jLabel6.setBounds(x, y, width, height);
        cameraBox2.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel6);
        jpanel.add(cameraBox2);

        y = y + deltaHeight;
        jButton.setBounds(x, y, width / 2, height);
        jButton1.setBounds(x + deltaWidth, y, width / 2, height);
        jpanel.add(jButton);
        jpanel.add(jButton1);

        y = y + deltaHeight;
        jLabel9.setBounds(x, y, width * 2, height);
        jpanel.add(jLabel9);
    }

    public void createAndShowGUI() {
        refreshProp();

        //增加按钮事件
        ActionListener actionListener = (ActionEvent e) -> {
            Runnable runnable = () -> {
                EventSetup eventSetup = new EventSetup();
                if (e.getSource() == jButton) { //开始
                    //更新props
                    if (!postConfig()) {
                        return;
                    }
                    refreshProp();
                    eventSetup.setType(EventEnum.SETUPDONE);
                    jButton1.doClick();
                }
                if (e.getSource() == jButton1) {
                    eventSetup.setType(EventEnum.STARTUP);
                }
                eventBus.post(eventSetup);
            };
            exec.submit(runnable);
        };
        jButton.addActionListener(actionListener);
        jButton1.addActionListener(actionListener);
        cameraBox1.addItemListener((itemEvent) -> {
            int stateChange = itemEvent.getStateChange();
            //切换的摄像头
            if (ItemEvent.SELECTED == stateChange) {
                int index = cameraBox1.getSelectedIndex();
                String value = String.valueOf(cameraBox1.getSelectedItem());
                if (index == 0) {
                    value = "";
                }
                Main.prop.setProperty("device.camera1", value);
                refreshProp();
            }
        });
        cameraBox2.addItemListener((itemEvent) -> {
            int stateChange = itemEvent.getStateChange();
            //切换的摄像头
            if (ItemEvent.SELECTED == stateChange) {
                int index = cameraBox2.getSelectedIndex();
                String value = String.valueOf(cameraBox2.getSelectedItem());
                if (index == 0) {
                    value = "";
                }
                Main.prop.setProperty("device.camera2", value);
                refreshProp();
            }
        });

        //jButton.doClick();

        // 创建及设置窗口
        //设置panel的layout以及sieze
        jpanel.setLayout(null);
        jpanel.setPreferredSize(new Dimension(frame_width, frame_height));
        // 设置窗体属性
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(jpanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public void refreshProp() {
        Properties prop = Main.prop;
        //读取配置文件参数
        //如果这个商户号不存在，则需要重新拉取配置
        String appId = prop.getProperty("device.appid"); //为这台机器分配的商户号
        jLabel77.setText(appId);
        String token = prop.getProperty("device.token"); //为这台机器分配的访问token
        jLabel88.setText(token);
        String deviceId = prop.getProperty("device.id"); //机器ID，通常是机器的mac
        jLabel00.setText(deviceId);
        String deviceName = prop.getProperty("device.name"); //机器名称，维护标识
        jTextField1.setText(deviceName);
        String server = prop.getProperty("server.uri"); //操作地址
        jLabel22.setText(server);
        String stream = prop.getProperty("stream.uri"); //视频流地址
        jLabel33.setText(stream);

        String comPort = prop.getProperty("device.comport"); //串口号
        List<String> portList = SerialTool.findPort();
        String[] array = new String[portList.size() + 1];
        String selectedItem;
        if (portList.size() <= 0) {
            logger.error("None Camera Found.");
            portList.add("未找到串口设备");
            selectedItem = portList.get(0);
        } else {
            portList.add(0, "请选择串口设备");
            selectedItem = portList.get(0);
            if (portList.contains(comPort)) {
                selectedItem = comPort;
            }
        }
        ComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(portList.toArray(array));
        comboBoxModel.setSelectedItem(selectedItem);
        comPortBox1.setModel(comboBoxModel);

        String[] cameraList = CaptureUtils.cameraList();

        String camera1 = prop.getProperty("device.camera1"); //摄像头1
        array = new String[cameraList.length + 1];
        if (cameraList.length <= 0) {
            logger.error("None Camera Found.");
            array[0] = "未找视频采集设备";
            selectedItem = array[0];
        } else {
            array[0] = "请选择视频设备";
            selectedItem = array[0];
            for (int i = 0; i < cameraList.length; i++) {
                if (cameraList[i].equals(camera1)) {
                    selectedItem = camera1;
                }
                array[i + 1] = cameraList[i];
            }
        }
        comboBoxModel = new DefaultComboBoxModel<>(array);
        comboBoxModel.setSelectedItem(selectedItem);
        cameraBox1.setModel(comboBoxModel);

        String camera2 = prop.getProperty("device.camera2"); //摄像头2
        selectedItem = array[0];
        if (Arrays.asList(array).contains(camera2)) {
            selectedItem = camera2;
        }
        comboBoxModel = new DefaultComboBoxModel<>(array);
        comboBoxModel.setSelectedItem(selectedItem);
        cameraBox2.setModel(comboBoxModel);

    }

    //获取屏幕大小
    public static Dimension getScreenSize() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = screensize.width;
        int h = screensize.height;

        return new Dimension(w, h);
    }

    public void register(Object obj) {
        eventBus.register(obj);
    }

    public void unRegister(Object obj) {
        eventBus.unregister(obj);
    }

    public boolean postConfig() {
        try {
            Properties prop = Main.prop;
            String _id = prop.getProperty("device.id"); //机器ID，通常是机器的mac
            String name = jTextField1.getText(); //机器名称，维护标识
            if (StringUtils.isBlank(name)) {
                jLabel9.setText("请填写机器名称。");
                return false;
            }
            prop.setProperty("device.name", name);
            String comport = (String) comPortBox1.getSelectedItem();
            int index = comPortBox1.getSelectedIndex();
            if (index == 0 || StringUtils.isBlank(comport)) {
                jLabel9.setText("请选择串口号。");
                return false;
            }
            prop.setProperty("device.comport", comport);
            String camera1 = (String) cameraBox1.getSelectedItem();
            int camera1Index = cameraBox1.getSelectedIndex();
            if (camera1Index == 0) {
                camera1 = null;
            }
            prop.setProperty("device.camera1", camera1);
            String camera2 = (String) cameraBox2.getSelectedItem();
            int camera2Index = cameraBox2.getSelectedIndex();
            if (camera2Index == 0) {
                camera2 = "";
            }
            if (StringUtils.isBlank(camera1) && StringUtils.isBlank(camera2)) {
                jLabel9.setText("请选择摄像头。");
                return false;
            }
            if (camera1Index == camera2Index) {
                camera2 = "";
                cameraBox2.setSelectedIndex(0);
                jLabel9.setText("警告：两个摄像头配置为同一个，只生效一个");
            }
            prop.setProperty("device.camera2", camera2);

            String url = prop.getProperty("device.url");
            if (url == null) {
                url = Main.url;
            }
            Map<String, String> param = new HashMap<>();
            param.put("_id", _id);
            param.put("name", name);
            param.put("comport", comport);
            param.put("camera1", camera1);
            param.put("camera2", camera2);
            String str = HttpClientUtils.post(url, param, null);
            Response<Map<String, Object>> resp = parseResponse(str);
            if (resp == null || resp.getCode() == null || 1 != resp.getCode()) {
                jLabel9.setText("配置信息拉取失败，请联系管理员。" + str);
                return false;
            }
            Map<String, Object> result = resp.getData();
            if (result.containsKey("app_id")) {
                prop.setProperty("device.appid", (String) result.get("app_id"));
            }
            if (result.containsKey("app_token")) {
                prop.setProperty("device.token", (String) result.get("app_token"));
            }
            if (result.containsKey("server_uri")) {
                prop.setProperty("server.uri", (String) result.get("server_uri"));
            }
            if (result.containsKey("push_uri")) {
                prop.setProperty("stream.uri", (String) result.get("push_uri"));
            }
            if (result.containsKey("url")) {
                prop.setProperty("device.url", (String) result.get("url"));
            }
            if (result.containsKey("last_modify")) {
                prop.setProperty("last.modify", String.valueOf(result.get("last_modify")));
            }
            jLabel9.setText("配置信息拉取成功。");
            return true;
        } catch (IOException e) {
            logger.error("配置信息拉取失败！" + e);
        }
        jLabel9.setText("配置信息拉取失败！");
        return false;
    }

    public static Response<Map<String, Object>> parseResponse(String response) {
        JavaType innerType = typeFactory.constructMapLikeType(Map.class, String.class, Object.class);
        JavaType javaType = typeFactory.constructSimpleType(Response.class, new JavaType[]{innerType});
        try {
            return JSONUtil.jsonToBean(response, javaType);
        } catch (Exception e) {
            logger.error("parse response error." + response);
        }
        return null;
    }

}
