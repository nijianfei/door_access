package com.csc.door.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CardPermTime {
    private String permitTimeFrom;
    private String permitTimeTo;
    private String weekendLimitCls;

    private List<String> cardIdList;
}
