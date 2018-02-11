package com.chen.serialport.component;

import com.chen.common.component.Event;
import com.chen.common.component.EventListener;
import com.chen.common.component.EventObserver;

import java.util.ArrayList;
import java.util.List;

public class ReceiveDataObserver implements EventObserver<String> {

    private List<EventListener<String>> receiveDataListeners;

    public ReceiveDataObserver() {
    }

    public ReceiveDataObserver(EventListener<String> eventListener) {
        if (this.receiveDataListeners == null) {
            this.receiveDataListeners = new ArrayList<>();
        }
        this.receiveDataListeners.add(eventListener);
    }

    @Override
    public void fireEvent(Event<String> event) {
        if (receiveDataListeners.size() <= 0) {
            return;
        }
        for(EventListener<String> receiveDataListener: receiveDataListeners) {
            receiveDataListener.onActive(event);
        }
    }

    public void addListener(EventListener<String> listener) {
        if (this.receiveDataListeners == null) {
            this.receiveDataListeners = new ArrayList<>();
        }
        this.receiveDataListeners.add(listener);
    }

}
