package com.csc.door.enums;

public enum RecordValidEnum {
    pass(1,"通过"),
    forbidden(0,"禁止"),

    ;

    RecordValidEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;
}
