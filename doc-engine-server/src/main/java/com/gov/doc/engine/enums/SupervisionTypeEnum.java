package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum SupervisionTypeEnum {

    TIMEOUT("timeout", "超时督办"),
    URGE_OVERDUE("urge_overdue", "催办超标督办");

    private final String code;
    private final String name;

    SupervisionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (SupervisionTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
