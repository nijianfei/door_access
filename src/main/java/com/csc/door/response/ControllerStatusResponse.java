package com.csc.door.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ControllerStatusResponse {

    private String controllerIp;
    private String controllerNo;
    private String doorId;
    private String controllerStatusCls;
    private String doorLock;
    private String doorBtn;
    private String doorOprcls;
}
