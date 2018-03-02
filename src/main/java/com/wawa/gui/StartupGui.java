package com.wawa.gui;

import com.google.common.eventbus.EventBus;
import com.wawa.Main;
import com.wawa.common.serialport.SerialTool;
import com.wawa.common.utils.CaptureUtils;
import com.wawa.common.utils.Localmac;
import com.wawa.gui.component.VideoComboBox;
import com.wawa.model.EventSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/3/2.
 */
public class StartupGui extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(StartupGui.class);
    public static int frame_width = 0;
    public static int frame_height = 0;
    public static final double rate = 0.5;
    public static final JPanel jpanel = new JPanel();
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private EventBus eventBus = new EventBus();

    private static JLabel jLabel= new JLabel("设备编号"); //device.id mac通常是mac地址
    private static JTextField jTextField = new JTextField("");
    private static JLabel jLabel1 = new JLabel("设备名称"); //device.name手动指定（方便查看）
    private static JTextField jTextField1 = new JTextField("");
    private static JLabel jLabel2 = new JLabel("操作控制URI"); //server.uri
    private static JTextField jTextField2 = new JTextField("");
    private static JLabel jLabel3 = new JLabel("视频流URI"); //stream.uri
    private static JTextField jTextField3 = new JTextField("");
    private static JLabel jLabel4 = new JLabel("娃娃机串口号"); //device.comport
    private static JComboBox<String> comPortBox1 = new JComboBox<>();
    private static JLabel jLabel5 = new JLabel("摄像头1"); //device.camera1
    private static JComboBox<String> cameraBox1 = new JComboBox<>();
    private static JLabel jLabel6 = new JLabel("摄像头2"); //device.camera2
    private static JComboBox<String> cameraBox2 = new JComboBox<>();
    private static JLabel jLabel7 = new JLabel("商户号"); //device.appid
    private static JTextField jTextField7 = new JTextField("");
    private static JLabel jLabel8 = new JLabel("访问令牌"); //device.token
    private static JTextField jTextField8 = new JTextField("");
    private static JLabel jLabel9 = new JLabel("更新时间戳"); //last.modify
    private static JTextField jTextField9 = new JTextField("");
    private static JButton jButton = new JButton("确定");

    static {
        Dimension dimension = getScreenSize();
        frame_width = (int) (dimension.getWidth() * rate);
        frame_height = (int) (dimension.getHeight() * rate);
        int width = 200, height = 25, deltaHeight = 40, deltaWidth = 120;
        int x = frame_width/2 - width;
        int y = frame_height/12;
        jLabel7.setBounds(x, y, width, height);
        jTextField7.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel7);
        jpanel.add(jTextField7);
        y = y + deltaHeight;
        jLabel8.setBounds(x, y, width, height);
        jTextField8.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel8);
        jpanel.add(jTextField8);
        y = y + deltaHeight;
        jLabel.setBounds(x, y, width, height);
        jTextField.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel);
        jpanel.add(jTextField);
        y = y + deltaHeight;
        jLabel1.setBounds(x, y, width, height);
        jTextField1.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel1);
        jpanel.add(jTextField1);
        y = y + deltaHeight;
        jLabel2.setBounds(x, y, width, height);
        jTextField2.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel2);
        jpanel.add(jTextField2);
        y = y + deltaHeight;
        jLabel3.setBounds(x, y, width, height);
        jTextField3.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel3);
        jpanel.add(jTextField3);
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
        jLabel9.setBounds(x, y, width, height);
        jTextField9.setBounds(x + deltaWidth, y, width, height);
        jpanel.add(jLabel9);
        jpanel.add(jTextField9);

        y = y + deltaHeight;
        jButton.setBounds(x + deltaWidth / 2, y, width, height);
        jpanel.add(jButton);

    }

    public void createAndShowGUI() {
        Properties prop = Main.prop;
        //读取配置文件参数
        //如果这个商户号不存在，则需要重新拉取配置
        String appId = prop.getProperty("device.appid"); //为这台机器分配的商户号
        jTextField7.setText(appId);
        String token = prop.getProperty("device.token"); //为这台机器分配的访问token
        jTextField8.setText(token);
        String deviceId = prop.getProperty("device.id"); //机器ID，通常是机器的mac
        jTextField.setText(deviceId);
        String deviceName = prop.getProperty("device.name"); //机器名称，维护标识
        jTextField1.setText(deviceName);
        String server = prop.getProperty("server.uri"); //操作地址
        jTextField2.setText(server);
        String stream = prop.getProperty("stream.uri"); //视频流地址
        jTextField3.setText(stream);

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

        String [] cameraList = CaptureUtils.cameraList();

        String camera1 = prop.getProperty("device.camera1"); //摄像头1
        array = new String[cameraList.length + 1];
        if (cameraList.length <= 0) {
            logger.error("None Camera Found.");
            array[0] = "未找视频采集设备";
            selectedItem = array[0];
        } else {
            array[0] = "请选择视频设备";
            selectedItem = array[0];
            for(int i = 0; i < cameraList.length; i++) {
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
        if (Arrays.asList(array).contains(camera2)) {
            selectedItem = camera2;
        }
        comboBoxModel = new DefaultComboBoxModel<>(array);
        comboBoxModel.setSelectedItem(selectedItem);
        cameraBox2.setModel(comboBoxModel);

        String lastModify = prop.getProperty("last.modify"); //时间戳
        jTextField9.setText(lastModify);

        // 创建及设置窗口
        //设置panel的layout以及sieze
        jpanel.setLayout(null);
        jpanel.setPreferredSize(new Dimension(frame_width, frame_height));

        //增加按钮事件
        ActionListener actionListener = (ActionEvent e) -> {
            Runnable runnable = () -> {
                if (e.getSource() == jButton) { //开始
                    //更新props
                    EventSetup eventSetup = new EventSetup();
                    eventSetup.setType("done");
                    eventBus.post(eventSetup);
                    Integer a = 123;
                    eventBus.post(a);
                }
            };
            exec.submit(runnable);
        };
        jButton.addActionListener(actionListener);

        // 设置窗体属性
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(jpanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
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

}
