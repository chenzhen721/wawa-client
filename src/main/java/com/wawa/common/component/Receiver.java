package com.wawa.common.component;

import com.wawa.model.ComRequest;
import com.wawa.model.ComResponse;

public interface Receiver {

    public ComResponse action(ComRequest request, int timeout);

}
