package com.chen.gui.component;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogScroll {
    private static DefaultListModel<String> listModel;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    public LogScroll() {

    }

    public static void init(DefaultListModel<String> model) {
        listModel = model;
    }

    public static synchronized void log(Class clazz, String txt) {
        if (listModel == null) {
            System.out.println("none model set");
        }

        listModel.add(0, sdf.format(new Date()) + ':' + clazz.getSimpleName() + ':' + txt);
        if (listModel.size() > 100) {
            listModel.removeElementAt(listModel.getSize() - 1);
        }
    }

}
