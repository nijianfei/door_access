package com.csc.door.enums;

public enum ResultStatusEnum {
    SUCCESS("70"),
    FAILURE("90"),
    ;

    public final String code;

    ResultStatusEnum(String code) {
        this.code = code;
    }
}
