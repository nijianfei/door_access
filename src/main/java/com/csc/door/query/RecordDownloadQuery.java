package com.csc.door.query;

import lombok.Data;

@Data
public class RecordDownloadQuery {


    //起始时间  2010-01-01
    private String recordDateFrom;
    //截止时间  2010-01-01
    private String recordDateTo;
}
