package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocHandlingStatusEnum {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String name;

    DocHandlingStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocHandlingStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
