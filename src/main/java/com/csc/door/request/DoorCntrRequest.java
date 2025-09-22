package com.csc.door.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoorCntrRequest {


    //控制器IP
    private String controllerIp;
    //门号  取值为1，2，3，4
    private String controllerNo;
    //[卡号]  可选值，卡号取值为整数值，若不传卡号，管理员直接开门，卡号值设置为888888
    private String cardId;
    //[标记]  进出门标记1为进门，2为出门，不输入缺省为进门
    private int inOrout;

    private long sn;
}
