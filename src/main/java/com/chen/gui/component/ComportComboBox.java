package com.chen.gui.component;

import com.chen.serialport.SerialTool;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 串口下拉框
 */
public class ComportComboBox extends JComboBox<String> {

    public ComportComboBox() {
        super();
        List<String> list = SerialTool.findPort();
        if (list.size() <= 0) {
            LogScroll.log(this.getClass(), "none port found.");
            list.add("未找到串口端口");
        } else {
            list.add(0, "请选择端口");
        }
        String[] array = list.toArray(new String[0]);
        this.setModel(new DefaultComboBoxModel<>(array));

        this.addActionListener((ActionEvent e) -> {
            System.out.println(e.getSource());
        });
        LogScroll.log(this.getClass(), "串口下拉框初始化成功");
    }

}
