package com.csc.door.enums;

public enum RecordInOrOutEnum {
    IN(1,"进门"),
    OUT(2,"出门"),

    ;

    RecordInOrOutEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;

}
