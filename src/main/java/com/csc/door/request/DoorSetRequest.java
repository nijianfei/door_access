package com.csc.door.request;

public class DoorSetRequest {

    //控制器IP
    private String controllerIp;
    //门号
    private String controllerNo;
    //设置值
    private String controllerOprcls;
    //[开门延时]（秒）
    private String controllerDelay;

    public String getControllerIp() {
        return controllerIp;
    }

    public void setControllerIp(String controllerIp) {
        this.controllerIp = controllerIp;
    }

    public String getControllerNo() {
        return controllerNo;
    }

    public void setControllerNo(String controllerNo) {
        this.controllerNo = controllerNo;
    }

    public String getControllerOprcls() {
        return controllerOprcls;
    }

    public void setControllerOprcls(String controllerOprcls) {
        this.controllerOprcls = controllerOprcls;
    }

    public String getControllerDelay() {
        return controllerDelay;
    }

    public void setControllerDelay(String controllerDelay) {
        this.controllerDelay = controllerDelay;
    }
}
