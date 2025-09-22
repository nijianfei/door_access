package com.csc.door.response;

import lombok.Data;

@Data
public class ClockSyncResponse{
    private String controllerIp;

    //70 : 成功；90 : 失败
    private String resultCls;
    //文字描述
    private String resultDetail;
}
