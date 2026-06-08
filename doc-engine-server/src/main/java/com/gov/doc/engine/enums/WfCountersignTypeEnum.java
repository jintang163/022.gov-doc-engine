package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfCountersignTypeEnum {

    PARALLEL("parallel", "并行会签"),
    SEQUENTIAL("sequential", "顺序会签");

    private final String code;
    private final String name;

    WfCountersignTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfCountersignTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
