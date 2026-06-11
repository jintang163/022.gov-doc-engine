package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum UrgeStatusEnum {

    SENT("sent", "已发送"),
    ACKNOWLEDGED("acknowledged", "已确认");

    private final String code;
    private final String name;

    UrgeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (UrgeStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
