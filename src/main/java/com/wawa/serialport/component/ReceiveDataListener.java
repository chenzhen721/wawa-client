package com.wawa.serialport.component;

import com.wawa.common.component.Event;
import com.wawa.common.component.EventListener;

public class ReceiveDataListener implements EventListener<String> {

    @Override
    public void onActive(Event<String> event) {
        System.out.println(event.getMsg());
    }
}
