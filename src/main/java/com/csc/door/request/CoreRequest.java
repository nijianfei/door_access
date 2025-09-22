package com.csc.door.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoreRequest {
    private String controllerIp;
    private String controllerNo;
    private String doorName;
    private String triggerTime;
    private String inOrout;
    private String cardId;
    private String qrcode;

}
