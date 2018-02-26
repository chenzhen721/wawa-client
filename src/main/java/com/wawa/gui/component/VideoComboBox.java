package com.wawa.gui.component;

import com.wawa.capture.CameraList;
import com.wawa.common.utils.CaptureUtils;
import com.wawa.gui.MainGui;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 串口下拉框
 */
public class VideoComboBox extends JComboBox<String> {

    private Map<Integer, CameraList> map = new HashMap<>();
    private int currentIndex = -1;

    public VideoComboBox() {
        super();
        String[] list = CaptureUtils.cameraList();
        if (list.length <= 0) {
            LogScroll.log(this.getClass(), "None Camera Found.");
            list = new String[]{"未找到视频采集设备"};
        }
        String[] array = new String[list.length + 1];
        array[0] = "请选择摄像头";
        System.arraycopy(list, 0, array, 1, list.length);
        this.setModel(new DefaultComboBoxModel<>(array));

        //切换下拉框选项时的动作
        this.addItemListener((itemEvent) -> {
            //关闭原来index的摄像头，开启现在index的摄像头
            System.out.println(itemEvent);
            int stateChange = itemEvent.getStateChange();
            //切换的摄像头
            if (ItemEvent.SELECTED == stateChange) {
                VideoComboBox selected = (VideoComboBox)itemEvent.getSource();
                int i = selected.getSelectedIndex() - 1;
                if (i != currentIndex) {
                    CameraList cameraList = map.get(currentIndex);
                    if (cameraList != null) {
                        cameraList.onClose();
                        MainGui.jpanel.remove(cameraList.getLabel());
                    }
                    currentIndex = i;
                    if (i >= 0) {
                        showImage(i);
                    }
                }
            }
        });

        LogScroll.log(this.getClass(), "视频采集下拉框初始化成功");
    }

    public void showImage(int deviceIndex) {
        CameraList camera = map.get(deviceIndex);
        if (camera == null) {
            camera = new CameraList();
            JLabel label = camera.getLabel();
            int x = this.getX();
            int y = this.getY();
            label.setBounds(x, y, 400, 400);
            map.put(deviceIndex, camera);
        }
        MainGui.jpanel.add(camera.getLabel());
        MainGui.jpanel.updateUI();
        MainGui.jpanel.repaint();
        camera.onOpen(deviceIndex);
        camera.playVideoOnLabel();
    }

    public void onClose() {
        CameraList cameraList = map.get(currentIndex);
        if (cameraList != null) {
            cameraList.onClose();
            MainGui.jpanel.remove(cameraList.getLabel());
            MainGui.jpanel.updateUI();
            MainGui.jpanel.repaint();
        }
    }
}
