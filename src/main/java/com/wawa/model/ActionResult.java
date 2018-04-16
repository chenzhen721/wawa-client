package com.wawa.model;

/**
 * Created by Administrator on 2018/4/16.
 */
public class ActionResult {

    private String action_type;

    private String result;

    private String log_id;

    public String getAction_type() {
        return action_type;
    }

    public void setAction_type(String action_type) {
        this.action_type = action_type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }
}
