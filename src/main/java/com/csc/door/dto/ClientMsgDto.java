package com.csc.door.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientMsgDto {
    private long recordIndex;
    //0=无记录
    //1=刷卡记录
    //2=门磁,按钮, 设备启动, 远程开门记录
    //3=报警记录	1
    private int recordType;

    //有效性(0 表示不通过, 1表示通过)
    private int recordValid;

    private int recordDoorNO;

    //进门/出门(1表示进门, 2表示出门)
    private int recordInOrOut;

    private long recordCardNO;

    private String recordTime;

    private int reason;

    private String recordDetail;

    private String qrCode;
}
