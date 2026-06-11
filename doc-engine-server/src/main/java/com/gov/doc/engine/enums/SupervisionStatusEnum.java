package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum SupervisionStatusEnum {

    GENERATED("generated", "已生成"),
    NOTIFIED("notified", "已通知"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    CLOSED("closed", "已关闭");

    private final String code;
    private final String name;

    SupervisionStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (SupervisionStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
