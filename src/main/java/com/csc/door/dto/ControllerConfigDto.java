package com.csc.door.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;

@XmlType(propOrder = {
        "controllerNo",
        "sn",
        "ip",
        "subnetMask",
        "gateway",
        "serverIp",
        "serverPort",
        "door1",
        "door2",
        "door3",
        "door4"
})
@Builder
public class ControllerConfigDto {
    public ControllerConfigDto() {
    }

    public ControllerConfigDto(String controllerNo, long sn, String ip, String subnetMask, String gateway, String serverIp,
                               String serverPort,String door1, String door2, String door3, String door4) {
        this.controllerNo = controllerNo;
        this.sn = sn;
        this.ip = ip;
        this.subnetMask = subnetMask;
        this.gateway = gateway;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.door1 = door1;
        this.door2 = door2;
        this.door3 = door3;
        this.door4 = door4;
    }

    private String controllerNo;
    private long sn;

    private String ip;

    private String subnetMask;

    private String gateway;

    private String serverIp;

    private String serverPort;

    private String door1;
    private String door2;
    private String door3;
    private String door4;

    @XmlElement(name = "控制器编号")
    public String getControllerNo() {
        return controllerNo;
    }

    public void setControllerNo(String controllerNo) {
        this.controllerNo = controllerNo;
    }

    @XmlElement(name = "控制器序列号")
    public long getSn() {
        return sn;
    }

    public void setSn(long sn) {
        this.sn = sn;
    }

    @XmlElement(name = "控制器IP")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @XmlElement(name = "子网掩码")
    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    @XmlElement(name = "网关")
    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @XmlElement(name = "服务器IP")
    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    @XmlElement(name = "服务器Port")
    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    @XmlElement(name = "门1")
    public String getDoor1() {
        return door1;
    }

    public void setDoor1(String door1) {
        this.door1 = door1;
    }

    @XmlElement(name = "门2")
    public String getDoor2() {
        return door2;
    }

    public void setDoor2(String door2) {
        this.door2 = door2;
    }

    @XmlElement(name = "门3")
    public String getDoor3() {
        return door3;
    }

    public void setDoor3(String door3) {
        this.door3 = door3;
    }

    @XmlElement(name = "门4")
    public String getDoor4() {
        return door4;
    }

    public void setDoor4(String door4) {
        this.door4 = door4;
    }

    public String getDoorName(int no){
        switch (no){
            case 1:
                return door1;
            case 2:
                return door2;
            case 3:
                return door3;
            case 4:
                return door4;
            default:
                throw new RuntimeException("不存在的端口号:" + no);
        }
    }
}
