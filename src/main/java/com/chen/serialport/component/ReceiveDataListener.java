package com.chen.serialport.component;

import com.chen.common.component.Event;
import com.chen.common.component.EventListener;

public class ReceiveDataListener implements EventListener<String> {

    @Override
    public void onActive(Event<String> event) {
        System.out.println(event.getMsg());
    }
}
