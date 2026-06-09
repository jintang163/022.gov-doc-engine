package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocIncomingStatusEnum {

    REGISTERED("registered", "已登记"),
    PROPOSED("proposed", "已拟办"),
    HANDLING("handling", "承办中"),
    COMPLETED("completed", "已办结"),
    TRANSFERRED("transferred", "已转发");

    private final String code;
    private final String name;

    DocIncomingStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocIncomingStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
