package com.csc.door.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessRecordResponse{

    private String recordTimestamp;
    private String controllerIp;
    private int controllerNo;
    private String recordSrc;
    private String recordTypeCls;
    private String recordDetail;
    private String cardId;
    private String sInorOut;

    //70 : 成功；90 : 失败
    private String resultCls;
    //文字描述
    private String resultDetail;
}
