package com.csc.door.query;

public class RecordDownloadQuery {


    //起始时间  2010-01-01
    private String recordDateFrom;
    //截止时间  2010-01-01
    private String recordDateTo;

    public String getRecordDateFrom() {
        return recordDateFrom;
    }

    public void setRecordDateFrom(String recordDateFrom) {
        this.recordDateFrom = recordDateFrom;
    }

    public String getRecordDateTo() {
        return recordDateTo;
    }

    public void setRecordDateTo(String recordDateTo) {
        this.recordDateTo = recordDateTo;
    }
}
