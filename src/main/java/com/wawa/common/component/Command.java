package com.wawa.common.component;

import com.wawa.model.ComRequest;
import com.wawa.model.ComResponse;

public interface Command {
    public ComResponse execute(ComRequest comRequest);
}
