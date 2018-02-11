package com.chen.gui;

import com.chen.C0Command;
import com.chen.C1Command;
import com.chen.C2Command;
import com.chen.ClientServer;
import com.chen.capture.CameraList;
import com.chen.common.component.Command;
import com.chen.common.component.Invoker;
import com.chen.gui.component.LogScroll;
import com.chen.gui.component.VideoComboBox;
import com.chen.model.C1Config;
import com.chen.model.C2Config;
import com.chen.model.ComRequest;
import com.chen.model.ComResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainGui {


    public static int frame_width = 0;
    public static int frame_height = 0;
    public static final double rate = 0.8;
    public static final JPanel jpanel = new JPanel();
    //main frame
    public static final JFrame frame = new JFrame("娃娃机");

    static {
        Dimension dimension = getScreenSize();
        frame_width = (int) (dimension.getWidth() * rate);
        frame_height = (int) (dimension.getHeight() * rate);
    }

    private static JScrollPane logScroll;
    private static VideoComboBox lcombobox;
    private static VideoComboBox rcombobox;
    private static JLabel jLabel= new JLabel("前后速度");
    private static JTextField jTextField = new JTextField("50");
    private static JLabel jLabel1 = new JLabel("前后时间");
    private static JTextField jTextField1 = new JTextField("5");
    private static JLabel jLabel2 = new JLabel("左右速度");
    private static JTextField jTextField2 = new JTextField("50");
    private static JLabel jLabel3 = new JLabel("左右时间");
    private static JTextField jTextField3 = new JTextField("5");
    private static JLabel jLabel4 = new JLabel("上下速度");
    private static JTextField jTextField4 = new JTextField("50");
    private static JLabel jLabel5 = new JLabel("弱抓力");
    private static JTextField jTextField5 = new JTextField("60");
    private static JLabel jLabel6 = new JLabel("强抓力");
    private static JTextField jTextField6 = new JTextField("90");
    private static JLabel jLabel7 = new JLabel("强转弱0-255");
    private static JTextField jTextField7 = new JTextField("110");
    private static JLabel jLabel8 = new JLabel("游戏时间5-90");
    private static JTextField jTextField8 = new JTextField("40");
    private static JLabel jLabel9 = new JLabel("出口0前1后");
    private static JTextField jTextField9 = new JTextField("1");

    private static JButton jButton = new JButton("开始");
    private static JButton jButton5 = new JButton("抓");
    private static JButton jButton1 = new JButton("上");
    private static JButton jButton2 = new JButton("下");
    private static JButton jButton3 = new JButton("左");
    private static JButton jButton4 = new JButton("右");

    private Invoker invoker = new Invoker();
    private C1Config c1Config = new C1Config();
    private final ExecutorService exec = Executors.newCachedThreadPool();

    /**
     * 创建并显示GUI。出于线程安全的考虑，
     */
    private void createAndShowGUI() {
        ClientServer clientServer = ClientServer.getInstance("COM3");
        C0Command c0Command = new C0Command(clientServer);
        C1Command c1Command = new C1Command(clientServer);
        C2Command c2Command = new C2Command(clientServer);
        invoker.setC0Command(c0Command);
        invoker.setC1Command(c1Command);
        invoker.setC2Command(c2Command);


        // 确保一个漂亮的外观风格
        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        //设置panel的layout以及sieze
        jpanel.setLayout(null);
        jpanel.setPreferredSize(new Dimension(frame_width, frame_height));

        //日志列表
        logScroll = this.initLogList(frame_width, frame_height);
        jpanel.add(logScroll);
        LogScroll.log(this.getClass(), "初始化...");

        //左侧视频下拉框
        lcombobox = new VideoComboBox();
        lcombobox.setBounds(20 + frame_width/3 + 50, 20, 200, 20);
        jpanel.add(lcombobox);

        //右侧视频下拉框
        rcombobox = new VideoComboBox();
        rcombobox.setBounds(frame_width - 400, 20, 200, 20);
        jpanel.add(rcombobox);

        //哪些参数需要设置

        int x = frame_width/3 + 50;
        int y = frame_height/2;
        int width = 100, height = 20;
        jLabel.setBounds(x, y, width, height);
        jTextField.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel);
        jpanel.add(jTextField);
        y = y + 30;
        jLabel1.setBounds(x, y, width, height);
        jTextField1.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel1);
        jpanel.add(jTextField1);
        y = y + 30;
        jLabel2.setBounds(x, y, width, height);
        jTextField2.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel2);
        jpanel.add(jTextField2);
        y = y + 30;
        jLabel3.setBounds(x, y, width, height);
        jTextField3.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel3);
        jpanel.add(jTextField3);
        y = y + 30;
        jLabel4.setBounds(x, y, width, height);
        jTextField4.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel4);
        jpanel.add(jTextField4);
        y = y + 30;
        jLabel5.setBounds(x, y, width, height);
        jTextField5.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel5);
        jpanel.add(jTextField5);
        y = y + 30;
        jLabel6.setBounds(x, y, width, height);
        jTextField6.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel6);
        jpanel.add(jTextField6);
        y = y + 30;
        jLabel7.setBounds(x, y, width, height);
        jTextField7.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel7);
        jpanel.add(jTextField7);
        y = y + 30;
        jLabel8.setBounds(x, y, width, height);
        jTextField8.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel8);
        jpanel.add(jTextField8);
        y = y + 30;
        jLabel9.setBounds(x, y, width, height);
        jTextField9.setBounds(x + 80, y, width, height);
        jpanel.add(jLabel9);
        jpanel.add(jTextField9);




        ActionListener actionListener = (ActionEvent e) -> {
            Runnable runnable = () -> {
                if (e.getSource() == jButton) { //开始
                    //生成c1config
                    c1Config.setFBspeed(Integer.valueOf(jTextField.getText()));
                    c1Config.setLRspeed(Integer.valueOf(jTextField2.getText()));
                    c1Config.setUDspeed(Integer.valueOf(jTextField4.getText()));
                    c1Config.setLightWeight(Integer.valueOf(jTextField5.getText()));
                    c1Config.setHeavyWeight(Integer.valueOf(jTextField6.getText()));
                    c1Config.setHeavyToLight(Integer.valueOf(jTextField7.getText()));
                    c1Config.setPlaytime(Integer.valueOf(jTextField8.getText()));
                    c1Config.setExitDirection(Integer.valueOf(jTextField9.getText()));
                    ComResponse comResponse = invoker.start(c1Config);
                    LogScroll.log(this.getClass(), jButton.getText() + ":" + comResponse.getCode());
                    return;
                }
                if (e.getSource() == jButton5) { //抓
                    System.out.println("操作：" + jButton5.getText());
                    C2Config c2Config = initC2Config();
                    ComResponse comResponse = invoker.pressButton(c2Config, 8);
                    LogScroll.log(this.getClass(), jButton5.getText() + ":" + comResponse.getCode() + " 结果:" + comResponse.getResult());
                    return;
                }
                if (e.getSource() == jButton1) { //上
                    System.out.println("操作：" + jButton1.getText());
                    C2Config c2Config = initC2Config();
                    c2Config.setFBtime(Integer.valueOf(jTextField1.getText()));
                    ComResponse comResponse = invoker.pressButton(c2Config, 0);
                    LogScroll.log(this.getClass(), jButton1.getText() + ":" + comResponse.getCode() + "");
                    return;
                }
                if (e.getSource() == jButton2) { //下
                    System.out.println("操作：" + jButton2.getText());
                    C2Config c2Config = initC2Config();
                    c2Config.setFBtime(Integer.valueOf(jTextField1.getText()));
                    ComResponse comResponse = invoker.pressButton(c2Config, 1);
                    LogScroll.log(this.getClass(), jButton2.getText() + ":" + comResponse.getCode() + "");
                    return;
                }
                if (e.getSource() == jButton3) { //左
                    System.out.println("操作：" + jButton3.getText());
                    C2Config c2Config = initC2Config();
                    c2Config.setLRtime(Integer.valueOf(jTextField3.getText()));
                    ComResponse comResponse = invoker.pressButton(c2Config, 2);
                    LogScroll.log(this.getClass(), jButton3.getText() + ":" + comResponse.getCode() + "");
                    return;
                }
                if (e.getSource() == jButton4) { //右
                    System.out.println("操作：" + jButton4.getText());
                    C2Config c2Config = initC2Config();
                    c2Config.setLRtime(Integer.valueOf(jTextField3.getText()));
                    ComResponse comResponse = invoker.pressButton(c2Config, 3);
                    LogScroll.log(this.getClass(), jButton4.getText() + ":" + comResponse.getCode() + "");
                    return;
                }
                LogScroll.log(this.getClass(), "unknown button source");
            };
            exec.submit(runnable);
        };

        //方向盘
        x = frame_width - 180;
        y = frame_height * 3/ 4 - 60;

        jButton.setBounds(frame_width - 100, y - 80, 60, 60);
        jButton.addActionListener(actionListener);
        jpanel.add(jButton);

        jButton5.setBounds(x, y, 60, 60);
        jButton5.addActionListener(actionListener);
        jpanel.add(jButton5);

        jButton1.setBounds(x, y - 80, 60, 60);
        jButton1.addActionListener(actionListener);
        jpanel.add(jButton1);

        jButton2.setBounds(x, y + 80, 60, 60);
        jButton2.addActionListener(actionListener);
        jpanel.add(jButton2);

        jButton3.setBounds(x - 80, y, 60, 60);
        jButton3.addActionListener(actionListener);
        jpanel.add(jButton3);

        jButton4.setBounds(x + 80, y, 60, 60);
        jButton4.addActionListener(actionListener);
        jpanel.add(jButton4);

        // 设置窗体属性
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(jpanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                lcombobox.onClose();
                rcombobox.onClose();
                if (clientServer != null) {
                    clientServer.onClose();
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        LogScroll.log(this.getClass(), "初始化成功！");
    }

    //获取屏幕大小
    public static Dimension getScreenSize() {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = screensize.width;
        int h = screensize.height;

        return new Dimension(w, h);
    }

    private static C2Config initC2Config() {
        C2Config c2Config = new C2Config();
        c2Config.setFBtime(0);
        c2Config.setLRtime(0);
        c2Config.setDoll(0); //默认为方向移动
        c2Config.setKeepTillTop(0); //todo 暂时不知道什么用
        return c2Config;
    }

    //初始化Log及文本框
    public JScrollPane initLogList(int width, int height) {
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> wordList = new JList<>(model);
        wordList.setVisibleRowCount(1);
        JScrollPane scrollPane = new JScrollPane(wordList);
        scrollPane.setBounds(20, 20, width/3, height - 30);
        LogScroll.init(model);
        return scrollPane;
    }

    public static void main(String[] args) {
        MainGui mainGui = new MainGui();
        // 显示应用 GUI
        SwingUtilities.invokeLater(mainGui::createAndShowGUI);
    }

}
