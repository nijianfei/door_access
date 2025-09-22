package com.csc.door.request;

public class LightCntrRequest {

    private String controllerIp	;
    private int controllerNo	;
    private int inOrout;
    private int colourCls;

    public LightCntrRequest() {
    }

    public LightCntrRequest(String controllerIp, int controllerNo, int inOrout, int colourCls) {
        this.controllerIp = controllerIp;
        this.controllerNo = controllerNo;
        this.inOrout = inOrout;
        this.colourCls = colourCls;
    }

    public String getControllerIp() {
        return controllerIp;
    }

    public void setControllerIp(String controllerIp) {
        this.controllerIp = controllerIp;
    }

    public int getControllerNo() {
        return controllerNo;
    }

    public void setControllerNo(int controllerNo) {
        this.controllerNo = controllerNo;
    }

    public int getInOrout() {
        return inOrout;
    }

    public void setInOrout(int inOrout) {
        this.inOrout = inOrout;
    }

    public int getColourCls() {
        return colourCls;
    }

    public void setColourCls(int colourCls) {
        this.colourCls = colourCls;
    }
}
