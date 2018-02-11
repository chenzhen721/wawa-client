package com.chen.common.component;

import com.chen.model.ComRequest;
import com.chen.model.ComResponse;

public interface Command {
    public ComResponse execute(ComRequest comRequest);
}
