package com.chen.common.component;

import com.chen.model.ComRequest;
import com.chen.model.ComResponse;

public interface Receiver {

    public ComResponse action(ComRequest request, int timeout);

}
