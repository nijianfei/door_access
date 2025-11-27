package com.csc.door.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PermByIpAndNoResponse {
    private String controllerIp;
    private String controllerNo;
    private List<CardPermTime> cardpermTimeList = new ArrayList<>();
}
