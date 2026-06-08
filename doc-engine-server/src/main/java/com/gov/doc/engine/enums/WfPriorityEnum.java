package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfPriorityEnum {

    NORMAL(0, "普通"),
    HIGH(1, "高"),
    URGENT(2, "紧急");

    private final Integer code;
    private final String name;

    WfPriorityEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (WfPriorityEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return "普通";
    }
}
