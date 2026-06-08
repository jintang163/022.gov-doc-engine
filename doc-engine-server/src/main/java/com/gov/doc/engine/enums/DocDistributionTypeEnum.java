package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocDistributionTypeEnum {

    ELECTRONIC("electronic", "电子传输"),
    PRINT("print", "打印分发"),
    BOTH("both", "电子+打印");

    private final String code;
    private final String name;

    DocDistributionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocDistributionTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
