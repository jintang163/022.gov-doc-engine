package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfTaskStatusEnum {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    DELEGATED("delegated", "已转办"),
    CANCELED("canceled", "已取消");

    private final String code;
    private final String name;

    WfTaskStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfTaskStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
