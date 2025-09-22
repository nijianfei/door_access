package com.csc.door.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CmdParamDto {
    /*设备SN*/
    private long devSn = 0;
    /*门号 1-4*/
    private int doorNo;
    /*卡号*/
    private long card;
    /*生效日期*/
    private long effectiveDate;
    /*失效日期*/
    private long expiryDate;
    /*进出标志*/
    private int inOrout;
    /*控制方式 1-常开;2-常闭;3-在线控制*/
    private int controlType;
    /*开门延时 缺省 3秒*/
    private int doorOpeningDelay;

    /*时钟同步时间*/
    private Date currentTime;

    /*控制器索引号*/
    private long recordNo;

    private String deviceIp;
    private String serverIp;
    private String serverPort;
    private String subnetMask;
    private String gateway;
    /*控制器上报间隔(超时判定)*/
    private String clientTimeOut;

    /*服务端下发间隔(超时判定)*/
    private String serverTimeOut;

    /*服务端控制*/
    private String serverControl;

}
