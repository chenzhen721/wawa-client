package com.wawa.common.component;

public interface EventObserver<T> {

    public void fireEvent(Event<T> event);

    public void addListener(EventListener<T> listener);

}
