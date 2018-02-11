package com.chen.common.component;

/**
 * 事件传递
 * @param <T>
 */
public class Event<T> {

    private T msg;

    public Event(T t) {
        this.msg = t;
    }

    public T getMsg() {
        return this.msg;
    }

}
