package com.wawa.common.component;

public interface EventListener<T> {

    public void onActive(Event<T> event);

}
